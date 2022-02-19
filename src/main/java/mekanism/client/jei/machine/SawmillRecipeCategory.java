package mekanism.client.jei.machine;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Arrays;
import java.util.Collections;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.client.SpecialColors;
import mekanism.client.gui.element.GuiUpArrow;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.util.text.TextUtils;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;

public class SawmillRecipeCategory extends BaseRecipeCategory<SawmillRecipe> {

    private final GuiSlot input;
    private final GuiSlot output;

    public SawmillRecipeCategory(IGuiHelper helper, IBlockProvider mekanismBlock) {
        super(helper, mekanismBlock, 28, 16, 144, 54);
        addElement(new GuiUpArrow(this, 60, 38));
        input = addSlot(SlotType.INPUT, 56, 17);
        addSlot(SlotType.POWER, 56, 53).with(SlotOverlay.POWER);
        output = addSlot(SlotType.OUTPUT_WIDE, 112, 31);
        addElement(new GuiVerticalPowerBar(this, FULL_BAR, 164, 15));
        addSimpleProgress(ProgressType.BAR, 78, 38);
    }

    @Override
    public Class<? extends SawmillRecipe> getRecipeClass() {
        return SawmillRecipe.class;
    }

    @Override
    public void setIngredients(SawmillRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getInput().getRepresentations()));
        ingredients.setOutputLists(VanillaTypes.ITEM, Arrays.asList(recipe.getMainOutputDefinition(), recipe.getSecondaryOutputDefinition()));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, SawmillRecipe recipe, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        initItem(itemStacks, 0, true, input, recipe.getInput().getRepresentations());
        initItem(itemStacks, 1, false, output.getRelativeX() + 4, output.getRelativeY() + 4, recipe.getMainOutputDefinition());
        initItem(itemStacks, 2, false, output.getRelativeX() + 20, output.getRelativeY() + 4, recipe.getSecondaryOutputDefinition());
    }

    @Override
    public void draw(SawmillRecipe recipe, MatrixStack matrix, double mouseX, double mouseY) {
        super.draw(recipe, matrix, mouseX, mouseY);
        double secondaryChance = recipe.getSecondaryChance();
        if (secondaryChance > 0) {
            getFont().draw(matrix, TextUtils.getPercent(secondaryChance), 104, 41, SpecialColors.TEXT_TITLE.argb());
        }
    }
}