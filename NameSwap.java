package com.example.donutlarp;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.util.Optional;

/** Replaces your real username in text with your display name (and optional rank tag). */
public final class NameSwap {
    private NameSwap() {}

    public static String displayName() {
        String real = MinecraftClient.getInstance().getSession().getUsername();
        String f = DonutConfig.get().displayName;
        return (f == null || f.trim().isEmpty()) ? real : f.trim();
    }

    public static Text swap(Text in) {
        DonutConfig c = DonutConfig.get();
        if (!DonutConfig.active() || !c.nameSwap) return in;

        final String real = MinecraftClient.getInstance().getSession().getUsername();
        final String fake = displayName();
        final boolean tag = c.rankTag && c.rankIndex() != 0;
        if (real == null || real.isEmpty()) return in;
        if (fake.equals(real) && !tag) return in;
        if (!in.getString().contains(real)) return in;

        final Style tagStyle = Style.EMPTY.withColor(TextColor.fromRgb(c.rankColor()));
        final String tagText = "[" + c.rank + "] ";
        final MutableText out = Text.empty();

        in.visit((style, str) -> {
            int from = 0, idx;
            while ((idx = str.indexOf(real, from)) >= 0) {
                if (idx > from) out.append(Text.literal(str.substring(from, idx)).setStyle(style));
                if (tag) out.append(Text.literal(tagText).setStyle(tagStyle));
                out.append(Text.literal(fake).setStyle(style));
                from = idx + real.length();
            }
            if (from < str.length()) out.append(Text.literal(str.substring(from)).setStyle(style));
            return Optional.empty();
        }, Style.EMPTY);

        return out;
    }
}
