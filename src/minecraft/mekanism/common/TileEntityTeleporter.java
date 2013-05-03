package mekanism.common;

import ic2.api.Direction;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.item.IElectricItem;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.EnumColor;
import mekanism.api.IStrictEnergyAcceptor;
import mekanism.api.Object3D;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.item.IItemElectric;

import com.google.common.io.ByteArrayDataInput;

import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IPeripheral;

public class TileEntityTeleporter extends TileEntityElectricBlock implements IEnergySink, IPeripheral, IStrictEnergyAcceptor
{
	/** This teleporter's frequency. */
	public Teleporter.Code code;
	
	/** This teleporter's current status. */
	public String status = (EnumColor.DARK_RED + "Not ready.");
	
	public TileEntityTeleporter()
	{
		super("Teleporter", 1000000);
		inventory = new ItemStack[1];
		code = new Teleporter.Code(0, 0, 0, 0);
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if(!worldObj.isRemote)
		{
			if(Mekanism.teleporters.containsKey(code))
			{
				if(!Mekanism.teleporters.get(code).contains(Object3D.get(this)) && hasFrame())
				{
					Mekanism.teleporters.get(code).add(Object3D.get(this));
				}
				else if(Mekanism.teleporters.get(code).contains(Object3D.get(this)) && !hasFrame())
				{
					Mekanism.teleporters.get(code).remove(Object3D.get(this));
				}
			}
			else if(hasFrame())
			{
				ArrayList<Object3D> newCoords = new ArrayList<Object3D>();
				newCoords.add(Object3D.get(this));
				Mekanism.teleporters.put(code, newCoords);
			}
			
			switch(canTeleport())
			{
				case 1:
					status = EnumColor.DARK_GREEN + "Ready.";
					break;
				case 2:
					status = EnumColor.DARK_RED + "No frame.";
					break;
				case 3:
					status = EnumColor.DARK_RED + "No link found.";
					break;
				case 4:
					status = EnumColor.DARK_RED + "Links > 2.";
					break;
				case 5:
					status = EnumColor.DARK_RED + "Needs energy.";
					break;
				case 6:
					status = EnumColor.DARK_GREEN + "Idle.";
					break;
			}
		}
		
		ChargeUtils.discharge(0, this);
	}
	
	@Override
	public int[] getSizeInventorySide(int side)
	{
		return new int[] {0};
	}
	
	@Override
	public boolean isStackValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 0)
		{
			return (itemstack.getItem() instanceof IElectricItem && ((IElectricItem)itemstack.getItem()).canProvideEnergy(itemstack)) || 
					(itemstack.getItem() instanceof IItemElectric && ((IItemElectric)itemstack.getItem()).getProvideRequest(itemstack).amperes != 0) || 
					itemstack.itemID == Item.redstone.itemID;
		}
		
		return true;
	}
	
	/**
	 * 1: yes
	 * 2: no frame
	 * 3: no link found
	 * 4: too many links
	 * 5: not enough electricity
	 * 6: nothing to teleport
	 * @return
	 */
	public byte canTeleport()
	{
		if(!hasFrame())
		{
			return 2;
		}
		
		if(!Mekanism.teleporters.containsKey(code) || Mekanism.teleporters.get(code).isEmpty())
		{
			return 3;
		}
		
		if(Mekanism.teleporters.get(code).size() > 2) 
		{
			return 4;
		}
		
		if(Mekanism.teleporters.get(code).size() == 2)
		{
			List<EntityPlayer> entitiesInPortal = worldObj.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox(xCoord-1, yCoord, zCoord-1, xCoord+1, yCoord+3, zCoord+1));

			Object3D closestCoords = null;
			
			for(Object3D coords : Mekanism.teleporters.get(code))
			{
				if(!coords.equals(Object3D.get(this)))
				{
					closestCoords = coords;
					break;
				}
			}
			
			int electricityNeeded = 0;
			
			for(EntityPlayer entity : entitiesInPortal)
			{
				electricityNeeded += calculateEnergyCost(entity, closestCoords);
			}
			
			if(entitiesInPortal.size() == 0)
			{
				return 6;
			}
			
			if(electricityStored < electricityNeeded)
			{
				return 5;
			}
			
			return 1;
		}
		
		return 3;
	}
	
	public void teleport()
	{
		if(worldObj.isRemote) return;
		
		List<EntityPlayer> entitiesInPortal = worldObj.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox(xCoord-1, yCoord, zCoord-1, xCoord+1, yCoord+3, zCoord+1));

		Object3D closestCoords = null;
		
		for(Object3D coords : Mekanism.teleporters.get(code))
		{
			if(!coords.equals(Object3D.get(this)))
			{
				closestCoords = coords;
				break;
			}
		}
		
		for(EntityPlayer entity : entitiesInPortal)
		{
			setEnergy(electricityStored - calculateEnergyCost(entity, closestCoords));
			
			worldObj.playSoundAtEntity((EntityPlayerMP)entity, "mob.endermen.portal", 1.0F, 1.0F);
			
			if(entity.worldObj.provider.dimensionId != closestCoords.dimensionId)
			{
				entity.travelToDimension(closestCoords.dimensionId);
			}
			
			((EntityPlayerMP)entity).playerNetServerHandler.setPlayerLocation(closestCoords.xCoord+0.5, closestCoords.yCoord, closestCoords.zCoord+0.5, entity.rotationYaw, entity.rotationPitch);
			
			for(Object3D coords : Mekanism.teleporters.get(code))
			{
				PacketHandler.sendPortalFX(coords.xCoord, coords.yCoord, coords.zCoord, coords.dimensionId);
			}
		}
	}

	@Override
	public int getSizeInventorySide(ForgeDirection side)
	{
		return 1;
	}
	
	@Override
	public void invalidate()
	{
		super.invalidate();
		
		if(!worldObj.isRemote)
		{
			if(Mekanism.teleporters.containsKey(code))
			{
				if(Mekanism.teleporters.get(code).contains(Object3D.get(this)))
				{
					Mekanism.teleporters.get(code).remove(Object3D.get(this));
				}
				
				if(Mekanism.teleporters.get(code).isEmpty()) 
				{
					Mekanism.teleporters.remove(code);
				}
			}
		}
	}
	
	public int calculateEnergyCost(Entity entity, Object3D coords)
	{
		int energyCost = 1000;
		
		if(entity.worldObj.provider.dimensionId != coords.dimensionId)
		{
			energyCost+=10000;
		}
		
		int distance = (int)entity.getDistance(coords.xCoord, coords.yCoord, coords.zCoord);
		energyCost+=(distance*10);
		
		return energyCost;
	}
	
	public boolean hasFrame()
	{
		if(isFrame(xCoord-1, yCoord, zCoord) && isFrame(xCoord+1, yCoord, zCoord)
				&& isFrame(xCoord-1, yCoord+1, zCoord) && isFrame(xCoord+1, yCoord+1, zCoord)
				&& isFrame(xCoord-1, yCoord+2, zCoord) && isFrame(xCoord+1, yCoord+2, zCoord)
				&& isFrame(xCoord-1, yCoord+3, zCoord) && isFrame(xCoord+1, yCoord+3, zCoord)
				&& isFrame(xCoord, yCoord+3, zCoord)) {return true;}
		if(isFrame(xCoord, yCoord, zCoord-1) && isFrame(xCoord, yCoord, zCoord+1)
				&& isFrame(xCoord, yCoord+1, zCoord-1) && isFrame(xCoord, yCoord+1, zCoord+1)
				&& isFrame(xCoord, yCoord+2, zCoord-1) && isFrame(xCoord, yCoord+2, zCoord+1)
				&& isFrame(xCoord, yCoord+3, zCoord-1) && isFrame(xCoord, yCoord+3, zCoord+1)
				&& isFrame(xCoord, yCoord+3, zCoord)) {return true;}
		return false;
	}
	
	public boolean isFrame(int x, int y, int z)
	{
		return worldObj.getBlockId(x, y, z) == Mekanism.basicBlockID && worldObj.getBlockMetadata(x, y, z) == 7;
	}
	
	public int getScaledEnergyLevel(int i)
	{
		return (int)(electricityStored*i / MAX_ELECTRICITY);
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);

        code.digitOne = nbtTags.getInteger("digitOne");
        code.digitTwo = nbtTags.getInteger("digitTwo");
        code.digitThree = nbtTags.getInteger("digitThree");
        code.digitFour = nbtTags.getInteger("digitFour");
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setInteger("digitOne", code.digitOne);
        nbtTags.setInteger("digitTwo", code.digitTwo);
        nbtTags.setInteger("digitThree", code.digitThree);
        nbtTags.setInteger("digitFour", code.digitFour);
    }
	
	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		if(!worldObj.isRemote)
		{
			if(Mekanism.teleporters.containsKey(code))
			{
				if(Mekanism.teleporters.get(code).contains(Object3D.get(this)))
				{
					Mekanism.teleporters.get(code).remove(Object3D.get(this));
				}
				
				if(Mekanism.teleporters.get(code).isEmpty()) Mekanism.teleporters.remove(code);
			}
			
			int type = dataStream.readInt();
			
			if(type == 0)
			{
				code.digitOne = dataStream.readInt();
			}
			else if(type == 1)
			{
				code.digitTwo = dataStream.readInt();
			}
			else if(type == 2)
			{
				code.digitThree = dataStream.readInt();
			}
			else if(type == 3)
			{
				code.digitFour = dataStream.readInt();
			}
			return;
		}
		
		super.handlePacketData(dataStream);
		status = dataStream.readUTF().trim();
		code.digitOne = dataStream.readInt();
		code.digitTwo = dataStream.readInt();
		code.digitThree = dataStream.readInt();
		code.digitFour = dataStream.readInt();
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		data.add(status);
		data.add(code.digitOne);
		data.add(code.digitTwo);
		data.add(code.digitThree);
		data.add(code.digitFour);
		return data;
	}

	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, Direction direction)
	{
		return true;
	}
	
	@Override
	public boolean func_102008_b(int slotID, ItemStack itemstack, int side)
	{
		return (itemstack.getItem() instanceof IItemElectric && ((IItemElectric)itemstack.getItem()).getProvideRequest(itemstack).getWatts() == 0) ||
				(itemstack.getItem() instanceof IElectricItem && (!(itemstack.getItem() instanceof IItemElectric) || 
						((IItemElectric)itemstack.getItem()).getProvideRequest(itemstack).getWatts() == 0));
	}
	
	@Override
	public double transferEnergyToAcceptor(double amount)
	{
    	double rejects = 0;
    	double neededGas = MAX_ELECTRICITY-electricityStored;
    	
    	if(amount <= neededGas)
    	{
    		electricityStored += amount;
    	}
    	else {
    		electricityStored += neededGas;
    		rejects = amount-neededGas;
    	}
    	
    	return rejects;
	}
	
	@Override
	public boolean canReceiveEnergy(ForgeDirection side)
	{
		return true;
	}

	@Override
	public String getType()
	{
		return getInvName();
	}

	@Override
	public String[] getMethodNames()
	{
		return new String[] {"getStored", "canTeleport", "getMaxEnergy", "getEnergyNeeded", "teleport", "set"};
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, int method, Object[] arguments) throws Exception
	{
		switch(method)
		{
			case 0:
				return new Object[] {electricityStored};
			case 1:
				return new Object[] {canTeleport()};
			case 2:
				return new Object[] {MAX_ELECTRICITY};
			case 3:
				return new Object[] {(MAX_ELECTRICITY-electricityStored)};
			case 4:
				teleport();
				return new Object[] {"Attempted to teleport."};
			case 5:
				if(!(arguments[0] instanceof Integer) || !(arguments[1] instanceof Integer))
				{
					return new Object[] {"Invalid parameters."};
				}
				
				int digit = (Integer)arguments[0];
				int newDigit = (Integer)arguments[1];
				
				switch(digit)
				{
					case 0:
						code.digitOne = newDigit;
						break;
					case 1:
						code.digitTwo = newDigit;
						break;
					case 2:
						code.digitThree = newDigit;
						break;
					case 3:
						code.digitFour = newDigit;
						break;
					default:
						return new Object[] {"No digit found."};
				}
			default:
				System.err.println("[Mekanism] Attempted to call unknown method with computer ID " + computer.getID());
				return new Object[] {"Unknown command."};
		}
	}

	@Override
	public boolean canAttachToSide(int side)
	{
		return true;
	}

	@Override
	public void attach(IComputerAccess computer) {}

	@Override
	public void detach(IComputerAccess computer) {}

	@Override
	public int demandsEnergy()
	{
		return (int)((MAX_ELECTRICITY - electricityStored)*Mekanism.TO_IC2);
	}

	@Override
	public int injectEnergy(Direction directionFrom, int amount)
	{
		double givenEnergy = amount*Mekanism.FROM_IC2;
    	double rejects = 0;
    	double neededEnergy = MAX_ELECTRICITY-electricityStored;
    	
    	if(givenEnergy < neededEnergy)
    	{
    		electricityStored += givenEnergy;
    	}
    	else if(givenEnergy > neededEnergy)
    	{
    		electricityStored += neededEnergy;
    		rejects = givenEnergy-neededEnergy;
    	}
    	
    	return (int)(rejects*Mekanism.TO_IC2);
	}

	@Override
	public int getMaxSafeInput()
	{
		return 2048;
	}
}
