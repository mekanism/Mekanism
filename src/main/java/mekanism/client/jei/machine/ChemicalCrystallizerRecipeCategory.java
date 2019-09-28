package mekanism.client.jei.machine;

import java.util.Collections;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.GasToItemStackRecipe;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.MekanismBlock;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;

public class ChemicalCrystallizerRecipeCategory extends BaseRecipeCategory<GasToItemStackRecipe> {

    public ChemicalCrystallizerRecipeCategory(IGuiHelper helper) {
        super(helper, "mekanism:gui/nei/chemical_crystallizer.png", MekanismBlock.CHEMICAL_CRYSTALLIZER, null, 5, 3, 147, 79);
    }

    @Override
    public void draw(GasToItemStackRecipe recipe, double mouseX, double mouseY) {
        super.draw(recipe, mouseX, mouseY);
        drawTexturedRect(53 - xOffset, 61 - yOffset, 176, 63, (int) (48 * ((float) timer.getValue() / 20F)), 8);
    }

    @Override
    public Class<? extends GasToItemStackRecipe> getRecipeClass() {
        return GasToItemStackRecipe.class;
    }

    @Override
    public void setIngredients(GasToItemStackRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(MekanismJEI.TYPE_GAS, Collections.singletonList(recipe.getInput().getRepresentations()));
        ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getOutputDefinition()));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, GasToItemStackRecipe recipe, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        itemStacks.init(0, false, 130 - xOffset, 56 - yOffset);
        itemStacks.set(0, recipe.getOutputDefinition());
        IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
        initGas(gasStacks, 0, true, 6 - xOffset, 5 - yOffset, 16, 58, recipe.getInput().getRepresentations(), true);
    }
}