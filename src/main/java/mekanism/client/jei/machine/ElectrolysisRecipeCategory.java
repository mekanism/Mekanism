package mekanism.client.jei.machine;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
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

public class ElectrolysisRecipeCategory extends BaseRecipeCategory<ElectrolysisRecipe> {

    public ElectrolysisRecipeCategory(IGuiHelper helper) {
        super(helper, MekanismBlocks.ELECTROLYTIC_SEPARATOR, ProgressBar.BI, 4, 9, 167, 62);
    }

    @Override
    protected void addGuiElements() {
        guiElements.add(GuiFluidGauge.getDummy(GuiGauge.Type.STANDARD, this, 5, 10));
        guiElements.add(GuiGasGauge.getDummy(GuiGauge.Type.SMALL, this, 58, 18));
        guiElements.add(GuiGasGauge.getDummy(GuiGauge.Type.SMALL, this, 100, 18));
        guiElements.add(new GuiVerticalPowerBar(this, () -> 1F, 164, 15));
        guiElements.add(new GuiSlot(SlotType.NORMAL, this, 25, 34));
        guiElements.add(new GuiSlot(SlotType.NORMAL, this, 58, 51));
        guiElements.add(new GuiSlot(SlotType.NORMAL, this, 100, 51));
        guiElements.add(new GuiSlot(SlotType.NORMAL, this, 142, 34).with(SlotOverlay.POWER));
        guiElements.add(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return 1;
            }
        }, progressBar, this, 78, 29));
    }

    @Override
    public Class<? extends ElectrolysisRecipe> getRecipeClass() {
        return ElectrolysisRecipe.class;
    }

    @Override
    public void setIngredients(ElectrolysisRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.FLUID, Collections.singletonList(recipe.getInput().getRepresentations()));
        ingredients.setOutputs(MekanismJEI.TYPE_GAS, Arrays.asList(recipe.getLeftGasOutputRepresentation(), recipe.getRightGasOutputRepresentation()));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, ElectrolysisRecipe recipe, IIngredients ingredients) {
        IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
        @NonNull List<FluidStack> fluidInputs = recipe.getInput().getRepresentations();
        int max = fluidInputs.stream().mapToInt(FluidStack::getAmount).filter(input -> input >= 0).max().orElse(0);
        fluidStacks.init(0, true, 2, 2, 16, 58, max, false, fluidOverlayLarge);
        fluidStacks.set(0, fluidInputs);
        IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
        initGas(gasStacks, 0, false, 59 - xOffset, 19 - yOffset, 16, 28, recipe.getLeftGasOutputRepresentation(), true);
        initGas(gasStacks, 1, false, 101 - xOffset, 19 - yOffset, 16, 28, recipe.getRightGasOutputRepresentation(), true);
    }
}