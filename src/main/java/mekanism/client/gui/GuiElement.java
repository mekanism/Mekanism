package mekanism.client.gui;

import java.util.List;

import mekanism.common.ObfuscatedNames;
import mekanism.common.util.MekanismUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import codechicken.lib.vec.Rectangle4i;

@SideOnly(Side.CLIENT)
public abstract class GuiElement
{
	protected static Minecraft mc = Minecraft.getMinecraft();

	protected ResourceLocation RESOURCE;

	public IGuiWrapper guiObj;

	public ResourceLocation defaultLocation;

	public GuiElement(ResourceLocation resource, IGuiWrapper gui, ResourceLocation def)
	{
		RESOURCE = resource;
		guiObj = gui;
		defaultLocation = def;
	}

	protected void displayTooltip(String s, int xAxis, int yAxis)
	{
		guiObj.displayTooltip(s, xAxis, yAxis);
	}

	protected void displayTooltips(List<String> list, int xAxis, int yAxis)
	{
		guiObj.displayTooltips(list, xAxis, yAxis);
	}

	protected void offsetX(int xSize)
	{
		if(guiObj instanceof GuiContainer)
		{
			try {
				int size = (Integer)MekanismUtils.getPrivateValue(guiObj, GuiContainer.class, ObfuscatedNames.GuiContainer_xSize);
				MekanismUtils.setPrivateValue(guiObj, size + xSize, GuiContainer.class, ObfuscatedNames.GuiContainer_xSize);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected void offsetY(int ySize)
	{
		if(guiObj instanceof GuiContainer)
		{
			try {
				int size = (Integer)MekanismUtils.getPrivateValue(guiObj, GuiContainer.class, ObfuscatedNames.GuiContainer_ySize);
				MekanismUtils.setPrivateValue(guiObj, size + ySize, GuiContainer.class, ObfuscatedNames.GuiContainer_ySize);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	protected void offsetLeft(int guiLeft)
	{
		if(guiObj instanceof GuiContainer)
		{
			try {
				int left = (Integer)MekanismUtils.getPrivateValue(guiObj, GuiContainer.class, ObfuscatedNames.GuiContainer_guiLeft);
				System.out.println(left + " " + guiLeft);
				MekanismUtils.setPrivateValue(guiObj, left + guiLeft, GuiContainer.class, ObfuscatedNames.GuiContainer_guiLeft);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	protected void offsetTop(int guiTop)
	{
		if(guiObj instanceof GuiContainer)
		{
			try {
				int top = (Integer)MekanismUtils.getPrivateValue(guiObj, GuiContainer.class, ObfuscatedNames.GuiContainer_guiTop);
				MekanismUtils.setPrivateValue(guiObj, top + guiTop, GuiContainer.class, ObfuscatedNames.GuiContainer_guiTop);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected FontRenderer getFontRenderer()
	{
		return guiObj.getFont();
	}
	
	protected void mouseClickMove(int mouseX, int mouseY, int button, long ticks) {}

	protected void mouseMovedOrUp(int x, int y, int type) {}
	
	public abstract Rectangle4i getBounds(int guiWidth, int guiHeight);

	public abstract void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight);

	public abstract void renderForeground(int xAxis, int yAxis);

	public abstract void preMouseClicked(int xAxis, int yAxis, int button);

	public abstract void mouseClicked(int xAxis, int yAxis, int button);
}
