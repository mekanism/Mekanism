package mekanism.generators.common.content.turbine;

import mekanism.common.capabilities.fluid.MultiblockFluidTank;
import mekanism.common.tags.MekanismTags;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;

public class TurbineFluidTank extends MultiblockFluidTank<TileEntityTurbineCasing> {

    private int lastStored;

    public TurbineFluidTank(TileEntityTurbineCasing tile) {
        super(tile, () -> tile.structure == null ? 0 : tile.structure.getFluidCapacity(), fluid -> fluid.getFluid().isIn(MekanismTags.Fluids.STEAM));
        lastStored = getFluidAmount();
    }

    @Override
    public void onContentsChanged() {
        super.onContentsChanged();
        int amount = getFluidAmount();
        if (multiblock.structure != null) {
            if (amount >= lastStored) {
                multiblock.structure.lastSteamInput = amount - lastStored;
            } else {
                multiblock.structure.lastSteamInput = 0;
            }
        }
        lastStored = amount;
    }
}