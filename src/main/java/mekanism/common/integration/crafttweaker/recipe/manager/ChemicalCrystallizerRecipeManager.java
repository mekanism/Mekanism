package mekanism.common.integration.crafttweaker.recipe.manager;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.util.ItemStackUtil;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.basic.BasicChemicalCrystallizerRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.vanilla_input.SingleChemicalRecipeInput;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.recipe.MekanismRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_MANAGER_CRYSTALLIZING)
public class ChemicalCrystallizerRecipeManager extends MekanismRecipeManager<SingleChemicalRecipeInput, ChemicalCrystallizerRecipe> {

    public static final ChemicalCrystallizerRecipeManager INSTANCE = new ChemicalCrystallizerRecipeManager();

    private ChemicalCrystallizerRecipeManager() {
        super(MekanismRecipeType.CRYSTALLIZING);
    }

    /**
     * Adds a crystallizing recipe that converts a chemical into an item. Chemical Crystallizers can process this recipe type.
     *
     * @param name   Name of the new recipe.
     * @param input  {@link ChemicalStackIngredient} representing the input of the recipe.
     * @param output {@link IItemStack} representing the output of the recipe.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, ChemicalStackIngredient input, IItemStack output) {
        addRecipe(name, makeRecipe(input, output));
    }

    /**
     * Creates a crystallizing recipe that converts a chemical into an item.
     *
     * @param input  {@link ChemicalStackIngredient} representing the input of the recipe.
     * @param output {@link IItemStack} representing the output of the recipe. Will be validated as not empty.
     */
    public final BasicChemicalCrystallizerRecipe makeRecipe(ChemicalStackIngredient input, IItemStack output) {
        return new BasicChemicalCrystallizerRecipe(input, getAndValidateNotEmpty(output));
    }

    @Override
    protected String describeOutputs(ChemicalCrystallizerRecipe recipe) {
        return CrTUtils.describeOutputs(recipe.getOutputDefinition(), ItemStackUtil::getCommandString);
    }
}