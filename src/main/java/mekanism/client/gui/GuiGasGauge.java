package mekanism.client.gui;

import mekanism.api.gas.GasTank;
import mekanism.common.util.MekanismUtils;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

public class GuiGasGauge extends GuiGauge
{
	IGasInfoHandler infoHandler;

	public GuiGasGauge(IGasInfoHandler handler, Type type, IGuiWrapper gui, ResourceLocation def, int x, int y)
	{
		super(type, gui, def, x, y);

		infoHandler = handler;
	}

	@Override
	public int getScaledLevel()
	{
		return infoHandler.getTank().getGas() != null ? infoHandler.getTank().getStored()*(height-2) / infoHandler.getTank().getMaxGas() : 0;
	}

	@Override
	public IIcon getIcon()
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
