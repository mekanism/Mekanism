package mekanism.common;

import ic2.api.Direction;
import ic2.api.ElectricItem;
import ic2.api.IElectricItem;
import ic2.api.energy.tile.IEnergySink;
import mekanism.api.IActiveState;
import mekanism.api.Tier.SmeltingFactoryTier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import universalelectricity.core.implement.IItemElectric;
import universalelectricity.core.implement.IJouleStorage;
import universalelectricity.core.implement.IVoltage;

import com.google.common.io.ByteArrayDataInput;

import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IPeripheral;

public class TileEntitySmeltingFactory extends TileEntityElectricBlock implements IEnergySink, IJouleStorage, IVoltage, IPeripheral, IActiveState
{	
	/** This Smelting Factory's tier. */
	public SmeltingFactoryTier tier;
	
	/** An int[] used to track all current operations' progress. */
	public int[] progress;
	
	/** How many ticks it takes, by default, to run an operation. */
	public int TICKS_REQUIRED = 200;
	
	/** How much energy each operation consumes per tick. */
	public int ENERGY_PER_TICK = 5;
	
	/** How many ticks it takes, currently, to run an operation. */
	public int currentTicksRequired;
	
	/** The current electricity cap this machine can handle. */
	public double currentMaxElectricity;
	
	/** This machine's previous active state, used for calculating packets. */
	public boolean prevActive;
	
	/** This machine's active state. */
	public boolean isActive;
	
	public TileEntitySmeltingFactory()
	{
		this(SmeltingFactoryTier.BASIC);
	}
	
	public TileEntitySmeltingFactory(SmeltingFactoryTier type)
	{
		super(type.name + " Smelting Factory", type.processes*1000);
		tier = type;
		currentTicksRequired = TICKS_REQUIRED;
		currentMaxElectricity = MAX_ELECTRICITY;
		inventory = new ItemStack[2+type.processes*2];
		progress = new int[type.processes];
		isActive = false;
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		boolean testActive = false;
		
		for(int i : progress)
		{
			if(i > 0)
			{
				testActive = true;
			}
		}
		
		if(inventory[1] != null)
		{
			if(electricityStored < currentMaxElectricity)
			{
				if(inventory[1].getItem() instanceof IItemElectric)
				{
					IItemElectric electricItem = (IItemElectric)inventory[1].getItem();

					if (electricItem.canProduceElectricity())
					{
						double joulesNeeded = currentMaxElectricity-electricityStored;
						double joulesReceived = 0;
						
						if(electricItem.getVoltage() <= joulesNeeded)
						{
							joulesReceived = electricItem.onUse(electricItem.getVoltage(), inventory[1]);
						}
						else if(electricItem.getVoltage() > joulesNeeded)
						{
							joulesReceived = electricItem.onUse(joulesNeeded, inventory[1]);
						}
						
						setJoules(electricityStored + joulesReceived);
					}
				}
				else if(inventory[1].getItem() instanceof IElectricItem)
				{
					IElectricItem item = (IElectricItem)inventory[1].getItem();
					if(item.canProvideEnergy())
					{
						double gain = ElectricItem.discharge(inventory[1], (int)((MAX_ELECTRICITY - electricityStored)*Mekanism.TO_IC2), 3, false, false)*Mekanism.FROM_IC2;
						setJoules(electricityStored + gain);
					}
				}
			}
			if(inventory[1].itemID == Item.redstone.shiftedIndex && electricityStored <= (currentMaxElectricity-1000))
			{
				setJoules(electricityStored + 1000);
				--inventory[1].stackSize;
				
	            if (inventory[1].stackSize <= 0)
	            {
	                inventory[1] = null;
	            }
			}
		}
		
		if(inventory[0] != null)
		{
			int energyToAdd = 0;
			int ticksToRemove = 0;
			
			if(inventory[0].isItemEqual(new ItemStack(Mekanism.SpeedUpgrade)))
			{
				if(currentTicksRequired == TICKS_REQUIRED)
				{
					ticksToRemove = 150;
				}
			}
			else if(inventory[0].isItemEqual(new ItemStack(Mekanism.EnergyUpgrade)))
			{
				if(currentMaxElectricity == MAX_ELECTRICITY)
				{
					energyToAdd = 600;
				}
			}
			else if(inventory[0].isItemEqual(new ItemStack(Mekanism.UltimateUpgrade)))
			{
				if(currentTicksRequired == TICKS_REQUIRED)
				{
					ticksToRemove = 150;
				}
				if(currentMaxElectricity == MAX_ELECTRICITY)
				{
					energyToAdd = 600;
				}
			}
			
			currentMaxElectricity += energyToAdd;
			currentTicksRequired -= ticksToRemove;
		}
		else if(inventory[0] == null)
		{
			currentTicksRequired = TICKS_REQUIRED;
			currentMaxElectricity = MAX_ELECTRICITY;
		}
		
		for(int mainSlot = 0; mainSlot < tier.processes; mainSlot++)
		{
			if(canOperate(getInputSlot(mainSlot), getOutputSlot(mainSlot)) && (progress[mainSlot]+1) < currentTicksRequired)
			{
				++progress[mainSlot];
				electricityStored -= ENERGY_PER_TICK;
			}
			else if(canOperate(getInputSlot(mainSlot), getOutputSlot(mainSlot)) && (progress[mainSlot]+1) >= currentTicksRequired)
			{
				if(!worldObj.isRemote)
				{
					operate(getInputSlot(mainSlot), getOutputSlot(mainSlot));
				}
				progress[mainSlot] = 0;
				electricityStored -= ENERGY_PER_TICK;
			}
			
			if(electricityStored < 0)
			{
				electricityStored = 0;
			}
			
			if(electricityStored > currentMaxElectricity)
			{
				electricityStored = currentMaxElectricity;
			}
			
			if(!canOperate(getInputSlot(mainSlot), getOutputSlot(mainSlot)))
			{
				progress[mainSlot] = 0;
			}
		}
		
		if(!worldObj.isRemote)
		{
			boolean newActive = false;
			boolean hasOperation = false;
			
			for(int i = 0; i < tier.processes; i++)
			{
				if(canOperate(getInputSlot(i), getOutputSlot(i)))
				{
					hasOperation = true;
				}
			}
			
			for(int i : progress)
			{
				if(i > 0)
				{
					newActive = true;
				}
			}
			
			if(testActive != newActive)
			{
				if(newActive)
				{
					setActive(true);
				}
				
				else if(!hasOperation)
				{
					setActive(false);
				}
			}
		}
	}
	
	public int getScaledProgress(int i, int process)
	{
		return progress[process]*i / currentTicksRequired;
	}
	
	/**
	 * Gets the scaled energy level for the GUI.
	 * @param i - multiplier
	 * @return scaled energy
	 */
	public int getScaledEnergyLevel(int i)
	{
		return (int)(electricityStored*i / currentMaxElectricity);
	}
	
	public boolean canOperate(int inputSlot, int outputSlot)
	{
        if (inventory[inputSlot] == null)
        {
            return false;
        }
        
        if(electricityStored < ENERGY_PER_TICK)
        {
        	return false;
        }

        ItemStack itemstack = FurnaceRecipes.smelting().getSmeltingResult(inventory[inputSlot]).copy();

        if (itemstack == null)
        {
            return false;
        }

        if (inventory[outputSlot] == null)
        {
            return true;
        }

        if (!inventory[outputSlot].isItemEqual(itemstack))
        {
            return false;
        }
        else
        {
            return inventory[outputSlot].stackSize + itemstack.stackSize <= inventory[outputSlot].getMaxStackSize();
        }
	}
	
	public void operate(int inputSlot, int outputSlot)
	{
        if (!canOperate(inputSlot, outputSlot))
        {
            return;
        }

        ItemStack itemstack = FurnaceRecipes.smelting().getSmeltingResult(inventory[inputSlot]).copy();
        
        inventory[inputSlot].stackSize--;

        if (inventory[inputSlot].stackSize <= 0)
        {
            inventory[inputSlot] = null;
        }

        if (inventory[outputSlot] == null)
        {
            inventory[outputSlot] = itemstack;
        }
        else
        {
            inventory[outputSlot].stackSize += itemstack.stackSize;
        }
	}

	@Override
	public void handlePacketData(INetworkManager network, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream)
	{
		try {
			facing = dataStream.readInt();
			electricityStored = dataStream.readDouble();
			currentTicksRequired = dataStream.readInt();
			currentMaxElectricity = dataStream.readDouble();
			isActive = dataStream.readBoolean();
			
			for(int i = 0; i < tier.processes; i++)
			{
				progress[i] = dataStream.readInt();
			}
			
			worldObj.markBlockForRenderUpdate(xCoord, yCoord, zCoord);
		} catch (Exception e)
		{
			System.out.println("[Mekanism] Error while handling tile entity packet.");
			e.printStackTrace();
		}
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);
        
        currentTicksRequired = nbtTags.getInteger("currentTicksRequired");
        currentMaxElectricity = nbtTags.getDouble("currentMaxElectricity");
        isActive = nbtTags.getBoolean("isActive");
        
        for(int i = 0; i < tier.processes; i++)
        {
        	progress[i] = nbtTags.getInteger("progress" + i);
        }
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setInteger("currentTicksRequired", currentTicksRequired);
        nbtTags.setDouble("currentMaxElectricity", currentMaxElectricity);
        nbtTags.setBoolean("isActive", isActive);
        
        for(int i = 0; i < tier.processes; i++)
        {
        	nbtTags.setInteger("progress" + i, progress[i]);
        }
    }

	@Override
	public void sendPacket()
	{
		PacketHandler.sendTileEntityPacketToClients(this, 0, facing, electricityStored, currentTicksRequired, currentMaxElectricity, isActive, progress);
	}

	@Override
	public void sendPacketWithRange() 
	{
		PacketHandler.sendTileEntityPacketToClients(this, 50, facing, electricityStored, currentTicksRequired, currentMaxElectricity, isActive, progress);
	}

	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, Direction direction) 
	{
		return true;
	}
	
	public int getInputSlot(int operation)
	{
		return (operation+operation)+2;
	}
	
	public int getOutputSlot(int operation)
	{
		return (operation+operation)+3;
	}

	@Override
	public String getType()
	{
		return getInvName();
	}

	@Override
	public String[] getMethodNames() 
	{
		return new String[] {"getStored", "getProgress", "facing", "canOperate", "getMaxEnergy", "getEnergyNeeded"};
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, int method, Object[] arguments) throws Exception 
	{
		switch(method)
		{
			case 0:
				return new Object[] {electricityStored};
			case 1:
				if(arguments[0] == null)
				{
					return new Object[] {"Please provide a target operation."};
				}
				
				if(!(arguments[0] instanceof Double) && !(arguments[0] instanceof Integer))
				{
					return new Object[] {"Invalid characters."};
				}
				
				if((Integer)arguments[0] < 0 || (Integer)arguments[0] > progress.length)
				{
					return new Object[] {"No such operation found."};
				}
				
				return new Object[] {progress[(Integer)arguments[0]]};
			case 2:
				return new Object[] {facing};
			case 3:
				if(arguments[0] == null)
				{
					return new Object[] {"Please provide a target operation."};
				}
				
				if(!(arguments[0] instanceof Double) && !(arguments[0] instanceof Integer))
				{
					return new Object[] {"Invalid characters."};
				}
				
				if((Integer)arguments[0] < 0 || (Integer)arguments[0] > progress.length)
				{
					return new Object[] {"No such operation found."};
				}
				
				return new Object[] {canOperate(getInputSlot((Integer)arguments[0]), getOutputSlot((Integer)arguments[0]))};
			case 4:
				return new Object[] {currentMaxElectricity};
			case 5:
				return new Object[] {(currentMaxElectricity-electricityStored)};
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
	public void attach(IComputerAccess computer, String computerSide) {}

	@Override
	public void detach(IComputerAccess computer) {}

	@Override
	public double getVoltage() 
	{
		return 120;
	}

	@Override
	public double getJoules(Object... data) 
	{
		return electricityStored;
	}

	@Override
	public void setJoules(double joules, Object... data) 
	{
		electricityStored = Math.max(Math.min(joules, getMaxJoules()), 0);
	}

	@Override
	public double getMaxJoules(Object... data) 
	{
		return currentMaxElectricity;
	}

	@Override
	public int demandsEnergy() 
	{
		return (int)((currentMaxElectricity - electricityStored)*Mekanism.TO_IC2);
	}
	
	@Override
    public void setActive(boolean active)
    {
    	isActive = active;
    	
    	if(prevActive != active)
    	{
    		sendPacket();
    	}
    	
    	prevActive = active;
    }
    
    @Override
    public boolean getActive()
    {
    	return isActive;
    }
    
	@Override
	public int getMaxSafeInput()
	{
		return 2048;
	}

	@Override
    public int injectEnergy(Direction direction, int i)
    {
		double givenEnergy = i*Mekanism.FROM_IC2;
    	double rejects = 0;
    	double neededEnergy = currentMaxElectricity-electricityStored;
    	
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
}
