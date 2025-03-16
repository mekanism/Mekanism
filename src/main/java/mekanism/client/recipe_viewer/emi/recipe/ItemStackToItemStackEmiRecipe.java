package mekanism.client.recipe_viewer.emi.recipe;

import dev.emi.emi.api.widget.WidgetHolder;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.client.gui.element.GuiUpArrow;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.emi.MekanismEmiRecipeCategory;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.tile.prefab.TileEntityElectricMachine;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ItemStackToItemStackEmiRecipe extends MekanismEmiHolderRecipe<ItemStackToItemStackRecipe> {

    private final boolean hideCraftable;

    public ItemStackToItemStackEmiRecipe(MekanismEmiRecipeCategory category, RecipeHolder<ItemStackToItemStackRecipe> recipeHolder) {
        super(category, recipeHolder);
        this.hideCraftable = recipeHolder.id().getPath().startsWith("/mekanism_generated/");
        addInputDefinition(recipe.getInput());
        addItemOutputDefinition(recipe.getOutputDefinition());
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        addElement(widgetHolder, new GuiUpArrow(this, 68, 38));

        addSlot(widgetHolder, SlotType.INPUT, 64, 17, input(0));
        addSlot(widgetHolder, SlotType.OUTPUT, 116, 35, output(0)).recipeContext(this);

        addSlot(widgetHolder, SlotType.POWER, 64, 53).with(SlotOverlay.POWER);
        addElement(widgetHolder, new GuiVerticalPowerBar(this, RecipeViewerUtils.FULL_BAR, 164, 16));
        addSimpleProgress(widgetHolder, ProgressType.BAR, 86, 38, TileEntityElectricMachine.BASE_TICKS_REQUIRED);
    }

    @Override
    public boolean hideCraftable() {
        return hideCraftable;
    }
}