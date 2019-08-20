package mekanism.client.jei.machine.chemical;

import mekanism.api.gas.GasStack;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.MekanismBlock;
import mekanism.common.recipe.machines.OxidationRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;

public class ChemicalOxidizerRecipeCategory extends BaseRecipeCategory<OxidationRecipe> {

    public ChemicalOxidizerRecipeCategory(IGuiHelper helper) {
        super(helper, "mekanism:gui/chemical_oxidizer.png", MekanismBlock.CHEMICAL_OXIDIZER, ProgressBar.LARGE_RIGHT, 20, 12, 132, 62);
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
    public Class<? extends OxidationRecipe> getRecipeClass() {
        return OxidationRecipe.class;
    }

    @Override
    public void setIngredients(OxidationRecipe recipe, IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.ITEM, recipe.recipeInput.ingredient);
        ingredients.setOutput(MekanismJEI.TYPE_GAS, recipe.recipeOutput.output);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, OxidationRecipe recipe, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        itemStacks.init(0, true, 25 - xOffset, 35 - yOffset);
        itemStacks.set(0, recipe.getInput().ingredient);
        IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
        initGas(gasStacks, 0, false, 134 - xOffset, 14 - yOffset, 16, 58, recipe.recipeOutput.output, true);
    }
}