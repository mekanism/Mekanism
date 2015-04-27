package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;
import codechicken.lib.vec.Rectangle4i;

public class GuiSlot extends GuiElement
{
	protected int xLocation;
	protected int yLocation;

	protected int textureX;
	protected int textureY;

	protected int width;
	protected int height;

	protected SlotOverlay overlay = null;

	public GuiSlot(SlotType type, IGuiWrapper gui, ResourceLocation def, int x, int y)
	{
		super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiSlot.png"), gui, def);

		xLocation = x;
		yLocation = y;

		width = type.width;
		height = type.height;

		textureX = type.textureX;
		textureY = type.textureY;
	}

	public GuiSlot with(SlotOverlay overlay)
	{
		this.overlay = overlay;
		return this;
	}
	
	@Override
	public Rectangle4i getBounds(int guiWidth, int guiHeight)
	{
		return new Rectangle4i(guiWidth + xLocation, guiHeight + yLocation, width, height);
	}

	@Override
	public void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight)
	{
		mc.renderEngine.bindTexture(RESOURCE);

		guiObj.drawTexturedRect(guiWidth + xLocation, guiHeight + yLocation, textureX, textureY, width, height);

		if(overlay != null)
		{
			int w = overlay.width;
			int h = overlay.height;
			int xLocationOverlay = xLocation + (width-w)/2;
			int yLocationOverlay = yLocation + (height-h)/2;

			guiObj.drawTexturedRect(guiWidth + xLocationOverlay, guiHeight + yLocationOverlay, overlay.textureX, overlay.textureY, w, h);
		}

		mc.renderEngine.bindTexture(defaultLocation);
	}

	@Override
	public void renderForeground(int xAxis, int yAxis) {}

	@Override
	public void preMouseClicked(int xAxis, int yAxis, int button) {}

	@Override
	public void mouseClicked(int xAxis, int yAxis, int button) {}

	public enum SlotType
	{
		NORMAL(18, 18, 0, 0),
		POWER(18, 18, 18, 0),
		INPUT(18, 18, 36, 0),
		EXTRA(18, 18, 54, 0),
		OUTPUT(18, 18, 72, 0),
		OUTPUT_LARGE(26, 26, 90, 0),
		OUTPUT_WIDE(42, 26, 116, 0);

		public int width;
		public int height;

		public int textureX;
		public int textureY;

		private SlotType(int w, int h, int x, int y)
		{
			width = w;
			height = h;

			textureX = x;
			textureY = y;
		}
	}

	public enum SlotOverlay
	{
		MINUS(18, 18, 0, 18),
		PLUS(18, 18, 18, 18),
		POWER(18, 18, 36, 18),
		INPUT(18, 18, 54, 18),
		OUTPUT(18, 18, 72, 18),
		CHECK(18, 18, 0, 36);

		public int width;
		public int height;

		public int textureX;
		public int textureY;

		private SlotOverlay(int w, int h, int x, int y)
		{
			width = w;
			height = h;

			textureX = x;
			textureY = y;
		}
	}
}
