package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.impl.helper.ItemStackHelper;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.inputs.chemical.IChemicalStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.ChemicalCrystallizerIRecipe;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_CRYSTALLIZING)
public class ChemicalCrystallizerRecipeManager extends MekanismRecipeManager<ChemicalCrystallizerRecipe> {

    public static final ChemicalCrystallizerRecipeManager INSTANCE = new ChemicalCrystallizerRecipeManager();

    private ChemicalCrystallizerRecipeManager() {
        super(MekanismRecipeType.CRYSTALLIZING);
    }

    /**
     * Adds a crystallizing recipe that converts a chemical into an item. Chemical Crystallizers can process this recipe type.
     *
     * @param name   Name of the new recipe.
     * @param input  {@link IChemicalStackIngredient} representing the input of the recipe.
     * @param output {@link IItemStack} representing the output of the recipe.
     */
    @ZenCodeType.Method
    public void addRecipe(String name, IChemicalStackIngredient<?, ?> input, IItemStack output) {
        addRecipe(new ChemicalCrystallizerIRecipe(getAndValidateName(name), input, getAndValidateNotEmpty(output)));
    }

    @Override
    protected ActionAddMekanismRecipe getAction(ChemicalCrystallizerRecipe recipe) {
        return new ActionAddMekanismRecipe(recipe) {
            @Override
            protected String describeOutputs() {
                return CrTUtils.describeOutputs(getRecipe().getOutputDefinition(), ItemStackHelper::getCommandString);
            }
        };
    }
}