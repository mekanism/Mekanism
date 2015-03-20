package mekanism.common;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.Range4D;
import mekanism.common.network.PacketBoxBlacklist;
import mekanism.common.network.PacketBoxBlacklist.BoxBlacklistMessage;
import mekanism.common.network.PacketConfigSync;
import mekanism.common.network.PacketConfigSync.ConfigSyncMessage;
import mekanism.common.network.PacketConfigurationUpdate;
import mekanism.common.network.PacketConfigurationUpdate.ConfigurationUpdateMessage;
import mekanism.common.network.PacketConfiguratorState;
import mekanism.common.network.PacketConfiguratorState.ConfiguratorStateMessage;
import mekanism.common.network.PacketContainerEditMode;
import mekanism.common.network.PacketContainerEditMode.ContainerEditModeMessage;
import mekanism.common.network.PacketDataRequest;
import mekanism.common.network.PacketDataRequest.DataRequestMessage;
import mekanism.common.network.PacketDigitalMinerGui;
import mekanism.common.network.PacketDigitalMinerGui.DigitalMinerGuiMessage;
import mekanism.common.network.PacketDropperUse;
import mekanism.common.network.PacketDropperUse.DropperUseMessage;
import mekanism.common.network.PacketEditFilter;
import mekanism.common.network.PacketEditFilter.EditFilterMessage;
import mekanism.common.network.PacketElectricBowState;
import mekanism.common.network.PacketElectricBowState.ElectricBowStateMessage;
import mekanism.common.network.PacketElectricChest;
import mekanism.common.network.PacketElectricChest.ElectricChestMessage;
import mekanism.common.network.PacketFlamethrowerActive;
import mekanism.common.network.PacketFlamethrowerActive.FlamethrowerActiveMessage;
import mekanism.common.network.PacketJetpackData;
import mekanism.common.network.PacketJetpackData.JetpackDataMessage;
import mekanism.common.network.PacketKey;
import mekanism.common.network.PacketKey.KeyMessage;
import mekanism.common.network.PacketLogisticalSorterGui;
import mekanism.common.network.PacketLogisticalSorterGui.LogisticalSorterGuiMessage;
import mekanism.common.network.PacketNewFilter;
import mekanism.common.network.PacketNewFilter.NewFilterMessage;
import mekanism.common.network.PacketOredictionificatorGui;
import mekanism.common.network.PacketOredictionificatorGui.OredictionificatorGuiMessage;
import mekanism.common.network.PacketPortableTankState;
import mekanism.common.network.PacketPortableTankState.PortableTankStateMessage;
import mekanism.common.network.PacketPortableTeleporter;
import mekanism.common.network.PacketPortableTeleporter.PortableTeleporterMessage;
import mekanism.common.network.PacketPortalFX;
import mekanism.common.network.PacketPortalFX.PortalFXMessage;
import mekanism.common.network.PacketRedstoneControl;
import mekanism.common.network.PacketRedstoneControl.RedstoneControlMessage;
import mekanism.common.network.PacketRemoveUpgrade;
import mekanism.common.network.PacketRemoveUpgrade.RemoveUpgradeMessage;
import mekanism.common.network.PacketRobit;
import mekanism.common.network.PacketRobit.RobitMessage;
import mekanism.common.network.PacketScubaTankData;
import mekanism.common.network.PacketScubaTankData.ScubaTankDataMessage;
import mekanism.common.network.PacketSimpleGui;
import mekanism.common.network.PacketSimpleGui.SimpleGuiMessage;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.network.PacketTransmitterUpdate;
import mekanism.common.network.PacketTransmitterUpdate.TransmitterUpdateMessage;
import mekanism.common.network.PacketWalkieTalkieState;
import mekanism.common.network.PacketWalkieTalkieState.WalkieTalkieStateMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

/**
 * Mekanism packet handler. As always, use packets sparingly!
 * @author AidanBrady
 *
 */
public class PacketHandler
{
	public SimpleNetworkWrapper netHandler = NetworkRegistry.INSTANCE.newSimpleChannel("MEK");
	
	public void initialize()
	{
		netHandler.registerMessage(PacketRobit.class, RobitMessage.class, 0, Side.SERVER);
		netHandler.registerMessage(PacketTransmitterUpdate.class, TransmitterUpdateMessage.class, 1, Side.CLIENT);
		netHandler.registerMessage(PacketElectricChest.class, ElectricChestMessage.class, 2, Side.CLIENT);
		netHandler.registerMessage(PacketElectricChest.class, ElectricChestMessage.class, 2, Side.SERVER);
		netHandler.registerMessage(PacketElectricBowState.class, ElectricBowStateMessage.class, 3, Side.SERVER);
		netHandler.registerMessage(PacketConfiguratorState.class, ConfiguratorStateMessage.class, 4, Side.SERVER);
		netHandler.registerMessage(PacketTileEntity.class, TileEntityMessage.class, 5, Side.CLIENT);
		netHandler.registerMessage(PacketTileEntity.class, TileEntityMessage.class, 5, Side.SERVER);
		netHandler.registerMessage(PacketPortalFX.class, PortalFXMessage.class, 6, Side.CLIENT);
		netHandler.registerMessage(PacketDataRequest.class, DataRequestMessage.class, 7, Side.SERVER);
		netHandler.registerMessage(PacketOredictionificatorGui.class, OredictionificatorGuiMessage.class, 8, Side.CLIENT);
		netHandler.registerMessage(PacketOredictionificatorGui.class, OredictionificatorGuiMessage.class, 8, Side.SERVER);
		//EMPTY SLOT 9
		netHandler.registerMessage(PacketPortableTeleporter.class, PortableTeleporterMessage.class, 10, Side.CLIENT);
		netHandler.registerMessage(PacketPortableTeleporter.class, PortableTeleporterMessage.class, 10, Side.SERVER);
		netHandler.registerMessage(PacketRemoveUpgrade.class, RemoveUpgradeMessage.class, 11, Side.SERVER);
		netHandler.registerMessage(PacketRedstoneControl.class, RedstoneControlMessage.class, 12, Side.SERVER);
		netHandler.registerMessage(PacketWalkieTalkieState.class, WalkieTalkieStateMessage.class, 13, Side.SERVER);
		netHandler.registerMessage(PacketLogisticalSorterGui.class, LogisticalSorterGuiMessage.class, 14, Side.CLIENT);
		netHandler.registerMessage(PacketLogisticalSorterGui.class, LogisticalSorterGuiMessage.class, 14, Side.SERVER);
		netHandler.registerMessage(PacketNewFilter.class, NewFilterMessage.class, 15, Side.SERVER);
		netHandler.registerMessage(PacketEditFilter.class, EditFilterMessage.class, 16, Side.SERVER);
		netHandler.registerMessage(PacketConfigurationUpdate.class, ConfigurationUpdateMessage.class, 17, Side.SERVER);
		netHandler.registerMessage(PacketSimpleGui.class, SimpleGuiMessage.class, 18, Side.CLIENT);
		netHandler.registerMessage(PacketSimpleGui.class, SimpleGuiMessage.class, 18, Side.SERVER);
		netHandler.registerMessage(PacketDigitalMinerGui.class, DigitalMinerGuiMessage.class, 19, Side.CLIENT);
		netHandler.registerMessage(PacketDigitalMinerGui.class, DigitalMinerGuiMessage.class, 19, Side.SERVER);
		netHandler.registerMessage(PacketJetpackData.class, JetpackDataMessage.class, 20, Side.CLIENT);
		netHandler.registerMessage(PacketJetpackData.class, JetpackDataMessage.class, 20, Side.SERVER);
		netHandler.registerMessage(PacketKey.class, KeyMessage.class, 21, Side.SERVER);
		netHandler.registerMessage(PacketScubaTankData.class, ScubaTankDataMessage.class, 22, Side.CLIENT);
		netHandler.registerMessage(PacketScubaTankData.class, ScubaTankDataMessage.class, 22, Side.SERVER);
		netHandler.registerMessage(PacketConfigSync.class, ConfigSyncMessage.class, 23, Side.CLIENT);
		netHandler.registerMessage(PacketBoxBlacklist.class, BoxBlacklistMessage.class, 24, Side.CLIENT);
		netHandler.registerMessage(PacketPortableTankState.class, PortableTankStateMessage.class, 25, Side.SERVER);
		netHandler.registerMessage(PacketContainerEditMode.class, ContainerEditModeMessage.class, 26, Side.SERVER);
		netHandler.registerMessage(PacketFlamethrowerActive.class, FlamethrowerActiveMessage.class, 27, Side.CLIENT);
		netHandler.registerMessage(PacketFlamethrowerActive.class, FlamethrowerActiveMessage.class, 27, Side.SERVER);
		netHandler.registerMessage(PacketDropperUse.class, DropperUseMessage.class, 28, Side.SERVER);
	}
	
	/**
	 * Encodes an Object[] of data into a DataOutputStream.
	 * @param dataValues - an Object[] of data to encode
	 * @param output - the output stream to write to
	 */
	public static void encode(Object[] dataValues, ByteBuf output)
	{
		try {
			for(Object data : dataValues)
			{
				if(data instanceof Integer)
				{
					output.writeInt((Integer)data);
				}
				else if(data instanceof Boolean)
				{
					output.writeBoolean((Boolean)data);
				}
				else if(data instanceof Double)
				{
					output.writeDouble((Double)data);
				}
				else if(data instanceof Float)
				{
					output.writeFloat((Float)data);
				}
				else if(data instanceof String)
				{
					writeString(output, (String)data);
				}
				else if(data instanceof Byte)
				{
					output.writeByte((Byte)data);
				}
				else if(data instanceof ItemStack)
				{
					writeStack(output, (ItemStack)data);
				}
				else if(data instanceof NBTTagCompound)
				{
					writeNBT(output, (NBTTagCompound)data);
				}
				else if(data instanceof int[])
				{
					for(int i : (int[])data)
					{
						output.writeInt(i);
					}
				}
				else if(data instanceof byte[])
				{
					for(byte b : (byte[])data)
					{
						output.writeByte(b);
					}
				}
				else if(data instanceof ArrayList)
				{
					encode(((ArrayList)data).toArray(), output);
				}
			}
		} catch(Exception e) {
			Mekanism.logger.error("Error while encoding packet data.");
			e.printStackTrace();
		}
	}
	
	public static void writeString(ByteBuf output, String s)
	{
		output.writeInt(s.getBytes().length);
		output.writeBytes(s.getBytes());
	}
	
	public static String readString(ByteBuf input)
	{
		return new String(input.readBytes(input.readInt()).array());
	}
	
	public static void writeStack(ByteBuf output, ItemStack stack)
	{
		output.writeInt(stack != null ? Item.getIdFromItem(stack.getItem()) : -1);
		
		if(stack != null)
		{
			output.writeInt(stack.stackSize);
			output.writeInt(stack.getItemDamage());
			
			if(stack.getTagCompound() != null && stack.getItem().getShareTag())
			{
				output.writeBoolean(true);
				writeNBT(output, stack.getTagCompound());
			}
			else {
				output.writeBoolean(false);
			}
		}
	}
	
	public static ItemStack readStack(ByteBuf input)
	{
		int id = input.readInt();
		
		if(id >= 0)
		{
			ItemStack stack = new ItemStack(Item.getItemById(id), input.readInt(), input.readInt());
			
			if(input.readBoolean())
			{
				stack.setTagCompound(readNBT(input));
			}
			
			return stack;
		}
		
		return null;
	}
	
	public static void writeNBT(ByteBuf output, NBTTagCompound nbtTags)
	{
		try {
			byte[] buffer = CompressedStreamTools.compress(nbtTags);
			
			output.writeInt(buffer.length);
			output.writeBytes(buffer);
		} catch(Exception e) {}
	}
	
	public static NBTTagCompound readNBT(ByteBuf input)
	{
		try {
			byte[] buffer = new byte[input.readInt()];
			input.readBytes(buffer);
			
			return CompressedStreamTools.func_152457_a(buffer, new NBTSizeTracker(2097152L));
		} catch(Exception e) {
			return null;
		}
	}
	
	public static EntityPlayer getPlayer(MessageContext context)
	{
		return Mekanism.proxy.getPlayer(context);
	}


	/**
	 * Send this message to the specified player.
	 * @param message - the message to send
	 * @param player - the player to send it to
	 */
	public void sendTo(IMessage message, EntityPlayerMP player)
	{
		netHandler.sendTo(message, player);
	}

	/**
	 * Send this message to everyone within a certain range of a point.
	 *
	 * @param message - the message to send
	 * @param point - the TargetPoint around which to send
	 */
	public void sendToAllAround(IMessage message, NetworkRegistry.TargetPoint point)
	{
		netHandler.sendToAllAround(message, point);
	}

	/**
	 * Send this message to everyone within the supplied dimension.
	 * @param message - the message to send
	 * @param dimensionId - the dimension id to target
	 */
	public void sendToDimension(IMessage message, int dimensionId)
	{
		netHandler.sendToDimension(message, dimensionId);
	}

	/**
	 * Send this message to the server.
	 * @param message - the message to send
	 */
	public void sendToServer(IMessage message)
	{
		netHandler.sendToServer(message);
	}
	
	/**
	 * Send this message to all players within a defined AABB cuboid.
	 * @param message - the message to send
	 * @param cuboid - the AABB cuboid to send the packet in
	 * @param dimId - the dimension the cuboid is in
	 */
	public void sendToCuboid(IMessage message, AxisAlignedBB cuboid, int dimId)
	{
		MinecraftServer server = MinecraftServer.getServer();

		if(server != null && cuboid != null)
		{
			for(EntityPlayerMP player : (List<EntityPlayerMP>)server.getConfigurationManager().playerEntityList)
			{
				if(player.dimension == dimId && cuboid.isVecInside(Vec3.createVectorHelper(player.posX, player.posY, player.posZ)))
				{
					sendTo(message, player);
				}
			}
		}
	}
	
	public void sendToReceivers(IMessage message, Range4D range)
	{
		MinecraftServer server = MinecraftServer.getServer();

		if(server != null)
		{
			for(EntityPlayerMP player : (List<EntityPlayerMP>)server.getConfigurationManager().playerEntityList)
			{
				if(player.dimension == range.dimensionId && Range4D.getChunkRange(player).intersects(range))
				{
					sendTo(message, player);
				}
			}
		}
	}
}
