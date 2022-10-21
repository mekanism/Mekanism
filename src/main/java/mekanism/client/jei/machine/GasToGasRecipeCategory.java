package mekanism.client.jei.machine;

import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.tile.component.config.DataType;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import org.jetbrains.annotations.NotNull;

public class GasToGasRecipeCategory extends BaseRecipeCategory<GasToGasRecipe> {

    private final GuiGauge<?> input;
    private final GuiGauge<?> output;

    public GasToGasRecipeCategory(IGuiHelper helper, MekanismJEIRecipeType<GasToGasRecipe> recipeType, IBlockProvider mekanismBlock) {
        super(helper, recipeType, mekanismBlock, 4, 13, 168, 60);
        addSlot(SlotType.INPUT, 5, 56).with(SlotOverlay.MINUS);
        addSlot(SlotType.OUTPUT, 155, 56).with(SlotOverlay.PLUS);
        input = addElement(GuiGasGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 25, 13));
        output = addElement(GuiGasGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 133, 13));
        addConstantProgress(ProgressType.LARGE_RIGHT, 64, 39);
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, GasToGasRecipe recipe, @NotNull IFocusGroup focusGroup) {
        initChemical(builder, MekanismJEI.TYPE_GAS, RecipeIngredientRole.INPUT, input, recipe.getInput().getRepresentations());
        initChemical(builder, MekanismJEI.TYPE_GAS, RecipeIngredientRole.OUTPUT, output, recipe.getOutputDefinition());
    }
}