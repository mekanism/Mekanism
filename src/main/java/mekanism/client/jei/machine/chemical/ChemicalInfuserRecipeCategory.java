package mekanism.client.jei.machine.chemical;

import mekanism.api.gas.GasStack;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.recipe.machines.ChemicalInfuserRecipe;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IGuiIngredientGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;

public class ChemicalInfuserRecipeCategory extends BaseRecipeCategory {

    public ChemicalInfuserRecipeCategory(IGuiHelper helper) {
        super(helper, "mekanism:gui/nei/GuiChemicalInfuser.png", "chemical_infuser",
              "tile.MachineBlock2.ChemicalInfuser.name", null, 3, 3, 170, 80);
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        super.drawExtras(minecraft);
        drawTexturedRect(47 - xOffset, 39 - yOffset, 176, 71, 28, 8);
        drawTexturedRect(101 - xOffset, 39 - yOffset, 176, 63, 28, 8);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper recipeWrapper, IIngredients ingredients) {
        if (recipeWrapper instanceof ChemicalInfuserRecipeWrapper) {
            ChemicalInfuserRecipe tempRecipe = ((ChemicalInfuserRecipeWrapper) recipeWrapper).getRecipe();
            IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
            initGas(gasStacks, 0, true, 26 - xOffset, 14 - yOffset, 16, 58, tempRecipe.getInput().leftGas, true);
            initGas(gasStacks, 1, true, 134 - xOffset, 14 - yOffset, 16, 58, tempRecipe.getInput().rightGas, true);
            initGas(gasStacks, 2, false, 80 - xOffset, 5 - yOffset, 16, 58, tempRecipe.getOutput().output, true);
        }
    }
}
