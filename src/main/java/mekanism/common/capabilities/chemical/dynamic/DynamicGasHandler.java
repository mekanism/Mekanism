package mekanism.common.capabilities.chemical.dynamic;

import java.util.List;
import java.util.function.Function;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.IMekanismGasHandler;
import net.minecraft.util.Direction;

public class DynamicGasHandler extends DynamicChemicalHandler<Gas, GasStack, IGasTank> implements IMekanismGasHandler {

    public DynamicGasHandler(Function<Direction, List<IGasTank>> tankSupplier, InteractPredicate canExtract, InteractPredicate canInsert, IContentsListener listener) {
        super(tankSupplier, canExtract, canInsert, listener);
    }
}