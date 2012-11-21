package mekanism.common;

/**
 * This class is designated to provide easy packet management in PacketHandler. Each type of packet is assigned to a
 * unique ID, which is sent to the server or client as a data int and then handled in PacketHandler along with it's
 * corresponding data.
 * @author AidanBrady
 *
 */
public enum EnumPacketType 
{
	/** Used for sending a time update to the server. Send this along with an int between 0 and 24. */
	TIME(0),
	/** Used for sending a weather update to the server. Send this along with an EnumWeatherType. */
	WEATHER(1),
	/** Used for sending a tile entity update to all clients. Send this along with x, y, and z coordinates of the block. */
	TILE_ENTITY(2),
	/** Used for sending a control panel GUI request to the server. */
	CONTROL_PANEL(3),
	/** A custom packet type. Handled in PacketHandler. */
	CUSTOM(4);
	
	/** The ID of the packet type */
	public final int id;
	
	private EnumPacketType(int i)
	{
		id = i;
	}
}
