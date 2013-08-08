package mekanism.client;

import java.lang.reflect.Method;

import mekanism.api.Object3D;
import mekanism.common.IRedstoneControl;
import mekanism.common.IRedstoneControl.RedstoneControl;
import mekanism.common.MekanismUtils;
import mekanism.common.MekanismUtils.ResourceType;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.network.PacketRedstoneControl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class GuiRedstoneControl 
{
	private static Minecraft mc = Minecraft.getMinecraft();
	
	private static ResourceLocation RESOURCE = MekanismUtils.getResource(ResourceType.GUI, "GuiRedstoneControl.png");
	
	public GuiContainer guiContainer;
	
	public TileEntity tileEntity;
	
	public ResourceLocation defaultLocation;
	
	public GuiRedstoneControl(GuiContainer gui, TileEntity tile, ResourceLocation def)
	{
		guiContainer = gui;
		tileEntity = tile;
		defaultLocation = def;
	}
	
	private void displayTooltip(String s, int xAxis, int yAxis)
	{
		try {
			Method m = GuiContainer.class.getDeclaredMethod("drawCreativeTabHoveringText", String.class, Integer.TYPE, Integer.TYPE);
			m.setAccessible(true);
			m.invoke(guiContainer, s, xAxis, yAxis);
		} catch(Exception e) {e.printStackTrace();}
	}
	
	public void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight)
	{
		mc.renderEngine.func_110577_a(RESOURCE);
		
		guiContainer.drawTexturedModalRect(guiWidth + 176, guiHeight + 138, 0, 0, 26, 26);
		
		IRedstoneControl control = (IRedstoneControl)tileEntity;
		int renderX = 26 + (18*control.getControlType().ordinal());
		
		if(xAxis >= 179 && xAxis <= 197 && yAxis >= 142 && yAxis <= 160)
		{
			guiContainer.drawTexturedModalRect(guiWidth + 179, guiHeight + 142, renderX, 0, 18, 18);
		}
		else {
			guiContainer.drawTexturedModalRect(guiWidth + 179, guiHeight + 142, renderX, 18, 18, 18);
		}
		
		mc.renderEngine.func_110577_a(defaultLocation);
	}
	
	public void renderForeground(int xAxis, int yAxis)
	{
		mc.renderEngine.func_110577_a(RESOURCE);
		
		IRedstoneControl control = (IRedstoneControl)tileEntity;
		
		if(xAxis >= 179 && xAxis <= 197 && yAxis >= 142 && yAxis <= 160)
		{
			displayTooltip(control.getControlType().getDisplay(), xAxis, yAxis);
		}
		
		mc.renderEngine.func_110577_a(defaultLocation);
	}
	
	public void mouseClicked(int xAxis, int yAxis)
	{
		IRedstoneControl control = (IRedstoneControl)tileEntity;
		
		if(xAxis >= 179 && xAxis <= 197 && yAxis >= 142 && yAxis <= 160)
		{
			RedstoneControl current = control.getControlType();
			int ordinalToSet = current.ordinal() < (RedstoneControl.values().length-1) ? current.ordinal()+1 : 0;
			
			PacketHandler.sendPacket(Transmission.SERVER, new PacketRedstoneControl().setParams(Object3D.get(tileEntity), RedstoneControl.values()[ordinalToSet]));
		}
	}
}
