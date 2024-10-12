package mekanism.client.gui.element.window;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.custom.GuiEntityPreview;
import mekanism.client.gui.element.text.BackgroundType;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.SelectedWindowData.WindowType;
import mekanism.common.lib.Color;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.InputValidator;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

public class GuiColorWindow extends GuiWindow {

    public static final ResourceLocation TRANSPARENCY_GRID = MekanismUtils.getResource(ResourceType.GUI, "transparency_grid.png");
    private static final ResourceLocation HUE_PICKER = MekanismUtils.getResource(ResourceType.GUI, "color_picker.png");
    private static final int S_TILES = 10, V_TILES = 10;

    private final GuiTextField textField;
    private final boolean handlesAlpha;
    @Nullable
    private final Consumer<Color> updatePreviewColor;
    @Nullable
    private final Runnable previewReset;

    private float hue;
    private float saturation = 0.5F;
    private float value = 0.5F;
    private float alpha = 1;

    public GuiColorWindow(IGuiWrapper gui, int x, int y, boolean handlesAlpha, Color initialColor, Consumer<Color> callback) {
        this(gui, x, y, handlesAlpha, initialColor, callback, null, null, null);
    }

    public GuiColorWindow(IGuiWrapper gui, int x, int y, boolean handlesAlpha, Color initialColor, Consumer<Color> callback, @Nullable Supplier<LivingEntity> armorPreview,
          @Nullable Consumer<Color> updatePreviewColor, @Nullable Runnable previewReset) {
        super(gui, x, y, (handlesAlpha ? 184 : 158) + (armorPreview == null ? 0 : 83), handlesAlpha ? 152 : 140, WindowType.COLOR);
        interactionStrategy = InteractionStrategy.NONE;
        this.handlesAlpha = handlesAlpha;
        this.updatePreviewColor = updatePreviewColor;
        this.previewReset = previewReset;
        int extraWidth = this.handlesAlpha ? 26 : 0;
        int extraShadeWidth = this.handlesAlpha ? 20 : 0;
        int extraViewWidth = extraWidth - extraShadeWidth;
        addChild(new GuiElementHolder(gui, relativeX + 6, relativeY + 17, 41 + extraViewWidth, 82));
        addChild(new GuiColorView(gui, relativeX + 7, relativeY + 18, 39 + extraViewWidth, 80));

        addChild(new GuiElementHolder(gui, relativeX + 50 + extraViewWidth, relativeY + 17, 102 + extraShadeWidth, 82));
        addChild(new GuiShadePicker(gui, relativeX + 51 + extraViewWidth, relativeY + 18, 100 + extraShadeWidth, 80));

        addChild(new GuiElementHolder(gui, relativeX + 6, relativeY + 103, 146 + extraWidth, 10));
        addChild(new GuiHuePicker(gui, relativeX + 7, relativeY + 104, 144 + extraWidth, 8));

        if (this.handlesAlpha) {
            addChild(new GuiElementHolder(gui, relativeX + 6, relativeY + 115, 146 + extraWidth, 10));
            addChild(new GuiAlphaPicker(gui, relativeX + 7, relativeY + 116, 144 + extraWidth, 8));
        }

        int textOffset = this.handlesAlpha ? 6 : 0;
        textField = addChild(new GuiTextField(gui, this, relativeX + 30 + textOffset, relativeY + height - 20, 63 + extraWidth - textOffset, 12));
        textField.setInputValidator(InputValidator.DIGIT.or(c -> c == ','))
              //Transform paste to remove any spaces to allow pasting from sources that have a space after the comma
              .setPasteTransformer(text -> text.replace(" ", ""))
              .setBackground(BackgroundType.ELEMENT_HOLDER)
              .setMaxLength(this.handlesAlpha ? 15 : 11);
        addChild(new TranslationButton(gui, relativeX + 98 + extraWidth, relativeY + height - 21, 54, 14, MekanismLang.BUTTON_CONFIRM, (element, mouseX, mouseY) -> {
            callback.accept(getColor());
            return close(element, mouseX, mouseY);
        }));

        if (armorPreview != null) {
            addChild(new GuiEntityPreview(gui, relativeX + 155 + extraWidth, relativeY + 17, 80, height - 24, armorPreview));
        }

        setColor(initialColor);
    }

    @Override
    public void close() {
        super.close();
        if (previewReset != null) {
            previewReset.run();
        }
    }

    public Color getColor() {
        Color color = Color.hsv(hue, saturation, value);
        if (handlesAlpha) {
            color = color.alpha(alpha);
        }
        return color;
    }

    public void setColor(Color color) {
        setFromColor(color);
        updateTextFromColor();
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);
        drawTitleText(guiGraphics, MekanismLang.COLOR_PICKER.translate(), 6);
        ILangEntry entry = handlesAlpha ? MekanismLang.RGBA : MekanismLang.RGB;
        drawScrollingString(guiGraphics, entry.translate(), 2, height - 18, TextAlignment.RIGHT, titleTextColor(), textField.getRelativeX() - relativeX - 2, 2, false);
    }

    private void drawTiledGradient(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        int tileWidth = Math.round((float) width / S_TILES);
        int tileHeight = Math.round((float) height / V_TILES);
        for (int i = 0; i < 10; i++) {
            float minV = (float) i / V_TILES, maxV = (float) (i + 1) / V_TILES;
            for (int j = 0; j < 10; j++) {
                float minS = (float) j / S_TILES, maxS = (float) (j + 1) / S_TILES;
                Color tl = Color.hsv(hue, minS, maxV), tr = Color.hsv(hue, maxS, maxV), bl = Color.hsv(hue, minS, minV), br = Color.hsv(hue, maxS, minV);
                drawGradient(guiGraphics, x + j * tileWidth, y + (V_TILES - i - 1) * tileHeight, tileWidth, tileHeight, tl, tr, bl, br);
            }
        }
    }

    //Based on GuiGraphics#fillGradient
    private void drawGradient(GuiGraphics guiGraphics, int x, int y, int width, int height, Color tl, Color tr, Color bl, Color br) {
        VertexConsumer buffer = guiGraphics.bufferSource().getBuffer(RenderType.gui());
        Matrix4f matrix4f = guiGraphics.pose().last().pose();
        buffer.addVertex(matrix4f, x, y + height, 0)
              .setColor(bl.r(), bl.g(), bl.b(), bl.a());
        buffer.addVertex(matrix4f, x + width, y + height, 0)
              .setColor(br.r(), br.g(), br.b(), br.a());
        buffer.addVertex(matrix4f, x + width, y, 0)
              .setColor(tr.r(), tr.g(), tr.b(), tr.a());
        buffer.addVertex(matrix4f, x, y, 0)
              .setColor(tl.r(), tl.g(), tl.b(), tl.a());
        //Note: This technically should probably be flushIfUnmanaged, but I believe we are always unmanaged here, so it is not worth ATing the method to call it
        guiGraphics.flush();
    }

    private void updateTextFromColor() {
        Color color = getColor();
        String text = color.r() + "," + color.g() + "," + color.b();
        if (handlesAlpha) {
            text += "," + color.a();
        }
        textField.setText(text);
    }

    private void setFromColor(Color c) {
        double[] hsv = c.hsvArray();
        hue = (float) hsv[0];
        saturation = (float) hsv[1];
        value = (float) hsv[2];
        alpha = handlesAlpha ? c.af() : 255;
        if (updatePreviewColor != null) {
            updatePreviewColor.accept(c);
        }
    }

    private void updateArmorPreview() {
        if (updatePreviewColor != null) {
            updatePreviewColor.accept(getColor());
        }
    }

    private void updateColorFromText() {
        String[] split = textField.getText().split(",");
        if (split.length == (handlesAlpha ? 4 : 3)) {
            try {
                int r = Integer.parseInt(split[0]);
                int g = Integer.parseInt(split[1]);
                int b = Integer.parseInt(split[2]);
                int a = handlesAlpha ? Integer.parseInt(split[3]) : 255;
                if (!byteCheck(r) || !byteCheck(g) || !byteCheck(b) || !byteCheck(a)) {
                    return;
                }
                setFromColor(Color.rgbai(r, g, b, a));
            } catch (NumberFormatException e) {
                // ignore any NumberFormatException
            }
        }
    }

    private boolean byteCheck(int val) {
        return val >= 0 && val <= 255;
    }

    private void drawColorBar(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        for (int i = 0; i < width; i++) {
            GuiUtils.fill(guiGraphics, x + i, y, 1, height, Color.hsv(((float) i / width) * 360F, 1, 1).argb());
        }
    }

    private void drawAlphaBar(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        Color hsv = Color.hsv(hue, saturation, value);
        for (int i = 0; i < width; i++) {
            GuiUtils.fill(guiGraphics, x + i, y, 1, height, hsv.alpha((float) i / width).argb());
        }
    }

    private void drawTransparencyGrid(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        if (handlesAlpha) {
            guiGraphics.blit(TRANSPARENCY_GRID, x, y, 0, 0, width, height);
        }
    }

    @Override
    public boolean charTyped(char c, int keyCode) {
        boolean ret = super.charTyped(c, keyCode);
        if (textField.canWrite()) {
            updateColorFromText();
        }
        return ret;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean ret = super.keyPressed(keyCode, scanCode, modifiers);
        if (textField.canWrite()) {
            //Update color if the key caused a change to the text contents
            if (Screen.isPaste(keyCode) || Screen.isCut(keyCode) || keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE) {
                updateColorFromText();
            }
        }
        return ret;
    }

    public class GuiColorView extends GuiElement {

        @Nullable
        private Tooltip lastTooltip = null;
        @Nullable
        private Color lastColor = null;

        public GuiColorView(IGuiWrapper gui, int x, int y, int width, int height) {
            super(gui, x, y, width, height);
        }

        @Override
        public void updateTooltip(int mouseX, int mouseY) {
            Color color = getColor();
            if (!color.equals(lastColor)) {
                lastColor = color;
                String hex;
                if (GuiColorWindow.this.handlesAlpha) {
                    hex = TextUtils.hex(false, 4, color.argb());
                } else {
                    hex = TextUtils.hex(false, 3, color.rgb());
                }
                lastTooltip = TooltipUtils.create(MekanismLang.GENERIC_HEX.translateColored(EnumColor.GRAY, hex));
            }
            setTooltip(lastTooltip);
        }

        @Override
        public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
            drawTransparencyGrid(guiGraphics, relativeX, relativeY, width, height);
            Color c = getColor();
            GuiUtils.fill(guiGraphics, relativeX, relativeY, width, height, c.argb());
        }
    }

    private abstract static class GuiPicker extends GuiElement {

        public GuiPicker(IGuiWrapper gui, int x, int y, int width, int height) {
            super(gui, x, y, width, height);
        }

        protected abstract void set(double mouseX, double mouseY);

        @Override
        public void onClick(double mouseX, double mouseY, int button) {
            super.onClick(mouseX, mouseY, button);
            set(mouseX, mouseY);
            setDragging(true);
        }

        @Override
        public void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
            super.onDrag(mouseX, mouseY, deltaX, deltaY);
            if (isDragging()) {
                set(mouseX, mouseY);
            }
        }
    }

    public class GuiShadePicker extends GuiPicker {

        public GuiShadePicker(IGuiWrapper gui, int x, int y, int width, int height) {
            super(gui, x, y, width, height);
        }

        @Override
        public void renderBackgroundOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY) {
            super.renderBackgroundOverlay(guiGraphics, mouseX, mouseY);
            drawTiledGradient(guiGraphics, relativeX, relativeY, width, height);
            int posX = relativeX + Math.round(GuiColorWindow.this.saturation * width) - 2;
            int posY = relativeY + Math.round((1 - GuiColorWindow.this.value) * height) - 2;
            GuiUtils.drawOutline(guiGraphics, posX, posY, 5, 5, 0xFFFFFFFF);
            //Fill the selection in without taking alpha into account
            GuiUtils.fill(guiGraphics, posX + 1, posY + 1, 3, 3, getColor().alpha(1.0).argb());
        }

        @Override
        protected void set(double mouseX, double mouseY) {
            float newS = (float) (mouseX - getX()) / width;
            GuiColorWindow.this.saturation = Mth.clamp(newS, 0, 1);
            float newV = (float) (mouseY - getY()) / height;
            GuiColorWindow.this.value = 1 - Mth.clamp(newV, 0, 1);
            updateTextFromColor();
            updateArmorPreview();
        }
    }

    public class GuiHuePicker extends GuiPicker {

        public GuiHuePicker(IGuiWrapper gui, int x, int y, int width, int height) {
            super(gui, x, y, width, height);
        }

        @Override
        public void renderBackgroundOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY) {
            super.renderBackgroundOverlay(guiGraphics, mouseX, mouseY);
            drawColorBar(guiGraphics, relativeX, relativeY, width, height);
            //Draw selector
            int posX = Math.round((GuiColorWindow.this.hue / 360F) * (width - 3));
            guiGraphics.blit(HUE_PICKER, relativeX - 2 + posX, relativeY - 2, 0, 0, 7, 12, 12, 12);
            //Note: This is needed as we want to draw same color in all three pixels instead of each having their own
            GuiUtils.fill(guiGraphics, relativeX + posX, relativeY, 3, 8, Color.hsv(GuiColorWindow.this.hue, 1, 1).argb());
        }

        @Override
        protected void set(double mouseX, double mouseY) {
            float val = (float) (mouseX - getX()) / width;
            GuiColorWindow.this.hue = Mth.clamp(val, 0, 1) * 360F;
            updateTextFromColor();
            updateArmorPreview();
        }
    }

    public class GuiAlphaPicker extends GuiPicker {

        public GuiAlphaPicker(IGuiWrapper gui, int x, int y, int width, int height) {
            super(gui, x, y, width, height);
        }

        @Override
        public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
            //Draw transparency checkerboard
            drawTransparencyGrid(guiGraphics, relativeX, relativeY, width, height);
        }

        @Override
        public void renderBackgroundOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY) {
            super.renderBackgroundOverlay(guiGraphics, mouseX, mouseY);
            //Draw alpha bar
            drawAlphaBar(guiGraphics, relativeX, relativeY, width, height);
            //Draw selector
            int posX = Math.round(GuiColorWindow.this.alpha * (width - 3));
            guiGraphics.blit(HUE_PICKER, relativeX - 2 + posX, relativeY - 2, 0, 0, 7, 12, 12, 12);
            //Note: This is needed as we want to draw same color in all three pixels instead of each having their own
            //Draw transparency checkerboard on the selector
            drawTransparencyGrid(guiGraphics, relativeX + posX, relativeY, 3, 8);
            GuiUtils.fill(guiGraphics, relativeX + posX, relativeY, 3, 8, getColor().argb());
        }

        @Override
        protected void set(double mouseX, double mouseY) {
            float val = (float) (mouseX - getX()) / width;
            GuiColorWindow.this.alpha = Mth.clamp(val, 0, 1);
            updateTextFromColor();
            updateArmorPreview();
        }
    }
}
