package mekanism.common.capabilities.chemical.dynamic;

import java.util.List;
import java.util.function.Function;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.IMekanismInfusionHandler;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import net.minecraft.util.Direction;

public class DynamicInfusionHandler extends DynamicChemicalHandler<InfuseType, InfusionStack, IInfusionTank> implements IMekanismInfusionHandler {

    public DynamicInfusionHandler(Function<Direction, List<IInfusionTank>> tankSupplier, InteractPredicate canExtract, InteractPredicate canInsert,
          IContentsListener listener) {
        super(tankSupplier, canExtract, canInsert, listener);
    }
}