package mekanism.common.lib.multiblock;

import java.util.List;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IMekanismHeatHandler;
import mekanism.api.inventory.IInventorySlot;
import org.jetbrains.annotations.NotNull;

//TODO - 26.1: Potentially rename this interface
public interface IMultiblockContents extends IMekanismFluidHandler, IMekanismStrictEnergyHandler, IMekanismHeatHandler, IMekanismChemicalHandler {

    @NotNull
    List<IInventorySlot> getInventorySlots();

    @NotNull
    default List<IExtendedFluidTank> getFluidTanks() {
        return getFluidTanks(null);
    }

    @NotNull
    default List<IEnergyContainer> getEnergyContainers() {
        return getEnergyContainers(null);
    }

    @NotNull
    default List<IHeatCapacitor> getHeatCapacitors() {
        return getHeatCapacitors(null);
    }
}