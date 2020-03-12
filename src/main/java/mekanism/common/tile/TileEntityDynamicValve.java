package mekanism.common.tile;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.MekanismUtils;
import net.minecraft.util.Direction;

public class TileEntityDynamicValve extends TileEntityDynamicTank {

    public TileEntityDynamicValve() {
        super(MekanismBlocks.DYNAMIC_VALVE);
    }

    @Override
    public boolean canHandleFluid() {
        //Mark that we can handle fluid
        return true;
    }

    @Override
    public boolean persistFluid() {
        //But that we do not handle fluid when it comes to syncing it/saving this tile to disk
        return false;
    }

    @Nonnull
    @Override
    public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
        return canHandleFluid() && structure != null ? structure.getFluidTanks(side) : Collections.emptyList();
    }

    @Override
    public int getRedstoneLevel() {
        return structure == null ? 0 : MekanismUtils.redstoneLevelFromContents(structure.fluidTank.getFluidAmount(), structure.fluidTank.getCapacity());
    }
}