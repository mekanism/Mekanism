package mekanism.client.gui;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.gas.GasStack;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.ContainerSalinationController;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntitySalinationController;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSalinationController extends GuiMekanism
{
	public TileEntitySalinationController tileEntity;

	public GuiSalinationController(InventoryPlayer inventory, TileEntitySalinationController tentity)
	{
		super(tentity, new ContainerSalinationController(inventory, tentity));
		tileEntity = tentity;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		fontRendererObj.drawString(MekanismUtils.localize("container.inventory"), 8, (ySize - 96) + 4, 0x404040);
		fontRendererObj.drawString(tileEntity.getInventoryName(), (xSize/2)-(fontRendererObj.getStringWidth(tileEntity.getInventoryName())/2), 6, 0x404040);

		fontRendererObj.drawString(getStruct(), 50, 21, 0x00CD00);
		fontRendererObj.drawString("Height: " + tileEntity.height, 50, 30, 0x00CD00);
		fontRendererObj.drawString("Mult: " + getTempMult(), 50, 39, 0x00CD00);
		fontRendererObj.drawString("Max: " + getMaxTemp(), 50, 48, 0x00CD00);

		if(xAxis >= 7 && xAxis <= 23 && yAxis >= 14 && yAxis <= 72)
		{
			drawCreativeTabHoveringText(tileEntity.waterTank.getFluid() != null ? tileEntity.waterTank.getFluid().getFluid().getLocalizedName() + ": " + tileEntity.waterTank.getFluidAmount() : MekanismUtils.localize("gui.empty"), xAxis, yAxis);
		}

		if(xAxis >= 153 && xAxis <= 169 && yAxis >= 14 && yAxis <= 72)
		{
			drawCreativeTabHoveringText(tileEntity.brineTank.getFluid() != null ? tileEntity.brineTank.getFluid().getFluid().getLocalizedName() + ": " + tileEntity.brineTank.getFluidAmount() : MekanismUtils.localize("gui.empty"), xAxis, yAxis);
		}

		if(xAxis >= 49 && xAxis <= 127 && yAxis >= 64 && yAxis <= 72)
		{
			drawCreativeTabHoveringText(getTemp(), xAxis, yAxis);
		}

		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	private String getStruct()
	{
		if(tileEntity.structured)
		{
			return "Structured";
		}
		else if(!tileEntity.structured)
		{
			if(tileEntity.controllerConflict)
			{
				return "Conflict";
			}
			else {
				return "Incomplete";
			}
		}

		return null;
	}

	private String getTemp()
	{
		float temp = (float)Math.round((tileEntity.getTemperature()*200)*10)/10F;

		return temp + " F";
	}

	private String getMaxTemp()
	{
		float temp = (float)Math.round((tileEntity.getMaxTemperature()*200)*10)/10F;

		return temp + " F";
	}

	private String getTempMult()
	{
		float temp = (float)Math.round((tileEntity.getTempMultiplier())*10)/10F;

		return temp + "x";
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);

		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiSalinationController.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);

		int xAxis = mouseX - guiWidth;
		int yAxis = mouseY - guiHeight;

		int displayInt;

		if(tileEntity.getScaledWaterLevel(58) > 0)
		{
			displayGauge(7, 14, tileEntity.getScaledWaterLevel(58), tileEntity.waterTank.getFluid(), null);
		}

		if(tileEntity.getScaledBrineLevel(58) > 0)
		{
			displayGauge(153, 14, tileEntity.getScaledBrineLevel(58), tileEntity.brineTank.getFluid(), null);
		}

		displayInt = tileEntity.getScaledTempLevel(78);
		drawTexturedModalRect(guiWidth + 49, guiHeight + 64, 176, 59, displayInt, 8);
	}

	public void displayGauge(int xPos, int yPos, int scale, FluidStack fluid, GasStack gas)
	{
		if(fluid == null && gas == null)
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

			if(fluid != null)
			{
				drawTexturedModelRectFromIcon(guiWidth + xPos, guiHeight + yPos + 58 - renderRemaining - start, fluid.getFluid().getIcon(), 16, 16 - (16 - renderRemaining));
			}
			else if(gas != null)
			{
				drawTexturedModelRectFromIcon(guiWidth + xPos, guiHeight + yPos + 58 - renderRemaining - start, gas.getGas().getIcon(), 16, 16 - (16 - renderRemaining));
			}

			start+=16;

			if(renderRemaining == 0 || scale == 0)
			{
				break;
			}
		}

		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiSalinationController.png"));
		drawTexturedModalRect(guiWidth + xPos, guiHeight + yPos, 176, 0, 16, 59);
	}
}
