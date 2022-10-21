package mekanism.generators.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
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
    protected void drawForegroundText(@NotNull PoseStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        FusionReactorMultiblockData multiblock = tile.getMultiblock();
        if (multiblock.isFormed()) {
            drawString(matrix, GeneratorsLang.REACTOR_PASSIVE.translateColored(EnumColor.DARK_GREEN), 6, 26, titleTextColor());
            drawTextScaledBound(matrix, GeneratorsLang.REACTOR_MIN_INJECTION.translate(multiblock.getMinInjectionRate(false)), 16, 36, titleTextColor(), 156);
            drawTextScaledBound(matrix, GeneratorsLang.REACTOR_IGNITION.translate(MekanismUtils.getTemperatureDisplay(multiblock.getIgnitionTemperature(false),
                  TemperatureUnit.KELVIN, true)), 16, 46, titleTextColor(), 156);
            drawTextScaledBound(matrix, GeneratorsLang.REACTOR_MAX_PLASMA.translate(MekanismUtils.getTemperatureDisplay(multiblock.getMaxPlasmaTemperature(false),
                  TemperatureUnit.KELVIN, true)), 16, 56, titleTextColor(), 156);
            drawTextScaledBound(matrix, GeneratorsLang.REACTOR_MAX_CASING.translate(MekanismUtils.getTemperatureDisplay(multiblock.getMaxCasingTemperature(false),
                  TemperatureUnit.KELVIN, true)), 16, 66, titleTextColor(), 156);
            drawTextScaledBound(matrix, GeneratorsLang.REACTOR_PASSIVE_RATE.translate(EnergyDisplay.of(multiblock.getPassiveGeneration(false, false))),
                  16, 76, titleTextColor(), 156);

            drawString(matrix, GeneratorsLang.REACTOR_ACTIVE.translateColored(EnumColor.DARK_BLUE), 6, 92, titleTextColor());
            drawTextScaledBound(matrix, GeneratorsLang.REACTOR_MIN_INJECTION.translate(multiblock.getMinInjectionRate(true)), 16, 102, titleTextColor(), 156);
            drawTextScaledBound(matrix, GeneratorsLang.REACTOR_IGNITION.translate(MekanismUtils.getTemperatureDisplay(multiblock.getIgnitionTemperature(true),
                  TemperatureUnit.KELVIN, true)), 16, 112, titleTextColor(), 156);
            drawTextScaledBound(matrix, GeneratorsLang.REACTOR_MAX_PLASMA.translate(MekanismUtils.getTemperatureDisplay(multiblock.getMaxPlasmaTemperature(true),
                  TemperatureUnit.KELVIN, true)), 16, 122, titleTextColor(), 156);
            drawTextScaledBound(matrix, GeneratorsLang.REACTOR_MAX_CASING.translate(MekanismUtils.getTemperatureDisplay(multiblock.getMaxCasingTemperature(true),
                  TemperatureUnit.KELVIN, true)), 16, 132, titleTextColor(), 156);
            drawTextScaledBound(matrix, GeneratorsLang.REACTOR_PASSIVE_RATE.translate(EnergyDisplay.of(multiblock.getPassiveGeneration(true, false))),
                  16, 142, titleTextColor(), 156);
            drawTextScaledBound(matrix, GeneratorsLang.REACTOR_STEAM_PRODUCTION.translate(TextUtils.format(multiblock.getSteamPerTick(false))),
                  16, 152, titleTextColor(), 156);
        }
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}