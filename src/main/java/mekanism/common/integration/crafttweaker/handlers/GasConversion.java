package mekanism.common.integration.crafttweaker.handlers;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import mekanism.common.Mekanism;
import mekanism.common.integration.crafttweaker.CrafttweakerIntegration;
import mekanism.common.integration.crafttweaker.gas.IGasStack;
import mekanism.common.integration.crafttweaker.helpers.GasHelper;
import mekanism.common.integration.crafttweaker.helpers.IngredientHelper;
import mekanism.common.recipe.GasConversionHandler;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.mekanism.GasConversion")
@ZenRegister
public class GasConversion {

    public static final String NAME = Mekanism.MOD_NAME + " Gas Conversion";

    @ZenMethod
    public static void register(IIngredient ingredient, IGasStack gas) {
        if (IngredientHelper.checkNotNull(NAME, ingredient, gas)) {
            CrafttweakerIntegration.LATE_ADDITIONS.add(new Add(ingredient, gas));
        }
    }

    @ZenMethod
    public static void unregister(IIngredient ingredient, IGasStack gas) {
        if (IngredientHelper.checkNotNull(NAME, ingredient, gas)) {
            CrafttweakerIntegration.LATE_REMOVALS.add(new Remove(ingredient, gas));
        }
    }

    @ZenMethod
    public static void unregisterAll() {
        CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveAll());
    }

    private static class Add implements IAction {

        private final IIngredient ingredient;
        private final IGasStack gas;

        private Add(IIngredient ingredient, IGasStack gas) {
            this.ingredient = ingredient;
            this.gas = gas;
        }

        @Override
        public void apply() {
            boolean noOverride = GasConversionHandler.addGasMapping(IngredientHelper.getMekanismIngredient(ingredient), GasHelper.toGas(gas));
            if (!noOverride) {
                CraftTweakerAPI.logWarning(String.format("%s: %s overrides another conversion. It is recommended to manually call unregisterGasItem and then registerGasItem " +
                                                         "to override conversions as unexpected things may occur.", NAME, ingredient.toCommandString()));
            }
        }

        @Override
        public String describe() {
            return "Adding gas conversion between: " + ingredient.toCommandString() + " to " + gas.toCommandString();
        }
    }

    private static class Remove implements IAction {

        private final IIngredient ingredient;
        private final IGasStack gas;

        private Remove(IIngredient ingredient, IGasStack gas) {
            this.ingredient = ingredient;
            this.gas = gas;
        }

        @Override
        public void apply() {
            int count = GasConversionHandler.removeGasMapping(IngredientHelper.getMekanismIngredient(ingredient), GasHelper.toGas(gas));
            CraftTweakerAPI.logInfo("Removing " + count + " gas conversion" + (count == 1 ? "" : "s") + " between: " + ingredient.toCommandString() + " and " + gas.toCommandString());
        }

        @Override
        public String describe() {
            //Returns null as we handle it manually when calling apply
            return null;
        }
    }

    private static class RemoveAll implements IAction {

        @Override
        public void apply() {
            GasConversionHandler.removeAllGasMappings();
        }

        @Override
        public String describe() {
            return "Removing all gas conversions.";
        }
    }
}