package mekanism.client.gui.machine;

import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.bar.GuiHorizontalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.tile.machine.TileEntityChemicalDissolutionChamber;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiChemicalDissolutionChamber extends GuiConfigurableTile<TileEntityChemicalDissolutionChamber, MekanismTileContainer<TileEntityChemicalDissolutionChamber>> {

    private GuiElement inputGauge;

    public GuiChemicalDissolutionChamber(MekanismTileContainer<TileEntityChemicalDissolutionChamber> container, Inventory inv, Component title) {
        super(container, inv, title);
        dynamicSlots = true;
        titleLabelY = 4;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiHorizontalPowerBar(this, tile.getEnergyContainer(), 115, 75))
              .warning(WarningType.NOT_ENOUGH_ENERGY, tile.getWarningCheck(RecipeError.NOT_ENOUGH_ENERGY))
              .warning(WarningType.NOT_ENOUGH_ENERGY_REDUCED_RATE, tile.getWarningCheck(RecipeError.NOT_ENOUGH_ENERGY_REDUCED_RATE));
        addRenderableWidget(new GuiEnergyTab(this, tile.getEnergyContainer(), tile::getActive));
        inputGauge = addRenderableWidget(new GuiChemicalGauge(() -> tile.injectTank, () -> tile.getChemicalTanks(null), GaugeType.STANDARD, this, 7, 4))
              .warning(WarningType.NO_MATCHING_RECIPE, tile.getWarningCheck(RecipeError.NOT_ENOUGH_SECONDARY_INPUT));
        addRenderableWidget(new GuiChemicalGauge(() -> tile.outputTank, () -> tile.getChemicalTanks(null), GaugeType.STANDARD, this, 131, 13))
              .warning(WarningType.NO_SPACE_IN_OUTPUT, tile.getWarningCheck(RecipeError.NOT_ENOUGH_OUTPUT_SPACE));
        addRenderableWidget(new GuiProgress(tile::getScaledProgress, ProgressType.LARGE_RIGHT, this, 64, 40).recipeViewerCategory(tile))
              .warning(WarningType.INPUT_DOESNT_PRODUCE_OUTPUT, tile.getWarningCheck(RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT));
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleTextWithOffset(guiGraphics, inputGauge.getRelativeRight());
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }
}