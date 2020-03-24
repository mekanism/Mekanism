package mekanism.generators.common.tile.turbine;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.util.CableUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.util.Direction;

public class TileEntityTurbineValve extends TileEntityTurbineCasing {

    public TileEntityTurbineValve() {
        super(GeneratorsBlocks.TURBINE_VALVE);
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (structure != null) {
            CableUtils.emit(structure.getDirectionsToEmit(Coord4D.get(this)), structure.energyContainer, this);
        }
    }

    @Override
    public boolean canHandleGas() {
        //Mark that we can handle gas
        return true;
    }

    @Override
    public boolean persistGas() {
        //But that we do not handle gas when it comes to syncing it/saving this tile to disk
        return false;
    }

    @Nonnull
    @Override
    public List<? extends IChemicalTank<Gas, GasStack>> getGasTanks(@Nullable Direction side) {
        return canHandleGas() && structure != null ? structure.getGasTanks(side) : Collections.emptyList();
    }

    @Override
    public boolean canHandleEnergy() {
        //Mark that we can handle energy
        return true;
    }

    @Override
    public boolean persistEnergy() {
        //But that we do not handle energy when it comes to syncing it/saving this tile to disk
        return false;
    }

    @Nonnull
    @Override
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
        return canHandleEnergy() && structure != null ? structure.getEnergyContainers(side) : Collections.emptyList();
    }

    @Override
    public int getRedstoneLevel() {
        return structure == null ? 0 : MekanismUtils.redstoneLevelFromContents(structure.gasTank.getStored(), structure.gasTank.getCapacity());
    }
}