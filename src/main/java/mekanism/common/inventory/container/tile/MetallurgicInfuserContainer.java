package mekanism.common.inventory.container.tile;

import javax.annotation.Nonnull;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.api.infuse.InfusionStack;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.inventory.container.slot.SlotEnergy;
import mekanism.common.inventory.container.slot.SlotOutput;
import mekanism.common.tile.TileEntityMetallurgicInfuser;
import mekanism.common.util.ChargeUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

public class MetallurgicInfuserContainer extends MekanismTileContainer<TileEntityMetallurgicInfuser> {

    public MetallurgicInfuserContainer(int id, PlayerInventory inv, TileEntityMetallurgicInfuser tile) {
        super(MekanismContainerTypes.METALLURGIC_INFUSER, id, inv, tile);
    }

    public MetallurgicInfuserContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityMetallurgicInfuser.class));
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
                InfusionStack slotInfusionStack = InfuseRegistry.getObject(slotStack);
                if (!slotInfusionStack.isEmpty() && (tile.infusionTank.isEmpty() || tile.infusionTank.getType() == slotInfusionStack.getType())) {
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
        //If we have a type make sure that the recipe is valid for the type we have stored
        if (!tile.infusionTank.isEmpty()) {
            InfusionStack currentInfuseType = new InfusionStack(tile.infusionTank.getType(), tile.infusionTank.getStored());
            return tile.containsRecipe(recipe -> recipe.getInfusionInput().testType(currentInfuseType) && recipe.getItemInput().testType(itemStack));
        }
        //Otherwise just look for items that can be used
        return tile.containsRecipe(recipe -> recipe.getItemInput().testType(itemStack));
    }

    @Override
    protected void addSlots() {
        addSlot(new Slot(tile, 1, 17, 35));
        addSlot(new Slot(tile, 2, 51, 43));
        addSlot(new SlotOutput(tile, 3, 109, 43));
        addSlot(new SlotEnergy.SlotDischarge(tile, 4, 143, 35));
    }
}