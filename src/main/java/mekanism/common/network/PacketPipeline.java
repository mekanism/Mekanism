package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Packet pipeline class. Directs all registered packet data to be handled by
 * the packets themselves.
 *
 * @author unpairedbracket based on sirgingalot's tutorial
 */
@ChannelHandler.Sharable
public class PacketPipeline extends MessageToMessageCodec<FMLProxyPacket, MekanismPacket>
{
	private EnumMap<Side, FMLEmbeddedChannel> channels;
	
	private LinkedList<Class<? extends MekanismPacket>> packets = new LinkedList<Class<? extends MekanismPacket>>();
	
	private boolean isPostInitialised = false;

	/**
	 * Register your packet with the pipeline. Discriminators are automatically
	 * set.
	 *
	 * @param clazz - the class to register
	 *
	 * @return whether registration was successful. Failure may occur if 256
	 *         packets have been registered or if the registry already contains
	 *         this packet
	 */
	public boolean registerPacket(Class<? extends MekanismPacket> clazz)
	{
		if(packets.size() > 256)
		{
			// You should log here!!
			return false;
		}

		if(packets.contains(clazz))
		{
			// You should log here!!
			return false;
		}

		if(isPostInitialised)
		{
			// You should log here!!
			return false;
		}

		packets.add(clazz);
		
		return true;
	}

	// In line encoding of the packet, including discriminator setting
	@Override
	protected void encode(ChannelHandlerContext ctx, MekanismPacket msg, List<Object> out) throws Exception
	{
		ByteBuf buffer = Unpooled.buffer();
		Class<? extends MekanismPacket> clazz = msg.getClass();
		
		if(!packets.contains(msg.getClass()))
		{
			throw new NullPointerException("No Packet Registered for: " + msg.getClass().getCanonicalName());
		}

		byte discriminator = (byte)packets.indexOf(clazz);
		buffer.writeByte(discriminator);
		msg.write(ctx, buffer);
		FMLProxyPacket proxyPacket = new FMLProxyPacket(buffer.copy(), ctx.channel().attr(NetworkRegistry.FML_CHANNEL).get());
		out.add(proxyPacket);
	}

	// In line decoding and handling of the packet
	@Override
	protected void decode(ChannelHandlerContext ctx, FMLProxyPacket msg, List<Object> out) throws Exception
	{
		ByteBuf payload = msg.payload();
		byte discriminator = payload.readByte();
		Class<? extends MekanismPacket> clazz = packets.get(discriminator);
		
		if(clazz == null)
		{
			throw new NullPointerException("No packet registered for discriminator: " + discriminator);
		}
		
		EntityPlayer player = null;
		
		switch(FMLCommonHandler.instance().getEffectiveSide())
		{
			case CLIENT:
				player = getClientPlayer();
				break;
			case SERVER:
				INetHandler netHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
				player = ((NetHandlerPlayServer)netHandler).playerEntity;
				break;
		}

		MekanismPacket pkt = clazz.newInstance();
		pkt.read(ctx, player, payload.slice());
		
		switch(FMLCommonHandler.instance().getEffectiveSide())
		{
			case CLIENT:
				pkt.handleClientSide(player);
				break;
			case SERVER:
				pkt.handleServerSide(player);
				break;
		}

		out.add(pkt);
	}

	// Method to call from FMLInitializationEvent
	public void initalise()
	{
		channels = NetworkRegistry.INSTANCE.newChannel("Mekanism", this);
		registerPackets();
	}

	public void registerPackets()
	{
		//Packet registrations
		registerPacket(PacketRobit.class);
		registerPacket(PacketTransmitterUpdate.class);
		registerPacket(PacketElectricChest.class);
		registerPacket(PacketElectricBowState.class);
		registerPacket(PacketConfiguratorState.class);
		registerPacket(PacketTileEntity.class);
		registerPacket(PacketPortalFX.class);
		registerPacket(PacketDataRequest.class);
		registerPacket(PacketStatusUpdate.class);
		registerPacket(PacketDigitUpdate.class);
		registerPacket(PacketPortableTeleport.class);
		registerPacket(PacketRemoveUpgrade.class);
		registerPacket(PacketRedstoneControl.class);
		registerPacket(PacketWalkieTalkieState.class);
		registerPacket(PacketLogisticalSorterGui.class);
		registerPacket(PacketNewFilter.class);
		registerPacket(PacketEditFilter.class);
		registerPacket(PacketConfigurationUpdate.class);
		registerPacket(PacketSimpleGui.class);
		registerPacket(PacketDigitalMinerGui.class);
		registerPacket(PacketJetpackData.class);
		registerPacket(PacketKey.class);
		registerPacket(PacketScubaTankData.class);
		registerPacket(PacketConfigSync.class);
		registerPacket(PacketBoxBlacklist.class);
	}

	// Method to call from FMLPostInitializationEvent
	// Ensures that packet discriminators are common between server and client
	// by using logical sorting
	public void postInitialise()
	{
		if(isPostInitialised)
		{
			return;
		}

		isPostInitialised = true;
		Collections.sort(packets, new Comparator<Class<? extends MekanismPacket>>() {
			@Override
			public int compare(Class<? extends MekanismPacket> clazz1, Class<? extends MekanismPacket> clazz2)
			{
				int com = String.CASE_INSENSITIVE_ORDER.compare(clazz1.getCanonicalName(), clazz2.getCanonicalName());
				
				if(com == 0)
				{
					com = clazz1.getCanonicalName().compareTo(clazz2.getCanonicalName());
				}

				return com;
			}
		});
	}

	@SideOnly(Side.CLIENT)
	private EntityPlayer getClientPlayer()
	{
		return Minecraft.getMinecraft().thePlayer;
	}

	/**
	 * Send this message to everyone.
	 * <p/>
	 * Adapted from CPW's code in
	 * cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper
	 *
	 * @param message
	 *            The message to send
	 */
	public void sendToAll(MekanismPacket message)
	{
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALL);
		channels.get(Side.SERVER).writeAndFlush(message);
	}

	/**
	 * Send this message to the specified player.
	 * <p/>
	 * Adapted from CPW's code in
	 * cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper
	 *
	 * @param message
	 *            The message to send
	 * @param player
	 *            The player to send it to
	 */
	public void sendTo(MekanismPacket message, EntityPlayerMP player)
	{
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
		channels.get(Side.SERVER).writeAndFlush(message);
	}

	/**
	 * Send this message to everyone within a certain range of a point.
	 * <p/>
	 * Adapted from CPW's code in
	 * cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper
	 *
	 * @param message
	 *            The message to send
	 * @param point
	 *            The
	 *            {@link cpw.mods.fml.common.network.NetworkRegistry.TargetPoint}
	 *            around which to send
	 */
	public void sendToAllAround(MekanismPacket message, NetworkRegistry.TargetPoint point)
	{
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT);
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(point);
		channels.get(Side.SERVER).writeAndFlush(message);
	}

	/**
	 * Send this message to everyone within the supplied dimension.
	 * <p/>
	 * Adapted from CPW's code in
	 * cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper
	 *
	 * @param message
	 *            The message to send
	 * @param dimensionId
	 *            The dimension id to target
	 */
	public void sendToDimension(MekanismPacket message, int dimensionId)
	{
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.DIMENSION);
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(dimensionId);
		channels.get(Side.SERVER).writeAndFlush(message);
	}

	/**
	 * Send this message to the server.
	 * <p/>
	 * Adapted from CPW's code in
	 * cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper
	 *
	 * @param message
	 *            The message to send
	 */
	public void sendToServer(MekanismPacket message)
	{
		channels.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.TOSERVER);
		channels.get(Side.CLIENT).writeAndFlush(message);
	}
}