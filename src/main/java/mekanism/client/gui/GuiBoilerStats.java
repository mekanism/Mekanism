package mekanism.client.gui;

import java.util.List;

import mekanism.api.MekanismConfig.general;
import mekanism.api.util.ListUtils;
import mekanism.api.util.UnitDisplayUtils;
import mekanism.api.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.client.gui.element.GuiBoilerTab;
import mekanism.client.gui.element.GuiBoilerTab.BoilerTab;
import mekanism.client.gui.element.GuiElement.IInfoHandler;
import mekanism.client.gui.element.GuiHeatInfo;
import mekanism.common.content.boiler.BoilerUpdateProtocol;
import mekanism.common.content.boiler.SynchronizedBoilerData;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.tile.TileEntityBoilerCasing;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiBoilerStats extends GuiMekanism
{
	public TileEntityBoilerCasing tileEntity;

	public GuiBoilerStats(InventoryPlayer inventory, TileEntityBoilerCasing tentity)
	{
		super(tentity, new ContainerNull(inventory.player, tentity));
		tileEntity = tentity;
		guiElements.add(new GuiBoilerTab(this, tileEntity, BoilerTab.MAIN, 6, MekanismUtils.getResource(ResourceType.GUI, "GuiNull.png")));
		guiElements.add(new GuiHeatInfo(new IInfoHandler() {
			@Override
			public List<String> getInfo()
			{
				String loss = UnitDisplayUtils.getDisplayShort(tileEntity.structure.lastEnvironmentLoss, TemperatureUnit.KELVIN);
				return ListUtils.asList(LangUtils.localize("gui.dissipated") + ": " + loss + "/t");
			}
		}, this, MekanismUtils.getResource(ResourceType.GUI, "GuiNull.png")));
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		String stats = LangUtils.localize("gui.boilerStats");
		
		fontRendererObj.drawString(stats, (xSize/2)-(fontRendererObj.getStringWidth(stats)/2), 6, 0x404040);
		
		fontRendererObj.drawString(LangUtils.localize("gui.maxWater") + ": " + tileEntity.structure.waterVolume*BoilerUpdateProtocol.WATER_PER_TANK, 8, 26, 0x404040);
		fontRendererObj.drawString(LangUtils.localize("gui.maxSteam") + ": " + tileEntity.structure.steamVolume*BoilerUpdateProtocol.STEAM_PER_TANK, 8, 35, 0x404040);
		
		fontRendererObj.drawString(LangUtils.localize("gui.heatTransfer"), 8, 49, 0x797979);
		fontRendererObj.drawString(LangUtils.localize("gui.superheaters") + ": " + tileEntity.structure.superheatingElements, 14, 58, 0x404040);
		
		int boilCapacity = (int)((tileEntity.structure.superheatingElements*general.superheatingHeatTransfer)/SynchronizedBoilerData.getHeatEnthalpy());
		fontRendererObj.drawString(LangUtils.localize("gui.boilCapacity") + ": " + boilCapacity + " mB/t", 8, 72, 0x404040);
		
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
