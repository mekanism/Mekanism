package universalelectricity.electricity;

import java.util.EnumSet;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class ElectricityManagerTicker implements ITickHandler
{
	public static long inGameTicks = 0;

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData)
	{
		if (ElectricityManager.instance != null)
		{
			ElectricityManager.instance.onTick(type, tickData);
		}

		inGameTicks++;

		if (inGameTicks >= Long.MAX_VALUE)
		{
			inGameTicks = 0;
		}
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData)
	{
		
	}

	@Override
	public EnumSet<TickType> ticks()
	{
		return EnumSet.of(TickType.WORLD, TickType.WORLDLOAD, TickType.SERVER);
	}

	@Override
	public String getLabel()
	{
		return "Electricity Manager";
	}
}
