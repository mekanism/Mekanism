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
	
	/** Used to request data from the server by tile entities. */
	DATA_REQUEST(8),
	
	/** Used to change a Configurator's state on the server side. */
	CONFIGURATOR_STATE(9),
	
	/** Used to change an Electric Bow's state on the server side. */
	ELECTRIC_BOW_STATE(10),
	
	/** Used to open an Electric Chest's GUI on the server side. */
	ELECTRIC_CHEST_SERVER_OPEN(11),
	
	/** Used to open an Electric Chest's GUI on the client side. */
	ELECTRIC_CHEST_CLIENT_OPEN(12),
	
	/** Used to send a password update packet to the server. */
	ELECTRIC_CHEST_PASSWORD(13),
	
	/** Used to send a lock update packet to the server. */
	ELECTRIC_CHEST_LOCK(14),
	
	/** Used to send a liquid transfer update packet to all clients. */
	LIQUID_TRANSFER_UPDATE(15),
	
	/** Used to send an energy transfer update packet to all clients. */
	ENERGY_TRANSFER_UPDATE(16),
	
	/** Used to send a gas transfer update packet to all clients. */
	GAS_TRANSFER_UPDATE(17),
	
	/** Used to send an electrolytic separator particle to all clients. */
	ELECTROLYTIC_SEPARATOR_PARTICLE(18),
	
	/** A custom packet type. Handled in PacketHandler. */
	CUSTOM(-1);
	
	/** The ID of the packet type */
	public final int id;
	
	private EnumPacketType(int i)
	{
		id = i;
	}
}
