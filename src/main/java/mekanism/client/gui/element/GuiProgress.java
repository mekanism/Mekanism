package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import codechicken.lib.vec.Rectangle4i;

@SideOnly(Side.CLIENT)
public class GuiProgress extends GuiElement
{
	private int xLocation;
	private int yLocation;

	private int innerOffsetX = 2;

	private ProgressBar type;
	private IProgressInfoHandler handler;

	public GuiProgress(IProgressInfoHandler handler, ProgressBar type, IGuiWrapper gui, ResourceLocation def, int x, int y)
	{
		super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiProgress.png"), gui, def);
		xLocation = x;
		yLocation = y;

		this.type = type;
		this.handler = handler;
	}
	
	@Override
	public Rectangle4i getBounds(int guiWidth, int guiHeight)
	{
		return new Rectangle4i(guiWidth + xLocation, guiHeight + yLocation, type.width, type.height);
	}

	@Override
	public void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight)
	{
		mc.renderEngine.bindTexture(RESOURCE);
		
		if(handler.isActive())
		{
			guiObj.drawTexturedRect(guiWidth + xLocation, guiHeight + yLocation, type.textureX, type.textureY, type.width, type.height);
			int displayInt = (int)(handler.getProgress() * (type.width-2*innerOffsetX));
			guiObj.drawTexturedRect(guiWidth + xLocation + innerOffsetX, guiHeight + yLocation, type.textureX + type.width + innerOffsetX, type.textureY, displayInt, type.height);
		}
		
		mc.renderEngine.bindTexture(defaultLocation);
	}

	@Override
	public void renderForeground(int xAxis, int yAxis) {}

	@Override
	public void preMouseClicked(int xAxis, int yAxis, int button) {}

	@Override
	public void mouseClicked(int xAxis, int yAxis, int button) {}

	public static abstract class IProgressInfoHandler
	{
		public abstract double getProgress();

		public boolean isActive()
		{
			return true;
		}
	}

	public enum ProgressBar
	{
		BLUE(28, 11, 0, 0),
		YELLOW(28, 11, 0, 11),
		RED(28, 11, 0, 22),
		GREEN(28, 11, 0, 33),
		PURPLE(28, 11, 0, 44),
		STONE(28, 11, 0, 55),
		CRUSH(28, 11, 0, 66),

		LARGE_RIGHT(52, 10, 128, 0),
		LARGE_LEFT(52, 10, 128, 10),
		MEDIUM(36, 10, 128, 20),
		SMALL_RIGHT(32, 10, 128, 30),
		SMALL_LEFT(32, 10, 128, 40),
		BI(20, 8, 128, 50);

		public int width;
		public int height;

		public int textureX;
		public int textureY;

		private ProgressBar(int w, int h, int u, int v)
		{
			width = w;
			height = h;
			textureX = u;
			textureY = v;
		}
	}
}
