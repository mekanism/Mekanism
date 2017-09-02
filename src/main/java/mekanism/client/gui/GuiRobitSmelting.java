package mekanism.client.gui;

import java.io.IOException;

import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.ContainerRobitSmelting;
import mekanism.common.network.PacketRobit.RobitMessage;
import mekanism.common.network.PacketRobit.RobitPacketType;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;

import org.lwjgl.opengl.GL11;

public class GuiRobitSmelting extends GuiMekanism
{
	public EntityRobit robit;

	public GuiRobitSmelting(InventoryPlayer inventory, EntityRobit entity)
	{
		super(new ContainerRobitSmelting(inventory, entity));
		robit = entity;
		xSize += 25;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		fontRenderer.drawString(LangUtils.localize("gui.robit.smelting"), 8, 6, 0x404040);
		fontRenderer.drawString(LangUtils.localize("container.inventory"), 8, ySize - 96 + 3, 0x404040);

		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiRobitSmelting.png"));
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);

		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		if(xAxis >= 179 && xAxis <= 197 && yAxis >= 10 && yAxis <= 28)
		{
			drawTexturedModalRect(guiWidth + 179, guiHeight + 10, 176 + 25, 0, 18, 18);
		}
		else {
			drawTexturedModalRect(guiWidth + 179, guiHeight + 10, 176 + 25, 18, 18, 18);
		}

		if(xAxis >= 179 && xAxis <= 197 && yAxis >= 30 && yAxis <= 48)
		{
			drawTexturedModalRect(guiWidth + 179, guiHeight + 30, 176 + 25, 36, 18, 18);
		}
		else {
			drawTexturedModalRect(guiWidth + 179, guiHeight + 30, 176 + 25, 54, 18, 18);
		}

		if(xAxis >= 179 && xAxis <= 197 && yAxis >= 50 && yAxis <= 68)
		{
			drawTexturedModalRect(guiWidth + 179, guiHeight + 50, 176 + 25, 72, 18, 18);
		}
		else {
			drawTexturedModalRect(guiWidth + 179, guiHeight + 50, 176 + 25, 90, 18, 18);
		}

		if(xAxis >= 179 && xAxis <= 197 && yAxis >= 70 && yAxis <= 88)
		{
			drawTexturedModalRect(guiWidth + 179, guiHeight + 70, 176 + 25, 108, 18, 18);
		}
		else {
			drawTexturedModalRect(guiWidth + 179, guiHeight + 70, 176 + 25, 126, 18, 18);
		}

		if(xAxis >= 179 && xAxis <= 197 && yAxis >= 90 && yAxis <= 108)
		{
			drawTexturedModalRect(guiWidth + 179, guiHeight + 90, 176 + 25, 144, 18, 18);
		}
		else {
			drawTexturedModalRect(guiWidth + 179, guiHeight + 90, 176 + 25, 162, 18, 18);
		}

		int displayInt;

		if(robit.furnaceBurnTime > 0)
		{
			displayInt = getBurnTimeRemainingScaled(12);
			drawTexturedModalRect(guiWidth + 56, guiHeight + 36 + 12 - displayInt, 176 + 25 + 18, 36 + 12 - displayInt, 14, displayInt + 2);
		}

		displayInt = getCookProgressScaled(24);
		drawTexturedModalRect(guiWidth + 79, guiHeight + 34, 176 + 25 + 18, 36 + 14, displayInt + 1, 16);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, button);

		if(button == 0)
		{
			int xAxis = (mouseX - (width - xSize) / 2);
			int yAxis = (mouseY - (height - ySize) / 2);

			if(xAxis >= 179 && xAxis <= 197 && yAxis >= 10 && yAxis <= 28)
			{
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
				Mekanism.packetHandler.sendToServer(new RobitMessage(RobitPacketType.GUI, 0, robit.getEntityId(), null));
				mc.player.openGui(Mekanism.instance, 21, mc.world, robit.getEntityId(), 0, 0);
			}
			else if(xAxis >= 179 && xAxis <= 197 && yAxis >= 30 && yAxis <= 48)
			{
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
				Mekanism.packetHandler.sendToServer(new RobitMessage(RobitPacketType.GUI, 1, robit.getEntityId(), null));
				mc.player.openGui(Mekanism.instance, 22, mc.world, robit.getEntityId(), 0, 0);
			}
			else if(xAxis >= 179 && xAxis <= 197 && yAxis >= 50 && yAxis <= 68)
			{
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
				Mekanism.packetHandler.sendToServer(new RobitMessage(RobitPacketType.GUI, 2, robit.getEntityId(), null));
				mc.player.openGui(Mekanism.instance, 23, mc.world, robit.getEntityId(), 0, 0);
			}
			else if(xAxis >= 179 && xAxis <= 197 && yAxis >= 70 && yAxis <= 88)
			{
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
			}
			else if(xAxis >= 179 && xAxis <= 197 && yAxis >= 90 && yAxis <= 108)
			{
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
				Mekanism.packetHandler.sendToServer(new RobitMessage(RobitPacketType.GUI, 4, robit.getEntityId(), null));
				mc.player.openGui(Mekanism.instance, 25, mc.world, robit.getEntityId(), 0, 0);
			}
		}
	}

	private int getCookProgressScaled(int i)
	{
		return robit.furnaceCookTime * i / 200;
	}

	private int getBurnTimeRemainingScaled(int i)
	{
		if(robit.currentItemBurnTime == 0)
		{
			robit.currentItemBurnTime = 200;
		}

		return robit.furnaceBurnTime * i / robit.currentItemBurnTime;
	}
}
