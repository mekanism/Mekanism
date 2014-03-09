package mekanism.client.gui;

import org.lwjgl.opengl.GL11;

import mekanism.api.Coord4D;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.miner.MItemStackFilter;
import mekanism.common.miner.MOreDictFilter;
import mekanism.common.miner.MinerFilter;
import mekanism.common.network.PacketDigitalMinerGui;
import mekanism.common.network.PacketDigitalMinerGui.MinerGuiPacket;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;

public class GuiMFilterSelect extends GuiMekanism
{
	public TileEntityDigitalMiner tileEntity;
	
	public GuiMFilterSelect(EntityPlayer player, TileEntityDigitalMiner tentity)
	{
		super(new ContainerNull(player, tentity));
		
		tileEntity = tentity;
	}
	
	@Override
	public void initGui()
	{
		super.initGui();

		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		
		buttonList.clear();
		buttonList.add(new GuiButton(0, guiWidth + 40, guiHeight + 32, 96, 20, "ItemStack"));
		buttonList.add(new GuiButton(1, guiWidth + 40, guiHeight + 52, 96, 20, "OreDict"));
		buttonList.add(new GuiButton(2, guiWidth + 40, guiHeight + 72, 96, 20, "Material"));
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton)
	{
		super.actionPerformed(guibutton);

		if(guibutton.id == 0)
		{
			PacketHandler.sendPacket(Transmission.SERVER, new PacketDigitalMinerGui().setParams(MinerGuiPacket.SERVER, Coord4D.get(tileEntity), 1));
		}
		else if(guibutton.id == 1)
		{
			PacketHandler.sendPacket(Transmission.SERVER, new PacketDigitalMinerGui().setParams(MinerGuiPacket.SERVER, Coord4D.get(tileEntity), 2));
		}
		else if(guibutton.id == 2)
		{
			PacketHandler.sendPacket(Transmission.SERVER, new PacketDigitalMinerGui().setParams(MinerGuiPacket.SERVER, Coord4D.get(tileEntity), 3));
		}
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);

		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiFilterSelect.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);

		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		if(xAxis >= 5 && xAxis <= 16 && yAxis >= 5 && yAxis <= 16)
		{
			drawTexturedModalRect(guiWidth + 5, guiHeight + 5, 176, 0, 11, 11);
		}
		else {
			drawTexturedModalRect(guiWidth + 5, guiHeight + 5, 176, 11, 11, 11);
		}
	}
	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int button)
	{
		super.mouseClicked(mouseX, mouseY, button);
		
		if(button == 0)
		{
			int xAxis = (mouseX - (width - xSize) / 2);
			int yAxis = (mouseY - (height - ySize) / 2);
			
			if(xAxis >= 5 && xAxis <= 16 && yAxis >= 5 && yAxis <= 16)
			{
				mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
				PacketHandler.sendPacket(Transmission.SERVER, new PacketDigitalMinerGui().setParams(MinerGuiPacket.SERVER, Coord4D.get(tileEntity), 0));
			}
		}
	}
}
