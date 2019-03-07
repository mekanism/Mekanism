package mekanism.common.integration.crafttweaker.gas;

import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import stanhebben.zenscript.annotations.*;

@ZenClass("mod.mekanism.gas.IGasStack")
@ModOnly("mtlib")
@ZenRegister
public interface IGasStack extends IIngredient {
    @ZenGetter("definition")
    IGasDefinition getDefinition();

    @ZenGetter("NAME")
    String getName();

    @ZenGetter("displayName")
    String getDisplayName();

    @ZenGetter("amount")
    int getAmount();

    @ZenOperator(OperatorType.MUL)
    @ZenMethod
    IGasStack withAmount(int amount);

    Object getInternal();
}