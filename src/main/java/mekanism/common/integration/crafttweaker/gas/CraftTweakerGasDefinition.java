package mekanism.common.integration.crafttweaker.gas;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;

public class CraftTweakerGasDefinition implements IGasDefinition
{
    private final Gas gas;

    public CraftTweakerGasDefinition(Gas gas)
    {
        this.gas = gas;
    }

    @Override
    public IGasStack asStack(int millibuckets)
    {
        return new CraftTweakerGasStack(new GasStack(gas, millibuckets));
    }

    @Override
    public String getName()
    {
        return gas.getName();
    }

    @Override
    public String getDisplayName()
    {
        return gas.getLocalizedName();
    }
}
