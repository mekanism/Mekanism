package mekanism.common.content.transporter;

import mekanism.common.util.InventoryUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.IItemHandler;

public class StackSearcher {

    public int i = -1;
    public TileEntity tileEntity;
    public EnumFacing side;

    public StackSearcher(TileEntity tile, EnumFacing direction) {
        tileEntity = tile;
        side = direction;

        if (InventoryUtils.isItemHandler(tile, direction.getOpposite())) {
            i = InventoryUtils.getItemHandler(tile, direction.getOpposite()).getSlots();
        }
    }

    public InvStack takeTopStack(Finder id) {
        if (InventoryUtils.isItemHandler(tileEntity, side.getOpposite())) {
            IItemHandler inventory = InventoryUtils.getItemHandler(tileEntity, side.getOpposite());

            for (i = i - 1; i >= 0; i--) {
                ItemStack stack = inventory.extractItem(i, 64, true);

                if (!stack.isEmpty() && id.modifies(stack)) {
                    return new InvStack(tileEntity, i, stack, side.getOpposite());
                }
            }
        }

        return null;
    }

    public InvStack takeDefinedItem(ItemStack type, int min, int max) {
        InvStack ret = new InvStack(tileEntity, side.getOpposite());

        if (InventoryUtils.isItemHandler(tileEntity, side.getOpposite())) {
            IItemHandler inventory = InventoryUtils.getItemHandler(tileEntity, side.getOpposite());

            for (i = i - 1; i >= 0; i--) {
                ItemStack stack = inventory.extractItem(i, max, true);

                if (!stack.isEmpty() && StackUtils.equalsWildcard(stack, type)) {
                    int current = !ret.getStack().isEmpty() ? ret.getStack().getCount() : 0;

                    if (current + stack.getCount() <= max) {
                        ret.appendStack(i, stack.copy());
                    } else {
                        ItemStack copy = stack.copy();
                        copy.setCount(max - current);
                        ret.appendStack(i, copy);
                    }

                    if (!ret.getStack().isEmpty() && ret.getStack().getCount() == max) {
                        return ret;
                    }
                }
            }
        }

        if (!ret.getStack().isEmpty() && ret.getStack().getCount() >= min) {
            return ret;
        }

        return null;
    }
}
