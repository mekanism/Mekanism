package mekanism.client.jei.machine;

import java.util.Collections;
import java.util.List;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.FluidGasToGasRecipe;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.registries.MekanismBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraftforge.fluids.FluidStack;

public class FluidGasToGasRecipeCategory extends BaseRecipeCategory<FluidGasToGasRecipe> {

    public FluidGasToGasRecipeCategory(IGuiHelper helper) {
        super(helper, MekanismBlocks.CHEMICAL_WASHER, 3, 3, 170, 70);
    }

    @Override
    protected void addGuiElements() {
        guiElements.add(GuiFluidGauge.getDummy(GuiGauge.Type.STANDARD, this, 5, 4));
        guiElements.add(GuiGasGauge.getDummy(GuiGauge.Type.STANDARD, this, 26, 13));
        guiElements.add(GuiGasGauge.getDummy(GuiGauge.Type.STANDARD, this, 133, 13));
        guiElements.add(new GuiSlot(SlotType.POWER, this, 154, 4).with(SlotOverlay.POWER));
        guiElements.add(new GuiSlot(SlotType.EXTRA, this, 154, 55).with(SlotOverlay.MINUS));
        guiElements.add(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return 1;
            }
        }, ProgressBar.LARGE_RIGHT, this, 62, 38));
    }

    @Override
    public Class<? extends FluidGasToGasRecipe> getRecipeClass() {
        return FluidGasToGasRecipe.class;
    }

    @Override
    public void setIngredients(FluidGasToGasRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.FLUID, Collections.singletonList(recipe.getFluidInput().getRepresentations()));
        ingredients.setInputLists(MekanismJEI.TYPE_GAS, Collections.singletonList(recipe.getGasInput().getRepresentations()));
        ingredients.setOutput(MekanismJEI.TYPE_GAS, recipe.getOutputRepresentation());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, FluidGasToGasRecipe recipe, IIngredients ingredients) {
        IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
        @NonNull List<FluidStack> fluidInputs = recipe.getFluidInput().getRepresentations();
        int max = fluidInputs.stream().mapToInt(FluidStack::getAmount).filter(input -> input >= 0).max().orElse(0);
        fluidStacks.init(0, true, 6 - xOffset, 5 - yOffset, 16, 58, max, false, fluidOverlayLarge);
        fluidStacks.set(0, fluidInputs);
        IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
        initGas(gasStacks, 0, true, 27 - xOffset, 14 - yOffset, 16, 58, recipe.getGasInput().getRepresentations(), true);
        initGas(gasStacks, 1, false, 134 - xOffset, 14 - yOffset, 16, 58, recipe.getOutputRepresentation(), true);
    }
}