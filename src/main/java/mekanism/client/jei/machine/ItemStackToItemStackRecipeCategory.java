package mekanism.client.jei.machine;

import java.util.Collections;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.client.gui.element.GuiUpArrow;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
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

    private final GuiSlot input;
    private final GuiSlot output;

    public ItemStackToItemStackRecipeCategory(IGuiHelper helper, IBlockProvider mekanismBlock) {
        super(helper, mekanismBlock, 28, 16, 144, 54);
        addElement(new GuiUpArrow(this, 68, 38));
        input = addSlot(SlotType.INPUT, 64, 17);
        output = addSlot(SlotType.OUTPUT, 116, 35);
        addSlot(SlotType.POWER, 64, 53).with(SlotOverlay.POWER);
        addElement(new GuiVerticalPowerBar(this, FULL_BAR, 164, 15));
        addSimpleProgress(ProgressType.BAR, 86, 38);
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
        initItem(itemStacks, 0, true, input, recipe.getInput().getRepresentations());
        initItem(itemStacks, 1, false, output, recipe.getOutputDefinition());
    }
}