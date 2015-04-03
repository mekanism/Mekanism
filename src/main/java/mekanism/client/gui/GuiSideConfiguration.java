package mekanism.client.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.element.GuiConfigTypeTab;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.SideData;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.network.PacketConfigurationUpdate.ConfigurationPacket;
import mekanism.common.network.PacketConfigurationUpdate.ConfigurationUpdateMessage;
import mekanism.common.network.PacketSimpleGui.SimpleGuiMessage;
import mekanism.common.tile.TileEntityContainerBlock;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSideConfiguration extends GuiMekanism
{
	public Map<Integer, GuiPos> slotPosMap = new HashMap<Integer, GuiPos>();

	public ISideConfiguration configurable;
	
	public TransmissionType currentType;
	
	public List<GuiConfigTypeTab> configTabs = new ArrayList<GuiConfigTypeTab>();

	public GuiSideConfiguration(EntityPlayer player, ISideConfiguration tile)
	{
		super((TileEntityContainerBlock)tile, new ContainerNull(player, (TileEntityContainerBlock)tile));

		ySize = 95;

		configurable = tile;
		
		for(TransmissionType type : configurable.getConfig().transmissions)
		{
			GuiConfigTypeTab tab = new GuiConfigTypeTab(this, (TileEntity)configurable, type, MekanismUtils.getResource(ResourceType.GUI, "GuiConfiguration.png"));
			
			guiElements.add(tab);
			configTabs.add(tab);
		}
		
		currentType = getTopTransmission();
		
		updateTabs();

		slotPosMap.put(0, new GuiPos(81, 64));
		slotPosMap.put(1, new GuiPos(81, 34));
		slotPosMap.put(2, new GuiPos(81, 49));
		slotPosMap.put(3, new GuiPos(66, 64));
		slotPosMap.put(4, new GuiPos(66, 49));
		slotPosMap.put(5, new GuiPos(96, 49));
	}
	
	public TransmissionType getTopTransmission()
	{
		return configurable.getConfig().transmissions.get(0);
	}
	
	public void updateTabs()
	{
		int rendered = 0;
		
		for(GuiConfigTypeTab tab : configTabs)
		{
			tab.visible = currentType != tab.transmission;
			
			if(tab.visible)
			{
				tab.left = rendered >= 0 && rendered <= 2;
				tab.setY(2+((rendered%3)*(26+2)));
			}
			
			rendered++;
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiConfiguration.png"));

		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;

		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);

		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
		
		if(xAxis >= 6 && xAxis <= 20 && yAxis >= 6 && yAxis <= 20)
		{
			drawTexturedModalRect(guiWidth + 6, guiHeight + 6, 176 + 28, 0, 14, 14);
		}
		else {
			drawTexturedModalRect(guiWidth + 6, guiHeight + 6, 176 + 28, 14, 14, 14);
		}

		if(xAxis >= 156 && xAxis <= 170 && yAxis >= 6 && yAxis <= 20)
		{
			drawTexturedModalRect(guiWidth + 156, guiHeight + 6, 176 + 14, 0, 14, 14);
		}
		else {
			drawTexturedModalRect(guiWidth + 156, guiHeight + 6, 176 + 14, 14, 14, 14);
		}

		for(int i = 0; i < slotPosMap.size(); i++)
		{
			MekanismRenderer.resetColor();

			int x = slotPosMap.get(i).xPos;
			int y = slotPosMap.get(i).yPos;

			SideData data = configurable.getConfig().getOutput(currentType, i);

			if(data.color != EnumColor.GREY)
			{
				MekanismRenderer.color(data.color);
			}

			if(xAxis >= x && xAxis <= x+14 && yAxis >= y && yAxis <= y+14)
			{
				drawTexturedModalRect(guiWidth + x, guiHeight + y, 176, 0, 14, 14);
			}
			else {
				drawTexturedModalRect(guiWidth + x, guiHeight + y, 176, 14, 14, 14);
			}
		}

		MekanismRenderer.resetColor();
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		String title = currentType.localize() + " " + MekanismUtils.localize("gui.config");
		fontRendererObj.drawString(title, (xSize/2)-(fontRendererObj.getStringWidth(title)/2), 5, 0x404040);
		
		if(configurable.getConfig().canEject(currentType))
		{
			fontRendererObj.drawString(MekanismUtils.localize("gui.eject") + ": " + (configurable.getConfig().isEjecting(currentType) ? "On" : "Off"), 53, 17, 0x00CD00);
		}
		else {
			fontRendererObj.drawString(MekanismUtils.localize("gui.noEject"), 53, 17, 0x00CD00);
		}
		
		fontRendererObj.drawString(MekanismUtils.localize("gui.slots"), 77, 81, 0x787878);

		for(int i = 0; i < slotPosMap.size(); i++)
		{
			int x = slotPosMap.get(i).xPos;
			int y = slotPosMap.get(i).yPos;

			SideData data = configurable.getConfig().getOutput(currentType, i);

			if(xAxis >= x && xAxis <= x+14 && yAxis >= y && yAxis <= y+14)
			{
				drawCreativeTabHoveringText(data.color + data.localize() + " (" + data.color.getName() + ")", xAxis, yAxis);
			}
		}

		if(xAxis >= 156 && xAxis <= 170 && yAxis >= 6 && yAxis <= 20)
		{
			drawCreativeTabHoveringText(MekanismUtils.localize("gui.autoEject"), xAxis, yAxis);
		}

		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	@Override
	public void updateScreen()
	{
		super.updateScreen();

		TileEntity tile = (TileEntity)configurable;

		if(tile == null || mc.theWorld.getTileEntity(tile.xCoord, tile.yCoord, tile.zCoord) == null)
		{
			mc.displayGuiScreen(null);
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button)
	{
		super.mouseClicked(mouseX, mouseY, button);

		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		TileEntity tile = (TileEntity)configurable;

		if(button == 0)
		{
			if(xAxis >= 6 && xAxis <= 20 && yAxis >= 6 && yAxis <= 20)
			{
				int guiId = MachineType.get(tile.getBlockType(), tile.getBlockMetadata()).guiId;
                SoundHandler.playSound("gui.button.press");
				Mekanism.packetHandler.sendToServer(new SimpleGuiMessage(Coord4D.get(tile), guiId));
			}

			if(xAxis >= 156 && xAxis <= 170 && yAxis >= 6 && yAxis <= 20)
			{
                SoundHandler.playSound("gui.button.press");
				Mekanism.packetHandler.sendToServer(new ConfigurationUpdateMessage(ConfigurationPacket.EJECT, Coord4D.get(tile), 0, 0, currentType));
			}
		}

		if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && button == 0)
		{
			button = 2;
		}

		for(int i = 0; i < slotPosMap.size(); i++)
		{
			int x = slotPosMap.get(i).xPos;
			int y = slotPosMap.get(i).yPos;

			if(xAxis >= x && xAxis <= x+14 && yAxis >= y && yAxis <= y+14)
			{
                SoundHandler.playSound("gui.button.press");
				Mekanism.packetHandler.sendToServer(new ConfigurationUpdateMessage(ConfigurationPacket.SIDE_DATA, Coord4D.get(tile), button, i, currentType));
			}
		}
	}

	public static class GuiPos
	{
		public int xPos;
		public int yPos;

		public GuiPos(int x, int y)
		{
			xPos = x;
			yPos = y;
		}
	}
}
