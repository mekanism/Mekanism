package mekanism.client.gui.element.window;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
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
import mekanism.common.inventory.container.IGUIWindow;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.inventory.container.SelectedWindowData.WindowPosition;
import mekanism.common.inventory.container.SelectedWindowData.WindowType;
import mekanism.common.lib.Color;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.lwjgl.glfw.GLFW;

public class GuiWindow extends GuiTexturedElement implements IGUIWindow {

    private static final Color OVERLAY_COLOR = Color.rgbai(60, 60, 60, 128);

    private final SelectedWindowData windowData;
    private boolean dragging = false;
    private double dragX, dragY;
    private int prevDX, prevDY;

    private Consumer<GuiWindow> closeListener;
    private Consumer<GuiWindow> reattachListener;

    protected InteractionStrategy interactionStrategy = InteractionStrategy.CONTAINER;

    private static WindowPosition calculateOpenPosition(IGuiWrapper gui, SelectedWindowData windowData, int x, int y, int width, int height) {
        WindowPosition lastPosition = windowData.getLastPosition();
        int lastX = lastPosition.x();
        if (lastX != Integer.MAX_VALUE) {
            int guiLeft = gui.getLeft();
            if (guiLeft + lastX < 0) {
                //If our x position would be off the screen, then we shift it to as close as we can go
                lastX = -guiLeft;
            } else if (guiLeft + lastX + width > minecraft.getWindow().getGuiScaledWidth()) {
                //If our window's end would be off the screen shift it to be as close as we can go
                lastX = minecraft.getWindow().getGuiScaledWidth() - guiLeft - width;
            }
        }
        int lastY = lastPosition.y();
        if (lastY != Integer.MAX_VALUE) {
            int guiTop = gui.getTop();
            if (guiTop + lastY < 0) {
                //If our y position would be off the screen, then we shift it to as close as we can go
                lastY = -guiTop;
            } else if (guiTop + lastY + height > minecraft.getWindow().getGuiScaledHeight()) {
                //If our window's end would be off the screen shift it to be as close as we can go
                lastY = minecraft.getWindow().getGuiScaledHeight() - guiTop - height;
            }
        }
        return new WindowPosition(lastX == Integer.MAX_VALUE ? x : lastX, lastY == Integer.MAX_VALUE ? y : lastY);
    }

    public GuiWindow(IGuiWrapper gui, int x, int y, int width, int height, WindowType windowType) {
        this(gui, x, y, width, height, windowType == WindowType.UNSPECIFIED ? SelectedWindowData.UNSPECIFIED : new SelectedWindowData(windowType));
    }

    public GuiWindow(IGuiWrapper gui, int x, int y, int width, int height, SelectedWindowData windowData) {
        //Hacky system to calculate proper x and y positions
        this(gui, calculateOpenPosition(gui, windowData, x, y, width, height), width, height, windowData);
    }

    private GuiWindow(IGuiWrapper gui, WindowPosition calculatedPosition, int width, int height, SelectedWindowData windowData) {
        super(GuiMekanism.BASE_BACKGROUND, gui, calculatedPosition.x(), calculatedPosition.y(), width, height);
        this.windowData = windowData;
        isOverlay = true;
        active = true;
        if (!isFocusOverlay()) {
            addCloseButton();
        }
    }

    public void onFocusLost() {
    }

    public void onFocused() {
        gui().setSelectedWindow(windowData);
    }

    protected void addCloseButton() {
        addChild(new GuiCloseButton(gui(), relativeX + 6, relativeY + 6, this));
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
            if (gui() instanceof GuiMekanism<?> gui) {
                AbstractContainerMenu c = gui.getMenu();
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
            int changeX = Math.max(-x, Math.min(minecraft.getWindow().getGuiScaledWidth() - (x + width), newDX - prevDX));
            int changeY = Math.max(-y, Math.min(minecraft.getWindow().getGuiScaledHeight() - (y + height), newDY - prevDY));
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
    public void renderBackgroundOverlay(PoseStack matrix, int mouseX, int mouseY) {
        if (isFocusOverlay()) {
            MekanismRenderer.renderColorOverlay(matrix, 0, 0, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight(), OVERLAY_COLOR.rgba());
        } else {
            RenderSystem.setShaderColor(1, 1, 1, 0.75F);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            GuiUtils.renderBackgroundTexture(matrix, GuiMekanism.SHADOW, 4, 4, getButtonX() - 3, getButtonY() - 3, getButtonWidth() + 6, getButtonHeight() + 6, 256, 256);
            MekanismRenderer.resetColor();
        }
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

    public void renderBlur(PoseStack matrix) {
        RenderSystem.setShaderColor(1, 1, 1, 0.3F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        GuiUtils.renderBackgroundTexture(matrix, GuiMekanism.BLUR, 4, 4, relativeX, relativeY, width, height, 256, 256);
        MekanismRenderer.resetColor();
    }

    public void close() {
        gui().removeWindow(this);
        children.forEach(GuiElement::onWindowClose);
        if (closeListener != null) {
            closeListener.accept(this);
        }
        //Only save new position when we are finally closing a specific window
        windowData.updateLastPosition(relativeX, relativeY);
    }

    protected boolean isFocusOverlay() {
        return false;
    }

    @Override
    public void drawTitleText(PoseStack matrix, Component text, float y) {
        if (isFocusOverlay()) {
            super.drawTitleText(matrix, text, y);
        } else {
            //Adjust spacing for close button and any other buttons like side config's auto eject
            int leftShift = getTitlePadStart();
            int xSize = getXSize() - leftShift - getTitlePadEnd();
            int maxLength = xSize - 12;
            float textWidth = getStringWidth(text);
            float scale = Math.min(1, maxLength / textWidth);
            float left = relativeX + xSize / 2F;
            drawScaledCenteredText(matrix, text, left + leftShift, relativeY + y, titleTextColor(), scale);
        }
    }

    /**
     * @apiNote Only used if not a {@link #isFocusOverlay()}
     */
    protected int getTitlePadStart() {
        return 12;
    }

    /**
     * @apiNote Only used if not a {@link #isFocusOverlay()}
     */
    protected int getTitlePadEnd() {
        return 0;
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
