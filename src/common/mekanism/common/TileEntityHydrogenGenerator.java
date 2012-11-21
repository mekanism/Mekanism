package mekanism.common;

import ic2.api.ElectricItem;
import ic2.api.IElectricItem;
import universalelectricity.core.UniversalElectricity;
import universalelectricity.core.electricity.ElectricInfo;
import universalelectricity.core.implement.IItemElectric;

import com.google.common.io.ByteArrayDataInput;

import dan200.computer.api.IComputerAccess;
import mekanism.api.IEnergizedItem;
import mekanism.api.IGasAcceptor;
import mekanism.api.IGasStorage;
import mekanism.api.IStorageTank;
import mekanism.api.IStorageTank.EnumGas;
import net.minecraft.src.*;
import net.minecraftforge.common.ForgeDirection;

public class TileEntityHydrogenGenerator extends TileEntityGenerator implements IGasAcceptor, IGasStorage
{
	/** The maximum amount of hydrogen this block can store. */
	public int MAX_HYDROGEN = 18000;
	
	/** The amount of hydrogen this block is storing. */
	public int hydrogenStored;
	
	public TileEntityHydrogenGenerator()
	{
		super("Hydrogen Generator", 10000000, 512);
		inventory = new ItemStack[2];
	}
	
	public void onUpdate()
	{
		super.onUpdate();
		
		if(inventory[1] != null && energyStored > 0)
		{
			if(inventory[1].getItem() instanceof IEnergizedItem)
			{
				IEnergizedItem item = (IEnergizedItem)inventory[1].getItem();
				int sendingEnergy = 0;
				
				if(item.getRate() <= energyStored)
				{
					sendingEnergy = item.getRate();
				}
				else if(item.getRate() > energyStored)
				{
					sendingEnergy = energyStored;
				}
				
				int rejects = item.charge(inventory[1], sendingEnergy);
				setEnergy(energyStored - (sendingEnergy - rejects));
			}
			else if(inventory[1].getItem() instanceof IItemElectric)
			{
				IItemElectric electricItem = (IItemElectric) inventory[1].getItem();
				double ampsToGive = Math.min(ElectricInfo.getAmps(electricItem.getMaxJoules() * 0.005, getVoltage()), (energyStored*UniversalElectricity.IC2_RATIO));
				double joules = electricItem.onReceive(ampsToGive, getVoltage(), inventory[1]);
				setJoules((energyStored*UniversalElectricity.IC2_RATIO) - (ElectricInfo.getJoules(ampsToGive, getVoltage(), 1) - joules));
			}
			else if(inventory[1].getItem() instanceof IElectricItem)
			{
				int sent = ElectricItem.charge(inventory[1], energyStored, 3, false, false);
				setEnergy(energyStored - sent);
			}
		}
		
		if(inventory[0] != null && hydrogenStored < MAX_HYDROGEN)
		{
			if(inventory[0].getItem() instanceof IStorageTank)
			{
				IStorageTank item = (IStorageTank)inventory[0].getItem();
				
				if(item.canProvideGas() && item.gasType() == EnumGas.HYDROGEN)
				{
					int received = 0;
					int hydrogenNeeded = MAX_HYDROGEN - hydrogenStored;
					if(item.getRate() <= hydrogenNeeded)
					{
						received = item.removeGas(inventory[0], item.getRate());
					}
					else if(item.getRate() > hydrogenNeeded)
					{
						received = item.removeGas(inventory[0], hydrogenNeeded);
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
			
			hydrogenStored--;
			setEnergy(energyStored + 128*getEnvironmentBoost());
		}
		else {
			if(!worldObj.isRemote)
			{
				setActive(false);
			}
		}
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
		return energyStored < MAX_ENERGY && hydrogenStored > 0;
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
				return new Object[] {energyStored};
			case 1:
				return new Object[] {output};
			case 2:
				return new Object[] {MAX_ENERGY};
			case 3:
				return new Object[] {(MAX_ENERGY-energyStored)};
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
			energyStored = dataStream.readInt();
			hydrogenStored = dataStream.readInt();
			isActive = dataStream.readBoolean();
			worldObj.markBlockForRenderUpdate(xCoord, yCoord, zCoord);
		} catch (Exception e)
		{
			System.out.println("[Mekanism] Error while handling tile entity packet.");
			e.printStackTrace();
		}
	}

	@Override
	public void sendPacket() 
	{
		PacketHandler.sendTileEntityPacketToClients(this, 0, facing, energyStored, hydrogenStored, isActive);
	}

	@Override
	public void sendPacketWithRange() 
	{
		PacketHandler.sendTileEntityPacketToClients(this, 50, facing, energyStored, hydrogenStored, isActive);
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
