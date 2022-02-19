package mekanism.client.jei.machine;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.client.gui.element.bar.GuiHorizontalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.machine.TileEntityChemicalDissolutionChamber;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;

public class ChemicalDissolutionRecipeCategory extends BaseRecipeCategory<ChemicalDissolutionRecipe> {

    private final GuiGauge<?> inputGauge;
    private final GuiGauge<?> outputGauge;
    private final GuiSlot inputSlot;

    public ChemicalDissolutionRecipeCategory(IGuiHelper helper) {
        super(helper, MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER, 3, 3, 170, 79);
        //Note: This previously had a lang key for a shorter string. Though ideally especially due to translations
        // we will eventually instead just make the text scale
        inputGauge = addElement(GuiGasGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 7, 4));
        outputGauge = addElement(GuiGasGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 131, 13));
        inputSlot = addSlot(SlotType.INPUT, 28, 36);
        addSlot(SlotType.EXTRA, 8, 65).with(SlotOverlay.MINUS);
        addSlot(SlotType.OUTPUT, 152, 55).with(SlotOverlay.PLUS);
        addSlot(SlotType.POWER, 152, 14).with(SlotOverlay.POWER);
        addSimpleProgress(ProgressType.LARGE_RIGHT, 64, 40);
        addElement(new GuiHorizontalPowerBar(this, FULL_BAR, 115, 75));
    }

    @Override
    public Class<? extends ChemicalDissolutionRecipe> getRecipeClass() {
        return ChemicalDissolutionRecipe.class;
    }

    @Override
    public void setIngredients(ChemicalDissolutionRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getItemInput().getRepresentations()));
        List<@NonNull GasStack> gasInputs = recipe.getGasInput().getRepresentations();
        List<GasStack> scaledGases = gasInputs.stream().map(gas -> new GasStack(gas, gas.getAmount() * TileEntityChemicalDissolutionChamber.BASE_TICKS_REQUIRED))
              .collect(Collectors.toList());
        ingredients.setInputLists(MekanismJEI.TYPE_GAS, Collections.singletonList(scaledGases));
        BoxedChemicalStack outputDefinition = recipe.getOutputDefinition();
        ChemicalType chemicalType = outputDefinition.getChemicalType();
        if (chemicalType == ChemicalType.GAS) {
            ingredients.setOutput(MekanismJEI.TYPE_GAS, (GasStack) outputDefinition.getChemicalStack());
        } else if (chemicalType == ChemicalType.INFUSION) {
            ingredients.setOutput(MekanismJEI.TYPE_INFUSION, (InfusionStack) outputDefinition.getChemicalStack());
        } else if (chemicalType == ChemicalType.PIGMENT) {
            ingredients.setOutput(MekanismJEI.TYPE_PIGMENT, (PigmentStack) outputDefinition.getChemicalStack());
        } else if (chemicalType == ChemicalType.SLURRY) {
            ingredients.setOutput(MekanismJEI.TYPE_SLURRY, (SlurryStack) outputDefinition.getChemicalStack());
        } else {
            throw new IllegalStateException("Unknown chemical type");
        }
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, ChemicalDissolutionRecipe recipe, IIngredients ingredients) {
        initItem(recipeLayout.getItemStacks(), 0, true, inputSlot, recipe.getItemInput().getRepresentations());
        List<@NonNull GasStack> gasInputs = recipe.getGasInput().getRepresentations();
        List<GasStack> scaledGases = gasInputs.stream().map(gas -> new GasStack(gas, gas.getAmount() * TileEntityChemicalDissolutionChamber.BASE_TICKS_REQUIRED))
              .collect(Collectors.toList());
        IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
        initChemical(gasStacks, 0, true, inputGauge, scaledGases);
        BoxedChemicalStack outputDefinition = recipe.getOutputDefinition();
        ChemicalType chemicalType = outputDefinition.getChemicalType();
        if (chemicalType == ChemicalType.GAS) {
            initChemicalOutput(recipeLayout, MekanismJEI.TYPE_GAS, (GasStack) outputDefinition.getChemicalStack());
        } else if (chemicalType == ChemicalType.INFUSION) {
            initChemicalOutput(recipeLayout, MekanismJEI.TYPE_INFUSION, (InfusionStack) outputDefinition.getChemicalStack());
        } else if (chemicalType == ChemicalType.PIGMENT) {
            initChemicalOutput(recipeLayout, MekanismJEI.TYPE_PIGMENT, (PigmentStack) outputDefinition.getChemicalStack());
        } else if (chemicalType == ChemicalType.SLURRY) {
            initChemicalOutput(recipeLayout, MekanismJEI.TYPE_SLURRY, (SlurryStack) outputDefinition.getChemicalStack());
        } else {
            throw new IllegalStateException("Unknown chemical type");
        }
    }

    private <STACK extends ChemicalStack<?>> void initChemicalOutput(IRecipeLayout recipeLayout, IIngredientType<STACK> type, STACK stack) {
        initChemical(recipeLayout.getIngredientsGroup(type), 1, false, outputGauge, Collections.singletonList(stack));
    }
}