package mekanism.common.base;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.util.Direction;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

//TODO: Should we inline SidedInvWrapper? Previously we had to extend to be able to override isItemValid,
// but all 1.14 versions of forge have the proper impl for isItemValid
public class ItemHandlerWrapper extends SidedInvWrapper {

    public ItemHandlerWrapper(ISidedInventory inv, Direction side) {
        super(inv, side);
    }
}