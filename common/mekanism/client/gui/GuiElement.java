package mekanism.client.gui;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public abstract class GuiElement
{
	protected static Minecraft mc = Minecraft.getMinecraft();
	
	protected ResourceLocation RESOURCE;
	
	public GuiMekanism guiObj;
	
	public TileEntity tileEntity;
	
	public ResourceLocation defaultLocation;
	
	public GuiElement(ResourceLocation resource, GuiMekanism gui, TileEntity tile, ResourceLocation def)
	{
		RESOURCE = resource;
		guiObj = gui;
		tileEntity = tile;
		defaultLocation = def;
	}
	
	protected void displayTooltip(String s, int xAxis, int yAxis)
	{
		guiObj.drawCreativeTabHoveringText(s, xAxis, yAxis);
	}
	
	protected void displayTooltips(List<String> list, int xAxis, int yAxis)
	{
		guiObj.func_102021_a(list, xAxis, yAxis);
	}
	
	protected void offsetX(int xSize)
	{
		guiObj.xSize += xSize;
	}
	
	protected void offsetY(int ySize)
	{
		guiObj.ySize += ySize;
	}
	
	protected FontRenderer getFontRenderer()
	{
		return guiObj.getFontRenderer();
	}
	
	public abstract void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight);
	
	public abstract void renderForeground(int xAxis, int yAxis);
	
	public abstract void preMouseClicked(int xAxis, int yAxis, int button);
	
	public abstract void mouseClicked(int xAxis, int yAxis, int button);
}
