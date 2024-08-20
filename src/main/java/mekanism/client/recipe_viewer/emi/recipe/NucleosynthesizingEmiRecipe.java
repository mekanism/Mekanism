package mekanism.client.recipe_viewer.emi.recipe;

import dev.emi.emi.api.widget.WidgetHolder;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiDynamicHorizontalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.gauge.GuiEnergyGauge.IEnergyInfoHandler;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.emi.MekanismEmiRecipeCategory;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.lib.Color;
import mekanism.common.lib.Color.ColorFunction;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.machine.TileEntityAntiprotonicNucleosynthesizer;
import net.minecraft.world.item.crafting.RecipeHolder;

public class NucleosynthesizingEmiRecipe extends MekanismEmiHolderRecipe<NucleosynthesizingRecipe> {

    public NucleosynthesizingEmiRecipe(MekanismEmiRecipeCategory category, RecipeHolder<NucleosynthesizingRecipe> recipeHolder) {
        super(category, recipeHolder);
        addInputDefinition(recipe.getItemInput());
        addInputDefinition(recipe.getChemicalInput(), recipe.perTickUsage() ? TileEntityAntiprotonicNucleosynthesizer.BASE_TICKS_REQUIRED : 1);
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
        GaugeType type = GaugeType.SMALL_MED.with(DataType.INPUT);
        initTank(widgetHolder, GuiChemicalGauge.getDummy(type, this, 5, 18), input(1));
        addElement(widgetHolder, new GuiEnergyGauge(new IEnergyInfoHandler() {
            @Override
            public long getEnergy() {
                return 1L;
            }

            @Override
            public long getMaxEnergy() {
                return 1L;
            }
        }, GaugeType.SMALL_MED, this, 172, 18));
        addElement(widgetHolder, new GuiDynamicHorizontalRateBar(this, RecipeViewerUtils.barProgressHandler(recipe.getDuration()),
              5, 88, 183, ColorFunction.scale(Color.rgbi(60, 45, 74), Color.rgbi(100, 30, 170))));
    }
}