package mekanism.client.jei.machine.chemical;

import mekanism.api.gas.GasStack;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.MekanismBlock;
import mekanism.common.recipe.machines.WasherRecipe;
import mekanism.common.temporary.FluidRegistry;
import mekanism.common.tile.TileEntityChemicalWasher;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraftforge.fluids.FluidStack;

public class ChemicalWasherRecipeCategory extends BaseRecipeCategory<WasherRecipe> {

    public ChemicalWasherRecipeCategory(IGuiHelper helper) {
        super(helper, "mekanism:gui/nei/chemical_washer.png", MekanismBlock.CHEMICAL_WASHER, null, 3, 3, 170, 70);
    }

    @Override
    public void draw(WasherRecipe recipe, double mouseX, double mouseY) {
        super.draw(recipe, mouseX, mouseY);
        drawTexturedRect(61 - xOffset, 39 - yOffset, 176, 63, 55, 8);
    }

    @Override
    public Class<? extends WasherRecipe> getRecipeClass() {
        return WasherRecipe.class;
    }

    @Override
    public void setIngredients(WasherRecipe recipe, IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.FLUID, new FluidStack(FluidRegistry.WATER, TileEntityChemicalWasher.WATER_USAGE));
        ingredients.setInput(MekanismJEI.TYPE_GAS, recipe.recipeInput.ingredient);
        ingredients.setOutput(MekanismJEI.TYPE_GAS, recipe.recipeOutput.output);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, WasherRecipe recipe, IIngredients ingredients) {
        IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
        fluidStacks.init(0, true, 6 - xOffset, 5 - yOffset, 16, 58, TileEntityChemicalWasher.WATER_USAGE, false,
              fluidOverlayLarge);
        fluidStacks.set(0, ingredients.getInputs(VanillaTypes.FLUID).get(0));
        IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
        initGas(gasStacks, 0, true, 27 - xOffset, 14 - yOffset, 16, 58, recipe.getInput().ingredient, true);
        initGas(gasStacks, 1, false, 134 - xOffset, 14 - yOffset, 16, 58, recipe.getOutput().output, true);
    }
}