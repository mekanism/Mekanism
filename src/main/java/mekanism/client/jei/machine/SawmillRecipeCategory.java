package mekanism.client.jei.machine;

import mekanism.api.recipes.SawmillRecipe;
import mekanism.client.SpecialColors;
import mekanism.client.gui.element.GuiUpArrow;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.HolderRecipeCategory;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.text.TextUtils;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

public class SawmillRecipeCategory extends HolderRecipeCategory<SawmillRecipe> {

    private final GuiSlot input;
    private final GuiSlot output;

    public SawmillRecipeCategory(IGuiHelper helper, MekanismJEIRecipeType<SawmillRecipe> recipeType) {
        super(helper, recipeType, MekanismBlocks.PRECISION_SAWMILL, 28, 16, 144, 54);
        addElement(new GuiUpArrow(this, 60, 38));
        input = addSlot(SlotType.INPUT, 56, 17);
        addSlot(SlotType.POWER, 56, 53).with(SlotOverlay.POWER);
        output = addSlot(SlotType.OUTPUT_WIDE, 112, 31);
        addElement(new GuiVerticalPowerBar(this, FULL_BAR, 164, 15));
        addSimpleProgress(ProgressType.BAR, 78, 38);
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, RecipeHolder<SawmillRecipe> recipeHolder, @NotNull IFocusGroup focusGroup) {
        SawmillRecipe recipe = recipeHolder.value();
        initItem(builder, RecipeIngredientRole.INPUT, input, recipe.getInput().getRepresentations());
        initItem(builder, RecipeIngredientRole.OUTPUT, output.getRelativeX() + 4, output.getRelativeY() + 4, recipe.getMainOutputDefinition());
        initItem(builder, RecipeIngredientRole.OUTPUT, output.getRelativeX() + 20, output.getRelativeY() + 4, recipe.getSecondaryOutputDefinition());
    }

    @Override
    public void draw(RecipeHolder<SawmillRecipe> recipeHolder, IRecipeSlotsView recipeSlotView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        super.draw(recipeHolder, recipeSlotView, guiGraphics, mouseX, mouseY);
        double secondaryChance = recipeHolder.value().getSecondaryChance();
        if (secondaryChance > 0) {
            guiGraphics.drawString(getFont(), TextUtils.getPercent(secondaryChance), 104, 41, SpecialColors.TEXT_TITLE.argb(), false);
        }
    }
}