package mekanism.client.recipe_viewer.emi.recipe;

import dev.emi.emi.api.widget.WidgetHolder;
import mekanism.api.recipes.ItemStackChemicalToItemStackRecipe;
import mekanism.client.gui.element.bar.GuiEmptyBar;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.emi.MekanismEmiRecipeCategory;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ItemStackChemicalToItemStackEmiRecipe extends MekanismEmiHolderRecipe<ItemStackChemicalToItemStackRecipe> {

    private static final int PROCESS_TIME = TileEntityAdvancedElectricMachine.BASE_TICKS_REQUIRED;

    public ItemStackChemicalToItemStackEmiRecipe(MekanismEmiRecipeCategory category, RecipeHolder<ItemStackChemicalToItemStackRecipe> recipeHolder) {
        super(category, recipeHolder);
        addInputDefinition(recipe.getItemInput());
        addItemOutputDefinition(recipe.getOutputDefinition());
        addInputDefinition(recipe.getChemicalInput(), recipe.perTickUsage() ? PROCESS_TIME : 1);
        addCatalsyst(recipe.getChemicalInput());
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        addSlot(widgetHolder, SlotType.INPUT, 64, 17, input(0));
        addSlot(widgetHolder, SlotType.EXTRA, 64, 53, catalyst(0)).catalyst(true);
        addSlot(widgetHolder, SlotType.OUTPUT, 116, 35, output(0)).recipeContext(this);
        addSlot(widgetHolder, SlotType.POWER, 39, 35).with(SlotOverlay.POWER);
        addElement(widgetHolder, new GuiVerticalPowerBar(this, RecipeViewerUtils.FULL_BAR, 164, 16));
        initTank(widgetHolder, new GuiEmptyBar(this, 68, 36, 6, 12), input(1));
        addSimpleProgress(widgetHolder, ProgressType.BAR, 86, 38, PROCESS_TIME);
    }
}