package mekanism.client.jei.machine;

import java.util.Arrays;
import java.util.Collections;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.client.gui.element.bar.GuiHorizontalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.config.DataType;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;

public class ChemicalInfuserRecipeCategory extends BaseRecipeCategory<ChemicalInfuserRecipe> {

    private final GuiGauge<?> leftInputGauge;
    private final GuiGauge<?> rightInputGauge;
    private final GuiGauge<?> outputGauge;

    public ChemicalInfuserRecipeCategory(IGuiHelper helper) {
        super(helper, MekanismBlocks.CHEMICAL_INFUSER, 3, 3, 170, 80);
        leftInputGauge = addElement(GuiGasGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT_1), this, 25, 13));
        outputGauge = addElement(GuiGasGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 79, 4));
        rightInputGauge = addElement(GuiGasGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT_2), this, 133, 13));
        addSlot(SlotType.INPUT, 6, 56).with(SlotOverlay.MINUS);
        addSlot(SlotType.INPUT_2, 154, 56).with(SlotOverlay.MINUS);
        addSlot(SlotType.OUTPUT, 80, 65).with(SlotOverlay.PLUS);
        addSlot(SlotType.POWER, 154, 14).with(SlotOverlay.POWER);
        addConstantProgress(ProgressType.SMALL_RIGHT, 47, 39);
        addConstantProgress(ProgressType.SMALL_LEFT, 101, 39);
        addElement(new GuiHorizontalPowerBar(this, FULL_BAR, 115, 75));
    }

    @Override
    public Class<? extends ChemicalInfuserRecipe> getRecipeClass() {
        return ChemicalInfuserRecipe.class;
    }

    @Override
    public void setIngredients(ChemicalInfuserRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(MekanismJEI.TYPE_GAS, Arrays.asList(recipe.getLeftInput().getRepresentations(), recipe.getRightInput().getRepresentations()));
        ingredients.setOutputLists(MekanismJEI.TYPE_GAS, Collections.singletonList(recipe.getOutputDefinition()));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, ChemicalInfuserRecipe recipe, IIngredients ingredients) {
        IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
        initChemical(gasStacks, 0, true, leftInputGauge, recipe.getLeftInput().getRepresentations());
        initChemical(gasStacks, 1, true, rightInputGauge, recipe.getRightInput().getRepresentations());
        initChemical(gasStacks, 2, false, outputGauge, recipe.getOutputDefinition());
    }
}