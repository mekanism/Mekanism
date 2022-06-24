package mekanism.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiElement.IHoverable;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.GuiVirtualSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.tab.GuiWarningTab;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.client.render.IFancyFontRenderer;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public abstract class GuiMekanism<CONTAINER extends AbstractContainerMenu> extends VirtualSlotContainerScreen<CONTAINER> implements IGuiWrapper, IFancyFontRenderer {

    public static final ResourceLocation BASE_BACKGROUND = MekanismUtils.getResource(ResourceType.GUI, "base.png");
    public static final ResourceLocation SHADOW = MekanismUtils.getResource(ResourceType.GUI, "shadow.png");
    public static final ResourceLocation BLUR = MekanismUtils.getResource(ResourceType.GUI, "blur.png");
    //TODO: Look into defaulting this to true
    protected boolean dynamicSlots;
    protected final LRU<GuiWindow> windows = new LRU<>();
    protected final List<GuiElement> focusListeners = new ArrayList<>();
    public boolean switchingToJEI;
    @Nullable
    private IWarningTracker warningTracker;

    private boolean hasClicked = false;

    public static int maxZOffset;
    private int maxZOffsetNoWindows;

    protected GuiMekanism(CONTAINER container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Nonnull
    @Override
    public BooleanSupplier trackWarning(@Nonnull WarningType type, @Nonnull BooleanSupplier warningSupplier) {
        if (warningTracker == null) {
            warningTracker = new WarningTracker();
        }
        return warningTracker.trackWarning(type, warningSupplier);
    }

    @Override
    public void removed() {
        if (!switchingToJEI) {
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
        }
        addGuiElements();
        if (warningTracker != null) {
            //If we have a warning tracker add it as a button, we do so via a method in case any of the sub GUIs need to reposition where it ends up
            addWarningTab(warningTracker);
        }
    }

    protected void addWarningTab(IWarningTracker warningTracker) {
        addRenderableWidget(new GuiWarningTab(this, warningTracker, 109));
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
        children().stream().filter(child -> child instanceof GuiElement).map(child -> (GuiElement) child).forEach(GuiElement::tick);
        windows.forEach(GuiWindow::tick);
    }

    protected void renderTitleText(PoseStack poseStack) {
        drawTitleText(poseStack, title, titleLabelY);
    }

    protected IHoverable getOnHover(ILangEntry translationHelper) {
        return getOnHover((Supplier<Component>) translationHelper::translate);
    }

    protected IHoverable getOnHover(Supplier<Component> componentSupplier) {
        return (onHover, matrix, mouseX, mouseY) -> displayTooltips(matrix, mouseX, mouseY, componentSupplier.get());
    }

    protected ResourceLocation getButtonLocation(String name) {
        return MekanismUtils.getResource(ResourceType.GUI_BUTTON, name + ".png");
    }

    @Override
    public void addFocusListener(GuiElement element) {
        focusListeners.add(element);
    }

    @Override
    public void removeFocusListener(GuiElement element) {
        focusListeners.remove(element);
    }

    @Override
    public void focusChange(GuiElement changed) {
        focusListeners.stream().filter(e -> e != changed).forEach(e -> e.setFocused(false));
    }

    @Override
    public void incrementFocus(GuiElement current) {
        int index = focusListeners.indexOf(current);
        if (index != -1) {
            GuiElement next = focusListeners.get((index + 1) % focusListeners.size());
            next.setFocused(true);
            focusChange(next);
        }
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeftIn, int guiTopIn, int mouseButton) {
        return getWindowHovering(mouseX, mouseY) == null && super.hasClickedOutside(mouseX, mouseY, guiLeftIn, guiTopIn, mouseButton);
    }

    @Override
    public void init(@Nonnull Minecraft minecraft, int width, int height) {
        //Mark that we are not switching to JEI if we start being initialized again
        switchingToJEI = false;
        //Note: We are forced to do the logic that normally would be inside the "resize" method
        // here in init, as when mods like JEI take over the screen to show recipes, and then
        // return the screen to the "state" it was beforehand it does not actually properly
        // transfer the state from the previous instance to the new instance. If we run the
        // code we normally would run for when things get resized, we then are able to
        // properly reinstate/transfer the states of the various elements
        record PreviousElement(int index, GuiElement element) {
        }
        List<PreviousElement> prevElements = new ArrayList<>();
        for (int i = 0; i < children().size(); i++) {
            GuiEventListener widget = children().get(i);
            if (widget instanceof GuiElement element && element.hasPersistentData()) {
                prevElements.add(new PreviousElement(i, element));
            }
        }
        // flush the focus listeners list unless it's an overlay
        focusListeners.removeIf(element -> !element.isOverlay);
        int prevLeft = leftPos, prevTop = topPos;
        super.init(minecraft, width, height);

        windows.forEach(window -> window.resize(prevLeft, prevTop, leftPos, topPos));

        prevElements.forEach(e -> {
            if (e.index() < children().size()) {
                GuiEventListener widget = children().get(e.index());
                // we're forced to assume that the children list is the same before and after the resize.
                // for verification, we run a lightweight class equality check
                // Note: We do not perform an instance check on widget to ensure it is a GuiElement, as that is
                // ensured by the class comparison, and the restrictions of what can go in prevElements
                if (widget.getClass() == e.element().getClass()) {
                    ((GuiElement) widget).syncFrom(e.element());
                }
            }
        });
    }

    @Override
    protected void renderLabels(@Nonnull PoseStack matrix, int mouseX, int mouseY) {
        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        //Note: We intentionally don't push the modelViewStack, see notes further down in this method for more details
        matrix.translate(0, 0, 300);
        modelViewStack.translate(-leftPos, -topPos, 0);
        RenderSystem.applyModelViewMatrix();
        children().stream().filter(c -> c instanceof GuiElement).forEach(c -> ((GuiElement) c).onDrawBackground(matrix, mouseX, mouseY, MekanismRenderer.getPartialTick()));
        modelViewStack.translate(leftPos, topPos, 0);
        RenderSystem.applyModelViewMatrix();
        drawForegroundText(matrix, mouseX, mouseY);
        // first render general foregrounds
        int zOffset = 200;
        maxZOffsetNoWindows = maxZOffset = zOffset;
        for (GuiEventListener widget : children()) {
            if (widget instanceof GuiElement element) {
                matrix.pushPose();
                element.onRenderForeground(matrix, mouseX, mouseY, zOffset, zOffset);
                matrix.popPose();
            }
        }
        maxZOffsetNoWindows = maxZOffset;
        int windowSeparation = 150;

        // now render overlays in reverse-order (i.e. back to front)
        for (LRU<GuiWindow>.LRUIterator iter = getWindowsDescendingIterator(); iter.hasNext(); ) {
            GuiWindow overlay = iter.next();
            //Max z offset is incremented based on what is the deepest level offset we go to
            // if our gui isn't flagged as visible we won't increment it as nothing is drawn
            // we need to do this based on what the max is after having rendered the previous
            // window as while the windows don't necessarily overlap, if they do we want to
            // ensure that there is no clipping
            zOffset = maxZOffset + windowSeparation;
            matrix.pushPose();
            overlay.onRenderForeground(matrix, mouseX, mouseY, zOffset, zOffset);
            if (iter.hasNext()) {
                // if this isn't the focused window, render a 'blur' effect over it
                overlay.renderBlur(matrix);
            }
            matrix.popPose();
        }
        // then render tooltips, translating above max z offset to prevent clashing
        GuiElement tooltipElement = getWindowHovering(mouseX, mouseY);
        if (tooltipElement == null) {
            for (int i = children().size() - 1; i >= 0; i--) {
                GuiEventListener widget = children().get(i);
                if (widget instanceof GuiElement element && element.isMouseOver(mouseX, mouseY)) {
                    tooltipElement = element;
                    break;
                }
            }
        }

        // translate forwards using RenderSystem. this should never have to happen as we do all the necessary translations with MatrixStacks,
        // but Minecraft has decided to not fully adopt MatrixStacks for many crucial ContainerScreen render operations. should be re-evaluated
        // when mc updates related logic on their end (IMPORTANT)
        modelViewStack.translate(0, 0, maxZOffset);

        // render tooltips
        modelViewStack.translate(-leftPos, -topPos, 0);
        RenderSystem.applyModelViewMatrix();
        if (tooltipElement != null) {
            tooltipElement.renderToolTip(matrix, mouseX, mouseY);
        }
        renderTooltip(matrix, mouseX, mouseY);
        modelViewStack.translate(leftPos, topPos, 0);

        // IMPORTANT: additional hacky translation so held items render okay. re-evaluate as discussed above
        // Note: It is important that we don't wrap our adjustments to the modelViewStack in so that we can
        // have the adjustments to the z-value persist into the vanilla methods
        modelViewStack.translate(0, 0, 200);
        RenderSystem.applyModelViewMatrix();

        //Adjust the amount we offset tooltips to put tooltips made by JEI above the windows
        maxZOffsetNoWindows = -(maxZOffset - windowSeparation * windows.size());
    }

    protected void drawForegroundText(@Nonnull PoseStack matrix, int mouseX, int mouseY) {
    }

    @Nonnull
    @Override
    public Optional<GuiEventListener> getChildAt(double mouseX, double mouseY) {
        GuiWindow window = getWindowHovering(mouseX, mouseY);
        return window == null ? super.getChildAt(mouseX, mouseY) : Optional.of(window);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        hasClicked = true;
        // first try to send the mouse event to our overlays
        GuiWindow top = windows.isEmpty() ? null : windows.iterator().next();
        GuiWindow focused = windows.stream().filter(overlay -> overlay.mouseClicked(mouseX, mouseY, button)).findFirst().orElse(null);
        if (focused != null) {
            if (windows.contains(focused)) {
                //Validate that the focused window is still one of our windows, as if it wasn't focused/on top, and
                // it is being closed, we don't want to update and mark it as focused, as our defocusing code won't
                // run as we ran it when we pressed the button
                setFocused(focused);
                if (button == 0) {
                    setDragging(true);
                }
                // this check prevents us from moving the window to the top of the stack if the clicked window opened up an additional window
                if (top != focused) {
                    top.onFocusLost();
                    windows.moveUp(focused);
                    focused.onFocused();
                }
            }
            return true;
        }
        // otherwise, we send it to the current element
        for (int i = children().size() - 1; i >= 0; i--) {
            GuiEventListener listener = children().get(i);
            if (listener.mouseClicked(mouseX, mouseY, button)) {
                setFocused(listener);
                if (button == 0) {
                    setDragging(true);
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (hasClicked) {
            // always pass mouse released events to windows for drag checks
            windows.forEach(w -> w.onRelease(mouseX, mouseY));
            return super.mouseReleased(mouseX, mouseY, button);
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return windows.stream().anyMatch(window -> window.keyPressed(keyCode, scanCode, modifiers)) ||
               GuiUtils.checkChildren(children(), child -> child.keyPressed(keyCode, scanCode, modifiers)) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int keyCode) {
        return windows.stream().anyMatch(window -> window.charTyped(c, keyCode)) || GuiUtils.checkChildren(children(), child -> child.charTyped(c, keyCode)) ||
               super.charTyped(c, keyCode);
    }

    /**
     * @apiNote mouseXOld and mouseYOld are just guessed mappings I couldn't find any usage from a quick glance.
     */
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double mouseXOld, double mouseYOld) {
        super.mouseDragged(mouseX, mouseY, button, mouseXOld, mouseYOld);
        return getFocused() != null && isDragging() && button == 0 && getFocused().mouseDragged(mouseX, mouseY, button, mouseXOld, mouseYOld);
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
                    } else if (virtual && window.childrenContainsElement(element -> element instanceof GuiVirtualSlot v && v.isElementForSlot((IVirtualSlot) slot))) {
                        return slot;
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected boolean isMouseOverSlot(@Nonnull Slot slot, double mouseX, double mouseY) {
        if (slot instanceof IVirtualSlot virtualSlot) {
            //Virtual slots need special handling to allow for matching them to the window they may be attached to
            if (isVirtualSlotAvailable(virtualSlot)) {
                //Start by checking if the slot is even "active/available"
                int xPos = virtualSlot.getActualX();
                int yPos = virtualSlot.getActualY();
                if (super.isHovering(xPos, yPos, 16, 16, mouseX, mouseY)) {
                    GuiWindow window = getWindowHovering(mouseX, mouseY);
                    //If we are hovering over a window, check if the virtual slot is a child of the window
                    if (window == null || window.childrenContainsElement(element -> element instanceof GuiVirtualSlot v && v.isElementForSlot(virtualSlot))) {
                        return overNoButtons(window, mouseX, mouseY);
                    }
                }
            }
            return false;
        }
        return isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY);
    }

    private boolean overNoButtons(@Nullable GuiWindow window, double mouseX, double mouseY) {
        if (window == null) {
            return children().stream().noneMatch(button -> button.isMouseOver(mouseX, mouseY));
        }
        return !window.childrenContainsElement(e -> e.isMouseOver(mouseX, mouseY));
    }

    private boolean isVirtualSlotAvailable(IVirtualSlot virtualSlot) {
        //If there is a window linked to the slot, and it no longer exists then skip checking if the slot is available
        return !(virtualSlot.getLinkedWindow() instanceof GuiWindow linkedWindow) || windows.contains(linkedWindow);
    }

    @Override
    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        // overridden to prevent slot interactions when a GuiElement is blocking
        return super.isHovering(x, y, width, height, mouseX, mouseY) && getWindowHovering(mouseX, mouseY) == null &&
               overNoButtons(null, mouseX, mouseY);
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
        if (menu instanceof MekanismTileContainer container && container.getTileEntity() instanceof ISideConfiguration sideConfig) {
            return sideConfig.getActiveDataType(slot.getInventorySlot());
        }
        return null;
    }

    protected ItemStack checkValidity(int slotIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    protected void renderBg(@Nonnull PoseStack matrix, float partialTick, int mouseX, int mouseY) {
        //Ensure the GL color is white as mods adding an overlay (such as JEI for bookmarks), might have left
        // it in an unexpected state.
        MekanismRenderer.resetColor();
        if (width < 8 || height < 8) {
            Mekanism.logger.warn("Gui: {}, was too small to draw the background of. Unable to draw a background for a gui smaller than 8 by 8.", getClass().getSimpleName());
            return;
        }
        GuiUtils.renderBackgroundTexture(matrix, BASE_BACKGROUND, 4, 4, leftPos, topPos, imageWidth, imageHeight, 256, 256);
    }

    @Override
    public Font getFont() {
        return font;
    }

    @Override
    public void render(@Nonnull PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();

        // shift back a whole lot so we can stack more windows
        modelViewStack.translate(0, 0, -500);
        RenderSystem.applyModelViewMatrix();
        matrix.pushPose();
        renderBackground(matrix);
        //Apply our matrix stack to the render system and pass an unmodified one to the super method
        // Vanilla still renders the items into the GUI using render system transformations so this
        // is required to not have tooltips of GuiElements rendering behind the items
        super.render(matrix, mouseX, mouseY, partialTicks);
        matrix.popPose();
        modelViewStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    @Override
    public void renderTooltip(@Nonnull PoseStack poseStack, @Nonnull List<Component> tooltips, @Nonnull Optional<TooltipComponent> visualTooltipComponent, int mouseX,
          int mouseY) {
        adjustTooltipZ(poseStack, pose -> super.renderTooltip(pose, tooltips, visualTooltipComponent, mouseX, mouseY));
    }

    @Override
    public void renderComponentTooltip(@Nonnull PoseStack poseStack, @Nonnull List<Component> tooltips, int mouseX, int mouseY) {
        adjustTooltipZ(poseStack, pose -> super.renderComponentTooltip(pose, tooltips, mouseX, mouseY));
    }

    @Override
    public void renderComponentTooltip(@Nonnull PoseStack poseStack, @Nonnull List<? extends FormattedText> tooltips, int mouseX, int mouseY, @Nullable Font font,
          @Nonnull ItemStack stack) {
        adjustTooltipZ(poseStack, pose -> super.renderComponentTooltip(pose, tooltips, mouseX, mouseY, font, stack));
    }

    @Override
    public void renderTooltip(@Nonnull PoseStack poseStack, @Nonnull List<? extends FormattedCharSequence> tooltips, int mouseX, int mouseY) {
        adjustTooltipZ(poseStack, pose -> super.renderTooltip(pose, tooltips, mouseX, mouseY));
    }

    //Used as a helper to wrap and fix the various calls to renderTooltipInternal so that tooltips for bundles appear in the proper location
    private void adjustTooltipZ(@Nonnull PoseStack poseStack, @Nonnull Consumer<PoseStack> tooltipRender) {
        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        //Apply the current matrix to the view so that we render items and tooltips at the proper level
        // (especially for bundles that render items as part of the tooltip). We also translate to make things
        // fit better in the z direction
        modelViewStack.translate(0, 0, -maxZOffsetNoWindows - 1);
        modelViewStack.mulPoseMatrix(poseStack.last().pose());
        RenderSystem.applyModelViewMatrix();
        tooltipRender.accept(new PoseStack());
        modelViewStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    @Override
    public void renderItemTooltip(PoseStack matrix, @Nonnull ItemStack stack, int xAxis, int yAxis) {
        renderTooltip(matrix, stack, xAxis, yAxis);
    }

    @Override
    public void renderItemTooltipWithExtra(PoseStack matrix, @Nonnull ItemStack stack, int xAxis, int yAxis, List<Component> toAppend) {
        if (toAppend.isEmpty()) {
            renderItemTooltip(matrix, stack, xAxis, yAxis);
        } else {
            List<Component> tooltip = new ArrayList<>(getTooltipFromItem(stack));
            tooltip.addAll(toAppend);
            renderTooltip(matrix, tooltip, stack.getTooltipImage(), xAxis, yAxis, stack);
        }
    }

    @Override
    public ItemRenderer getItemRenderer() {
        return itemRenderer;
    }

    @Override
    public boolean currentlyQuickCrafting() {
        return isQuickCrafting && !quickCraftSlots.isEmpty();
    }

    @Override
    public void addWindow(GuiWindow window) {
        GuiWindow top = windows.isEmpty() ? null : windows.iterator().next();
        if (top != null) {
            top.onFocusLost();
        }
        windows.add(window);
        window.onFocused();
    }

    @Override
    public void removeWindow(GuiWindow window) {
        if (!windows.isEmpty()) {
            GuiWindow top = windows.iterator().next();
            windows.remove(window);
            if (window == top) {
                //If the window was the top window, make it lose focus
                window.onFocusLost();
                //Amd check if a new window is now in focus
                GuiWindow newTop = windows.isEmpty() ? null : windows.iterator().next();
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
        return windows.stream().filter(w -> w.isMouseOver(mouseX, mouseY)).findFirst().orElse(null);
    }

    public Collection<GuiWindow> getWindows() {
        return windows;
    }

    public LRU<GuiWindow>.LRUIterator getWindowsDescendingIterator() {
        return windows.descendingIterator();
    }
}