package mekanism.generators.client.gui;

import java.util.Arrays;
import java.util.Collections;
import mekanism.api.math.FloatingLong;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.bar.GuiVerticalRateBar;
import mekanism.client.gui.element.button.GuiGasMode;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.TileEntityGasTank.GasMode;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.generators.client.gui.element.GuiTurbineTab;
import mekanism.generators.client.gui.element.GuiTurbineTab.TurbineTab;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.turbine.TurbineUpdateProtocol;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiIndustrialTurbine extends GuiMekanismTile<TileEntityTurbineCasing, MekanismTileContainer<TileEntityTurbineCasing>> {

    public GuiIndustrialTurbine(MekanismTileContainer<TileEntityTurbineCasing> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiInnerScreen(this, 50, 23, 112, 41));
        addButton(new GuiTurbineTab(this, tile, TurbineTab.STAT));
        addButton(new GuiVerticalPowerBar(this, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                if (tile.structure == null) {
                    return EnergyDisplay.ZERO.getTextComponent();
                }
                return EnergyDisplay.of(tile.structure.energyContainer.getEnergy(), tile.structure.energyContainer.getMaxEnergy()).getTextComponent();
            }

            @Override
            public double getLevel() {
                if (tile.structure == null) {
                    return 1;
                }
                return tile.structure.energyContainer.getEnergy().divideToLevel(tile.structure.energyContainer.getMaxEnergy());
            }
        }, 164, 16));
        addButton(new GuiVerticalRateBar(this, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return GeneratorsLang.TURBINE_STEAM_INPUT_RATE.translate(tile.structure == null ? 0 : tile.structure.lastSteamInput);
            }

            @Override
            public double getLevel() {
                if (tile.structure == null) {
                    return 0;
                }
                double rate = Math.min(tile.structure.lowerVolume * tile.structure.clientDispersers * MekanismGeneratorsConfig.generators.turbineDisperserGasFlow.get(),
                      tile.structure.vents * MekanismGeneratorsConfig.generators.turbineVentGasFlow.get());
                if (rate == 0) {
                    return 0;
                }
                return tile.structure.lastSteamInput / rate;
            }
        }, 40, 13));
        addButton(new GuiGasGauge(() -> tile.structure == null ? null : tile.structure.gasTank,
              () -> tile.structure == null ? Collections.emptyList() : tile.structure.getGasTanks(null), GaugeType.MEDIUM, this, 6, 13));
        addButton(new GuiEnergyInfo(() -> {
            EnergyDisplay storing;
            EnergyDisplay producing;
            if (tile.structure == null) {
                storing = EnergyDisplay.ZERO;
                producing = EnergyDisplay.ZERO;
            } else {
                storing = EnergyDisplay.of(tile.structure.energyContainer.getEnergy(), tile.structure.energyContainer.getMaxEnergy());
                producing = EnergyDisplay.of(MekanismConfig.general.maxEnergyPerSteam.get().divide(TurbineUpdateProtocol.MAX_BLADES)
                      .multiply(tile.structure.clientFlow * Math.min(tile.structure.blades,
                            tile.structure.coils * MekanismGeneratorsConfig.generators.turbineBladesPerCoil.get())));
            }
            return Arrays.asList(MekanismLang.STORING.translate(storing), GeneratorsLang.PRODUCING_AMOUNT.translate(producing));
        }, this));
        addButton(new GuiGasMode(this, getGuiLeft() + 159, getGuiTop() + 72, true, () -> tile.structure == null ? GasMode.IDLE : tile.structure.dumpMode,
              tile.getPos(), 0));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 4, titleTextColor());
        drawString(tile.getName(), (getXSize() / 2) - (getStringWidth(tile.getName()) / 2), 5, titleTextColor());
        if (tile.structure != null) {
            FloatingLong energyMultiplier = MekanismConfig.general.maxEnergyPerSteam.get().divide(TurbineUpdateProtocol.MAX_BLADES)
                  .multiply(Math.min(tile.structure.blades, tile.structure.coils * MekanismGeneratorsConfig.generators.turbineBladesPerCoil.get()));
            double rate = tile.structure.lowerVolume * (tile.structure.clientDispersers * MekanismGeneratorsConfig.generators.turbineDisperserGasFlow.get());
            rate = Math.min(rate, tile.structure.vents * MekanismGeneratorsConfig.generators.turbineVentGasFlow.get());
            renderScaledText(GeneratorsLang.TURBINE_PRODUCTION_AMOUNT.translate(EnergyDisplay.of(energyMultiplier.multiply(tile.structure.clientFlow))),
                  53, 26, screenTextColor(), 106);
            renderScaledText(GeneratorsLang.TURBINE_FLOW_RATE.translate(tile.structure.clientFlow), 53, 35, screenTextColor(), 106);
            renderScaledText(GeneratorsLang.TURBINE_CAPACITY.translate(tile.structure.getSteamCapacity()), 53, 44, screenTextColor(), 106);
            renderScaledText(GeneratorsLang.TURBINE_MAX_FLOW.translate(rate), 53, 53, screenTextColor(), 106);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}