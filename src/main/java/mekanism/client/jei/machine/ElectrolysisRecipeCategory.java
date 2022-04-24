package mekanism.client.jei.machine;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.ElectrolysisRecipe.ElectrolysisRecipeOutput;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.config.DataType;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;

public class ElectrolysisRecipeCategory extends BaseRecipeCategory<ElectrolysisRecipe> {

    private final GuiGauge<?> input;
    private final GuiGauge<?> leftOutput;
    private final GuiGauge<?> rightOutput;

    public ElectrolysisRecipeCategory(IGuiHelper helper, MekanismJEIRecipeType<ElectrolysisRecipe> recipeType) {
        super(helper, recipeType, MekanismBlocks.ELECTROLYTIC_SEPARATOR, 4, 9, 167, 62);
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
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, ElectrolysisRecipe recipe, @Nonnull IFocusGroup focusGroup) {
        initFluid(builder, RecipeIngredientRole.INPUT, input, recipe.getInput().getRepresentations());
        List<GasStack> leftDefinition = new ArrayList<>();
        List<GasStack> rightDefinition = new ArrayList<>();
        for (ElectrolysisRecipeOutput output : recipe.getOutputDefinition()) {
            leftDefinition.add(output.left());
            rightDefinition.add(output.right());
        }
        initChemical(builder, MekanismJEI.TYPE_GAS, RecipeIngredientRole.OUTPUT, leftOutput, leftDefinition);
        initChemical(builder, MekanismJEI.TYPE_GAS, RecipeIngredientRole.OUTPUT, rightOutput, rightDefinition);
    }
}