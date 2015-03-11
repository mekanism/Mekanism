package mekanism.client.gui.element;

import java.util.List;

import org.lwjgl.opengl.GL11;

import mekanism.client.gui.IGuiWrapper;
import mekanism.common.ObfuscatedNames;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;
import codechicken.lib.vec.Rectangle4i;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class GuiElement
{
	public static Minecraft mc = Minecraft.getMinecraft();

	public ResourceLocation RESOURCE;

	public IGuiWrapper guiObj;

	public ResourceLocation defaultLocation;

	public GuiElement(ResourceLocation resource, IGuiWrapper gui, ResourceLocation def)
	{
		RESOURCE = resource;
		guiObj = gui;
		defaultLocation = def;
	}

	public void displayTooltip(String s, int xAxis, int yAxis)
	{
		guiObj.displayTooltip(s, xAxis, yAxis);
	}

	public void displayTooltips(List<String> list, int xAxis, int yAxis)
	{
		guiObj.displayTooltips(list, xAxis, yAxis);
	}

	public void offsetX(int xSize)
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

	public void offsetY(int ySize)
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
	
	public void offsetLeft(int guiLeft)
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
	
	public void offsetTop(int guiTop)
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
	
	public void renderScaledText(String text, int x, int y, int color, int maxX)
	{
		int length = getFontRenderer().getStringWidth(text);
		
		if(length <= maxX)
		{
			getFontRenderer().drawString(text, x, y, color);
		}
		else {
			float scale = (float)maxX/length;
			float reverse = 1/scale;
			float yAdd = 4-(scale*8)/2F;
			
			GL11.glPushMatrix();
			
			GL11.glScalef(scale, scale, scale);
			getFontRenderer().drawString(text, (int)(x*reverse), (int)((y*reverse)+yAdd), color);
			
			GL11.glPopMatrix();
		}
	}

	public FontRenderer getFontRenderer()
	{
		return guiObj.getFont();
	}
	
	public void mouseClickMove(int mouseX, int mouseY, int button, long ticks) {}

	public void mouseMovedOrUp(int x, int y, int type) {}
	
	public abstract Rectangle4i getBounds(int guiWidth, int guiHeight);

	public abstract void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight);

	public abstract void renderForeground(int xAxis, int yAxis);

	public abstract void preMouseClicked(int xAxis, int yAxis, int button);

	public abstract void mouseClicked(int xAxis, int yAxis, int button);
}
