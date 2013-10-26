package mekanism.client.gui;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

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
		try {
			Method m = GuiContainer.class.getDeclaredMethod("drawCreativeTabHoveringText", String.class, Integer.TYPE, Integer.TYPE);
			m.setAccessible(true);
			m.invoke(guiObj, s, xAxis, yAxis);
		} catch(Exception e) {}
	}
	
	protected void offsetX(int xSize)
	{
		if(guiObj instanceof GuiContainer)
		{
			try {
				Field f = GuiContainer.class.getDeclaredField("xSize");
				f.setAccessible(true);
				f.set(guiObj, ((Integer)f.get(guiObj))+xSize);
			} catch(Exception e) {}
		}
	}
	
	protected void offsetY(int ySize)
	{
		if(guiObj instanceof GuiContainer)
		{
			try {
				Field f = GuiContainer.class.getDeclaredField("ySize");
				f.setAccessible(true);
				f.set(guiObj, ((Integer)f.get(guiObj))+ySize);
			} catch(Exception e) {}
		}
	}
	
	protected FontRenderer getFontRenderer()
	{
		try {
			Field f = GuiScreen.class.getDeclaredField("fontRenderer");
			f.setAccessible(true);
			return (FontRenderer)f.get(guiObj);
		} catch(Exception e) {}
		
		return null;
	}
	
	public abstract void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight);
	
	public abstract void renderForeground(int xAxis, int yAxis);
	
	public abstract void preMouseClicked(int xAxis, int yAxis, int button);
	
	public abstract void mouseClicked(int xAxis, int yAxis, int button);
}
