package mekanism.common.integration.crafttweaker.handlers;

import mekanism.common.Mekanism;

//@ZenRegister
//@ZenCodeType.Name("mekanism.gas.conversion")
public class GasConversion {

    //Commit that still had things this references in case it will be useful to find stuff quicker
    // https://github.com/mekanism/Mekanism/tree/a911d7d76c9b2b253664b1d21c75b157cdb4ee01
    public static final String NAME = Mekanism.MOD_NAME + " Gas Conversion";

    /*@ZenCodeType.Method
    public static void register(IIngredient ingredient, IGasStack gas) {
        if (IngredientHelper.checkNotNull(NAME, ingredient, gas)) {
            CrafttweakerIntegration.LATE_ADDITIONS.add(new Add(ingredient, gas));
        }
    }

    @ZenCodeType.Method
    public static void unregister(IIngredient ingredient, IGasStack gas) {
        if (IngredientHelper.checkNotNull(NAME, ingredient, gas)) {
            CrafttweakerIntegration.LATE_REMOVALS.add(new Remove(ingredient, gas));
        }
    }

    @ZenCodeType.Method
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
                                                         "to override conversions as unexpected things may occur.", NAME, ingredient.getCommandString()));
            }
        }

        @Override
        public String describe() {
            return "Adding gas conversion between: " + ingredient.getCommandString() + " to " + gas.getCommandString();
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
            CraftTweakerAPI.logInfo("Removing " + count + " gas conversion" + (count == 1 ? "" : "s") + " between: " + ingredient.getCommandString() + " and " + gas.getCommandString());
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
    }*/
}