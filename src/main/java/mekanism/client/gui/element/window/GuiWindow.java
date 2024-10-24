package mekanism.client.gui.element.window;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.gui.element.button.GuiCloseButton;
import mekanism.client.gui.element.button.GuiPinButton;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.inventory.container.IGUIWindow;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.inventory.container.SelectedWindowData.WindowPosition;
import mekanism.common.inventory.container.SelectedWindowData.WindowType;
import mekanism.common.lib.Color;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.lwjgl.glfw.GLFW;

public class GuiWindow extends GuiTexturedElement implements IGUIWindow {

    private static final Color OVERLAY_COLOR = Color.rgbai(60, 60, 60, 128);

    private final SelectedWindowData windowData;
    private double dragX, dragY;
    private int prevDX, prevDY;
    private boolean pinned;

    private Consumer<GuiWindow> closeListener;
    private Consumer<GuiWindow> reattachListener;
    private final long msOpened;

    protected InteractionStrategy interactionStrategy = InteractionStrategy.CONTAINER;

    private static WindowPosition calculateOpenPosition(IGuiWrapper gui, SelectedWindowData windowData, int x, int y, int width, int height) {
        WindowPosition lastPosition = windowData.getLastPosition();
        int lastX = lastPosition.x();
        if (lastX != Integer.MAX_VALUE) {
            int guiLeft = gui.getGuiLeft();
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
            int guiTop = gui.getGuiTop();
            if (guiTop + lastY < 0) {
                //If our y position would be off the screen, then we shift it to as close as we can go
                lastY = -guiTop;
            } else if (guiTop + lastY + height > minecraft.getWindow().getGuiScaledHeight()) {
                //If our window's end would be off the screen shift it to be as close as we can go
                lastY = minecraft.getWindow().getGuiScaledHeight() - guiTop - height;
            }
        }
        return new WindowPosition(lastX == Integer.MAX_VALUE ? x : lastX, lastY == Integer.MAX_VALUE ? y : lastY, lastPosition.pinned());
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
        this.pinned = calculatedPosition.pinned();
        isOverlay = true;
        active = true;
        msOpened = Util.getMillis();
        if (!isFocusOverlay()) {
            addCloseButton();
            if (this.windowData.type.canPin()) {
                addChild(new GuiPinButton(gui(), relativeX + 16, relativeY + 6, this));
            }
        }
    }

    @Override
    public long getTimeOpened() {
        return msOpened;
    }

    public void onFocusLost() {
    }

    public void onFocused() {
        gui().setSelectedWindow(windowData);
    }

    protected void addCloseButton() {
        addChild(new GuiCloseButton(gui(), relativeX + 6, relativeY + 6, this));
    }

    public final InteractionStrategy getInteractionStrategy() {
        return interactionStrategy;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean ret = super.mouseClicked(mouseX, mouseY, button);
        // drag 'safe area'
        if (isMouseOver(mouseX, mouseY)) {
            if (mouseY < getY() + 18) {
                setDragging(true);
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
        //If we didn't interact, and we don't always allow interacting, pretend we did interact in order to prevent background clicking
        return ret || !interactionStrategy.allowAll();
    }

    @Override
    public void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        super.onDrag(mouseX, mouseY, deltaX, deltaY);
        if (isDragging()) {
            int newDX = (int) Math.round(mouseX - dragX), newDY = (int) Math.round(mouseY - dragY);
            int changeX = Mth.clamp(newDX - prevDX, -getX(), minecraft.getWindow().getGuiScaledWidth() - getRight());
            int changeY = Mth.clamp(newDY - prevDY, -getY(), minecraft.getWindow().getGuiScaledHeight() - getBottom());
            prevDX = newDX;
            prevDY = newDY;
            move(changeX, changeY);
        }
    }

    @Override
    public void renderBackgroundOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (isFocusOverlay()) {
            MekanismRenderer.renderColorOverlay(guiGraphics, -getGuiLeft(), -getGuiTop(), OVERLAY_COLOR.rgba());
        } else {
            guiGraphics.setColor(1, 1, 1, 0.75F);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            GuiUtils.renderBackgroundTexture(guiGraphics, GuiMekanism.SHADOW, 4, 4, relativeX - 3, relativeY - 3, width + 6, height + 6, 256, 256);
            MekanismRenderer.resetColor(guiGraphics);
        }
        renderBackgroundTexture(guiGraphics, getResource(), 4, 4);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && !isPinned()) {
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

    public void renderBlur(GuiGraphics guiGraphics) {
        guiGraphics.setColor(1, 1, 1, 0.3F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        GuiUtils.renderBackgroundTexture(guiGraphics, GuiMekanism.BLUR, 4, 4, relativeX, relativeY, width, height, 256, 256);
        MekanismRenderer.resetColor(guiGraphics);
        RenderSystem.enableDepthTest();
    }

    public final boolean togglePinned(GuiElement toggler, double mouseX, double mouseY) {
        togglePinned();
        return true;
    }

    public void togglePinned() {
        pinned = !pinned;
    }

    public boolean isPinned() {
        return pinned;
    }

    public final boolean close(GuiElement closer, double mouseX, double mouseY) {
        close();
        return true;
    }

    public void close() {
        gui().removeWindow(this);
        children().forEach(GuiElement::onWindowClose);
        if (closeListener != null) {
            closeListener.accept(this);
        }
        //Only save new position when we are finally closing a specific window
        windowData.updateLastPosition(relativeX, relativeY, pinned);
    }

    protected boolean isFocusOverlay() {
        return false;
    }

    @Override
    public void drawTitleText(GuiGraphics guiGraphics, Component text, int y) {
        if (isFocusOverlay()) {
            super.drawTitleText(guiGraphics, text, y);
        } else {
            //Adjust spacing for close button and any other buttons like side config's auto eject
            drawTitleTextTextWithOffset(guiGraphics, text, getTitlePadStart(), y, getXSize() - getTitlePadEnd());
        }
    }

    /**
     * @apiNote Only used if not a {@link #isFocusOverlay()}
     */
    protected int getTitlePadStart() {
        if (windowData.type.canPin()) {
            return 14 + GuiPinButton.WIDTH;
        }
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

        public boolean allowContainer() {
            return this != NONE;
        }

        public boolean allowAll() {
            return this == ALL;
        }
    }
}
