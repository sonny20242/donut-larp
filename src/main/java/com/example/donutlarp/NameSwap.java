package com.example.donutlarp;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Locale;

public final class NameSwap {
    private NameSwap() {}

    public static String getDisplayName() {
        MinecraftClient client = MinecraftClient.getInstance();
        DonutConfig c = DonutLarpClient.CONFIG;
        if (c != null && c.displayName != null && !c.displayName.isBlank()) {
            return c.displayName.trim();
        }
        return client.getSession().getUsername();
    }

    public static MutableText getDisplayText() {
        DonutConfig c = DonutLarpClient.CONFIG;
        MutableText result = Text.empty();
        if (c != null && c.rankTag && c.rank != null && !c.rank.equalsIgnoreCase("None")) {
            result.append(Text.literal("[" + c.rank + "] ").formatted(rankColor(c.rank)));
        }
        result.append(Text.literal(getDisplayName()).formatted(Formatting.WHITE));
        return result;
    }

    public static Text replaceUnsignedText(Text original) {
        MinecraftClient client = MinecraftClient.getInstance();
        DonutConfig c = DonutLarpClient.CONFIG;
        if (original == null || c == null || !c.nameSwap || !c.isAllowedOn(client)) return original;

        String realName = client.getSession().getUsername();
        String shownName = getDisplayName();
        if (realName.equals(shownName) || !original.getString().contains(realName)) return original;

        String changed = original.getString().replace(realName, shownName);
        MutableText replacement = Text.literal(changed);
        replacement.setStyle(original.getStyle());
        return replacement;
    }

    public static Formatting rankColor(String rank) {
        if (rank == null) return Formatting.LIGHT_PURPLE;
        return switch (rank.toLowerCase(Locale.ROOT)) {
            case "vip" -> Formatting.AQUA;
            case "mvp" -> Formatting.GOLD;
            case "elite" -> Formatting.LIGHT_PURPLE;
            case "legend" -> Formatting.RED;
            default -> Formatting.GRAY;
        };
    }
}
