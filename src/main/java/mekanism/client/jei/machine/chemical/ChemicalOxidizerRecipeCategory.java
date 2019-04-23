package mekanism.client.jei.machine.chemical;

import mekanism.api.gas.GasStack;
import mekanism.client.gui.element.GuiGasGauge;
import mekanism.client.gui.element.GuiGauge;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.machines.OxidationRecipe;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IGuiIngredientGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;

public class ChemicalOxidizerRecipeCategory extends BaseRecipeCategory {

    public ChemicalOxidizerRecipeCategory(IGuiHelper helper) {
        super(helper, "mekanism:gui/GuiChemicalOxidizer.png", Recipe.CHEMICAL_OXIDIZER.getJEICategory(),
              "tile.MachineBlock2.ChemicalOxidizer.name", ProgressBar.LARGE_RIGHT, 20, 12, 132, 62);
    }

    @Override
    protected void addGuiElements() {
        guiElements.add(GuiGasGauge.getDummy(GuiGauge.Type.STANDARD, this, guiLocation, 133, 13));
        guiElements.add(new GuiSlot(SlotType.NORMAL, this, guiLocation, 25, 35));
        guiElements.add(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return (double) timer.getValue() / 20F;
            }
        }, progressBar, this, guiLocation, 62, 39));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper recipeWrapper, IIngredients ingredients) {
        if (recipeWrapper instanceof ChemicalOxidizerRecipeWrapper) {
            OxidationRecipe tempRecipe = ((ChemicalOxidizerRecipeWrapper) recipeWrapper).getRecipe();
            IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
            itemStacks.init(0, true, 25 - xOffset, 35 - yOffset);
            itemStacks.set(0, tempRecipe.getInput().ingredient);
            IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
            initGas(gasStacks, 0, false, 134 - xOffset, 14 - yOffset, 16, 58, tempRecipe.recipeOutput.output, true);
        }
    }
}