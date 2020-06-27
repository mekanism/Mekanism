package mekanism.client.jei.machine;

import java.util.Collections;
import java.util.List;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.FluidSlurryToSlurryRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
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

public class FluidSlurryToSlurryRecipeCategory extends BaseRecipeCategory<FluidSlurryToSlurryRecipe> {

    public FluidSlurryToSlurryRecipeCategory(IGuiHelper helper) {
        super(helper, MekanismBlocks.CHEMICAL_WASHER, 3, 3, 170, 70);
    }

    @Override
    protected void addGuiElements() {
        guiElements.add(GuiFluidGauge.getDummy(GaugeType.STANDARD, this, 7, 13));
        guiElements.add(GuiGasGauge.getDummy(GaugeType.STANDARD, this, 28, 13));
        guiElements.add(GuiGasGauge.getDummy(GaugeType.STANDARD, this, 131, 13));
        guiElements.add(new GuiSlot(SlotType.POWER, this, 151, 4).with(SlotOverlay.POWER));
        guiElements.add(new GuiSlot(SlotType.EXTRA, this, 151, 55).with(SlotOverlay.MINUS));
        guiElements.add(new GuiProgress(() -> 1, ProgressType.LARGE_RIGHT, this, 64, 39));
    }

    @Override
    public Class<? extends FluidSlurryToSlurryRecipe> getRecipeClass() {
        return FluidSlurryToSlurryRecipe.class;
    }

    @Override
    public void setIngredients(FluidSlurryToSlurryRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.FLUID, Collections.singletonList(recipe.getFluidInput().getRepresentations()));
        ingredients.setInputLists(MekanismJEI.TYPE_SLURRY, Collections.singletonList(recipe.getChemicalInput().getRepresentations()));
        ingredients.setOutput(MekanismJEI.TYPE_SLURRY, recipe.getOutputRepresentation());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, FluidSlurryToSlurryRecipe recipe, IIngredients ingredients) {
        IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
        List<FluidStack> fluidInputs = recipe.getFluidInput().getRepresentations();
        int max = fluidInputs.stream().mapToInt(FluidStack::getAmount).filter(input -> input >= 0).max().orElse(0);
        fluidStacks.init(0, true, 8 - xOffset, 14 - yOffset, 16, 58, max, false, fluidOverlayLarge);
        fluidStacks.set(0, fluidInputs);
        IGuiIngredientGroup<SlurryStack> slurryStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_SLURRY);
        initChemical(slurryStacks, 0, true, 29 - xOffset, 14 - yOffset, 16, 58, recipe.getChemicalInput().getRepresentations(), true);
        initChemical(slurryStacks, 1, false, 132 - xOffset, 14 - yOffset, 16, 58, Collections.singletonList(recipe.getOutputRepresentation()), true);
    }
}