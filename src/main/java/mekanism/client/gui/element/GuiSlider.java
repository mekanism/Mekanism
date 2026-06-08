package mekanism.client.gui.element;

import java.util.function.DoubleConsumer;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class GuiSlider extends GuiElement {

    private static final Identifier SLIDER = MekanismUtils.getResource(ResourceType.GUI, "smooth_slider.png");

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
    public void renderBackgroundOverlay(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        super.renderBackgroundOverlay(guiGraphics, mouseX, mouseY);
        GuiUtils.fill(guiGraphics, relativeX + 2, relativeY + 3, width - 4, 6, 0xFF555555);
        int posX = (int) (value * (width - 6));
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, SLIDER, relativeX + posX, relativeY, 0, 0, 7, 12, 12, 12);
    }

    @Override
    public void onClick(@NotNull MouseButtonEvent event, boolean isDoubleClick) {
        super.onClick(event, isDoubleClick);
        set(event.x());
        setDragging(true);
    }

    @Override
    protected void onDrag(@NotNull MouseButtonEvent event, double deltaX, double deltaY) {
        super.onDrag(event, deltaX, deltaY);
        if (isDragging()) {
            set(event.x());
        }
    }

    @Override
    public boolean keyPressed(@NotNull KeyEvent event) {
        if (super.keyPressed(event)) {
            return true;
        }
        double shift;
        if (isPreviousButton(event) && value > 0) {
            shift = -0.01;
        } else if (isNextButton(event) && value < 1) {
            shift = 0.01;
        } else {
            return false;
        }
        value = Math.clamp(value + shift, 0, 1);
        callback.accept(value);
        return true;
    }

    private void set(double mouseX) {
        double oldValue = value;
        value = Math.clamp((mouseX - getX() - 2) / (width - 6), 0, 1);
        if (!Mth.equal(value, oldValue)) {
            callback.accept(value);
        }
    }

    private boolean isPreviousButton(InputWithModifiers key) {
        return key.isUp() || key.isLeft();
    }

    private boolean isNextButton(InputWithModifiers key) {
        return key.isDown() || key.isRight();
    }
}
