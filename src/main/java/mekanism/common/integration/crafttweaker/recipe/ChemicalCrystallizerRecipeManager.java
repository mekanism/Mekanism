package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.item.MCItemStackMutable;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.crafting.IRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_CRYSTALLIZING)
public class ChemicalCrystallizerRecipeManager extends MekanismRecipeManager {

    public static final ChemicalCrystallizerRecipeManager INSTANCE = new ChemicalCrystallizerRecipeManager();

    private ChemicalCrystallizerRecipeManager() {
    }

    @Override
    public IRecipeType<ChemicalCrystallizerRecipe> getRecipeType() {
        return MekanismRecipeType.CRYSTALLIZING;
    }

    private static class ActionAddChemicalCrystallizerRecipe extends ActionAddMekanismRecipe<ChemicalCrystallizerRecipe> {

        protected ActionAddChemicalCrystallizerRecipe(MekanismRecipeManager recipeManager, ChemicalCrystallizerRecipe recipe) {
            super(recipeManager, recipe);
        }

        @Override
        protected String describeOutputs() {
            return CrTUtils.describeOutputs(getRecipe().getOutputDefinition(), MCItemStackMutable::new);
        }
    }
}