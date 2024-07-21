package mekanism.client.recipe_viewer.emi.recipe;

import dev.emi.emi.api.widget.WidgetHolder;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.gauge.GuiEnergyGauge.IEnergyInfoHandler;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.emi.MekanismEmiRecipeCategory;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ItemStackToEnergyEmiRecipe extends MekanismEmiHolderRecipe<ItemStackToEnergyRecipe> {

    public ItemStackToEnergyEmiRecipe(MekanismEmiRecipeCategory category, RecipeHolder<ItemStackToEnergyRecipe> recipeHolder) {
        super(category, recipeHolder);
        addInputDefinition(recipe.getInput());
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        IEnergyInfoHandler energyInfoHandler = getEnergyInfoHandler();
        addElement(widgetHolder, new GuiEnergyGauge(energyInfoHandler, GaugeType.STANDARD.with(DataType.OUTPUT), this, 133, 13));
        addSlot(widgetHolder, SlotType.INPUT, 26, 36, input(0));
        addConstantProgress(widgetHolder, ProgressType.LARGE_RIGHT, 64, 40);
    }

    private IEnergyInfoHandler getEnergyInfoHandler() {
        long[] outputDefinition = recipe.getOutputDefinition();
        if (outputDefinition.length > 1) {
            long maxEnergy = 0;
            for (long val : outputDefinition) {
                maxEnergy = Math.max(maxEnergy, val);
            }
            long finalMaxEnergy = maxEnergy;
            return new IEnergyInfoHandler() {
                @Override
                public long getEnergy() {
                    return RecipeViewerUtils.getCurrent(outputDefinition);
                }

                @Override
                public long getMaxEnergy() {
                    return finalMaxEnergy;
                }
            };
        }
        long energy = outputDefinition.length == 0 ? 0L : outputDefinition[0];
        return new IEnergyInfoHandler() {
            @Override
            public long getEnergy() {
                return energy;
            }

            @Override
            public long getMaxEnergy() {
                return energy;
            }
        };
    }
}