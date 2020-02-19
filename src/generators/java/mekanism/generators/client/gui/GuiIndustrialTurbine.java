package mekanism.generators.client.gui;

import java.util.Arrays;
import mekanism.api.TileNetworkList;
import mekanism.client.gui.GuiEmbeddedGaugeTile;
import mekanism.client.gui.button.GuiGasMode;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.bar.GuiVerticalRateBar;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.TileEntityGasTank.GasMode;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.generators.client.gui.element.GuiTurbineTab;
import mekanism.generators.client.gui.element.GuiTurbineTab.TurbineTab;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.turbine.TurbineUpdateProtocol;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiIndustrialTurbine extends GuiEmbeddedGaugeTile<TileEntityTurbineCasing, MekanismTileContainer<TileEntityTurbineCasing>> {

    public GuiIndustrialTurbine(MekanismTileContainer<TileEntityTurbineCasing> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiInnerScreen(this, 50, 23, 112, 41));
        addButton(new GuiTurbineTab(this, tile, TurbineTab.STAT));
        addButton(new GuiVerticalPowerBar(this, tile, 164, 16));
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
                return (double) tile.structure.lastSteamInput / rate;
            }
        }, 40, 13));
        addButton(new GuiEnergyInfo(() -> {
            double producing = tile.structure == null ? 0 : tile.structure.clientFlow * (MekanismConfig.general.maxEnergyPerSteam.get() / TurbineUpdateProtocol.MAX_BLADES) *
                                                            Math.min(tile.structure.blades, tile.structure.coils * MekanismGeneratorsConfig.generators.turbineBladesPerCoil.get());
            return Arrays.asList(MekanismLang.STORING.translate(EnergyDisplay.of(tile.getEnergy(), tile.getMaxEnergy())),
                  GeneratorsLang.PRODUCING_AMOUNT.translate(EnergyDisplay.of(producing)));
        }, this));
        addButton(new GuiGasMode(this, getGuiLeft() + 159, getGuiTop() + 72, true, () -> tile.structure == null ? GasMode.IDLE : tile.structure.dumpMode,
              () -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(0)))));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 4, 0x404040);
        drawString(tile.getName(), (getXSize() / 2) - (getStringWidth(tile.getName()) / 2), 5, 0x404040);
        if (tile.structure != null) {
            double energyMultiplier = (MekanismConfig.general.maxEnergyPerSteam.get() / TurbineUpdateProtocol.MAX_BLADES) *
                                      Math.min(tile.structure.blades, tile.structure.coils * MekanismGeneratorsConfig.generators.turbineBladesPerCoil.get());
            double rate = tile.structure.lowerVolume * (tile.structure.clientDispersers * MekanismGeneratorsConfig.generators.turbineDisperserGasFlow.get());
            rate = Math.min(rate, tile.structure.vents * MekanismGeneratorsConfig.generators.turbineVentGasFlow.get());
            renderScaledText(GeneratorsLang.TURBINE_PRODUCTION_AMOUNT.translate(EnergyDisplay.of(tile.structure.clientFlow * energyMultiplier)),
                  53, 26, 0x00CD00, 106);
            renderScaledText(GeneratorsLang.TURBINE_FLOW_RATE.translate(tile.structure.clientFlow), 53, 35, 0x00CD00, 106);
            renderScaledText(GeneratorsLang.TURBINE_CAPACITY.translate(tile.structure.getFluidCapacity()), 53, 44, 0x00CD00, 106);
            renderScaledText(GeneratorsLang.TURBINE_MAX_FLOW.translate(rate), 53, 53, 0x00CD00, 106);
            int xAxis = mouseX - getGuiLeft();
            int yAxis = mouseY - getGuiTop();
            //TODO: Convert to GuiElement
            if (xAxis >= 7 && xAxis <= 39 && yAxis >= 14 && yAxis <= 72) {
                if (tile.structure.fluidStored.isEmpty()) {
                    displayTooltip(MekanismLang.EMPTY.translate(), xAxis, yAxis);
                } else {
                    displayTooltip(MekanismLang.GENERIC_STORED_MB.translate(tile.structure.fluidStored, tile.structure.fluidStored.getAmount()), xAxis, yAxis);
                }
            }
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        if (tile.structure != null) {
            int scaledFluidLevel = tile.getScaledFluidLevel(58);
            if (scaledFluidLevel > 0) {
                displayGauge(7, 14, scaledFluidLevel, tile.structure.fluidStored, 0);
                displayGauge(23, 14, scaledFluidLevel, tile.structure.fluidStored, 1);
            }
        }
    }

    @Override
    protected ResourceLocation getGaugeResource() {
        return MekanismGenerators.rl("gui/industrial_turbine.png");
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismGenerators.rl("gui/industrial_turbine.png");
    }
}