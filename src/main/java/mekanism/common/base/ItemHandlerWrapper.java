package mekanism.common.base;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

public class ItemHandlerWrapper extends SidedInvWrapper {

    public ItemHandlerWrapper(ISidedInventory inv, EnumFacing side) {
        super(inv, side);
    }
}
