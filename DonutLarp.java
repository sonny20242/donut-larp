package com.example.donutlarp;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class DonutLarp implements ClientModInitializer {
    private static final int PINK = 0xFFFF8FC8, CREAM = 0xFFFFF1E0, MUTED = 0xFF9C8579;
    private static KeyBinding menuKey;

    @Override
    public void onInitializeClient() {
        DonutConfig.get();

        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("donutlarp", "main"));
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.donutlarp.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (menuKey.wasPressed()) mc.setScreen(new LarpScreen());
        });

        // Name + rank swap in incoming chat/system messages
        ClientReceiveMessageEvents.MODIFY_GAME.register((msg, overlay) -> overlay ? msg : NameSwap.swap(msg));

        // /pay <user> <amount> and /withdraw <amount> take the amount off the displayed balance.
        // localPay ON  = command is NOT sent to the server (display only).
        // localPay OFF = command is sent to the server as normal.
        ClientSendMessageEvents.ALLOW_COMMAND.register(cmd -> {
            if (!DonutConfig.active()) return true;
            String[] t = cmd.trim().split("\\s+");
            if (t.length < 2) return true;
            String name = t[0].toLowerCase();
            boolean pay = name.equals("pay") && t.length >= 3;
            boolean withdraw = name.equals("withdraw");
            if (!pay && !withdraw) return true;
            double amt = DonutConfig.parseAmount(pay ? t[2] : t[1]);
            if (Double.isNaN(amt) || amt <= 0) return true;

            DonutConfig c = DonutConfig.get();
            c.money = Math.max(0, c.money - amt);
            DonutConfig.save();

            String msg = c.localPay
                    ? "[Donut Larp] Local only, not sent to server. Balance: $" + DonutConfig.fmt(c.money)
                    : "Balance: $" + DonutConfig.fmt(c.money);
            MinecraftClient.getInstance().inGameHud.setOverlayMessage(Text.literal(msg), false);
            return !c.localPay;
        });

        HudLayerRegistrationCallback.EVENT.register(layeredDrawer ->
        layeredDrawer.attachLayerAfter(
                IdentifiedLayer.MISC_OVERLAYS,
                Identifier.of("donutlarp", "board"),
                DonutLarp::renderBoard
    }

    private static void renderBoard(DrawContext ctx, RenderTickCounter tick) {
        MinecraftClient mc = MinecraftClient.getInstance();
        DonutConfig c = DonutConfig.get();
        if (mc.player == null || mc.options.hudHidden || !DonutConfig.active() || !c.fakeBoard) return;

        TextRenderer tr = mc.textRenderer;
        PlayerListEntry me = mc.player.networkHandler.getPlayerListEntry(mc.player.getUuid());
        int ping = me != null ? me.getLatency() : 0;

        String[] labels = {"Money", "Shards", "Kills", "Deaths", "Playtime", "Ping"};
        String[] values = {
                "$" + DonutConfig.fmt(c.money), DonutConfig.fmt(c.shards),
                String.valueOf(c.kills), String.valueOf(c.deaths),
                c.playtime, ping + "ms"};
        int[] colors = {0xFFFFD36B, 0xFF7DE3FF, 0xFFFF7A7A, 0xFFC9B8B0, CREAM, 0xFF8CE99A};

        int w = 128, line = 11;
        int h = 10 + 14 + 14 + labels.length * line + 6;
        int x = ctx.getScaledWindowWidth() - w - 6;
        int y = (ctx.getScaledWindowHeight() - h) / 2;

        ctx.fill(x, y, x + w, y + h, 0xB0140D10);
        ctx.fill(x, y, x + 2, y + h, PINK);
        ctx.fill(x + 2, y, x + w, y + 1, 0x55FF8FC8);

        int cy = y + 7;
        ctx.drawTextWithShadow(tr, "DONUT", x + 8, cy, PINK);
        ctx.drawTextWithShadow(tr, "SMP", x + 8 + tr.getWidth("DONUT "), cy, CREAM);
        cy += 14;

        // name line with optional rank tag
        int nx = x + 8;
        if (c.rankIndex() != 0) {
            String tag = "[" + c.rank + "] ";
            ctx.drawTextWithShadow(tr, tag, nx, cy, 0xFF000000 | c.rankColor());
            nx += tr.getWidth(tag);
        }
        ctx.drawTextWithShadow(tr, NameSwap.displayName(), nx, cy, CREAM);
        cy += 14;

        for (int i = 0; i < labels.length; i++) {
            ctx.drawTextWithShadow(tr, labels[i], x + 8, cy, MUTED);
            ctx.drawTextWithShadow(tr, values[i], x + w - 8 - tr.getWidth(values[i]), cy, colors[i]);
            cy += line;
        }
    }
}
