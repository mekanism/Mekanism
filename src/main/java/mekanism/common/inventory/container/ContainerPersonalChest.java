package mekanism.common.inventory.container;

import invtweaks.api.container.ChestContainer;
import javax.annotation.Nonnull;
import mekanism.common.inventory.slot.SlotPersonalChest;
import mekanism.common.item.block.machine.ItemBlockPersonalChest;
import mekanism.common.tile.TileEntityPersonalChest;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

@ChestContainer(isLargeChest = true)
public class ContainerPersonalChest extends ContainerMekanism<TileEntityPersonalChest> {

    private IInventory itemInventory;
    private boolean isBlock;

    public ContainerPersonalChest(PlayerInventory inventory, TileEntityPersonalChest tile) {
        super(tile, inventory);
        itemInventory = null;
        isBlock = true;
        addAndOpen(inventory);
    }

    public ContainerPersonalChest(PlayerInventory inventory, IInventory inv) {
        super(null, inventory);
        itemInventory = inv;
        isBlock = false;
        addAndOpen(inventory);
    }

    private void addAndOpen(PlayerInventory inventory) {
        //Manually handle this stuff so that it gets called at the correct time
        addSlots();
        addInventorySlots(inventory);
        openInventory(inventory);
    }

    @Override
    protected boolean shouldAddSlots() {
        return false;
    }

    @Override
    protected void addSlots() {
        for (int slotY = 0; slotY < 6; slotY++) {
            for (int slotX = 0; slotX < 9; slotX++) {
                addSlot(new SlotPersonalChest(getInv(), slotX + slotY * 9, 8 + slotX * 18, 26 + slotY * 18));
            }
        }
    }

    @Override
    protected int getInventoryOffset() {
        return 148;
    }

    @Override
    protected void closeInventory(PlayerEntity entityplayer) {
        if (isBlock) {
            tileEntity.close(entityplayer);
            tileEntity.closeInventory(entityplayer);
        } else {
            itemInventory.closeInventory(entityplayer);
        }
    }

    @Override
    protected void openInventory(PlayerInventory inventory) {
        if (isBlock) {
            tileEntity.open(inventory.player);
            tileEntity.openInventory(inventory.player);
        } else {
            itemInventory.openInventory(inventory.player);
        }
    }

    public IInventory getInv() {
        if (isBlock) {
            return tileEntity;
        }
        return itemInventory;
    }

    @Override
    public boolean canInteractWith(@Nonnull PlayerEntity entityplayer) {
        if (isBlock) {
            return tileEntity.isUsableByPlayer(entityplayer);
        }
        return true;
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int slotID) {
        ItemStack stack = ItemStack.EMPTY;
        Slot currentSlot = inventorySlots.get(slotID);
        if (currentSlot != null && currentSlot.getHasStack()) {
            ItemStack slotStack = currentSlot.getStack();
            stack = slotStack.copy();
            if (slotID < 54) {
                if (!mergeItemStack(slotStack, 54, inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!mergeItemStack(slotStack, 0, 54, false)) {
                return ItemStack.EMPTY;
            }
            if (slotStack.getCount() == 0) {
                currentSlot.putStack(ItemStack.EMPTY);
            } else {
                currentSlot.onSlotChanged();
            }
            if (slotStack.getCount() == stack.getCount()) {
                return ItemStack.EMPTY;
            }
            currentSlot.onTake(player, slotStack);
        }
        return stack;
    }

    @Nonnull
    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickType, PlayerEntity player) {
        int hotbarSlotId = slotId - 81;
        //Disallow moving Personal Chest if held and accessed directly from inventory (not from a placed block)
        if (!isBlock && hotbarSlotId >= 0 && hotbarSlotId < 9 && player.inventory.currentItem == hotbarSlotId) {
            ItemStack itemStack = player.inventory.getStackInSlot(hotbarSlotId);
            if (!itemStack.isEmpty() && itemStack.getItem() instanceof ItemBlockPersonalChest) {
                return ItemStack.EMPTY;
            }
        }
        return super.slotClick(slotId, dragType, clickType, player);
    }
}