package mekanism.client.gui.element;

import static java.lang.Math.min;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.IGuiWrapper;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

public class GuiNumberGauge extends GuiGauge
{
	INumberInfoHandler infoHandler;

	public GuiNumberGauge(INumberInfoHandler handler, Type type, IGuiWrapper gui, ResourceLocation def, int x, int y)
	{
		super(type, gui, def, x, y);

		infoHandler = handler;
	}
	
	@Override
	public TransmissionType getTransmission()
	{
		return null;
	}

	@Override
	public int getScaledLevel()
	{
		return (int)((height-2) * min(infoHandler.getLevel() / infoHandler.getMaxLevel(), 1));
	}

	@Override
	public IIcon getIcon()
	{
		return infoHandler.getIcon();
	}

	@Override
	public String getTooltipText()
	{
		return infoHandler.getText(infoHandler.getLevel());
	}


	public static interface INumberInfoHandler
	{
		public IIcon getIcon();

		public double getLevel();

		public double getMaxLevel();

		public String getText(double level);
	}
}
