package mekanism.generators.client.gui;

import java.io.IOException;
import java.util.Arrays;
import mekanism.api.TileNetworkList;
import mekanism.client.gui.GuiEmbeddedGaugeTile;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiPowerBar;
import mekanism.client.gui.element.GuiRateBar;
import mekanism.client.gui.element.GuiRateBar.IRateInfoHandler;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.ContainerFilter;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.gas_tank.TileEntityGasTank.GasMode;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.gui.element.GuiTurbineTab;
import mekanism.generators.client.gui.element.GuiTurbineTab.TurbineTab;
import mekanism.generators.common.content.turbine.TurbineUpdateProtocol;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiIndustrialTurbine extends GuiEmbeddedGaugeTile<TileEntityTurbineCasing> {

    public GuiIndustrialTurbine(PlayerInventory inventory, TileEntityTurbineCasing tile) {
        super(tile, new ContainerFilter(inventory, tile));
        ResourceLocation resource = getGuiLocation();
        addGuiElement(new GuiTurbineTab(this, tileEntity, TurbineTab.STAT, resource));
        addGuiElement(new GuiPowerBar(this, tileEntity, resource, 164, 16));
        addGuiElement(new GuiRateBar(this, new IRateInfoHandler() {
            @Override
            public String getTooltip() {
                return LangUtils.localize("gui.steamInput") + ": " + (tileEntity.structure == null ? 0 : tileEntity.structure.lastSteamInput) + " mB/t";
            }

            @Override
            public double getLevel() {
                if (tileEntity.structure == null) {
                    return 0;
                }
                double rate = Math.min(tileEntity.structure.lowerVolume * tileEntity.structure.clientDispersers * MekanismConfig.current().generators.turbineDisperserGasFlow.val(),
                      tileEntity.structure.vents * MekanismConfig.current().generators.turbineVentGasFlow.val());
                if (rate == 0) {
                    return 0;
                }
                return (double) tileEntity.structure.lastSteamInput / rate;
            }
        }, resource, 40, 13));
        addGuiElement(new GuiEnergyInfo(() -> {
            double producing = tileEntity.structure == null ? 0 : tileEntity.structure.clientFlow * (MekanismConfig.current().general.maxEnergyPerSteam.val() / TurbineUpdateProtocol.MAX_BLADES) *
                                                                  Math.min(tileEntity.structure.blades, tileEntity.structure.coils * MekanismConfig.current().generators.turbineBladesPerCoil.val());
            return Arrays.asList(LangUtils.localize("gui.storing") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getEnergy(), tileEntity.getMaxEnergy()),
                  LangUtils.localize("gui.producing") + ": " + MekanismUtils.getEnergyDisplay(producing) + "/t");
        }, this, resource));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        font.drawString(LangUtils.localize("container.inventory"), 8, (ySize - 96) + 4, 0x404040);
        font.drawString(tileEntity.getName(), (xSize / 2) - (font.getStringWidth(tileEntity.getName()) / 2), 5, 0x404040);
        if (tileEntity.structure != null) {
            double energyMultiplier = (MekanismConfig.current().general.maxEnergyPerSteam.val() / TurbineUpdateProtocol.MAX_BLADES) *
                                      Math.min(tileEntity.structure.blades, tileEntity.structure.coils * MekanismConfig.current().generators.turbineBladesPerCoil.val());
            double rate = tileEntity.structure.lowerVolume * (tileEntity.structure.clientDispersers * MekanismConfig.current().generators.turbineDisperserGasFlow.val());
            rate = Math.min(rate, tileEntity.structure.vents * MekanismConfig.current().generators.turbineVentGasFlow.val());
            renderScaledText(LangUtils.localize("gui.production") + ": " +
                             MekanismUtils.getEnergyDisplay(tileEntity.structure.clientFlow * energyMultiplier), 53, 26, 0x00CD00, 106);
            renderScaledText(LangUtils.localize("gui.flowRate") + ": " + tileEntity.structure.clientFlow + " mB/t", 53, 35, 0x00CD00, 106);
            renderScaledText(LangUtils.localize("gui.capacity") + ": " + tileEntity.structure.getFluidCapacity() + " mB", 53, 44, 0x00CD00, 106);
            renderScaledText(LangUtils.localize("gui.maxFlow") + ": " + rate + " mB/t", 53, 53, 0x00CD00, 106);
            String name = LangUtils.localize(tileEntity.structure.dumpMode.getLangKey());
            renderScaledText(name, 156 - (int) (font.getStringWidth(name) * getNeededScale(name, 66)), 73, 0x404040, 66);
            int xAxis = mouseX - guiLeft;
            int yAxis = mouseY - guiTop;
            if (xAxis >= 7 && xAxis <= 39 && yAxis >= 14 && yAxis <= 72) {
                displayTooltip(tileEntity.structure.fluidStored != null
                               ? LangUtils.localizeFluidStack(tileEntity.structure.fluidStored) + ": " + tileEntity.structure.fluidStored.amount + "mB"
                               : LangUtils.localize("gui.empty"), xAxis, yAxis);
            }
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        if (tileEntity.structure != null) {
            int displayInt = GasMode.chooseByMode(tileEntity.structure.dumpMode, 142, 150, 158);
            drawTexturedModalRect(guiLeft + 160, guiTop + 73, 176, displayInt, 8, 8);
            int scaledFluidLevel = tileEntity.getScaledFluidLevel(58);
            if (scaledFluidLevel > 0) {
                displayGauge(7, 14, scaledFluidLevel, tileEntity.structure.fluidStored, 0);
                displayGauge(23, 14, scaledFluidLevel, tileEntity.structure.fluidStored, 1);
            }
        }
    }

    @Override
    protected ResourceLocation getGaugeResource() {
        return getGuiLocation();
    }

    @Override
    protected void mouseClicked(int x, int y, int button) throws IOException {
        super.mouseClicked(x, y, button);
        int xAxis = x - guiLeft;
        int yAxis = y - guiTop;
        if (xAxis > 160 && xAxis < 169 && yAxis > 73 && yAxis < 82) {
            TileNetworkList data = TileNetworkList.withContents((byte) 0);
            Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, data));
            SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiIndustrialTurbine.png");
    }
}