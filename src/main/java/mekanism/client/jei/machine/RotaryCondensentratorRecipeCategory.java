//TODO: Fix this
/*package mekanism.client.jei.machine;

import mekanism.api.gas.GasStack;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.Mekanism;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.util.ResourceLocation;

public class RotaryCondensentratorRecipeCategory extends BaseRecipeCategory<RotaryCondensentratorRecipeWrapper> {

    private final boolean condensentrating;

    public RotaryCondensentratorRecipeCategory(IGuiHelper helper, boolean condensentrating) {
        super(helper, "mekanism:gui/nei/GuiRotaryCondensentrator.png",
              new ResourceLocation(Mekanism.MODID, condensentrating ? "rotary_condensentrator_condensentrating" : "rotary_condensentrator_decondensentrating"),
              condensentrating ? "gui.mekanism.condensentrating" : "gui.mekanism.decondensentrating", null, 3, 12, 170, 71);
        this.condensentrating = condensentrating;
    }

    @Override
    public void draw(RotaryCondensentratorRecipeWrapper recipe, double mouseX, double mouseY) {
        super.draw(recipe, mouseX, mouseY);
        drawTexturedRect(64 - xOffset, 39 - yOffset, 176, condensentrating ? 123 : 115, 48, 8);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, RotaryCondensentratorRecipeWrapper recipeWrapper, IIngredients ingredients) {
        IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
        IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
        fluidStacks.init(0, !condensentrating, 134 - xOffset, 14 - yOffset, 16, 58, RotaryCondensentratorRecipeWrapper.FLUID_AMOUNT, false, fluidOverlayLarge);
        if (condensentrating) {
            initGas(gasStacks, 0, true, 26 - xOffset, 14 - yOffset, 16, 58, new GasStack(recipeWrapper.getGasType(), RotaryCondensentratorRecipeWrapper.GAS_AMOUNT), true);
            fluidStacks.set(0, ingredients.getOutputs(VanillaTypes.FLUID).get(0));
        } else {
            initGas(gasStacks, 0, false, 26 - xOffset, 14 - yOffset, 16, 58, new GasStack(recipeWrapper.getGasType(), RotaryCondensentratorRecipeWrapper.GAS_AMOUNT), true);
            fluidStacks.set(0, ingredients.getInputs(VanillaTypes.FLUID).get(0));
        }
    }
}*/