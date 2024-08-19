package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredientWithAmount;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ItemStackToChemicalRecipe;
import mekanism.api.recipes.basic.BasicChemicalConversionRecipe;
import mekanism.api.recipes.basic.BasicChemicalOxidizerRecipe;
import mekanism.api.recipes.basic.BasicPigmentExtractingRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_ITEM_STACK_TO_CHEMICAL)
public abstract class ItemStackToChemicalRecipeManager extends MekanismRecipeManager<SingleRecipeInput, ItemStackToChemicalRecipe> {

    protected ItemStackToChemicalRecipeManager(IMekanismRecipeTypeProvider<SingleRecipeInput, ItemStackToChemicalRecipe, ?> recipeType) {
        super(recipeType);
    }

    /**
     * Adds a recipe that an item into a chemical.
     * <br>
     * If this is called from the chemical conversion recipe manager, this will be a chemical conversion recipe and be able to be used in any slots in Mekanism machines
     * that are able to convert items to chemicals, for example in the Osmium Compressor and a variety of other machines.
     * <br>
     * If this is called from the oxidizing recipe manager, this will be an oxidizing recipe. Chemical Oxidizers can process this recipe type.
     * <br>
     * If this is called from the pigment extracting recipe manager, this will be a pigment extracting recipe. Pigment Extractors can process this recipe type.
     *
     * @param name   Name of the new recipe.
     * @param input  {@link IIngredientWithAmount} representing the input of the recipe.
     * @param output Chemical stack representing the output of the recipe.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, IIngredientWithAmount input, ICrTChemicalStack output) {
        addRecipe(name, makeRecipe(input, output));
    }

    /**
     * Creates a recipe that an item into a chemical.
     *
     * @param input  {@link IIngredientWithAmount} representing the input of the recipe.
     * @param output Chemical stack representing the output of the recipe. Will be validated as not empty.
     */
    public final ItemStackToChemicalRecipe makeRecipe(IIngredientWithAmount input, ICrTChemicalStack output) {
        return makeRecipe(input, getAndValidateNotEmpty(output));
    }

    protected abstract ItemStackToChemicalRecipe makeRecipe(IIngredientWithAmount input, ChemicalStack output);

    @Override
    protected String describeOutputs(ItemStackToChemicalRecipe recipe) {
        return CrTUtils.describeOutputs(recipe.getOutputDefinition());
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_CHEMICAL_CONVERSION)
    public static class ChemicalConversionRecipeManager extends ItemStackToChemicalRecipeManager {

        public static final ChemicalConversionRecipeManager INSTANCE = new ChemicalConversionRecipeManager();

        private ChemicalConversionRecipeManager() {
            super(MekanismRecipeType.CHEMICAL_CONVERSION);
        }

        @Override
        protected ItemStackToChemicalRecipe makeRecipe(IIngredientWithAmount input, ChemicalStack output) {
            return new BasicChemicalConversionRecipe(CrTUtils.fromCrT(input), output);
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_OXIDIZING)
    public static class ChemicalOxidizerRecipeManager extends ItemStackToChemicalRecipeManager {

        public static final ChemicalOxidizerRecipeManager INSTANCE = new ChemicalOxidizerRecipeManager();

        private ChemicalOxidizerRecipeManager() {
            super(MekanismRecipeType.OXIDIZING);
        }

        @Override
        protected ItemStackToChemicalRecipe makeRecipe(IIngredientWithAmount input, ChemicalStack output) {
            return new BasicChemicalOxidizerRecipe(CrTUtils.fromCrT(input), output);
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_PIGMENT_EXTRACTING)
    public static class PigmentExtractingRecipeManager extends ItemStackToChemicalRecipeManager {

        public static final PigmentExtractingRecipeManager INSTANCE = new PigmentExtractingRecipeManager();

        private PigmentExtractingRecipeManager() {
            super(MekanismRecipeType.PIGMENT_EXTRACTING);
        }

        @Override
        protected ItemStackToChemicalRecipe makeRecipe(IIngredientWithAmount input, ChemicalStack output) {
            return new BasicPigmentExtractingRecipe(CrTUtils.fromCrT(input), output);
        }
    }
}