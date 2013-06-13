package mekanism.common.network;

import java.io.DataOutputStream;

import mekanism.api.IEnergizedItem;
import mekanism.api.Object3D;
import mekanism.common.IElectricChest;
import mekanism.common.InventoryElectricChest;
import mekanism.common.Mekanism;
import mekanism.common.MekanismUtils;
import mekanism.common.TileEntityElectricChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;

public class PacketElectricChest implements IMekanismPacket
{
	public ElectricChestPacketType activeType;
	
	public boolean isBlock;
	
	public boolean locked;
	
	public String password;
	
	public int guiType;
	public int windowId;
	
	public boolean useEnergy;
	
	public Object3D obj;
	
	public PacketElectricChest(ElectricChestPacketType type, Object... params)
	{
		activeType = type;
		
		switch(activeType)
		{
			case LOCK:
				locked = (Boolean)params[0];
				isBlock = (Boolean)params[1];
				
				if(isBlock)
				{
					obj = (Object3D)params[2];
				}
				
				break;
			case PASSWORD:
				password = (String)params[0];
				isBlock = (Boolean)params[1];
				
				if(isBlock)
				{
					obj = (Object3D)params[2];
				}
				
				break;
			case CLIENT_OPEN:
				guiType = (Integer)params[0];
				windowId = (Integer)params[1];
				isBlock = (Boolean)params[2];
				
				if(isBlock)
				{
					obj = (Object3D)params[3];
				}
				
				break;
			case SERVER_OPEN:
				useEnergy = (Boolean)params[0];
				isBlock = (Boolean)params[1];
				
				if(isBlock)
				{
					obj = (Object3D)params[2];
				}
				
				break;
		}
	}
	
	public PacketElectricChest() {}
	
	@Override
	public String getName() 
	{
		return "ElectricChest";
	}

	@Override
	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception 
	{
		ElectricChestPacketType packetType = ElectricChestPacketType.values()[dataStream.readInt()];
		
	    if(packetType == ElectricChestPacketType.SERVER_OPEN)
	    {
	    	try {
	    		boolean energy = dataStream.readBoolean();
	    		boolean block = dataStream.readBoolean();
	    		
	    		if(block)
	    		{
		    		int x = dataStream.readInt();
		    		int y = dataStream.readInt();
		    		int z = dataStream.readInt();
		    		
		    		TileEntityElectricChest tileEntity = (TileEntityElectricChest)world.getBlockTileEntity(x, y, z);
		    		
		    		if(energy)
		    		{
		    			tileEntity.setEnergy(tileEntity.getEnergy() - 100);
		    		}
		    		
		    		MekanismUtils.openElectricChestGui((EntityPlayerMP)player, tileEntity, null, true);
	    		}
	    		else {
	    			ItemStack stack = player.getCurrentEquippedItem();
	    			
	    			if(stack != null && stack.getItem() instanceof IElectricChest && ((IElectricChest)stack.getItem()).isElectricChest(stack))
	    			{
	    				if(energy)
	    				{
	    					((IEnergizedItem)stack.getItem()).setEnergy(stack, ((IEnergizedItem)stack.getItem()).getEnergy(stack) - 100);
	    				}
	    				
	    				InventoryElectricChest inventory = new InventoryElectricChest(player);
	    				MekanismUtils.openElectricChestGui((EntityPlayerMP)player, null, inventory, false);
	    			}
	    		}
	    	} catch(Exception e) {
	       		System.err.println("[Mekanism] Error while handling electric chest open packet.");
	    		e.printStackTrace();
	    	}
	    }
	    else if(packetType == ElectricChestPacketType.CLIENT_OPEN)
	    {
	    	try {
	    		int type = dataStream.readInt();
	    		int id = dataStream.readInt();
	    		boolean block = dataStream.readBoolean();
	    		
	    		int x = 0;
	    		int y = 0;
	    		int z = 0;
	    		
	    		if(block)
	    		{
	        		x = dataStream.readInt();
		    		y = dataStream.readInt();
		    		z = dataStream.readInt();
	    		}
	    		
	    		Mekanism.proxy.openElectricChest(player, type, id, block, x, y, z);
	    	} catch(Exception e) {
	       		System.err.println("[Mekanism] Error while handling electric chest open packet.");
	    		e.printStackTrace();
	    	}
	    }
	    else if(packetType == ElectricChestPacketType.PASSWORD)
	    {
	    	try {
	    		String pass = dataStream.readUTF();
	    		boolean block = dataStream.readBoolean();
	    		
	    		if(block)
	    		{
		    		int x = dataStream.readInt();
		    		int y = dataStream.readInt();
		    		int z = dataStream.readInt();
		    		
		    		TileEntityElectricChest tileEntity = (TileEntityElectricChest)world.getBlockTileEntity(x, y, z);
		    		tileEntity.password = pass;
		    		tileEntity.authenticated = true;
	    		}
	    		else {
	    			ItemStack stack = player.getCurrentEquippedItem();
	    			
	    			if(stack != null && stack.getItem() instanceof IElectricChest && ((IElectricChest)stack.getItem()).isElectricChest(stack))
	    			{
	    				((IElectricChest)stack.getItem()).setPassword(stack, pass);
	    				((IElectricChest)stack.getItem()).setAuthenticated(stack, true);
	    			}
	    		}
	    	} catch(Exception e) {
	       		System.err.println("[Mekanism] Error while handling electric chest password packet.");
	    		e.printStackTrace();
	    	}
	    }
	    else if(packetType == ElectricChestPacketType.LOCK)
	    {
	    	try {
	    		boolean lock = dataStream.readBoolean();
	    		boolean block = dataStream.readBoolean();
	    		
	    		if(block)
	    		{
		    		int x = dataStream.readInt();
		    		int y = dataStream.readInt();
		    		int z = dataStream.readInt();
		    		
		    		TileEntityElectricChest tileEntity = (TileEntityElectricChest)world.getBlockTileEntity(x, y, z);
		    		tileEntity.locked = lock;
	    		}
	    		else {
	    			ItemStack stack = player.getCurrentEquippedItem();
	    			
	    			if(stack != null && stack.getItem() instanceof IElectricChest && ((IElectricChest)stack.getItem()).isElectricChest(stack))
	    			{
	    				((IElectricChest)stack.getItem()).setLocked(stack, lock);
	    			}
	    		}
	    	} catch(Exception e) {
	       		System.err.println("[Mekanism] Error while handling electric chest password packet.");
	    		e.printStackTrace();
	    	}
	    }
	}

	@Override
	public void write(DataOutputStream dataStream) throws Exception
	{
		dataStream.writeInt(activeType.ordinal());
		
		switch(activeType)
		{
			case LOCK:
				dataStream.writeBoolean(locked);
				dataStream.writeBoolean(isBlock);
				
				if(isBlock)
				{
					dataStream.writeInt(obj.xCoord);
					dataStream.writeInt(obj.yCoord);
					dataStream.writeInt(obj.zCoord);
				}
				
				break;
			case PASSWORD:
				dataStream.writeUTF(password);
				dataStream.writeBoolean(isBlock);
				
				if(isBlock)
				{
					dataStream.writeInt(obj.xCoord);
					dataStream.writeInt(obj.yCoord);
					dataStream.writeInt(obj.zCoord);
				}
				
				break;
			case CLIENT_OPEN:
				dataStream.writeInt(guiType);
				dataStream.writeInt(windowId);
				dataStream.writeBoolean(isBlock);
				
				if(isBlock)
				{
					dataStream.writeInt(obj.xCoord);
					dataStream.writeInt(obj.yCoord);
					dataStream.writeInt(obj.zCoord);
				}
				
				break;
			case SERVER_OPEN:
				dataStream.writeBoolean(useEnergy);
				dataStream.writeBoolean(isBlock);
				
				if(isBlock)
				{
					dataStream.writeInt(obj.xCoord);
					dataStream.writeInt(obj.yCoord);
					dataStream.writeInt(obj.zCoord);
				}
				
				break;
		}
	}
	
	public static enum ElectricChestPacketType
	{
		LOCK,
		PASSWORD,
		CLIENT_OPEN,
		SERVER_OPEN
	}
}
