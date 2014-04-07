package mekanism.client.gui;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class GuiGasGauge extends GuiElement
{
	private int xLocation;
	private int yLocation;

	private int width;
	private int height;

	private int number;

	IGasInfoHandler infoHandler;

	public GuiGasGauge(IGasInfoHandler handler, Type type, GuiMekanism gui, TileEntity tile, ResourceLocation def, int x, int y)
	{
		super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, type.textureLocation), gui, tile, def);

		xLocation = x;
		yLocation = y;

		width = type.width;
		height = type.height;
		number = type.number;

		infoHandler = handler;
	}

	@Override
	public void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight)
	{
		mc.renderEngine.bindTexture(RESOURCE);

		int scale = getScaledGasLevel(height-2);
		int start = 0;
		GasStack gas = infoHandler.getTank().getGas();

		guiObj.drawTexturedModalRect(guiWidth + xLocation, guiHeight + yLocation, 0, 0, width, height);

		while(true)
		{
			int renderRemaining = 0;

			if(scale > 16)
			{
				renderRemaining = 16;
				scale -= 16;
			}
			else {
				renderRemaining = scale;
				scale = 0;
			}

			mc.renderEngine.bindTexture(MekanismRenderer.getBlocksTexture());

			for(int i = 0; i < number; i++)
			{
				guiObj.drawTexturedModelRectFromIcon(guiWidth + xLocation + 16*i + 1, guiHeight + yLocation + height - renderRemaining - start - 1, gas.getGas().getIcon(), 16, renderRemaining);
			}

			start+=16;

			if(renderRemaining == 0 || scale == 0)
			{
				break;
			}
		}

		mc.renderEngine.bindTexture(RESOURCE);
		guiObj.drawTexturedModalRect(guiWidth + xLocation, guiHeight + yLocation, width, 0, width, height);

		mc.renderEngine.bindTexture(defaultLocation);
	}

	@Override
	public void renderForeground(int xAxis, int yAxis)
	{

	}

	@Override
	public void preMouseClicked(int xAxis, int yAxis, int button)
	{

	}

	@Override
	public void mouseClicked(int xAxis, int yAxis, int button)
	{

	}

	public static interface IGasInfoHandler
	{
		public GasTank getTank();
	}

	public int getScaledGasLevel(int i)
	{
		return infoHandler.getTank().getGas() != null ? infoHandler.getTank().getStored()*i / infoHandler.getTank().getMaxGas() : 0;
	}

	public static enum Type
	{
		STANDARD(18, 60, 1, "GuiGaugeStandard.png"),
		WIDE(66, 50, 4, "GuiGaugeWide.png");

		public int width;
		public int height;
		public int number;
		public String textureLocation;

		private Type(int w, int h, int n, String t)
		{
			width = w;
			height = h;
			number = n;
			textureLocation = t;
		}
	}
}
