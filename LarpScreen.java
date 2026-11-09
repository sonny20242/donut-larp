package com.example.donutlarp;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class LarpScreen extends Screen {
    static final int PINK = 0xFFFF8FC8, ORANGE = 0xFFFFB86B, CREAM = 0xFFFFF1E0, MUTED = 0xFF9C8579;
    static final int PANEL = 0xF01E1418, FIELD = 0xFF120C0F, LINE = 0xFF3A2A30;
    private static final int W = 320, H = 248;

    private int px, py, tab;
    private final List<TextFieldWidget> fields = new ArrayList<>();
    private final List<Object[]> labels = new ArrayList<>(); // {String text, Integer x, Integer y, Integer color}

    public LarpScreen() { super(Text.literal("Donut Larp")); }

    // ---------- layout ----------

    @Override
    protected void init() {
        px = (width - W) / 2;
        py = (height - H) / 2;
        fields.clear();
        labels.clear();

        String[] names = {"Stats", "Identity", "Options"};
        for (int i = 0; i < names.length; i++) {
            final int idx = i;
            FlatButton b = new FlatButton(px + 16 + i * 74, py + 36, 68, 18, names[i], btn -> { tab = idx; clearAndInit(); });
            b.selected = tab == i;
            addDrawableChild(b);
        }

        switch (tab) {
            case 0 -> statsTab();
            case 1 -> identityTab();
            default -> optionsTab();
        }

        addDrawableChild(new FlatButton(px + 16, py + H - 30, 84, 18, "Reset", b -> { DonutConfig.reset(); clearAndInit(); }));
        FlatButton done = new FlatButton(px + W - 16 - 84, py + H - 30, 84, 18, "Done", b -> close());
        done.selected = true;
        addDrawableChild(done);
    }

    private void statsTab() {
        DonutConfig c = DonutConfig.get();
        int y = py + 68;
        row("Money", y);
        field(y, DonutConfig.fmt(c.money), "e.g. 1.5m", s -> { double v = DonutConfig.parseAmount(s); if (!Double.isNaN(v)) c.money = v; });
        y += 28; row("Shards", y);
        field(y, DonutConfig.fmt(c.shards), "e.g. 500", s -> { double v = DonutConfig.parseAmount(s); if (!Double.isNaN(v)) c.shards = v; });
        y += 28; row("Kills", y);
        field(y, String.valueOf(c.kills), "0", s -> { try { c.kills = Math.max(0, Integer.parseInt(s.trim())); } catch (NumberFormatException ignored) {} });
        y += 28; row("Deaths", y);
        field(y, String.valueOf(c.deaths), "0", s -> { try { c.deaths = Math.max(0, Integer.parseInt(s.trim())); } catch (NumberFormatException ignored) {} });
        y += 28; row("Playtime", y);
        field(y, c.playtime, "e.g. 3d 14h", s -> c.playtime = s);
    }

    private void identityTab() {
        DonutConfig c = DonutConfig.get();
        int y = py + 68;
        row("Display name", y);
        TextFieldWidget f = field(y, c.displayName == null ? "" : c.displayName,
                MinecraftClient.getInstance().getSession().getUsername(), s -> c.displayName = s.trim());
        f.setMaxLength(20);
        label("Blank uses your real name. Only you see this.", px + 20, y + 24, MUTED);

        y += 44; row("Rank", y);
        FlatButton rank = new FlatButton(px + 132, y, 168, 20, c.rank, btn -> {
            int next = (c.rankIndex() + 1) % DonutConfig.RANKS.length;
            c.rank = DonutConfig.RANKS[next];
            btn.setMessage(Text.literal(c.rank));
            ((FlatButton) btn).textColor = 0xFF000000 | c.rankColor();
        });
        rank.textColor = 0xFF000000 | c.rankColor();
        addDrawableChild(rank);

        y += 28; toggle(y, "Rank tag in chat & tab", () -> c.rankTag, v -> c.rankTag = v);
        y += 28; toggle(y, "Swap name in chat & tab", () -> c.nameSwap, v -> c.nameSwap = v);
    }

    private void optionsTab() {
        DonutConfig c = DonutConfig.get();
        int y = py + 68;
        toggle(y, "Enabled", () -> c.enabled, v -> c.enabled = v);
        y += 28; toggle(y, "Fake scoreboard", () -> c.fakeBoard, v -> c.fakeBoard = v);
        y += 28; toggle(y, "Local pay / withdraw", () -> c.localPay, v -> c.localPay = v);
        y += 28; toggle(y, "DonutSMP only", () -> c.onlyDonut, v -> c.onlyDonut = v);
        label("Local pay: /pay and /withdraw only change your display.", px + 20, y + 28, MUTED);
        label("Off = the command is also sent to the server.", px + 20, y + 40, MUTED);
    }

    // ---------- widget helpers ----------

    private void row(String text, int y) { label(text, px + 20, y + 6, CREAM); }

    private void label(String text, int x, int y, int color) { labels.add(new Object[]{text, x, y, color}); }

    private TextFieldWidget field(int y, String value, String placeholder, Consumer<String> onChange) {
        TextFieldWidget f = new TextFieldWidget(textRenderer, px + 136, y + 4, 160, 12, Text.empty());
        f.setDrawsBackground(false);
        f.setMaxLength(32);
        f.setEditableColor(CREAM);
        f.setPlaceholder(Text.literal(placeholder).formatted(Formatting.DARK_GRAY));
        f.setText(value);
        f.setChangedListener(onChange);
        fields.add(f);
        addDrawableChild(f);
        return f;
    }

    private void toggle(int y, String text, BooleanSupplier get, Consumer<Boolean> set) {
        row(text, y);
        FlatButton b = new FlatButton(px + 232, y, 68, 20, get.getAsBoolean() ? "ON" : "OFF", btn -> {
            boolean v = !get.getAsBoolean();
            set.accept(v);
            btn.setMessage(Text.literal(v ? "ON" : "OFF"));
            ((FlatButton) btn).selected = v;
        });
        b.selected = get.getAsBoolean();
        addDrawableChild(b);
    }

    // ---------- drawing ----------

    @Override
    public void renderBackground(DrawContext c, int mx, int my, float delta) {
        super.renderBackground(c, mx, my, delta);

        // panel + glaze stripe
        c.fill(px - 1, py - 1, px + W + 1, py + H + 1, 0xFF3A2A30);
        c.fill(px, py, px + W, py + H, PANEL);
        c.fill(px, py, px + W / 2, py + 3, PINK);
        c.fill(px + W / 2, py, px + W, py + 3, ORANGE);

        // title
        c.drawTextWithShadow(textRenderer, Text.literal("Donut").formatted(Formatting.BOLD), px + 16, py + 12, PINK);
        c.drawTextWithShadow(textRenderer, Text.literal("Larp").formatted(Formatting.BOLD), px + 16 + textRenderer.getWidth("Donut "), py + 12, CREAM);
        String sub = "visual only - client side";
        c.drawTextWithShadow(textRenderer, sub, px + W - 16 - textRenderer.getWidth(sub), py + 13, MUTED);

        // dividers
        c.fill(px + 16, py + 60, px + W - 16, py + 61, LINE);
        c.fill(px + 16, py + H - 40, px + W - 16, py + H - 39, LINE);

        // input boxes
        for (TextFieldWidget f : fields) {
            int x = f.getX() - 4, y = f.getY() - 4, w = f.getWidth() + 8, h = 20;
            c.fill(x - 1, y - 1, x + w + 1, y + h + 1, f.isFocused() ? PINK : LINE);
            c.fill(x, y, x + w, y + h, FIELD);
        }

        // labels
        for (Object[] l : labels) {
            c.drawTextWithShadow(textRenderer, (String) l[0], (Integer) l[1], (Integer) l[2], (Integer) l[3]);
        }
    }

    @Override
    public void removed() { DonutConfig.save(); }

    // ---------- flat button ----------

    static class FlatButton extends ButtonWidget {
        boolean selected;
        int textColor = CREAM;

        FlatButton(int x, int y, int w, int h, String label, PressAction action) {
            super(x, y, w, h, net.minecraft.text.Text.literal(label), action, DEFAULT_NARRATION_SUPPLIER);
        }
public void renderWidget(DrawContext c, int mx, int my, float delta) {
        // In 1.21.11 the button background is drawn by the parent class, then this is called.
        // We cover it with our own flat style.
        @Override
        
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();
            int bg = selected ? PINK : (isHovered() ? 0xFF3A2A30 : 0xFF2A1D22);
            c.fill(x, y, x + w, y + h, bg);
            if (!selected) {
                c.fill(x, y, x + w, y + 1, LINE);
                c.fill(x, y + h - 1, x + w, y + h, LINE);
                c.fill(x, y, x + 1, y + h, LINE);
                c.fill(x + w - 1, y, x + w, y + h, LINE);
            }
            int fg = selected ? 0xFF2B1B17 : textColor;
            c.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, getMessage(),
                    x + w / 2, y + (h - 8) / 2, fg);
        }
    }
}
