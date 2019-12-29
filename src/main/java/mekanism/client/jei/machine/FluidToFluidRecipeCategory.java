package mekanism.client.jei.machine;

import java.util.Collections;
import java.util.List;
import mekanism.api.annotations.NonNull;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.common.registries.MekanismBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraftforge.fluids.FluidStack;

public class FluidToFluidRecipeCategory extends BaseRecipeCategory<FluidToFluidRecipe> {

    public FluidToFluidRecipeCategory(IGuiHelper helper) {
        super(helper, "mekanism:gui/nei/thermal_evaporation_controller.png", MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER, null, 3, 12, 170, 62);
    }

    @Override
    public void draw(FluidToFluidRecipe recipe, double mouseX, double mouseY) {
        super.draw(recipe, mouseX, mouseY);
        drawTexturedRect(49 - xOffset, 64 - yOffset, 176, 59, 78, 8);
    }

    @Override
    public Class<? extends FluidToFluidRecipe> getRecipeClass() {
        return FluidToFluidRecipe.class;
    }

    @Override
    public void setIngredients(FluidToFluidRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.FLUID, Collections.singletonList(recipe.getInput().getRepresentations()));
        ingredients.setOutput(VanillaTypes.FLUID, recipe.getOutputRepresentation());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, FluidToFluidRecipe recipe, IIngredients ingredients) {
        IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
        @NonNull List<FluidStack> fluidInputs = recipe.getInput().getRepresentations();
        int max = fluidInputs.stream().mapToInt(FluidStack::getAmount).filter(input -> input >= 0).max().orElse(0);
        fluidStacks.init(0, true, 7 - xOffset, 14 - yOffset, 16, 58, max, false, fluidOverlayLarge);
        fluidStacks.init(1, false, 153 - xOffset, 14 - yOffset, 16, 58, recipe.getOutputRepresentation().getAmount(), false,
              fluidOverlayLarge);
        fluidStacks.set(0, fluidInputs);
        fluidStacks.set(1, recipe.getOutputRepresentation());
    }
}