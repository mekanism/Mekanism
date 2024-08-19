package mekanism.client.recipe_viewer.emi.recipe;

import dev.emi.emi.api.widget.WidgetHolder;
import mekanism.api.recipes.ItemStackToChemicalRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.emi.MekanismEmiRecipeCategory;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ItemStackToChemicalEmiRecipe<RECIPE extends ItemStackToChemicalRecipe> extends MekanismEmiHolderRecipe<RECIPE> {

    private final int processTime;

    public ItemStackToChemicalEmiRecipe(MekanismEmiRecipeCategory category, RecipeHolder<RECIPE> recipeHolder, int processTime) {
        super(category, recipeHolder);
        this.processTime = processTime;
        addInputDefinition(recipe.getInput());
        addChemicalOutputDefinition(recipe.getOutputDefinition());
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        initTank(widgetHolder, GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 131, 13), output(0)).recipeContext(this);
        addSlot(widgetHolder, SlotType.INPUT, 26, 36, input(0));
        addProgressBar(widgetHolder, ProgressType.LARGE_RIGHT, 64, 40);
    }

    protected GuiProgress addProgressBar(WidgetHolder widgetHolder, ProgressType type, int x, int y) {
        if (processTime == 0) {
            return addConstantProgress(widgetHolder, type, x, y);
        }
        return addSimpleProgress(widgetHolder, type, x, y, processTime);
    }
}