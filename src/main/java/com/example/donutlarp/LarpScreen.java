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

public final class LarpScreen extends Screen {
    private static final int PINK = 0xFFFF8FC8;
    private static final int ORANGE = 0xFFFFB86B;
    private static final int CREAM = 0xFFFFF1E0;
    private static final int MUTED = 0xFFB99FA7;
    private static final int PANEL = 0xF01E1418;
    private static final int FIELD = 0xFF120C0F;
    private static final int LINE = 0xFF4B303A;
    private static final int W = 356;
    private static final int H = 286;

    private int px;
    private int py;
    private int tab;
    private final List<TextFieldWidget> fields = new ArrayList<>();
    private final List<Label> labels = new ArrayList<>();

    public LarpScreen() {
        super(Text.literal("Donut Larp"));
    }

    @Override
    protected void init() {
        px = (width - W) / 2;
        py = (height - H) / 2;
        fields.clear();
        labels.clear();

        String[] tabs = {"Stats", "Identity", "Options"};
        for (int i = 0; i < tabs.length; i++) {
            final int index = i;
            FlatButton button = new FlatButton(px + 16 + i * 108, py + 37, 100, 19, tabs[i], b -> {
                tab = index;
                clearAndInit();
            });
            button.selected = tab == i;
            addDrawableChild(button);
        }

        switch (tab) {
            case 0 -> statsTab();
            case 1 -> identityTab();
            default -> optionsTab();
        }

        FlatButton reset = new FlatButton(px + 16, py + H - 30, 86, 18, "Reset", b -> {
            DonutLarpClient.CONFIG.reset();
            clearAndInit();
        });
        addDrawableChild(reset);
        FlatButton done = new FlatButton(px + W - 102, py + H - 30, 86, 18, "Done", b -> close());
        done.selected = true;
        addDrawableChild(done);
    }

    private void statsTab() {
        DonutConfig c = DonutLarpClient.CONFIG;
        int y = py + 70;
        row("Money", y);
        field(y, DonutConfig.formatAmount(c.money), "e.g. 1.5m", s -> {
            double v = DonutConfig.parseAmount(s);
            if (Double.isFinite(v)) c.money = v;
        });
        y += 29;
        row("Shards", y);
        field(y, DonutConfig.formatAmount(c.shards), "e.g. 500", s -> {
            double v = DonutConfig.parseAmount(s);
            if (Double.isFinite(v)) c.shards = v;
        });
        y += 29;
        row("Kills", y);
        field(y, Integer.toString(c.kills), "0", s -> c.kills = parseNonNegativeInt(s, c.kills));
        y += 29;
        row("Deaths", y);
        field(y, Integer.toString(c.deaths), "0", s -> c.deaths = parseNonNegativeInt(s, c.deaths));
        y += 29;
        row("Playtime", y);
        field(y, c.playtime, "e.g. 3d 14h", s -> c.playtime = s);
        label("These values are local display stats, not DonutSMP records.", px + 20, py + H - 54, MUTED);
    }

    private void identityTab() {
        DonutConfig c = DonutLarpClient.CONFIG;
        int y = py + 75;
        row("Display name", y);
        TextFieldWidget name = field(y, c.displayName == null ? "" : c.displayName,
                MinecraftClient.getInstance().getSession().getUsername(), s -> c.displayName = s.trim());
        name.setMaxLength(16);
        label("Blank uses your real name. This only changes your own client view.", px + 20, y + 28, MUTED);

        y += 59;
        row("Visual rank", y);
        FlatButton rank = new FlatButton(px + 144, y, 188, 20, c.rank, button -> {
            int next = 0;
            for (int i = 0; i < DonutConfig.RANKS.length; i++) {
                if (DonutConfig.RANKS[i].equalsIgnoreCase(c.rank)) {
                    next = (i + 1) % DonutConfig.RANKS.length;
                    break;
                }
            }
            c.rank = DonutConfig.RANKS[next];
            button.setMessage(Text.literal(c.rank));
            ((FlatButton) button).textColor = 0xFF000000 | rankColor(c.rank);
        });
        rank.textColor = 0xFF000000 | rankColor(c.rank);
        addDrawableChild(rank);
        y += 32;
        toggle(y, "Rank tag in your tab name", () -> c.rankTag, v -> c.rankTag = v);
        y += 29;
        toggle(y, "Swap name in unsigned chat", () -> c.nameSwap, v -> c.nameSwap = v);
        label("Player-signed chat and other players' screens are not changed.", px + 20, py + H - 54, MUTED);
    }

    private void optionsTab() {
        DonutConfig c = DonutLarpClient.CONFIG;
        int y = py + 72;
        toggle(y, "Mod enabled", () -> c.enabled, v -> c.enabled = v);
        y += 31;
        toggle(y, "Fake stats panel", () -> c.fakeBoard, v -> c.fakeBoard = v);
        y += 31;
        toggle(y, "Deduct local balance on /pay", () -> c.localPay, v -> c.localPay = v);
        y += 31;
        toggle(y, "DonutSMP server only", () -> c.onlyDonut, v -> c.onlyDonut = v);
        label("Server-only checks the address for 'donutsmp'. Turn it off for testing.", px + 20, py + H - 54, MUTED);
    }

    private void row(String text, int y) {
        label(text, px + 20, y + 6, CREAM);
    }

    private void label(String text, int x, int y, int color) {
        labels.add(new Label(text, x, y, color));
    }

    private TextFieldWidget field(int y, String value, String placeholder, Consumer<String> onChange) {
        TextFieldWidget field = new TextFieldWidget(textRenderer, px + 144, y + 3, 188, 17, Text.empty());
        field.setDrawsBackground(false);
        field.setMaxLength(32);
        field.setEditableColor(CREAM);
        field.setUneditableColor(MUTED);
        field.setPlaceholder(Text.literal(placeholder).formatted(Formatting.DARK_GRAY));
        field.setText(value);
        field.setChangedListener(onChange);
        fields.add(field);
        addDrawableChild(field);
        return field;
    }

    private void toggle(int y, String text, BooleanSupplier get, Consumer<Boolean> set) {
        row(text, y);
        FlatButton button = new FlatButton(px + 264, y, 68, 20, get.getAsBoolean() ? "ON" : "OFF", b -> {
            boolean value = !get.getAsBoolean();
            set.accept(value);
            b.setMessage(Text.literal(value ? "ON" : "OFF"));
            ((FlatButton) b).selected = value;
        });
        button.selected = get.getAsBoolean();
        addDrawableChild(button);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        drawPanel(context);
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawPanel(DrawContext context) {
        context.fill(px - 1, py - 1, px + W + 1, py + H + 1, 0xFF4B303A);
        context.fill(px, py, px + W, py + H, PANEL);
        context.fill(px, py, px + W / 2, py + 3, PINK);
        context.fill(px + W / 2, py, px + W, py + 3, ORANGE);

        context.drawTextWithShadow(textRenderer, Text.literal("Donut").formatted(Formatting.BOLD), px + 16, py + 12, PINK);
        context.drawTextWithShadow(textRenderer, Text.literal("Larp").formatted(Formatting.BOLD), px + 57, py + 12, CREAM);
        String subtitle = "LOCAL VISUALS ONLY";
        context.drawTextWithShadow(textRenderer, subtitle, px + W - 16 - textRenderer.getWidth(subtitle), py + 13, MUTED);

        context.fill(px + 16, py + 62, px + W - 16, py + 63, LINE);
        context.fill(px + 16, py + H - 42, px + W - 16, py + H - 41, LINE);

        for (TextFieldWidget field : fields) {
            int x = field.getX() - 5;
            int y = field.getY() - 2;
            int w = field.getWidth() + 10;
            int h = field.getHeight() + 4;
            int edge = field.isFocused() ? PINK : LINE;
            context.fill(x - 1, y - 1, x + w + 1, y + h + 1, edge);
            context.fill(x, y, x + w, y + h, FIELD);
        }

        for (Label label : labels) {
            context.drawTextWithShadow(textRenderer, label.text(), label.x(), label.y(), label.color());
        }
    }

    @Override
    public void removed() {
        DonutLarpClient.CONFIG.save();
        super.removed();
    }

    private static int parseNonNegativeInt(String input, int fallback) {
        try {
            return Math.max(0, Integer.parseInt(input.trim()));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static int rankColor(String rank) {
        Integer color = NameSwap.rankColor(rank).getColorValue();
        return 0xFF000000 | (color == null ? 0xFF8FC8 : color);
    }

    private record Label(String text, int x, int y, int color) {}

    private static final class FlatButton extends ButtonWidget {
        private boolean selected;
        private int textColor = CREAM;

        private FlatButton(int x, int y, int width, int height, String message, PressAction action) {
            super(x, y, width, height, Text.literal(message), action, DEFAULT_NARRATION_SUPPLIER);
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            int x = getX();
            int y = getY();
            int bg = selected ? PINK : (isHovered() ? 0xFF49303A : 0xFF2C1E24);
            context.fill(x, y, x + width, y + height, bg);
            if (!selected) {
                context.fill(x, y, x + width, y + 1, LINE);
                context.fill(x, y + height - 1, x + width, y + height, LINE);
            }
            int fg = selected ? 0xFF2B1B17 : textColor;
            context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,
                    getMessage(), x + width / 2, y + (height - 8) / 2, fg);
        }
    }
}
