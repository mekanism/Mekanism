package mekanism.client.recipe_viewer.emi.recipe;

import dev.emi.emi.api.widget.WidgetHolder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.recipes.ItemStackToFluidOptionalItemRecipe.FluidOptionalItemOutput;
import mekanism.api.recipes.basic.BasicItemStackToFluidOptionalItemRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.emi.MekanismEmiRecipeCategory;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

public class ItemStackToFluidOptionalItemEmiRecipe extends MekanismEmiRecipe<BasicItemStackToFluidOptionalItemRecipe> {

    private final int processTime;

    public ItemStackToFluidOptionalItemEmiRecipe(MekanismEmiRecipeCategory category, ResourceLocation id, BasicItemStackToFluidOptionalItemRecipe recipe, int processTime) {
        super(category, id, recipe);
        this.processTime = processTime;
        addInputDefinition(recipe.getInput());
        List<FluidOptionalItemOutput> outputDefinition = recipe.getOutputDefinition();
        List<FluidStack> fluidOutputs = new ArrayList<>(outputDefinition.size());
        List<ItemStack> itemOutputs = new ArrayList<>();
        for (FluidOptionalItemOutput output : outputDefinition) {
            fluidOutputs.add(output.fluid());
            itemOutputs.add(output.optionalItem());
        }
        addFluidOutputDefinition(fluidOutputs);
        if (itemOutputs.stream().allMatch(ConstantPredicates.ITEM_EMPTY)) {
            addOutputDefinition(Collections.emptyList());
        } else {
            addItemOutputDefinition(itemOutputs);
        }
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