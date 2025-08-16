package mekanism.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.GuiVirtualSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.tab.GuiWarningTab;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.IVirtualSlot;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.IWarningTracker;
import mekanism.common.inventory.warning.WarningTracker;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.lib.collection.LRU;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.Util;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public abstract class GuiMekanism<CONTAINER extends AbstractContainerMenu> extends VirtualSlotContainerScreen<CONTAINER> implements IGuiWrapper {

    public static final ResourceLocation BASE_BACKGROUND = MekanismUtils.getResource(ResourceType.GUI, "base.png");
    public static final ResourceLocation SHADOW = MekanismUtils.getResource(ResourceType.GUI, "shadow.png");
    public static final ResourceLocation BLUR = MekanismUtils.getResource(ResourceType.GUI, "blur.png");
    //TODO: Look into defaulting this to true
    protected boolean dynamicSlots;
    protected boolean initialFocusSet;
    protected final LRU<GuiWindow> windows = new LRU<>();
    public boolean switchingToRecipeViewer;
    @Nullable
    private IWarningTracker warningTracker;
    private long lastMSInitialized;

    private boolean hasClicked = false;

    public static int maxZOffset;

    protected GuiMekanism(CONTAINER container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @NotNull
    @Override
    public BooleanSupplier trackWarning(@NotNull WarningType type, @NotNull BooleanSupplier warningSupplier) {
        if (warningTracker == null) {
            warningTracker = new WarningTracker();
        }
        return warningTracker.trackWarning(type, warningSupplier);
    }

    @Override
    public void removed() {
        if (!switchingToRecipeViewer) {
            //If we are not switching to JEI then run the super close method
            // which will exit the container. We don't want to mark the
            // container as exited if it will be revived when leaving JEI
            // Note: We start by closing all open windows so that any cleanup
            // they need to have done such as saving positions can be done
            windows.forEach(GuiWindow::close);
            super.removed();
        }
    }

    @Override
    protected void init() {
        super.init();
        if (warningTracker != null) {
            //If our warning tracker isn't null (so this isn't the first time we are initializing, such as after resizing)
            // clear out any tracked warnings, so we don't have duplicates being tracked when we add our elements again
            warningTracker.clearTrackedWarnings();
        } else {
            //If we haven't been initialized yet, we can initialize it here
            lastMSInitialized = Util.getMillis();
        }
        addGuiElements();
        if (warningTracker != null) {
            //If we have a warning tracker add it as a button, we do so via a method in case any of the sub GUIs need to reposition where it ends up
            addWarningTab(warningTracker);
        }
        initPinnedWindows();
    }

    protected void initPinnedWindows() {
        if (this.windows.isEmpty()) {
            //TODO: Improve support for this if we have windows that are opened from other windows
            for (GuiEventListener child : children()) {
                if (child instanceof GuiElement element) {
                    element.openPinnedWindows();
                }
            }
        }
    }

    protected void addWarningTab(IWarningTracker warningTracker) {
        addRenderableWidget(new GuiWarningTab(this, warningTracker, true));
    }

    /**
     * Called to add gui elements to the GUI. Add elements before calling super if they should be before the slots, and after if they should be after the slots. Most
     * elements can and should be added after the slots.
     */
    protected void addGuiElements() {
        if (dynamicSlots) {
            addSlots();
        }
    }

    @Override
    protected void setInitialFocus(@NotNull GuiEventListener listener) {
        if (!initialFocusSet) {
            super.setInitialFocus(listener);
            initialFocusSet = true;
        }
    }

    /**
     * Like {@link #addRenderableWidget(GuiEventListener)}, except doesn't add the element as narratable.
     */
    protected <T extends GuiElement> T addElement(T element) {
        renderables.add(element);
        ((List<GuiEventListener>) children()).add(element);
        return element;
    }

    protected <T extends GuiElement> T addRenderableWidget(T element) {
        //TODO: At some point we want to replace calls of this to directly call addElement, and then add in better support
        // for the narrator and what is currently focused and implement in some gui elements updateNarration rather than
        // just have it NO-OP. We currently redirect this to our version that doesn't add it as narratable so that we don't
        // have hitting tab with the narrator on just list indices
        return addElement(element);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        for (GuiEventListener child : children()) {
            if (child instanceof GuiElement element) {
                element.tick();
            }
        }
        windows.forEach(GuiWindow::tick);
    }

    @Override
    public long getTimeOpened() {
        return lastMSInitialized;
    }

    protected void renderTitleText(GuiGraphics guiGraphics) {
        drawTitleText(guiGraphics, title, titleLabelY);
    }

    protected void renderTitleTextWithOffset(GuiGraphics guiGraphics, int x) {
        renderTitleTextWithOffset(guiGraphics, x, getXSize());
    }

    protected void renderTitleTextWithOffset(GuiGraphics guiGraphics, int x, int end) {
        drawTitleTextTextWithOffset(guiGraphics, title, x, titleLabelY, end);
    }

    protected void renderTitleTextWithOffset(GuiGraphics guiGraphics, int x, int end, int maxLengthPad, TextAlignment alignment) {
        drawTitleTextTextWithOffset(guiGraphics, title, x, titleLabelY, end, maxLengthPad, alignment);
    }

    protected void renderInventoryText(GuiGraphics guiGraphics) {
        renderInventoryText(guiGraphics, getXSize());
    }

    protected void renderInventoryText(GuiGraphics guiGraphics, int end) {
        drawScrollingString(guiGraphics, playerInventoryTitle, inventoryLabelX, inventoryLabelY, TextAlignment.LEFT, titleTextColor(), end - inventoryLabelX - 6, 0, false);
    }

    protected void renderInventoryTextAndOther(GuiGraphics guiGraphics, Component rightAlignedText) {
        renderInventoryTextAndOther(guiGraphics, rightAlignedText, 0);
    }

    protected void renderInventoryTextAndOther(GuiGraphics guiGraphics, Component rightAlignedText, int rightEndPad) {
        drawScrollingString(guiGraphics, playerInventoryTitle, inventoryLabelX, inventoryLabelY, TextAlignment.LEFT, titleTextColor(), 53, 0, false);
        int rightStart = inventoryLabelX + 51;
        drawScrollingString(guiGraphics, rightAlignedText, rightStart, inventoryLabelY, TextAlignment.RIGHT, titleTextColor(), getXSize() - rightStart - rightEndPad, 6, false);
    }

    protected ResourceLocation getButtonLocation(String name) {
        return MekanismUtils.getResource(ResourceType.GUI_BUTTON, name + ".png");
    }

    @NotNull
    @Override
    public ItemStack getCarriedItem() {
        return getMenu().getCarried();
    }

    @Nullable
    private <NAVIGATION extends FocusNavigationEvent> ComponentPath handleNavigationWithWindows(NAVIGATION navigation,
          BiFunction<ContainerEventHandler, NAVIGATION, @Nullable ComponentPath> handleNavigation) {
        GuiWindow topWindow = windows.head();
        List<GuiEventListener> combinedChildren;
        //Note: As allowContainer wise only allows interacting with slots, which we don't have navigation for, we check against allowAll.
        // If we are allowed to interact with everything, we want to include all the windows and children in the navigation check,
        // otherwise we only include the top window, as while it should already be focused, maybe it isn't, so we need to make sure
        // to handle checking navigation on it
        if (topWindow.getInteractionStrategy().allowAll()) {
            combinedChildren = new ArrayList<>(windows);
            combinedChildren.addAll(children());
        } else {
            combinedChildren = List.of(topWindow);
        }
        ContainerEventHandler handlerWithWindows = new ContainerEventHandler() {
            @NotNull
            @Override
            public List<? extends GuiEventListener> children() {
                return combinedChildren;
            }

            @Override
            public boolean isDragging() {
                return GuiMekanism.this.isDragging();
            }

            @Override
            public void setDragging(boolean dragging) {
            }

            @Nullable
            @Override
            public GuiEventListener getFocused() {
                return GuiMekanism.this.getFocused();
            }

            @Override
            public void setFocused(@Nullable GuiEventListener focused) {
            }

            @NotNull
            @Override
            public ScreenRectangle getRectangle() {
                return GuiMekanism.this.getRectangle();
            }
        };
        ComponentPath componentPath = handleNavigation.apply(handlerWithWindows, navigation);
        if (componentPath == null) {
            return null;
        } else if (componentPath instanceof ComponentPath.Path(ContainerEventHandler component, ComponentPath childPath)) {
            if (component == handlerWithWindows) {
                //Replace the root of the path with ourselves rather than our fake wrapper
                return ComponentPath.path(this, childPath);
            }
        }
        return componentPath;
    }

    @Nullable
    @Override
    public ComponentPath handleTabNavigation(@NotNull FocusNavigationEvent.TabNavigation navigation) {
        //Note: We have to AT this method and the arrow navigation one, as Screen explicitly calls super.nextFocusPath,
        // so we can't get away with just overriding getFocusPath
        if (windows.isEmpty()) {
            return super.handleTabNavigation(navigation);
        }
        return handleNavigationWithWindows(navigation, ContainerEventHandler::handleTabNavigation);
    }

    @Nullable
    @Override
    public ComponentPath handleArrowNavigation(@NotNull FocusNavigationEvent.ArrowNavigation navigation) {
        if (windows.isEmpty()) {
            return super.handleArrowNavigation(navigation);
        }
        return handleNavigationWithWindows(navigation, ContainerEventHandler::handleArrowNavigation);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeftIn, int guiTopIn, int mouseButton) {
        return getWindowHovering(mouseX, mouseY) == null && super.hasClickedOutside(mouseX, mouseY, guiLeftIn, guiTopIn, mouseButton);
    }

    @Override
    protected void repositionElements() {
        if (switchingToRecipeViewer) {
            //Mark that we are not switching to JEI if we start being initialized again
            // Note: We can do this here as the screen will always have initialized as true, so we don't need to definalize init(mc, width, height)
            // as it will never potentially have init() with no params be the call path
            // Additionally, as the screen is not actively being used we shouldn't have cases this is called from resize while we are not present
            // and setting this to false when it is already false does nothing
            switchingToRecipeViewer = false;
            //If we were switching to a recipe viewer, then we also want to restart the time the scrolling text is using
            lastMSInitialized = Util.getMillis();
        }
        super.repositionElements();
    }

    @Override
    protected void rebuildWidgets() {
        //Gather any persistent data from existing elements
        record PreviousElement(int index, GuiElement element, boolean wasFocus) {
        }
        List<PreviousElement> prevElements = new ArrayList<>();
        GuiEventListener previousFocused = getFocused();
        for (int i = 0; i < children().size(); i++) {
            GuiEventListener widget = children().get(i);
            if (widget instanceof GuiElement element) {
                boolean wasPreviousFocus = element == previousFocused;
                if (wasPreviousFocus || element.hasPersistentData()) {
                    prevElements.add(new PreviousElement(i, element, wasPreviousFocus));
                }
            }
        }
        int prevLeft = leftPos, prevTop = topPos;
        //Allow the elements to be cleared and reinitialized
        super.rebuildWidgets();

        //Resize any windows as we can't easily just rebuild them
        for (GuiWindow window : windows) {
            window.resize(prevLeft, prevTop, leftPos, topPos);
        }

        //And set any persistent data that we stored
        int childCount = children().size();
        for (PreviousElement e : prevElements) {
            if (e.index() < childCount) {
                GuiEventListener widget = children().get(e.index());
                // we're forced to assume that the children list is the same before and after the resize.
                // for verification, we run a lightweight class equality check
                // Note: We do not perform an instance check on widget to ensure it is a GuiElement, as that is
                // ensured by the class comparison, and the restrictions of what can go in prevElements
                if (widget.getClass() == e.element().getClass()) {
                    ((GuiElement) widget).syncFrom(e.element());
                    if (e.wasFocus()) {
                        setFocused(widget);
                    }
                }
            }
        }
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        //Shift forward as far as tooltips get shifted so that we don't risk intersecting the rendered items
        pose.translate(0, 0, 400);
        for (GuiEventListener c : children()) {
            if (c instanceof GuiElement element) {
                element.onDrawBackground(guiGraphics, mouseX, mouseY, MekanismRenderer.getPartialTick());
            }
        }
        drawForegroundText(guiGraphics, mouseX, mouseY);
        // first render general foregrounds
        int zOffset = 200;
        maxZOffset = zOffset;
        for (GuiEventListener widget : children()) {
            if (widget instanceof GuiElement element) {
                pose.pushPose();
                element.onRenderForeground(guiGraphics, mouseX, mouseY, zOffset, zOffset);
                pose.popPose();
            }
        }

        // now render overlays in reverse-order (i.e. back to front)
        for (LRU<GuiWindow>.LRUIterator iter = getWindowsDescendingIterator(); iter.hasNext(); ) {
            GuiWindow overlay = iter.next();
            //Max z offset is incremented based on what is the deepest level offset we go to
            // if our gui isn't flagged as visible we won't increment it as nothing is drawn
            // we need to do this based on what the max is after having rendered the previous
            // window as while the windows don't necessarily overlap, if they do we want to
            // ensure that there is no clipping
            zOffset = maxZOffset + 150;
            pose.pushPose();
            overlay.onRenderForeground(guiGraphics, mouseX, mouseY, zOffset, zOffset);
            if (iter.hasNext()) {
                // if this isn't the focused window, render a 'blur' effect over it
                overlay.renderBlur(guiGraphics);
            }
            pose.popPose();
        }
        pose.popPose();
        //Additionally hacky offset to make it so that we render above items in higher z-levels for things like tooltips and held items
        maxZOffset += 200;
        // then render tooltips, translating above max z offset to prevent clashing
        // It is IMPORTANT that we do this to ensure any delayed rendering we do the for the tooltip happens above the other things
        // and so that we let the translation leak out into the super method so that the carried item renders at the correct z level
        pose.translate(0, 0, maxZOffset);

        pose.pushPose();
        //Note: Because we are doing this from renderLabels instead of as part of a render override,
        // we need to unshift back to the position the other methods expect to be called from
        pose.translate(-leftPos, -topPos, 0);
        GuiElement tooltipElement = getWindowHovering(mouseX, mouseY);
        if (tooltipElement == null) {
            tooltipElement = (GuiElement) GuiUtils.findChild(children(), mouseX, mouseY, (child, x, y) -> child instanceof GuiElement && child.isMouseOver(x, y));
        }
        if (tooltipElement != null) {
            tooltipElement.renderToolTip(guiGraphics, mouseX, mouseY);
        }
        renderTooltip(guiGraphics, mouseX, mouseY);
        pose.popPose();
    }

    /**
     * @implNote Copy of super, but adjusts the z value for tooltip rendering
     */
    @Override
    public final void renderWithTooltip(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        //Note: We wrap super with a push and pop, so that when we intentionally don't pop our changes in renderLabels
        // then we make sure to clean them up here
        PoseStack pose = graphics.pose();
        pose.pushPose();
        render(graphics, mouseX, mouseY, partialTick);
        if (deferredTooltipRendering != null) {
            //Note: render has a pop at the end of it in vanilla, so we have to apply the deferred tooltip rendering again
            pose.translate(0, 0, maxZOffset);
            graphics.renderTooltip(font, deferredTooltipRendering.tooltip(), deferredTooltipRendering.positioner(), mouseX, mouseY);
            clearTooltipForNextRenderPass();
        }
        pose.popPose();
    }

    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    @NotNull
    @Override
    public Optional<GuiEventListener> getChildAt(double mouseX, double mouseY) {
        GuiWindow window = getWindowHovering(mouseX, mouseY);
        return window == null ? super.getChildAt(mouseX, mouseY) : Optional.of(window);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double xDelta, double yDelta) {
        // first try to send the mouse event to our focused window
        GuiWindow top = windows.peek();
        if (top != null) {
            boolean windowScroll = top.mouseScrolled(mouseX, mouseY, xDelta, yDelta);
            if (windowScroll || !top.getInteractionStrategy().allowAll()) {
                //If our focused window was able to handle the scroll or doesn't allow interacting with
                // things outside the window, return our scroll result
                return windowScroll;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, xDelta, yDelta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        hasClicked = true;
        // first try to send the mouse event to our overlays
        GuiWindow top = windows.peek();
        for (GuiWindow overlay : windows) {
            if (overlay.mouseClicked(mouseX, mouseY, button)) {
                if (windows.contains(overlay)) {
                    //Validate that the focused window is still one of our windows, as if it wasn't focused/on top, and
                    // it is being closed, we don't want to update and mark it as focused, as our defocusing code won't
                    // run as we ran it when we pressed the button
                    setFocused(overlay);
                    if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                        setDragging(true);
                    }
                    // this check prevents us from moving the window to the top of the stack if the clicked window opened up an additional window
                    if (top != overlay) {
                        //noinspection DataFlowIssue: if null, we won't be in this loop
                        top.onFocusLost();
                        windows.moveUp(overlay);
                        overlay.onFocused();
                    }
                }
                return true;
            }
        }
        // otherwise, we send it to the current element (this is the same as super.super [ContainerEventHandler#mouseClicked], but in reverse order)
        //TODO: Why do we do this in reverse order?
        GuiEventListener clickedChild = GuiUtils.findChild(children(), mouseX, mouseY, button, GuiEventListener::mouseClicked);
        if (clickedChild != null) {
            setFocused(clickedChild);
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                setDragging(true);
            }
            return true;
        } else {
            //If we can't find a child, allow clearing whatever focus we currently have
            clearFocus();
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (hasClicked) {
            // always pass mouse released events to windows for drag checks
            for (GuiWindow w : windows) {
                w.onRelease(mouseX, mouseY);
            }
            return super.mouseReleased(mouseX, mouseY, button);
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (GuiWindow window : windows) {
            if (window.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return GuiUtils.checkChildren(children(), keyCode, scanCode, modifiers, (child, k, s, m) -> child instanceof GuiElement && child.keyPressed(k, s, m)) ||
               super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int keyCode) {
        for (GuiWindow window : windows) {
            if (window.charTyped(c, keyCode)) {
                return true;
            }
        }
        return GuiUtils.checkChildrenChar(children(), c, keyCode, (child, ch, k) -> child instanceof GuiElement && child.charTyped(ch, k)) || super.charTyped(c, keyCode);
    }

    /**
     * @apiNote mouseXOld and mouseYOld are just guessed mappings I couldn't find any usage from a quick glance.
     */
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double mouseXOld, double mouseYOld) {
        super.mouseDragged(mouseX, mouseY, button, mouseXOld, mouseYOld);
        return getFocused() != null && isDragging() && button == GLFW.GLFW_MOUSE_BUTTON_LEFT && getFocused().mouseDragged(mouseX, mouseY, button, mouseXOld, mouseYOld);
    }

    @Nullable
    @Override
    @Deprecated//Don't use directly, this is normally private in ContainerScreen
    protected Slot findSlot(double mouseX, double mouseY) {
        //We override the implementation we have in VirtualSlotContainerScreen so that we can cache getting our window
        // and have some general performance improvements given we can batch a bunch of lookups together
        boolean checkedWindow = false;
        boolean overNoButtons = false;
        GuiWindow window = null;
        for (Slot slot : menu.slots) {
            if (!slot.isActive()) {
                continue;
            }
            boolean virtual = slot instanceof IVirtualSlot;
            int xPos = slot.x;
            int yPos = slot.y;
            if (virtual) {
                //Virtual slots need special handling to allow for matching them to the window they may be attached to
                IVirtualSlot virtualSlot = (IVirtualSlot) slot;
                if (!isVirtualSlotAvailable(virtualSlot)) {
                    //If the slot is not available just skip all checks related to it
                    continue;
                }
                xPos = virtualSlot.getActualX();
                yPos = virtualSlot.getActualY();
            }
            if (super.isHovering(xPos, yPos, 16, 16, mouseX, mouseY)) {
                if (!checkedWindow) {
                    //Only lookup the window once
                    checkedWindow = true;
                    window = getWindowHovering(mouseX, mouseY);
                    overNoButtons = overNoButtons(window, mouseX, mouseY);
                }
                if (overNoButtons && slot.isActive()) {
                    if (window == null) {
                        return slot;
                    } else if (virtual) {
                        for (GuiElement child : window.children()) {
                            if (child instanceof GuiVirtualSlot v && v.isElementForSlot((IVirtualSlot) slot)) {
                                return slot;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected boolean isMouseOverSlot(@NotNull Slot slot, double mouseX, double mouseY) {
        if (slot instanceof IVirtualSlot virtualSlot) {
            //Virtual slots need special handling to allow for matching them to the window they may be attached to
            if (isVirtualSlotAvailable(virtualSlot)) {
                //Start by checking if the slot is even "active/available"
                int xPos = virtualSlot.getActualX();
                int yPos = virtualSlot.getActualY();
                if (super.isHovering(xPos, yPos, 16, 16, mouseX, mouseY)) {
                    GuiWindow window = getWindowHovering(mouseX, mouseY);
                    //If we are hovering over a window, check if the virtual slot is a child of the window
                    if (window == null) {
                        return overNoButtons((GuiWindow) null, mouseX, mouseY);
                    } else {
                        for (GuiElement child : window.children()) {
                            if (child instanceof GuiVirtualSlot v && v.isElementForSlot(virtualSlot)) {
                                return overNoButtons(window, mouseX, mouseY);
                            }
                        }
                    }
                }
            }
            return false;
        }
        return isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY);
    }

    private boolean overNoButtons(@Nullable GuiWindow window, double mouseX, double mouseY) {
        if (window == null) {
            return overNoButtons(children(), mouseX, mouseY);
        }
        return overNoButtons(window.children(), mouseX, mouseY);
    }

    private static boolean overNoButtons(List<? extends GuiEventListener> children, double mouseX, double mouseY) {
        for (GuiEventListener child : children) {
            if (child.isMouseOver(mouseX, mouseY)) {
                return false;
            }
        }
        return true;
    }

    private boolean isVirtualSlotAvailable(IVirtualSlot virtualSlot) {
        //If there is a window linked to the slot, and it no longer exists then skip checking if the slot is available
        return !(virtualSlot.getLinkedWindow() instanceof GuiWindow linkedWindow) || windows.contains(linkedWindow);
    }

    @Override
    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        // overridden to prevent slot interactions when a GuiElement is blocking
        return super.isHovering(x, y, width, height, mouseX, mouseY) && getWindowHovering(mouseX, mouseY) == null &&
               overNoButtons((GuiWindow) null, mouseX, mouseY);
    }

    protected void addSlots() {
        int size = menu.slots.size();
        for (int i = 0; i < size; i++) {
            Slot slot = menu.slots.get(i);
            if (slot instanceof InventoryContainerSlot containerSlot) {
                ContainerSlotType slotType = containerSlot.getSlotType();
                DataType dataType = findDataType(containerSlot);
                //Shift the slots by one as the elements include the border of the slot
                SlotType type;
                if (dataType != null) {
                    type = SlotType.get(dataType);
                } else if (slotType == ContainerSlotType.INPUT || slotType == ContainerSlotType.OUTPUT || slotType == ContainerSlotType.EXTRA) {
                    type = SlotType.NORMAL;
                } else if (slotType == ContainerSlotType.POWER) {
                    type = SlotType.POWER;
                } else if (slotType == ContainerSlotType.NORMAL || slotType == ContainerSlotType.VALIDITY) {
                    type = SlotType.NORMAL;
                } else {//slotType == ContainerSlotType.IGNORED: don't do anything
                    continue;
                }
                GuiSlot guiSlot = new GuiSlot(type, this, slot.x - 1, slot.y - 1);
                containerSlot.addWarnings(guiSlot);
                SlotOverlay slotOverlay = containerSlot.getSlotOverlay();
                if (slotOverlay != null) {
                    guiSlot.with(slotOverlay);
                }
                if (slotType == ContainerSlotType.VALIDITY) {
                    int index = i;
                    guiSlot.validity(() -> checkValidity(index));
                }
                addRenderableWidget(guiSlot);
            } else {
                addRenderableWidget(new GuiSlot(SlotType.NORMAL, this, slot.x - 1, slot.y - 1));
            }
        }
    }

    @Nullable
    protected DataType findDataType(InventoryContainerSlot slot) {
        if (menu instanceof MekanismTileContainer<?> container && container.getTileEntity() instanceof ISideConfiguration sideConfig) {
            return sideConfig.getActiveDataType(slot.getInventorySlot());
        }
        return null;
    }

    protected ItemStack checkValidity(int slotIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        //Ensure the GL color is white as mods adding an overlay (such as JEI for bookmarks), might have left
        // it in an unexpected state.
        MekanismRenderer.resetColor(guiGraphics);
        if (getXSize() < 8 || getYSize() < 8) {
            Mekanism.logger.warn("Gui: {}, was too small to draw the background of. Unable to draw a background for a gui smaller than 8 by 8.", getClass().getSimpleName());
            return;
        }
        GuiUtils.renderBackgroundTexture(guiGraphics, BASE_BACKGROUND, 4, 4, leftPos, topPos, imageWidth, imageHeight, 256, 256);
    }

    @Override
    public Font font() {
        //Note: In theory font is never null here, as we should only be calling it after init has happened
        // Previously we checked if it was and then fell back to Minecraft's overall font. But as the minecraft
        // object we queried was actually null as well, we know that this doesn't ever get called before init
        return font;
    }

    @Override
    public boolean currentlyQuickCrafting() {
        return isQuickCrafting && !quickCraftSlots.isEmpty();
    }

    @Override
    public void addWindow(GuiWindow window) {
        GuiWindow top = windows.peek();
        if (top != null) {
            top.onFocusLost();
        }
        windows.add(window);
        window.onFocused();
    }

    @Override
    public void removeWindow(GuiWindow window) {
        if (!windows.isEmpty()) {
            GuiWindow top = windows.head();
            windows.remove(window);
            if (window == top) {
                //If the window was the top window, make it lose focus
                window.onFocusLost();
                //Amd check if a new window is now in focus
                GuiWindow newTop = windows.peek();
                if (newTop == null) {
                    //If there isn't any because they have all been removed
                    // fire an "event" for any post all windows being closed
                    lastWindowRemoved();
                } else {
                    //Otherwise, mark the new window as being focused
                    newTop.onFocused();
                }
                //Update the listener to being the window that is now selected or null if none are
                setFocused(newTop);
            }
        }
    }

    protected void lastWindowRemoved() {
        //Mark that no windows are now selected
        if (menu instanceof MekanismContainer container) {
            container.setSelectedWindow(null);
        }
    }

    @Override
    public void setSelectedWindow(SelectedWindowData selectedWindow) {
        if (menu instanceof MekanismContainer container) {
            container.setSelectedWindow(selectedWindow);
        }
    }

    @Nullable
    @Override
    public GuiWindow getWindowHovering(double mouseX, double mouseY) {
        for (GuiWindow w : windows) {
            if (w.isMouseOver(mouseX, mouseY)) {
                return w;
            }
        }
        return null;
    }

    public Collection<GuiWindow> getWindows() {
        return windows;
    }

    public LRU<GuiWindow>.LRUIterator getWindowsDescendingIterator() {
        return windows.descendingIterator();
    }
}