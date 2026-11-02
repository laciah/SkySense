package com.example.skysense;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.lwjgl.glfw.GLFW;

/**
 * The menu is fixed: always centred, never dragged or resized. Its size comes from ONE thing only - Minecraft's
 * GUI Scale option (Options > Video Settings > GUI Scale). Bigger GUI Scale = bigger menu.
 *
 * Everything is drawn in REAL screen pixels (the GUI matrix is scaled by 1/guiScale), so 1px lines are really 1px.
 * Layout numbers ("base px") were measured from the reference screenshots (842x722 window) and are multiplied by the
 * current window size. Text uses Minecraft's own font, scaled by WHOLE numbers only so it stays crisp.
 */
public class SkySenseScreen extends Screen {
    // ---- Reference window the layout numbers were measured on ----
    private static final int REF_W = 842;

    /**
     * Window size in GUI units. Real size = this x GUI Scale:
     * GUI Scale 2 = 842x722 px, 3 = 1263x1083 px, 4 = 1684x1444 px.
     * If that doesn't fit on your screen, the whole menu is shrunk to fit.
     */
    private static final int WINDOW_GUI_W = 421, WINDOW_GUI_H = 361;

    // ---- Window chrome (measured) ----
    private static final int C_BLACK = 0xFF0C0C0C;
    private static final int C_LIGHT = 0xFF3D3D3D;
    private static final int C_MID   = 0xFF2B2B2B;
    private static final int C_BODY  = 0xFF111111; // average of the 0C/14 dither texture
    private static final int C_BODY_LIGHT = 0xFF141414;
    private static final int C_SIDEBAR = 0xFF0C0C0C;

    // ---- Group box / lines ----
    private static final int C_GROUP_LINE = 0xFF272827;
    private static final int C_GROUP_FILL = 0xFF171717;

    // ---- Text ----
    private static final int C_TEXT    = 0xFFCDCECD;
    private static final int C_DD_TEXT = 0xFF9EA09F;

    // ---- Button / input fill: three vertical bands ----
    private static final int[] BAND       = {0xFF1E1F1E, 0xFF202020, 0xFF222322};
    private static final int[] BAND_HOVER = {0xFF282928, 0xFF2A2A2A, 0xFF2C2D2C};
    private static final int[] BAND_DOWN  = {0xFF181918, 0xFF1A1A1A, 0xFF1C1D1C};

    // ---- Checkbox ----
    private static final int[] CB_OFF = {0xFF4F504F, 0xFF494946, 0xFF414447, 0xFF3D3D3D, 0xFF363839, 0xFF313334};
    private static final int C_BLUE_TOP = 0xFF2C92F2;
    private static final int C_BLUE_BOT = 0xFF2686E0;

    // ---- Slider look (measured from the reference screenshot) ----
    private static final int[] SLIDER_TRACK = {0xFF313334, 0xFF363839, 0xFF3D3D3D, 0xFF3D3D3D, 0xFF414447};
    private static final int[] SLIDER_FILL  = {0xFF2C92F2, 0xFF2C92F2, 0xFF2686E0, 0xFF2686E0, 0xFF2077CB};
    private static final int C_TEXT_OUTLINE = 0xFF0E0F0F;

    // ---- Sidebar icon palettes: { main, highlight, shadow } ----
    private static final int[] ICON_SELECTED = {0xFFE2E2E2, 0xFFFFFFFF, 0xFFA8A8A8};
    private static final int[] ICON_HOVER    = {0xFF7E7E7E, 0xFF9A9A9A, 0xFF5A5A5A};
    private static final int[] ICON_DIM      = {0xFF5B5B5B, 0xFF7A7A7A, 0xFF3E3E3E};

    // ---- Fade line (3 rows, measured; 41 colour stops left -> right) ----
    private static final int[] LINE_A = {
        0x5696BB, 0x60ABD7, 0x58A0D2, 0x579DD2, 0x5C95D2, 0x6698D6, 0x6698D6, 0x6698D6,
        0x6698D6, 0x7E82D3, 0x926FD1, 0x926FD1, 0x926FD1, 0x926FD1, 0x926FD1, 0x926FD1,
        0xAE63C7, 0xC658BE, 0xC658BE, 0xC658BE, 0xC658BE, 0xC658BE, 0xC658BE, 0xC658BE,
        0xC65BBD, 0xC981B0, 0xC984AF, 0xC681AA, 0xC58C99, 0xC7A183, 0xC7A183, 0xC7A183,
        0xC6A382, 0xC5AF62, 0xC7B056, 0xC7B056, 0xC7B056, 0xD5C14C, 0xE6D440, 0xE6D440,
        0xC7B739
    };

    private static final int[] LINE_B = {
        0x18556A, 0x1A617A, 0x1A617A, 0x275A78, 0x2F5576, 0x2F5576, 0x305473, 0x3A4B6E,
        0x424E70, 0x424E70, 0x3F5371, 0x485271, 0x485271, 0x4E496D, 0x593766, 0x65275F,
        0x5B3265, 0x5B3265, 0x5E2E63, 0x6C175A, 0x6F175B, 0x6F1E5A, 0x732A59, 0x773659,
        0x773659, 0x763459, 0x773659, 0x6E464B, 0x6D454C, 0x6D4350, 0x6D5143, 0x705639,
        0x705639, 0x70573C, 0x70573B, 0x686334, 0x616D2F, 0x5A772B, 0x5A772B, 0x5A772B,
        0x556F22
    };

    private static final int[] LINE_C = {
        0x050505, 0x030303, 0x030303, 0x030303, 0x040404, 0x030303, 0x040404, 0x050505,
        0x040404, 0x040404, 0x030303, 0x030303, 0x030303, 0x040404, 0x070707, 0x090C13,
        0x080A0E, 0x080A0F, 0x090C12, 0x0A0E17, 0x0A0E17, 0x0A0E17, 0x08090D, 0x040404,
        0x030303, 0x030303, 0x030303, 0x040404, 0x050505, 0x040404, 0x040404, 0x050505,
        0x050505, 0x070707, 0x0A0A0A, 0x0D0C0B, 0x120D09, 0x0C0906, 0x030303, 0x050505,
        0x040404
    };

    // ---- Pixel-art icons (16x16): '#' main, '+' highlight, '-' shadow, '.' empty ----
    private static final String[] ICON_GEAR = {
        "......+++#......",
        "......+##-......",
        "..+#..+##-..+#..",
        "..##++####++#-..",
        "...+########-...",
        "...+##----##-...",
        "+++##-....+##++#",
        "+####-....+####-",
        "+####-....+####-",
        "#--##-....+##---",
        "...+##++++##-...",
        "...+########-...",
        "..+#--####--##..",
        "..#-..+##-..#-..",
        "......+##-......",
        "......#---......"
    };

    private static final String[] ICON_SUN = {
        ".......+#.......",
        ".+#....+-....+#.",
        ".#-....#-....#-.",
        "................",
        "....+++++++#....",
        "....+######-....",
        "....+######-....",
        "++#.+######-.++#",
        "#--.+######-.#--",
        "....+######-....",
        "....+######-....",
        "....#-------....",
        "................",
        ".+#....+#....+#.",
        ".#-....+-....#-.",
        ".......#-......."
    };

    // ---- Tabs ----
    private static final int TAB_VISUAL = 0, TAB_SETTINGS = 1;
    private static final int TAB_SIZE = 75;       // sidebar tabs are squares, like the screenshot
    private static final int ICON_SIZE = 44;      // icon width inside a tab

    // ---- Page grid (base px, relative to the window's top-left). Two equal columns like the reference:
    //      21px from the sidebar, 27px between columns, ~20px from the right edge, boxes end at BOX_BOTTOM. ----
    private static final int COL_A_X = 101, COL_B_X = 472, BOX_W = 344, BOX_Y = 31, BOX_BOTTOM = 696;
    private static final int NORENDER_BOX_H = 50;
    private static final int CTL_X = 41, CTL_W = 200, CTL_H = 20;

    // ---- Viewmodel box contents ----
    private static final int VM_CB_Y0 = 21, VM_CB_PITCH = 18;            // checkbox rows (same pitch as the reference)
    private static final int VM_SLIDER_Y0 = 133, VM_SLIDER_PITCH = 34;   // slider blocks: label + bar
    private static final int FIELD_GAP = 8, FIELD_W = 62, FIELD_H = 20, FIELD_DY = 5; // number box beside the bar
    private static final String VM_TITLE = "Viewmodel";
    private static final String VM_RESET_LABEL = "Reset To Default";
    private static final String[] VM_CB_LABEL = {"Enabled", "Apply To Hand", "No Equip Animation",
            "No Bow Swing", "No Swing Reset", "No Haste"};
    private static final String[] VM_LABEL = {"Position X", "Position Y", "Position Z",
            "Rotation X", "Rotation Y", "Rotation Z", "Scale X", "Scale Y", "Scale Z",
            "Swing X", "Swing Y", "Swing Z", "Swing Speed"};
    private static final String[] VM_SUFFIX = {"", "", "", "\u00B0", "\u00B0", "\u00B0",
            "%", "%", "%", "%", "%", "%", "%"};

    private static final String VISUAL_TITLE   = "No Render";
    private static final String LABEL_HIDE_ABS = "Hide Absorption Hearts";

    /** Nudge text up/down in real px if it looks 1px off in game (+ = down). */
    private static final int NUDGE_Y = 0;

    // ---- Remembered while the game is open ----
    private static int savedTab = TAB_VISUAL;

    private double guiScale = 1.0;
    private int screenW, screenH;       // real px
    private int tab = TAB_VISUAL;
    private int px, py, pw, ph;         // window rectangle, real px (always centred)

    private double mouseRX = -1, mouseRY = -1; // latest mouse position, real px (fractional)

    // layout results (real px, absolute)
    private double ui = 1.5;            // window width / 842
    private int fontK = 2;              // whole-number text scale
    private int lineT, headerH;
    private int sbX1, sbX2, tabH, iconCell;
    private int boxX1, boxY1, boxX2, boxY2;     // No Render box
    private int cbX, cbY, cbS, labelX;

    // Viewmodel box geometry (real px)
    private int vmBoxX1, vmBoxY1, vmBoxX2, vmBoxY2, vmCbX, vmLabelX, vmTrackX1, vmTrackX2, fieldX1, fieldX2;
    private int vmBtnX1, vmBtnY1, vmBtnX2, vmBtnY2;   // "Reset To Default" button under the sliders
    private boolean vmBtnDown = false;
    private final int[] vmCbY = new int[6];
    private final int[] slCapTop = new int[SkySenseConfig.VM_COUNT];
    private final int[] slY1 = new int[SkySenseConfig.VM_COUNT];
    private final int[] slY2 = new int[SkySenseConfig.VM_COUNT];
    private final int[] fieldY1 = new int[SkySenseConfig.VM_COUNT];
    private final int[] fieldY2 = new int[SkySenseConfig.VM_COUNT];
    private int activeSlider = -1;      // slider being dragged
    private int focusSlider = -1;       // slider last clicked (arrow keys act on it when nothing is hovered)

    // number box editing
    private int editing = -1;
    private int editOriginal = 0;
    private final StringBuilder editBuf = new StringBuilder();

    public SkySenseScreen() {
        super(Component.translatable("screen.skysense.title"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ---------------------------------------------------------------- setup

    @Override
    protected void init() {
        guiScale = Math.max(1.0, Minecraft.getInstance().getWindow().getGuiScale());
        screenW = (int) Math.round(this.width * guiScale);
        screenH = (int) Math.round(this.height * guiScale);

        // size comes from the GUI Scale; only shrunk if the screen is too small to show it
        double w = WINDOW_GUI_W * guiScale, h = WINDOW_GUI_H * guiScale;
        double fit = Math.min(1.0, Math.min((screenW - 16.0) / w, (screenH - 16.0) / h));
        pw = Math.max(300, (int) Math.round(w * fit));
        ph = Math.max(260, (int) Math.round(h * fit));
        px = (screenW - pw) / 2;
        py = (screenH - ph) / 2;

        tab = savedTab;
    }

    /** base px (measured on the 842x722 reference) -> real px at the current window size */
    private int sc(double base) {
        return (int) Math.round(base * ui);
    }

    // ------------------------------------------------------------------ text

    private static final Style BOLD = Style.EMPTY.withBold(true);

    private Component sm(String text) {
        return Component.literal(text);
    }

    private Component ti(String text) {
        return Component.literal(text).withStyle(BOLD);
    }

    /** Height of capital letters in real px (Minecraft's font has 7px capitals). */
    private int cap() {
        return 7 * fontK;
    }

    private int textW(Component c) {
        return this.font.width(c) * fontK;
    }

    /** Draws text with the top of its capital letters at capTop, scaled by a whole number so it stays crisp. */
    private void drawText(GuiGraphicsExtractor g, Component c, int x, int capTop, int color) {
        var pose = g.pose();
        pose.pushMatrix();
        pose.translate((float) x, (float) (capTop + NUDGE_Y));
        pose.scale((float) fontK, (float) fontK);
        g.text(this.font, c, 0, 0, color, false);
        pose.popMatrix();
    }

    // ---------------------------------------------------------------- layout

    private void layout() {
        ui = pw / (double) REF_W;
        fontK = Math.max(1, Math.min(8, (int) Math.round(ui * 4.0 / 3.0)));   // whole multiples of the default font

        lineT = Math.max(1, (int) Math.round(ui));                              // fade line row thickness
        headerH = 6 + 1 + 3 * lineT;

        sbX1 = px + 6;
        sbX2 = px + 6 + sc(TAB_SIZE);
        tabH = sc(TAB_SIZE);                                                    // square tabs
        iconCell = Math.max(1, (int) Math.round(sc(ICON_SIZE) / 16.0));
        cbS = Math.max(6, sc(8));

        // No Render box: right column
        boxX1 = px + sc(COL_B_X);
        boxY1 = py + sc(BOX_Y);
        boxX2 = px + sc(COL_B_X + BOX_W);
        boxY2 = py + sc(BOX_Y + NORENDER_BOX_H);
        cbX = px + sc(COL_B_X + 21);
        cbY = py + sc(BOX_Y + 21);
        labelX = px + sc(COL_B_X + CTL_X);

        // Viewmodel box: left column, full height
        vmBoxX1 = px + sc(COL_A_X);
        vmBoxY1 = py + sc(BOX_Y);
        vmBoxX2 = px + sc(COL_A_X + BOX_W);
        vmBoxY2 = py + sc(BOX_BOTTOM);
        vmCbX = px + sc(COL_A_X + 21);
        vmLabelX = px + sc(COL_A_X + CTL_X);
        vmTrackX1 = vmLabelX;
        vmTrackX2 = px + sc(COL_A_X + CTL_X + CTL_W);
        fieldX1 = px + sc(COL_A_X + CTL_X + CTL_W + FIELD_GAP);
        fieldX2 = px + sc(COL_A_X + CTL_X + CTL_W + FIELD_GAP + FIELD_W);
        for (int i = 0; i < vmCbY.length; i++) {
            vmCbY[i] = py + sc(BOX_Y + VM_CB_Y0 + i * VM_CB_PITCH);
        }
        for (int i = 0; i < SkySenseConfig.VM_COUNT; i++) {
            slCapTop[i] = py + sc(BOX_Y + VM_SLIDER_Y0 + i * VM_SLIDER_PITCH);
            slY1[i] = slCapTop[i] + sc(13);
            slY2[i] = slY1[i] + Math.max(5, sc(7));
            fieldY1[i] = slCapTop[i] + sc(FIELD_DY);
            fieldY2[i] = fieldY1[i] + sc(FIELD_H);
        }
        vmBtnX1 = vmLabelX;
        vmBtnX2 = vmTrackX2;
        vmBtnY1 = py + sc(BOX_Y + VM_SLIDER_Y0 + SkySenseConfig.VM_COUNT * VM_SLIDER_PITCH + 4);
        vmBtnY2 = vmBtnY1 + sc(CTL_H);
    }

    // ------------------------------------------------------------- hit tests

    private static boolean in(double mx, double my, int x1, int y1, int x2, int y2) {
        return mx >= x1 && mx < x2 && my >= y1 && my < y2;
    }

    private boolean visualOpen() {
        return tab == TAB_VISUAL;
    }

    private int tabAt(double mx, double my) {
        int top = py + headerH;
        for (int i = 0; i < 2; i++) {
            if (in(mx, my, sbX1, top + i * tabH, sbX2, top + (i + 1) * tabH)) return i;
        }
        return -1;
    }

    private boolean inNoRenderCheckbox(double mx, double my) {
        int labelEnd = labelX + textW(sm(LABEL_HIDE_ABS));
        return in(mx, my, cbX - 1, cbY - 2, labelEnd, cbY + cbS + 2);
    }

    private int vmCheckboxAt(double mx, double my) {
        for (int i = 0; i < vmCbY.length; i++) {
            int labelEnd = vmLabelX + textW(sm(VM_CB_LABEL[i]));
            if (in(mx, my, vmCbX - 1, vmCbY[i] - 2, labelEnd, vmCbY[i] + cbS + 2)) return i;
        }
        return -1;
    }

    private int sliderAt(double mx, double my) {
        int padY = sc(6);
        for (int i = 0; i < SkySenseConfig.VM_COUNT; i++) {
            if (in(mx, my, vmTrackX1 - 2, slY1[i] - padY, vmTrackX2 + 2, slY2[i] + padY)) return i;
        }
        return -1;
    }

    private int fieldAt(double mx, double my) {
        for (int i = 0; i < SkySenseConfig.VM_COUNT; i++) {
            if (in(mx, my, fieldX1, fieldY1[i], fieldX2, fieldY2[i])) return i;
        }
        return -1;
    }

    private boolean inVmButton(double mx, double my) {
        return in(mx, my, vmBtnX1, vmBtnY1, vmBtnX2, vmBtnY2);
    }

    private void setSliderFromMouse(int i, double mx) {
        int ix1 = vmTrackX1 + 1, ix2 = vmTrackX2 - 1;
        double t = (mx - ix1) / (double) Math.max(1, ix2 - ix1);
        t = Math.max(0.0, Math.min(1.0, t));
        int min = SkySenseConfig.VM_MIN[i], max = SkySenseConfig.VM_MAX[i];
        SkySenseConfig.vm[i] = min + (int) Math.round(t * (max - min));
    }

    // ------------------------------------------------------- number box editing

    private void beginEdit(int i) {
        if (editing == i) return;
        commitEdit();
        editing = i;
        editOriginal = SkySenseConfig.vm[i];
        editBuf.setLength(0);
        editBuf.append(SkySenseConfig.vm[i]);
    }

    /** Applies whatever is typed so far (clamped), so the slider follows as you type. */
    private void applyEditLive() {
        if (editing < 0) return;
        String s = editBuf.toString();
        if (s.isEmpty() || s.equals("-")) return;
        try {
            int v = Integer.parseInt(s);
            SkySenseConfig.vm[editing] = Math.max(SkySenseConfig.VM_MIN[editing],
                    Math.min(SkySenseConfig.VM_MAX[editing], v));
        } catch (NumberFormatException ignored) {
            // too long / not a number: ignore
        }
    }

    private void commitEdit() {
        if (editing < 0) return;
        String s = editBuf.toString();
        if (s.isEmpty() || s.equals("-")) {
            SkySenseConfig.vm[editing] = editOriginal;
        } else {
            applyEditLive();
        }
        editing = -1;
        SkySenseConfig.save();
    }

    private void cancelEdit() {
        if (editing < 0) return;
        SkySenseConfig.vm[editing] = editOriginal;
        editing = -1;
    }

    private boolean handleEditKey(int key) {
        int digit = -1;
        if (key >= GLFW.GLFW_KEY_0 && key <= GLFW.GLFW_KEY_9) digit = key - GLFW.GLFW_KEY_0;
        else if (key >= GLFW.GLFW_KEY_KP_0 && key <= GLFW.GLFW_KEY_KP_9) digit = key - GLFW.GLFW_KEY_KP_0;

        if (digit >= 0) {
            if (editBuf.length() < 4) editBuf.append(digit);
            applyEditLive();
            return true;
        }
        if (key == GLFW.GLFW_KEY_MINUS || key == GLFW.GLFW_KEY_KP_SUBTRACT) {
            if (SkySenseConfig.VM_MIN[editing] < 0) {
                if (editBuf.length() > 0 && editBuf.charAt(0) == '-') editBuf.deleteCharAt(0);
                else editBuf.insert(0, '-');
                applyEditLive();
            }
            return true;
        }
        if (key == GLFW.GLFW_KEY_BACKSPACE) {
            if (editBuf.length() > 0) editBuf.setLength(editBuf.length() - 1);
            applyEditLive();
            return true;
        }
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            commitEdit();
            return true;
        }
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            cancelEdit();
            return true;
        }
        return false;
    }

    @Override
    public void removed() {
        commitEdit();
        super.removed();
    }

    // ------------------------------------------------------------- rendering

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        if (mouseRX < 0) { // until the first real mouse event, fall back to the integer position
            mouseRX = mouseX * guiScale;
            mouseRY = mouseY * guiScale;
        }
        layout();

        var pose = g.pose();
        pose.pushMatrix();
        float inv = (float) (1.0 / guiScale);
        pose.scale(inv, inv);               // from here on: 1 unit = 1 real screen pixel

        drawWindow(g);
        drawSidebar(g);
        if (visualOpen()) {
            drawGroupBoxAt(g, vmBoxX1, vmBoxY1, vmBoxX2, vmBoxY2, VM_TITLE);
            boolean[] vmOn = {SkySenseConfig.vmEnabled, SkySenseConfig.vmApplyHand, SkySenseConfig.vmNoEquip,
                    SkySenseConfig.vmNoBowSwing, SkySenseConfig.vmNoSwingReset, SkySenseConfig.vmNoHaste};
            for (int i = 0; i < vmCbY.length; i++) {
                drawCheckboxAt(g, vmCbX, vmCbY[i], vmLabelX, vmOn[i], VM_CB_LABEL[i]);
            }
            for (int i = 0; i < SkySenseConfig.VM_COUNT; i++) {
                drawSlider(g, i);
                drawField(g, i);
            }
            drawButtonAt(g, vmBtnX1, vmBtnY1, vmBtnX2, vmBtnY2, VM_RESET_LABEL,
                    inVmButton(mouseRX, mouseRY), vmBtnDown);

            drawGroupBoxAt(g, boxX1, boxY1, boxX2, boxY2, VISUAL_TITLE);
            drawCheckboxAt(g, cbX, cbY, labelX, SkySenseConfig.hideAbsorptionHearts, LABEL_HIDE_ABS);
        }
        // the Settings tab has no options right now

        pose.popMatrix();
    }

    private void drawWindow(GuiGraphicsExtractor g) {
        int x2 = px + pw, y2 = py + ph;
        g.fill(px, py, x2, y2, C_BLACK);                         // 1px black edge
        g.fill(px + 1, py + 1, x2 - 1, y2 - 1, C_LIGHT);         // 1px light line
        g.fill(px + 2, py + 2, x2 - 2, y2 - 2, C_MID);           // 3px grey band
        g.fill(px + 5, py + 5, x2 - 5, y2 - 5, C_LIGHT);         // 1px light line
        g.fill(px + 6, py + 6, x2 - 6, y2 - 6, C_BODY);          // body

        // fade line: 1px black gap, then three rows (bright, dim, shadow), each lineT thick
        int ix = px + 6, iw = pw - 12;
        g.fill(ix, py + 6, ix + iw, py + 7, C_BLACK);
        int ya = py + 7, yb = ya + lineT, yc = yb + lineT, yd = yc + lineT;
        int step = Math.max(2, iw / 420);
        for (int dx = 0; dx < iw; dx += step) {
            float t = iw > 1 ? (float) dx / (iw - 1) : 0f;
            int xe = Math.min(ix + dx + step, ix + iw);
            g.fill(ix + dx, ya, xe, yb, stop(LINE_A, t));
            g.fill(ix + dx, yb, xe, yc, stop(LINE_B, t));
            g.fill(ix + dx, yc, xe, yd, stop(LINE_C, t));
        }
    }

    private static int stop(int[] arr, float t) {
        float f = Math.max(0f, Math.min(1f, t)) * (arr.length - 1);
        int i = (int) f;
        if (i >= arr.length - 1) return 0xFF000000 | arr[arr.length - 1];
        float k = f - i;
        int a = arr[i], b = arr[i + 1];
        int r = Math.round(((a >> 16) & 255) * (1 - k) + ((b >> 16) & 255) * k);
        int gr = Math.round(((a >> 8) & 255) * (1 - k) + ((b >> 8) & 255) * k);
        int bl = Math.round((a & 255) * (1 - k) + (b & 255) * k);
        return 0xFF000000 | (r << 16) | (gr << 8) | bl;
    }

    // ---- sidebar ----

    private void drawSidebar(GuiGraphicsExtractor g) {
        int top0 = py + headerH;
        int bottom = py + ph - 6;
        g.fill(sbX1, top0, sbX2, bottom, C_SIDEBAR);
        g.fill(sbX2 - 1, top0, sbX2, bottom, C_GROUP_LINE);            // right edge line

        // selected tab opens into the page (no right edge) and has a line above and below it
        int t0 = top0 + tab * tabH, t1 = t0 + tabH;
        g.fill(sbX1, t0, sbX2, t1, C_BODY);
        if (tab > 0) g.fill(sbX1, t0, sbX2, t0 + 1, C_GROUP_LINE);
        g.fill(sbX1, t1 - 1, sbX2, t1, C_GROUP_LINE);

        int hover = tabAt(mouseRX, mouseRY);
        for (int i = 0; i < 2; i++) {
            String[] icon = (i == TAB_VISUAL) ? ICON_SUN : ICON_GEAR;
            int[] pal = (i == tab) ? ICON_SELECTED : (i == hover ? ICON_HOVER : ICON_DIM);
            int isz = 16 * iconCell;
            int ix = sbX1 + ((sbX2 - 1 - sbX1) - isz) / 2;
            int iy = top0 + i * tabH + (tabH - isz) / 2;
            drawIcon(g, icon, ix, iy, iconCell, pal);
        }
    }

    private void drawIcon(GuiGraphicsExtractor g, String[] rows, int x, int y, int c, int[] pal) {
        for (int r = 0; r < rows.length; r++) {
            String row = rows[r];
            int col = 0;
            while (col < row.length()) {
                char ch = row.charAt(col);
                if (ch == '.') { col++; continue; }
                int end = col;
                while (end < row.length() && row.charAt(end) == ch) end++;
                int color = ch == '+' ? pal[1] : (ch == '-' ? pal[2] : pal[0]);
                g.fill(x + col * c, y + r * c, x + end * c, y + (r + 1) * c, color);
                col = end;
            }
        }
    }

    // ---- boxes and widgets ----

    private void drawGroupBoxAt(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, String titleText) {
        g.fill(x1 - 1, y1 - 1, x2 + 1, y2 + 1, C_BLACK);   // 1px black outline
        g.fill(x1, y1, x2, y2, C_GROUP_LINE);             // 1px line
        g.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, C_GROUP_FILL);

        Component title = ti(titleText);
        int tx = x1 + sc(12);
        int tw = textW(title);
        g.fill(tx - 3, y1, tx + tw + 1, y1 + 1, C_BODY_LIGHT);  // gap in the line for the title
        g.fill(tx - 3, y1, tx - 2, y1 + 1, C_BLACK);
        g.fill(tx + tw, y1, tx + tw + 1, y1 + 1, C_BLACK);
        drawText(g, title, tx, y1 - cap() / 2, C_TEXT);
    }

    private void drawCheckboxAt(GuiGraphicsExtractor g, int x, int y, int lx, boolean on, String label) {
        g.fill(x, y, x + cbS, y + cbS, C_BLACK);
        int ix1 = x + 1, iy1 = y + 1, ix2 = x + cbS - 1, iy2 = y + cbS - 1;
        int n = iy2 - iy1;
        for (int i = 0; i < n; i++) {
            int color;
            if (on) color = (i * 2 < n) ? C_BLUE_TOP : C_BLUE_BOT;
            else color = CB_OFF[Math.min(CB_OFF.length - 1, i * CB_OFF.length / n)];
            g.fill(ix1, iy1 + i, ix2, iy1 + i + 1, color);
        }
        drawText(g, sm(label), lx, y + (cbS - cap()) / 2, C_TEXT);
    }

    /**
     * Slider, like the reference: label above, black outline, 5-row track, 3-tone fill, and the value centred on the
     * end of the fill with a black outline, hanging just below the bar.
     */
    private void drawSlider(GuiGraphicsExtractor g, int i) {
        drawText(g, sm(VM_LABEL[i]), vmLabelX, slCapTop[i], C_TEXT);

        int x1 = vmTrackX1, x2 = vmTrackX2, y1 = slY1[i], y2 = slY2[i];
        g.fill(x1, y1, x2, y2, C_BLACK);
        int ix1 = x1 + 1, ix2 = x2 - 1, iy1 = y1 + 1, iy2 = y2 - 1;
        int n = iy2 - iy1;

        int min = SkySenseConfig.VM_MIN[i], max = SkySenseConfig.VM_MAX[i], v = SkySenseConfig.vm[i];
        int fillX = ix1 + (int) Math.round((ix2 - ix1) * (double) (v - min) / (max - min));
        for (int r = 0; r < n; r++) {
            int band = Math.min(4, r * 5 / n);
            g.fill(ix1, iy1 + r, fillX, iy1 + r + 1, SLIDER_FILL[band]);
            g.fill(fillX, iy1 + r, ix2, iy1 + r + 1, SLIDER_TRACK[band]);
        }

        Component value = sm(v + VM_SUFFIX[i]);
        int tw = textW(value);
        int tx = Math.max(x1, Math.min(fillX - tw / 2, x2 - tw));
        int ty = y1 + sc(2);
        int o = fontK; // outline = one font pixel
        drawText(g, value, tx - o, ty, C_TEXT_OUTLINE);
        drawText(g, value, tx + o, ty, C_TEXT_OUTLINE);
        drawText(g, value, tx, ty - o, C_TEXT_OUTLINE);
        drawText(g, value, tx, ty + o, C_TEXT_OUTLINE);
        drawText(g, value, tx, ty, C_TEXT);
    }

    /** The number box beside a slider: click, type an exact value, press Enter. */
    private void drawField(GuiGraphicsExtractor g, int i) {
        int x1 = fieldX1, x2 = fieldX2, y1 = fieldY1[i], y2 = fieldY2[i];
        boolean active = editing == i;
        boolean hover = !active && in(mouseRX, mouseRY, x1, y1, x2, y2);
        drawBox(g, x1, y1, x2, y2, hover ? BAND_HOVER : BAND);
        if (active) { // thin blue outline while typing
            g.fill(x1, y1, x2, y1 + 1, C_BLUE_BOT);
            g.fill(x1, y2 - 1, x2, y2, C_BLUE_BOT);
            g.fill(x1, y1, x1 + 1, y2, C_BLUE_BOT);
            g.fill(x2 - 1, y1, x2, y2, C_BLUE_BOT);
        }
        String s = active ? editBuf.toString() : Integer.toString(SkySenseConfig.vm[i]);
        Component c = sm(s);
        int tx = x1 + sc(8);
        int capTop = centeredCapTop(y1, y2);
        drawText(g, c, tx, capTop, active ? C_TEXT : C_DD_TEXT);
        if (active && (System.currentTimeMillis() / 500) % 2 == 0) { // blinking caret
            int cx = tx + textW(c) + fontK;
            g.fill(cx, capTop, cx + fontK, capTop + cap(), C_TEXT);
        }
    }

    /** 1px black border + three vertical colour bands, like the screenshot's buttons and inputs. */
    private void drawBox(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, int[] bands) {
        g.fill(x1, y1, x2, y2, C_BLACK);
        int n = (y2 - y1) - 2;
        int top = Math.max(1, Math.round(n * 2f / 18f));
        int mid = Math.max(1, Math.round(n * 8f / 18f));
        int y = y1 + 1;
        g.fill(x1 + 1, y, x2 - 1, y + top, bands[0]);
        y += top;
        g.fill(x1 + 1, y, x2 - 1, y + mid, bands[1]);
        y += mid;
        g.fill(x1 + 1, y, x2 - 1, y2 - 1, bands[2]);
    }

    private int centeredCapTop(int y1, int y2) {
        return y1 + ((y2 - y1) - cap()) / 2;
    }

    private void drawButtonAt(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, String text,
                              boolean hover, boolean down) {
        int[] bands = (down && hover) ? BAND_DOWN : (hover ? BAND_HOVER : BAND);
        drawBox(g, x1, y1, x2, y2, bands);
        Component label = sm(text);
        int tx = x1 + ((x2 - x1) - textW(label)) / 2;
        drawText(g, label, tx, centeredCapTop(y1, y2), C_TEXT);
    }

    // ----------------------------------------------------------------- input

    @Override
    public void mouseMoved(double x, double y) {
        mouseRX = x * guiScale;
        mouseRY = y * guiScale;
        super.mouseMoved(x, y);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        mouseRX = event.x() * guiScale;
        mouseRY = event.y() * guiScale;
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            layout();
            double mx = mouseRX, my = mouseRY;

            // clicking anywhere but the number box being edited finishes the edit
            if (editing >= 0 && fieldAt(mx, my) != editing) {
                commitEdit();
            }

            int t = tabAt(mx, my);
            if (t >= 0) {
                commitEdit();
                tab = t;
                savedTab = t;
                return true;
            }

            if (visualOpen()) {
                if (inNoRenderCheckbox(mx, my)) {
                    SkySenseConfig.hideAbsorptionHearts = !SkySenseConfig.hideAbsorptionHearts;
                    SkySenseConfig.save();
                    return true;
                }
                int vc = vmCheckboxAt(mx, my);
                if (vc >= 0) {
                    switch (vc) {
                        case 0 -> SkySenseConfig.vmEnabled = !SkySenseConfig.vmEnabled;
                        case 1 -> SkySenseConfig.vmApplyHand = !SkySenseConfig.vmApplyHand;
                        case 2 -> SkySenseConfig.vmNoEquip = !SkySenseConfig.vmNoEquip;
                        case 3 -> SkySenseConfig.vmNoBowSwing = !SkySenseConfig.vmNoBowSwing;
                        case 4 -> SkySenseConfig.vmNoSwingReset = !SkySenseConfig.vmNoSwingReset;
                        default -> SkySenseConfig.vmNoHaste = !SkySenseConfig.vmNoHaste;
                    }
                    SkySenseConfig.save();
                    return true;
                }
                if (inVmButton(mx, my)) {
                    vmBtnDown = true;
                    return true;
                }
                int fi = fieldAt(mx, my);
                if (fi >= 0) {
                    beginEdit(fi);
                    focusSlider = fi;
                    return true;
                }
                int si = sliderAt(mx, my);
                if (si >= 0) {
                    activeSlider = si;
                    focusSlider = si;
                    setSliderFromMouse(si, mx);
                    if (editing == si) { // keep an open number box in step with the slider
                        editBuf.setLength(0);
                        editBuf.append(SkySenseConfig.vm[si]);
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        mouseRX = event.x() * guiScale;
        mouseRY = event.y() * guiScale;
        if (activeSlider >= 0) {
            setSliderFromMouse(activeSlider, mouseRX);
            if (editing == activeSlider) {
                editBuf.setLength(0);
                editBuf.append(SkySenseConfig.vm[activeSlider]);
            }
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        mouseRX = event.x() * guiScale;
        mouseRY = event.y() * guiScale;
        boolean handled = false;
        if (activeSlider >= 0) {
            SkySenseConfig.save();
            activeSlider = -1;
            handled = true;
        }
        if (vmBtnDown) {
            vmBtnDown = false;
            layout();
            if (inVmButton(mouseRX, mouseRY)) {
                cancelEdit();
                SkySenseConfig.resetViewmodel();
                SkySenseConfig.save();
            }
            handled = true;
        }
        return handled || super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int key = event.key();

        // typing into a number box
        if (editing >= 0 && handleEditKey(key)) {
            return true;
        }

        // Pressing the open key again closes the menu (ESC works too).
        if (SkySenseClient.OPEN_KEY.matches(event)) {
            commitEdit();
            this.onClose();
            return true;
        }

        // Left / Right arrow: nudge a slider. The one under the mouse, otherwise the last one you clicked.
        // Hold Shift to move 10 at a time.
        if (visualOpen() && (key == GLFW.GLFW_KEY_LEFT || key == GLFW.GLFW_KEY_RIGHT)) {
            int target = sliderAt(mouseRX, mouseRY);
            if (target < 0) target = focusSlider;
            if (target >= 0) {
                int step = (event.modifiers() & GLFW.GLFW_MOD_SHIFT) != 0 ? 10 : 1;
                int dir = key == GLFW.GLFW_KEY_RIGHT ? 1 : -1;
                int min = SkySenseConfig.VM_MIN[target], max = SkySenseConfig.VM_MAX[target];
                SkySenseConfig.vm[target] = Math.max(min, Math.min(max, SkySenseConfig.vm[target] + dir * step));
                focusSlider = target;
                if (editing == target) {
                    editBuf.setLength(0);
                    editBuf.append(SkySenseConfig.vm[target]);
                }
                SkySenseConfig.save();
                return true;
            }
        }
        return super.keyPressed(event);
    }
}
