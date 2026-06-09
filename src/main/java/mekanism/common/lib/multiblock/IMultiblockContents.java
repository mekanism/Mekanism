package mekanism.common.lib.multiblock;

import java.util.List;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IMekanismHeatHandler;
import mekanism.api.inventory.IInventorySlot;
import org.jspecify.annotations.Nullable;

public interface IMultiblockContents extends IMekanismHeatHandler {

    List<IInventorySlot> getInventorySlots();

    List<IFluidTank> getFluidTanks();

    List<IChemicalTank> getChemicalTanks();

    @Nullable
    IEnergyContainer getEnergyContainer();

    default List<IHeatCapacitor> getHeatCapacitors() {
        return getHeatCapacitors(null);
    }
}