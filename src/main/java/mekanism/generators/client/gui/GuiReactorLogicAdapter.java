package mekanism.generators.client.gui;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.common.tile.reactor.TileEntityReactorLogicAdapter;
import mekanism.generators.common.tile.reactor.TileEntityReactorLogicAdapter.ReactorLogic;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiReactorLogicAdapter extends GuiMekanism
{
	public TileEntityReactorLogicAdapter tileEntity;
	
	public GuiReactorLogicAdapter(InventoryPlayer inventory, final TileEntityReactorLogicAdapter tentity)
	{
		super(new ContainerNull(inventory.player, tentity));
		
		tileEntity = tentity;
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
		
		fontRendererObj.drawString(tileEntity.getInventoryName(), (xSize/2)-(fontRendererObj.getStringWidth(tileEntity.getInventoryName())/2), 6, 0x404040);
		renderScaledText(MekanismUtils.localize("gui.coolingMeasurements") + ": " + EnumColor.RED + LangUtils.transOnOff(tileEntity.activeCooled), 36, 20, 0x404040, 117);
		renderScaledText(MekanismUtils.localize("gui.redstoneOutputMode") + ": " + EnumColor.RED + tileEntity.logicType.getLocalizedName(), 23, 123, 0x404040, 130);
		
		String text = MekanismUtils.localize("gui.status") + ": " + EnumColor.RED + MekanismUtils.localize("gui." + (tileEntity.checkMode() ? "outputting" : "idle"));
		fontRendererObj.drawString(text, (xSize/2)-(fontRendererObj.getStringWidth(text)/2), 136, 0x404040); 
		
		for(ReactorLogic type : ReactorLogic.values())
		{
			GL11.glPushMatrix();
			GL11.glEnable(GL11.GL_LIGHTING);
			itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), type.getRenderStack(), 27, 35 + (22*type.ordinal()));
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glPopMatrix();
			
			fontRendererObj.drawString(EnumColor.WHITE + type.getLocalizedName(), 46, 34+(22*type.ordinal()), 0x404040);
			
			if(xAxis >= 24 && xAxis <= 152 && yAxis >= 32+(22*type.ordinal()) && yAxis <= 32+22+(22*type.ordinal()))
			{
				drawCreativeTabHoveringText(type.getDescription(), xAxis, yAxis);
			}
		}
		
		if(xAxis >= 23 && xAxis <= 34 && yAxis >= 19 && yAxis <= 30)
		{
			drawCreativeTabHoveringText(MekanismUtils.localize("gui.toggleCooling"), xAxis, yAxis);
		}
		
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiReactorLogicAdapter.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
		
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
		
		for(ReactorLogic type : ReactorLogic.values())
		{
			MekanismRenderer.color(EnumColor.RED);
			
			drawTexturedModalRect(guiWidth + 24, guiHeight + 32+(22*type.ordinal()), 0, 166+(type == tileEntity.logicType ? 22 : 0), 128, 22);
			
			MekanismRenderer.resetColor();
		}
		
		if(xAxis >= 23 && xAxis <= 34 && yAxis >= 19 && yAxis <= 30)
		{
			drawTexturedModalRect(guiWidth + 23, guiHeight + 19, 176, 0, 11, 11);
		}
		else {
			drawTexturedModalRect(guiWidth + 23, guiHeight + 19, 176, 11, 11, 11);
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
			
			if(xAxis >= 23 && xAxis <= 34 && yAxis >= 19 && yAxis <= 30)
			{
				SoundHandler.playSound("gui.button.press");
				
				ArrayList data = new ArrayList();
				data.add(0);
				
				Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
				
				return;
			}
			
			for(ReactorLogic type : ReactorLogic.values())
			{
				if(xAxis >= 24 && xAxis <= 152 && yAxis >= 32+(22*type.ordinal()) && yAxis <= 32+22+(22*type.ordinal()))
				{
					if(type != tileEntity.logicType)
					{
						SoundHandler.playSound("gui.button.press");
						
						ArrayList data = new ArrayList();
						data.add(1);
						data.add(type.ordinal());
						
						Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
						
						return;
					}
				}
			}
		}
	}
}
