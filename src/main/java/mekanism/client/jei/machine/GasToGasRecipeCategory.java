package mekanism.client.jei.machine;

import java.util.Collections;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.tile.component.config.DataType;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;

public class GasToGasRecipeCategory extends BaseRecipeCategory<GasToGasRecipe> {

    private final GuiGauge<?> input;
    private final GuiGauge<?> output;

    public GasToGasRecipeCategory(IGuiHelper helper, IBlockProvider mekanismBlock) {
        super(helper, mekanismBlock, 4, 13, 168, 60);
        addSlot(SlotType.INPUT, 5, 56).with(SlotOverlay.MINUS);
        addSlot(SlotType.OUTPUT, 155, 56).with(SlotOverlay.PLUS);
        input = addElement(GuiGasGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 25, 13));
        output = addElement(GuiGasGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 133, 13));
        addConstantProgress(ProgressType.LARGE_RIGHT, 64, 39);
    }

    @Override
    public Class<? extends GasToGasRecipe> getRecipeClass() {
        return GasToGasRecipe.class;
    }

    @Override
    public void setIngredients(GasToGasRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(MekanismJEI.TYPE_GAS, Collections.singletonList(recipe.getInput().getRepresentations()));
        ingredients.setOutputLists(MekanismJEI.TYPE_GAS, Collections.singletonList(recipe.getOutputDefinition()));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, GasToGasRecipe recipe, IIngredients ingredients) {
        IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
        initChemical(gasStacks, 0, true, input, recipe.getInput().getRepresentations());
        initChemical(gasStacks, 1, false, output, recipe.getOutputDefinition());
    }
}