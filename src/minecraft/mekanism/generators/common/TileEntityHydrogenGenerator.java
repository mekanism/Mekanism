package mekanism.generators.common;

import ic2.api.ElectricItem;
import ic2.api.IElectricItem;
import mekanism.api.EnumGas;
import mekanism.api.IGasAcceptor;
import mekanism.api.IGasStorage;
import mekanism.api.IStorageTank;
import mekanism.common.MekanismUtils;
import mekanism.common.PacketHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.UniversalElectricity;
import universalelectricity.core.electricity.ElectricInfo;
import universalelectricity.core.implement.IItemElectric;

import com.google.common.io.ByteArrayDataInput;

import dan200.computer.api.IComputerAccess;

public class TileEntityHydrogenGenerator extends TileEntityGenerator implements IGasAcceptor, IGasStorage
{
	/** The maximum amount of hydrogen this block can store. */
	public int MAX_HYDROGEN = 18000;
	
	/** The amount of hydrogen this block is storing. */
	public int hydrogenStored;
	
	public TileEntityHydrogenGenerator()
	{
		super("Hydrogen Generator", 400000, 1024);
		inventory = new ItemStack[2];
	}
	
	public void onUpdate()
	{
		super.onUpdate();
		
		if(inventory[1] != null && electricityStored > 0)
		{
			if(inventory[1].getItem() instanceof IItemElectric)
			{
				IItemElectric electricItem = (IItemElectric)inventory[1].getItem();
				
				if(electricItem.canReceiveElectricity())
				{
					double ampsToGive = Math.min(ElectricInfo.getAmps(Math.min(electricItem.getMaxJoules(inventory[1])*0.005, electricityStored), getVoltage()), electricityStored);
					double rejects = electricItem.onReceive(ampsToGive, getVoltage(), inventory[1]);
					setJoules(electricityStored - (ElectricInfo.getJoules(ampsToGive, getVoltage(), 1) - rejects));
				}
			}
			else if(inventory[1].getItem() instanceof IElectricItem)
			{
				double sent = ElectricItem.charge(inventory[1], (int)(electricityStored*UniversalElectricity.TO_IC2_RATIO), 3, false, false)*UniversalElectricity.IC2_RATIO;
				setJoules(electricityStored - sent);
			}
		}
		
		if(inventory[0] != null && hydrogenStored < MAX_HYDROGEN)
		{
			if(inventory[0].getItem() instanceof IStorageTank)
			{
				IStorageTank item = (IStorageTank)inventory[0].getItem();
				
				if(item.canProvideGas(inventory[0], EnumGas.HYDROGEN) && item.getGasType(inventory[0]) == EnumGas.HYDROGEN)
				{
					int received = 0;
					int hydrogenNeeded = MAX_HYDROGEN - hydrogenStored;
					if(item.getRate() <= hydrogenNeeded)
					{
						received = item.removeGas(inventory[0], EnumGas.HYDROGEN, item.getRate());
					}
					else if(item.getRate() > hydrogenNeeded)
					{
						received = item.removeGas(inventory[0], EnumGas.HYDROGEN, hydrogenNeeded);
					}
					
					setGas(EnumGas.HYDROGEN, hydrogenStored + received);
				}
			}
		}
		
		if(hydrogenStored > MAX_HYDROGEN)
		{
			hydrogenStored = MAX_HYDROGEN;
		}
		
		if(canOperate())
		{
			if(!worldObj.isRemote)
			{
				setActive(true);
			}
			
			hydrogenStored-=2;
			setJoules(electricityStored + 1000);
		}
		else {
			if(!worldObj.isRemote)
			{
				setActive(false);
			}
		}
	}
	
	@Override
	public int getStartInventorySide(ForgeDirection side) 
	{
		if(side == MekanismUtils.getRight(facing))
		{
			return 1;
		}
		
		return 0;
	}

	@Override
	public int getSizeInventorySide(ForgeDirection side)
	{
		return 1;
	}
    
    @Override
	public void setGas(EnumGas type, int amount)
	{
		if(type == EnumGas.HYDROGEN)
		{
			hydrogenStored = Math.max(Math.min(amount, MAX_HYDROGEN), 0);
		}
	}
    
	@Override
	public int getGas(EnumGas type)
	{
		if(type == EnumGas.HYDROGEN)
		{
			return hydrogenStored;
		}
		
		return 0;
	}
	
	@Override
	public boolean canOperate()
	{
		return electricityStored < MAX_ELECTRICITY && hydrogenStored-10 > -1;
	}
	
	/**
	 * Gets the scaled hydrogen level for the GUI.
	 * @param i - multiplier
	 * @return
	 */
	public int getScaledHydrogenLevel(int i)
	{
		return hydrogenStored*i / MAX_HYDROGEN;
	}

	@Override
	public String[] getMethodNames() 
	{
		return new String[] {"getStored", "getOutput", "getMaxEnergy", "getEnergyNeeded", "getHydrogen", "getHydrogenNeeded"};
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, int method, Object[] arguments) throws Exception 
	{
		switch(method)
		{
			case 0:
				return new Object[] {electricityStored};
			case 1:
				return new Object[] {output};
			case 2:
				return new Object[] {MAX_ELECTRICITY};
			case 3:
				return new Object[] {(MAX_ELECTRICITY-electricityStored)};
			case 4:
				return new Object[] {hydrogenStored};
			case 5:
				return new Object[] {MAX_HYDROGEN-hydrogenStored};
			default:
				System.err.println("[Mekanism] Attempted to call unknown method with computer ID " + computer.getID());
				return null;
		}
	}

	@Override
	public void handlePacketData(INetworkManager network, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream)
	{
		try {
			facing = dataStream.readInt();
			electricityStored = dataStream.readDouble();
			hydrogenStored = dataStream.readInt();
			isActive = dataStream.readBoolean();
			worldObj.markBlockForRenderUpdate(xCoord, yCoord, zCoord);
			worldObj.updateAllLightTypes(xCoord, yCoord, zCoord);
		} catch (Exception e)
		{
			System.out.println("[Mekanism] Error while handling tile entity packet.");
			e.printStackTrace();
		}
	}

	@Override
	public void sendPacket() 
	{
		PacketHandler.sendTileEntityPacketToClients(this, 0, facing, electricityStored, hydrogenStored, isActive);
	}

	@Override
	public void sendPacketWithRange() 
	{
		PacketHandler.sendTileEntityPacketToClients(this, 50, facing, electricityStored, hydrogenStored, isActive);
	}

	@Override
	public int getEnvironmentBoost() 
	{
		int boost = 1;
		
		if(yCoord > 64 && yCoord < 80)
		{
			boost = 2;
		}
		else if(yCoord > 80 && yCoord < 96)
		{
			boost = 3;
		}
		else if(yCoord > 96)
		{
			boost = 4;
		}
		return boost;
	}
	
	@Override
	public double getVoltage(Object... data)
	{
		return 240;
	}

	@Override
	public int transferGasToAcceptor(int amount, EnumGas type)
	{
		if(type == EnumGas.HYDROGEN)
		{
	    	int rejects = 0;
	    	int neededHydrogen = MAX_HYDROGEN-hydrogenStored;
	    	if(amount <= neededHydrogen)
	    	{
	    		hydrogenStored += amount;
	    	}
	    	else if(amount > neededHydrogen)
	    	{
	    		hydrogenStored += neededHydrogen;
	    		rejects = amount-neededHydrogen;
	    	}
	    	
	    	return rejects;
		}
		return 0;
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);

        hydrogenStored = nbtTags.getInteger("hydrogenStored");
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setInteger("hydrogenStored", hydrogenStored);
    }

	@Override
	public boolean canReceiveGas(ForgeDirection side, EnumGas type) 
	{
		return type == EnumGas.HYDROGEN && side != ForgeDirection.getOrientation(facing);
	}
}
