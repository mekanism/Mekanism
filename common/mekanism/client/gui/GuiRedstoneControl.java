package mekanism.client.gui;

import mekanism.api.Coord4D;
import mekanism.common.IRedstoneControl;
import mekanism.common.IRedstoneControl.RedstoneControl;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.network.PacketRedstoneControl;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class GuiRedstoneControl extends GuiElement
{
	public GuiRedstoneControl(GuiScreen gui, TileEntity tile, ResourceLocation def)
	{
		super(MekanismUtils.getResource(ResourceType.GUI, "GuiRedstoneControl.png"), gui, tile, def);
	}
	
	@Override
	public void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight)
	{
		mc.renderEngine.bindTexture(RESOURCE);
		
		guiObj.drawTexturedModalRect(guiWidth + 176, guiHeight + 138, 0, 0, 26, 26);
		
		IRedstoneControl control = (IRedstoneControl)tileEntity;
		int renderX = 26 + (18*control.getControlType().ordinal());
		
		if(xAxis >= 179 && xAxis <= 197 && yAxis >= 142 && yAxis <= 160)
		{
			guiObj.drawTexturedModalRect(guiWidth + 179, guiHeight + 142, renderX, 0, 18, 18);
		}
		else {
			guiObj.drawTexturedModalRect(guiWidth + 179, guiHeight + 142, renderX, 18, 18, 18);
		}
		
		mc.renderEngine.bindTexture(defaultLocation);
	}
	
	@Override
	public void renderForeground(int xAxis, int yAxis)
	{
		mc.renderEngine.bindTexture(RESOURCE);
		
		IRedstoneControl control = (IRedstoneControl)tileEntity;
		
		if(xAxis >= 179 && xAxis <= 197 && yAxis >= 142 && yAxis <= 160)
		{
			displayTooltip(control.getControlType().getDisplay(), xAxis, yAxis);
		}
		
		mc.renderEngine.bindTexture(defaultLocation);
	}
	
	@Override
	public void preMouseClicked(int xAxis, int yAxis, int button) {}
	
	@Override
	public void mouseClicked(int xAxis, int yAxis, int button)
	{
		IRedstoneControl control = (IRedstoneControl)tileEntity;
		
		if(button == 0)
		{
			if(xAxis >= 179 && xAxis <= 197 && yAxis >= 142 && yAxis <= 160)
			{
				RedstoneControl current = control.getControlType();
				int ordinalToSet = current.ordinal() < (RedstoneControl.values().length-1) ? current.ordinal()+1 : 0;
				
				mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
				PacketHandler.sendPacket(Transmission.SERVER, new PacketRedstoneControl().setParams(Coord4D.get(tileEntity), RedstoneControl.values()[ordinalToSet]));
			}
		}
	}
}
