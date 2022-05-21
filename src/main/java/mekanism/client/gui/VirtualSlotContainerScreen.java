package mekanism.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.inventory.container.slot.IVirtualSlot;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

//TODO - 1.19: Heavily re-evaluate this class/make sure nothing has gotten broken
public abstract class VirtualSlotContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {

    public VirtualSlotContainerScreen(T container, Inventory inv, Component titleIn) {
        super(container, inv, titleIn);
    }

    protected abstract boolean isMouseOverSlot(@Nonnull Slot slot, double mouseX, double mouseY);

    @Nullable
    @Override
    @Deprecated//Don't use directly, this is normally private in ContainerScreen
    protected Slot findSlot(double mouseX, double mouseY) {
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
    protected final boolean isHovering(@Nonnull Slot slot, double mouseX, double mouseY) {
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

    @Override
    @Deprecated//Don't use directly, this is normally private in ContainerScreen
    protected final void renderFloatingItem(@Nonnull ItemStack stack, int x, int y, @Nullable String altText) {
        if (!stack.isEmpty()) {
            //Note: We ignore if the virtual slot is not actually available as we still want to transition back to the spot
            // it was in visually
            if (stack == this.snapbackItem && this.snapbackEnd instanceof IVirtualSlot returningVirtualSlot) {
                //Use an instance equality check to see if we are rendering the returning stack (used in touch screens)
                // if we are and the slot we are returning to is a virtual one, so the position may be changing
                // then recalculate where the stack actually is/should be to send it to the correct position
                float f = (float) (Util.getMillis() - this.snapbackTime) / 100.0F;
                if (f >= 1.0F) {
                    //I don't think this should ever happen given we validated it isn't the case before entering
                    // drawItemStack, but just in case it is, update the returningStack and exit
                    this.snapbackItem = ItemStack.EMPTY;
                    return;
                }
                //Recalculate the x and y values to make sure they are the correct values
                int xOffset = returningVirtualSlot.getActualX() - this.snapbackStartX;
                int yOffset = returningVirtualSlot.getActualY() - this.snapbackStartY;
                x = this.snapbackStartX + (int) (xOffset * f);
                y = this.snapbackStartY + (int) (yOffset * f);
            }
            //noinspection ConstantConditions, altText can be null, just is marked as caught as nonnull by mojang's class level stuff
            super.renderFloatingItem(stack, x, y, altText);
        }
    }

    @Override
    @Deprecated//Don't use directly, this is normally private in ContainerScreen
    protected final void renderSlot(@Nonnull PoseStack matrixStack, @Nonnull Slot slot) {
        if (!(slot instanceof IVirtualSlot virtualSlot)) {
            //If we are not a virtual slot, the super method is good enough
            super.renderSlot(matrixStack, slot);
            return;
        }
        //Basically a copy of super.moveItems, except with the rendering at the bottom adjusted
        // for if we are a virtual slot
        ItemStack currentStack = slot.getItem();
        boolean shouldDrawOverlay = false;
        boolean skipStackRendering = slot == this.clickedSlot && !this.draggingItem.isEmpty() && !this.isSplittingStack;
        ItemStack heldStack = minecraft.player.containerMenu.getCarried();
        String s = null;
        if (slot == this.clickedSlot && !this.draggingItem.isEmpty() && this.isSplittingStack && !currentStack.isEmpty()) {
            currentStack = currentStack.copy();
            currentStack.setCount(currentStack.getCount() / 2);
        } else if (isQuickCrafting && quickCraftSlots.contains(slot) && !heldStack.isEmpty()) {
            if (quickCraftSlots.size() == 1) {
                return;
            }
            if (AbstractContainerMenu.canItemQuickReplace(slot, heldStack, true) && this.menu.canDragTo(slot)) {
                currentStack = heldStack.copy();
                shouldDrawOverlay = true;
                AbstractContainerMenu.getQuickCraftSlotCount(quickCraftSlots, this.quickCraftingType, currentStack, slot.getItem().isEmpty() ? 0 : slot.getItem().getCount());
                int k = Math.min(currentStack.getMaxStackSize(), slot.getMaxStackSize(currentStack));
                if (currentStack.getCount() > k) {
                    s = ChatFormatting.YELLOW.toString() + k;
                    currentStack.setCount(k);
                }
            } else {
                quickCraftSlots.remove(slot);
                recalculateQuickCraftRemaining();
            }
        }
        //If the slot is a virtual slot, have the GuiSlot that corresponds to it handle the rendering
        virtualSlot.updateRenderInfo(skipStackRendering ? ItemStack.EMPTY : currentStack, shouldDrawOverlay, s);
    }

    public boolean slotClicked(@Nonnull Slot slot, int button) {
        //Copy of super.mouseClicked, minus the call to all the sub elements as we know how we are interacting with it
        InputConstants.Key mouseKey = InputConstants.Type.MOUSE.getOrCreate(button);
        boolean pickBlockButton = minecraft.options.keyPickItem.isActiveAndMatches(mouseKey);
        long time = Util.getMillis();
        this.doubleclick = this.lastClickSlot == slot && time - this.lastClickTime < 250L && this.lastClickButton == button;
        this.skipNextRelease = false;
        if (button != 0 && button != 1 && !pickBlockButton) {
            checkHotbarMouseClicked(button);
        } else if (slot.index != -1) {
            if (minecraft.options.touchscreen) {
                if (slot.hasItem()) {
                    this.clickedSlot = slot;
                    this.draggingItem = ItemStack.EMPTY;
                    this.isSplittingStack = button == 1;
                } else {
                    this.clickedSlot = null;
                }
            } else if (!this.isQuickCrafting) {
                if (minecraft.player.containerMenu.getCarried().isEmpty()) {
                    if (pickBlockButton) {
                        this.slotClicked(slot, slot.index, button, ClickType.CLONE);
                    } else {
                        ClickType clicktype = ClickType.PICKUP;
                        if (Screen.hasShiftDown()) {
                            this.lastQuickMoved = slot.hasItem() ? slot.getItem().copy() : ItemStack.EMPTY;
                            clicktype = ClickType.QUICK_MOVE;
                        }
                        this.slotClicked(slot, slot.index, button, clicktype);
                    }
                    this.skipNextRelease = true;
                } else {
                    this.isQuickCrafting = true;
                    this.quickCraftingButton = button;
                    this.quickCraftSlots.clear();
                    if (button == 0) {
                        this.quickCraftingType = 0;
                    } else if (button == 1) {
                        this.quickCraftingType = 1;
                    } else if (pickBlockButton) {
                        this.quickCraftingType = 2;
                    }
                }
            }
        }
        this.lastClickSlot = slot;
        this.lastClickTime = time;
        this.lastClickButton = button;
        return true;
    }
}