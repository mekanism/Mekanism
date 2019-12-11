package mekanism.common.content.transporter;

import mekanism.common.util.InventoryUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.items.IItemHandler;

public class StackSearcher {

    private int slotCount = -1;
    private TileEntity tile;
    private Direction side;

    public StackSearcher(TileEntity tile, Direction direction) {
        this.tile = tile;
        side = direction;
        if (InventoryUtils.isItemHandler(tile, direction.getOpposite())) {
            slotCount = InventoryUtils.getItemHandler(tile, direction.getOpposite()).getSlots();
        }
    }

    public InvStack takeTopStack(Finder id, int amount) {
        if (!InventoryUtils.assertItemHandler("StackSearcher", tile, side.getOpposite())) {
            return null;
        }
        IItemHandler inventory = InventoryUtils.getItemHandler(tile, side.getOpposite());
        for (slotCount = slotCount - 1; slotCount >= 0; slotCount--) {
            ItemStack stack = inventory.extractItem(slotCount, amount, true);
            if (!stack.isEmpty() && id.modifies(stack)) {
                return new InvStack(tile, slotCount, stack, side.getOpposite());
            }
        }
        return null;
    }

    public InvStack takeDefinedItem(ItemStack type, int min, int max) {
        InvStack ret = new InvStack(tile, side.getOpposite());
        if (!InventoryUtils.assertItemHandler("StackSearcher", tile, side.getOpposite())) {
            return null;
        }
        IItemHandler inventory = InventoryUtils.getItemHandler(tile, side.getOpposite());
        for (slotCount = slotCount - 1; slotCount >= 0; slotCount--) {
            ItemStack stack = inventory.extractItem(slotCount, max, true);
            if (!stack.isEmpty() && StackUtils.equalsWildcardWithNBT(stack, type)) {
                int current = ret.getStack().getCount();
                if (current + stack.getCount() <= max) {
                    ret.appendStack(slotCount, stack.copy());
                } else {
                    ItemStack copy = stack.copy();
                    copy.setCount(max - current);
                    ret.appendStack(slotCount, copy);
                }
                if (!ret.getStack().isEmpty() && ret.getStack().getCount() == max) {
                    return ret;
                }
            }
        }

        if (!ret.getStack().isEmpty() && ret.getStack().getCount() >= min) {
            return ret;
        }
        return null;
    }

    public int getSlotCount() {
        return slotCount;
    }
}