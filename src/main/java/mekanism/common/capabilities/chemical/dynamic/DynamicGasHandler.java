package mekanism.common.capabilities.chemical.dynamic;

import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.IMekanismGasHandler;
import mekanism.common.capabilities.resolver.manager.chemical.ChemicalHandlerManager;
import net.minecraftforge.common.util.NonNullSupplier;

public class DynamicGasHandler extends DynamicChemicalHandler<Gas, GasStack, IGasTank> implements IMekanismGasHandler {

    public DynamicGasHandler(NonNullSupplier<ChemicalHandlerManager<Gas, GasStack, IGasTank, ?, ?>> handlerManager, InteractPredicate canExtract,
          InteractPredicate canInsert, Runnable onContentsChanged) {
        super(handlerManager, canExtract, canInsert, onContentsChanged);
    }
}