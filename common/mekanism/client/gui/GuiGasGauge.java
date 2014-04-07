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

	private int width = 6;
	private int height = 56;
	private int innerOffsetY = 2;

	private Type gaugeType;

	IGasInfoHandler infoHandler;

	public GuiGasGauge(IGasInfoHandler handler, Type type, GuiMekanism gui, TileEntity tile, ResourceLocation def, int x, int y)
	{
		super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, type.textureLocation), gui, tile, def);

		xLocation = x;
		yLocation = y;

		width = type.width;
		height = type.height;
		infoHandler = handler;
	}

	@Override
	public void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight)
	{
		int scale = getScaledGasLevel(height);
		int start = 0;
		GasStack gas = infoHandler.getTank().getGas();

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

			guiObj.drawTexturedModelRectFromIcon(guiWidth + xLocation, guiHeight + yLocation + 58 - renderRemaining - start, gas.getGas().getIcon(), 16, 16 - (16 - renderRemaining));

			start+=16;

			if(renderRemaining == 0 || scale == 0)
			{
				break;
			}
		}

		mc.renderEngine.bindTexture(defaultLocation);
		guiObj.drawTexturedModalRect(guiWidth + xLocation, guiHeight + yLocation, 176, 40, 16, 59);

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
		STANDARD(20, 58, "mekanism:gasGaugeStandard"),
		WIDE(100, 58, "mekanism:gasGaugeWide");

		public int width;
		public int height;
		public String textureLocation;

		private Type(int w, int h, String t)
		{
			width = w;
			height = h;
			textureLocation = t;
		}
	}
}
