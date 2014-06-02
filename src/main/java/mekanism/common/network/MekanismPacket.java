package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;

/**
 * AbstractPacket class. Should be the parent of all packets wishing to use the
 * PacketPipeline.
 *
 * @author unpairedbracket based on sirgingalot's tutorial
 */
public abstract class MekanismPacket
{
	/**
	 * Encode the packet data into the ByteBuf stream. Complex data sets may
	 * need specific data handlers (See
	 * @link{cpw.mods.fml.common.network.ByteBuffUtils})
	 *
	 * @param ctx - channel context
	 * @param buffer - the buffer to encode into
	 */
	public abstract void write(ChannelHandlerContext ctx, ByteBuf dataStream) throws Exception;

	/**
	 * Decode the packet data from the ByteBuf stream. Complex data sets may
	 * need specific data handlers (See
	 * @link{cpw.mods.fml.common.network.ByteBuffUtils})
	 *
	 * @param ctx - channel context
	 * @param buffer - the buffer to decode from
	 */
	public abstract void read(ChannelHandlerContext ctx, EntityPlayer player, ByteBuf dataStream) throws Exception;

	/**
	 * Handle a packet on the client side. Note this occurs after decoding has
	 * completed.
	 *
	 * @param player - the player reference
	 */
	public abstract void handleClientSide(EntityPlayer player) throws Exception;

	/**
	 * Handle a packet on the server side. Note this occurs after decoding has
	 * completed.
	 *
	 * @param player - the player reference
	 */
	public abstract void handleServerSide(EntityPlayer player) throws Exception;
}
