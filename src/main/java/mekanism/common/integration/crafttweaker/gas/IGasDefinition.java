package mekanism.common.integration.crafttweaker.gas;

import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.OperatorType;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenOperator;

@ZenClass("mod.mekanism.gas.IGasDefinition")
@ZenRegister
public interface IGasDefinition {

    @ZenOperator(OperatorType.MUL)
    IGasStack asStack(int mb);

    @ZenGetter("NAME")
    String getName();

    @ZenGetter("displayName")
    String getDisplayName();
}