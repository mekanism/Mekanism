package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredientWithAmount;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.Unsigned;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.api.recipes.basic.BasicItemStackToEnergyRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_ITEM_STACK_TO_ENERGY)
public abstract class ItemStackToEnergyRecipeManager extends MekanismRecipeManager<ItemStackToEnergyRecipe> {

    protected ItemStackToEnergyRecipeManager(IMekanismRecipeTypeProvider<?, ItemStackToEnergyRecipe, ?> recipeType) {
        super(recipeType);
    }

    /**
     * Adds a recipe that an item into energy.
     * <br>
     * If this is called from the energy conversion recipe manager, this will be an energy conversion recipe and be able to be used in energy slots in Mekanism machines
     * to convert items to energy.
     *
     * @param name   Name of the new recipe.
     * @param input  {@link IIngredientWithAmount} representing the input of the recipe.
     * @param output Energy output, must be greater than zero.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, IIngredientWithAmount input, @Unsigned long output) {
        addRecipe(name, makeRecipe(input, output));
    }

    /**
     * Creates a recipe that an item into energy.
     *
     * @param input  {@link IIngredientWithAmount} representing the input of the recipe.
     * @param output Energy output. Will be validated as being greater than zero.
     */
    public final ItemStackToEnergyRecipe makeRecipe(IIngredientWithAmount input, @Unsigned long output) {
        if (output.isZero()) {
            throw new IllegalArgumentException("Output must be greater than zero.");
        }
        return makeRecipeInternal(input, output.copyAsConst());
    }

    protected abstract ItemStackToEnergyRecipe makeRecipeInternal(IIngredientWithAmount input, @Unsigned long output);

    @Override
    protected String describeOutputs(ItemStackToEnergyRecipe recipe) {
        return CrTUtils.describeOutputs(recipe.getOutputDefinition(), fl -> fl);
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_ENERGY_CONVERSION)
    public static class EnergyConversionRecipeManager extends ItemStackToEnergyRecipeManager {

        public static final EnergyConversionRecipeManager INSTANCE = new EnergyConversionRecipeManager();

        private EnergyConversionRecipeManager() {
            super(MekanismRecipeType.ENERGY_CONVERSION);
        }

        @Override
        protected ItemStackToEnergyRecipe makeRecipeInternal(IIngredientWithAmount input, @Unsigned long output) {
            return new BasicItemStackToEnergyRecipe(CrTUtils.fromCrT(input), output);
        }
    }
}