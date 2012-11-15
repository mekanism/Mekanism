package mekanism.common;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;


import universalelectricity.core.UniversalElectricity;
import universalelectricity.electricity.ElectricInfo;
import universalelectricity.implement.IItemElectric;

import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.PowerFramework;

import com.google.common.io.ByteArrayDataInput;

import dan200.computer.api.IComputerAccess;

import ic2.api.ElectricItem;
import ic2.api.EnergyNet;
import ic2.api.IElectricItem;
import ic2.api.IWrenchable;
import mekanism.api.IEnergizedItem;
import net.minecraft.src.*;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;

public abstract class TileEntityElectricMachine extends TileEntityBasicMachine
{
	/**
	 * A simple electrical machine. This has 3 slots - the input slot (0), the energy slot (1), 
	 * output slot (2), and the upgrade slot (3). It will not run if it does not have enough energy.
	 * 
	 * @param soundPath - location of the sound effect
	 * @param name - full name of this machine
	 * @param path - GUI texture path of this machine
	 * @param perTick - energy used per tick.
	 * @param ticksRequired - ticks required to operate -- or smelt an item.
	 * @param maxEnergy - maximum energy this machine can hold.
	 */
	public TileEntityElectricMachine(String soundPath, String name, String path, int perTick, int ticksRequired, int maxEnergy)
	{
		super(soundPath, name, path, perTick, ticksRequired, maxEnergy);
		inventory = new ItemStack[4];
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		boolean testActive = operatingTicks > 0;
		
		if(inventory[1] != null)
		{
			if(energyStored < currentMaxEnergy)
			{
				if(inventory[1].getItem() instanceof IEnergizedItem)
				{
					IEnergizedItem item = (IEnergizedItem)inventory[1].getItem();
					
					if(item.canBeDischarged())
					{
						int received = 0;
						int energyNeeded = currentMaxEnergy - energyStored;
						if(item.getRate() <= energyNeeded)
						{
							received = item.discharge(inventory[1], item.getRate());
						}
						else if(item.getRate() > energyNeeded)
						{
							received = item.discharge(inventory[1], energyNeeded);
						}
						
						setEnergy(energyStored + received);
					}
				}
				else if(inventory[1].getItem() instanceof IItemElectric)
				{
					IItemElectric electricItem = (IItemElectric) inventory[1].getItem();

					if (electricItem.canProduceElectricity())
					{
						double joulesReceived = electricItem.onUse(electricItem.getMaxJoules() * 0.005, inventory[1]);
						setEnergy(energyStored + (int)(joulesReceived*UniversalElectricity.TO_IC2_RATIO));
					}
				}
				else if(inventory[1].getItem() instanceof IElectricItem)
				{
					IElectricItem item = (IElectricItem)inventory[1].getItem();
					if(item.canProvideEnergy())
					{
						int gain = ElectricItem.discharge(inventory[1], currentMaxEnergy - energyStored, 3, false, false);
						setEnergy(energyStored + gain);
					}
				}
			}
			if(inventory[1].itemID == Item.redstone.shiftedIndex && energyStored <= (currentMaxEnergy-1000))
			{
				setEnergy(energyStored + 1000);
				--inventory[1].stackSize;
				
	            if (inventory[1].stackSize <= 0)
	            {
	                inventory[1] = null;
	            }
			}
		}
		
		if(inventory[3] != null)
		{
			int energyToAdd = 0;
			int ticksToRemove = 0;
			
			if(inventory[3].isItemEqual(new ItemStack(Mekanism.SpeedUpgrade)))
			{
				if(currentTicksRequired == TICKS_REQUIRED)
				{
					ticksToRemove = 150;
				}
			}
			else if(inventory[3].isItemEqual(new ItemStack(Mekanism.EnergyUpgrade)))
			{
				if(currentMaxEnergy == MAX_ENERGY)
				{
					energyToAdd = 600;
				}
			}
			else if(inventory[3].isItemEqual(new ItemStack(Mekanism.UltimateUpgrade)))
			{
				if(currentTicksRequired == TICKS_REQUIRED)
				{
					ticksToRemove = 150;
				}
				if(currentMaxEnergy == MAX_ENERGY)
				{
					energyToAdd = 600;
				}
			}
			
			currentMaxEnergy += energyToAdd;
			currentTicksRequired -= ticksToRemove;
		}
		else if(inventory[3] == null)
		{
			currentTicksRequired = TICKS_REQUIRED;
			currentMaxEnergy = MAX_ENERGY;
		}
		
		if(canOperate() && (operatingTicks+1) < currentTicksRequired)
		{
			++operatingTicks;
			energyStored -= ENERGY_PER_TICK;
		}
		else if(canOperate() && (operatingTicks+1) >= currentTicksRequired)
		{
			if(!worldObj.isRemote)
			{
				operate();
			}
			operatingTicks = 0;
			energyStored -= ENERGY_PER_TICK;
		}
		
		if(energyStored < 0)
		{
			energyStored = 0;
		}
		
		if(energyStored > currentMaxEnergy)
		{
			energyStored = currentMaxEnergy;
		}
		
		if(!canOperate())
		{
			operatingTicks = 0;
		}
		
		if(!worldObj.isRemote)
		{
			if(testActive != operatingTicks > 0)
			{
				if(operatingTicks > 0)
				{
					setActive(true);
				}
				else if(!canOperate())
				{
					setActive(false);
				}
			}
		}
	}

	@Override
    public void operate()
    {
        if (!canOperate())
        {
            return;
        }

        ItemStack itemstack;

        if (inventory[0].getItem().hasContainerItem())
        {
            itemstack = RecipeHandler.getOutput(inventory[0], false, getRecipes()).copy();
            inventory[0] = new ItemStack(inventory[0].getItem().getContainerItem());
        }
        else
        {
            itemstack = RecipeHandler.getOutput(inventory[0], true, getRecipes()).copy();
        }

        if (inventory[0].stackSize <= 0)
        {
            inventory[0] = null;
        }

        if (inventory[2] == null)
        {
            inventory[2] = itemstack;
        }
        else
        {
            inventory[2].stackSize += itemstack.stackSize;
        }
    }

	@Override
    public boolean canOperate()
    {
        if (inventory[0] == null)
        {
            return false;
        }
        
        if(energyStored < ENERGY_PER_TICK)
        {
        	return false;
        }

        ItemStack itemstack = RecipeHandler.getOutput(inventory[0], false, getRecipes());

        if (itemstack == null)
        {
            return false;
        }

        if (inventory[2] == null)
        {
            return true;
        }

        if (!inventory[2].isItemEqual(itemstack))
        {
            return false;
        }
        else
        {
            return inventory[2].stackSize + itemstack.stackSize <= inventory[2].getMaxStackSize();
        }
    }
	
	@Override
	public void handlePacketData(INetworkManager network, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream)
	{
		try {
			facing = dataStream.readInt();
			isActive = dataStream.readBoolean();
			operatingTicks = dataStream.readInt();
			energyStored = dataStream.readInt();
			currentMaxEnergy = dataStream.readInt();
			currentTicksRequired = dataStream.readInt();
			worldObj.markBlockAsNeedsUpdate(xCoord, yCoord, zCoord);
		} catch (Exception e)
		{
			System.out.println("[Mekanism] Error while handling tile entity packet.");
			e.printStackTrace();
		}
	}
    
	@Override
    public void sendPacket()
    {
		PacketHandler.sendTileEntityPacket(this, facing, isActive, operatingTicks, energyStored, currentMaxEnergy, currentTicksRequired);
    }
    
	@Override
    public void sendPacketWithRange()
    {
		PacketHandler.sendTileEntityPacketWithRange(this, 50, facing, isActive, operatingTicks, energyStored, currentMaxEnergy, currentTicksRequired);
    }

	@Override
	public String[] getMethodNames() 
	{
		return new String[] {"getStored", "getProgress", "isActive", "facing", "canOperate", "getMaxEnergy", "getEnergyNeeded"};
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, int method, Object[] arguments) throws Exception 
	{
		switch(method)
		{
			case 0:
				return new Object[] {energyStored};
			case 1:
				return new Object[] {operatingTicks};
			case 2:
				return new Object[] {isActive};
			case 3:
				return new Object[] {facing};
			case 4:
				return new Object[] {canOperate()};
			case 5:
				return new Object[] {currentMaxEnergy};
			case 6:
				return new Object[] {(currentMaxEnergy-energyStored)};
			default:
				System.err.println("[Mekanism] Attempted to call unknown method with computer ID " + computer.getID());
				return new Object[] {"Unknown command."};
		}
	}
}
