package mekanism.generators.client.gui;

import java.io.IOException;

import mekanism.api.Coord4D;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiPowerBar;
import mekanism.client.gui.element.GuiRateBar;
import mekanism.client.gui.element.GuiRateBar.IRateInfoHandler;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.base.TileNetworkList;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.ContainerFilter;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityGasTank;
import mekanism.common.util.LangUtils;
import mekanism.common.util.ListUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.gui.element.GuiTurbineTab;
import mekanism.generators.client.gui.element.GuiTurbineTab.TurbineTab;
import mekanism.generators.common.content.turbine.TurbineUpdateProtocol;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

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
				double rate = tileEntity.structure.lowerVolume*(tileEntity.structure.clientDispersers* MekanismConfig.current().generators.turbineDisperserGasFlow.val());
				rate = Math.min(rate, tileEntity.structure.vents* MekanismConfig.current().generators.turbineVentGasFlow.val());
				
				if(rate == 0)
				{
					return 0;
				}
				
				return (double)tileEntity.structure.lastSteamInput/rate;
			}
		}, MekanismUtils.getResource(ResourceType.GUI, "GuiIndustrialTurbine.png"), 40, 13));
		guiElements.add(new GuiEnergyInfo(() ->
        {
            double energyMultiplier = (MekanismConfig.current().general.maxEnergyPerSteam.val()/TurbineUpdateProtocol.MAX_BLADES)*Math.min(tileEntity.structure.blades, tileEntity.structure.coils* MekanismConfig.current().generators.turbineBladesPerCoil.val());

            return ListUtils.asList(
                    LangUtils.localize("gui.storing") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getEnergy(), tileEntity.getMaxEnergy()),
                    LangUtils.localize("gui.producing") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.structure.clientFlow*energyMultiplier) + "/t");
        }, this, MekanismUtils.getResource(ResourceType.GUI, "GuiIndustrialTurbine.png")));
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		fontRenderer.drawString(LangUtils.localize("container.inventory"), 8, (ySize - 96) + 4, 0x404040);
		fontRenderer.drawString(tileEntity.getName(), (xSize/2)-(fontRenderer.getStringWidth(tileEntity.getName())/2), 5, 0x404040);
		
		double energyMultiplier = (MekanismConfig.current().general.maxEnergyPerSteam.val()/TurbineUpdateProtocol.MAX_BLADES)*Math.min(tileEntity.structure.blades, tileEntity.structure.coils* MekanismConfig.current().generators.turbineBladesPerCoil.val());
		
		double rate = tileEntity.structure.lowerVolume*(tileEntity.structure.clientDispersers* MekanismConfig.current().generators.turbineDisperserGasFlow.val());
		rate = Math.min(rate, tileEntity.structure.vents* MekanismConfig.current().generators.turbineVentGasFlow.val());
		
		renderScaledText(LangUtils.localize("gui.production") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.structure.clientFlow*energyMultiplier), 53, 26, 0x00CD00, 106);
		renderScaledText(LangUtils.localize("gui.flowRate") + ": " + tileEntity.structure.clientFlow + " mB/t", 53, 35, 0x00CD00, 106);
		renderScaledText(LangUtils.localize("gui.capacity") + ": " + tileEntity.structure.getFluidCapacity() + " mB", 53, 44, 0x00CD00, 106);
		renderScaledText(LangUtils.localize("gui.maxFlow") + ": " + rate + " mB/t", 53, 53, 0x00CD00, 106);
		
		String name = chooseByMode(tileEntity.structure.dumpMode, LangUtils.localize("gui.idle"), LangUtils.localize("gui.dumping"), LangUtils.localize("gui.dumping_excess"));
		renderScaledText(name, 156-(int)(fontRenderer.getStringWidth(name)*getNeededScale(name, 66)), 73, 0x404040, 66);
		
		if(xAxis >= 7 && xAxis <= 39 && yAxis >= 14 && yAxis <= 72)
		{
			drawHoveringText(tileEntity.structure.fluidStored != null ? LangUtils.localizeFluidStack(tileEntity.structure.fluidStored) + ": " + tileEntity.structure.fluidStored.amount + "mB" : LangUtils.localize("gui.empty"), xAxis, yAxis);
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
		
		int displayInt = chooseByMode(tileEntity.structure.dumpMode, 142, 150, 158);
		drawTexturedModalRect(guiWidth + 160, guiHeight + 73, 176, displayInt, 8, 8);
		
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
			int renderRemaining;

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
			drawTexturedModalRect(guiWidth + xPos, guiHeight + yPos + 58 - renderRemaining - start, MekanismRenderer.getFluidTexture(fluid, FluidType.STILL), 16, 16 - (16 - renderRemaining));
			start+=16;

			if(renderRemaining == 0 || scale == 0)
			{
				break;
			}
		}

		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiIndustrialTurbine.png"));
		drawTexturedModalRect(guiWidth + xPos, guiHeight + yPos, 176, side == 0 ? 0 : 54, 16, 54);
	}
	
	@Override
	protected void mouseClicked(int x, int y, int button) throws IOException
	{
		super.mouseClicked(x, y, button);

		int xAxis = (x - (width - xSize) / 2);
		int yAxis = (y - (height - ySize) / 2);

		if(xAxis > 160 && xAxis < 169 && yAxis > 73 && yAxis < 82)
		{
			TileNetworkList data = TileNetworkList.withContents((byte)0);

			Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
			SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
		}
	}
	
	private <T> T chooseByMode(TileEntityGasTank.GasMode dumping, T idleOption, T dumpingOption, T dumpingExcessOption)
	{
		if(dumping.equals(TileEntityGasTank.GasMode.IDLE))
		{
			return idleOption;
		}
		else if(dumping.equals(TileEntityGasTank.GasMode.DUMPING))
		{
			return dumpingOption;
		}
		else if(dumping.equals(TileEntityGasTank.GasMode.DUMPING_EXCESS))
		{
			return dumpingExcessOption;
		}
		
		return idleOption; //should not happen;
	}
}
