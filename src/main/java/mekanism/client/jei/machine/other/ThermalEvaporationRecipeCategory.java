package mekanism.client.jei.machine.other;

import mekanism.client.jei.BaseRecipeCategory;
import mekanism.common.MekanismBlock;
import mekanism.common.recipe.machines.ThermalEvaporationRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;

public class ThermalEvaporationRecipeCategory extends BaseRecipeCategory<ThermalEvaporationRecipe> {

    public ThermalEvaporationRecipeCategory(IGuiHelper helper) {
        super(helper, "mekanism:gui/nei/GuiThermalEvaporationController.png", MekanismBlock.THERMAL_EVAPORATION_CONTROLLER, null, 3, 12, 170, 62);
    }

    @Override
    public void draw(ThermalEvaporationRecipe recipe, double mouseX, double mouseY) {
        super.draw(recipe, mouseX, mouseY);
        drawTexturedRect(49 - xOffset, 64 - yOffset, 176, 59, 78, 8);
    }

    @Override
    public Class<? extends ThermalEvaporationRecipe> getRecipeClass() {
        return ThermalEvaporationRecipe.class;
    }

    @Override
    public void setIngredients(ThermalEvaporationRecipe recipe, IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.FLUID, recipe.getInput().ingredient);
        ingredients.setOutput(VanillaTypes.FLUID, recipe.getOutput().output);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, ThermalEvaporationRecipe recipe, IIngredients ingredients) {
        IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
        fluidStacks.init(0, true, 7 - xOffset, 14 - yOffset, 16, 58, recipe.getInput().ingredient.amount, false,
              fluidOverlayLarge);
        fluidStacks.init(1, false, 153 - xOffset, 14 - yOffset, 16, 58, recipe.getOutput().output.amount, false,
              fluidOverlayLarge);
        fluidStacks.set(0, recipe.recipeInput.ingredient);
        fluidStacks.set(1, recipe.recipeOutput.output);
    }
}