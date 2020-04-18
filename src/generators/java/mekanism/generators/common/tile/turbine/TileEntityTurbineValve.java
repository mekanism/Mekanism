package mekanism.generators.common.tile.turbine;

import java.util.Collections;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.util.CableUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.registries.GeneratorsBlocks;

public class TileEntityTurbineValve extends TileEntityTurbineCasing {

    public TileEntityTurbineValve() {
        super(GeneratorsBlocks.TURBINE_VALVE);
    }

    @Nonnull
    @Override
    protected IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks() {
        return side -> structure == null ? Collections.emptyList() : structure.getGasTanks(side);
    }

    @Nonnull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers() {
        return side -> structure == null ? Collections.emptyList() : structure.getEnergyContainers(side);
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (structure != null) {
            CableUtils.emit(structure.getDirectionsToEmit(Coord4D.get(this)), structure.energyContainer, this);
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
        return structure == null ? 0 : MekanismUtils.redstoneLevelFromContents(structure.gasTank.getStored(), structure.gasTank.getCapacity());
    }
}