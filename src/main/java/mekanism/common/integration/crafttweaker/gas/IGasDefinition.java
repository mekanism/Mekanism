package mekanism.common.integration.crafttweaker.gas;

import stanhebben.zenscript.annotations.OperatorType;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenOperator;

@ZenClass("mod.mekanism.gas.IGasDefinition")
public interface IGasDefinition
{
    @ZenOperator(OperatorType.MUL)
    IGasStack asStack(int millibuckets);

    @ZenGetter("NAME")
    String getName();

    @ZenGetter("displayName")
    String getDisplayName();
}
