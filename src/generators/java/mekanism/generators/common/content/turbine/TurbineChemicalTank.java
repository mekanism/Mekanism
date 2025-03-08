package mekanism.generators.common.content.turbine;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.common.capabilities.chemical.VariableCapacityChemicalTank;
import mekanism.common.registries.MekanismChemicals;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class TurbineChemicalTank extends VariableCapacityChemicalTank {

    private final TurbineMultiblockData multiblock;

    public TurbineChemicalTank(TurbineMultiblockData multiblock, @Nullable IContentsListener listener) {
        super(multiblock::getSteamCapacity, multiblock.notExternalFormedBiPred(), multiblock.formedBiPred(),
              chemical -> chemical.is(MekanismChemicals.STEAM), null, listener);
        this.multiblock = multiblock;
    }

    @Override
    public ChemicalStack insert(@NotNull ChemicalStack stack, Action action, AutomationType automationType) {
        ChemicalStack returned = super.insert(stack, action, automationType);
        if (action == Action.EXECUTE && multiblock.isFormed()) {
            multiblock.newSteamInput += stack.getAmount() - returned.getAmount();
        }
        return returned;
    }
}