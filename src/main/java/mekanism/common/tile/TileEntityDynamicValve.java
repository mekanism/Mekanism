package mekanism.common.tile;

import java.util.Collections;
import javax.annotation.Nonnull;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.MekanismUtils;

public class TileEntityDynamicValve extends TileEntityDynamicTank {

    public TileEntityDynamicValve() {
        super(MekanismBlocks.DYNAMIC_VALVE);
    }

    @Nonnull
    @Override
    protected IFluidTankHolder getInitialFluidTanks() {
        return side -> structure == null ? Collections.emptyList() : structure.getFluidTanks(side);
    }

    @Override
    public boolean persistFluid() {
        //Do not handle fluid when it comes to syncing it/saving this tile to disk
        return false;
    }

    @Override
    public int getRedstoneLevel() {
        return structure == null ? 0 : MekanismUtils.redstoneLevelFromContents(structure.fluidTank.getFluidAmount(), structure.fluidTank.getCapacity());
    }
}