package com.example.donutlarp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;

import java.nio.file.Files;
import java.nio.file.Path;

public class DonutConfig {
    // Edit these to add or rename ranks (colors are RGB).
    public static final String[] RANKS = {"None", "VIP", "MVP", "Elite", "Legend"};
    public static final int[] RANK_COLORS = {0xFFF1E0, 0x55FF55, 0x55FFFF, 0xFFAA00, 0xFF55FF};

    public boolean enabled = true;
    public boolean fakeBoard = true;
    public boolean onlyDonut = false;
    public boolean localPay = true;
    public boolean nameSwap = true;
    public boolean rankTag = true;
    public String displayName = "";
    public String rank = "None";
    public double money = 1_250_000;
    public double shards = 450;
    public int kills = 37;
    public int deaths = 12;
    public String playtime = "3d 14h";

    private static DonutConfig inst;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("donutlarp.json");
    }

    public static DonutConfig get() {
        if (inst == null) {
            try {
                if (Files.exists(file())) inst = GSON.fromJson(Files.readString(file()), DonutConfig.class);
            } catch (Exception ignored) {}
            if (inst == null) inst = new DonutConfig();
        }
        return inst;
    }

    public static void save() {
        try { Files.writeString(file(), GSON.toJson(get())); } catch (Exception ignored) {}
    }

    public static void reset() {
        inst = new DonutConfig();
        save();
    }

    /** True when the mod should be doing anything right now. */
    public static boolean active() {
        DonutConfig c = get();
        if (!c.enabled) return false;
        if (!c.onlyDonut) return true;
        ServerInfo s = MinecraftClient.getInstance().getCurrentServerEntry();
        return s != null && s.address != null && s.address.toLowerCase().contains("donutsmp");
    }

    public int rankIndex() {
        for (int i = 0; i < RANKS.length; i++) if (RANKS[i].equalsIgnoreCase(rank)) return i;
        return 0;
    }

    public int rankColor() { return RANK_COLORS[rankIndex()]; }

    /** Parses 1500, 1.5k, 2m, 1b, 1t, $1,000. Returns NaN if invalid. */
    public static double parseAmount(String s) {
        if (s == null) return Double.NaN;
        s = s.trim().toLowerCase().replace(",", "").replace("$", "");
        if (s.isEmpty()) return Double.NaN;
        double mult = 1;
        char last = s.charAt(s.length() - 1);
        switch (last) {
            case 'k' -> mult = 1e3;
            case 'm' -> mult = 1e6;
            case 'b' -> mult = 1e9;
            case 't' -> mult = 1e12;
            default -> {}
        }
        if (mult != 1) s = s.substring(0, s.length() - 1);
        try {
            double v = Double.parseDouble(s) * mult;
            return v < 0 ? Double.NaN : v;
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    /** 1250000 -> 1.25M */
    public static String fmt(double v) {
        String[] suf = {"", "K", "M", "B", "T"};
        double[] div = {1, 1e3, 1e6, 1e9, 1e12};
        int i = 0;
        while (i < 4 && v >= div[i + 1]) i++;
        String n = String.format("%.2f", v / div[i]);
        if (n.contains(".")) n = n.replaceAll("0+$", "").replaceAll("\\.$", "");
        return n + suf[i];
    }
}
