package mekanism.generators.client.gui;

import java.util.List;

import mekanism.api.MekanismConfig.generators;
import mekanism.api.util.ListUtils;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiElement.IInfoHandler;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiPowerBar;
import mekanism.client.gui.element.GuiRateBar;
import mekanism.client.gui.element.GuiRateBar.IRateInfoHandler;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.inventory.container.ContainerFilter;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.gui.element.GuiTurbineTab;
import mekanism.generators.client.gui.element.GuiTurbineTab.TurbineTab;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiIndustrialTurbine extends GuiMekanism
{
	public TileEntityTurbineCasing tileEntity;

	public GuiIndustrialTurbine(InventoryPlayer inventory, TileEntityTurbineCasing tentity)
	{
		super(tentity, new ContainerFilter(inventory, tentity));
		tileEntity = tentity;
		guiElements.add(new GuiTurbineTab(this, tileEntity, TurbineTab.STAT, 6, MekanismUtils.getResource(ResourceType.GUI, "GuiIndustrialTurbine.png")));
		guiElements.add(new GuiPowerBar(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiIndustrialTurbine.png"), 164, 16));
		guiElements.add(new GuiRateBar(this, new IRateInfoHandler()
		{
			@Override
			public String getTooltip()
			{
				return LangUtils.localize("gui.steamInput") + ": " + tileEntity.structure.lastSteamInput + " mB/t";
			}
			
			@Override
			public double getLevel()
			{
				double rate = tileEntity.structure.lowerVolume*(tileEntity.structure.clientDispersers*generators.turbineDisperserGasFlow);		
				rate = Math.min(rate, tileEntity.structure.vents*generators.turbineVentGasFlow);
				
				return (double)tileEntity.structure.lastSteamInput/rate;
			}
		}, MekanismUtils.getResource(ResourceType.GUI, "GuiIndustrialTurbine.png"), 40, 13));
		guiElements.add(new GuiEnergyInfo(new IInfoHandler()
		{
			@Override
			public List<String> getInfo()
			{
				double energyMultiplier = generators.turbineBaseEnergyPerSteam*Math.min(tileEntity.structure.blades, tileEntity.structure.coils*generators.turbineBladesPerCoil);
				
				return ListUtils.asList(
						LangUtils.localize("gui.storing") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getEnergy()),
						LangUtils.localize("gui.producing") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.structure.clientFlow*energyMultiplier) + "/t");
			}
		}, this, MekanismUtils.getResource(ResourceType.GUI, "GuiIndustrialTurbine.png")));
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		fontRendererObj.drawString(LangUtils.localize("container.inventory"), 8, (ySize - 96) + 4, 0x404040);
		fontRendererObj.drawString(tileEntity.getInventoryName(), (xSize/2)-(fontRendererObj.getStringWidth(tileEntity.getInventoryName())/2), 5, 0x404040);
		
		double energyMultiplier = generators.turbineBaseEnergyPerSteam*Math.min(tileEntity.structure.blades, tileEntity.structure.coils*generators.turbineBladesPerCoil);
		
		double rate = tileEntity.structure.lowerVolume*(tileEntity.structure.clientDispersers*generators.turbineDisperserGasFlow);		
		rate = Math.min(rate, tileEntity.structure.vents*generators.turbineVentGasFlow);
		
		renderScaledText(LangUtils.localize("gui.production") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.structure.clientFlow*energyMultiplier), 53, 26, 0x00CD00, 106);
		renderScaledText(LangUtils.localize("gui.flowRate") + ": " + tileEntity.structure.clientFlow + " mB/t", 53, 35, 0x00CD00, 106);
		renderScaledText(LangUtils.localize("gui.capacity") + ": " + tileEntity.structure.getFluidCapacity() + " mB", 53, 44, 0x00CD00, 106);
		renderScaledText(LangUtils.localize("gui.maxFlow") + ": " + rate + " mB/t", 53, 53, 0x00CD00, 106);
		
		if(xAxis >= 7 && xAxis <= 39 && yAxis >= 14 && yAxis <= 72)
		{
			drawCreativeTabHoveringText(tileEntity.structure.fluidStored != null ? LangUtils.localizeFluidStack(tileEntity.structure.fluidStored) + ": " + tileEntity.structure.fluidStored.amount + "mB" : LangUtils.localize("gui.empty"), xAxis, yAxis);
		}
		
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiIndustrialTurbine.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
		
		if(tileEntity.getScaledFluidLevel(58) > 0)
		{
			displayGauge(7, 14, tileEntity.getScaledFluidLevel(58), tileEntity.structure.fluidStored, 0);
			displayGauge(23, 14, tileEntity.getScaledFluidLevel(58), tileEntity.structure.fluidStored, 1);
		}
		
		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
	}
	
	public void displayGauge(int xPos, int yPos, int scale, FluidStack fluid, int side /*0-left, 1-right*/)
	{
		if(fluid == null)
		{
			return;
		}

		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;

		int start = 0;

		while(true)
		{
			int renderRemaining = 0;

			if(scale > 16)
			{
				renderRemaining = 16;
				scale -= 16;
			}
			else {
				renderRemaining = scale;
				scale = 0;
			}

			mc.renderEngine.bindTexture(MekanismRenderer.getBlocksTexture());
			drawTexturedModelRectFromIcon(guiWidth + xPos, guiHeight + yPos + 58 - renderRemaining - start, fluid.getFluid().getIcon(), 16, 16 - (16 - renderRemaining));
			start+=16;

			if(renderRemaining == 0 || scale == 0)
			{
				break;
			}
		}

		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiIndustrialTurbine.png"));
		drawTexturedModalRect(guiWidth + xPos, guiHeight + yPos, 176, side == 0 ? 0 : 54, 16, 54);
	}
}
