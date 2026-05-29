package mekanism.common.lib.multiblock;

import java.util.List;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IMekanismHeatHandler;
import mekanism.api.inventory.IInventorySlot;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

public interface IMultiblockContents extends IMekanismHeatHandler {

    @NotNull
    List<IInventorySlot> getInventorySlots();

    @NotNull
    List<IFluidTank> getFluidTanks();

    @NotNull
    List<IChemicalTank> getChemicalTanks();

    @Nullable
    IEnergyContainer getEnergyContainer();

    @NotNull
    default List<IHeatCapacitor> getHeatCapacitors() {
        return getHeatCapacitors(null);
    }
}