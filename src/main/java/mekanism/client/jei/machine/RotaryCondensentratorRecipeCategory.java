package mekanism.client.jei.machine;

import javax.annotation.Nonnull;
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
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.registries.MekanismBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;

public class RotaryCondensentratorRecipeCategory extends BaseRecipeCategory<RotaryRecipe> {

    private final boolean condensentrating;
    private final GuiGauge<?> gasGauge;
    private final GuiGauge<?> fluidGauge;

    public RotaryCondensentratorRecipeCategory(IGuiHelper helper, boolean condensentrating) {
        //We override the things that reference the provider
        super(helper, condensentrating ? MekanismJEIRecipeType.CONDENSENTRATING : MekanismJEIRecipeType.DECONDENSENTRATING,
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
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, RotaryRecipe recipe, @Nonnull IFocusGroup focusGroup) {
        if (condensentrating) {
            if (recipe.hasGasToFluid()) {
                initChemical(builder, MekanismJEI.TYPE_GAS, RecipeIngredientRole.INPUT, gasGauge, recipe.getGasInput().getRepresentations());
                initFluid(builder, RecipeIngredientRole.OUTPUT, fluidGauge, recipe.getFluidOutputDefinition());
            }
        } else if (recipe.hasFluidToGas()) {
            initFluid(builder, RecipeIngredientRole.INPUT, fluidGauge, recipe.getFluidInput().getRepresentations());
            initChemical(builder, MekanismJEI.TYPE_GAS, RecipeIngredientRole.OUTPUT, gasGauge, recipe.getGasOutputDefinition());
        }
    }
}