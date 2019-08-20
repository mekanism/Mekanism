package mekanism.client.jei.machine.other;

import mekanism.api.gas.GasStack;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.MekanismBlock;
import mekanism.common.recipe.machines.SolarNeutronRecipe;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;

public class SolarNeutronRecipeCategory extends BaseRecipeCategory<SolarNeutronRecipe> {

    public SolarNeutronRecipeCategory(IGuiHelper helper) {
        super(helper, "mekanism:gui/nei/solar_neutron_activator.png", MekanismBlock.SOLAR_NEUTRON_ACTIVATOR, null, 3, 12, 170, 70);
    }

    @Override
    public void draw(SolarNeutronRecipe recipe, double mouseX, double mouseY) {
        super.draw(recipe, mouseX, mouseY);
        drawTexturedRect(64 - xOffset, 39 - yOffset, 176, 58, 55, 8);
    }

    @Override
    public Class<? extends SolarNeutronRecipe> getRecipeClass() {
        return SolarNeutronRecipe.class;
    }

    @Override
    public void setIngredients(SolarNeutronRecipe recipe, IIngredients ingredients) {
        ingredients.setInput(MekanismJEI.TYPE_GAS, recipe.getInput().ingredient);
        ingredients.setOutput(MekanismJEI.TYPE_GAS, recipe.getOutput().output);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, SolarNeutronRecipe recipe, IIngredients ingredients) {
        IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
        initGas(gasStacks, 0, true, 26 - xOffset, 14 - yOffset, 16, 58, recipe.recipeInput.ingredient, true);
        initGas(gasStacks, 1, false, 134 - xOffset, 14 - yOffset, 16, 58, recipe.recipeOutput.output, true);
    }
}