package mekanism.client.gui.element.window;

import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.button.GuiCloseButton;
import mekanism.client.gui.element.button.GuiPinButton;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.inventory.container.IGUIWindow;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.inventory.container.SelectedWindowData.WindowPosition;
import mekanism.common.inventory.container.SelectedWindowData.WindowType;
import mekanism.common.lib.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Util;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jspecify.annotations.Nullable;

public class GuiWindow extends GuiElement implements IGUIWindow {

    private static final Color OVERLAY_COLOR = Color.rgbai(60, 60, 60, 128);

    private final SelectedWindowData windowData;
    private double dragX, dragY;
    private int prevDX, prevDY;
    private boolean pinned;

    @Nullable
    private Consumer<GuiWindow> closeListener;
    @Nullable
    private Consumer<GuiWindow> reattachListener;
    private final long msOpened;

    protected InteractionStrategy interactionStrategy = InteractionStrategy.CONTAINER;

    private static int calculateTarget(int lastPosition, int start, int size, int scaledSize, int defaultValue) {
        if (lastPosition == Integer.MAX_VALUE) {
            return defaultValue;
        } else if (start + lastPosition < 0) {
            //If our x position would be off the screen, then we shift it to as close as we can go
            return -start;
        } else if (start + lastPosition + size > scaledSize) {
            //If our window's end would be off the screen shift it to be as close as we can go
            return scaledSize - start - size;
        }
        return lastPosition;
    }

    public GuiWindow(IGuiWrapper gui, int x, int y, int width, int height, WindowType windowType) {
        this(gui, x, y, width, height, windowType == WindowType.UNSPECIFIED ? SelectedWindowData.UNSPECIFIED : new SelectedWindowData(windowType));
    }

    public GuiWindow(IGuiWrapper gui, int x, int y, int width, int height, SelectedWindowData windowData) {
        WindowPosition lastPosition = windowData.getLastPosition();
        int targetX = calculateTarget(lastPosition.x(), gui.getLeftPos(), width, minecraft.getWindow().getGuiScaledWidth(), x);
        int targetY = calculateTarget(lastPosition.y(), gui.getTopPos(), height, minecraft.getWindow().getGuiScaledHeight(), y);
        this.pinned = lastPosition.pinned();
        this.windowData = windowData;
        super(gui, targetX, targetY, width, height);
        isOverlay = true;
        active = true;
        msOpened = Util.getMillis();
        if (!isFocusOverlay()) {
            addCloseButton();
            if (this.windowData.type().canPin()) {
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
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        boolean ret = super.mouseClicked(event, isDoubleClick);
        // drag 'safe area'
        if (isMouseOver(event.x(), event.y())) {
            if (event.y() < getY() + 18) {
                setDragging(true);
                dragX = event.x();
                dragY = event.y();
                prevDX = 0;
                prevDY = 0;
            }
        } else if (!ret && interactionStrategy.allowContainer()) {
            if (gui() instanceof GuiMekanism<?> gui) {
                AbstractContainerMenu c = gui.getMenu();
                if (!(c instanceof IEmptyContainer)) {
                    // allow interaction with slots
                    if (event.x() >= getGuiLeft() && event.x() < getGuiLeft() + getGuiWidth() && event.y() >= getGuiTop() + getGuiHeight() - 90) {
                        return false;
                    }
                }
            }
        }
        //If we didn't interact, and we don't always allow interacting, pretend we did interact in order to prevent background clicking
        return ret || !interactionStrategy.allowAll();
    }

    @Override
    protected void onDrag(MouseButtonEvent event, double deltaX, double deltaY) {
        super.onDrag(event, deltaX, deltaY);
        if (isDragging()) {
            int newDX = (int) Math.round(event.x() - dragX), newDY = (int) Math.round(event.y() - dragY);
            int changeX = Math.clamp(newDX - prevDX, -getX(), minecraft.getWindow().getGuiScaledWidth() - getRight());
            int changeY = Math.clamp(newDY - prevDY, -getY(), minecraft.getWindow().getGuiScaledHeight() - getBottom());
            prevDX = newDX;
            prevDY = newDY;
            move(changeX, changeY);
        }
    }

    @Override
    public void renderBackgroundOverlay(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        if (isFocusOverlay()) {
            //TODO - 26.2: This used to pass rgba instead of argb, which is wrong. See if the color overlay still renders as expected, or if we wanted the messed up values
            MekanismRenderer.renderColorOverlay(guiGraphics, -getGuiLeft(), -getGuiTop(), OVERLAY_COLOR.argb());
        } else {
            //TODO - 26.2: check this vs the old. Looks rather strong on top of other windows
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, GuiMekanism.SHADOW, relativeX - 3, relativeY - 3, width + 6, height + 6, ARGB.color(0.75F, CommonColors.WHITE));
        }
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, GuiMekanism.BASE_BACKGROUND_SLICE, getButtonX(), getButtonY(), getButtonWidth(), getButtonHeight());
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (super.keyPressed(event)) {
            return true;
        }
        if (event.isEscape() && !isPinned()) {
            close();
            return true;
        }
        return false;
    }

    public void setListenerTab(Supplier<? extends GuiElement> elementSupplier) {
        setTabListeners(_ -> elementSupplier.get().active = true, _ -> elementSupplier.get().active = false);
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

    public void renderBlur(GuiGraphicsExtractor guiGraphics) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, GuiMekanism.BLUR, relativeX, relativeY, width, height, ARGB.color(0.3F, CommonColors.WHITE));
    }

    public final boolean togglePinned(GuiElement toggler, MouseButtonEvent event, boolean isDoubleClick) {
        togglePinned();
        return true;
    }

    public void togglePinned() {
        pinned = !pinned;
    }

    public boolean isPinned() {
        return pinned;
    }

    public final boolean close(GuiElement closer, MouseButtonEvent event, boolean isDoubleClick) {
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
    public void drawTitleText(GuiGraphicsExtractor guiGraphics, Component text, int y) {
        if (isFocusOverlay()) {
            super.drawTitleText(guiGraphics, text, y);
        } else {
            //Adjust spacing for close button and any other buttons like side config's auto eject
            drawTitleTextTextWithOffset(guiGraphics, text, getTitlePadStart(), y, getImageWidth() - getTitlePadEnd());
        }
    }

    /// @apiNote Only used if not a [#isFocusOverlay()]
    protected int getTitlePadStart() {
        if (windowData.type().canPin()) {
            return 14 + GuiPinButton.WIDTH;
        }
        return 12;
    }

    /// @apiNote Only used if not a [#isFocusOverlay()]
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
