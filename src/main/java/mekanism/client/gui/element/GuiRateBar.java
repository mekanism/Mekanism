package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;
import codechicken.lib.vec.Rectangle4i;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiRateBar extends GuiElement
{
	private int xLocation;
	private int yLocation;

	private int width = 8;
	private int height = 60;

	private IRateInfoHandler handler;

	public GuiRateBar(IGuiWrapper gui, IRateInfoHandler h, ResourceLocation def, int x, int y)
	{
		super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiRateBar.png"), gui, def);
		
		handler = h;
		
		xLocation = x;
		yLocation = y;
	}
	
	@Override
	public Rectangle4i getBounds(int guiWidth, int guiHeight)
	{
		return new Rectangle4i(guiWidth + xLocation, guiHeight + yLocation, width, height);
	}
	
	public static abstract class IRateInfoHandler
	{
		public String getTooltip()
		{
			return null;
		}
		
		public abstract double getLevel();
	}

	@Override
	public void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight)
	{
		mc.renderEngine.bindTexture(RESOURCE);

		guiObj.drawTexturedRect(guiWidth + xLocation, guiHeight + yLocation, 0, 0, width, height);
		
		if(handler.getLevel() > 0)
		{
			int displayInt = (int)(handler.getLevel()*58);
			guiObj.drawTexturedRect(guiWidth + xLocation+1, guiHeight + yLocation + height-1 - displayInt, 8, height-2 - displayInt, width-2, displayInt);
		}

		mc.renderEngine.bindTexture(defaultLocation);
	}

	@Override
	public void renderForeground(int xAxis, int yAxis)
	{
		mc.renderEngine.bindTexture(RESOURCE);

		if(handler.getTooltip() != null && xAxis >= xLocation+1 && xAxis <= xLocation + width-1 && yAxis >= yLocation+1 && yAxis <= yLocation + height-1)
		{
			displayTooltip(handler.getTooltip(), xAxis, yAxis);
		}

		mc.renderEngine.bindTexture(defaultLocation);
	}

	@Override
	public void preMouseClicked(int xAxis, int yAxis, int button) {}

	@Override
	public void mouseClicked(int xAxis, int yAxis, int button) {}
}
