package mekanism.generators.client.gui;

import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiNumberGauge;
import mekanism.client.gui.element.gauge.GuiNumberGauge.INumberInfoHandler;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidTextureType;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.generators.client.gui.element.GuiFusionReactorTab;
import mekanism.generators.client.gui.element.GuiFusionReactorTab.FusionReactorTab;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.content.fusion.FusionReactorMultiblockData;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorController;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.Fluids;

public class GuiFusionReactorHeat extends GuiFusionReactorInfo {

    private static final double MAX_LEVEL = 500_000_000;

    public GuiFusionReactorHeat(EmptyTileContainer<TileEntityFusionReactorController> container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiNumberGauge(new INumberInfoHandler() {
            @Override
            public TextureAtlasSprite getIcon() {
                return MekanismRenderer.getBaseFluidTexture(Fluids.LAVA, FluidTextureType.STILL);
            }

            @Override
            public double getLevel() {
                return tile.getMultiblock().getLastPlasmaTemp();
            }

            @Override
            public double getScaledLevel() {
                return Math.min(1, getLevel() / MAX_LEVEL);
            }

            @Override
            public Component getText() {
                return GeneratorsLang.REACTOR_PLASMA.translate(MekanismUtils.getTemperatureDisplay(getLevel(), TemperatureUnit.KELVIN, true));
            }
        }, GaugeType.STANDARD, this, 12, 50));
        addRenderableWidget(new GuiProgress(() -> {
            FusionReactorMultiblockData multiblock = tile.getMultiblock();
            return multiblock.getLastPlasmaTemp() > multiblock.getLastCaseTemp();
        }, ProgressType.SMALL_RIGHT, this, 34, 76));
        addRenderableWidget(new GuiNumberGauge(new INumberInfoHandler() {
            @Override
            public TextureAtlasSprite getIcon() {
                return MekanismRenderer.getBaseFluidTexture(Fluids.LAVA, FluidTextureType.STILL);
            }

            @Override
            public double getLevel() {
                return tile.getMultiblock().getLastCaseTemp();
            }

            @Override
            public double getScaledLevel() {
                return Math.min(1, getLevel() / MAX_LEVEL);
            }

            @Override
            public Component getText() {
                return GeneratorsLang.REACTOR_CASE.translate(MekanismUtils.getTemperatureDisplay(getLevel(), TemperatureUnit.KELVIN, true));
            }
        }, GaugeType.STANDARD, this, 66, 50));
        addRenderableWidget(new GuiProgress(() -> tile.getMultiblock().getCaseTemp() > 0, ProgressType.SMALL_RIGHT, this, 88, 61));
        addRenderableWidget(new GuiProgress(() -> {
            FusionReactorMultiblockData multiblock = tile.getMultiblock();
            return multiblock.getCaseTemp() > 0 && !multiblock.waterTank.isEmpty() && multiblock.steamTank.getStored() < multiblock.steamTank.getCapacity();
        }, ProgressType.SMALL_RIGHT, this, 88, 91));
        addRenderableWidget(new GuiFluidGauge(() -> tile.getMultiblock().waterTank, () -> tile.getFluidTanks(null), GaugeType.SMALL, this, 120, 84));
        addRenderableWidget(new GuiChemicalGauge(() -> tile.getMultiblock().steamTank, () -> tile.getChemicalTanks(null), GaugeType.SMALL, this, 156, 84));
        addRenderableWidget(new GuiEnergyGauge(tile.getMultiblock().energyContainer, GaugeType.SMALL, this, 120, 46));
        addRenderableWidget(new GuiFusionReactorTab(this, tile, FusionReactorTab.FUEL));
        addRenderableWidget(new GuiFusionReactorTab(this, tile, FusionReactorTab.STAT));
    }
}