package mekanism.client.gui.element.window;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.GuiRelativeElement;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.text.BackgroundType;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.gui.element.text.InputValidator;
import mekanism.common.MekanismLang;
import mekanism.common.lib.Color;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.opengl.GL11;

public class GuiColorWindow extends GuiWindow {

    private static final ResourceLocation HUE_PICKER = MekanismUtils.getResource(ResourceType.GUI, "color_picker.png");

    private final GuiTextField textField;

    private final GuiShadePicker shadePicker;
    private final GuiHuePicker huePicker;

    private float hue;
    private float saturation = 0.5F;
    private float value = 0.5F;

    public GuiColorWindow(IGuiWrapper gui, int x, int y, Consumer<Color> callback) {
        super(gui, x, y, 160, 140);
        interactionStrategy = InteractionStrategy.NONE;
        addChild(new GuiElementHolder(gui, x + 6, y + 17, 43, 82));
        addChild(new GuiColorView(gui, x + 7, y + 18, 41, 80));

        addChild(new GuiElementHolder(gui, x + 52, y + 17, 102, 82));
        addChild(shadePicker = new GuiShadePicker(gui, x + 53, y + 18, 100, 80));

        addChild(new GuiElementHolder(gui, x + 6, y + 103, 148, 10));
        addChild(huePicker = new GuiHuePicker(gui, x + 7, y + 104, 146, 8));

        addChild(textField = new GuiTextField(gui, x + 30, y + getButtonHeight() - 20, 67, 12));
        textField.setMaxStringLength(11);
        textField.setInputValidator(InputValidator.DIGIT.or(c -> c == ','));
        textField.setBackground(BackgroundType.ELEMENT_HOLDER);
        addChild(new TranslationButton(gui, x + 100, y + getButtonHeight() - 21, 54, 14, MekanismLang.BUTTON_CONFIRM, () -> {
            callback.accept(getColor());
            close();
        }));
        setColor(Color.rgbi(128, 70, 70));
    }

    public Color getColor() {
        return Color.hsv(hue, saturation, value);
    }

    public void setColor(Color color) {
        setFromColor(color);
        updateTextFromColor();
    }

    @Override
    public void renderForeground(MatrixStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);

        drawCenteredTextScaledBound(matrix, MekanismLang.COLOR_PICKER.translate(), 120, 6, titleTextColor());
        drawTextScaledBound(matrix, MekanismLang.RGB.translate(), relativeX + 7, relativeY + getButtonHeight() - 17.5F, titleTextColor(), 20);
    }

    private static final int S_TILES = 10, V_TILES = 10;

    private void drawTiledGradient(MatrixStack matrix, int x, int y, int width, int height) {
        int tileWidth = Math.round((float) width / S_TILES);
        int tileHeight = Math.round((float) height / V_TILES);
        for (int i = 0; i < 10; i++) {
            float minV = (float) i / V_TILES, maxV = (float) (i + 1) / V_TILES;
            for (int j = 0; j < 10; j++) {
                float minS = (float) j / S_TILES, maxS = (float) (j + 1) / S_TILES;
                Color tl = Color.hsv(hue, minS, maxV), tr = Color.hsv(hue, maxS, maxV), bl = Color.hsv(hue, minS, minV), br = Color.hsv(hue, maxS, minV);
                drawGradient(matrix, x + j * tileWidth, y + (V_TILES - i - 1) * tileHeight, tileWidth, tileHeight, tl, tr, bl, br);
            }
        }
    }

    private void drawGradient(MatrixStack matrix, int x, int y, int width, int height, Color tl, Color tr, Color bl, Color br) {
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        Matrix4f matrix4f = matrix.getLast().getMatrix();
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(matrix4f, x, y + height, 0).color(bl.rf(), bl.gf(), bl.bf(), bl.af()).endVertex();
        buffer.pos(matrix4f, x + width, y + height, 0).color(br.rf(), br.gf(), br.bf(), br.af()).endVertex();
        buffer.pos(matrix4f, x + width, y, 0).color(tr.rf(), tr.gf(), tr.bf(), tr.af()).endVertex();
        buffer.pos(matrix4f, x, y, 0).color(tl.rf(), tl.gf(), tl.bf(), tl.af()).endVertex();
        buffer.finishDrawing();
        WorldVertexBufferUploader.draw(buffer);
        RenderSystem.shadeModel(GL11.GL_FLAT);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
    }

    private void updateTextFromColor() {
        int[] rgb = getColor().rgbArray();
        textField.setText(rgb[0] + "," + rgb[1] + "," + rgb[2]);
    }

    private void setFromColor(Color c) {
        double[] hsv = c.hsvArray();
        hue = (float) hsv[0];
        saturation = (float) hsv[1];
        value = (float) hsv[2];
    }

    private void updateColorFromText() {
        String[] split = textField.getText().split(",");
        if (split.length == 3) {
            try {
                int r = Integer.parseInt(split[0]);
                int g = Integer.parseInt(split[1]);
                int b = Integer.parseInt(split[2]);
                if (!byteCheck(r) || !byteCheck(g) || !byteCheck(b)) {
                    return;
                }
                setFromColor(Color.rgbi(r, g, b));
            } catch (NumberFormatException e) {
                // ignore any NumberFormatException
            }
        }
    }

    private boolean byteCheck(int val) {
        return val >= 0 && val <= 255;
    }

    private void drawColorBar(MatrixStack matrix, int x, int y, int width, int height) {
        for (int i = 0; i < width; i++) {
            GuiUtils.fill(matrix, x + i, y, 1, height, Color.hsv(((float) i / width) * 360F, 1, 1).argb());
        }
    }

    @Override
    public boolean charTyped(char c, int keyCode) {
        boolean ret = super.charTyped(c, keyCode);
        updateColorFromText();
        return ret;
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        super.onRelease(mouseX, mouseY);
        huePicker.isDragging = false;
        shadePicker.isDragging = false;
    }

    public class GuiColorView extends GuiRelativeElement {

        public GuiColorView(IGuiWrapper gui, int x, int y, int width, int height) {
            super(gui, x, y, width, height);
        }

        @Override
        public void renderToolTip(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
            super.renderToolTip(matrix, mouseX, mouseY);
            ITextComponent hex = MekanismLang.GENERIC_HEX.translateColored(EnumColor.GRAY, TextUtils.hex(false, 3, getColor().rgb()));
            guiObj.displayTooltip(matrix, hex, mouseX, mouseY);
        }

        @Override
        public void drawBackground(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
            super.drawBackground(matrix, mouseX, mouseY, partialTicks);

            Color c = Color.hsv(hue, saturation, value);
            GuiUtils.fill(matrix, getButtonX(), getButtonY(), getButtonWidth(), getButtonHeight(), c.argb());
        }
    }

    public class GuiShadePicker extends GuiRelativeElement {

        private boolean isDragging;

        public GuiShadePicker(IGuiWrapper gui, int x, int y, int width, int height) {
            super(gui, x, y, width, height);
        }

        @Override
        public void renderBackgroundOverlay(MatrixStack matrix, int mouseX, int mouseY) {
            super.renderBackgroundOverlay(matrix, mouseX, mouseY);
            drawTiledGradient(matrix, getButtonX(), getButtonY(), getButtonWidth(), getButtonHeight());
            int posX = getButtonX() + Math.round(saturation * getButtonWidth()) - 2;
            int posY = getButtonY() + Math.round((1 - value) * getButtonHeight()) - 2;
            GuiUtils.drawOutline(matrix, posX, posY, 5, 5, 0xFFFFFFFF);
            GuiUtils.fill(matrix, posX + 1, posY + 1, 3, 3, getColor().argb());
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            if (clicked(mouseX, mouseY)) {
                set(mouseX, mouseY);
                isDragging = true;
            }
        }

        @Override
        public void onDrag(double mouseX, double mouseY, double mouseXOld, double mouseYOld) {
            if (isDragging) {
                set(mouseX, mouseY);
            }
        }

        private void set(double mouseX, double mouseY) {
            float newS = (float) (mouseX - getButtonX()) / getButtonWidth();
            saturation = Math.min(1, Math.max(newS, 0));
            float newV = (float) (mouseY - getButtonY()) / getButtonHeight();
            value = 1 - Math.min(1, Math.max(newV, 0));
            updateTextFromColor();
        }
    }

    public class GuiHuePicker extends GuiRelativeElement {

        private boolean isDragging;

        public GuiHuePicker(IGuiWrapper gui, int x, int y, int width, int height) {
            super(gui, x, y, width, height);
        }

        @Override
        public void renderBackgroundOverlay(MatrixStack matrix, int mouseX, int mouseY) {
            super.renderBackgroundOverlay(matrix, mouseX, mouseY);
            drawColorBar(matrix, getButtonX(), getButtonY(), getButtonWidth(), getButtonHeight());
            minecraft.textureManager.bindTexture(HUE_PICKER);
            int posX = Math.round((hue / 360F) * (getButtonWidth() - 3));
            blit(matrix, getButtonX() - 2 + posX, getButtonY() - 2, 0, 0, 7, 12, 12, 12);
            GuiUtils.fill(matrix, getButtonX() + posX, getButtonY(), 3, 8, Color.hsv(hue, 1, 1).argb());
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            if (clicked(mouseX, mouseY)) {
                set(mouseX, mouseY);
                isDragging = true;
            }
        }

        @Override
        public void onDrag(double mouseX, double mouseY, double mouseXOld, double mouseYOld) {
            if (isDragging) {
                set(mouseX, mouseY);
            }
        }

        private void set(double mouseX, double mouseY) {
            float val = (float) (mouseX - getButtonX()) / getButtonWidth();
            hue = Math.min(1, Math.max(val, 0)) * 360F;
            updateTextFromColor();
        }
    }
}
