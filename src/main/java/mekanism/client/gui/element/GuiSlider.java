package mekanism.client.gui.element;

import java.util.function.DoubleConsumer;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class GuiSlider extends GuiElement {

    private static final ResourceLocation SLIDER = MekanismUtils.getResource(ResourceType.GUI, "smooth_slider.png");

    private final DoubleConsumer callback;

    private double value;

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
    public void renderBackgroundOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderBackgroundOverlay(guiGraphics, mouseX, mouseY);
        GuiUtils.fill(guiGraphics, relativeX + 2, relativeY + 3, width - 4, 6, 0xFF555555);
        int posX = (int) (value * (width - 6));
        guiGraphics.blit(SLIDER, relativeX + posX, relativeY, 0, 0, 7, 12, 12, 12);
    }

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

    private void set(double mouseX, double mouseY) {
        value = Mth.clamp(((mouseX - getX() - 2) / (width - 6)), 0, 1);
        callback.accept(value);
    }
}
