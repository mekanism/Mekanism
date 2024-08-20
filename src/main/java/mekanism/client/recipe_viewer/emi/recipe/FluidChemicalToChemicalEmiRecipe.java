package mekanism.client.recipe_viewer.emi.recipe;

import dev.emi.emi.api.widget.WidgetHolder;
import mekanism.api.recipes.FluidChemicalToChemicalRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.emi.MekanismEmiRecipeCategory;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.world.item.crafting.RecipeHolder;

public class FluidChemicalToChemicalEmiRecipe extends MekanismEmiHolderRecipe<FluidChemicalToChemicalRecipe> {

    public FluidChemicalToChemicalEmiRecipe(MekanismEmiRecipeCategory category, RecipeHolder<FluidChemicalToChemicalRecipe> recipeHolder) {
        super(category, recipeHolder);
        addInputDefinition(recipe.getFluidInput());
        addInputDefinition(recipe.getChemicalInput());
        addChemicalOutputDefinition(recipe.getOutputDefinition());
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        initTank(widgetHolder, GuiFluidGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 7, 13), input(0));
        initTank(widgetHolder, GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 28, 13), input(1));
        initTank(widgetHolder, GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 131, 13), output(0)).recipeContext(this);
        addSlot(widgetHolder, SlotType.POWER, 152, 14).with(SlotOverlay.POWER);
        addSlot(widgetHolder, SlotType.OUTPUT, 152, 56).with(SlotOverlay.MINUS);
        addConstantProgress(widgetHolder, ProgressType.LARGE_RIGHT, 64, 39);
    }
}