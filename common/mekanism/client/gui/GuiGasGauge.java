package mekanism.client.gui;

import mekanism.api.gas.GasTank;
import mekanism.common.util.MekanismUtils;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;

public class GuiGasGauge extends GuiGauge
{
	IGasInfoHandler infoHandler;

	public GuiGasGauge(IGasInfoHandler handler, Type type, GuiMekanism gui, TileEntity tile, ResourceLocation def, int x, int y)
	{
		super(type, gui, tile, def, x, y);

		infoHandler = handler;
	}

	@Override
	public int getScaledLevel()
	{
		return infoHandler.getTank().getGas() != null ? infoHandler.getTank().getStored()*(height-2) / infoHandler.getTank().getMaxGas() : 0;
	}

	@Override
	public Icon getIcon()
	{
		return infoHandler.getTank().getGas().getGas().getIcon();
	}

	@Override
	public String getTooltipText()
	{
		return (infoHandler.getTank().getGas() != null) ? infoHandler.getTank().getGas().getGas().getLocalizedName() + ": " + infoHandler.getTank().getStored() + "mB" : MekanismUtils.localize("gui.empty");
	}

	public static interface IGasInfoHandler
	{
		public GasTank getTank();
	}
}
