package mekanism.client.gui;

import java.util.List;

import mekanism.api.MekanismConfig.general;
import mekanism.api.util.ListUtils;
import mekanism.api.util.UnitDisplayUtils;
import mekanism.api.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.client.gui.element.GuiBoilerTab;
import mekanism.client.gui.element.GuiBoilerTab.BoilerTab;
import mekanism.client.gui.element.GuiElement.IInfoHandler;
import mekanism.client.gui.element.GuiGraph;
import mekanism.client.gui.element.GuiGraph.GraphDataHandler;
import mekanism.client.gui.element.GuiHeatInfo;
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
	
	public GuiGraph boilGraph;
	public GuiGraph maxGraph;

	public GuiBoilerStats(InventoryPlayer inventory, TileEntityBoilerCasing tentity)
	{
		super(tentity, new ContainerNull(inventory.player, tentity));
		tileEntity = tentity;
		guiElements.add(new GuiBoilerTab(this, tileEntity, BoilerTab.MAIN, 6, MekanismUtils.getResource(ResourceType.GUI, "GuiBoilerStats.png")));
		guiElements.add(new GuiHeatInfo(new IInfoHandler() {
			@Override
			public List<String> getInfo()
			{
				TemperatureUnit unit = TemperatureUnit.values()[general.tempUnit.ordinal()];
				String environment = UnitDisplayUtils.getDisplayShort(tileEntity.structure.lastEnvironmentLoss*unit.intervalSize, false, unit);
				return ListUtils.asList(LangUtils.localize("gui.dissipated") + ": " + environment + "/t");
			}
		}, this, MekanismUtils.getResource(ResourceType.GUI, "GuiBoilerStats.png")));
		guiElements.add(boilGraph = new GuiGraph(this, MekanismUtils.getResource(ResourceType.GUI, "GuiBoilerStats.png"), 8, 83, 160, 36, new GraphDataHandler() {
			@Override
			public String getDataDisplay(int data)
			{
				return LangUtils.localize("gui.boilRate") + ": " + data + " mB/t";
			}
		}));
		guiElements.add(maxGraph = new GuiGraph(this, MekanismUtils.getResource(ResourceType.GUI, "GuiBoilerStats.png"), 8, 122, 160, 36, new GraphDataHandler() {
			@Override
			public String getDataDisplay(int data)
			{
				return LangUtils.localize("gui.maxBoil") + ": " + data + " mB/t";
			}
		}));
		maxGraph.enableFixedScale((int)((tentity.structure.superheatingElements*general.superheatingHeatTransfer)/SynchronizedBoilerData.getHeatEnthalpy()));
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		String stats = LangUtils.localize("gui.boilerStats");
		
		fontRendererObj.drawString(stats, (xSize/2)-(fontRendererObj.getStringWidth(stats)/2), 6, 0x404040);
		
		fontRendererObj.drawString(LangUtils.localize("gui.maxWater") + ": " + tileEntity.clientWaterCapacity + " mB", 8, 26, 0x404040);
		fontRendererObj.drawString(LangUtils.localize("gui.maxSteam") + ": " + tileEntity.clientSteamCapacity + " mB", 8, 35, 0x404040);
		
		fontRendererObj.drawString(LangUtils.localize("gui.heatTransfer"), 8, 49, 0x797979);
		fontRendererObj.drawString(LangUtils.localize("gui.superheaters") + ": " + tileEntity.structure.superheatingElements, 14, 58, 0x404040);
		
		int boilCapacity = (int)((tileEntity.structure.superheatingElements*general.superheatingHeatTransfer)/SynchronizedBoilerData.getHeatEnthalpy());
		fontRendererObj.drawString(LangUtils.localize("gui.boilCapacity") + ": " + boilCapacity + " mB/t", 8, 72, 0x404040);
		
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}
	
	@Override
	public void updateScreen()
	{
		super.updateScreen();
		
		boilGraph.addData(tileEntity.structure.lastBoilRate);
		maxGraph.addData(tileEntity.structure.lastMaxBoil);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiBoilerStats.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
		
		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
	}
}
