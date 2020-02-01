package mekanism.generators.client.gui;

import java.util.Arrays;
import java.util.Collections;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGauge.Type;
import mekanism.client.gui.element.gauge.GuiNumberGauge;
import mekanism.client.gui.element.gauge.GuiNumberGauge.INumberInfoHandler;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.common.MekanismLang;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.generators.client.gui.element.GuiReactorTab;
import mekanism.generators.client.gui.element.GuiReactorTab.ReactorTab;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.inventory.container.reactor.info.ReactorHeatContainer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiReactorHeat extends GuiReactorInfo<ReactorHeatContainer> {

    public GuiReactorHeat(ReactorHeatContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        ResourceLocation resource = getGuiLocation();
        addButton(new GuiEnergyInfo(() -> tile.isFormed() ? Arrays.asList(MekanismLang.STORING.translate(EnergyDisplay.of(tile.getEnergy(), tile.getMaxEnergy())),
              GeneratorsLang.PRODUCING_AMOUNT.translate(EnergyDisplay.of(tile.getReactor().getPassiveGeneration(false, true)))) : Collections.emptyList(),
              this, resource));
        addButton(new GuiNumberGauge(new INumberInfoHandler() {
            @Override
            public TextureAtlasSprite getIcon() {
                return MekanismRenderer.getBaseFluidTexture(Fluids.LAVA, FluidType.STILL);
            }

            @Override
            public double getLevel() {
                return TemperatureUnit.AMBIENT.convertToK(tile.getPlasmaTemp(), true);
            }

            @Override
            public double getMaxLevel() {
                return 5E8;
            }

            @Override
            public ITextComponent getText(double level) {
                return GeneratorsLang.REACTOR_PLASMA.translate(MekanismUtils.getTemperatureDisplay(level, TemperatureUnit.KELVIN));
            }
        }, Type.STANDARD, this, resource, 7, 50));
        addButton(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return tile.getPlasmaTemp() > tile.getCaseTemp() ? 1 : 0;
            }
        }, ProgressBar.SMALL_RIGHT, this, resource, 27, 75));
        addButton(new GuiNumberGauge(new INumberInfoHandler() {
            @Override
            public TextureAtlasSprite getIcon() {
                return MekanismRenderer.getBaseFluidTexture(Fluids.LAVA, FluidType.STILL);
            }

            @Override
            public double getLevel() {
                return TemperatureUnit.AMBIENT.convertToK(tile.getCaseTemp(), true);
            }

            @Override
            public double getMaxLevel() {
                return 5E8;
            }

            @Override
            public ITextComponent getText(double level) {
                return GeneratorsLang.REACTOR_CASE.translate(MekanismUtils.getTemperatureDisplay(level, TemperatureUnit.KELVIN));
            }
        }, Type.STANDARD, this, resource, 61, 50));
        addButton(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return tile.getCaseTemp() > 0 ? 1 : 0;
            }
        }, ProgressBar.SMALL_RIGHT, this, resource, 81, 60));
        addButton(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return (tile.getCaseTemp() > 0 && !tile.waterTank.isEmpty() && tile.steamTank.getFluidAmount() < tile.steamTank.getCapacity()) ? 1 : 0;
            }
        }, ProgressBar.SMALL_RIGHT, this, resource, 81, 90));
        addButton(new GuiFluidGauge(() -> tile.waterTank, Type.SMALL, this, resource, 115, 84));
        addButton(new GuiFluidGauge(() -> tile.steamTank, Type.SMALL, this, resource, 151, 84));
        addButton(new GuiEnergyGauge(() -> tile, Type.SMALL, this, resource, 115, 46));
        addButton(new GuiReactorTab(this, tile, ReactorTab.FUEL, resource));
        addButton(new GuiReactorTab(this, tile, ReactorTab.STAT, resource));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        drawString(tile.getName(), 46, 6, 0x404040);
    }
}