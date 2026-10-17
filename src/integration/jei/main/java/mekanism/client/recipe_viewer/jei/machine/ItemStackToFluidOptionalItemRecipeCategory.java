package mekanism.client.recipe_viewer.jei.machine;

import mekanism.api.recipes.ItemStackToFluidOptionalItemRecipe;
import mekanism.api.recipes.book.MekanismBookCategory;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.jei.HolderRecipeCategory;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.tile.component.config.DataType;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public class ItemStackToFluidOptionalItemRecipeCategory<BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>>
      extends HolderRecipeCategory<ItemStackToFluidOptionalItemRecipe<BOOK_CATEGORY, BOOK_INFO>> {

    private final GuiGauge<?> outputTank;
    private final GuiSlot outputItem;
    private final GuiSlot input;

    public ItemStackToFluidOptionalItemRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<ItemStackToFluidOptionalItemRecipe<BOOK_CATEGORY, BOOK_INFO>> recipeType, boolean isConversion) {
        super(helper, recipeType);
        input = addSlot(SlotType.INPUT, 26, 36);
        outputItem = addSlot(SlotType.OUTPUT, 110, 36);
        outputTank = addElement(GuiFluidGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 131, 13));
        addElement(new GuiProgress(isConversion ? () -> 1 : getSimpleProgressTimer(), ProgressType.LARGE_RIGHT, this, 54, 40));
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<ItemStackToFluidOptionalItemRecipe<BOOK_CATEGORY, BOOK_INFO>> recipeHolder, IFocusGroup focusGroup) {
        ItemStackToFluidOptionalItemRecipe<BOOK_CATEGORY, BOOK_INFO> recipe = recipeHolder.value();
        initItem(builder, RecipeIngredientRole.INPUT, input, recipe.getInput().display());
        SlotDisplay outputDisplay = recipe.getOutputDisplay();
        initFluid(builder, RecipeIngredientRole.OUTPUT, outputTank, outputDisplay);
        initItem(builder, RecipeIngredientRole.OUTPUT, outputItem, outputDisplay);
    }

    @Override
    protected boolean hasSyntheticRecipes() {
        return true;
    }
}