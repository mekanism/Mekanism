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
import mekanism.common.tile.machine.TileEntityMetallurgicInfuser;
import net.minecraft.world.item.crafting.RecipeHolder;

public class MetallurgicInfuserEmiRecipe extends MekanismEmiHolderRecipe<ItemStackChemicalToItemStackRecipe> {

    public MetallurgicInfuserEmiRecipe(MekanismEmiRecipeCategory category, RecipeHolder<ItemStackChemicalToItemStackRecipe> recipeHolder) {
        super(category, recipeHolder);
        addInputDefinition(recipe.getItemInput());
        addInputDefinition(recipe.getChemicalInput(), recipe.perTickUsage() ? TileEntityMetallurgicInfuser.BASE_TICKS_REQUIRED : 1);
        addItemOutputDefinition(recipe.getOutputDefinition());
        addCatalsyst(recipe.getChemicalInput());
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        addSlot(widgetHolder, SlotType.EXTRA, 17, 35, catalyst(0)).catalyst(true);
        addSlot(widgetHolder, SlotType.INPUT, 51, 43, input(0));
        addSlot(widgetHolder, SlotType.OUTPUT, 109, 43, output(0)).recipeContext(this);
        addSlot(widgetHolder, SlotType.POWER, 143, 35).with(SlotOverlay.POWER);
        addElement(widgetHolder, new GuiVerticalPowerBar(this, RecipeViewerUtils.FULL_BAR, 164, 15));
        addSimpleProgress(widgetHolder, ProgressType.RIGHT, 72, 47, TileEntityMetallurgicInfuser.BASE_TICKS_REQUIRED);
        initTank(widgetHolder, new GuiEmptyBar(this, 7, 15, 4, 52), input(1));
    }
}