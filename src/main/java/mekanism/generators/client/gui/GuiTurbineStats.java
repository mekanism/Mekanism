package mekanism.generators.client.gui;

import mekanism.api.EnumColor;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.util.LangUtils;
import mekanism.common.util.ListUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.gui.element.GuiTurbineTab;
import mekanism.generators.client.gui.element.GuiTurbineTab.TurbineTab;
import mekanism.generators.common.content.turbine.TurbineUpdateProtocol;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiTurbineStats extends GuiMekanism
{
	public TileEntityTurbineCasing tileEntity;

	public GuiTurbineStats(InventoryPlayer inventory, TileEntityTurbineCasing tentity)
	{
		super(tentity, new ContainerNull(inventory.player, tentity));
		tileEntity = tentity;
		guiElements.add(new GuiTurbineTab(this, tileEntity, TurbineTab.MAIN, 6, MekanismUtils.getResource(ResourceType.GUI, "GuiNull.png")));
		guiElements.add(new GuiEnergyInfo(() ->
        {
            double energyMultiplier = (MekanismConfig.current().general.maxEnergyPerSteam.val()/TurbineUpdateProtocol.MAX_BLADES)*Math.min(tileEntity.structure.blades, tileEntity.structure.coils* MekanismConfig.current().generators.turbineBladesPerCoil.val());

            return ListUtils.asList(
                    LangUtils.localize("gui.storing") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getEnergy(), tileEntity.getMaxEnergy()),
                    LangUtils.localize("gui.producing") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.structure.clientFlow*energyMultiplier) + "/t");
        }, this, MekanismUtils.getResource(ResourceType.GUI, "GuiNull.png")));
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		String stats = LangUtils.localize("gui.turbineStats");
		String limiting = EnumColor.DARK_RED + " (" + LangUtils.localize("gui.limiting") + ")";
		
		fontRenderer.drawString(stats, (xSize/2)-(fontRenderer.getStringWidth(stats)/2), 6, 0x404040);
		
		fontRenderer.drawString(LangUtils.localize("gui.tankVolume") + ": " + tileEntity.structure.lowerVolume, 8, 26, 0x404040);
		
		boolean dispersersLimiting = tileEntity.structure.lowerVolume*tileEntity.structure.clientDispersers* MekanismConfig.current().generators.turbineDisperserGasFlow.val() <
				tileEntity.structure.vents* MekanismConfig.current().generators.turbineVentGasFlow.val();
		boolean ventsLimiting = tileEntity.structure.lowerVolume*tileEntity.structure.clientDispersers* MekanismConfig.current().generators.turbineDisperserGasFlow.val() >
				tileEntity.structure.vents* MekanismConfig.current().generators.turbineVentGasFlow.val();
		
		fontRenderer.drawString(LangUtils.localize("gui.steamFlow"), 8, 40, 0x797979);
		fontRenderer.drawString(LangUtils.localize("gui.dispersers") + ": " + tileEntity.structure.clientDispersers + (dispersersLimiting ? limiting : ""), 14, 49, 0x404040);
		fontRenderer.drawString(LangUtils.localize("gui.vents") + ": " + tileEntity.structure.vents + (ventsLimiting ? limiting : ""), 14, 58, 0x404040);
		
		boolean bladesLimiting = tileEntity.structure.coils*4 > tileEntity.structure.blades;
		boolean coilsLimiting = tileEntity.structure.coils*4 < tileEntity.structure.blades;
		
		fontRenderer.drawString(LangUtils.localize("gui.production"), 8, 72, 0x797979);
		fontRenderer.drawString(LangUtils.localize("gui.blades") + ": " + tileEntity.structure.blades + (bladesLimiting ? limiting : ""), 14, 81, 0x404040);
		fontRenderer.drawString(LangUtils.localize("gui.coils") + ": " + tileEntity.structure.coils + (coilsLimiting ? limiting : ""), 14, 90, 0x404040);
		
		double energyMultiplier = (MekanismConfig.current().general.maxEnergyPerSteam.val()/TurbineUpdateProtocol.MAX_BLADES)*Math.min(tileEntity.structure.blades, tileEntity.structure.coils* MekanismConfig.current().generators.turbineBladesPerCoil.val());
		double rate = tileEntity.structure.lowerVolume*(tileEntity.structure.clientDispersers* MekanismConfig.current().generators.turbineDisperserGasFlow.val());
		rate = Math.min(rate, tileEntity.structure.vents* MekanismConfig.current().generators.turbineVentGasFlow.val());
		
		fontRenderer.drawString(LangUtils.localize("gui.maxProduction") + ": " + MekanismUtils.getEnergyDisplay(rate*energyMultiplier), 8, 104, 0x404040);
		fontRenderer.drawString(LangUtils.localize("gui.maxWaterOutput") + ": " + tileEntity.structure.condensers* MekanismConfig.current().generators.condenserRate.val() + " mB/t", 8, 113, 0x404040);
		
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiNull.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
		
		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
	}
}
