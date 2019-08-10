package mekanism.common.inventory.container;

import javax.annotation.Nonnull;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.common.inventory.slot.SlotEnergy;
import mekanism.common.inventory.slot.SlotOutput;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.InfusionInput;
import mekanism.common.tile.TileEntityMetallurgicInfuser;
import mekanism.common.util.ChargeUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

public class ContainerMetallurgicInfuser extends ContainerMekanism<TileEntityMetallurgicInfuser> {

    public ContainerMetallurgicInfuser(PlayerInventory inventory, TileEntityMetallurgicInfuser tile) {
        super(tile, inventory);
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int slotID) {
        ItemStack stack = ItemStack.EMPTY;
        Slot currentSlot = inventorySlots.get(slotID);
        if (currentSlot != null && currentSlot.getHasStack()) {
            ItemStack slotStack = currentSlot.getStack();
            stack = slotStack.copy();
            if (slotID != 0 && slotID != 1 && slotID != 2 && slotID != 3) {
                if (InfuseRegistry.getObject(slotStack) != null && (tileEntity.infuseStored.getType() == null || tileEntity.infuseStored.getType() == InfuseRegistry.getObject(slotStack).type)) {
                    if (!mergeItemStack(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (ChargeUtils.canBeDischarged(slotStack)) {
                    if (!mergeItemStack(slotStack, 3, 4, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (isInputItem(slotStack)) {
                    if (!mergeItemStack(slotStack, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (slotID <= 30) {
                        if (!mergeItemStack(slotStack, 31, inventorySlots.size(), false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!mergeItemStack(slotStack, 4, 30, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!mergeItemStack(slotStack, 4, inventorySlots.size(), true)) {
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

    public boolean isInputItem(ItemStack itemStack) {
        if (tileEntity.infuseStored.getType() != null) {
            return RecipeHandler.getMetallurgicInfuserRecipe(new InfusionInput(tileEntity.infuseStored, itemStack)) != null;
        }
        for (InfusionInput input : Recipe.METALLURGIC_INFUSER.get().keySet()) {
            if (ItemHandlerHelper.canItemStacksStack(input.inputStack, itemStack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void addSlots() {
        addSlot(new Slot(tileEntity, 1, 17, 35));
        addSlot(new Slot(tileEntity, 2, 51, 43));
        addSlot(new SlotOutput(tileEntity, 3, 109, 43));
        addSlot(new SlotEnergy.SlotDischarge(tileEntity, 4, 143, 35));
    }
}