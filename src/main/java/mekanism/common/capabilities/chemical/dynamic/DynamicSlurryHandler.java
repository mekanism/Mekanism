package mekanism.common.capabilities.chemical.dynamic;

import mekanism.api.chemical.slurry.IMekanismSlurryHandler;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.capabilities.resolver.manager.chemical.ChemicalHandlerManager;
import net.minecraftforge.common.util.NonNullSupplier;

public class DynamicSlurryHandler extends DynamicChemicalHandler<Slurry, SlurryStack, ISlurryTank> implements IMekanismSlurryHandler {

    public DynamicSlurryHandler(NonNullSupplier<ChemicalHandlerManager<Slurry, SlurryStack, ISlurryTank, ?, ?>> handlerManager, InteractPredicate canExtract,
          InteractPredicate canInsert, Runnable onContentsChanged) {
        super(handlerManager, canExtract, canInsert, onContentsChanged);
    }
}