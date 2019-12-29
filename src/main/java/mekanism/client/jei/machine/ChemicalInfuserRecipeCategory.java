package mekanism.client.jei.machine;

import java.util.Arrays;
import java.util.Collections;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.registries.MekanismBlocks;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;

public class ChemicalInfuserRecipeCategory extends BaseRecipeCategory<ChemicalInfuserRecipe> {

    public ChemicalInfuserRecipeCategory(IGuiHelper helper) {
        super(helper, "mekanism:gui/nei/chemical_infuser.png", MekanismBlocks.CHEMICAL_INFUSER, null, 3, 3, 170, 80);
    }

    @Override
    public void draw(ChemicalInfuserRecipe recipe, double mouseX, double mouseY) {
        super.draw(recipe, mouseX, mouseY);
        drawTexturedRect(47 - xOffset, 39 - yOffset, 176, 71, 28, 8);
        drawTexturedRect(101 - xOffset, 39 - yOffset, 176, 63, 28, 8);
    }

    @Override
    public Class<? extends ChemicalInfuserRecipe> getRecipeClass() {
        return ChemicalInfuserRecipe.class;
    }

    @Override
    public void setIngredients(ChemicalInfuserRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(MekanismJEI.TYPE_GAS, Arrays.asList(recipe.getLeftInput().getRepresentations(), recipe.getRightInput().getRepresentations()));
        ingredients.setOutputLists(MekanismJEI.TYPE_GAS, Collections.singletonList(recipe.getOutputDefinition()));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, ChemicalInfuserRecipe recipe, IIngredients ingredients) {
        IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
        initGas(gasStacks, 0, true, 26 - xOffset, 14 - yOffset, 16, 58, recipe.getLeftInput().getRepresentations(), true);
        initGas(gasStacks, 1, true, 134 - xOffset, 14 - yOffset, 16, 58, recipe.getRightInput().getRepresentations(), true);
        initGas(gasStacks, 2, false, 80 - xOffset, 5 - yOffset, 16, 58, recipe.getOutputDefinition(), true);
    }
}