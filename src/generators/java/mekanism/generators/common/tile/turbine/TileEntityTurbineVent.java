package mekanism.generators.common.tile.turbine;

import java.util.Collections;
import javax.annotation.Nonnull;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.util.PipeUtils;
import mekanism.generators.common.registries.GeneratorsBlocks;

public class TileEntityTurbineVent extends TileEntityTurbineCasing {

    public TileEntityTurbineVent() {
        super(GeneratorsBlocks.TURBINE_VENT);
    }

    @Nonnull
    @Override
    protected IFluidTankHolder getInitialFluidTanks() {
        return side -> structure == null ? Collections.emptyList() : structure.ventTanks;
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (structure != null) {
            PipeUtils.emit(structure.ventTank, this);
        }
    }

    @Override
    public boolean persistFluid() {
        //Do not handle fluid when it comes to syncing it/saving this tile to disk
        return false;
    }
}