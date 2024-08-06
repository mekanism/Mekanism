package mekanism.client.recipe_viewer.emi.recipe;

import dev.emi.emi.api.widget.WidgetHolder;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.emi.MekanismEmiRecipeCategory;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.world.item.crafting.RecipeHolder;

public abstract class ItemStackToChemicalEmiRecipe<CHEMICAL extends Chemical, STACK extends ChemicalStack,
      RECIPE extends ItemStackToChemicalRecipe> extends MekanismEmiHolderRecipe<RECIPE> {

    private final int processTime;

    protected ItemStackToChemicalEmiRecipe(MekanismEmiRecipeCategory category, RecipeHolder<RECIPE> recipeHolder, int processTime) {
        super(category, recipeHolder);
        this.processTime = processTime;
        addInputDefinition(recipe.getInput());
        addChemicalOutputDefinition(recipe.getOutputDefinition());
    }

    protected abstract GuiChemicalGauge getGauge(GaugeType type, int x, int y);

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        initTank(widgetHolder, getGauge(GaugeType.STANDARD.with(DataType.OUTPUT), 131, 13), output(0)).recipeContext(this);
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