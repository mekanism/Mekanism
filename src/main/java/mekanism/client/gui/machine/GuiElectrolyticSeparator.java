package mekanism.client.gui.machine;

import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.button.GuiGasMode;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.tile.machine.TileEntityElectrolyticSeparator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiElectrolyticSeparator extends GuiConfigurableTile<TileEntityElectrolyticSeparator, MekanismTileContainer<TileEntityElectrolyticSeparator>> {

    private GuiElement fluidGauge;

    public GuiElectrolyticSeparator(MekanismTileContainer<TileEntityElectrolyticSeparator> container, Inventory inv, Component title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiEnergyTab(this, tile.getEnergyContainer(), tile::getEnergyUsed));
        fluidGauge = addRenderableWidget(new GuiFluidGauge(() -> tile.fluidTank, () -> tile.getFluidTanks(null), GaugeType.STANDARD, this, 5, 10))
              .warning(WarningType.NO_MATCHING_RECIPE, tile.getWarningCheck(RecipeError.NOT_ENOUGH_INPUT));
        addRenderableWidget(new GuiChemicalGauge(() -> tile.leftTank, () -> tile.getChemicalTanks(null), GaugeType.SMALL, this, 58, 18))
              .warning(WarningType.NO_SPACE_IN_OUTPUT, tile.getWarningCheck(TileEntityElectrolyticSeparator.NOT_ENOUGH_SPACE_LEFT_OUTPUT_ERROR));
        addRenderableWidget(new GuiChemicalGauge(() -> tile.rightTank, () -> tile.getChemicalTanks(null), GaugeType.SMALL, this, 100, 18))
              .warning(WarningType.NO_SPACE_IN_OUTPUT, tile.getWarningCheck(TileEntityElectrolyticSeparator.NOT_ENOUGH_SPACE_RIGHT_OUTPUT_ERROR));
        addRenderableWidget(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 164, 15))
              .warning(WarningType.NOT_ENOUGH_ENERGY, tile.getWarningCheck(RecipeError.NOT_ENOUGH_ENERGY))
              .warning(WarningType.NOT_ENOUGH_ENERGY_REDUCED_RATE, tile.getWarningCheck(RecipeError.NOT_ENOUGH_ENERGY_REDUCED_RATE));
        addRenderableWidget(new GuiProgress(tile::getActive, ProgressType.BI, this, 80, 30).recipeViewerCategory(tile))
              .warning(WarningType.INPUT_DOESNT_PRODUCE_OUTPUT, tile.getWarningCheck(RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT));
        addRenderableWidget(new GuiGasMode(this, 7, 72, false, () -> tile.dumpLeft, tile.getBlockPos(), 0));
        addRenderableWidget(new GuiGasMode(this, 159, 72, true, () -> tile.dumpRight, tile.getBlockPos(), 1));
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleTextWithOffset(guiGraphics, fluidGauge.getRelativeRight());
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }
}