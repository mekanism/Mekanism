package mekanism.client.jei.machine;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import javax.annotation.Nonnull;
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
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;

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
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, SawmillRecipe recipe, @Nonnull List<? extends IFocus<?>> focuses) {
        initItem(builder, 0, RecipeIngredientRole.INPUT, input, recipe.getInput().getRepresentations());
        initItem(builder, 1, RecipeIngredientRole.OUTPUT, output.getRelativeX() + 4, output.getRelativeY() + 4, recipe.getMainOutputDefinition());
        initItem(builder, 2, RecipeIngredientRole.OUTPUT, output.getRelativeX() + 20, output.getRelativeY() + 4, recipe.getSecondaryOutputDefinition());
    }

    @Override
    public void draw(SawmillRecipe recipe, IRecipeSlotsView recipeSlotView, PoseStack matrix, double mouseX, double mouseY) {
        super.draw(recipe, recipeSlotView, matrix, mouseX, mouseY);
        double secondaryChance = recipe.getSecondaryChance();
        if (secondaryChance > 0) {
            getFont().draw(matrix, TextUtils.getPercent(secondaryChance), 104, 41, SpecialColors.TEXT_TITLE.argb());
        }
    }
}