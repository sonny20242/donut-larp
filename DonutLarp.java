
package com.example.donutlarp;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import org.lwjgl.glfw.GLFW;

public class DonutLarp implements ClientModInitializer {

    private static final int PINK = 0xFFFF8FC8;
    private static final int CREAM = 0xFFFFF1E0;
    private static final int MUTED = 0xFF9C8579;

    private static final Identifier BOARD_LAYER =
            Identifier.of("donutlarp", "board");

    private static KeyBinding menuKey;

    @Override
    public void onInitializeClient() {

        DonutConfig.get();

        // Right Shift menu key
        KeyBinding.Category category =
                KeyBinding.Category.create(
                        Identifier.of("donutlarp", "main")
                );

        menuKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.donutlarp.menu",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_RIGHT_SHIFT,
                        category
                )
        );

        // Open the menu
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.wasPressed()) {
                client.setScreen(new LarpScreen());
            }
        });

        // Locally replace your name and rank in incoming messages
        ClientReceiveMessageEvents.MODIFY_GAME.register(
                (message, overlay) -> {
                    if (overlay) {
                        return message;
                    }

                    return NameSwap.swap(message);
                }
        );

        // Handle /pay without blocking the real server command
        ClientSendMessageEvents.ALLOW_COMMAND.register(command -> {

            DonutConfig config = DonutConfig.get();

            if (!DonutConfig.active() || !config.localPay) {
                return true;
            }

            String cleaned = command.trim();

            // Support commands both with and without a leading slash
            if (cleaned.startsWith("/")) {
                cleaned = cleaned.substring(1);
            }

            String[] parts = cleaned.split("\\s+");

            if (parts.length < 3) {
                return true;
            }

            if (!parts[0].equalsIgnoreCase("pay")) {
                return true;
            }

            // Read the amount being paid
            double amount = DonutConfig.parseAmount(parts[2]);

            if (Double.isNaN(amount) || amount <= 0) {
                return true;
            }

            // Update the local displayed balance
            config.money = Math.max(0, config.money - amount);

            DonutConfig.save();

            // Clearly mark this as a local estimate
            MinecraftClient client = MinecraftClient.getInstance();

            if (client.inGameHud != null) {
                client.inGameHud.setOverlayMessage(
                        Text.literal(
                                "[Donut Larp] Local estimated balance: $"
                                        + DonutConfig.fmt(config.money)
                        ),
                        false
                );
            }

            // IMPORTANT:
            // Always allow the original command to reach the server.
            // DonutSMP decides whether the payment actually succeeds.
            return true;
        });

        // Register the custom scoreboard HUD
        HudLayerRegistrationCallback.EVENT.register(layeredDrawer ->
                layeredDrawer.attachLayerAfter(
                        IdentifiedLayer.MISC_OVERLAYS,
                        BOARD_LAYER,
                        DonutLarp::renderBoard
                )
        );
    }

    // ---------------- HUD SCOREBOARD ----------------

    private static void renderBoard(
            DrawContext ctx,
            RenderTickCounter tick
    ) {

        MinecraftClient mc = MinecraftClient.getInstance();

        DonutConfig config = DonutConfig.get();

        if (mc.player == null
                || mc.options.hudHidden
                || !DonutConfig.active()
                || !config.fakeBoard) {
            return;
        }

        // Get real player ping
        PlayerListEntry me =
                mc.player.networkHandler.getPlayerListEntry(
                        mc.player.getUuid()
                );

        int ping = me != null ? me.getLatency() : 0;

        // Scoreboard labels
        String[] labels = {
                "Money",
                "Shards",
                "Kills",
                "Deaths",
                "Playtime",
                "Ping"
        };

        String[] values = {
                "$" + DonutConfig.fmt(config.money),
                DonutConfig.fmt(config.shards),
                String.valueOf(config.kills),
                String.valueOf(config.deaths),
                config.playtime,
                ping + "ms"
        };

        int[] colors = {
                0xFFFFD36B,
                0xFF7DE3FF,
                0xFFFF7A7A,
                0xFFC9B8B0,
                CREAM,
                0xFF8CE99A
        };

        int width = 128;
        int lineHeight = 11;

        int height = 10 + 14 + 14
                + labels.length * lineHeight + 6;

        int x = ctx.getScaledWindowWidth() - width - 6;

        int y = (ctx.getScaledWindowHeight() - height) / 2;

        // Main panel
        ctx.fill(
                x,
                y,
                x + width,
                y + height,
                0xB0140D10
        );

        // Pink side stripe
        ctx.fill(
                x,
                y,
                x + 2,
                y + height,
                PINK
        );

        // Top highlight
        ctx.fill(
                x + 2,
                y,
                x + width,
                y + 1,
                0x55FF8FC8
        );

        var renderer = mc.textRenderer;

        int currentY = y + 7;

        // DonutSMP title
        ctx.drawTextWithShadow(
                renderer,
                "DONUT",
                x + 8,
                currentY,
                PINK
        );

        ctx.drawTextWithShadow(
                renderer,
                "SMP",
                x + 8 + renderer.getWidth("DONUT "),
                currentY,
                CREAM
        );

        // Local preview label
        String localLabel = "LOCAL";

        ctx.drawTextWithShadow(
                renderer,
                localLabel,
                x + width - 8 - renderer.getWidth(localLabel),
                currentY,
                MUTED
        );

        currentY += 14;

        // Display name and optional fake rank
        int nameX = x + 8;

        if (config.rankIndex() != 0) {

            String rankTag = "[" + config.rank + "] ";

            ctx.drawTextWithShadow(
                    renderer,
                    rankTag,
                    nameX,
                    currentY,
                    0xFF000000 | config.rankColor()
            );

            nameX += renderer.getWidth(rankTag);
        }

        ctx.drawTextWithShadow(
                renderer,
                NameSwap.displayName(),
                nameX,
                currentY,
                CREAM
        );

        currentY += 14;

        // Draw scoreboard statistics
        for (int i = 0; i < labels.length; i++) {

            ctx.drawTextWithShadow(
                    renderer,
                    labels[i],
                    x + 8,
                    currentY,
                    MUTED
            );

            ctx.drawTextWithShadow(
                    renderer,
                    values[i],
                    x + width - 8
                            - renderer.getWidth(values[i]),
                    currentY,
                    colors[i]
            );

            currentY += lineHeight;
        }
    }
}
