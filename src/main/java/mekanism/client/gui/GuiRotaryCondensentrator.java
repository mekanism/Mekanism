package mekanism.client.gui;

import java.io.IOException;

import mekanism.api.Coord4D;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiFluidGauge;
import mekanism.client.gui.element.GuiGasGauge;
import mekanism.client.gui.element.GuiGauge;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSecurityTab;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.GuiUpgradeTab;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.api.TileNetworkList;
import mekanism.common.inventory.container.ContainerRotaryCondensentrator;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityRotaryCondensentrator;
import mekanism.common.util.LangUtils;
import mekanism.common.util.ListUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiRotaryCondensentrator extends GuiMekanism
{
	public TileEntityRotaryCondensentrator tileEntity;

	public GuiRotaryCondensentrator(InventoryPlayer inventory, TileEntityRotaryCondensentrator tentity)
	{
		super(tentity, new ContainerRotaryCondensentrator(inventory, tentity));
		tileEntity = tentity;

		guiElements.add(new GuiSecurityTab(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiRotaryCondensentrator.png")));
		guiElements.add(new GuiRedstoneControl(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiRotaryCondensentrator.png")));
		guiElements.add(new GuiUpgradeTab(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiRotaryCondensentrator.png")));
		guiElements.add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiRotaryCondensentrator.png"), 4, 24).with(SlotOverlay.PLUS));
		guiElements.add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiRotaryCondensentrator.png"), 4, 55).with(SlotOverlay.MINUS));

		guiElements.add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiRotaryCondensentrator.png"), 154, 24));
		guiElements.add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiRotaryCondensentrator.png"), 154, 55));

		guiElements.add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiRotaryCondensentrator.png"), 154, 4).with(SlotOverlay.POWER));
		
		guiElements.add(new GuiEnergyInfo(() ->
		{
            String usage = MekanismUtils.getEnergyDisplay(tileEntity.clientEnergyUsed);
            return ListUtils.asList(LangUtils.localize("gui.using") + ": " + usage + "/t", LangUtils.localize("gui.needed") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getMaxEnergy()-tileEntity.getEnergy()));
        }, this, MekanismUtils.getResource(ResourceType.GUI, "GuiRotaryCondensentrator.png")));
		guiElements.add(new GuiFluidGauge(() -> tileEntity.fluidTank, GuiGauge.Type.STANDARD, this, MekanismUtils.getResource(ResourceType.GUI, "GuiRotaryCondensentrator.png"), 133, 13));
		guiElements.add(new GuiGasGauge(() -> tileEntity.gasTank, GuiGauge.Type.STANDARD, this, MekanismUtils.getResource(ResourceType.GUI, "GuiRotaryCondensentrator.png"), 25, 13));

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

		fontRenderer.drawString(tileEntity.getName(), (xSize/2)-(fontRenderer.getStringWidth(tileEntity.getName())/2), 4, 0x404040);
		fontRenderer.drawString(tileEntity.mode == 0 ? LangUtils.localize("gui.condensentrating") : LangUtils.localize("gui.decondensentrating"), 6, (ySize - 94) + 2, 0x404040);

		if(xAxis >= 116 && xAxis <= 168 && yAxis >= 76 && yAxis <= 80)
		{
			drawHoveringText(MekanismUtils.getEnergyDisplay(tileEntity.getEnergy(), tileEntity.getMaxEnergy()), xAxis, yAxis);
		}

		if(xAxis >= 4 && xAxis <= 22 && yAxis >= 4 && yAxis <= 22)
		{
			drawHoveringText(LangUtils.localize("gui.rotaryCondensentrator.toggleOperation"), xAxis, yAxis);
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
	protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, button);

		if(button == 0)
		{
			int xAxis = (mouseX - (width - xSize) / 2);
			int yAxis = (mouseY - (height - ySize) / 2);

			if(xAxis >= 4 && xAxis <= 22 && yAxis >= 4 && yAxis <= 22)
			{
				TileNetworkList data = TileNetworkList.withContents(0);

				Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
			}
		}
	}
}
