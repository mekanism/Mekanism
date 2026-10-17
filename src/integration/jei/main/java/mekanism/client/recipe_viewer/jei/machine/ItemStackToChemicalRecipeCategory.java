package mekanism.client.recipe_viewer.jei.machine;

import mekanism.api.recipes.ItemStackToChemicalRecipe;
import mekanism.api.recipes.book.MekanismBookCategory;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
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

public class ItemStackToChemicalRecipeCategory<BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>>
      extends HolderRecipeCategory<ItemStackToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO>> {

    protected static final String CHEMICAL_OUTPUT = "chemicalOutput";

    protected final GuiProgress progressBar;
    private final GuiGauge<?> output;
    private final GuiSlot input;

    public ItemStackToChemicalRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<ItemStackToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO>> recipeType, boolean isConversion) {
        super(helper, recipeType);
        output = addElement(GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 131, 13));
        input = addSlot(SlotType.INPUT, 26, 36);
        progressBar = addElement(new GuiProgress(isConversion ? () -> 1 : getSimpleProgressTimer(), ProgressType.LARGE_RIGHT, this, 64, 40));
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<ItemStackToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO>> recipeHolder, IFocusGroup focusGroup) {
        ItemStackToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO> recipe = recipeHolder.value();
        initItem(builder, RecipeIngredientRole.INPUT, input, recipe.getInput().display());
        initChemical(builder, RecipeIngredientRole.OUTPUT, output, recipe.getOutputDisplay())
              .setSlotName(CHEMICAL_OUTPUT);
    }
}