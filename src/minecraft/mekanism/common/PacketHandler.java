package mekanism.common;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import universalelectricity.core.electricity.ElectricityPack;
import universalelectricity.core.item.IItemElectric;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

/**
 * Mekanism packet handler. As always, use packets sparingly!
 * @author AidanBrady
 *
 */
public class PacketHandler implements IPacketHandler
{
	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
	{
		ByteArrayDataInput dataStream = ByteStreams.newDataInput(packet.data);
        EntityPlayer entityplayer = (EntityPlayer)player;
        
		if(packet.channel.equals("Mekanism"))
		{
			try {
				int packetType = dataStream.readInt();
				
			    if(packetType == EnumPacketType.TIME.id)
			    {
			        System.out.println("[Mekanism] Received time update packet from " + entityplayer.username + ".");
			        entityplayer.getCurrentEquippedItem().damageItem(4999, entityplayer);
			        MekanismUtils.setHourForward(entityplayer.worldObj, dataStream.readInt());
			    }
			    else if(packetType == EnumPacketType.WEATHER.id)
			    {
			    	System.out.println("[Mekanism] Received weather update packet from " + entityplayer.username + ".");
			    	entityplayer.getCurrentEquippedItem().damageItem(4999, entityplayer);
			    	int weatherType = dataStream.readInt();
			    	
			    	if(weatherType == EnumWeatherType.CLEAR.id)
			    	{
			    		entityplayer.worldObj.getWorldInfo().setRaining(false);
				        entityplayer.worldObj.getWorldInfo().setThundering(false);
			    	}
			    	if(weatherType == EnumWeatherType.HAZE.id)
			    	{
			    		entityplayer.worldObj.getWorldInfo().setRaining(true);
				        entityplayer.worldObj.getWorldInfo().setThundering(true);
			    	}
			    	if(weatherType == EnumWeatherType.RAIN.id)
			    	{
			    		entityplayer.worldObj.getWorldInfo().setRaining(true);
			    	}
			    	if(weatherType == EnumWeatherType.STORM.id)
			    	{
				    	entityplayer.worldObj.getWorldInfo().setThundering(true);
			    	}
			    }
			    else if(packetType == EnumPacketType.TILE_ENTITY.id)
			    {
			    	try {
						int x = dataStream.readInt();
						int y = dataStream.readInt();
						int z = dataStream.readInt();
						
						World world = entityplayer.worldObj;
						TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
						if(tileEntity instanceof ITileNetwork)
						{
							((ITileNetwork)tileEntity).handlePacketData(dataStream);
						}
					} catch(Exception e) {
						System.err.println("[Mekanism] Error while handling tile entity packet.");
						e.printStackTrace();
					}
			    }
			    else if(packetType == EnumPacketType.CONTROL_PANEL.id)
			    {
			    	try {
			    		String modClass = dataStream.readUTF();
			    		String modInstance = dataStream.readUTF();
			    		int x = dataStream.readInt();
			    		int y = dataStream.readInt();
			    		int z = dataStream.readInt();
			    		int guiId = dataStream.readInt();
			    		
			    		Class mod = Class.forName(modClass);
			    		
			    		if(mod == null)
			    		{
			    			System.err.println("[Mekanism] Incorrectly implemented IAccessibleGui -- ignoring handler packet.");
			    			System.err.println(" ~ Unable to locate class '" + modClass + ".'");
			    			System.err.println(" ~ GUI Container may not function correctly.");
			    			return;
			    		}
			    		
			    		Object instance = mod.getField(modInstance).get(null);
			    		
			    		if(instance == null)
			    		{
			    			System.err.println("[Mekanism] Incorrectly implemented IAccessibleGui -- ignoring handler packet.");
			    			System.err.println(" ~ Unable to locate instance object '" + modInstance + ".'");
			    			System.err.println(" ~ GUI Container may not function correctly.");
			    			return;
			    		}
			    		
			    		entityplayer.openGui(instance, guiId, entityplayer.worldObj, x, y, z);
			    	} catch(Exception e) {
			    		System.err.println("[Mekanism] Error while handling control panel packet.");
			    		e.printStackTrace();
			    	}
			    }
			    else if(packetType == EnumPacketType.PORTAL_FX.id)
			    {
			    	try {
			    		Random random = new Random();
			    		int x = dataStream.readInt();
			    		int y = dataStream.readInt();
			    		int z = dataStream.readInt();
			    		
						for(int i = 0; i < 50; i++)
						{
							entityplayer.worldObj.spawnParticle("portal", x + random.nextFloat(), y + random.nextFloat(), z + random.nextFloat(), 0.0F, 0.0F, 0.0F);
							entityplayer.worldObj.spawnParticle("portal", x + random.nextFloat(), y + 1 + random.nextFloat(), z + random.nextFloat(), 0.0F, 0.0F, 0.0F);
						}
			    	} catch(Exception e) {
			    		System.err.println("[Mekanism] Error while handling portal FX packet.");
			    		e.printStackTrace();
			    	}
			    }
			    else if(packetType == EnumPacketType.DIGIT_UPDATE.id)
			    {
			    	try {
			    		int index = dataStream.readInt();
			    		int digit = dataStream.readInt();
			    		
			    		ItemStack currentStack = entityplayer.getCurrentEquippedItem();
			    		
			    		if(currentStack != null && currentStack.getItem() instanceof ItemPortableTeleporter)
			    		{
			    			ItemPortableTeleporter item = (ItemPortableTeleporter)currentStack.getItem();
			    			item.setDigit(currentStack, index, digit);
			    		}
			    	} catch(Exception e) {
			    		System.err.println("[Mekanism] Error while handling digit update packet.");
			    		e.printStackTrace();
			    	}
			    }
			    else if(packetType == EnumPacketType.STATUS_UPDATE.id)
			    {
			    	try {
			    		ItemStack currentStack = entityplayer.getCurrentEquippedItem();
			    		
			    		if(currentStack != null && currentStack.getItem() instanceof ItemPortableTeleporter)
			    		{
			    			ItemPortableTeleporter item = (ItemPortableTeleporter)currentStack.getItem();
			    			item.setStatus(currentStack, dataStream.readInt());
			    		}
			    	} catch(Exception e) {
			    		System.err.println("[Mekanism] Error while handling status update packet.");
			    		e.printStackTrace();
			    	}
			    }
			    else if(packetType == EnumPacketType.PORTABLE_TELEPORT.id)
			    {
			    	try {
			    		if(entityplayer instanceof EntityPlayerMP)
			    		{
			    			EntityPlayerMP entityPlayerMP = (EntityPlayerMP)entityplayer;
			    			ItemStack itemstack = entityPlayerMP.getCurrentEquippedItem();
			    			
			    			if(itemstack != null && itemstack.getItem() instanceof ItemPortableTeleporter)
			    			{
			    				ItemPortableTeleporter item = (ItemPortableTeleporter)itemstack.getItem();
			    				
			    				if(item.getStatus(itemstack) == 1)
			    				{
			    					Teleporter.Coords coords = MekanismUtils.getClosestCoords(new Teleporter.Code(item.getDigit(itemstack, 0), item.getDigit(itemstack, 1), item.getDigit(itemstack, 2), item.getDigit(itemstack, 3)), entityPlayerMP);
			    					
			    					item.onProvide(new ElectricityPack(item.calculateEnergyCost(entityPlayerMP, coords)/120, 120), itemstack);
			    					
			    					if(entityPlayerMP.worldObj.provider.dimensionId != coords.dimensionId)
			    					{
			    						entityPlayerMP.travelToDimension(coords.dimensionId);
			    					}
			    					
			    					entityPlayerMP.playerNetServerHandler.setPlayerLocation(coords.xCoord+0.5, coords.yCoord, coords.zCoord+0.5, entityPlayerMP.rotationYaw, entityPlayerMP.rotationPitch);
			    					
			    					entityplayer.worldObj.playSoundAtEntity(entityplayer, "mob.endermen.portal", 1.0F, 1.0F);
			    					sendPortalFX(coords.xCoord, coords.yCoord, coords.zCoord, coords.dimensionId);
			    				}
			    			}
			    		}
			    	} catch(Exception e) {
			    		System.err.println("[Mekanism] Error while handling portable teleport packet.");
			    		e.printStackTrace();
			    	}
			    }
			    else if(packetType == EnumPacketType.DATA_REQUEST.id)
			    {
			    	try {
			    		int x = dataStream.readInt();
			    		int y = dataStream.readInt();
			    		int z = dataStream.readInt();
			    		int id = dataStream.readInt();
			    		
			    		World world = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(id);
			    		
			    		if(world != null && world.getBlockTileEntity(x, y, z) instanceof ITileNetwork)
			    		{
			    			sendTileEntityPacketToClients(world.getBlockTileEntity(x, y, z), 0, ((ITileNetwork)world.getBlockTileEntity(x, y, z)).getNetworkedData(new ArrayList()));
			    		}
			    	} catch (Exception e) {
			    		System.err.println("[Mekanism] Error while handling data request packet.");
			    		e.printStackTrace();
			    	}
			    }
			    else if(packetType == EnumPacketType.CONFIGURATOR_STATE.id)
			    {
			    	try {
			    		int state = dataStream.readInt();
			    		
			    		EntityPlayerMP entityPlayerMP = (EntityPlayerMP)entityplayer;
			    		ItemStack itemstack = entityPlayerMP.getCurrentEquippedItem();
			    		
			    		if(itemstack != null && itemstack.getItem() instanceof ItemConfigurator)
			    		{
			    			((ItemConfigurator)itemstack.getItem()).setState(itemstack, (byte)state);
			    		}
			    	} catch(Exception e) {
			    		System.err.println("[Mekanism] Error while handling configurator state packet.");
			    		e.printStackTrace();
			    	}
			    }
			    else if(packetType == EnumPacketType.ELECTRIC_BOW_STATE.id)
			    {
			    	try {
			    		boolean state = dataStream.readInt() == 1;
			    		
			    		EntityPlayerMP entityPlayerMP = (EntityPlayerMP)entityplayer;
			    		ItemStack itemstack = entityPlayerMP.getCurrentEquippedItem();
			    		
			    		if(itemstack != null && itemstack.getItem() instanceof ItemElectricBow)
			    		{
			    			((ItemElectricBow)itemstack.getItem()).setFireState(itemstack, state);
			    		}
			    	} catch(Exception e) {
			       		System.err.println("[Mekanism] Error while handling electric bow state packet.");
			    		e.printStackTrace();
			    	}
			    }
			    else if(packetType == EnumPacketType.ELECTRIC_CHEST_SERVER_OPEN.id)
			    {
			    	try {
			    		boolean isBlock = dataStream.readBoolean();
			    		boolean useEnergy = dataStream.readBoolean();
			    		
			    		if(isBlock)
			    		{
				    		int x = dataStream.readInt();
				    		int y = dataStream.readInt();
				    		int z = dataStream.readInt();
				    		
				    		TileEntityElectricChest tileEntity = (TileEntityElectricChest)entityplayer.worldObj.getBlockTileEntity(x, y, z);
				    		
				    		if(useEnergy)
				    		{
				    			tileEntity.setJoules(tileEntity.getJoules() - 100);
				    		}
				    		
				    		MekanismUtils.openElectricChestGui((EntityPlayerMP)entityplayer, tileEntity, null, true);
			    		}
			    		else {
			    			ItemStack stack = entityplayer.getCurrentEquippedItem();
			    			
			    			if(stack != null && stack.getItem() instanceof IElectricChest && ((IElectricChest)stack.getItem()).isElectricChest(stack))
			    			{
			    				if(useEnergy)
			    				{
			    					((IItemElectric)stack.getItem()).setJoules(((IItemElectric)stack.getItem()).getJoules(stack) - 100, stack);
			    				}
			    				
			    				InventoryElectricChest inventory = new InventoryElectricChest(stack);
			    				MekanismUtils.openElectricChestGui((EntityPlayerMP)entityplayer, null, inventory, false);
			    			}
			    		}
			    	} catch(Exception e) {
			       		System.err.println("[Mekanism] Error while handling electric chest open packet.");
			    		e.printStackTrace();
			    	}
			    }
			    else if(packetType == EnumPacketType.ELECTRIC_CHEST_CLIENT_OPEN.id)
			    {
			    	try {
			    		int id = dataStream.readInt();
			    		int windowId = dataStream.readInt();
			    		boolean isBlock = dataStream.readBoolean();
			    		int x = 0;
			    		int y = 0;
			    		int z = 0;
			    		
			    		if(isBlock)
			    		{
			        		x = dataStream.readInt();
				    		y = dataStream.readInt();
				    		z = dataStream.readInt();
			    		}
			    		
			    		Mekanism.proxy.openElectricChest(entityplayer, id, windowId, isBlock, x, y, z);
			    	} catch(Exception e) {
			       		System.err.println("[Mekanism] Error while handling electric chest open packet.");
			    		e.printStackTrace();
			    	}
			    }
			    else if(packetType == EnumPacketType.ELECTRIC_CHEST_PASSWORD.id)
			    {
			    	try {
			    		boolean isBlock = dataStream.readBoolean();
			    		String password = dataStream.readUTF();
			    		
			    		if(isBlock)
			    		{
				    		int x = dataStream.readInt();
				    		int y = dataStream.readInt();
				    		int z = dataStream.readInt();
				    		
				    		TileEntityElectricChest tileEntity = (TileEntityElectricChest)entityplayer.worldObj.getBlockTileEntity(x, y, z);
				    		tileEntity.password = password;
				    		tileEntity.authenticated = true;
			    		}
			    		else {
			    			ItemStack stack = entityplayer.getCurrentEquippedItem();
			    			
			    			if(stack != null && stack.getItem() instanceof IElectricChest && ((IElectricChest)stack.getItem()).isElectricChest(stack))
			    			{
			    				((IElectricChest)stack.getItem()).setPassword(stack, password);
			    				((IElectricChest)stack.getItem()).setAuthenticated(stack, true);
			    			}
			    		}
			    	} catch(Exception e) {
			       		System.err.println("[Mekanism] Error while handling electric chest password packet.");
			    		e.printStackTrace();
			    	}
			    }
			    else if(packetType == EnumPacketType.ELECTRIC_CHEST_LOCK.id)
			    {
			    	try {
			    		boolean isBlock = dataStream.readBoolean();
			    		boolean locked = dataStream.readBoolean();
			    		
			    		if(isBlock)
			    		{
				    		int x = dataStream.readInt();
				    		int y = dataStream.readInt();
				    		int z = dataStream.readInt();
				    		
				    		TileEntityElectricChest tileEntity = (TileEntityElectricChest)entityplayer.worldObj.getBlockTileEntity(x, y, z);
				    		tileEntity.locked = locked;
			    		}
			    		else {
			    			ItemStack stack = entityplayer.getCurrentEquippedItem();
			    			
			    			if(stack != null && stack.getItem() instanceof IElectricChest && ((IElectricChest)stack.getItem()).isElectricChest(stack))
			    			{
			    				((IElectricChest)stack.getItem()).setLocked(stack, locked);
			    			}
			    		}
			    	} catch(Exception e) {
			       		System.err.println("[Mekanism] Error while handling electric chest password packet.");
			    		e.printStackTrace();
			    	}
			    }
			} catch(Exception e) {
				System.err.println("[Mekanism] Error while handling packet.");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Sends a packet from client to server with the TILE_ENTITY ID as well as an undefined amount of objects.
	 * While it won't give you an error, you cannot send anything other than integers or booleans. This can
	 * also be sent with a defined range, so players far away won't receive the packet.
	 * @param sender - sending tile entity
	 * @param distance - distance to send the packet, 0 if infinite range
	 * @param dataValues - data to send
	 */
	public static void sendTileEntityPacketToServer(TileEntity sender, ArrayList dataValues)
	{
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream output = new DataOutputStream(bytes);
        
        try {
        	output.writeInt(EnumPacketType.TILE_ENTITY.id);
        	output.writeInt(sender.xCoord);
        	output.writeInt(sender.yCoord);
        	output.writeInt(sender.zCoord);
        	
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
        			output.writeUTF((String)data);
        		}
        		else if(data instanceof Byte)
        		{
        			output.writeByte((Byte)data);
        		}
        		else if(data instanceof int[])
        		{
        			for(int i : (int[])data)
        			{
        				output.writeInt(i);
        			}
        		}
        	}
        	
            Packet250CustomPayload packet = new Packet250CustomPayload();
            packet.channel = "Mekanism";
            packet.data = bytes.toByteArray();
            packet.length = packet.data.length;
            
            PacketDispatcher.sendPacketToServer(packet);
        } catch (IOException e) {
        	System.err.println("[Mekanism] Error while writing tile entity packet.");
        	e.printStackTrace();
        }
	}
	
	/**
	 * Sends a packet from server to client with the TILE_ENTITY ID as well as an undefined amount of objects.
	 * While it won't give you an error, you cannot send anything other than integers or booleans. This can
	 * also be sent with a defined range, so players far away won't receive the packet.
	 * @param sender - sending tile entity
	 * @param distance - distance to send the packet, 0 if infinite range
	 * @param dataValues - data to send
	 */
	public static void sendTileEntityPacketToClients(TileEntity sender, double distance, ArrayList dataValues)
	{
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream output = new DataOutputStream(bytes);
        
        try {
        	output.writeInt(EnumPacketType.TILE_ENTITY.id);
        	output.writeInt(sender.xCoord);
        	output.writeInt(sender.yCoord);
        	output.writeInt(sender.zCoord);
        	
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
        			output.writeUTF((String)data);
        		}
        		else if(data instanceof Byte)
        		{
        			output.writeByte((Byte)data);
        		}
        		else if(data instanceof int[])
        		{
        			for(int i : (int[])data)
        			{
        				output.writeInt(i);
        			}
        		}
        	}
        	
            Packet250CustomPayload packet = new Packet250CustomPayload();
            packet.channel = "Mekanism";
            packet.data = bytes.toByteArray();
            packet.length = packet.data.length;
            
            if(distance == 0) PacketDispatcher.sendPacketToAllPlayers(packet);
            else PacketDispatcher.sendPacketToAllAround(sender.xCoord, sender.yCoord, sender.zCoord, distance, sender.worldObj.provider.dimensionId, packet);
        } catch (IOException e) {
        	System.err.println("[Mekanism] Error while writing tile entity packet.");
        	e.printStackTrace();
        }
	}
	
	/**
	 * When the 'Access' button is clicked on the Control Panel's GUI, it both opens the client-side GUI, as well as
	 * send a packet to the server with enough data to open up the server-side object, or container. This packet does
	 * that function -- it sends over the data from IAccessibleGui (modClass, modInstance, guiId), and uses reflection
	 * to attempt and access the declared instance object from the mod's main class, which is also accessed using
	 * reflection. Upon being handled server-side, the data is put together, checked for NPEs, and then used inside
	 * EntityPlayer.openGui() along with the sent-over coords.
	 * @param modClass
	 * @param modInstance
	 * @param x
	 * @param y
	 * @param z
	 * @param guiId
	 */
	public static void sendGuiRequest(String modClass, String modInstance, int x, int y, int z, int guiId)
	{
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(bytes);
        
        try {
        	data.writeInt(EnumPacketType.CONTROL_PANEL.id);
        	data.writeUTF(modClass);
        	data.writeUTF(modInstance);
        	data.writeInt(x);
        	data.writeInt(y);
        	data.writeInt(z);
        	data.writeInt(guiId);
        } catch (IOException e) {
        	System.out.println("[Mekanism] An error occured while writing packet data.");
        	e.printStackTrace();
        }
        
        Packet250CustomPayload packet = new Packet250CustomPayload();
        packet.channel = "Mekanism";
        packet.data = bytes.toByteArray();
        packet.length = packet.data.length;
        PacketDispatcher.sendPacketToServer(packet);
        
        if(Mekanism.logPackets)
        {
        	System.out.println("[Mekanism] Sent control panel packet to server.");
        }
	}
	
	/**
	 * Sends a portal effect packet to all clients in a radius around a teleporter.
	 * @param x - x coordinate of teleporter
	 * @param y - y coordinate of teleporter
	 * @param z - z coordinate of teleporter
	 * @param id - dimension ID of teleporter
	 */
	public static void sendPortalFX(int x, int y, int z, int id)
	{
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(bytes);
        
        try {
        	data.writeInt(EnumPacketType.PORTAL_FX.id);
        	data.writeInt(x);
        	data.writeInt(y);
        	data.writeInt(z);
        } catch (IOException e) {
        	System.out.println("[Mekanism] An error occured while writing packet data.");
        	e.printStackTrace();
        }
        
        Packet250CustomPayload packet = new Packet250CustomPayload();
        packet.channel = "Mekanism";
        packet.data = bytes.toByteArray();
        packet.length = packet.data.length;
        PacketDispatcher.sendPacketToAllAround(x, y, z, 40, id, packet);
        
        if(Mekanism.logPackets)
        {
        	System.out.println("[Mekanism] Sent portal FX packet to clients.");
        }
	}
	
	/**
	 * Sends a digit update for a portable teleporter to the server as an integer.
	 * @param index - digit index
	 * @param digit - digit to send
	 */
	public static void sendDigitUpdate(int index, int digit)
	{
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(bytes);
        
        try {
        	data.writeInt(EnumPacketType.DIGIT_UPDATE.id);
			data.writeInt(index);
			data.writeInt(digit);
		} catch (IOException e) {
			System.out.println("[Mekanism] An error occured while writing packet data.");
			e.printStackTrace();
		}
        
        Packet250CustomPayload packet = new Packet250CustomPayload();
        packet.channel = "Mekanism";
        packet.data = bytes.toByteArray();
        packet.length = packet.data.length;
        PacketDispatcher.sendPacketToServer(packet);
        
        if(Mekanism.logPackets)
        {
        	System.out.println("[Mekanism] Sent digit update packet to server.");
        }
	}
	
	/**
	 * Sends a status update for a portable teleporter to the client as a string.
	 * @param entityplayer - player who is using the teleporter
	 * @param status - status to send
	 */
	public static void sendStatusUpdate(EntityPlayer entityplayer, int status)
	{
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(bytes);
        
        try {
        	data.writeInt(EnumPacketType.STATUS_UPDATE.id);
			data.writeInt(status);
		} catch (IOException e) {
			System.out.println("[Mekanism] An error occured while writing packet data.");
			e.printStackTrace();
		}
        
        Packet250CustomPayload packet = new Packet250CustomPayload();
        packet.channel = "Mekanism";
        packet.data = bytes.toByteArray();
        packet.length = packet.data.length;
        PacketDispatcher.sendPacketToPlayer(packet, (Player)entityplayer);
        
        if(Mekanism.logPackets)
        {
        	System.out.println("[Mekanism] Sent status update packet to " + entityplayer.username);
        }
	}
	
	/**
	 * Sends a packet to a specified client requesting a certain electric chest GUI to be opened.
	 * @param player - the player to send the chest-opening packet to
	 * @param tileEntity - TileEntity of the chest, if it's not an item
	 * @param i - GUI type: 0 if regular electric chest, 1 if password prompt, 2 if password modify
	 * @param windowId - Minecraft-based container window identifier
	 * @param isBlock - whether or not this electric chest is in it's block form
	 */
	public static void sendChestOpenToPlayer(EntityPlayer player, TileEntity tileEntity, int i, int windowId, boolean isBlock)
	{
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(bytes);
        
        try {
        	data.writeInt(EnumPacketType.ELECTRIC_CHEST_CLIENT_OPEN.id);
        	data.writeInt(i);
        	data.writeInt(windowId);
        	data.writeBoolean(isBlock);
        	
        	if(isBlock)
        	{
	        	data.writeInt(tileEntity.xCoord);
	        	data.writeInt(tileEntity.yCoord);
	        	data.writeInt(tileEntity.zCoord);
        	}
        } catch (IOException e) {
        	System.out.println("[Mekanism] An error occured while writing packet data.");
        	e.printStackTrace();
        }
        
        Packet250CustomPayload packet = new Packet250CustomPayload();
        packet.channel = "Mekanism";
        packet.data = bytes.toByteArray();
        packet.length = packet.data.length;
        ((EntityPlayerMP)player).playerNetServerHandler.sendPacketToPlayer(packet);
        
        if(Mekanism.logPackets)
        {
        	System.out.println("[Mekanism] Sent electric chest open packet to " + player.username);
        }
	}
	
	/**
	 * Sends a packet to the server requesting an electric chest to be opened.
	 * @param tileEntity - TileEntity of the chest, if it's not an item
	 * @param isBlock - whether or not this electric chest is in it's block form
	 * @param useEnergy - whether or not to use chest-opening energy (100 J)
	 */
	public static void sendChestOpen(TileEntity tileEntity, boolean isBlock, boolean useEnergy)
	{
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(bytes);
        
        try {
        	data.writeInt(EnumPacketType.ELECTRIC_CHEST_SERVER_OPEN.id);
        	data.writeBoolean(isBlock);
        	data.writeBoolean(useEnergy);
        	
        	if(isBlock)
        	{
	        	data.writeInt(tileEntity.xCoord);
	        	data.writeInt(tileEntity.yCoord);
	        	data.writeInt(tileEntity.zCoord);
        	}
        } catch (IOException e) {
        	System.out.println("[Mekanism] An error occured while writing packet data.");
        	e.printStackTrace();
        }
        
        Packet250CustomPayload packet = new Packet250CustomPayload();
        packet.channel = "Mekanism";
        packet.data = bytes.toByteArray();
        packet.length = packet.data.length;
        PacketDispatcher.sendPacketToServer(packet);
        
        if(Mekanism.logPackets)
        {
        	System.out.println("[Mekanism] Sent electric chest open packet to server.");
        }
	}
	
	/**
	 * Sends a packet to the server with a new 'password' value for an electric chest.
	 * @param tileEntity - TileEntity of the chest, if it's not an item
	 * @param password - new value
	 * @param isBlock - whether or not this electric chest is in it's block form
	 */
	public static void sendPasswordChange(TileEntity tileEntity, String password, boolean isBlock)
	{
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(bytes);
        
        try {
        	data.writeInt(EnumPacketType.ELECTRIC_CHEST_PASSWORD.id);
        	data.writeBoolean(isBlock);
        	data.writeUTF(password);
        	
        	if(isBlock)
        	{
               	data.writeInt(tileEntity.xCoord);
            	data.writeInt(tileEntity.yCoord);
            	data.writeInt(tileEntity.zCoord);
        	}
        } catch (IOException e) {
        	System.out.println("[Mekanism] An error occured while writing packet data.");
        	e.printStackTrace();
        }
        
        Packet250CustomPayload packet = new Packet250CustomPayload();
        packet.channel = "Mekanism";
        packet.data = bytes.toByteArray();
        packet.length = packet.data.length;
        PacketDispatcher.sendPacketToServer(packet);
        
        if(Mekanism.logPackets)
        {
        	System.out.println("[Mekanism] Sent electric chest password packet to server.");
        }
	}
	
	/**
	 * Sends a packet to the server with a new 'locked' value for an electric chest.
	 * @param tileEntity - TileEntity of the chest, if it's not an item
	 * @param locked - new value
	 * @param isBlock - whether or not this electric chest is in it's block form
	 */
	public static void sendLockChange(TileEntity tileEntity, boolean locked, boolean isBlock)
	{
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(bytes);
        
        try {
        	data.writeInt(EnumPacketType.ELECTRIC_CHEST_LOCK.id);
        	data.writeBoolean(isBlock);
        	data.writeBoolean(locked);
        	
        	if(isBlock)
        	{
               	data.writeInt(tileEntity.xCoord);
            	data.writeInt(tileEntity.yCoord);
            	data.writeInt(tileEntity.zCoord);
        	}
        } catch (IOException e) {
        	System.out.println("[Mekanism] An error occured while writing packet data.");
        	e.printStackTrace();
        }
        
        Packet250CustomPayload packet = new Packet250CustomPayload();
        packet.channel = "Mekanism";
        packet.data = bytes.toByteArray();
        packet.length = packet.data.length;
        PacketDispatcher.sendPacketToServer(packet);
        
        if(Mekanism.logPackets)
        {
        	System.out.println("[Mekanism] Sent electric chest lock packet to server.");
        }
	}
	
	/**
	 * Sends the server the defined packet data int.
	 * @param type - packet type
	 * @param i - int to send
	 */
	public static void sendPacketDataInt(EnumPacketType type, int i)
	{
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(bytes);
        
        try {
        	data.writeInt(type.id);
			data.writeInt(i);
		} catch (IOException e) {
			System.out.println("[Mekanism] An error occured while writing packet data.");
			e.printStackTrace();
		}
        
        Packet250CustomPayload packet = new Packet250CustomPayload();
        packet.channel = "Mekanism";
        packet.data = bytes.toByteArray();
        packet.length = packet.data.length;
        PacketDispatcher.sendPacketToServer(packet);
        
        if(Mekanism.logPackets)
        {
        	System.out.println("[Mekanism] Sent data int packet '" + type.id + ":" + i + "' to server");
        }
	}
	
	/**
	 * This method is used to request a data update from the server in order to update a client-sided tile entity.
	 * The server will receive this request, and then send a packet to the client with all the needed information.
	 * @param tileEntity
	 */
	public static void sendDataRequest(TileEntity tileEntity)
	{
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(bytes);
        
        try {
        	data.writeInt(EnumPacketType.DATA_REQUEST.id);
        	data.writeInt(tileEntity.xCoord);
        	data.writeInt(tileEntity.yCoord);
        	data.writeInt(tileEntity.zCoord);
        	data.writeInt(tileEntity.worldObj.provider.dimensionId);
        } catch (IOException e) {
        	System.out.println("[Mekanism] An error occured while writing packet data.");
        	e.printStackTrace();
        }
        
        Packet250CustomPayload packet = new Packet250CustomPayload();
        packet.channel = "Mekanism";
        packet.data = bytes.toByteArray();
        packet.length = packet.data.length;
        PacketDispatcher.sendPacketToServer(packet);
        
        if(Mekanism.logPackets)
        {
        	System.out.println("[Mekanism] Sent data request packet to server.");
        }
	}
}
