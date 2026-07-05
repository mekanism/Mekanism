package mekanism.client.recipe_viewer.emi.recipe;

import dev.emi.emi.api.widget.WidgetHolder;
import java.util.Arrays;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.gauge.GuiEnergyGauge.IEnergyInfoHandler;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.emi.MekanismEmiRecipeCategory;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.util.context.ContextMap;
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
        //TODO - Emi: ContextMap
        int[] outputDefinition = recipe.getOutputDefinition(ContextMap.EMPTY);
        if (outputDefinition.length > 1) {
            int maxEnergy = Arrays.stream(outputDefinition).max().getAsInt();
            return new IEnergyInfoHandler() {
                @Override
                public long getEnergy() {
                    return RecipeViewerUtils.getCurrent(outputDefinition);
                }

                @Override
                public long getMaxEnergy() {
                    return maxEnergy;
                }
            };
        }
        int energy = outputDefinition.length == 0 ? 0 : outputDefinition[0];
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