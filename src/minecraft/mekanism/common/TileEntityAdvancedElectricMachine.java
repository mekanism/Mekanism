package mekanism.common;

import java.util.ArrayList;

import ic2.api.ElectricItem;
import ic2.api.IElectricItem;
import mekanism.api.EnumColor;
import mekanism.api.SideData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.implement.IItemElectric;

import com.google.common.io.ByteArrayDataInput;

import dan200.computer.api.IComputerAccess;

public abstract class TileEntityAdvancedElectricMachine extends TileEntityBasicMachine
{
	/** How much secondary energy (fuel) this machine uses per tick. */
	public int SECONDARY_ENERGY_PER_TICK;
	
	/** Maximum amount of secondary energy (fuel) this machine can hold. */
	public int MAX_SECONDARY_ENERGY;
	
	/** How much secondary energy (fuel) is stored in this machine. */
	public int secondaryEnergyStored = 0;
	
	/**
	 * Advanced Electric Machine -- a machine like this has a total of 4 slots. Input slot (0), fuel slot (1), output slot (2), 
	 * energy slot (3), and the upgrade slot (4). The machine will not run if it does not have enough electricity, or if it doesn't have enough
	 * fuel ticks.
	 * 
	 * @param soundPath - location of the sound effect
	 * @param name - full name of this machine
	 * @param path - GUI texture path of this machine
	 * @param perTick - how much energy this machine uses per tick.
	 * @param secondaryPerTick - how much secondary energy (fuel) this machine uses per tick.
	 * @param ticksRequired - how many ticks it takes to smelt an item.
	 * @param maxEnergy - maximum amount of energy this machine can hold.
	 * @param maxSecondaryEnergy - maximum amount of secondary energy (fuel) this machine can hold.
	 */
	public TileEntityAdvancedElectricMachine(String soundPath, String name, String path, int perTick, int secondaryPerTick, int ticksRequired, int maxEnergy, int maxSecondaryEnergy)
	{
		super(soundPath, name, path, perTick, ticksRequired, maxEnergy);
		
		sideOutputs.add(new SideData(EnumColor.GREY, 0, 0));
		sideOutputs.add(new SideData(EnumColor.DARK_RED, 0, 1));
		sideOutputs.add(new SideData(EnumColor.PURPLE, 1, 1));
		sideOutputs.add(new SideData(EnumColor.DARK_BLUE, 2, 1));
		sideOutputs.add(new SideData(EnumColor.DARK_GREEN, 3, 1));
		sideOutputs.add(new SideData(EnumColor.ORANGE, 4, 1));
		
		sideConfig = new byte[] {2, 1, 0, 4, 5, 3};
		
		inventory = new ItemStack[5];
		SECONDARY_ENERGY_PER_TICK = secondaryPerTick;
		MAX_SECONDARY_ENERGY = maxSecondaryEnergy;
	}
    
    /**
     * Gets the amount of ticks the declared itemstack can fuel this machine.
     * @param itemstack - itemstack to check with
     * @return fuel ticks
     */
    public abstract int getFuelTicks(ItemStack itemstack);
	
    @Override
	public void onUpdate()
	{
		super.onUpdate();
		
		boolean testActive = operatingTicks > 0;
		
		if(inventory[3] != null)
		{
			if(electricityStored < MekanismUtils.getEnergy(energyMultiplier, MAX_ELECTRICITY))
			{
				if(inventory[3].getItem() instanceof IItemElectric)
				{
					IItemElectric electricItem = (IItemElectric)inventory[3].getItem();

					if (electricItem.canProduceElectricity())
					{
						double joulesNeeded = MekanismUtils.getEnergy(energyMultiplier, MAX_ELECTRICITY)-electricityStored;
						double joulesReceived = electricItem.onUse(Math.min(electricItem.getMaxJoules(inventory[3])*0.005, joulesNeeded), inventory[3]);
						setJoules(electricityStored + joulesReceived);
					}
				}
				else if(inventory[3].getItem() instanceof IElectricItem)
				{
					IElectricItem item = (IElectricItem)inventory[3].getItem();
					if(item.canProvideEnergy())
					{
						double gain = ElectricItem.discharge(inventory[3], (int)((MAX_ELECTRICITY - electricityStored)*Mekanism.TO_IC2), 3, false, false)*Mekanism.FROM_IC2;
						setJoules(electricityStored + gain);
					}
				}
			}
			if(inventory[3].itemID == Item.redstone.itemID && electricityStored+1000 <= MekanismUtils.getEnergy(energyMultiplier, MAX_ELECTRICITY))
			{
				setJoules(electricityStored + 1000);
				--inventory[3].stackSize;
				
	            if (inventory[3].stackSize <= 0)
	            {
	                inventory[3] = null;
	            }
			}
		}
		
		if(inventory[4] != null)
		{
			if(inventory[4].isItemEqual(new ItemStack(Mekanism.EnergyUpgrade)) && energyMultiplier < 8)
			{
				if(upgradeTicks < UPGRADE_TICKS_REQUIRED)
				{
					upgradeTicks++;
				}
				else if(upgradeTicks == UPGRADE_TICKS_REQUIRED)
				{
					upgradeTicks = 0;
					energyMultiplier+=1;
					
					inventory[4].stackSize--;
					
					if(inventory[4].stackSize == 0)
					{
						inventory[4] = null;
					}
				}
			}
			else if(inventory[4].isItemEqual(new ItemStack(Mekanism.SpeedUpgrade)) && speedMultiplier < 8)
			{
				if(upgradeTicks < UPGRADE_TICKS_REQUIRED)
				{
					upgradeTicks++;
				}
				else if(upgradeTicks == UPGRADE_TICKS_REQUIRED)
				{
					upgradeTicks = 0;
					speedMultiplier+=1;
					
					inventory[4].stackSize--;
					
					if(inventory[4].stackSize == 0)
					{
						inventory[4] = null;
					}
				}
			}
			else {
				upgradeTicks = 0;
			}
		}
		else {
			upgradeTicks = 0;
		}
		
		handleSecondaryFuel();
		
		if(electricityStored >= ENERGY_PER_TICK && secondaryEnergyStored >= SECONDARY_ENERGY_PER_TICK)
		{
			if(canOperate() && (operatingTicks+1) < MekanismUtils.getTicks(speedMultiplier) && secondaryEnergyStored >= SECONDARY_ENERGY_PER_TICK)
			{
				++operatingTicks;
				secondaryEnergyStored -= SECONDARY_ENERGY_PER_TICK;
				electricityStored -= ENERGY_PER_TICK;
			}
			else if((operatingTicks+1) >= MekanismUtils.getTicks(speedMultiplier))
			{
				if(!worldObj.isRemote)
				{
					operate();
				}
				operatingTicks = 0;
				secondaryEnergyStored -= SECONDARY_ENERGY_PER_TICK;
				electricityStored -= ENERGY_PER_TICK;
			}
		}
		
		if(!canOperate())
		{
			operatingTicks = 0;
		}
		
		if(!worldObj.isRemote)
		{
			if(canOperate() && electricityStored >= ENERGY_PER_TICK && secondaryEnergyStored >= SECONDARY_ENERGY_PER_TICK)
			{
				setActive(true);
			}
			else {
				setActive(false);
			}
		}
	}
    
    public void handleSecondaryFuel()
    {
		if(inventory[1] != null)
		{
			int fuelTicks = getFuelTicks(inventory[1]);
			int energyNeeded = MAX_SECONDARY_ENERGY - secondaryEnergyStored;
			if(fuelTicks > 0 && fuelTicks <= energyNeeded)
			{
				if(fuelTicks <= energyNeeded)
				{
					setSecondaryEnergy(secondaryEnergyStored + fuelTicks);
				}
				else if(fuelTicks > energyNeeded)
				{
					setSecondaryEnergy(secondaryEnergyStored + energyNeeded);
				}
				--inventory[1].stackSize;
				
				if(inventory[1].stackSize == 0)
				{
					inventory[1] = null;
				}
			}
		}
    }

    @Override
    public void operate()
    {
        ItemStack itemstack = RecipeHandler.getOutput(inventory[0], true, getRecipes());

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
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		super.handlePacketData(dataStream);
		secondaryEnergyStored = dataStream.readInt();
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		data.add(secondaryEnergyStored);
		return data;
	}
	
    @Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
    	super.readFromNBT(nbtTags);
    	
        secondaryEnergyStored = nbtTags.getInteger("secondaryEnergyStored");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setInteger("secondaryEnergyStored", secondaryEnergyStored);
    }
	
	/**
	 * Sets the secondary energy to a new amount
	 * @param energy - amount to store
	 */
	public void setSecondaryEnergy(int energy)
	{
		secondaryEnergyStored = Math.max(Math.min(energy, MAX_SECONDARY_ENERGY), 0);
	}
	
	/**
	 * Gets the scaled secondary energy level for the GUI.
	 * @param i - multiplier
	 * @return scaled secondary energy
	 */
	public int getScaledSecondaryEnergyLevel(int i)
	{
		return secondaryEnergyStored*i / MAX_SECONDARY_ENERGY;
	}

	@Override
	public String[] getMethodNames() 
	{
		return new String[] {"getStored", "getSecondaryStored", "getProgress", "isActive", "facing", "canOperate", "getMaxEnergy", "getEnergyNeeded"};
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, int method, Object[] arguments) throws Exception 
	{
		switch(method)
		{
			case 0:
				return new Object[] {electricityStored};
			case 1:
				return new Object[] {secondaryEnergyStored};
			case 2:
				return new Object[] {operatingTicks};
			case 3:
				return new Object[] {isActive};
			case 4:
				return new Object[] {facing};
			case 5:
				return new Object[] {canOperate()};
			case 6:
				return new Object[] {MekanismUtils.getEnergy(energyMultiplier, MAX_ELECTRICITY)};
			case 7:
				return new Object[] {(MekanismUtils.getEnergy(energyMultiplier, MAX_ELECTRICITY)-electricityStored)};
			default:
				System.err.println("[Mekanism] Attempted to call unknown method with computer ID " + computer.getID());
				return new Object[] {"Unknown command."};
		}
	}
}
