package mekanism.client.jei.machine;

import java.util.Collections;
import java.util.List;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.client.gui.element.GuiDownArrow;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraftforge.fluids.FluidStack;

public class RotaryCondensentratorRecipeCategory extends BaseRecipeCategory<RotaryRecipe> {

    private final boolean condensentrating;

    public RotaryCondensentratorRecipeCategory(IGuiHelper helper, boolean condensentrating) {
        //We override the things that reference the provider
        super(helper, Mekanism.rl(condensentrating ? "rotary_condensentrator_condensentrating" : "rotary_condensentrator_decondensentrating"),
              condensentrating ? MekanismLang.CONDENSENTRATING.translate() : MekanismLang.DECONDENSENTRATING.translate(), 3, 12, 170, 71);
        this.condensentrating = condensentrating;
        //Add the progress bar. addGuiElements gets called before condensentrating is set
        guiElements.add(new GuiProgress(() -> 1, condensentrating ? ProgressType.LARGE_RIGHT : ProgressType.LARGE_LEFT, this, 64, 39));
    }

    @Override
    protected void addGuiElements() {
        guiElements.add(new GuiDownArrow(this, 159, 44));
        guiElements.add(GuiGasGauge.getDummy(GaugeType.STANDARD, this, 25, 13));
        guiElements.add(GuiFluidGauge.getDummy(GaugeType.STANDARD, this, 133, 13));
        guiElements.add(new GuiSlot(SlotType.INPUT, this, 4, 24).with(SlotOverlay.PLUS));
        guiElements.add(new GuiSlot(SlotType.OUTPUT, this, 4, 55).with(SlotOverlay.MINUS));
        guiElements.add(new GuiSlot(SlotType.INPUT, this, 154, 24));
        guiElements.add(new GuiSlot(SlotType.OUTPUT, this, 154, 55));
    }

    @Override
    public Class<? extends RotaryRecipe> getRecipeClass() {
        return RotaryRecipe.class;
    }

    @Override
    public void setIngredients(RotaryRecipe recipe, IIngredients ingredients) {
        if (condensentrating) {
            if (recipe.hasGasToFluid()) {
                ingredients.setInputLists(MekanismJEI.TYPE_GAS, Collections.singletonList(recipe.getGasInput().getRepresentations()));
                ingredients.setOutput(VanillaTypes.FLUID, recipe.getFluidOutputRepresentation());
            }
        } else if (recipe.hasFluidToGas()) {
            ingredients.setInputLists(VanillaTypes.FLUID, Collections.singletonList(recipe.getFluidInput().getRepresentations()));
            ingredients.setOutput(MekanismJEI.TYPE_GAS, recipe.getGasOutputRepresentation());
        }
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, RotaryRecipe recipe, IIngredients ingredients) {
        if (condensentrating) {
            if (recipe.hasGasToFluid()) {
                //Setup gas
                IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
                initChemical(gasStacks, 0, true, 26 - xOffset, 14 - yOffset, 16, 58, recipe.getGasInput().getRepresentations(), true);
                //Setup fluid
                IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
                fluidStacks.init(0, true, 134 - xOffset, 14 - yOffset, 16, 58, recipe.getFluidOutputRepresentation().getAmount(), false, fluidOverlayLarge);
                fluidStacks.set(0, recipe.getFluidOutputRepresentation());
            }
        } else if (recipe.hasFluidToGas()) {
            //Setup fluid
            IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
            List<FluidStack> fluidInputs = recipe.getFluidInput().getRepresentations();
            int max = fluidInputs.stream().mapToInt(FluidStack::getAmount).filter(input -> input >= 0).max().orElse(0);
            fluidStacks.init(0, false, 134 - xOffset, 14 - yOffset, 16, 58, max, false, fluidOverlayLarge);
            fluidStacks.set(0, fluidInputs);
            //Setup gas
            IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
            initChemical(gasStacks, 0, false, 26 - xOffset, 14 - yOffset, 16, 58, Collections.singletonList(recipe.getGasOutputRepresentation()), true);
            gasStacks.set(0, recipe.getGasOutputRepresentation());
        }
    }
}