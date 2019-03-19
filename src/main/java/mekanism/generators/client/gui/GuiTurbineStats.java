package mekanism.generators.client.gui;

import java.util.Arrays;
import mekanism.api.EnumColor;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.common.config.MekanismConfig.general;
import mekanism.common.config.MekanismConfig.generators;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.gui.element.GuiTurbineTab;
import mekanism.generators.client.gui.element.GuiTurbineTab.TurbineTab;
import mekanism.generators.common.content.turbine.TurbineUpdateProtocol;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiTurbineStats extends GuiMekanismTile<TileEntityTurbineCasing> {

    public GuiTurbineStats(InventoryPlayer inventory, TileEntityTurbineCasing tile) {
        super(tile, new ContainerNull(inventory.player, tile));
        ResourceLocation resource = getGuiLocation();
        addGuiElement(new GuiTurbineTab(this, tileEntity, TurbineTab.MAIN, 6, resource));
        addGuiElement(new GuiEnergyInfo(() -> {
            double energyMultiplier = (general.maxEnergyPerSteam / TurbineUpdateProtocol.MAX_BLADES) * Math
                  .min(tileEntity.structure.blades, tileEntity.structure.coils * generators.turbineBladesPerCoil);
            return Arrays.asList(
                  LangUtils.localize("gui.storing") + ": " + MekanismUtils
                        .getEnergyDisplay(tileEntity.getEnergy(), tileEntity.getMaxEnergy()),
                  LangUtils.localize("gui.producing") + ": " + MekanismUtils
                        .getEnergyDisplay(tileEntity.structure.clientFlow * energyMultiplier) + "/t");
        }, this, resource));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String stats = LangUtils.localize("gui.turbineStats");
        String limiting = EnumColor.DARK_RED + " (" + LangUtils.localize("gui.limiting") + ")";
        fontRenderer.drawString(stats, (xSize / 2) - (fontRenderer.getStringWidth(stats) / 2), 6, 0x404040);
        fontRenderer.drawString(LangUtils.localize("gui.tankVolume") + ": " + tileEntity.structure.lowerVolume, 8, 26,
              0x404040);
        boolean dispersersLimiting = tileEntity.structure.lowerVolume * tileEntity.structure.clientDispersers
              * generators.turbineDisperserGasFlow <
              tileEntity.structure.vents * generators.turbineVentGasFlow;
        boolean ventsLimiting = tileEntity.structure.lowerVolume * tileEntity.structure.clientDispersers
              * generators.turbineDisperserGasFlow >
              tileEntity.structure.vents * generators.turbineVentGasFlow;
        fontRenderer.drawString(LangUtils.localize("gui.steamFlow"), 8, 40, 0x797979);
        fontRenderer.drawString(
              LangUtils.localize("gui.dispersers") + ": " + tileEntity.structure.clientDispersers + (dispersersLimiting
                    ? limiting : ""), 14, 49, 0x404040);
        fontRenderer.drawString(
              LangUtils.localize("gui.vents") + ": " + tileEntity.structure.vents + (ventsLimiting ? limiting : ""), 14,
              58, 0x404040);
        boolean bladesLimiting = tileEntity.structure.coils * 4 > tileEntity.structure.blades;
        boolean coilsLimiting = tileEntity.structure.coils * 4 < tileEntity.structure.blades;
        fontRenderer.drawString(LangUtils.localize("gui.production"), 8, 72, 0x797979);
        fontRenderer.drawString(
              LangUtils.localize("gui.blades") + ": " + tileEntity.structure.blades + (bladesLimiting ? limiting : ""),
              14, 81, 0x404040);
        fontRenderer.drawString(
              LangUtils.localize("gui.coils") + ": " + tileEntity.structure.coils + (coilsLimiting ? limiting : ""), 14,
              90, 0x404040);
        double energyMultiplier = (general.maxEnergyPerSteam / TurbineUpdateProtocol.MAX_BLADES) * Math
              .min(tileEntity.structure.blades, tileEntity.structure.coils * generators.turbineBladesPerCoil);
        double rate = tileEntity.structure.lowerVolume * (tileEntity.structure.clientDispersers
              * generators.turbineDisperserGasFlow);
        rate = Math.min(rate, tileEntity.structure.vents * generators.turbineVentGasFlow);
        fontRenderer.drawString(
              LangUtils.localize("gui.maxProduction") + ": " + MekanismUtils.getEnergyDisplay(rate * energyMultiplier),
              8, 104, 0x404040);
        fontRenderer.drawString(LangUtils.localize("gui.maxWaterOutput") + ": "
              + tileEntity.structure.condensers * generators.condenserRate + " mB/t", 8, 113, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        mc.renderEngine.bindTexture(getGuiLocation());
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiNull.png");
    }
}