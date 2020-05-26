package mekanism.common.capabilities.chemical.dynamic;

import mekanism.api.chemical.pigment.IMekanismPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.common.capabilities.resolver.manager.chemical.ChemicalHandlerManager;
import net.minecraftforge.common.util.NonNullSupplier;

public class DynamicPigmentHandler extends DynamicChemicalHandler<Pigment, PigmentStack, IPigmentTank> implements IMekanismPigmentHandler {

    public DynamicPigmentHandler(NonNullSupplier<ChemicalHandlerManager<Pigment, PigmentStack, IPigmentTank, ?, ?>> handlerManager, InteractPredicate canExtract,
          InteractPredicate canInsert, Runnable onContentsChanged) {
        super(handlerManager, canExtract, canInsert, onContentsChanged);
    }
}