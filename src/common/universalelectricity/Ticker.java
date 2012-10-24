package universalelectricity;

import java.util.EnumSet;

import universalelectricity.electricity.ElectricityManager;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class Ticker implements ITickHandler
{
	public static long inGameTicks = 0;

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData)
	{

	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData)
	{
		if(ElectricityManager.instance != null)
		{
			ElectricityManager.instance.onTick(type, tickData);
		}
		
		inGameTicks ++;
        
        if(inGameTicks >= Long.MAX_VALUE)
        {
        	inGameTicks = 0;
        }
	}

	@Override
	public EnumSet<TickType> ticks()
	{
		return EnumSet.of(TickType.WORLD, TickType.WORLDLOAD, TickType.CLIENT, TickType.SERVER);
	}

	@Override
	public String getLabel()
	{
		return "Electricity Manager";
	}
}
