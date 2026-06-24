package mekanism.client.gui.element.window;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.IntConsumer;
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
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.SelectedWindowData.WindowType;
import mekanism.common.lib.Color;
import mekanism.common.util.text.InputValidator;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.jspecify.annotations.Nullable;

public class GuiColorWindow extends GuiWindow {

    public static final Identifier TRANSPARENCY_GRID = Mekanism.rl("transparency_grid");
    private static final Identifier HUE_PICKER = Mekanism.rl("slider/color_picker");
    private static final int S_TILES = 10, V_TILES = 10;

    private final GuiTextField textField;
    private final boolean handlesAlpha;
    @Nullable
    private final IntConsumer updatePreviewColor;
    @Nullable
    private final Runnable previewReset;

    private float hue;
    private float saturation = 0.5F;
    private float value = 0.5F;
    private float alpha = 1;

    public GuiColorWindow(IGuiWrapper gui, int x, int y, boolean handlesAlpha, Color initialColor, IntConsumer callback) {
        this(gui, x, y, handlesAlpha, initialColor, callback, null, null, null);
    }

    public GuiColorWindow(IGuiWrapper gui, int x, int y, boolean handlesAlpha, Color initialColor, IntConsumer callback, @Nullable Supplier<? extends LivingEntityRenderState> armorPreview,
          @Nullable IntConsumer updatePreviewColor, @Nullable Runnable previewReset) {
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
        addChild(new TranslationButton(gui, relativeX + 98 + extraWidth, relativeY + height - 21, 54, 14, MekanismLang.BUTTON_CONFIRM, (element, event, isDoubleClick) -> {
            callback.accept(colorAsInt());
            return close(element, event, isDoubleClick);
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

    private int colorAsInt() {
        Color color = getColor();
        return this.handlesAlpha ? color.argb() : color.rgb();
    }

    public void setColor(Color color) {
        setFromColor(color);
        updateTextFromColor();
    }

    @Override
    public void renderForeground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);
        drawTitleText(guiGraphics, MekanismLang.COLOR_PICKER.translate(), 6);
        ILangEntry entry = handlesAlpha ? MekanismLang.RGBA : MekanismLang.RGB;
        drawScrollingString(guiGraphics, entry.translate(), 2, height - 18, TextAlignment.RIGHT, titleTextColor(), textField.getRelativeX() - relativeX - 2, 2, false);
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
        alpha = handlesAlpha ? c.af() : 1;
        if (updatePreviewColor != null) {
            updatePreviewColor.accept(handlesAlpha ? c.argb() : c.rgb());
        }
    }

    private void updateArmorPreview() {
        if (updatePreviewColor != null) {
            updatePreviewColor.accept(colorAsInt());
        }
    }

    private void updateColorFromText() {
        String[] split = textField.getText().split(",");
        if (split.length == (handlesAlpha ? 4 : 3)) {
            try {
                int r = Integer.parseInt(split[0]);
                int g = Integer.parseInt(split[1]);
                int b = Integer.parseInt(split[2]);
                int a = handlesAlpha ? Integer.parseInt(split[3]) : 0xFF;
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
        return val >= 0 && val <= 0xFF;
    }

    private void drawTransparencyGrid(GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height) {
        if (handlesAlpha) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, TRANSPARENCY_GRID, x, y, width, height);
        }
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        boolean ret = super.charTyped(event);
        if (textField.canWrite()) {
            updateColorFromText();
        }
        return ret;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        boolean ret = super.keyPressed(event);
        if (textField.canWrite()) {
            //Update color if the key caused a change to the text contents
            if (event.isPaste() || event.isCut() || event.key() == InputConstants.KEY_BACKSPACE || event.key() == InputConstants.KEY_DELETE) {
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
        public void drawBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
            super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
            drawTransparencyGrid(guiGraphics, relativeX, relativeY, width, height);
            GuiUtils.fill(guiGraphics, relativeX, relativeY, width, height, getColor().argb());
        }
    }

    private abstract static class GuiPicker extends GuiElement {

        public GuiPicker(IGuiWrapper gui, int x, int y, int width, int height) {
            super(gui, x, y, width, height);
        }

        protected abstract void set(double mouseX, double mouseY);

        @Override
        public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
            super.onClick(event, isDoubleClick);
            set(event.x(), event.y());
            setDragging(true);
        }

        @Override
        protected void onDrag(MouseButtonEvent event, double deltaX, double deltaY) {
            super.onDrag(event, deltaX, deltaY);
            if (isDragging()) {
                set(event.x(), event.y());
            }
        }
    }

    public class GuiShadePicker extends GuiPicker {

        public GuiShadePicker(IGuiWrapper gui, int x, int y, int width, int height) {
            super(gui, x, y, width, height);
        }

        @Override
        public void renderBackgroundOverlay(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
            super.renderBackgroundOverlay(guiGraphics, mouseX, mouseY);
            drawTiledGradient(guiGraphics, relativeX, relativeY, width, height);
            int posX = relativeX + Math.round(GuiColorWindow.this.saturation * width) - 2;
            int posY = relativeY + Math.round((1 - GuiColorWindow.this.value) * height) - 2;
            GuiUtils.drawOutline(guiGraphics, posX, posY, 5, 5, CommonColors.WHITE);
            //Fill the selection in without taking alpha into account
            GuiUtils.fill(guiGraphics, posX + 1, posY + 1, 3, 3, ARGB.opaque(colorAsInt()));
        }

        @Override
        protected void set(double mouseX, double mouseY) {
            float newS = (float) (mouseX - getX()) / width;
            GuiColorWindow.this.saturation = Math.clamp(newS, 0, 1);
            float newV = (float) (mouseY - getY()) / height;
            GuiColorWindow.this.value = 1 - Math.clamp(newV, 0, 1);
            updateTextFromColor();
            updateArmorPreview();
        }

        private void drawTiledGradient(GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height) {
            int tileWidth = Math.round((float) width / S_TILES);
            int tileHeight = Math.round((float) height / V_TILES);
            for (int i = 0; i < V_TILES; i++) {
                float minV = (float) i / V_TILES, maxV = (float) (i + 1) / V_TILES;
                for (int j = 0; j < S_TILES; j++) {
                    float minS = (float) j / S_TILES, maxS = (float) (j + 1) / S_TILES;
                    int tl = Color.hsv(hue, minS, maxV).argb(), tr = Color.hsv(hue, maxS, maxV).argb(),
                          bl = Color.hsv(hue, minS, minV).argb(), br = Color.hsv(hue, maxS, minV).argb();
                    int startX = x + j * tileWidth;
                    int startY = y + (V_TILES - i - 1) * tileHeight;
                    guiGraphics.submitGuiElementRenderState(new ColorGradientRenderState(startX, startY, startX + tileWidth, startY + tileHeight, tl, tr, bl, br,
                          new Matrix3x2f(guiGraphics.pose()), guiGraphics.peekScissorStack()));
                }
            }
        }

        private record ColorGradientRenderState(int x0, int y0, int x1, int y1, int topLeftColor, int topRightColor, int bottomLeftColor, int bottomRightColor,
                                                Matrix3x2fc pose, @Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds
        ) implements GuiElementRenderState {

            public ColorGradientRenderState(int x0, int y0, int x1, int y1, int topLeftColor, int topRightColor, int bottomLeftColor, int bottomRightColor, Matrix3x2fc pose,
                  @Nullable ScreenRectangle scissorArea) {
                ScreenRectangle bounds = new ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose);
                this(x0, y0, x1, y1, topLeftColor, topRightColor, bottomLeftColor, bottomRightColor, pose, scissorArea,
                      scissorArea == null ? bounds : scissorArea.intersection(bounds));
            }

            @Override
            public void buildVertices(VertexConsumer vertexConsumer) {
                vertexConsumer.addVertexWith2DPose(pose, x0, y1).setColor(bottomLeftColor);
                vertexConsumer.addVertexWith2DPose(pose, x1, y1).setColor(bottomRightColor);
                vertexConsumer.addVertexWith2DPose(pose, x1, y0).setColor(topRightColor);
                vertexConsumer.addVertexWith2DPose(pose, x0, y0).setColor(topLeftColor);
            }

            @Override
            public RenderPipeline pipeline() {
                return RenderPipelines.GUI;
            }

            @Override
            public TextureSetup textureSetup() {
                return TextureSetup.noTexture();
            }
        }
    }

    public class GuiHuePicker extends GuiPicker {

        public GuiHuePicker(IGuiWrapper gui, int x, int y, int width, int height) {
            super(gui, x, y, width, height);
        }

        @Override
        public void renderBackgroundOverlay(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
            super.renderBackgroundOverlay(guiGraphics, mouseX, mouseY);
            drawColorBar(guiGraphics, relativeX, relativeY, width, height);
            //Draw selector
            int posX = Math.round((GuiColorWindow.this.hue / 360F) * (width - 3));
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, HUE_PICKER, relativeX - 2 + posX, relativeY - 2, 7, 12);
            //Note: This is needed as we want to draw same color in all three pixels instead of each having their own
            GuiUtils.fill(guiGraphics, relativeX + posX, relativeY, 3, 8, Color.hsv(GuiColorWindow.this.hue, 1, 1).argb());
        }

        @Override
        protected void set(double mouseX, double mouseY) {
            float val = (float) (mouseX - getX()) / width;
            GuiColorWindow.this.hue = Math.clamp(val, 0, 1) * 360F;
            updateTextFromColor();
            updateArmorPreview();
        }

        private void drawColorBar(GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height) {
            for (int i = 0; i < width; i++) {
                GuiUtils.fill(guiGraphics, x + i, y, 1, height, Color.hsv(360 * ((float) i / width), 1, 1).argb());
            }
        }
    }

    public class GuiAlphaPicker extends GuiPicker {

        public GuiAlphaPicker(IGuiWrapper gui, int x, int y, int width, int height) {
            super(gui, x, y, width, height);
        }

        @Override
        public void drawBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
            super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
            //Draw transparency checkerboard
            drawTransparencyGrid(guiGraphics, relativeX, relativeY, width, height);
        }

        @Override
        public void renderBackgroundOverlay(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
            super.renderBackgroundOverlay(guiGraphics, mouseX, mouseY);
            //Draw alpha bar
            drawAlphaBar(guiGraphics, relativeX, relativeY, width, height);
            //Draw selector
            int posX = Math.round(GuiColorWindow.this.alpha * (width - 3));
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, HUE_PICKER, relativeX - 2 + posX, relativeY - 2, 7, 12);
            //Note: This is needed as we want to draw same color in all three pixels instead of each having their own
            //Draw transparency checkerboard on the selector
            drawTransparencyGrid(guiGraphics, relativeX + posX, relativeY, 3, 8);
            GuiUtils.fill(guiGraphics, relativeX + posX, relativeY, 3, 8, getColor().argb());
        }

        @Override
        protected void set(double mouseX, double mouseY) {
            float val = (float) (mouseX - getX()) / width;
            GuiColorWindow.this.alpha = Math.clamp(val, 0, 1);
            updateTextFromColor();
            updateArmorPreview();
        }

        private void drawAlphaBar(GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height) {
            int rgb = Color.hsv(hue, saturation, value).rgb();
            for (int i = 0; i < width; i++) {
                GuiUtils.fill(guiGraphics, x + i, y, 1, height, ARGB.color((float) i / width, rgb));
            }
        }
    }
}
