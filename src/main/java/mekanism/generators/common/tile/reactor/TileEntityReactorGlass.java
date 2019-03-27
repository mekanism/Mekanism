package mekanism.generators.common.tile.reactor;

import mekanism.common.util.InventoryUtils;
import net.minecraft.util.EnumFacing;

public class TileEntityReactorGlass extends TileEntityReactorBlock {

    @Override
    public boolean isFrame() {
        return false;
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return InventoryUtils.EMPTY;
    }
}
