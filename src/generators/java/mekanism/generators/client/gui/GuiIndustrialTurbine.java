package mekanism.generators.client.gui;

import java.util.Arrays;
import mekanism.api.TileNetworkList;
import mekanism.client.gui.GuiEmbeddedGaugeTile;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiGasMode;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.bar.GuiVerticalRateBar;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.TileEntityGasTank.GasMode;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import mekanism.generators.client.gui.element.GuiTurbineTab;
import mekanism.generators.client.gui.element.GuiTurbineTab.TurbineTab;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.turbine.TurbineUpdateProtocol;
import mekanism.generators.common.inventory.container.turbine.TurbineContainer;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiIndustrialTurbine extends GuiEmbeddedGaugeTile<TileEntityTurbineCasing, TurbineContainer> {

    public GuiIndustrialTurbine(TurbineContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        ResourceLocation resource = getGuiLocation();
        addButton(new GuiTurbineTab(this, tile, TurbineTab.STAT, resource));
        addButton(new GuiVerticalPowerBar(this, tile, resource, 164, 16));
        addButton(new GuiVerticalRateBar(this, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return TextComponentUtil.build(Translation.of("gui.mekanism.steamInput"),
                      ": " + (tile.structure == null ? 0 : tile.structure.lastSteamInput) + " mB/t");
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
        }, resource, 40, 13));
        addButton(new GuiEnergyInfo(() -> {
            double producing = tile.structure == null ? 0 : tile.structure.clientFlow * (MekanismConfig.general.maxEnergyPerSteam.get() / TurbineUpdateProtocol.MAX_BLADES) *
                                                            Math.min(tile.structure.blades, tile.structure.coils * MekanismGeneratorsConfig.generators.turbineBladesPerCoil.get());
            return Arrays.asList(TextComponentUtil.build(Translation.of("gui.mekanism.storing"), ": ", EnergyDisplay.of(tile.getEnergy(), tile.getMaxEnergy())),
                  TextComponentUtil.build(Translation.of("gui.mekanism.producing"), ": ", EnergyDisplay.of(producing), "/t"));
        }, this, resource));
        addButton(new GuiGasMode(this, resource, 159, 72, true, () -> tile.structure == null ? GasMode.IDLE : tile.structure.dumpMode,
              () -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(0)))));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(TextComponentUtil.translate("container.inventory"), 8, (ySize - 96) + 4, 0x404040);
        drawString(tile.getName(), (xSize / 2) - (getStringWidth(tile.getName()) / 2), 5, 0x404040);
        if (tile.structure != null) {
            double energyMultiplier = (MekanismConfig.general.maxEnergyPerSteam.get() / TurbineUpdateProtocol.MAX_BLADES) *
                                      Math.min(tile.structure.blades, tile.structure.coils * MekanismGeneratorsConfig.generators.turbineBladesPerCoil.get());
            double rate = tile.structure.lowerVolume * (tile.structure.clientDispersers * MekanismGeneratorsConfig.generators.turbineDisperserGasFlow.get());
            rate = Math.min(rate, tile.structure.vents * MekanismGeneratorsConfig.generators.turbineVentGasFlow.get());
            renderScaledText(TextComponentUtil.build(Translation.of("gui.mekanism.production"), ": ",
                  EnergyDisplay.of(tile.structure.clientFlow * energyMultiplier)), 53, 26, 0x00CD00, 106);
            renderScaledText(TextComponentUtil.build(Translation.of("gui.mekanism.flow_rate"), ": " + tile.structure.clientFlow + " mB/t"), 53, 35, 0x00CD00, 106);
            renderScaledText(TextComponentUtil.build(Translation.of("gui.mekanism.capacity"), ": " + tile.structure.getFluidCapacity() + " mB"), 53, 44, 0x00CD00, 106);
            renderScaledText(TextComponentUtil.build(Translation.of("gui.mekanism.max_flow"), ": " + rate + " mB/t"), 53, 53, 0x00CD00, 106);
            int xAxis = mouseX - guiLeft;
            int yAxis = mouseY - guiTop;
            //TODO: 1.14 Convert to GuiElement
            if (xAxis >= 7 && xAxis <= 39 && yAxis >= 14 && yAxis <= 72) {
                if (tile.structure.fluidStored.isEmpty()) {
                    displayTooltip(TextComponentUtil.translate("gui.mekanism.empty"), xAxis, yAxis);
                } else {
                    displayTooltip(TextComponentUtil.build(tile.structure.fluidStored, ": " + tile.structure.fluidStored.getAmount() + "mB"), xAxis, yAxis);
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
        return getGuiLocation();
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "industrial_turbine.png");
    }
}