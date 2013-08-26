package mekanism.common.tileentity;

import ic2.api.energy.tile.IEnergySink;

import java.util.ArrayList;

import mekanism.api.EnumColor;
import mekanism.api.IConfigurable;
import mekanism.api.IUpgradeManagement;
import mekanism.api.Object3D;
import mekanism.api.SideData;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.client.sound.IHasSound;
import mekanism.common.IActiveState;
import mekanism.common.IFactory;
import mekanism.common.IRedstoneControl;
import mekanism.common.IUpgradeTile;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.Tier;
import mekanism.common.TileComponentUpgrade;
import mekanism.common.IFactory.RecipeType;
import mekanism.common.IRedstoneControl.RedstoneControl;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.Tier.FactoryTier;
import mekanism.common.block.BlockMachine;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

import com.google.common.io.ByteArrayDataInput;

import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IPeripheral;

public class TileEntityFactory extends TileEntityElectricBlock implements IEnergySink, IPeripheral, IActiveState, IConfigurable, IUpgradeTile, IHasSound, IStrictEnergyAcceptor, IRedstoneControl
{	
	/** This Factory's tier. */
	public FactoryTier tier;
	
	/** This machine's side configuration. */
	public byte[] sideConfig = new byte[] {4, 3, 0, 0, 2, 1};
	
	/** An arraylist of SideData for this machine. */
	public ArrayList<SideData> sideOutputs = new ArrayList<SideData>();
	
	/** An int[] used to track all current operations' progress. */
	public int[] progress;
	
	/** How many ticks it takes, by default, to run an operation. */
	public int TICKS_REQUIRED = 200;
	
	/** How much energy each operation consumes per tick. */
	public double ENERGY_PER_TICK = Mekanism.factoryUsage;
	
	/** How long it takes this factory to switch recipe types. */
	public int RECIPE_TICKS_REQUIRED = 40;
	
	/** How many recipe ticks have progressed. */
	public int recipeTicks;
	
	/** The client's current active state. */
	public boolean clientActive;
	
	/** This machine's active state. */
	public boolean isActive;
	
	/** How many ticks must pass until this block's active state can sync with the client. */
	public int updateDelay;
	
	/** This machine's recipe type. */
	public int recipeType;
	
	/** This machine's current RedstoneControl type. */
	public RedstoneControl controlType = RedstoneControl.DISABLED;
	
	public TileComponentUpgrade upgradeComponent = new TileComponentUpgrade(this, 0);
	
	public TileEntityFactory()
	{
		this(FactoryTier.BASIC, MachineType.BASIC_FACTORY);
		
		sideOutputs.add(new SideData(EnumColor.GREY, new int[0]));
		sideOutputs.add(new SideData(EnumColor.ORANGE, new int[] {0}));
		sideOutputs.add(new SideData(EnumColor.DARK_GREEN, new int[] {1}));
		sideOutputs.add(new SideData(EnumColor.DARK_RED, new int[] {4, 5, 6}));
		sideOutputs.add(new SideData(EnumColor.DARK_BLUE, new int[] {7, 8, 9}));
	}
	
	public TileEntityFactory(FactoryTier type, MachineType machine)
	{
		super(type.name + " Factory", machine.baseEnergy);
		
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
			if(updateDelay > 0)
			{
				updateDelay--;
					
				if(updateDelay == 0 && clientActive != isActive)
				{
					PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity().setParams(Object3D.get(this), getNetworkedData(new ArrayList())));
				}
			}
			
			ChargeUtils.discharge(1, this);
			
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
				if(MekanismUtils.canFunction(this) && canOperate(getInputSlot(process), getOutputSlot(process)) && electricityStored >= MekanismUtils.getEnergyPerTick(getSpeedMultiplier(), getEnergyMultiplier(), ENERGY_PER_TICK))
				{
					if((progress[process]+1) < MekanismUtils.getTicks(getSpeedMultiplier(), TICKS_REQUIRED))
					{
						progress[process]++;
						electricityStored -= MekanismUtils.getEnergyPerTick(getSpeedMultiplier(), getEnergyMultiplier(), ENERGY_PER_TICK);
					}
					else if((progress[process]+1) >= MekanismUtils.getTicks(getSpeedMultiplier(), TICKS_REQUIRED))
					{
						operate(getInputSlot(process), getOutputSlot(process));
						
						progress[process] = 0;
						electricityStored -= MekanismUtils.getEnergyPerTick(getSpeedMultiplier(), getEnergyMultiplier(), ENERGY_PER_TICK);
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
				
				if(MekanismUtils.canFunction(this) && hasOperation && electricityStored >= MekanismUtils.getEnergyPerTick(getSpeedMultiplier(), getEnergyMultiplier(), ENERGY_PER_TICK))
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
	public boolean canExtractItem(int slotID, ItemStack itemstack, int side)
	{
		if(slotID == 1)
		{
			return ChargeUtils.canBeOutputted(itemstack, false);
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
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
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
			return ChargeUtils.canBeDischarged(itemstack);
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
		return progress[process]*i / MekanismUtils.getTicks(getSpeedMultiplier(), TICKS_REQUIRED);
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
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		super.handlePacketData(dataStream);
		
		upgradeComponent.read(dataStream);
		
		isActive = dataStream.readBoolean();
		recipeType = dataStream.readInt();
		recipeTicks = dataStream.readInt();
		controlType = RedstoneControl.values()[dataStream.readInt()];
		
		for(int i = 0; i < tier.processes; i++)
		{
			progress[i] = dataStream.readInt();
		}
		
		for(int i = 0; i < 6; i++)
		{
			sideConfig[i] = dataStream.readByte();
		}
		
		MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);
        
        upgradeComponent.read(nbtTags);
        
        clientActive = isActive = nbtTags.getBoolean("isActive");
        recipeType = nbtTags.getInteger("recipeType");
        recipeTicks = nbtTags.getInteger("recipeTicks");
        controlType = RedstoneControl.values()[nbtTags.getInteger("controlType")];
        
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
        
        upgradeComponent.write(nbtTags);
        
        nbtTags.setBoolean("isActive", isActive);
        nbtTags.setInteger("recipeType", recipeType);
        nbtTags.setInteger("recipeTicks", recipeTicks);
        nbtTags.setInteger("controlType", controlType.ordinal());
        
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
		
		upgradeComponent.write(data);
		
		data.add(isActive);
		data.add(recipeType);
		data.add(recipeTicks);
		data.add(controlType.ordinal());
		data.add(progress);
		data.add(sideConfig);
		
		return data;
	}

	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction) 
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
    	double neededElectricity = getMaxEnergy()-getEnergy();
    	
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
				return new Object[] {getMaxEnergy()};
			case 5:
				return new Object[] {getMaxEnergy()-getEnergy()};
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
		return MekanismUtils.getEnergy(getEnergyMultiplier(), MAX_ELECTRICITY);
	}

	@Override
	public double demandedEnergyUnits() 
	{
		return (getMaxEnergy()-getEnergy())*Mekanism.TO_IC2;
	}
	
	@Override
    public void setActive(boolean active)
    {
    	isActive = active;
    	
    	if(clientActive != active && updateDelay == 0)
    	{
    		PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity().setParams(Object3D.get(this), getNetworkedData(new ArrayList())));
    		
    		updateDelay = 10;
    		clientActive = active;
    	}
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
    public double injectEnergyUnits(ForgeDirection direction, double i)
    {
		double givenEnergy = i*Mekanism.FROM_IC2;
    	double rejects = 0;
    	double neededEnergy = getMaxEnergy()-getEnergy();
    	
    	if(givenEnergy <= neededEnergy)
    	{
    		electricityStored += givenEnergy;
    	}
    	else if(givenEnergy > neededEnergy)
    	{
    		electricityStored += neededEnergy;
    		rejects = givenEnergy-neededEnergy;
    	}
    	
    	return rejects*Mekanism.TO_IC2;
    }
	
	@Override
	public int[] getAccessibleSlotsFromSide(int side)
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
		return upgradeComponent.energyMultiplier;
	}

	@Override
	public void setEnergyMultiplier(int multiplier, Object... data) 
	{
		upgradeComponent.energyMultiplier = multiplier;
	}

	@Override
	public int getSpeedMultiplier(Object... data) 
	{
		return upgradeComponent.speedMultiplier;
	}

	@Override
	public void setSpeedMultiplier(int multiplier, Object... data) 
	{
		upgradeComponent.speedMultiplier = multiplier;
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
	
	@Override
	public float getVolumeMultiplier()
	{
		return 1;
	}
	
	@Override
	public boolean hasVisual()
	{
		return true;
	}
	
	@Override
	public RedstoneControl getControlType() 
	{
		return controlType;
	}

	@Override
	public void setControlType(RedstoneControl type) 
	{
		controlType = type;
	}
	
	@Override
	public TileComponentUpgrade getComponent()
	{
		return upgradeComponent;
	}
}