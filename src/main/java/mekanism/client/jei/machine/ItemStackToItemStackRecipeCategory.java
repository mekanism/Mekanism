package mekanism.client.jei.machine;

import java.util.Collections;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.client.gui.element.GuiUpArrow;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;

public class ItemStackToItemStackRecipeCategory extends BaseRecipeCategory<ItemStackToItemStackRecipe> {

    public ItemStackToItemStackRecipeCategory(IGuiHelper helper, IBlockProvider mekanismBlock) {
        super(helper, mekanismBlock, 28, 16, 144, 54);
    }

    @Override
    protected void addGuiElements() {
        guiElements.add(new GuiUpArrow(this, 68, 38));
        guiElements.add(new GuiSlot(SlotType.INPUT, this, 63, 16));
        guiElements.add(new GuiSlot(SlotType.POWER, this, 63, 52).with(SlotOverlay.POWER));
        guiElements.add(new GuiSlot(SlotType.OUTPUT, this, 116, 35));
        guiElements.add(new GuiVerticalPowerBar(this, () -> 1F, 164, 15));
        guiElements.add(new GuiProgress(() -> timer.getValue() / 20D, ProgressType.BAR, this, 86, 38));
    }

    @Override
    public Class<? extends ItemStackToItemStackRecipe> getRecipeClass() {
        return ItemStackToItemStackRecipe.class;
    }

    @Override
    public void setIngredients(ItemStackToItemStackRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getInput().getRepresentations()));
        ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getOutputDefinition()));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, ItemStackToItemStackRecipe recipe, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        itemStacks.init(0, true, 35, 0);
        itemStacks.init(1, false, 88, 19);
        itemStacks.set(ingredients);
    }
}