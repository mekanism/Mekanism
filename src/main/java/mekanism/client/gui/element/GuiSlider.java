package mekanism.client.gui.element;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.DoubleConsumer;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class GuiSlider extends GuiElement {

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
    public void renderBackgroundOverlay(PoseStack matrix, int mouseX, int mouseY) {
        super.renderBackgroundOverlay(matrix, mouseX, mouseY);
        GuiUtils.fill(matrix, getButtonX() + 2, getButtonY() + 3, getButtonWidth() - 4, 6, 0xFF555555);
        RenderSystem.setShaderTexture(0, SLIDER);
        int posX = (int) (value * (getButtonWidth() - 6));
        blit(matrix, getButtonX() + posX, getButtonY(), 0, 0, 7, 12, 12, 12);
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        super.onRelease(mouseX, mouseY);
        isDragging = false;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
        if (clicked(mouseX, mouseY)) {
            set(mouseX, mouseY);
            isDragging = true;
        }
    }

    @Override
    public void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        super.onDrag(mouseX, mouseY, deltaX, deltaY);
        if (isDragging) {
            set(mouseX, mouseY);
        }
    }

    private void set(double mouseX, double mouseY) {
        value = Mth.clamp(((mouseX - getButtonX() - 2) / (getButtonWidth() - 6)), 0, 1);
        callback.accept(value);
    }
}
