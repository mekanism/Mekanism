package mekanism.generators.common.tile.turbine;

import javax.annotation.Nonnull;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.IMekanismGasHandler;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.util.CableUtils;
import mekanism.generators.common.registries.GeneratorsBlocks;

public class TileEntityTurbineValve extends TileEntityTurbineCasing {

    public TileEntityTurbineValve() {
        super(GeneratorsBlocks.TURBINE_VALVE);
    }

    @Nonnull
    @Override
    public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(@Nonnull IMekanismGasHandler handler) {
        return side -> getMultiblock().getGasTanks(side);
    }

    @Nonnull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers() {
        return side -> getMultiblock().getEnergyContainers(side);
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (getMultiblock().isFormed()) {
            CableUtils.emit(getMultiblock().getDirectionsToEmit(getPos()), getMultiblock().energyContainer, this);
        }
    }

    @Override
    public boolean persists(SubstanceType type) {
        //Do not handle gas when it comes to syncing it/saving this tile to disk
        if (type == SubstanceType.GAS || type == SubstanceType.ENERGY) {
            return false;
        }
        return super.persists(type);
    }

    @Override
    public int getRedstoneLevel() {
        return getMultiblock().getCurrentRedstoneLevel();
    }
}