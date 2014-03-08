package mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;

import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class GuiEnergyInfo extends GuiElement
{
	public IInfoHandler infoHandler;

	public GuiEnergyInfo(IInfoHandler handler, GuiMekanism gui, TileEntity tile, ResourceLocation def)
	{
		super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiEnergyInfo.png"), gui, tile, def);

		infoHandler = handler;
	}

	public static interface IInfoHandler
	{
		public List<String> getInfo();
	}

	@Override
	public void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight)
	{
		mc.renderEngine.bindTexture(RESOURCE);

		guiObj.drawTexturedModalRect(guiWidth - 26, guiHeight + 138, 0, 0, 26, 26);

		mc.renderEngine.bindTexture(defaultLocation);
	}

	@Override
	public void renderForeground(int xAxis, int yAxis)
	{
		if(xAxis >= -21 && xAxis <= -3 && yAxis >= 142 && yAxis <= 160)
		{
			displayTooltips(infoHandler.getInfo(), xAxis, yAxis);
		}
	}

	@Override
	public void preMouseClicked(int xAxis, int yAxis, int button) {}

	@Override
	public void mouseClicked(int xAxis, int yAxis, int button) {}
}
