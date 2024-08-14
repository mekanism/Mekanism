package mekanism.client.recipe_viewer.emi.recipe;

import dev.emi.emi.api.widget.WidgetHolder;
import mekanism.api.recipes.ChemicalToChemicalRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.emi.MekanismEmiRecipeCategory;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ChemicalToChemicalEmiRecipe extends MekanismEmiHolderRecipe<ChemicalToChemicalRecipe> {

    public ChemicalToChemicalEmiRecipe(MekanismEmiRecipeCategory category, RecipeHolder<ChemicalToChemicalRecipe> recipeHolder) {
        super(category, recipeHolder);
        addInputDefinition(recipe.getInput());
        addChemicalOutputDefinition(recipe.getOutputDefinition());
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        addSlot(widgetHolder, SlotType.INPUT, 5, 56).with(SlotOverlay.MINUS);
        addSlot(widgetHolder, SlotType.OUTPUT, 155, 56).with(SlotOverlay.PLUS);
        GaugeType type1 = GaugeType.STANDARD.with(DataType.INPUT);
        initTank(widgetHolder, GuiChemicalGauge.getDummy(type1, this, 25, 13), input(0));
        GaugeType type = GaugeType.STANDARD.with(DataType.OUTPUT);
        initTank(widgetHolder, GuiChemicalGauge.getDummy(type, this, 133, 13), output(0)).recipeContext(this);
        addConstantProgress(widgetHolder, ProgressType.LARGE_RIGHT, 64, 39);
    }
}