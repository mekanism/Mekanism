package mekanism.client.jei.machine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.merged.BoxedChemicalStack;
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
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.machine.TileEntityChemicalDissolutionChamber;
import mekanism.common.util.ChemicalUtil;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;

public class ChemicalDissolutionRecipeCategory extends BaseRecipeCategory<ChemicalDissolutionRecipe> {

    private final GuiGauge<?> inputGauge;
    private final GuiGauge<?> outputGauge;
    private final GuiSlot inputSlot;

    public ChemicalDissolutionRecipeCategory(IGuiHelper helper, MekanismJEIRecipeType<ChemicalDissolutionRecipe> recipeType) {
        super(helper, recipeType, MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER, 3, 3, 170, 79);
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
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, ChemicalDissolutionRecipe recipe, @Nonnull IFocusGroup focusGroup) {
        initItem(builder, RecipeIngredientRole.INPUT, inputSlot, recipe.getItemInput().getRepresentations());
        List<@NonNull GasStack> gasInputs = recipe.getGasInput().getRepresentations();
        List<GasStack> scaledGases = gasInputs.stream().map(gas -> new GasStack(gas, gas.getAmount() * TileEntityChemicalDissolutionChamber.BASE_TICKS_REQUIRED))
              .toList();
        initChemical(builder, MekanismJEI.TYPE_GAS, RecipeIngredientRole.INPUT, inputGauge, scaledGases);
        List<BoxedChemicalStack> outputDefinition = recipe.getOutputDefinition();
        if (outputDefinition.size() == 1) {
            BoxedChemicalStack output = outputDefinition.get(0);
            initChemicalOutput(builder, MekanismJEI.getIngredientType(output.getChemicalType()), Collections.singletonList(output.getChemicalStack()));
        } else {
            //This in theory handles them being multiple types as well, but is that even something we want to allow?
            Map<ChemicalType, List<ChemicalStack<?>>> outputs = new EnumMap<>(ChemicalType.class);
            for (BoxedChemicalStack output : outputDefinition) {
                outputs.computeIfAbsent(output.getChemicalType(), type -> new ArrayList<>());
            }
            for (BoxedChemicalStack output : outputDefinition) {
                ChemicalType chemicalType = output.getChemicalType();
                for (Map.Entry<ChemicalType, List<ChemicalStack<?>>> entry : outputs.entrySet()) {
                    if (entry.getKey() == chemicalType) {
                        entry.getValue().add(output.getChemicalStack());
                    } else {
                        entry.getValue().add(ChemicalUtil.getEmptyStack(entry.getKey()));
                    }
                }
            }
            for (Map.Entry<ChemicalType, List<ChemicalStack<?>>> entry : outputs.entrySet()) {
                initChemicalOutput(builder, MekanismJEI.getIngredientType(entry.getKey()), entry.getValue());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <STACK extends ChemicalStack<?>> void initChemicalOutput(IRecipeLayoutBuilder builder, IIngredientType<STACK> type, List<ChemicalStack<?>> stacks) {
        initChemical(builder, type, RecipeIngredientRole.OUTPUT, outputGauge, (List<STACK>) stacks);
    }
}