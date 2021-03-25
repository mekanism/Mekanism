package mekanism.client.jei.machine;

import java.util.Arrays;
import java.util.Collections;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.config.DataType;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;

public class ElectrolysisRecipeCategory extends BaseRecipeCategory<ElectrolysisRecipe> {

    private final GuiGauge<?> input;
    private final GuiGauge<?> leftOutput;
    private final GuiGauge<?> rightOutput;

    public ElectrolysisRecipeCategory(IGuiHelper helper) {
        super(helper, MekanismBlocks.ELECTROLYTIC_SEPARATOR, 4, 9, 167, 62);
        input = addElement(GuiFluidGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 5, 10));
        leftOutput = addElement(GuiGasGauge.getDummy(GaugeType.SMALL.with(DataType.OUTPUT_1), this, 58, 18));
        rightOutput = addElement(GuiGasGauge.getDummy(GaugeType.SMALL.with(DataType.OUTPUT_2), this, 100, 18));
        addElement(new GuiVerticalPowerBar(this, FULL_BAR, 164, 15));
        addSlot(SlotType.INPUT, 26, 35);
        addSlot(SlotType.OUTPUT, 59, 52);
        addSlot(SlotType.OUTPUT_2, 101, 52);
        addSlot(SlotType.POWER, 143, 35).with(SlotOverlay.POWER);
        addConstantProgress(ProgressType.BI, 80, 30);
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
        initFluid(recipeLayout.getFluidStacks(), 0, true, input, recipe.getInput().getRepresentations());
        IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
        initChemical(gasStacks, 0, false, leftOutput, Collections.singletonList(recipe.getLeftGasOutputRepresentation()));
        initChemical(gasStacks, 1, false, rightOutput, Collections.singletonList(recipe.getRightGasOutputRepresentation()));
    }
}