package mekanism.generators.client.gui;

import mekanism.api.text.EnumColor;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextUtils;
import mekanism.generators.client.gui.element.GuiFusionReactorTab;
import mekanism.generators.client.gui.element.GuiFusionReactorTab.FusionReactorTab;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.content.fusion.FusionReactorMultiblockData;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorController;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiFusionReactorStats extends GuiFusionReactorInfo {

    public GuiFusionReactorStats(EmptyTileContainer<TileEntityFusionReactorController> container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiFusionReactorTab(this, tile, FusionReactorTab.HEAT));
        addRenderableWidget(new GuiFusionReactorTab(this, tile, FusionReactorTab.FUEL));
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        FusionReactorMultiblockData multiblock = tile.getMultiblock();
        if (multiblock.isFormed()) {
            int textArea = getXSize() - 8;
            drawScrollingString(guiGraphics,GeneratorsLang.REACTOR_PASSIVE.translateColored(EnumColor.DARK_GREEN),
                  0, 26, TextAlignment.LEFT, titleTextColor(), 6, false);
            drawScrollingString(guiGraphics, GeneratorsLang.REACTOR_MIN_INJECTION.translate(multiblock.getMinInjectionRate(false)),
                  8, 36, TextAlignment.LEFT, titleTextColor(), textArea, 6, false);
            drawScrollingString(guiGraphics, GeneratorsLang.REACTOR_IGNITION.translate(MekanismUtils.getTemperatureDisplay(multiblock.getIgnitionTemperature(false), TemperatureUnit.KELVIN, true)),
                  8, 46, TextAlignment.LEFT, titleTextColor(), textArea, 6, false);
            drawScrollingString(guiGraphics, GeneratorsLang.REACTOR_MAX_PLASMA.translate(MekanismUtils.getTemperatureDisplay(multiblock.getMaxPlasmaTemperature(false), TemperatureUnit.KELVIN, true)),
                  8, 56, TextAlignment.LEFT, titleTextColor(), textArea, 6, false);
            drawScrollingString(guiGraphics, GeneratorsLang.REACTOR_MAX_CASING.translate(MekanismUtils.getTemperatureDisplay(multiblock.getMaxCasingTemperature(false), TemperatureUnit.KELVIN, true)),
                  8, 66, TextAlignment.LEFT, titleTextColor(), textArea, 6, false);
            drawScrollingString(guiGraphics, GeneratorsLang.REACTOR_PASSIVE_RATE.translate(EnergyDisplay.of(multiblock.getPassiveGeneration(false, false))),
                  8, 76, TextAlignment.LEFT, titleTextColor(), textArea, 6, false);

            drawScrollingString(guiGraphics,GeneratorsLang.REACTOR_ACTIVE.translateColored(EnumColor.DARK_BLUE),
                  0, 92, TextAlignment.LEFT, titleTextColor(), 6, false);
            drawScrollingString(guiGraphics, GeneratorsLang.REACTOR_MIN_INJECTION.translate(multiblock.getMinInjectionRate(true)),
                  8, 102, TextAlignment.LEFT, titleTextColor(), textArea, 6, false);
            drawScrollingString(guiGraphics, GeneratorsLang.REACTOR_IGNITION.translate(MekanismUtils.getTemperatureDisplay(multiblock.getIgnitionTemperature(true), TemperatureUnit.KELVIN, true)),
                  8, 112, TextAlignment.LEFT, titleTextColor(), textArea, 6, false);
            drawScrollingString(guiGraphics, GeneratorsLang.REACTOR_MAX_PLASMA.translate(MekanismUtils.getTemperatureDisplay(multiblock.getMaxPlasmaTemperature(true), TemperatureUnit.KELVIN, true)),
                  8, 122, TextAlignment.LEFT, titleTextColor(), textArea, 6, false);
            drawScrollingString(guiGraphics, GeneratorsLang.REACTOR_MAX_CASING.translate(MekanismUtils.getTemperatureDisplay(multiblock.getMaxCasingTemperature(true), TemperatureUnit.KELVIN, true)),
                  8, 132, TextAlignment.LEFT, titleTextColor(), textArea, 6, false);
            drawScrollingString(guiGraphics, GeneratorsLang.REACTOR_PASSIVE_RATE.translate(EnergyDisplay.of(multiblock.getPassiveGeneration(true, false))),
                  8, 142, TextAlignment.LEFT, titleTextColor(), textArea, 6, false);
            drawScrollingString(guiGraphics, GeneratorsLang.REACTOR_STEAM_PRODUCTION.translate(TextUtils.format(multiblock.getSteamPerTick(false))),
                  8, 152, TextAlignment.LEFT, titleTextColor(), textArea, 6, false);
        }
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }
}