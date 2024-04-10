package mekanism.client.recipe_viewer.emi.recipe;

import dev.emi.emi.api.widget.WidgetHolder;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiDynamicHorizontalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.gauge.GuiEnergyGauge.IEnergyInfoHandler;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.emi.MekanismEmiRecipeCategory;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.lib.Color;
import mekanism.common.lib.Color.ColorFunction;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.world.item.crafting.RecipeHolder;

public class NucleosynthesizingEmiRecipe extends MekanismEmiHolderRecipe<NucleosynthesizingRecipe> {

    public NucleosynthesizingEmiRecipe(MekanismEmiRecipeCategory category, RecipeHolder<NucleosynthesizingRecipe> recipeHolder) {
        super(category, recipeHolder);
        addInputDefinition(recipe.getItemInput());
        addInputDefinition(recipe.getChemicalInput());
        addItemOutputDefinition(recipe.getOutputDefinition());
        addCatalsyst(recipe.getChemicalInput());
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        addSlot(widgetHolder, SlotType.INPUT, 26, 40, input(0));
        addSlot(widgetHolder, SlotType.EXTRA, 6, 69, catalyst(0)).catalyst(true);
        addSlot(widgetHolder, SlotType.OUTPUT, 152, 40, output(0)).recipeContext(this);
        addSlot(widgetHolder, SlotType.POWER, 173, 69).with(SlotOverlay.POWER);
        addElement(widgetHolder, new GuiInnerScreen(this, 45, 18, 104, 68));
        initTank(widgetHolder, GuiGasGauge.getDummy(GaugeType.SMALL_MED.with(DataType.INPUT), this, 5, 18), input(1));
        addElement(widgetHolder, new GuiEnergyGauge(new IEnergyInfoHandler() {
            @Override
            public FloatingLong getEnergy() {
                return FloatingLong.ONE;
            }

            @Override
            public FloatingLong getMaxEnergy() {
                return FloatingLong.ONE;
            }
        }, GaugeType.SMALL_MED, this, 172, 18));
        addElement(widgetHolder, new GuiDynamicHorizontalRateBar(this, RecipeViewerUtils.barProgressHandler(recipe.getDuration()),
              5, 88, 183, ColorFunction.scale(Color.rgbi(60, 45, 74), Color.rgbi(100, 30, 170))));
    }
}