package mekanism.client.jei.machine;

import java.util.Collections;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.registries.MekanismBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;

public class ItemStackToGasRecipeCategory extends BaseRecipeCategory<ItemStackToGasRecipe> {

    public ItemStackToGasRecipeCategory(IGuiHelper helper) {
        super(helper, MekanismBlocks.CHEMICAL_OXIDIZER, 20, 12, 132, 62);
    }

    @Override
    protected void addGuiElements() {
        guiElements.add(GuiGasGauge.getDummy(GaugeType.STANDARD, this, 133, 13));
        guiElements.add(new GuiSlot(SlotType.INPUT, this, 25, 35));
        guiElements.add(new GuiProgress(() -> timer.getValue() / 20D, ProgressType.LARGE_RIGHT, this, 64, 40));
    }

    @Override
    public Class<? extends ItemStackToGasRecipe> getRecipeClass() {
        return ItemStackToGasRecipe.class;
    }

    @Override
    public void setIngredients(ItemStackToGasRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getInput().getRepresentations()));
        ingredients.setOutput(MekanismJEI.TYPE_GAS, recipe.getOutputDefinition());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, ItemStackToGasRecipe recipe, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        itemStacks.init(0, true, 25 - xOffset, 35 - yOffset);
        itemStacks.set(0, recipe.getInput().getRepresentations());
        IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
        initGas(gasStacks, 0, false, 134 - xOffset, 14 - yOffset, 16, 58, Collections.singletonList(recipe.getOutputDefinition()), true);
    }
}