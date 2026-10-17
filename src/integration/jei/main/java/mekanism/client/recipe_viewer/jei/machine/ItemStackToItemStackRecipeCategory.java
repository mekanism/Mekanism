package mekanism.client.recipe_viewer.jei.machine;

import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.jei.HolderRecipeCategory;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.item.crafting.Recipe.BookInfo;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ItemStackToItemStackRecipeCategory<BOOK_CATEGORY, BOOK_INFO extends BookInfo<BOOK_CATEGORY>>
      extends HolderRecipeCategory<ItemStackToItemStackRecipe<BOOK_CATEGORY, BOOK_INFO>> {

    private final GuiSlot input;
    private final GuiSlot output;

    public ItemStackToItemStackRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<ItemStackToItemStackRecipe<BOOK_CATEGORY, BOOK_INFO>> recipeType) {
        super(helper, recipeType);
        addElement(GuiTexturedElement.upArrow(this, 68, 38));
        input = addSlot(SlotType.INPUT, 64, 17);
        output = addSlot(SlotType.OUTPUT, 116, 35);
        addSlot(SlotType.POWER, 64, 53).with(SlotOverlay.POWER);
        addElement(new GuiVerticalPowerBar(this, RecipeViewerUtils.FULL_BAR, 164, 16));
        addSimpleProgress(ProgressType.BAR, 86, 38);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<ItemStackToItemStackRecipe<BOOK_CATEGORY, BOOK_INFO>> recipeHolder, IFocusGroup focusGroup) {
        ItemStackToItemStackRecipe<BOOK_CATEGORY, BOOK_INFO> recipe = recipeHolder.value();
        initItem(builder, RecipeIngredientRole.INPUT, input, recipe.getInput().display());
        initItem(builder, RecipeIngredientRole.OUTPUT, output, recipe.getOutputDisplay());
    }
}