package mekanism.client.recipe_viewer.jei.machine;

import mekanism.api.recipes.GasToGasRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.jei.HolderRecipeCategory;
import mekanism.client.recipe_viewer.jei.MekanismJEI;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.tile.component.config.DataType;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

public class GasToGasRecipeCategory extends HolderRecipeCategory<GasToGasRecipe> {

    private final GuiGauge<?> input;
    private final GuiGauge<?> output;

    public GasToGasRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<GasToGasRecipe> recipeType) {
        super(helper, recipeType);
        addSlot(SlotType.INPUT, 5, 56).with(SlotOverlay.MINUS);
        addSlot(SlotType.OUTPUT, 155, 56).with(SlotOverlay.PLUS);
        GaugeType type1 = GaugeType.STANDARD.with(DataType.INPUT);
        input = addElement(GuiChemicalGauge.getDummy(type1, this, 25, 13));
        GaugeType type = GaugeType.STANDARD.with(DataType.OUTPUT);
        output = addElement(GuiChemicalGauge.getDummy(type, this, 133, 13));
        addConstantProgress(ProgressType.LARGE_RIGHT, 64, 39);
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, RecipeHolder<GasToGasRecipe> recipeHolder, @NotNull IFocusGroup focusGroup) {
        GasToGasRecipe recipe = recipeHolder.value();
        initChemical(builder, MekanismJEI.TYPE_GAS, RecipeIngredientRole.INPUT, input, recipe.getInput().getRepresentations());
        initChemical(builder, MekanismJEI.TYPE_GAS, RecipeIngredientRole.OUTPUT, output, recipe.getOutputDefinition());
    }
}