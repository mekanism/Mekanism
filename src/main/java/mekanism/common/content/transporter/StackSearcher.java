package mekanism.common.content.transporter;

import mekanism.common.util.InventoryUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.IItemHandler;

public class StackSearcher {

    private int slotCount = -1;
    private TileEntity tileEntity;
    private EnumFacing side;

    public StackSearcher(TileEntity tile, EnumFacing direction) {
        tileEntity = tile;
        side = direction;

        if (InventoryUtils.isItemHandler(tile, direction.getOpposite())) {
            slotCount = InventoryUtils.getItemHandler(tile, direction.getOpposite()).getSlots();
        }
    }

    public InvStack takeTopStack(Finder id) {
        if (!InventoryUtils.assertItemHandler("StackSearcher", tileEntity, side.getOpposite())) {
            return null;
        }

        IItemHandler inventory = InventoryUtils.getItemHandler(tileEntity, side.getOpposite());

        for (slotCount = slotCount - 1; slotCount >= 0; slotCount--) {
            ItemStack stack = inventory.extractItem(slotCount, 64, true);

            if (!stack.isEmpty() && id.modifies(stack)) {
                return new InvStack(tileEntity, slotCount, stack, side.getOpposite());
            }
        }

        return null;
    }

    public InvStack takeDefinedItem(ItemStack type, int min, int max) {
        InvStack ret = new InvStack(tileEntity, side.getOpposite());

        if (!InventoryUtils.assertItemHandler("StackSearcher", tileEntity, side.getOpposite())) {
            return null;
        }

        IItemHandler inventory = InventoryUtils.getItemHandler(tileEntity, side.getOpposite());

        for (slotCount = slotCount - 1; slotCount >= 0; slotCount--) {
            ItemStack stack = inventory.extractItem(slotCount, max, true);

            if (!stack.isEmpty() && StackUtils.equalsWildcard(stack, type)) {
                int current = !ret.getStack().isEmpty() ? ret.getStack().getCount() : 0;

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
