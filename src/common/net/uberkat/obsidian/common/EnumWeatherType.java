package net.uberkat.obsidian.common;

/**
 * This class is designated for easy management of weather packets sent to the server. Each weather type is set to a 
 * unique ID, which is sent to the server as a data int and handled in PacketHandler.
 * @author AidanBrady
 *
 */
public enum EnumWeatherType
{
	/** Clears the world of all weather effects, including rain, lightning, and clouds. */
	CLEAR(0),
	/** Sets the world's weather to thunder. This may or may not include rain. */
	STORM(1),
	/** Sets the world's weather to both thunder AND rain. */
	HAZE(2),
	/** Sets the world's weather to rain. */
	RAIN(3);
	
	/** The ID of the weather type */
	public final int id;
	
	private EnumWeatherType(int i)
	{
		id = i;
	}
}
	
