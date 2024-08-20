package mekanism.client.recipe_viewer.emi.recipe;

import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.widget.WidgetHolder;
import mekanism.api.recipes.ChemicalChemicalToChemicalRecipe;
import mekanism.client.gui.element.bar.GuiHorizontalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.emi.MekanismEmiRecipeCategory;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ChemicalChemicalToChemicalEmiRecipe extends MekanismEmiHolderRecipe<ChemicalChemicalToChemicalRecipe> {

    public ChemicalChemicalToChemicalEmiRecipe(MekanismEmiRecipeCategory category, RecipeHolder<ChemicalChemicalToChemicalRecipe> recipeHolder) {
        super(category, recipeHolder);
        addInputDefinition(recipe.getLeftInput());
        addInputDefinition(recipe.getRightInput());
        addChemicalOutputDefinition(recipe.getOutputDefinition());
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        //Add shapeless icon to represent that it doesn't matter which sides the inputs are added on
        widgetHolder.addTexture(EmiTexture.SHAPELESS, 152, 2);

        initTank(widgetHolder, GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT_1), this, 25, 13), input(0));
        initTank(widgetHolder, GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 79, 4), output(0)).recipeContext(this);
        initTank(widgetHolder, GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT_2), this, 133, 13), input(1));
        addSlot(widgetHolder, SlotType.INPUT, 6, 56).with(SlotOverlay.MINUS);
        addSlot(widgetHolder, SlotType.INPUT_2, 154, 56).with(SlotOverlay.MINUS);
        addSlot(widgetHolder, SlotType.OUTPUT, 80, 65).with(SlotOverlay.PLUS);
        //Skip the power button so that it doesn't intersect with the shapeless icon
        addConstantProgress(widgetHolder, ProgressType.SMALL_RIGHT, 47, 39, true);
        addConstantProgress(widgetHolder, ProgressType.SMALL_LEFT, 101, 39, false);
        addElement(widgetHolder, new GuiHorizontalPowerBar(this, RecipeViewerUtils.FULL_BAR, 115, 75));
    }

    protected GuiProgress addConstantProgress(WidgetHolder widgetHolder, ProgressType type, int x, int y, boolean left) {
        return addConstantProgress(widgetHolder, type, x, y);
    }
}