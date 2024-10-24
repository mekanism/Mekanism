package mekanism.client.gui.machine;

import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.GuiDownArrow;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.bar.GuiHorizontalPowerBar;
import mekanism.client.gui.element.button.ToggleButton;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.IProgressInfoHandler.IBooleanProgressInfoHandler;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.recipe_viewer.type.RecipeViewerRecipeType;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.machine.TileEntityRotaryCondensentrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiRotaryCondensentrator extends GuiConfigurableTile<TileEntityRotaryCondensentrator, MekanismTileContainer<TileEntityRotaryCondensentrator>> {

    private GuiElement energyBar;

    public GuiRotaryCondensentrator(MekanismTileContainer<TileEntityRotaryCondensentrator> container, Inventory inv, Component title) {
        super(container, inv, title);
        dynamicSlots = true;
        titleLabelY = 4;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiDownArrow(this, 159, 44));
        energyBar = addRenderableWidget(new GuiHorizontalPowerBar(this, tile.getEnergyContainer(), 115, 75))
              .warning(WarningType.NOT_ENOUGH_ENERGY, tile.getWarningCheck(RecipeError.NOT_ENOUGH_ENERGY))
              .warning(WarningType.NOT_ENOUGH_ENERGY_REDUCED_RATE, tile.getWarningCheck(RecipeError.NOT_ENOUGH_ENERGY_REDUCED_RATE));
        addRenderableWidget(new GuiEnergyTab(this, tile.getEnergyContainer(), tile::getEnergyUsed));
        addRenderableWidget(new GuiFluidGauge(() -> tile.fluidTank, () -> tile.getFluidTanks(null), GaugeType.STANDARD, this, 133, 13))
              .warning(WarningType.NO_MATCHING_RECIPE, tile.getWarningCheck(TileEntityRotaryCondensentrator.NOT_ENOUGH_FLUID_INPUT_ERROR))
              .warning(WarningType.NO_SPACE_IN_OUTPUT, tile.getWarningCheck(TileEntityRotaryCondensentrator.NOT_ENOUGH_SPACE_FLUID_OUTPUT_ERROR));
        addRenderableWidget(new GuiChemicalGauge(() -> tile.gasTank, () -> tile.getChemicalTanks(null), GaugeType.STANDARD, this, 25, 13))
              .warning(WarningType.NO_MATCHING_RECIPE, tile.getWarningCheck(TileEntityRotaryCondensentrator.NOT_ENOUGH_GAS_INPUT_ERROR))
              .warning(WarningType.NO_SPACE_IN_OUTPUT, tile.getWarningCheck(TileEntityRotaryCondensentrator.NOT_ENOUGH_SPACE_GAS_OUTPUT_ERROR));
        addRenderableWidget(new GuiProgress(new IBooleanProgressInfoHandler() {
            @Override
            public boolean fillProgressBar() {
                return tile.getActive();
            }

            @Override
            public boolean isActive() {
                return !tile.getMode();
            }
        }, ProgressType.LARGE_RIGHT, this, 64, 39).recipeViewerCategories(RecipeViewerRecipeType.CONDENSENTRATING))
              .warning(WarningType.INPUT_DOESNT_PRODUCE_OUTPUT, tile.getWarningCheck(RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT));
        addRenderableWidget(new GuiProgress(new IBooleanProgressInfoHandler() {
            @Override
            public boolean fillProgressBar() {
                return tile.getActive();
            }

            @Override
            public boolean isActive() {
                return tile.getMode();
            }
        }, ProgressType.LARGE_LEFT, this, 64, 39).recipeViewerCategories(RecipeViewerRecipeType.DECONDENSENTRATING))
              .warning(WarningType.INPUT_DOESNT_PRODUCE_OUTPUT, tile.getWarningCheck(RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT));
        addRenderableWidget(new ToggleButton(this, 4, 4, tile::getMode,
              (element, mouseX, mouseY) -> PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.NEXT_MODE, ((GuiRotaryCondensentrator) element.gui()).tile))))
              .setTooltip(MekanismLang.CONDENSENTRATOR_TOGGLE);
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        ILangEntry modeLang = tile.getMode() ? MekanismLang.DECONDENSENTRATING : MekanismLang.CONDENSENTRATING;
        drawScrollingString(guiGraphics, modeLang.translate(), 4, imageHeight - 92, TextAlignment.LEFT, titleTextColor(), energyBar.getRelativeX() - 4, 2, false);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }
}