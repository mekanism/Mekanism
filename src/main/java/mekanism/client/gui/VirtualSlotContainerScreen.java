package mekanism.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.inventory.container.slot.IVirtualSlot;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2i;
import org.jspecify.annotations.Nullable;

//TODO - 26.1: Heavily re-evaluate this class/make sure nothing has gotten broken
@NothingNullByDefault
public abstract class VirtualSlotContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {

    public VirtualSlotContainerScreen(T container, Inventory inv, Component titleIn) {
        super(container, inv, titleIn);
    }

    public VirtualSlotContainerScreen(T container, Inventory inv, Component titleIn, int imageWidth, int imageHeight) {
        super(container, inv, titleIn, imageWidth, imageHeight);
    }

    protected abstract boolean isMouseOverSlot(Slot slot, double mouseX, double mouseY);

    @Nullable
    @Override
    @Deprecated//Don't use directly, this is normally private in ContainerScreen
    protected Slot getHoveredSlot(double mouseX, double mouseY) {
        for (Slot slot : menu.slots) {
            //Like super.getSelectedSlot except uses our isMouseOverSlot so
            // that our redirection doesn't break this
            if (slot.isActive() && isMouseOverSlot(slot, mouseX, mouseY)) {
                return slot;
            }
        }
        return null;
    }

    @Override
    @Deprecated//Don't use directly, this is normally private in ContainerScreen
    protected final boolean isHovering(Slot slot, double mouseX, double mouseY) {
        boolean mouseOver = isMouseOverSlot(slot, mouseX, mouseY);
        if (mouseOver && slot instanceof IVirtualSlot) {
            //Fake that the slot is "not" selected so that when this is called by render
            // we don't render hover mask as it will be in the incorrect position
            if (hoveredSlot == null && slot.isActive()) {
                //If needed though we do make sure to update the hovered slot for use elsewhere
                hoveredSlot = slot;
            }
            return false;
        }
        return mouseOver;
    }

    private boolean mouseReleasedBase(MouseButtonEvent event) {
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && this.isDragging()) {
            setDragging(false);
            if (getFocused() != null) {
                return getFocused().mouseReleased(event);
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        //TODO - 26.1: If we AT x and y for slots to not be final, and actually update them I think we can skip this override
        if (!(clickedSlot instanceof IVirtualSlot virtualSlot)) {
            //If we are not a virtual slot, the super method is good enough
            return super.mouseReleased(event);
        }

        mouseReleasedBase(event);//Forge: call parent to release buttons
        Slot slot = getHoveredSlot(event.x(), event.y());
        int xo = this.leftPos;
        int yo = this.topPos;
        boolean clickedOutside = hasClickedOutside(event.x(), event.y(), xo, yo);
        if (slot != null) {
            clickedOutside = false; // Forge, prevent dropping of items through slots outside of GUI boundaries
        }
        InputConstants.Key mouseKey = InputConstants.Type.MOUSE.getOrCreate(event.button());
        int slotId = -1;
        if (slot != null) {
            slotId = slot.index;
        }

        if (clickedOutside) {
            slotId = AbstractContainerMenu.SLOT_CLICKED_OUTSIDE;
        }

        if (this.doubleclick && slot != null && event.button() == InputConstants.MOUSE_BUTTON_LEFT && this.menu.canTakeItemForPickAll(ItemStack.EMPTY, slot)) {
            if (event.hasShiftDown()) {
                if (!this.lastQuickMoved.isEmpty()) {
                    for (Slot target : this.menu.slots) {
                        if (target != null
                            && target.mayPickup(this.minecraft.player)
                            && target.hasItem()
                            && target.isSameInventory(slot)
                            && AbstractContainerMenu.canItemQuickReplace(target, this.lastQuickMoved, true)) {
                            this.slotClicked(target, target.index, event.button(), ContainerInput.QUICK_MOVE);
                        }
                    }
                }
            } else {
                this.slotClicked(slot, slotId, event.button(), ContainerInput.PICKUP_ALL);
            }

            this.doubleclick = false;
        } else {
            if (this.isQuickCrafting && this.quickCraftingButton != event.button()) {
                this.isQuickCrafting = false;
                this.quickCraftSlots.clear();
                this.skipNextRelease = true;
                return true;
            }

            if (this.skipNextRelease) {
                this.skipNextRelease = false;
                return true;
            }

            if (this.clickedSlot != null && this.minecraft.options.touchscreen().get()) {
                if (event.button() == InputConstants.MOUSE_BUTTON_LEFT || event.button() == InputConstants.MOUSE_BUTTON_RIGHT) {
                    if (this.draggingItem.isEmpty() && slot != this.clickedSlot) {
                        this.draggingItem = this.clickedSlot.getItem();
                    }

                    boolean canReplace = AbstractContainerMenu.canItemQuickReplace(slot, this.draggingItem, false);
                    if (slotId != -1 && !this.draggingItem.isEmpty() && canReplace) {
                        slotClicked(this.clickedSlot, this.clickedSlot.index, event.button(), ContainerInput.PICKUP);
                        slotClicked(slot, slotId, 0, ContainerInput.PICKUP);
                        if (this.menu.getCarried().isEmpty()) {
                            this.snapbackData = null;
                        } else {
                            this.slotClicked(this.clickedSlot, this.clickedSlot.index, event.button(), ContainerInput.PICKUP);
                            this.snapbackData = new AbstractContainerScreen.SnapbackData(
                                  this.draggingItem,
                                  new Vector2i((int) event.x(), (int) event.y()),
                                  new Vector2i(virtualSlot.getActualX() + xo, virtualSlot.getActualY() + yo),
                                  Util.getMillis()
                            );
                        }
                    } else if (!this.draggingItem.isEmpty()) {
                        this.snapbackData = new AbstractContainerScreen.SnapbackData(
                              this.draggingItem,
                              new Vector2i((int) event.x(), (int) event.y()),
                              new Vector2i(virtualSlot.getActualX() + xo, virtualSlot.getActualY() + yo),
                              Util.getMillis()
                        );
                    }

                    this.clearDraggingState();
                }
            } else if (this.isQuickCrafting && !this.quickCraftSlots.isEmpty()) {
                quickCraftToSlots();
            } else if (!this.menu.getCarried().isEmpty()) {
                if (this.minecraft.options.keyPickItem.isActiveAndMatches(mouseKey)) {
                    slotClicked(slot, slotId, event.button(), ContainerInput.CLONE);
                } else {
                    boolean quickKey = slotId != AbstractContainerMenu.SLOT_CLICKED_OUTSIDE && event.hasShiftDown();
                    if (quickKey) {
                        this.lastQuickMoved = slot != null && slot.hasItem() ? slot.getItem().copy() : ItemStack.EMPTY;
                    }

                    slotClicked(slot, slotId, event.button(), quickKey ? ContainerInput.QUICK_MOVE : ContainerInput.PICKUP);
                }
            }
        }
        this.isQuickCrafting = false;
        return mouseReleasedBase(event);
    }

    //TODO - 26.1: If we AT x and y for slots to not be final, and actually update them I think we can skip this override unless we need this to be delayed
    @Override
    @Deprecated//Don't use directly, this is normally private in ContainerScreen
    protected final void extractSlotHighlightBack(GuiGraphicsExtractor graphics) {
        if (this.hoveredSlot instanceof IVirtualSlot virtualSlot) {
            if (this.hoveredSlot.isHighlightable()) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_BACK_SPRITE, virtualSlot.getActualX() - 4, virtualSlot.getActualY() - 4, 24, 24);
            }
        } else {
            super.extractSlotHighlightBack(graphics);
        }
    }

    //TODO - 26.1: If we AT x and y for slots to not be final, and actually update them I think we can skip this override unless we need this to be delayed
    @Override
    @Deprecated//Don't use directly, this is normally private in ContainerScreen
    protected final void extractSlotHighlightFront(GuiGraphicsExtractor graphics) {
        if (this.hoveredSlot instanceof IVirtualSlot virtualSlot) {
            if (this.hoveredSlot.isHighlightable()) {
                //TODO - 26.1: Do we need to delay this?
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_FRONT_SPRITE, virtualSlot.getActualX() - 4, virtualSlot.getActualY() - 4, 24, 24);
            }
        } else {
            super.extractSlotHighlightBack(graphics);
        }
    }

    @Override
    protected void extractSlot(GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY) {
        if (!(slot instanceof IVirtualSlot virtualSlot)) {
            //If we are not a virtual slot, the super method is good enough
            super.extractSlot(graphics, slot, mouseX, mouseY);
            return;
        }
        //Basically a copy of super.extractSlot, except with the rendering at the bottom adjusted for if we are a virtual slot
        ItemStack itemStack = slot.getItem();
        boolean quickCraftStack = false;
        boolean done = slot == this.clickedSlot && !this.draggingItem.isEmpty() && !this.isSplittingStack;
        ItemStack carried = this.menu.getCarried();
        String itemCount = null;
        if (slot == this.clickedSlot && !this.draggingItem.isEmpty() && this.isSplittingStack && !itemStack.isEmpty()) {
            itemStack = itemStack.copyWithCount(itemStack.getCount() / 2);
        } else if (this.isQuickCrafting && this.quickCraftSlots.contains(slot) && !carried.isEmpty()) {
            if (this.quickCraftSlots.size() == 1) {
                return;
            }

            if (AbstractContainerMenu.canItemQuickReplace(slot, carried, true) && this.menu.canDragTo(slot)) {
                quickCraftStack = true;
                int maxSize = Math.min(carried.getMaxStackSize(), slot.getMaxStackSize(carried));
                int carry = slot.getItem().isEmpty() ? 0 : slot.getItem().getCount();
                int newCount = AbstractContainerMenu.getQuickCraftPlaceCount(this.quickCraftSlots.size(), this.quickCraftingType, carried) + carry;
                if (newCount > maxSize) {
                    newCount = maxSize;
                    itemCount = ChatFormatting.YELLOW.toString() + maxSize;
                }

                itemStack = carried.copyWithCount(newCount);
            } else {
                this.quickCraftSlots.remove(slot);
                recalculateQuickCraftRemaining();
            }
        }
        //Note: We don't include vanilla's no item icon rendering here as virtual slots can just have that set directly, and it simplifies our copy
        //If the slot is a virtual slot, have the GuiSlot that corresponds to it handle the rendering
        if (!done) {
            virtualSlot.updateRenderInfo(itemStack, quickCraftStack, itemCount);
        }
    }

    @Override
    protected void renderSlotContents(GuiGraphicsExtractor graphics, ItemStack itemStack, Slot slot, @Nullable String itemCount) {
        if (slot instanceof IVirtualSlot virtualSlot) {
            //TODO - 26.1: Re-evaluate overriding this method? I don't think this should ever be called as we override extractSlot and change the render call?
            virtualSlot.updateRenderInfo(itemStack, false, itemCount);
        } else {
            //If we are not a virtual slot, the super method is good enough
            super.renderSlotContents(graphics, itemStack, slot, itemCount);
        }
    }

    public boolean slotClicked(Slot slot, MouseButtonEvent event, boolean doubleClick) {
        //Copy of super.mouseClicked, minus the call to all the sub elements as we know how we are interacting with it
        InputConstants.Key mouseKey = InputConstants.Type.MOUSE.getOrCreate(event.button());
        boolean cloning = this.minecraft.options.keyPickItem.isActiveAndMatches(mouseKey) && this.minecraft.player.hasInfiniteMaterials();
        this.doubleclick = this.lastClickSlot == slot && doubleClick;
        this.skipNextRelease = false;
        if (event.button() != InputConstants.MOUSE_BUTTON_LEFT && event.button() != InputConstants.MOUSE_BUTTON_RIGHT && !cloning) {
            checkHotbarMouseClicked(event);
        } else {
            boolean clickedOutside = hasClickedOutside(event.x(), event.y(), this.leftPos, this.topPos);
            if (slot != null) {
                clickedOutside = false; // Forge, prevent dropping of items through slots outside of GUI boundaries
            }
            int slotId = -1;
            if (slot != null) {
                slotId = slot.index;
            }
            if (clickedOutside) {
                slotId = AbstractContainerMenu.SLOT_CLICKED_OUTSIDE;
            }
            if (this.minecraft.options.touchscreen().get() && clickedOutside && this.menu.getCarried().isEmpty()) {
                onClose();
                return true;
            }

            if (slotId != -1) {
                if (this.minecraft.options.touchscreen().get()) {
                    if (slot != null && slot.hasItem()) {
                        this.clickedSlot = slot;
                        this.draggingItem = ItemStack.EMPTY;
                        this.isSplittingStack = event.button() == InputConstants.MOUSE_BUTTON_RIGHT;
                    } else {
                        this.clickedSlot = null;
                    }
                } else if (!this.isQuickCrafting) {
                    if (this.menu.getCarried().isEmpty()) {
                        if (this.minecraft.options.keyPickItem.isActiveAndMatches(mouseKey)) {
                            slotClicked(slot, slotId, event.button(), ContainerInput.CLONE);
                        } else {
                            boolean quickKey = slotId != AbstractContainerMenu.SLOT_CLICKED_OUTSIDE && event.hasShiftDown();
                            ContainerInput containerInput = ContainerInput.PICKUP;
                            if (quickKey) {
                                this.lastQuickMoved = slot != null && slot.hasItem() ? slot.getItem().copy() : ItemStack.EMPTY;
                                containerInput = ContainerInput.QUICK_MOVE;
                            } else if (slotId == AbstractContainerMenu.SLOT_CLICKED_OUTSIDE) {
                                containerInput = ContainerInput.THROW;
                            }

                            slotClicked(slot, slotId, event.button(), containerInput);
                        }

                        this.skipNextRelease = true;
                    } else {
                        this.isQuickCrafting = true;
                        this.quickCraftingButton = event.button();
                        this.quickCraftSlots.clear();
                        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
                            this.quickCraftingType = AbstractContainerMenu.QUICKCRAFT_TYPE_CHARITABLE;
                        } else if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT) {
                            this.quickCraftingType = AbstractContainerMenu.QUICKCRAFT_TYPE_GREEDY;
                        } else if (this.minecraft.options.keyPickItem.isActiveAndMatches(mouseKey)) {
                            this.quickCraftingType = AbstractContainerMenu.QUICKCRAFT_TYPE_CLONE;
                        }
                    }
                }
            }
        }
        this.lastClickSlot = slot;
        return true;
    }
}