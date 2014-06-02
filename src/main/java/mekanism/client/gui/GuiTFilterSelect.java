package mekanism.client.gui;

import mekanism.api.Coord4D;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.network.PacketLogisticalSorterGui;
import mekanism.common.network.PacketLogisticalSorterGui.SorterGuiPacket;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiTFilterSelect extends GuiMekanism
{
	public TileEntityLogisticalSorter tileEntity;
	
	public GuiTFilterSelect(EntityPlayer player, TileEntityLogisticalSorter tentity)
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
		buttonList.add(new GuiButton(0, guiWidth + 24, guiHeight + 32, 128, 20, MekanismUtils.localize("gui.itemstack")));
		buttonList.add(new GuiButton(1, guiWidth + 24, guiHeight + 52, 128, 20, MekanismUtils.localize("gui.oredict")));
		buttonList.add(new GuiButton(2, guiWidth + 24, guiHeight + 72, 128, 20, MekanismUtils.localize("gui.material")));
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton)
	{
		super.actionPerformed(guibutton);

		if(guibutton.id == 0)
		{
			Mekanism.packetPipeline.sendToServer(new PacketLogisticalSorterGui(SorterGuiPacket.SERVER, Coord4D.get(tileEntity), 1, 0, 0));
		}
		else if(guibutton.id == 1)
		{
			Mekanism.packetPipeline.sendToServer(new PacketLogisticalSorterGui(SorterGuiPacket.SERVER, Coord4D.get(tileEntity), 2, 0, 0));
		}
		else if(guibutton.id == 2)
		{
			Mekanism.packetPipeline.sendToServer(new PacketLogisticalSorterGui(SorterGuiPacket.SERVER, Coord4D.get(tileEntity), 3, 0, 0));
		}
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		fontRendererObj.drawString(MekanismUtils.localize("gui.filterSelect.title"), 43, 6, 0x404040);
		
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
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
                SoundHandler.playSound("gui.button.press");
				Mekanism.packetPipeline.sendToServer(new PacketLogisticalSorterGui(SorterGuiPacket.SERVER, Coord4D.get(tileEntity), 0, 0, 0));
			}
		}
	}
}
