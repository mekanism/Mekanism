package mekanism.client.jei.machine.chemical;

import java.util.List;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.FluidGasToGasRecipe;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.client.jei.machine.FluidGasToGasRecipeWrapper;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiIngredientGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fluids.FluidStack;

public class ChemicalWasherRecipeCategory extends BaseRecipeCategory<FluidGasToGasRecipeWrapper> {

    public ChemicalWasherRecipeCategory(IGuiHelper helper) {
        super(helper, "mekanism:gui/nei/GuiChemicalWasher.png", Recipe.CHEMICAL_WASHER.getJEICategory(),
              "tile.MachineBlock2.ChemicalWasher.name", null, 3, 3, 170, 70);
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        super.drawExtras(minecraft);
        drawTexturedRect(61 - xOffset, 39 - yOffset, 176, 63, 55, 8);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, FluidGasToGasRecipeWrapper recipeWrapper, IIngredients ingredients) {
        FluidGasToGasRecipe tempRecipe = recipeWrapper.getRecipe();
        IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
        @NonNull List<FluidStack> fluidInputs = tempRecipe.getFluidInput().getRepresentations();
        int max = fluidInputs.stream().mapToInt(input -> input.amount).filter(input -> input >= 0).max().orElse(0);
        fluidStacks.init(0, true, 6 - xOffset, 5 - yOffset, 16, 58, max, false, fluidOverlayLarge);
        fluidStacks.set(0, fluidInputs);
        IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
        initGas(gasStacks, 0, true, 27 - xOffset, 14 - yOffset, 16, 58, tempRecipe.getGasInput().getRepresentations(), true);
        initGas(gasStacks, 1, false, 134 - xOffset, 14 - yOffset, 16, 58, tempRecipe.getOutputRepresentation(), true);
    }
}