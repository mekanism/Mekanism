package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.ChemicalOxidizerIRecipe;
import mekanism.common.recipe.impl.GasConversionIRecipe;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_ITEM_STACK_TO_GAS)
public abstract class ItemStackToGasRecipeManager extends MekanismRecipeManager<ItemStackToGasRecipe> {

    protected ItemStackToGasRecipeManager(MekanismRecipeType<ItemStackToGasRecipe, ?> recipeType) {
        super(recipeType);
    }

    /**
     * Adds a recipe that an item into a gas.
     * <br>
     * If this is called from the gas conversion recipe manager, this will be a gas conversion recipe and be able to be used in any slots in Mekanism machines that are
     * able to convert items to gases, for example in the Osmium Compressor and a variety of other machines.
     * <br>
     * If this is called from the oxidizing recipe manager, this will be an oxidizing recipe. Chemical Oxidizers can process this recipe type.
     *
     * @param name   Name of the new recipe.
     * @param input  {@link ItemStackIngredient} representing the input of the recipe.
     * @param output {@link ICrTGasStack} representing the output of the recipe.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, ItemStackIngredient input, ICrTGasStack output) {
        addRecipe(makeRecipe(getAndValidateName(name), input, getAndValidateNotEmpty(output)));
    }

    protected abstract ItemStackToGasRecipe makeRecipe(ResourceLocation id, ItemStackIngredient input, GasStack output);

    @Override
    protected ActionAddMekanismRecipe getAction(ItemStackToGasRecipe recipe) {
        return new ActionAddMekanismRecipe(recipe) {
            @Override
            protected String describeOutputs() {
                return CrTUtils.describeOutputs(getRecipe().getOutputDefinitionNew(), CrTGasStack::new);
            }
        };
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_GAS_CONVERSION)
    public static class GasConversionRecipeManager extends ItemStackToGasRecipeManager {

        public static final GasConversionRecipeManager INSTANCE = new GasConversionRecipeManager();

        private GasConversionRecipeManager() {
            super(MekanismRecipeType.GAS_CONVERSION);
        }

        @Override
        protected ItemStackToGasRecipe makeRecipe(ResourceLocation id, ItemStackIngredient input, GasStack output) {
            return new GasConversionIRecipe(id, input, output);
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_OXIDIZING)
    public static class ChemicalOxidizerRecipeManager extends ItemStackToGasRecipeManager {

        public static final ChemicalOxidizerRecipeManager INSTANCE = new ChemicalOxidizerRecipeManager();

        private ChemicalOxidizerRecipeManager() {
            super(MekanismRecipeType.OXIDIZING);
        }

        @Override
        protected ItemStackToGasRecipe makeRecipe(ResourceLocation id, ItemStackIngredient input, GasStack output) {
            return new ChemicalOxidizerIRecipe(id, input, output);
        }
    }
}