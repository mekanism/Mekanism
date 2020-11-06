package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.crafting.IRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_ITEM_STACK_TO_ENERGY)
public abstract class ItemStackToEnergyRecipeManager extends MekanismRecipeManager {

    protected ItemStackToEnergyRecipeManager() {
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_ENERGY_CONVERSION)
    public static class EnergyConversionRecipeManager extends ItemStackToEnergyRecipeManager {

        public static final EnergyConversionRecipeManager INSTANCE = new EnergyConversionRecipeManager();

        private EnergyConversionRecipeManager() {
        }

        @Override
        public IRecipeType<ItemStackToEnergyRecipe> getRecipeType() {
            return MekanismRecipeType.ENERGY_CONVERSION;
        }
    }

    private static class ActionAddItemStackToEnergyRecipe extends ActionAddMekanismRecipe<ItemStackToEnergyRecipe> {

        protected ActionAddItemStackToEnergyRecipe(MekanismRecipeManager recipeManager, ItemStackToEnergyRecipe recipe) {
            super(recipeManager, recipe);
        }

        @Override
        protected String describeOutputs() {
            //TODO: Figure out how we want to represent floating longs in CrT
            return getRecipe().getOutputDefinition().toString();
        }
    }
}