package mekanism.client.recipe_viewer.emi.recipe;

import dev.emi.emi.api.widget.WidgetHolder;
import mekanism.api.recipes.ItemStackToFluidRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.emi.MekanismEmiRecipeCategory;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.resources.ResourceLocation;

public class ItemStackToFluidEmiRecipe extends MekanismEmiRecipe<ItemStackToFluidRecipe> {

    private final int processTime;

    public ItemStackToFluidEmiRecipe(MekanismEmiRecipeCategory category, ResourceLocation id, ItemStackToFluidRecipe recipe, int processTime) {
        super(category, id, recipe);
        this.processTime = processTime;
        addInputDefinition(recipe.getInput());
        addFluidOutputDefinition(recipe.getOutputDefinition());
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        initTank(widgetHolder, GuiFluidGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 131, 13), output(0)).recipeContext(this);
        addSlot(widgetHolder, SlotType.INPUT, 26, 36, input(0));
        if (processTime == 0) {
            addConstantProgress(widgetHolder, ProgressType.LARGE_RIGHT, 64, 40);
        } else {
            addSimpleProgress(widgetHolder, ProgressType.LARGE_RIGHT, 64, 40, processTime);
        }
    }
}