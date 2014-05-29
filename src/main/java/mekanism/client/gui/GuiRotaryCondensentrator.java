package mekanism.client.gui;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.gas.GasTank;
import mekanism.client.gui.GuiFluidGauge.IFluidInfoHandler;
import mekanism.client.gui.GuiGasGauge.IGasInfoHandler;
import mekanism.client.gui.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.GuiProgress.ProgressBar;
import mekanism.client.gui.GuiSlot.SlotOverlay;
import mekanism.client.gui.GuiSlot.SlotType;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.ContainerRotaryCondensentrator;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.TileEntityRotaryCondensentrator;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fluids.FluidTank;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiRotaryCondensentrator extends GuiMekanism
{
	public TileEntityRotaryCondensentrator tileEntity;

	public GuiRotaryCondensentrator(InventoryPlayer inventory, TileEntityRotaryCondensentrator tentity)
	{
		super(new ContainerRotaryCondensentrator(inventory, tentity));
		tileEntity = tentity;

		guiElements.add(new GuiRedstoneControl(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiRotaryCondensentrator.png")));
		guiElements.add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiRotaryCondensentrator.png"), 4, 24).with(SlotOverlay.PLUS));
		guiElements.add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiRotaryCondensentrator.png"), 4, 55).with(SlotOverlay.MINUS));

		guiElements.add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiRotaryCondensentrator.png"), 154, 24));
		guiElements.add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiRotaryCondensentrator.png"), 154, 55));

		guiElements.add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiRotaryCondensentrator.png"), 154, 4).with(SlotOverlay.POWER));

		guiElements.add(new GuiFluidGauge(new IFluidInfoHandler() {
			@Override
			public FluidTank getTank()
			{
				return tileEntity.fluidTank;
			}
		}, GuiGauge.Type.STANDARD, this, MekanismUtils.getResource(ResourceType.GUI, "GuiRotaryCondensentrator.png"), 133, 13));
		guiElements.add(new GuiGasGauge(new IGasInfoHandler() {
			@Override
			public GasTank getTank()
			{
				return tileEntity.gasTank;
			}
		}, GuiGauge.Type.STANDARD, this, MekanismUtils.getResource(ResourceType.GUI, "GuiRotaryCondensentrator.png"), 25, 13));

		guiElements.add(new GuiProgress(new IProgressInfoHandler()
		{
			@Override
			public double getProgress()
			{
				return tileEntity.isActive ? 1 : 0;
			}

			@Override
			public boolean isActive()
			{
				return tileEntity.mode == 0;
			}
		}, ProgressBar.LARGE_RIGHT, this, MekanismUtils.getResource(ResourceType.GUI, "GuiRotaryCondensentrator.png"), 62, 38));
		guiElements.add(new GuiProgress(new IProgressInfoHandler()
		{
			@Override
			public double getProgress()
			{
				return tileEntity.isActive ? 1 : 0;
			}

			@Override
			public boolean isActive()
			{
				return tileEntity.mode == 1;
			}
		}, ProgressBar.LARGE_LEFT, this, MekanismUtils.getResource(ResourceType.GUI, "GuiRotaryCondensentrator.png"), 62, 38));

	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		fontRendererObj.drawString(tileEntity.getInventoryName(), 26, 4, 0x404040);
		fontRendererObj.drawString(tileEntity.mode == 0 ? MekanismUtils.localize("gui.condensentrating") : MekanismUtils.localize("gui.decondensentrating"), 6, (ySize - 94) + 2, 0x404040);

		if(xAxis >= 116 && xAxis <= 168 && yAxis >= 76 && yAxis <= 80)
		{
			drawCreativeTabHoveringText(MekanismUtils.getEnergyDisplay(tileEntity.getEnergy()), xAxis, yAxis);
		}

		if(xAxis >= 4 && xAxis <= 22 && yAxis >= 4 && yAxis <= 22)
		{
			drawCreativeTabHoveringText(MekanismUtils.localize("gui.rotaryCondensentrator.toggleOperation"), xAxis, yAxis);
		}

		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiRotaryCondensentrator.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
		int displayInt;

		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		displayInt = tileEntity.getScaledEnergyLevel(52);
		drawTexturedModalRect(guiWidth + 116, guiHeight + 76, 176, 36, displayInt, 4);

		if(xAxis >= 4 && xAxis <= 22 && yAxis >= 4 && yAxis <= 22)
		{
			drawTexturedModalRect(guiWidth + 4, guiHeight + 4, 176, 0, 18, 18);
		}
		else {
			drawTexturedModalRect(guiWidth + 4, guiHeight + 4, 176, 18, 18, 18);
		}

		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button)
	{
		super.mouseClicked(mouseX, mouseY, button);

		if(button == 0)
		{
			int xAxis = (mouseX - (width - xSize) / 2);
			int yAxis = (mouseY - (height - ySize) / 2);

			if(xAxis >= 4 && xAxis <= 22 && yAxis >= 4 && yAxis <= 22)
			{
				ArrayList data = new ArrayList();
				data.add(0);

				Mekanism.packetPipeline.sendToServer(new PacketTileEntity(Coord4D.get(tileEntity), data));
                playClickSound();
			}
		}
	}
}
