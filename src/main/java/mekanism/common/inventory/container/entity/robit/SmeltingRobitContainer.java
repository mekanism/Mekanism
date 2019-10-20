package mekanism.common.inventory.container.entity.robit;

import java.util.Optional;
import javax.annotation.Nonnull;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.MekanismContainerTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.FurnaceTileEntity;

//TODO: Rework this when we switch to using the cached recipe system.
public class SmeltingRobitContainer extends RobitContainer {

    private int lastCookTime = 0;
    private int lastBurnTime = 0;
    private int lastItemBurnTime = 0;

    public SmeltingRobitContainer(int id, PlayerInventory inv, EntityRobit robit) {
        super(MekanismContainerTypes.SMELTING_ROBIT, id, inv, robit);
    }

    public SmeltingRobitContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getEntityFromBuf(buf, EntityRobit.class));
    }

    @Override
    public void addListener(@Nonnull IContainerListener listener) {
        super.addListener(listener);
        listener.sendWindowProperty(this, 0, entity.furnaceCookTime);
        listener.sendWindowProperty(this, 1, entity.furnaceBurnTime);
        listener.sendWindowProperty(this, 2, entity.currentItemBurnTime);
    }

    //TODO: I believe this stuff is handled in the super handling of listeners
    /*@Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (IContainerListener listener : listeners) {
            if (lastCookTime != entity.furnaceCookTime) {
                listener.sendWindowProperty(this, 0, entity.furnaceCookTime);
            }
            if (lastBurnTime != entity.furnaceBurnTime) {
                listener.sendWindowProperty(this, 1, entity.furnaceBurnTime);
            }
            if (lastItemBurnTime != entity.currentItemBurnTime) {
                listener.sendWindowProperty(this, 2, entity.currentItemBurnTime);
            }
        }
        lastCookTime = entity.furnaceCookTime;
        lastBurnTime = entity.furnaceBurnTime;
        lastItemBurnTime = entity.currentItemBurnTime;
    }*/

    @Override
    public void updateProgressBar(int i, int j) {
        if (i == 0) {
            entity.furnaceCookTime = j;
        }
        if (i == 1) {
            entity.furnaceBurnTime = j;
        }
        if (i == 2) {
            entity.currentItemBurnTime = j;
        }
    }

    //TODO: Remove this, just leaving it for now until we switch to the cached recipe system to make it easier to remember to check things
    @Nonnull
    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int slotID) {
        ItemStack stack = ItemStack.EMPTY;
        Slot currentSlot = inventorySlots.get(slotID);
        if (currentSlot != null && currentSlot.getHasStack()) {
            ItemStack slotStack = currentSlot.getStack();
            stack = slotStack.copy();
            if (slotID == 2) {
                if (!mergeItemStack(slotStack, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotID != 1 && slotID != 0) {
                Optional<FurnaceRecipe> recipe = entity.world.getRecipeManager().getRecipe(IRecipeType.SMELTING, new Inventory(slotStack), entity.world);
                if (recipe.isPresent() && !recipe.get().getRecipeOutput().isEmpty()) {
                    if (!mergeItemStack(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (FurnaceTileEntity.isFuel(slotStack)) {
                    if (!mergeItemStack(slotStack, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotID < 30) {
                    if (!mergeItemStack(slotStack, 30, 39, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotID < 39 && !mergeItemStack(slotStack, 3, 30, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!mergeItemStack(slotStack, 3, 39, false)) {
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
}