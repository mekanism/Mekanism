package mekanism.client.recipe_viewer.jei.machine;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.recipes.ChemicalOxidizerRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.jei.HolderRecipeCategory;
import mekanism.client.recipe_viewer.jei.MekanismJEI;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.util.ChemicalUtil;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ChemicalOxidizerRecipeCategory extends HolderRecipeCategory<ChemicalOxidizerRecipe> {

    private final GuiSlot inputSlot;
    private final GuiGauge<?> outputGauge;


    public ChemicalOxidizerRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<ChemicalOxidizerRecipe> recipeType) {
        super(helper, recipeType);
        inputSlot = addSlot(SlotType.INPUT, 26, 36);
        outputGauge = addElement(GuiGasGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 131, 13));
        addSimpleProgress(ProgressType.LARGE_RIGHT, 64, 40);
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, RecipeHolder<ChemicalOxidizerRecipe> recipeHolder, @NotNull IFocusGroup focusGroup) {
        ChemicalOxidizerRecipe recipe = recipeHolder.value();
        initItem(builder, RecipeIngredientRole.INPUT, inputSlot, recipe.getInput().getRepresentations());
        List<BoxedChemicalStack> outputDefinition = recipe.getOutputDefinition();
        if (outputDefinition.size() == 1) {
            BoxedChemicalStack output = outputDefinition.get(0);
            initChemicalOutput(builder, MekanismJEI.getIngredientType(output.getChemicalType()), Collections.singletonList(output.getChemicalStack()));
        } else {
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