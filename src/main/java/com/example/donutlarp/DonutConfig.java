package com.example.donutlarp;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

/** Persistent settings and local display values. None of these values are sent to a server. */
public final class DonutConfig {
    public static final String[] RANKS = {"None", "VIP", "MVP", "Elite", "Legend"};

    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("donutlarp.properties");

    public boolean enabled = true;
    public boolean fakeBoard = true;
    public boolean localPay = true;
    public boolean onlyDonut = true;
    public boolean nameSwap = true;
    public boolean rankTag = true;

    public double money = 2_500_000;
    public double shards = 500;
    public int kills = 0;
    public int deaths = 0;
    public String playtime = "0d 0h";
    public String displayName = "";
    public String rank = "VIP";

    public static DonutConfig load() {
        DonutConfig config = new DonutConfig();
        if (!Files.exists(FILE)) {
            config.save();
            return config;
        }

        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(FILE)) {
            p.load(in);
            config.enabled = Boolean.parseBoolean(p.getProperty("enabled", "true"));
            config.fakeBoard = Boolean.parseBoolean(p.getProperty("fakeBoard", "true"));
            config.localPay = Boolean.parseBoolean(p.getProperty("localPay", "true"));
            config.onlyDonut = Boolean.parseBoolean(p.getProperty("onlyDonut", "true"));
            config.nameSwap = Boolean.parseBoolean(p.getProperty("nameSwap", "true"));
            config.rankTag = Boolean.parseBoolean(p.getProperty("rankTag", "true"));
            config.money = parseSavedDouble(p.getProperty("money"), 2_500_000);
            config.shards = parseSavedDouble(p.getProperty("shards"), 500);
            config.kills = parseSavedInt(p.getProperty("kills"), 0);
            config.deaths = parseSavedInt(p.getProperty("deaths"), 0);
            config.playtime = p.getProperty("playtime", "0d 0h");
            config.displayName = p.getProperty("displayName", "");
            config.rank = p.getProperty("rank", "VIP");
            if (!isKnownRank(config.rank)) config.rank = "VIP";
        } catch (IOException ignored) {
            // Defaults remain usable if the config file cannot be read.
        }
        return config;
    }

    public void save() {
        Properties p = new Properties();
        p.setProperty("enabled", Boolean.toString(enabled));
        p.setProperty("fakeBoard", Boolean.toString(fakeBoard));
        p.setProperty("localPay", Boolean.toString(localPay));
        p.setProperty("onlyDonut", Boolean.toString(onlyDonut));
        p.setProperty("nameSwap", Boolean.toString(nameSwap));
        p.setProperty("rankTag", Boolean.toString(rankTag));
        p.setProperty("money", Double.toString(money));
        p.setProperty("shards", Double.toString(shards));
        p.setProperty("kills", Integer.toString(kills));
        p.setProperty("deaths", Integer.toString(deaths));
        p.setProperty("playtime", playtime);
        p.setProperty("displayName", displayName);
        p.setProperty("rank", rank);

        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream out = Files.newOutputStream(FILE)) {
                p.store(out, "Donut Larp local display settings - not server data");
            }
        } catch (IOException ignored) {
            // The mod continues to run for this session if disk writes fail.
        }
    }

    public void reset() {
        enabled = true;
        fakeBoard = true;
        localPay = true;
        onlyDonut = true;
        nameSwap = true;
        rankTag = true;
        money = 2_500_000;
        shards = 500;
        kills = 0;
        deaths = 0;
        playtime = "0d 0h";
        displayName = "";
        rank = "VIP";
        save();
    }

    public boolean isAllowedOn(MinecraftClient client) {
        if (!enabled) return false;
        if (!onlyDonut) return true;
        ServerInfo server = client.getCurrentServerEntry();
        return server != null && server.address.toLowerCase(Locale.ROOT).contains("donutsmp");
    }

    public static boolean isKnownRank(String value) {
        for (String rank : RANKS) {
            if (rank.equalsIgnoreCase(value)) return true;
        }
        return false;
    }

    public static double parseAmount(String input) {
        if (input == null) return Double.NaN;
        String s = input.trim().toLowerCase(Locale.ROOT).replace(",", "").replace("$", "");
        if (s.isEmpty()) return Double.NaN;
        double multiplier = 1;
        char last = s.charAt(s.length() - 1);
        if (last == 'k' || last == 'm' || last == 'b') {
            multiplier = switch (last) {
                case 'k' -> 1_000d;
                case 'm' -> 1_000_000d;
                case 'b' -> 1_000_000_000d;
                default -> 1d;
            };
            s = s.substring(0, s.length() - 1);
        }
        try {
            double value = Double.parseDouble(s) * multiplier;
            return Double.isFinite(value) && value >= 0 ? value : Double.NaN;
        } catch (NumberFormatException ignored) {
            return Double.NaN;
        }
    }

    public static String formatAmount(double value) {
        if (!Double.isFinite(value)) return "0";
        if (value >= 1_000_000_000d) return compact(value / 1_000_000_000d) + "b";
        if (value >= 1_000_000d) return compact(value / 1_000_000d) + "m";
        if (value >= 1_000d) return compact(value / 1_000d) + "k";
        return String.format(Locale.ROOT, "%.0f", value);
    }

    private static String compact(double value) {
        if (value >= 100 || Math.abs(value - Math.rint(value)) < 0.0001) {
            return String.format(Locale.ROOT, "%.0f", value);
        }
        if (value >= 10) return String.format(Locale.ROOT, "%.1f", value);
        return String.format(Locale.ROOT, "%.2f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    private static double parseSavedDouble(String value, double fallback) {
        try {
            double parsed = Double.parseDouble(value);
            return Double.isFinite(parsed) && parsed >= 0 ? parsed : fallback;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static int parseSavedInt(String value, int fallback) {
        try {
            return Math.max(0, Integer.parseInt(value));
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
