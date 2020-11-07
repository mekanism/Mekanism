package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.impl.item.MCItemStackMutable;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.ingredient.CrTChemicalStackIngredient;
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

    @ZenCodeType.Method
    public void addRecipe(String name, CrTChemicalStackIngredient<?, ?, ?> input, IItemStack output) {
        name = validateRecipeName(name);
        //TODO: Validate the output isn't empty
        addRecipe(new ChemicalCrystallizerIRecipe(CrTUtils.rl(name), input.getInternal(), output.getInternal()));
    }

    @Override
    protected ActionAddMekanismRecipe getAction(ChemicalCrystallizerRecipe recipe) {
        return new ActionAddMekanismRecipe(recipe) {
            @Override
            protected String describeOutputs() {
                return CrTUtils.describeOutputs(getRecipe().getOutputDefinition(), MCItemStackMutable::new);
            }
        };
    }
}