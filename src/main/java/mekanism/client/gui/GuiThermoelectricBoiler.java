package mekanism.client.gui;

import java.util.Collections;
import mekanism.client.gui.element.GuiHeatInfo;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiVerticalRateBar;
import mekanism.client.gui.element.tab.GuiBoilerTab;
import mekanism.client.gui.element.tab.GuiBoilerTab.BoilerTab;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.boiler.SynchronizedBoilerData;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.TileEntityBoilerCasing;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;

public class GuiThermoelectricBoiler extends GuiEmbeddedGaugeTile<TileEntityBoilerCasing, MekanismTileContainer<TileEntityBoilerCasing>> {

    public GuiThermoelectricBoiler(MekanismTileContainer<TileEntityBoilerCasing> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiBoilerTab(this, tile, BoilerTab.STAT));
        addButton(new GuiVerticalRateBar(this, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return MekanismLang.BOIL_RATE.translate(tile.getLastBoilRate());
            }

            @Override
            public double getLevel() {
                return tile.structure == null ? 0 : (double) tile.getLastBoilRate() / (double) tile.structure.lastMaxBoil;
            }
        }, 24, 13));
        addButton(new GuiVerticalRateBar(this, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return MekanismLang.MAX_BOIL_RATE.translate(tile.getLastMaxBoil());
            }

            @Override
            public double getLevel() {
                return tile.structure == null ? 0 : tile.getLastMaxBoil() * SynchronizedBoilerData.getHeatEnthalpy() /
                                                    (tile.structure.superheatingElements * MekanismConfig.general.superheatingHeatTransfer.get());
            }
        }, 144, 13));
        addButton(new GuiHeatInfo(() -> {
            TemperatureUnit unit = EnumUtils.TEMPERATURE_UNITS[MekanismConfig.general.tempUnit.get().ordinal()];
            ITextComponent environment = UnitDisplayUtils.getDisplayShort(tile.getLastEnvironmentLoss() * unit.intervalSize, unit, false);
            return Collections.singletonList(MekanismLang.DISSIPATED_RATE.translate(environment));
        }, this));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 4, 0x404040);
        drawString(tile.getName(), (getXSize() / 2) - (getStringWidth(tile.getName()) / 2), 5, 0x404040);
        renderScaledText(MekanismLang.TEMPERATURE.translate(MekanismUtils.getTemperatureDisplay(tile.getTemperature(), TemperatureUnit.AMBIENT)), 43, 30, 0x00CD00, 90);
        renderScaledText(MekanismLang.BOIL_RATE.translate(tile.getLastBoilRate()), 43, 39, 0x00CD00, 90);
        renderScaledText(MekanismLang.MAX_BOIL_RATE.translate(tile.getLastMaxBoil()), 43, 48, 0x00CD00, 90);
        //TODO: Convert to GuiElement
        int xAxis = mouseX - getGuiLeft();
        int yAxis = mouseY - getGuiTop();
        if (xAxis >= 7 && xAxis <= 23 && yAxis >= 14 && yAxis <= 72) {
            FluidStack waterStored = tile.structure == null ? FluidStack.EMPTY : tile.structure.waterStored;
            if (waterStored.isEmpty()) {
                displayTooltip(MekanismLang.EMPTY.translate(), xAxis, yAxis);
            } else {
                displayTooltip(MekanismLang.GENERIC_STORED_MB.translate(waterStored, waterStored.getAmount()), xAxis, yAxis);
            }
        } else if (xAxis >= 153 && xAxis <= 169 && yAxis >= 14 && yAxis <= 72) {
            FluidStack steamStored = tile.structure == null ? FluidStack.EMPTY : tile.structure.steamStored;
            if (steamStored.isEmpty()) {
                displayTooltip(MekanismLang.EMPTY.translate(), xAxis, yAxis);
            } else {
                displayTooltip(MekanismLang.GENERIC_STORED_MB.translate(steamStored, steamStored.getAmount()), xAxis, yAxis);
            }
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        if (tile.structure != null) {
            if (tile.getScaledWaterLevel(58) > 0) {
                displayGauge(7, 14, tile.getScaledWaterLevel(58), tile.structure.waterStored, 0);
            }
            if (tile.getScaledSteamLevel(58) > 0) {
                displayGauge(153, 14, tile.getScaledSteamLevel(58), tile.structure.steamStored, 0);
            }
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "thermoelectric_boiler.png");
    }

    @Override
    protected ResourceLocation getGaugeResource() {
        //TODO: couldn't find it at a glance
        return MekanismUtils.getResource(ResourceType.GUI, "industrial_turbine.png");
    }
}