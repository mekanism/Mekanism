package mekanism.client.gui.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.function.DoubleConsumer;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;

public class GuiSlider extends GuiRelativeElement {

    private static final ResourceLocation SLIDER = MekanismUtils.getResource(ResourceType.GUI, "smooth_slider.png");

    private final DoubleConsumer callback;

    private double value;
    private boolean isDragging;

    public GuiSlider(IGuiWrapper gui, int x, int y, int width, DoubleConsumer callback) {
        super(gui, x, y, width, 12);
        this.callback = callback;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public void renderBackgroundOverlay(MatrixStack matrix, int mouseX, int mouseY) {
        super.renderBackgroundOverlay(matrix, mouseX, mouseY);
        GuiUtils.fill(matrix, getButtonX() + 2, getButtonY() + 3, getButtonWidth() - 4, 6, 0xFF555555);
        minecraft.textureManager.bindTexture(SLIDER);
        int posX = (int) (value * (getButtonWidth() - 6));
        func_238463_a_(matrix, getButtonX() + posX, getButtonY(), 0, 0, 7, 12, 12, 12);
    }

    @Override
    public void func_231000_a__(double mouseX, double mouseY) {
        super.func_231000_a__(mouseX, mouseY);
        isDragging = false;
    }

    @Override
    public void func_230982_a_(double mouseX, double mouseY) {
        super.func_230982_a_(mouseX, mouseY);
        if (func_230992_c_(mouseX, mouseY)) {
            set(mouseX, mouseY);
            isDragging = true;
        }
    }

    @Override
    public void func_230983_a_(double mouseX, double mouseY, double mouseXOld, double mouseYOld) {
        super.func_230983_a_(mouseX, mouseY, mouseXOld, mouseYOld);
        if (isDragging) {
            set(mouseX, mouseY);
        }
    }

    private void set(double mouseX, double mouseY) {
        value = ((mouseX - getButtonX() - 2) / (getButtonWidth() - 6));
        value = Math.max(0, Math.min(1, value));
        callback.accept(value);
    }
}
