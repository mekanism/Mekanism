package mekanism.client.jei.machine;

import java.util.Collections;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.registries.MekanismBlocks;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;

public class GasToGasRecipeCategory extends BaseRecipeCategory<GasToGasRecipe> {

    public GasToGasRecipeCategory(IGuiHelper helper) {
        super(helper, "mekanism:gui/nei/solar_neutron_activator.png", MekanismBlocks.SOLAR_NEUTRON_ACTIVATOR, null, 3, 12, 170, 70);
    }

    @Override
    public void draw(GasToGasRecipe recipe, double mouseX, double mouseY) {
        super.draw(recipe, mouseX, mouseY);
        drawTexturedRect(64 - xOffset, 39 - yOffset, 176, 58, 55, 8);
    }

    @Override
    public Class<? extends GasToGasRecipe> getRecipeClass() {
        return GasToGasRecipe.class;
    }

    @Override
    public void setIngredients(GasToGasRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(MekanismJEI.TYPE_GAS, Collections.singletonList(recipe.getInput().getRepresentations()));
        ingredients.setOutput(MekanismJEI.TYPE_GAS, recipe.getOutputRepresentation());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, GasToGasRecipe recipe, IIngredients ingredients) {
        IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
        initGas(gasStacks, 0, true, 26 - xOffset, 14 - yOffset, 16, 58, recipe.getInput().getRepresentations(), true);
        initGas(gasStacks, 1, false, 134 - xOffset, 14 - yOffset, 16, 58, recipe.getOutputRepresentation(), true);
    }
}