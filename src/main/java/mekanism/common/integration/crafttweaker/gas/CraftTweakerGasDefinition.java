/*package mekanism.common.integration.crafttweaker.gas;

import javax.annotation.Nonnull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;

public class CraftTweakerGasDefinition implements IGasDefinition {

    @Nonnull
    private final Gas gas;

    public CraftTweakerGasDefinition(@Nonnull Gas gas) {
        this.gas = gas;
    }

    @Override
    public IGasStack asStack(int mb) {
        return new CraftTweakerGasStack(new GasStack(gas, mb));
    }

    @Override
    public String getName() {
        return gas.getName();
    }

    @Override
    public String getDisplayName() {
        //TODO
        return gas.getTranslationKey();
    }
}*/