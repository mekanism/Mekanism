package mekanism.common.capabilities.chemical.dynamic;

import java.util.List;
import java.util.function.Function;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.pigment.IMekanismPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import net.minecraft.util.Direction;

public class DynamicPigmentHandler extends DynamicChemicalHandler<Pigment, PigmentStack, IPigmentTank> implements IMekanismPigmentHandler {

    public DynamicPigmentHandler(Function<Direction, List<IPigmentTank>> tankSupplier, InteractPredicate canExtract, InteractPredicate canInsert,
          IContentsListener listener) {
        super(tankSupplier, canExtract, canInsert, listener);
    }
}