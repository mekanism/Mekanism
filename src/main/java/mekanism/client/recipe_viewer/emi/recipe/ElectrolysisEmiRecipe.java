package mekanism.client.recipe_viewer.emi.recipe;

import dev.emi.emi.api.widget.WidgetHolder;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.ElectrolysisRecipe.ElectrolysisRecipeOutput;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.emi.MekanismEmiRecipeCategory;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ElectrolysisEmiRecipe extends MekanismEmiHolderRecipe<ElectrolysisRecipe> {

    public ElectrolysisEmiRecipe(MekanismEmiRecipeCategory category, RecipeHolder<ElectrolysisRecipe> recipeHolder) {
        super(category, recipeHolder);
        addInputDefinition(recipe.getInput());
        List<ChemicalStack> leftDefinition = new ArrayList<>();
        List<ChemicalStack> rightDefinition = new ArrayList<>();
        for (ElectrolysisRecipeOutput output : recipe.getOutputDefinition()) {
            leftDefinition.add(output.left());
            rightDefinition.add(output.right());
        }
        addChemicalOutputDefinition(leftDefinition);
        addChemicalOutputDefinition(rightDefinition);
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        initTank(widgetHolder, GuiFluidGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 5, 10), input(0));
        GaugeType type1 = GaugeType.SMALL.with(DataType.OUTPUT_1);
        initTank(widgetHolder, GuiChemicalGauge.getDummy(type1, this, 58, 18), output(0)).recipeContext(this);
        GaugeType type = GaugeType.SMALL.with(DataType.OUTPUT_2);
        initTank(widgetHolder, GuiChemicalGauge.getDummy(type, this, 100, 18), output(1)).recipeContext(this);
        addElement(widgetHolder, new GuiVerticalPowerBar(this, RecipeViewerUtils.FULL_BAR, 164, 15));
        addSlot(widgetHolder, SlotType.INPUT, 26, 35);
        addSlot(widgetHolder, SlotType.OUTPUT, 59, 52);
        addSlot(widgetHolder, SlotType.OUTPUT_2, 101, 52);
        addSlot(widgetHolder, SlotType.POWER, 143, 35).with(SlotOverlay.POWER);
        addConstantProgress(widgetHolder, ProgressType.BI, 80, 30);
    }
}