package mekanism.client.recipe_viewer.jei.machine;

import mekanism.api.recipes.ItemStackToFluidRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.jei.BaseRecipeCategory;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.tile.component.config.DataType;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemStackToFluidRecipeCategory extends BaseRecipeCategory<ItemStackToFluidRecipe> {

    protected final GuiProgress progressBar;
    private final GuiGauge<?> output;
    private final GuiSlot input;

    public ItemStackToFluidRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<ItemStackToFluidRecipe> recipeType, boolean isConversion) {
        super(helper, recipeType);
        output = addElement(GuiFluidGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 131, 13));
        input = addSlot(SlotType.INPUT, 26, 36);
        progressBar = addElement(new GuiProgress(isConversion ? () -> 1 : getSimpleProgressTimer(), ProgressType.LARGE_RIGHT, this, 64, 40));
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, ItemStackToFluidRecipe recipe, @NotNull IFocusGroup focusGroup) {
        initItem(builder, RecipeIngredientRole.INPUT, input, recipe.getInput().getRepresentations());
        initFluid(builder, RecipeIngredientRole.OUTPUT, output, recipe.getOutputDefinition());
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName(@NotNull ItemStackToFluidRecipe recipe) {
        return null;
    }
}