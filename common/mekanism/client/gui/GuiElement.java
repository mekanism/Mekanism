package mekanism.client.gui;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import mekanism.common.ObfuscatedNames;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public abstract class GuiElement
{
	protected static Minecraft mc = Minecraft.getMinecraft();
	
	protected ResourceLocation RESOURCE;
	
	public GuiScreen guiObj;
	
	public TileEntity tileEntity;
	
	public ResourceLocation defaultLocation;
	
	public GuiElement(ResourceLocation resource, GuiScreen gui, TileEntity tile, ResourceLocation def)
	{
		RESOURCE = resource;
		guiObj = gui;
		tileEntity = tile;
		defaultLocation = def;
	}
	
	protected void displayTooltip(String s, int xAxis, int yAxis)
	{
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT + GL11.GL_LIGHTING_BIT);
		try {
			Method m = MekanismUtils.getPrivateMethod(GuiContainer.class, ObfuscatedNames.GuiContainer_drawCreativeTabHoveringText, String.class, Integer.TYPE, Integer.TYPE);
			m.setAccessible(true);
			m.invoke(guiObj, s, xAxis, yAxis);
		} catch(Exception e) {}
		GL11.glPopAttrib();
	}
	
	protected void offsetX(int xSize)
	{
		if(guiObj instanceof GuiContainer)
		{
			try {
				int size = (Integer)MekanismUtils.getPrivateValue(guiObj, GuiContainer.class, ObfuscatedNames.GuiContainer_xSize);
				MekanismUtils.setPrivateValue(guiObj, size+xSize, GuiContainer.class, ObfuscatedNames.GuiContainer_xSize);
			} catch(Exception e) {}
		}
	}
	
	protected void offsetY(int ySize)
	{
		if(guiObj instanceof GuiContainer)
		{
			try {
				int size = (Integer)MekanismUtils.getPrivateValue(guiObj, GuiContainer.class, ObfuscatedNames.GuiContainer_ySize);
				MekanismUtils.setPrivateValue(guiObj, size+ySize, GuiContainer.class, ObfuscatedNames.GuiContainer_ySize);
			} catch(Exception e) {}
		}
	}
	
	protected FontRenderer getFontRenderer()
	{
		try {
			return (FontRenderer)MekanismUtils.getPrivateValue(guiObj, GuiScreen.class, ObfuscatedNames.GuiScreen_fontRenderer);
		} catch(Exception e) {}
		
		return null;
	}
	
	public abstract void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight);
	
	public abstract void renderForeground(int xAxis, int yAxis);
	
	public abstract void preMouseClicked(int xAxis, int yAxis, int button);
	
	public abstract void mouseClicked(int xAxis, int yAxis, int button);
}
