package mekanism.client.gui.element;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.function.Supplier;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.button.GuiCloseButton;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.lib.Color;
import net.minecraft.inventory.container.Container;
import org.lwjgl.glfw.GLFW;

public class GuiWindow extends GuiTexturedElement {

    private static final Color OVERLAY_COLOR = Color.rgbai(60, 60, 60, 128);

    private Runnable closeListener;
    private Runnable reattachListener;

    private boolean dragging = false;
    private double dragX, dragY;
    private int prevDX, prevDY;

    protected InteractionStrategy interactionStrategy = InteractionStrategy.CONTAINER;

    public GuiWindow(IGuiWrapper gui, int x, int y, int width, int height) {
        super(GuiMekanism.BASE_BACKGROUND, gui, x, y, width, height);
        isOverlay = true;
        field_230693_o_ = true;
        if (!isFocusOverlay()) {
            addCloseButton();
        }
    }

    protected void addCloseButton() {
        addChild(new GuiCloseButton(getGuiObj(), this.field_230690_l_ + 6, this.field_230691_m_ + 6, this));
    }

    @Override
    public boolean func_231044_a_(double mouseX, double mouseY, int button) {
        boolean ret = super.func_231044_a_(mouseX, mouseY, button);
        // drag 'safe area'
        if (isMouseOver(mouseX, mouseY)) {
            if (mouseY < field_230691_m_ + 18) {
                dragging = true;
                dragX = mouseX;
                dragY = mouseY;
                prevDX = 0;
                prevDY = 0;
            }
        } else if (!ret && interactionStrategy.allowContainer()) {
            if (guiObj instanceof GuiMekanism) {
                Container c = ((GuiMekanism<?>) guiObj).getContainer();
                if (!(c instanceof IEmptyContainer)) {
                    // allow interaction with slots
                    if (mouseX >= guiObj.getLeft() && mouseX < guiObj.getLeft() + guiObj.getWidth() && mouseY >= guiObj.getTop() + guiObj.getHeight() - 90) {
                        return false;
                    }
                }
            }
        }
        // always return true to prevent background clicking
        return ret || !interactionStrategy.allowAll();
    }

    @Override
    public void onDrag(double mouseX, double mouseY, double mouseXOld, double mouseYOld) {
        if (dragging) {
            int newDX = (int) Math.round(mouseX - dragX), newDY = (int) Math.round(mouseY - dragY);
            int changeX = Math.max(-field_230690_l_, Math.min(minecraft.getMainWindow().getScaledWidth() - (field_230690_l_ + field_230688_j_), newDX - prevDX));
            int changeY = Math.max(-field_230691_m_, Math.min(minecraft.getMainWindow().getScaledHeight() - (field_230691_m_ + field_230689_k_), newDY - prevDY));
            prevDX = newDX;
            prevDY = newDY;
            move(changeX, changeY);
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        super.onRelease(mouseX, mouseY);
        dragging = false;
    }

    @Override
    public void drawButton(int mouseX, int mouseY) {
    }

    @Override
    public void renderBackgroundOverlay(int mouseX, int mouseY) {
        if (isFocusOverlay()) {
            MekanismRenderer.renderColorOverlay(0, 0, minecraft.getMainWindow().getScaledWidth(), minecraft.getMainWindow().getScaledHeight(), OVERLAY_COLOR.rgba());
        } else {
            RenderSystem.color4f(1, 1, 1, 0.75F);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            GuiUtils.renderBackgroundTexture(GuiMekanism.SHADOW, 4, 4, getButtonX() - 3, getButtonY() - 3, getButtonWidth() + 6, getButtonHeight() + 6, 256, 256);
            MekanismRenderer.resetColor();
        }
        minecraft.textureManager.bindTexture(getResource());
        renderBackgroundTexture(getResource(), 4, 4);
    }

    @Override
    public boolean func_231046_a_(int keyCode, int scanCode, int modifiers) {
        if (super.func_231046_a_(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return false;
    }

    public void setListenerTab(Supplier<? extends GuiElement> elementSupplier) {
        closeListener = () -> elementSupplier.get().field_230693_o_ = true;
        reattachListener = () -> elementSupplier.get().field_230693_o_ = false;
    }

    @Override
    public void resize(int prevLeft, int prevTop, int left, int top) {
        super.resize(prevLeft, prevTop, left, top);
        if (reattachListener != null) {
            reattachListener.run();
        }
    }

    public void renderBlur() {
        RenderSystem.color4f(1, 1, 1, 0.3F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        GuiUtils.renderBackgroundTexture(GuiMekanism.BLUR, 4, 4, relativeX, relativeY, field_230688_j_, field_230689_k_, 256, 256);
        MekanismRenderer.resetColor();
    }

    public void close() {
        children.forEach(GuiElement::onWindowClose);
        guiObj.removeWindow(this);
        if (guiObj instanceof GuiMekanism) {
            ((GuiMekanism<?>) guiObj).setFocused(null);
        }
        if (closeListener != null) {
            closeListener.run();
        }
    }

    protected boolean isFocusOverlay() {
        return false;
    }

    public enum InteractionStrategy {
        NONE,
        CONTAINER,
        ALL;

        boolean allowContainer() {
            return this != NONE;
        }

        boolean allowAll() {
            return this == ALL;
        }
    }
}
