package mekanism.client.recipe_viewer.emi.recipe;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.client.gui.element.bar.GuiHorizontalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.emi.ChemicalEmiStack;
import mekanism.client.recipe_viewer.emi.MekanismEmiRecipeCategory;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.machine.TileEntityChemicalDissolutionChamber;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ChemicalDissolutionEmiRecipe extends MekanismEmiHolderRecipe<ChemicalDissolutionRecipe> {

    public ChemicalDissolutionEmiRecipe(MekanismEmiRecipeCategory category, RecipeHolder<ChemicalDissolutionRecipe> recipeHolder) {
        super(category, recipeHolder);
        addInputDefinition(recipe.getItemInput());
        addInputDefinition(recipe.getChemicalInput(), recipe.perTickUsage() ? TileEntityChemicalDissolutionChamber.BASE_TICKS_REQUIRED : 1);
        addOutputDefinition(recipe.getOutputDefinition().stream().<EmiStack>map(ChemicalEmiStack::new).toList());
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        GaugeType type1 = GaugeType.STANDARD.with(DataType.INPUT);
        initTank(widgetHolder, GuiChemicalGauge.getDummy(type1, this, 7, 4), input(1));
        GaugeType type = GaugeType.STANDARD.with(DataType.OUTPUT);
        initTank(widgetHolder, GuiChemicalGauge.getDummy(type, this, 131, 13), output(0)).recipeContext(this);
        addSlot(widgetHolder, SlotType.INPUT, 28, 36, input(0));
        addSlot(widgetHolder, SlotType.EXTRA, 8, 65).with(SlotOverlay.MINUS);
        addSlot(widgetHolder, SlotType.OUTPUT, 152, 55).with(SlotOverlay.PLUS);
        addSlot(widgetHolder, SlotType.POWER, 152, 14).with(SlotOverlay.POWER);
        addSimpleProgress(widgetHolder, ProgressType.LARGE_RIGHT, 64, 40, TileEntityChemicalDissolutionChamber.BASE_TICKS_REQUIRED);
        addElement(widgetHolder, new GuiHorizontalPowerBar(this, RecipeViewerUtils.FULL_BAR, 115, 75));
    }
}