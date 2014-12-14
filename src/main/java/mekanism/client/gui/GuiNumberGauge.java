package mekanism.client.gui;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

import static java.lang.Math.min;

public class GuiNumberGauge extends GuiGauge
{
	INumberInfoHandler infoHandler;

	public GuiNumberGauge(INumberInfoHandler handler, Type type, IGuiWrapper gui, ResourceLocation def, int x, int y)
	{
		super(type, gui, def, x, y);

		infoHandler = handler;
	}

	@Override
	public TextureAtlasSprite getIcon()
	{
		return infoHandler.getIcon();
	}

	@Override
	public String getTooltipText()
	{
		return infoHandler.getText(infoHandler.getLevel());
	}

	@Override
	public int getScaledLevel()
	{
		return (int)((height-2) * min(infoHandler.getLevel() / infoHandler.getMaxLevel(), 1));
	}


	public static interface INumberInfoHandler
	{
		public TextureAtlasSprite getIcon();

		public double getLevel();

		public double getMaxLevel();

		public String getText(double level);
	}
}
