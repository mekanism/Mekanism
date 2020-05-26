package mekanism.common.capabilities.chemical.dynamic;

import java.util.List;
import java.util.function.Function;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.slurry.IMekanismSlurryHandler;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import net.minecraft.util.Direction;

public class DynamicSlurryHandler extends DynamicChemicalHandler<Slurry, SlurryStack, ISlurryTank> implements IMekanismSlurryHandler {

    public DynamicSlurryHandler(Function<Direction, List<ISlurryTank>> tankSupplier, InteractPredicate canExtract, InteractPredicate canInsert,
          IContentsListener listener) {
        super(tankSupplier, canExtract, canInsert, listener);
    }
}