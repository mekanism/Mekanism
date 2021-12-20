package mekanism.client.gui.element.window;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
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
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.inventory.container.SelectedWindowData.WindowType;
import mekanism.common.lib.Color;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;

public class GuiWindow extends GuiTexturedElement {

    private static final Color OVERLAY_COLOR = Color.rgbai(60, 60, 60, 128);

    private final SelectedWindowData windowData;
    private boolean dragging = false;
    private double dragX, dragY;
    private int prevDX, prevDY;

    private Consumer<GuiWindow> closeListener;
    private Consumer<GuiWindow> reattachListener;

    protected InteractionStrategy interactionStrategy = InteractionStrategy.CONTAINER;

    //TODO - 1.18: Switch this method to returning a record instead of a pair
    private static Pair<Integer, Integer> calculateOpenPosition(IGuiWrapper gui, SelectedWindowData windowData, int x, int y, int width, int height) {
        Pair<Integer, Integer> lastPosition = windowData.getLastPosition();
        int lastX = lastPosition.getFirst();
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
        int lastY = lastPosition.getSecond();
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
        return Pair.of(lastX == Integer.MAX_VALUE ? x : lastX, lastY == Integer.MAX_VALUE ? y : lastY);
    }

    public GuiWindow(IGuiWrapper gui, int x, int y, int width, int height, WindowType windowType) {
        this(gui, x, y, width, height, windowType == WindowType.UNSPECIFIED ? SelectedWindowData.UNSPECIFIED : new SelectedWindowData(windowType));
    }

    public GuiWindow(IGuiWrapper gui, int x, int y, int width, int height, SelectedWindowData windowData) {
        //Hacky system to calculate proper x and y positions
        this(gui, calculateOpenPosition(gui, windowData, x, y, width, height), width, height, windowData);
    }

    private GuiWindow(IGuiWrapper gui, Pair<Integer, Integer> calculatedPosition, int width, int height, SelectedWindowData windowData) {
        super(GuiMekanism.BASE_BACKGROUND, gui, calculatedPosition.getFirst(), calculatedPosition.getSecond(), width, height);
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
            if (gui() instanceof GuiMekanism) {
                Container c = ((GuiMekanism<?>) gui()).getMenu();
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
    public void renderBackgroundOverlay(MatrixStack matrix, int mouseX, int mouseY) {
        if (isFocusOverlay()) {
            MekanismRenderer.renderColorOverlay(matrix, 0, 0, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight(), OVERLAY_COLOR.rgba());
        } else {
            RenderSystem.color4f(1, 1, 1, 0.75F);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            GuiUtils.renderBackgroundTexture(matrix, GuiMekanism.SHADOW, 4, 4, getButtonX() - 3, getButtonY() - 3, getButtonWidth() + 6, getButtonHeight() + 6, 256, 256);
            MekanismRenderer.resetColor();
        }
        minecraft.textureManager.bind(getResource());
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
    public void drawTitleText(MatrixStack matrix, ITextComponent text, float y) {
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
