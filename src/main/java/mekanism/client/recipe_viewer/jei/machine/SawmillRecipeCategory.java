package mekanism.client.recipe_viewer.jei.machine;

import mekanism.api.recipes.SawmillRecipe;
import mekanism.client.gui.element.GuiUpArrow;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.jei.HolderRecipeCategory;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.util.text.TextUtils;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

public class SawmillRecipeCategory extends HolderRecipeCategory<SawmillRecipe> {

    private final GuiSlot input;
    private final GuiSlot output;

    public SawmillRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<SawmillRecipe> recipeType) {
        super(helper, recipeType);
        addElement(new GuiUpArrow(this, 60, 38));
        input = addSlot(SlotType.INPUT, 56, 17);
        addSlot(SlotType.POWER, 56, 53).with(SlotOverlay.POWER);
        output = addSlot(SlotType.OUTPUT_WIDE, 112, 31);
        addElement(new GuiVerticalPowerBar(this, RecipeViewerUtils.FULL_BAR, 164, 15));
        addSimpleProgress(ProgressType.BAR, 78, 38);
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<SawmillRecipe> recipeHolder, IFocusGroup focuses) {
        super.createRecipeExtras(builder, recipeHolder, focuses);
        double secondaryChance = recipeHolder.value().getSecondaryChance();
        if (secondaryChance > 0) {
            builder.addText(TextUtils.getPercent(secondaryChance), output.getWidth() - 2, font().lineHeight)
                  //Perform the same translations as super does
                  .setPosition(getGuiLeft() + output.getRelativeX() + 1, getGuiTop() + output.getRelativeBottom() + 1)
                  .setTextAlignment(HorizontalAlignment.RIGHT)
                  .setColor(titleTextColor());
        }
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, RecipeHolder<SawmillRecipe> recipeHolder, @NotNull IFocusGroup focusGroup) {
        SawmillRecipe recipe = recipeHolder.value();
        initItem(builder, RecipeIngredientRole.INPUT, input, recipe.getInput().getRepresentations());
        initItem(builder, RecipeIngredientRole.OUTPUT, output.getX() + 4, output.getY() + 4, recipe.getMainOutputDefinition());
        initItem(builder, RecipeIngredientRole.OUTPUT, output.getX() + 20, output.getY() + 4, recipe.getSecondaryOutputDefinition());
    }
}