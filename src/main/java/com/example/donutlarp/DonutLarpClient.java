package com.example.donutlarp;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DonutLarpClient implements ClientModInitializer {
    public static final String MOD_ID = "donutlarp";
    public static DonutConfig CONFIG;

    private static final KeyBinding OPEN_MENU = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.donutlarp.open", GLFW.GLFW_KEY_RIGHT_SHIFT, "category.donutlarp")
    );

    private static final Pattern PAY_COMMAND = Pattern.compile(
            "^pay\\s+[A-Za-z0-9_]{1,16}\\s+([0-9]+(?:\\.[0-9]+)?[kmb]?)$",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public void onInitializeClient() {
        CONFIG = DonutConfig.load();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_MENU.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new LarpScreen());
            }
        });

        // Observe /pay locally, update only the local display balance, and let the original command through.
        ClientSendMessageEvents.ALLOW_COMMAND.register(DonutLarpClient::onCommand);

        HudLayerRegistrationCallback.EVENT.register(layers -> layers.attachLayerAfter(
                IdentifiedLayer.MISC_OVERLAYS,
                Identifier.of(MOD_ID, "fake_scoreboard"),
                DonutLarpClient::renderFakeScoreboard
        ));
    }

    public static boolean isActiveForCurrentServer() {
        MinecraftClient client = MinecraftClient.getInstance();
        return CONFIG != null && client != null && CONFIG.isAllowedOn(client);
    }

    private static boolean onCommand(String command) {
        if (CONFIG == null || !isActiveForCurrentServer() || !CONFIG.localPay) return true;

        Matcher matcher = PAY_COMMAND.matcher(command.trim());
        if (!matcher.matches()) return true;

        double amount = DonutConfig.parseAmount(matcher.group(1));
        if (!Double.isFinite(amount) || amount <= 0) return true;

        // This is explicitly a local display estimate, not a server-confirmed transaction.
        CONFIG.money = Math.max(0, CONFIG.money - amount);
        CONFIG.save();

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.inGameHud != null) {
            client.inGameHud.setOverlayMessage(
                    Text.literal("LOCAL BALANCE: $" + DonutConfig.formatAmount(CONFIG.money))
                            .formatted(Formatting.LIGHT_PURPLE),
                    false
            );
        }
        return true;
    }

    private static void renderFakeScoreboard(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (CONFIG == null || !CONFIG.fakeBoard || !isActiveForCurrentServer() || client.player == null) return;
        if (client.currentScreen instanceof LarpScreen) return;

        int panelWidth = 142;
        int panelHeight = 118;
        int x = client.getWindow().getScaledWidth() - panelWidth - 8;
        int y = 8;
        int pink = 0xFFFF8FC8;
        int cream = 0xFFFFF1E0;
        int muted = 0xFFB99FA7;

        context.fill(x - 1, y - 1, x + panelWidth + 1, y + panelHeight + 1, 0xFF4B2938);
        context.fill(x, y, x + panelWidth, y + panelHeight, 0xE91E1418);
        context.fill(x, y, x + panelWidth, y + 3, pink);

        int tx = x + 9;
        int ty = y + 9;
        context.drawTextWithShadow(client.textRenderer, Text.literal("DONUT SMP").formatted(Formatting.BOLD), tx, ty, pink);
        context.drawTextWithShadow(client.textRenderer, Text.literal("LOCAL PROFILE").formatted(Formatting.BOLD), tx, ty + 14, muted);
        context.drawTextWithShadow(client.textRenderer, NameSwap.getDisplayText(), tx, ty + 28, cream);
        context.drawTextWithShadow(client.textRenderer, "Money: $" + DonutConfig.formatAmount(CONFIG.money), tx, ty + 43, cream);
        context.drawTextWithShadow(client.textRenderer, "Shards: " + DonutConfig.formatAmount(CONFIG.shards), tx, ty + 56, cream);
        context.drawTextWithShadow(client.textRenderer, "Kills: " + CONFIG.kills + "  Deaths: " + CONFIG.deaths, tx, ty + 69, cream);
        context.drawTextWithShadow(client.textRenderer, "Playtime: " + CONFIG.playtime, tx, ty + 82, cream);

        PlayerListEntry self = client.getNetworkHandler() == null ? null
                : client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
        String ping = self == null ? "Ping: --" : "Ping: " + MathHelper.clamp(self.getLatency(), 0, 9999) + "ms";
        context.drawTextWithShadow(client.textRenderer, ping, tx, ty + 95, muted);
    }
}
