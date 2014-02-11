package mekanism.client.gui;

import mekanism.common.tile.TileEntityElectricBlock;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class GuiPowerBar extends GuiElement
{
	private int xLocation;
	private int yLocation;

	private int width = 6;
	private int height = 56;
	private int innerOffsetY = 2;

	private TileEntityElectricBlock tileEntityElectric;

	public GuiPowerBar(GuiMekanism gui, TileEntityElectricBlock tile, ResourceLocation def, int x, int y)
	{
		super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiPowerBar.png"), gui, tile, def);
		tileEntityElectric = tile;
		xLocation = x;
		yLocation = y;
	}

	@Override
	public void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight)
	{
		mc.renderEngine.bindTexture(RESOURCE);

		guiObj.drawTexturedModalRect(guiWidth + xLocation, guiHeight + yLocation, 0, 0, width, height);
		int displayInt = tileEntityElectric.getScaledEnergyLevel(52) + innerOffsetY;
		guiObj.drawTexturedModalRect(guiWidth + xLocation, guiHeight + yLocation + height - displayInt, 6, height - displayInt, width, displayInt);

		mc.renderEngine.bindTexture(defaultLocation);
	}

	@Override
	public void renderForeground(int xAxis, int yAxis)
	{
		mc.renderEngine.bindTexture(RESOURCE);

		if(xAxis >= xLocation && xAxis <= xLocation + width && yAxis >= yLocation && yAxis <= yLocation + height)
		{
			displayTooltip(MekanismUtils.getEnergyDisplay(tileEntityElectric.getEnergy()), xAxis, yAxis);
		}

		mc.renderEngine.bindTexture(defaultLocation);
	}

	@Override
	public void preMouseClicked(int xAxis, int yAxis, int button) {}

	@Override
	public void mouseClicked(int xAxis, int yAxis, int button) {}
}
