package mekanism.client.recipe_viewer.emi.recipe;

import dev.emi.emi.api.widget.WidgetHolder;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.recipes.ItemStackToFluidOptionalItemRecipe.FluidOptionalItemOutput;
import mekanism.api.recipes.basic.BasicItemStackToFluidOptionalItemRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.emi.MekanismEmiRecipeCategory;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStackTemplate;
import net.neoforged.neoforge.fluids.FluidStackTemplate;

public class ItemStackToFluidOptionalItemEmiRecipe extends MekanismEmiRecipe<BasicItemStackToFluidOptionalItemRecipe> {

    private final int processTime;

    public ItemStackToFluidOptionalItemEmiRecipe(MekanismEmiRecipeCategory category, Identifier id, BasicItemStackToFluidOptionalItemRecipe recipe, int processTime) {
        super(category, id, recipe);
        this.processTime = processTime;
        addInputDefinition(recipe.getInput());
        //TODO - Emi: ContextMap
        List<FluidOptionalItemOutput> outputDefinition = recipe.getOutputDefinition(ContextMap.EMPTY);
        List<FluidStackTemplate> fluidOutputs = new ArrayList<>(outputDefinition.size());
        List<ItemStackTemplate> itemOutputs = new ArrayList<>();
        for (FluidOptionalItemOutput output : outputDefinition) {
            fluidOutputs.add(output.fluid());
            ItemStackTemplate optionalItem = output.optionalItem();
            if (optionalItem != null) {
                itemOutputs.add(optionalItem);
            }
        }
        addFluidOutputDefinition(fluidOutputs);
        addItemOutputDefinition(itemOutputs);
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        initTank(widgetHolder, GuiFluidGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 131, 13), output(0)).recipeContext(this);
        addSlot(widgetHolder, SlotType.INPUT, 26, 36, input(0));
        addSlot(widgetHolder, SlotType.INPUT, 110, 36, output(1));
        if (processTime == 0) {
            addConstantProgress(widgetHolder, ProgressType.LARGE_RIGHT, 54, 40);
        } else {
            addSimpleProgress(widgetHolder, ProgressType.LARGE_RIGHT, 54, 40, processTime);
        }
    }
}