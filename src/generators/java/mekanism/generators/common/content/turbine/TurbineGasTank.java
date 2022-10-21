package mekanism.generators.common.content.turbine;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.common.capabilities.chemical.multiblock.MultiblockChemicalTankBuilder.MultiblockGasTank;
import mekanism.common.registries.MekanismGases;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class TurbineGasTank extends MultiblockGasTank {

    private final TurbineMultiblockData multiblock;

    public TurbineGasTank(TurbineMultiblockData multiblock, @Nullable IContentsListener listener) {
        super(multiblock::getSteamCapacity, multiblock.notExternalFormedBiPred(), multiblock.formedBiPred(), gas -> gas == MekanismGases.STEAM.getChemical(),
              null, listener);
        this.multiblock = multiblock;
    }

    @Override
    public GasStack insert(@NotNull GasStack stack, Action action, AutomationType automationType) {
        GasStack returned = super.insert(stack, action, automationType);
        if (action == Action.EXECUTE && multiblock.isFormed()) {
            multiblock.newSteamInput += stack.getAmount() - returned.getAmount();
        }
        return returned;
    }
}