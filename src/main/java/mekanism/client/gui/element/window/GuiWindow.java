package mekanism.client.gui.element.window;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.gui.element.button.GuiCloseButton;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.lib.Color;
import net.minecraft.inventory.container.Container;
import org.lwjgl.glfw.GLFW;

public class GuiWindow extends GuiTexturedElement {

    private static final Color OVERLAY_COLOR = Color.rgbai(60, 60, 60, 128);

    private Consumer<GuiWindow> closeListener;
    private Consumer<GuiWindow> reattachListener;

    private boolean dragging = false;
    private double dragX, dragY;
    private int prevDX, prevDY;

    protected InteractionStrategy interactionStrategy = InteractionStrategy.CONTAINER;

    public GuiWindow(IGuiWrapper gui, int x, int y, int width, int height) {
        super(GuiMekanism.BASE_BACKGROUND, gui, x, y, width, height);
        isOverlay = true;
        active = true;
        if (!isFocusOverlay()) {
            addCloseButton();
        }
    }

    public void onFocusLost() {
        //TODO: Validate onFocusLost gets called properly
    }

    public void onFocused() {
    }

    protected void addCloseButton() {
        addChild(new GuiCloseButton(gui(), this.x + 6, this.y + 6, this));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean ret = super.mouseClicked(mouseX, mouseY, button);
        // drag 'safe area'
        if (isMouseOver(mouseX, mouseY)) {
            if (mouseY < y + 18) {
                dragging = true;
                dragX = mouseX;
                dragY = mouseY;
                prevDX = 0;
                prevDY = 0;
            }
        } else if (!ret && interactionStrategy.allowContainer()) {
            if (gui() instanceof GuiMekanism) {
                Container c = ((GuiMekanism<?>) gui()).getContainer();
                if (!(c instanceof IEmptyContainer)) {
                    // allow interaction with slots
                    if (mouseX >= getGuiLeft() && mouseX < getGuiLeft() + getGuiWidth() && mouseY >= getGuiTop() + getGuiHeight() - 90) {
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
        super.onDrag(mouseX, mouseY, mouseXOld, mouseYOld);
        if (dragging) {
            int newDX = (int) Math.round(mouseX - dragX), newDY = (int) Math.round(mouseY - dragY);
            int changeX = Math.max(-x, Math.min(minecraft.getMainWindow().getScaledWidth() - (x + width), newDX - prevDX));
            int changeY = Math.max(-y, Math.min(minecraft.getMainWindow().getScaledHeight() - (y + height), newDY - prevDY));
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
    public void renderBackgroundOverlay(MatrixStack matrix, int mouseX, int mouseY) {
        if (isFocusOverlay()) {
            MekanismRenderer.renderColorOverlay(matrix, 0, 0, minecraft.getMainWindow().getScaledWidth(), minecraft.getMainWindow().getScaledHeight(), OVERLAY_COLOR.rgba());
        } else {
            RenderSystem.color4f(1, 1, 1, 0.75F);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            GuiUtils.renderBackgroundTexture(matrix, GuiMekanism.SHADOW, 4, 4, getButtonX() - 3, getButtonY() - 3, getButtonWidth() + 6, getButtonHeight() + 6, 256, 256);
            MekanismRenderer.resetColor();
        }
        minecraft.textureManager.bindTexture(getResource());
        renderBackgroundTexture(matrix, getResource(), 4, 4);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return false;
    }

    public void setListenerTab(Supplier<? extends GuiElement> elementSupplier) {
        setTabListeners(window -> elementSupplier.get().active = true, window -> elementSupplier.get().active = false);
    }

    public void setTabListeners(Consumer<GuiWindow> closeListener, Consumer<GuiWindow> reattachListener) {
        this.closeListener = closeListener;
        this.reattachListener = reattachListener;
    }

    @Override
    public void resize(int prevLeft, int prevTop, int left, int top) {
        super.resize(prevLeft, prevTop, left, top);
        if (reattachListener != null) {
            reattachListener.accept(this);
        }
    }

    public void renderBlur(MatrixStack matrix) {
        RenderSystem.color4f(1, 1, 1, 0.3F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        GuiUtils.renderBackgroundTexture(matrix, GuiMekanism.BLUR, 4, 4, relativeX, relativeY, width, height, 256, 256);
        MekanismRenderer.resetColor();
    }

    public void close() {
        children.forEach(GuiElement::onWindowClose);
        gui().removeWindow(this);
        if (gui() instanceof GuiMekanism) {
            ((GuiMekanism<?>) gui()).setListener(null);
        }
        if (closeListener != null) {
            closeListener.accept(this);
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
