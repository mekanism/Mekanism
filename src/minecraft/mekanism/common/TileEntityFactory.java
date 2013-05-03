package mekanism.common;

import ic2.api.Direction;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.item.IElectricItem;

import java.util.ArrayList;

import mekanism.api.EnumColor;
import mekanism.api.IConfigurable;
import mekanism.api.IStrictEnergyAcceptor;
import mekanism.api.IUpgradeManagement;
import mekanism.api.SideData;
import mekanism.client.IHasSound;
import mekanism.common.BlockMachine.MachineType;
import mekanism.common.IFactory.RecipeType;
import mekanism.common.Tier.FactoryTier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.item.IItemElectric;

import com.google.common.io.ByteArrayDataInput;

import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IPeripheral;

public class TileEntityFactory extends TileEntityElectricBlock implements IEnergySink, IPeripheral, IActiveState, IConfigurable, IUpgradeManagement, IHasSound, IStrictEnergyAcceptor
{	
	/** This Factory's tier. */
	public FactoryTier tier;
	
	/** This machine's side configuration. */
	public byte[] sideConfig;
	
	/** An arraylist of SideData for this machine. */
	public ArrayList<SideData> sideOutputs = new ArrayList<SideData>();
	
	/** An int[] used to track all current operations' progress. */
	public int[] progress;
	
	/** How many ticks it takes, by default, to run an operation. */
	public int TICKS_REQUIRED = 200;
	
	/** How much energy each operation consumes per tick. */
	public int ENERGY_PER_TICK = 10;
	
	/** This machine's speed multiplier. */
	public int speedMultiplier;
	
	/** This machine's energy multiplier. */
	public int energyMultiplier;
	
	/** How long it takes this machine to install an upgrade. */
	public int UPGRADE_TICKS_REQUIRED = 40;
	
	/** How many upgrade ticks have progressed. */
	public int upgradeTicks;
	
	/** How long it takes this factory to switch recipe types. */
	public int RECIPE_TICKS_REQUIRED = 40;
	
	/** How many recipe ticks have progressed. */
	public int recipeTicks;
	
	/** This machine's previous active state, used for calculating packets. */
	public boolean prevActive;
	
	/** This machine's active state. */
	public boolean isActive;
	
	/** This machine's recipe type. */
	public int recipeType;
	
	public TileEntityFactory()
	{
		this(FactoryTier.BASIC);
		
		sideOutputs.add(new SideData(EnumColor.GREY, 0, 0, new int[0]));
		sideOutputs.add(new SideData(EnumColor.ORANGE, 0, 1, new int[] {0}));
		sideOutputs.add(new SideData(EnumColor.DARK_GREEN, 1, 1, new int[] {1}));
		sideOutputs.add(new SideData(EnumColor.DARK_RED, 4, 3, new int[] {4, 5, 6}));
		sideOutputs.add(new SideData(EnumColor.DARK_BLUE, 7, 3, new int[] {7, 8, 9}));
		
		sideConfig = new byte[] {4, 3, 0, 0, 2, 1};
	}
	
	public TileEntityFactory(FactoryTier type)
	{
		super(type.name + " Factory", type.processes*2000);
		tier = type;
		inventory = new ItemStack[4+type.processes*2];
		progress = new int[type.processes];
		isActive = false;
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if(worldObj.isRemote)
		{
			Mekanism.proxy.registerSound(this);
		}
		
		if(!worldObj.isRemote)
		{
			ChargeUtils.discharge(1, this);
			
			if(inventory[0] != null)
			{
				if(inventory[0].isItemEqual(new ItemStack(Mekanism.EnergyUpgrade)) && energyMultiplier < 8)
				{
					if(upgradeTicks < UPGRADE_TICKS_REQUIRED)
					{
						upgradeTicks++;
					}
					else if(upgradeTicks == UPGRADE_TICKS_REQUIRED)
					{
						upgradeTicks = 0;
						energyMultiplier++;
						
						inventory[0].stackSize--;
						
						if(inventory[0].stackSize == 0)
						{
							inventory[0] = null;
						}
					}
				}
				else if(inventory[0].isItemEqual(new ItemStack(Mekanism.SpeedUpgrade)) && speedMultiplier < 8)
				{
					if(upgradeTicks < UPGRADE_TICKS_REQUIRED)
					{
						upgradeTicks++;
					}
					else if(upgradeTicks == UPGRADE_TICKS_REQUIRED)
					{
						upgradeTicks = 0;
						speedMultiplier++;
						
						inventory[0].stackSize--;
						
						if(inventory[0].stackSize == 0)
						{
							inventory[0] = null;
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
			
			if(inventory[2] != null && inventory[3] == null)
			{
				if(inventory[2].isItemEqual(new ItemStack(Mekanism.MachineBlock, 1, MachineType.ENERGIZED_SMELTER.meta)) && recipeType != 0)
				{
					if(recipeTicks < RECIPE_TICKS_REQUIRED)
					{
						recipeTicks++;
					}
					else if(recipeTicks == RECIPE_TICKS_REQUIRED)
					{
						recipeTicks = 0;
						
						inventory[2] = null;
						inventory[3] = getMachineStack();
						
						recipeType = 0;
					}
				}
				else if(inventory[2].isItemEqual(new ItemStack(Mekanism.MachineBlock, 1, MachineType.ENRICHMENT_CHAMBER.meta)) && recipeType != 1)
				{
					if(recipeTicks < RECIPE_TICKS_REQUIRED)
					{
						recipeTicks++;
					}
					else if(recipeTicks == RECIPE_TICKS_REQUIRED)
					{
						recipeTicks = 0;
						
						inventory[2] = null;
						inventory[3] = getMachineStack();
						
						recipeType = 1;
					}
				}
				else if(inventory[2].isItemEqual(new ItemStack(Mekanism.MachineBlock, 1, MachineType.CRUSHER.meta)) && recipeType != 2)
				{
					if(recipeTicks < RECIPE_TICKS_REQUIRED)
					{
						recipeTicks++;
					}
					else if(recipeTicks == RECIPE_TICKS_REQUIRED)
					{
						recipeTicks = 0;
						
						inventory[2] = null;
						inventory[3] = getMachineStack();
						
						recipeType = 2;
					}
				}
				else {
					recipeTicks = 0;
				}
			}
			else {
				recipeTicks = 0;
			}
			
			for(int process = 0; process < tier.processes; process++)
			{
				if(electricityStored >= ENERGY_PER_TICK)
				{
					if(canOperate(getInputSlot(process), getOutputSlot(process)) && (progress[process]+1) < MekanismUtils.getTicks(speedMultiplier, TICKS_REQUIRED))
					{
						progress[process]++;
						electricityStored -= ENERGY_PER_TICK;
					}
					else if(canOperate(getInputSlot(process), getOutputSlot(process)) && (progress[process]+1) >= MekanismUtils.getTicks(speedMultiplier, TICKS_REQUIRED))
					{
						operate(getInputSlot(process), getOutputSlot(process));
						
						progress[process] = 0;
						electricityStored -= ENERGY_PER_TICK;
					}
				}
				
				if(!canOperate(getInputSlot(process), getOutputSlot(process)))
				{
					progress[process] = 0;
				}
			}
			
			if(!worldObj.isRemote)
			{
				boolean hasOperation = false;
				
				for(int i = 0; i < tier.processes; i++)
				{
					if(canOperate(getInputSlot(i), getOutputSlot(i)))
					{
						hasOperation = true;
						break;
					}
				}
				
				if(hasOperation && electricityStored >= ENERGY_PER_TICK)
				{
					setActive(true);
				}
				else {
					setActive(false);
				}
			}
		}
	}
	
	public ItemStack getMachineStack()
	{
		switch(recipeType)
		{
			case 0:
				return new ItemStack(Mekanism.MachineBlock, 1, MachineType.ENERGIZED_SMELTER.meta);
			case 1:
				return new ItemStack(Mekanism.MachineBlock, 1, MachineType.ENRICHMENT_CHAMBER.meta);
			case 2:
				return new ItemStack(Mekanism.MachineBlock, 1, MachineType.CRUSHER.meta);
			default:
				return null;
		}
	}
	
	@Override
	public boolean func_102008_b(int slotID, ItemStack itemstack, int side)
	{
		if(slotID == 1)
		{
			return (itemstack.getItem() instanceof IItemElectric && ((IItemElectric)itemstack.getItem()).getProvideRequest(itemstack).getWatts() == 0) ||
					(itemstack.getItem() instanceof IElectricItem && ((IElectricItem)itemstack.getItem()).canProvideEnergy(itemstack) && 
							(!(itemstack.getItem() instanceof IItemElectric) || 
							((IItemElectric)itemstack.getItem()).getProvideRequest(itemstack).getWatts() == 0));
		}
		else if(tier == FactoryTier.BASIC && slotID >= 7 && slotID <= 9)
		{
			return true;
		}
		else if(tier == FactoryTier.ADVANCED && slotID >= 9 && slotID <= 13)
		{
			return true;
		}
		else if(tier == FactoryTier.ELITE && slotID >= 11 && slotID <= 17)
		{
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean isStackValidForSlot(int slotID, ItemStack itemstack)
	{
		if(tier == FactoryTier.BASIC)
		{
			if(slotID >= 7 && slotID <= 9)
			{
				return false;
			}
			else if(slotID >= 4 && slotID <= 6)
			{
				return RecipeType.values()[recipeType].getCopiedOutput(itemstack, false) != null;
			}
		}
		else if(tier == FactoryTier.ADVANCED)
		{
			if(slotID >= 9 && slotID <= 13)
			{
				return false;
			}
			else if(slotID >= 4 && slotID <= 8)
			{
				return RecipeType.values()[recipeType].getCopiedOutput(itemstack, false) != null;
			}
		}
		else if(tier == FactoryTier.ELITE)
		{
			if(slotID >= 11 && slotID <= 17)
			{
				return false;
			}
			else if(slotID >= 4 && slotID <= 10)
			{
				return RecipeType.values()[recipeType].getCopiedOutput(itemstack, false) != null;
			}
		}
		
		if(slotID == 0)
		{
			return itemstack.itemID == Mekanism.SpeedUpgrade.itemID || itemstack.itemID == Mekanism.EnergyUpgrade.itemID;
		}
		else if(slotID == 1)
		{
			return (itemstack.getItem() instanceof IElectricItem && ((IElectricItem)itemstack.getItem()).canProvideEnergy(itemstack)) || 
					(itemstack.getItem() instanceof IItemElectric && ((IItemElectric)itemstack.getItem()).getProvideRequest(itemstack).amperes != 0) || 
					itemstack.itemID == Item.redstone.itemID;
		}
		return true;
	}
	
	@Override
	public void invalidate()
	{
		super.invalidate();
		
		if(worldObj.isRemote)
		{
			Mekanism.proxy.unregisterSound(this);
		}
	}
	
	public int getScaledProgress(int i, int process)
	{
		return progress[process]*i / MekanismUtils.getTicks(speedMultiplier, TICKS_REQUIRED);
	}
	
	/**
	 * Gets the scaled energy level for the GUI.
	 * @param i - multiplier
	 * @return scaled energy
	 */
	public int getScaledEnergyLevel(int i)
	{
		return (int)(electricityStored*i / MekanismUtils.getEnergy(energyMultiplier, MAX_ELECTRICITY));
	}
	
	public int getScaledUpgradeProgress(int i)
	{
		return upgradeTicks*i / UPGRADE_TICKS_REQUIRED;
	}
	
	public int getScaledRecipeProgress(int i)
	{
		return recipeTicks*i / RECIPE_TICKS_REQUIRED;
	}
	
	public boolean canOperate(int inputSlot, int outputSlot)
	{
        if(inventory[inputSlot] == null)
        {
            return false;
        }

        ItemStack itemstack = RecipeType.values()[recipeType].getCopiedOutput(inventory[inputSlot], false);

        if(itemstack == null)
        {
            return false;
        }

        if(inventory[outputSlot] == null)
        {
            return true;
        }

        if(!inventory[outputSlot].isItemEqual(itemstack))
        {
            return false;
        }
        else {
            return inventory[outputSlot].stackSize + itemstack.stackSize <= inventory[outputSlot].getMaxStackSize();
        }
	}
	
	public void operate(int inputSlot, int outputSlot)
	{
        if(!canOperate(inputSlot, outputSlot))
        {
            return;
        }

        ItemStack itemstack = RecipeType.values()[recipeType].getCopiedOutput(inventory[inputSlot], true);

        if(inventory[inputSlot].stackSize <= 0)
        {
            inventory[inputSlot] = null;
        }

        if(inventory[outputSlot] == null)
        {
            inventory[outputSlot] = itemstack;
        }
        else {
            inventory[outputSlot].stackSize += itemstack.stackSize;
        }
	}
	
	@Override
	public int getStartInventorySide(ForgeDirection side) 
	{
		return sideOutputs.get(sideConfig[MekanismUtils.getBaseOrientation(side.ordinal(), facing)]).slotStart;
	}

	@Override
	public int getSizeInventorySide(ForgeDirection side)
	{
		return sideOutputs.get(sideConfig[MekanismUtils.getBaseOrientation(side.ordinal(), facing)]).slotAmount;
	}
	
	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		super.handlePacketData(dataStream);
		speedMultiplier = dataStream.readInt();
		energyMultiplier = dataStream.readInt();
		isActive = dataStream.readBoolean();
		recipeType = dataStream.readInt();
		upgradeTicks = dataStream.readInt();
		recipeTicks = dataStream.readInt();
		
		for(int i = 0; i < tier.processes; i++)
		{
			progress[i] = dataStream.readInt();
		}
		
		MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);
        
        speedMultiplier = nbtTags.getInteger("speedMultiplier");
        energyMultiplier = nbtTags.getInteger("energyMultiplier");
        isActive = nbtTags.getBoolean("isActive");
        recipeType = nbtTags.getInteger("recipeType");
        upgradeTicks = nbtTags.getInteger("upgradeTicks");
        recipeTicks = nbtTags.getInteger("recipeTicks");
        
        for(int i = 0; i < tier.processes; i++)
        {
        	progress[i] = nbtTags.getInteger("progress" + i);
        }
        
        if(nbtTags.hasKey("sideDataStored"))
        {
        	for(int i = 0; i < 6; i++)
        	{
        		sideConfig[i] = nbtTags.getByte("config"+i);
        	}
        }
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setInteger("speedMultiplier", speedMultiplier);
        nbtTags.setInteger("energyMultiplier", energyMultiplier);
        nbtTags.setBoolean("isActive", isActive);
        nbtTags.setInteger("recipeType", recipeType);
        nbtTags.setInteger("upgradeTicks", upgradeTicks);
        nbtTags.setInteger("recipeTicks", recipeTicks);
        
        for(int i = 0; i < tier.processes; i++)
        {
        	nbtTags.setInteger("progress" + i, progress[i]);
        }
        
        nbtTags.setBoolean("sideDataStored", true);
        
        for(int i = 0; i < 6; i++)
        {
        	nbtTags.setByte("config"+i, sideConfig[i]);
        }
    }
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		data.add(speedMultiplier);
		data.add(energyMultiplier);
		data.add(isActive);
		data.add(recipeType);
		data.add(upgradeTicks);
		data.add(recipeTicks);
		data.add(progress);
		return data;
	}

	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, Direction direction) 
	{
		return true;
	}
	
	public int getInputSlot(int operation)
	{
		return operation+4;
	}
	
	public int getOutputSlot(int operation)
	{
		return tier.processes+4+operation;
	}
	
	@Override
	public double transferEnergyToAcceptor(double amount)
	{
    	double rejects = 0;
    	double neededElectricity = MekanismUtils.getEnergy(energyMultiplier, MAX_ELECTRICITY)-electricityStored;
    	
    	if(amount <= neededElectricity)
    	{
    		electricityStored += amount;
    	}
    	else {
    		electricityStored += neededElectricity;
    		rejects = amount-neededElectricity;
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
				return new Object[] {MekanismUtils.getEnergy(energyMultiplier, MAX_ELECTRICITY)};
			case 5:
				return new Object[] {(MekanismUtils.getEnergy(energyMultiplier, MAX_ELECTRICITY)-electricityStored)};
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
	public double getMaxEnergy() 
	{
		return MekanismUtils.getEnergy(energyMultiplier, MAX_ELECTRICITY);
	}

	@Override
	public int demandsEnergy() 
	{
		return (int)((MekanismUtils.getEnergy(energyMultiplier, MAX_ELECTRICITY) - electricityStored)*Mekanism.TO_IC2);
	}
	
	@Override
    public void setActive(boolean active)
    {
    	isActive = active;
    	
    	if(prevActive != active)
    	{
    		PacketHandler.sendTileEntityPacketToClients(this, 0, getNetworkedData(new ArrayList()));
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
    	double neededEnergy = MekanismUtils.getEnergy(energyMultiplier, MAX_ELECTRICITY)-electricityStored;
    	
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
	public int powerRequest(ForgeDirection side) 
	{
		return (int)Math.min(((MekanismUtils.getEnergy(energyMultiplier, MAX_ELECTRICITY)-electricityStored)*Mekanism.TO_BC), 100);
	}
	
	@Override
	public int[] getSizeInventorySide(int side)
	{
		return sideOutputs.get(sideConfig[MekanismUtils.getBaseOrientation(side, facing)]).availableSlots;
	}
	
	@Override
	public ArrayList<SideData> getSideData()
	{
		return sideOutputs;
	}
	
	@Override
	public byte[] getConfiguration()
	{
		return sideConfig;
	}
	
	@Override
	public int getOrientation()
	{
		return facing;
	}
	
	@Override
	public int getEnergyMultiplier(Object... data) 
	{
		return energyMultiplier;
	}

	@Override
	public void setEnergyMultiplier(int multiplier, Object... data) 
	{
		energyMultiplier = multiplier;
	}

	@Override
	public int getSpeedMultiplier(Object... data) 
	{
		return speedMultiplier;
	}

	@Override
	public void setSpeedMultiplier(int multiplier, Object... data) 
	{
		speedMultiplier = multiplier;
	}
	
	@Override
	public boolean supportsUpgrades(Object... data)
	{
		return true;
	}

	@Override
	public String getSoundPath()
	{
		return RecipeType.values()[recipeType].getSound();
	}
}
