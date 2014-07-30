package mekanism.client.gui;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasTank;
import mekanism.common.util.MekanismUtils;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

public class GuiGasGauge extends GuiGauge<Gas>
{
	IGasInfoHandler infoHandler;

	public GuiGasGauge(IGasInfoHandler handler, Type type, IGuiWrapper gui, ResourceLocation def, int x, int y)
	{
		super(type, gui, def, x, y);

		infoHandler = handler;
	}
	
	public static GuiGasGauge getDummy(Type type, IGuiWrapper gui, ResourceLocation def, int x, int y)
	{
		GuiGasGauge gauge = new GuiGasGauge(null, type, gui, def, x, y);
		gauge.dummy = true;
		
		return gauge;
	}

	@Override
	public int getScaledLevel()
	{
		if(dummy)
		{
			return height-2;
		}
		
		return infoHandler.getTank().getGas() != null ? infoHandler.getTank().getStored()*(height-2) / infoHandler.getTank().getMaxGas() : 0;
	}

	@Override
	public IIcon getIcon()
	{
		if(dummy)
		{
			return dummyType.getIcon();
		}
		
		return (infoHandler.getTank() != null && infoHandler.getTank().getGas() != null && infoHandler.getTank().getGas().getGas() != null) ? infoHandler.getTank().getGas().getGas().getIcon() : null;
	}

	@Override
	public String getTooltipText()
	{
		if(dummy)
		{
			return dummyType.getLocalizedName();
		}
		
		return (infoHandler.getTank().getGas() != null) ? infoHandler.getTank().getGas().getGas().getLocalizedName() + ": " + infoHandler.getTank().getStored() : MekanismUtils.localize("gui.empty");
	}

	public static interface IGasInfoHandler
	{
		public GasTank getTank();
	}
}
