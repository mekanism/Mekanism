package mekanism.client.recipe_viewer.emi.recipe;

import dev.emi.emi.api.widget.WidgetHolder;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.client.gui.element.GuiUpArrow;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.emi.MekanismEmiRecipeCategory;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.tile.machine.TileEntityCombiner;
import net.minecraft.world.item.crafting.RecipeHolder;

public class CombinerEmiRecipe extends MekanismEmiHolderRecipe<CombinerRecipe> {

    public CombinerEmiRecipe(MekanismEmiRecipeCategory category, RecipeHolder<CombinerRecipe> recipeHolder) {
        super(category, recipeHolder);
        addInputDefinition(recipe.getMainInput());
        addInputDefinition(recipe.getExtraInput());
        addItemOutputDefinition(recipe.getOutputDefinition());
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        addElement(widgetHolder, new GuiUpArrow(this, 68, 38));
        addSlot(widgetHolder, SlotType.INPUT, 64, 17, input(0));
        addSlot(widgetHolder, SlotType.EXTRA, 64, 53, input(1));
        addSlot(widgetHolder, SlotType.OUTPUT, 116, 35, output(0)).recipeContext(this);
        addSlot(widgetHolder, SlotType.POWER, 39, 35).with(SlotOverlay.POWER);
        addElement(widgetHolder, new GuiVerticalPowerBar(this, RecipeViewerUtils.FULL_BAR, 164, 15));
        addSimpleProgress(widgetHolder, ProgressType.BAR, 86, 38, TileEntityCombiner.BASE_TICKS_REQUIRED);
    }
}