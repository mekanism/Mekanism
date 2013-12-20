package mekanism.client.gui;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tileentity.TileEntityFactory;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class GuiSortingTab extends GuiElement
{
	public GuiSortingTab(GuiScreen gui, TileEntity tile, ResourceLocation def)
	{
		super(MekanismUtils.getResource(ResourceType.GUI, "GuiSortingTab.png"), gui, tile, def);
	}
	
	@Override
	public void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight)
	{
		mc.renderEngine.bindTexture(RESOURCE);
		
		guiObj.drawTexturedModalRect(guiWidth - 26, guiHeight + 34, 0, 0, 26, 35);
		
		if(xAxis >= -21 && xAxis <= -3 && yAxis >= 38 && yAxis <= 56)
		{
			guiObj.drawTexturedModalRect(guiWidth - 21, guiHeight + 38, 26, 0, 18, 18);
		}
		else {
			guiObj.drawTexturedModalRect(guiWidth - 21, guiHeight + 38, 26, 18, 18, 18);
		}
		
		mc.renderEngine.bindTexture(defaultLocation);
	}
	
	@Override
	public void renderForeground(int xAxis, int yAxis)
	{
		mc.renderEngine.bindTexture(RESOURCE);
		
		getFontRenderer().drawString(((TileEntityFactory)tileEntity).sorting ? "On" : "Off", -21, 58, 0x0404040);
		
		if(xAxis >= -21 && xAxis <= -3 && yAxis >= 38 && yAxis <= 56)
		{
			displayTooltip(MekanismUtils.localize("gui.factory.autoSort"), xAxis, yAxis);
		}
		
		mc.renderEngine.bindTexture(defaultLocation);
	}
	
	@Override
	public void preMouseClicked(int xAxis, int yAxis, int button) {}
	
	@Override
	public void mouseClicked(int xAxis, int yAxis, int button)
	{
		if(button == 0)
		{
			if(xAxis >= -21 && xAxis <= -3 && yAxis >= 38 && yAxis <= 56)
			{
				ArrayList data = new ArrayList();
				data.add(0);
				PacketHandler.sendPacket(Transmission.SERVER, new PacketTileEntity().setParams(Coord4D.get(tileEntity), data));
				mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
			}
		}
	}
}
