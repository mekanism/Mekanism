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
	/** Used to send a portal FX packet to all clients. */
	PORTAL_FX(4),
	/** Used to send a teleport packet from an ItemStack to the server. */
	PORTABLE_TELEPORT(5),
	/** Used to send a digit update packet from a portable teleporter to the server. */
	DIGIT_UPDATE(6),
	/** Used to send a status update packet from a portable teleporter to the client. */
	STATUS_UPDATE(7),
	/** A custom packet type. Handled in PacketHandler. */
	CUSTOM(8);
	
	/** The ID of the packet type */
	public final int id;
	
	private EnumPacketType(int i)
	{
		id = i;
	}
}
