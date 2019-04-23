package mekanism.client.jei.machine.chemical;

import mekanism.api.gas.GasStack;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.machines.WasherRecipe;
import mekanism.common.tile.TileEntityChemicalWasher;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiIngredientGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;

public class ChemicalWasherRecipeCategory extends BaseRecipeCategory {

    public ChemicalWasherRecipeCategory(IGuiHelper helper) {
        super(helper, "mekanism:gui/nei/GuiChemicalWasher.png", Recipe.CHEMICAL_WASHER.getJEICategory(),
              "tile.MachineBlock2.ChemicalWasher.name", null, 3, 3, 170, 70);
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        super.drawExtras(minecraft);
        drawTexturedRect(61 - xOffset, 39 - yOffset, 176, 63, 55, 8);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper recipeWrapper, IIngredients ingredients) {
        if (recipeWrapper instanceof ChemicalWasherRecipeWrapper) {
            WasherRecipe tempRecipe = ((ChemicalWasherRecipeWrapper) recipeWrapper).getRecipe();
            IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
            fluidStacks.init(0, true, 6 - xOffset, 5 - yOffset, 16, 58, TileEntityChemicalWasher.WATER_USAGE, false,
                  fluidOverlayLarge);
            fluidStacks.set(0, ingredients.getInputs(VanillaTypes.FLUID).get(0));
            IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
            initGas(gasStacks, 0, true, 27 - xOffset, 14 - yOffset, 16, 58, tempRecipe.getInput().ingredient, true);
            initGas(gasStacks, 1, false, 134 - xOffset, 14 - yOffset, 16, 58, tempRecipe.getOutput().output, true);
        }
    }
}