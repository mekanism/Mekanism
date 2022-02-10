package mekanism.client.jei.machine;

import java.util.Collections;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.client.gui.element.GuiDownArrow;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.registries.MekanismBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;

public class RotaryCondensentratorRecipeCategory extends BaseRecipeCategory<RotaryRecipe> {

    private final boolean condensentrating;
    private final GuiGauge<?> gasGauge;
    private final GuiGauge<?> fluidGauge;

    public RotaryCondensentratorRecipeCategory(IGuiHelper helper, boolean condensentrating) {
        //We override the things that reference the provider
        super(helper, Mekanism.rl("rotary_condensentrator_" + (condensentrating ? "condensentrating" : "decondensentrating")),
              (condensentrating ? MekanismLang.CONDENSENTRATING : MekanismLang.DECONDENSENTRATING).translate(),
              createIcon(helper, MekanismBlocks.ROTARY_CONDENSENTRATOR), 3, 12, 170, 64);
        this.condensentrating = condensentrating;
        addElement(new GuiDownArrow(this, 159, 44));
        gasGauge = addElement(GuiGasGauge.getDummy(GaugeType.STANDARD, this, 25, 13));
        fluidGauge = addElement(GuiFluidGauge.getDummy(GaugeType.STANDARD, this, 133, 13));
        addSlot(SlotType.INPUT, 5, 25).with(SlotOverlay.PLUS);
        addSlot(SlotType.OUTPUT, 5, 56).with(SlotOverlay.MINUS);
        addSlot(SlotType.INPUT, 155, 25);
        addSlot(SlotType.OUTPUT, 155, 56);
        addConstantProgress(this.condensentrating ? ProgressType.LARGE_RIGHT : ProgressType.LARGE_LEFT, 64, 39);
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
                ingredients.setOutputLists(VanillaTypes.FLUID, Collections.singletonList(recipe.getFluidOutputDefinition()));
            }
        } else if (recipe.hasFluidToGas()) {
            ingredients.setInputLists(VanillaTypes.FLUID, Collections.singletonList(recipe.getFluidInput().getRepresentations()));
            ingredients.setOutputLists(MekanismJEI.TYPE_GAS, Collections.singletonList(recipe.getGasOutputDefinition()));
        }
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, RotaryRecipe recipe, IIngredients ingredients) {
        if (condensentrating) {
            if (recipe.hasGasToFluid()) {
                //Setup gas
                initChemical(recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS), 0, true, gasGauge, recipe.getGasInput().getRepresentations());
                //Setup fluid
                initFluid(recipeLayout.getFluidStacks(), 0, false, fluidGauge, recipe.getFluidOutputDefinition());
            }
        } else if (recipe.hasFluidToGas()) {
            //Setup fluid
            initFluid(recipeLayout.getFluidStacks(), 0, true, fluidGauge, recipe.getFluidInput().getRepresentations());
            //Setup gas
            initChemical(recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS), 0, false, gasGauge, recipe.getGasOutputDefinition());
        }
    }
}