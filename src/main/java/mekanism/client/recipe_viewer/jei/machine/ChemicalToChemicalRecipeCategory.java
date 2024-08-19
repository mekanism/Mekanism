package mekanism.client.recipe_viewer.jei.machine;

import mekanism.api.recipes.ChemicalToChemicalRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.jei.HolderRecipeCategory;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.tile.component.config.DataType;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

public class ChemicalToChemicalRecipeCategory extends HolderRecipeCategory<ChemicalToChemicalRecipe> {

    private final GuiGauge<?> input;
    private final GuiGauge<?> output;

    public ChemicalToChemicalRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<ChemicalToChemicalRecipe> recipeType) {
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
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, RecipeHolder<ChemicalToChemicalRecipe> recipeHolder, @NotNull IFocusGroup focusGroup) {
        ChemicalToChemicalRecipe recipe = recipeHolder.value();
        initChemical(builder, RecipeIngredientRole.INPUT, input, recipe.getInput().getRepresentations());
        initChemical(builder, RecipeIngredientRole.OUTPUT, output, recipe.getOutputDefinition());
    }
}