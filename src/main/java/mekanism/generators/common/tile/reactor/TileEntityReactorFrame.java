package mekanism.generators.common.tile.reactor;

import javax.annotation.Nonnull;
import mekanism.common.util.InventoryUtils;
import net.minecraft.util.EnumFacing;

public class TileEntityReactorFrame extends TileEntityReactorBlock {

    @Override
    public boolean isFrame() {
        return true;
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull EnumFacing side) {
        return InventoryUtils.EMPTY;
    }
}
