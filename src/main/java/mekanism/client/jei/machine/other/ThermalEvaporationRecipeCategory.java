package mekanism.client.jei.machine.other;

import java.util.List;
import mekanism.api.annotations.NonNull;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.machine.FluidToFluidRecipeWrapper;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fluids.FluidStack;

public class ThermalEvaporationRecipeCategory<WRAPPER extends FluidToFluidRecipeWrapper> extends BaseRecipeCategory<WRAPPER> {

    public ThermalEvaporationRecipeCategory(IGuiHelper helper) {
        super(helper, "mekanism:gui/nei/GuiThermalEvaporationController.png",
              Recipe.THERMAL_EVAPORATION_PLANT.getJEICategory(), "gui.thermalEvaporationController.short", null, 3, 12, 170, 62);
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        super.drawExtras(minecraft);
        drawTexturedRect(49 - xOffset, 64 - yOffset, 176, 59, 78, 8);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, WRAPPER recipeWrapper, IIngredients ingredients) {
        FluidToFluidRecipe tempRecipe = recipeWrapper.getRecipe();
        IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
        @NonNull List<FluidStack> fluidInputs = tempRecipe.getInput().getRepresentations();
        int max = fluidInputs.stream().mapToInt(input -> input.amount).filter(input -> input >= 0).max().orElse(0);
        fluidStacks.init(0, true, 7 - xOffset, 14 - yOffset, 16, 58, max, false, fluidOverlayLarge);
        fluidStacks.init(1, false, 153 - xOffset, 14 - yOffset, 16, 58, tempRecipe.getOutputRepresentation().amount, false,
              fluidOverlayLarge);
        fluidStacks.set(0, fluidInputs);
        fluidStacks.set(1, tempRecipe.getOutputRepresentation());
    }
}