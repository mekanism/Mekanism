package mekanism.common.integration.crafttweaker.gas;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import stanhebben.zenscript.annotations.OperatorType;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenOperator;

@ZenClass("mod.mekanism.gas.IGasStack")
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