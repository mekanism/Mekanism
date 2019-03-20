package mekanism.client.jei.machine.other;

import mekanism.api.gas.GasStack;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiIngredientGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;

public class RotaryCondensentratorRecipeCategory extends BaseRecipeCategory {

    private final IDrawable background;
    private final boolean condensentrating;

    public RotaryCondensentratorRecipeCategory(IGuiHelper helper, boolean condensentrating) {
        super(helper, "mekanism:gui/nei/GuiRotaryCondensentrator.png",
              condensentrating ? "rotary_condensentrator_condensentrating"
                    : "rotary_condensentrator_decondensentrating",
              condensentrating ? "gui.condensentrating" : "gui.decondensentrating", null);
        xOffset = 3;
        yOffset = 12;
        background = guiHelper.createDrawable(guiLocation, xOffset, yOffset, 170, 71);
        this.condensentrating = condensentrating;
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        super.drawExtras(minecraft);
        if (condensentrating) {
            drawTexturedRect(64 - xOffset, 39 - yOffset, 176, 123, 48, 8);
        } else {
            drawTexturedRect(64 - xOffset, 39 - yOffset, 176, 115, 48, 8);
        }
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper recipeWrapper, IIngredients ingredients) {
        if (recipeWrapper instanceof RotaryCondensentratorRecipeWrapper) {
            RotaryCondensentratorRecipeWrapper tempRecipe = (RotaryCondensentratorRecipeWrapper) recipeWrapper;
            IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
            IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
            fluidStacks
                  .init(0, !tempRecipe.condensentrating, 134 - xOffset, 14 - yOffset, 16, 58, tempRecipe.fluidAmount,
                        false, fluidOverlayLarge);
            if (tempRecipe.condensentrating) {
                initGas(gasStacks, 0, true, 26 - xOffset, 14 - yOffset, 16, 58,
                      new GasStack(tempRecipe.gasType, tempRecipe.gasAmount), true);
                fluidStacks.set(0, ingredients.getOutputs(VanillaTypes.FLUID).get(0));
            } else {
                initGas(gasStacks, 0, false, 26 - xOffset, 14 - yOffset, 16, 58,
                      new GasStack(tempRecipe.gasType, tempRecipe.gasAmount), true);
                fluidStacks.set(0, ingredients.getInputs(VanillaTypes.FLUID).get(0));
            }
        }
    }
}