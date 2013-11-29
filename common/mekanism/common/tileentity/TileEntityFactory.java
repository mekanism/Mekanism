package mekanism.common.tileentity;

import java.util.ArrayList;

import mekanism.api.EnumColor;
import mekanism.api.IConfigurable;
import mekanism.api.IEjector;
import mekanism.api.Object3D;
import mekanism.api.SideData;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasUtils;
import mekanism.api.gas.IGasAcceptor;
import mekanism.api.gas.IGasItem;
import mekanism.api.gas.IGasStorage;
import mekanism.api.gas.ITubeConnection;
import mekanism.client.sound.IHasSound;
import mekanism.common.IActiveState;
import mekanism.common.IFactory.RecipeType;
import mekanism.common.IRedstoneControl;
import mekanism.common.IUpgradeTile;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.Tier.FactoryTier;
import mekanism.common.TileComponentEjector;
import mekanism.common.TileComponentUpgrade;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;

import com.google.common.io.ByteArrayDataInput;

import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;
import dan200.computer.api.IPeripheral;

public class TileEntityFactory extends TileEntityElectricBlock implements IPeripheral, IActiveState, IConfigurable, IUpgradeTile, IHasSound, IRedstoneControl, IGasAcceptor, IGasStorage, ITubeConnection
{	
	/** This Factory's tier. */
	public FactoryTier tier;
	
	/** This machine's side configuration. */
	public byte[] sideConfig = new byte[] {5, 4, 0, 3, 2, 1};
	
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
	
	/** This machine's previous amount of energy. */
	public double prevEnergy;
	
	public int secondaryEnergyStored;
	
	/** This machine's current RedstoneControl type. */
	public RedstoneControl controlType = RedstoneControl.DISABLED;
	
	public TileComponentUpgrade upgradeComponent = new TileComponentUpgrade(this, 0);
	public TileComponentEjector ejectorComponent;
	
	public TileEntityFactory()
	{
		this(FactoryTier.BASIC, MachineType.BASIC_FACTORY);
		
		sideOutputs.add(new SideData(EnumColor.GREY, new int[0]));
		sideOutputs.add(new SideData(EnumColor.ORANGE, new int[] {0}));
		sideOutputs.add(new SideData(EnumColor.DARK_GREEN, new int[] {1}));
		sideOutputs.add(new SideData(EnumColor.PURPLE, new int[] {4}));
		sideOutputs.add(new SideData(EnumColor.DARK_RED, new int[] {5, 6, 7}));
		sideOutputs.add(new SideData(EnumColor.DARK_BLUE, new int[] {8, 9, 10}));
		
		ejectorComponent = new TileComponentEjector(this, sideOutputs.get(5));
	}
	
	public TileEntityFactory(FactoryTier type, MachineType machine)
	{
		super(type.name + " Factory", machine.baseEnergy);
		
		tier = type;
		inventory = new ItemStack[5+type.processes*2];
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
			
			if(updateDelay > 0)
			{
				updateDelay--;
				
				if(updateDelay == 0 && clientActive != isActive)
				{
					isActive = clientActive;
					MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
				}
			}
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
			
			handleSecondaryFuel();
			
			if(inventory[2] != null && inventory[3] == null)
			{
				RecipeType toSet = null;
				
				for(RecipeType type : RecipeType.values())
				{
					if(inventory[2].isItemEqual(type.getStack()))
					{
						toSet = type;
						break;
					}
				}
				
				if(toSet != null && recipeType != toSet.ordinal())
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
						
						recipeType = toSet.ordinal();
						setSecondaryEnergy(0);
						
						MekanismUtils.saveChunk(this);
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
				if(MekanismUtils.canFunction(this) && canOperate(getInputSlot(process), getOutputSlot(process)) && getEnergy() >= MekanismUtils.getEnergyPerTick(getSpeedMultiplier(), getEnergyMultiplier(), ENERGY_PER_TICK) && secondaryEnergyStored >= getSecondaryEnergyPerTick())
				{
					if((progress[process]+1) < MekanismUtils.getTicks(getSpeedMultiplier(), TICKS_REQUIRED))
					{
						progress[process]++;
						secondaryEnergyStored -= getSecondaryEnergyPerTick();
						electricityStored -= MekanismUtils.getEnergyPerTick(getSpeedMultiplier(), getEnergyMultiplier(), ENERGY_PER_TICK);
					}
					else if((progress[process]+1) >= MekanismUtils.getTicks(getSpeedMultiplier(), TICKS_REQUIRED))
					{
						operate(getInputSlot(process), getOutputSlot(process));
						
						progress[process] = 0;
						secondaryEnergyStored -= getSecondaryEnergyPerTick();
						electricityStored -= MekanismUtils.getEnergyPerTick(getSpeedMultiplier(), getEnergyMultiplier(), ENERGY_PER_TICK);
					}
				}
				
				if(!canOperate(getInputSlot(process), getOutputSlot(process)))
				{
					progress[process] = 0;
				}
			}
			
			boolean hasOperation = false;
			
			for(int i = 0; i < tier.processes; i++)
			{
				if(canOperate(getInputSlot(i), getOutputSlot(i)))
				{
					hasOperation = true;
					break;
				}
			}
			
			if(MekanismUtils.canFunction(this) && hasOperation && getEnergy() >= MekanismUtils.getEnergyPerTick(getSpeedMultiplier(), getEnergyMultiplier(), ENERGY_PER_TICK) && secondaryEnergyStored >= getSecondaryEnergyPerTick())
			{
				setActive(true);
			}
			else {
				if(prevEnergy >= getEnergy())
				{
					setActive(false);
				}
			}
			
			prevEnergy = getEnergy();
		}
	}
	
	public int getSecondaryEnergyPerTick()
	{
		return RecipeType.values()[recipeType].getSecondaryEnergyPerTick();
	}
	
	public int getMaxSecondaryEnergy()
	{
		return RecipeType.values()[recipeType].getMaxSecondaryEnergy()*tier.processes;
	}
	
	public void handleSecondaryFuel()
    {
		if(inventory[4] != null && RecipeType.values()[recipeType].usesFuel() && secondaryEnergyStored < getMaxSecondaryEnergy())
		{
			if(recipeType == RecipeType.PURIFYING.ordinal())
			{
				if(inventory[4].getItem() instanceof IGasItem)
				{
					GasStack removed = GasUtils.removeGas(inventory[4], GasRegistry.getGas("oxygen"), getMaxSecondaryEnergy()-secondaryEnergyStored);
					setSecondaryEnergy(secondaryEnergyStored + (removed != null ? removed.amount : 0));
					
					return;
				}
			}
			
			int fuelTicks = RecipeType.values()[recipeType].getFuelTicks(inventory[4]);
			int energyNeeded = getMaxSecondaryEnergy() - secondaryEnergyStored;
			
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
				
				inventory[4].stackSize--;
				
				if(inventory[4].stackSize == 0)
				{
					inventory[4] = null;
				}
			}
		}
    }
	
	public ItemStack getMachineStack()
	{
		return RecipeType.values()[recipeType].getStack();
	}
	
	@Override
	public boolean canExtractItem(int slotID, ItemStack itemstack, int side)
	{
		if(slotID == 1)
		{
			return ChargeUtils.canBeOutputted(itemstack, false);
		}
		else if(tier == FactoryTier.BASIC && slotID >= 8 && slotID <= 10)
		{
			return true;
		}
		else if(tier == FactoryTier.ADVANCED && slotID >= 10 && slotID <= 14)
		{
			return true;
		}
		else if(tier == FactoryTier.ELITE && slotID >= 12 && slotID <= 18)
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
			if(slotID >= 8 && slotID <= 10)
			{
				return false;
			}
			else if(slotID >= 5 && slotID <= 7)
			{
				return RecipeType.values()[recipeType].getCopiedOutput(itemstack, false) != null;
			}
		}
		else if(tier == FactoryTier.ADVANCED)
		{
			if(slotID >= 10 && slotID <= 14)
			{
				return false;
			}
			else if(slotID >= 5 && slotID <= 9)
			{
				return RecipeType.values()[recipeType].getCopiedOutput(itemstack, false) != null;
			}
		}
		else if(tier == FactoryTier.ELITE)
		{
			if(slotID >= 12 && slotID <= 18)
			{
				return false;
			}
			else if(slotID >= 5 && slotID <= 11)
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
		else if(slotID == 4)
		{
			return RecipeType.values()[recipeType].getFuelTicks(itemstack) > 0;
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
	
	public int getScaledSecondaryEnergy(int i)
	{
		return secondaryEnergyStored*i / getMaxSecondaryEnergy();
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
        
        onInventoryChanged();
        ejectorComponent.onOutput();
	}
	
	public void setSecondaryEnergy(int energy)
	{
		secondaryEnergyStored = Math.max(Math.min(energy, getMaxSecondaryEnergy()), 0);
	}
	
	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		super.handlePacketData(dataStream);
		
		clientActive = dataStream.readBoolean();
		recipeType = dataStream.readInt();
		recipeTicks = dataStream.readInt();
		controlType = RedstoneControl.values()[dataStream.readInt()];
		secondaryEnergyStored = dataStream.readInt();
		
		for(int i = 0; i < tier.processes; i++)
		{
			progress[i] = dataStream.readInt();
		}
		
		for(int i = 0; i < 6; i++)
		{
			sideConfig[i] = dataStream.readByte();
		}
		
		if(updateDelay == 0 && clientActive != isActive)
		{
			updateDelay = Mekanism.UPDATE_DELAY;
			isActive = clientActive;
			MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
		}
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);
        
        clientActive = isActive = nbtTags.getBoolean("isActive");
        recipeType = Math.min(5, nbtTags.getInteger("recipeType"));
        recipeTicks = nbtTags.getInteger("recipeTicks");
        controlType = RedstoneControl.values()[nbtTags.getInteger("controlType")];
        secondaryEnergyStored = nbtTags.getInteger("secondaryEnergyStored");
        
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
        
        nbtTags.setBoolean("isActive", isActive);
        nbtTags.setInteger("recipeType", recipeType);
        nbtTags.setInteger("recipeTicks", recipeTicks);
        nbtTags.setInteger("controlType", controlType.ordinal());
        nbtTags.setInteger("secondaryEnergyStored", secondaryEnergyStored);
        
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
		
		data.add(isActive);
		data.add(recipeType);
		data.add(recipeTicks);
		data.add(controlType.ordinal());
		data.add(secondaryEnergyStored);
		data.add(progress);
		data.add(sideConfig);
		
		return data;
	}
	
	public int getInputSlot(int operation)
	{
		return operation+5;
	}
	
	public int getOutputSlot(int operation)
	{
		return tier.processes+5+operation;
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
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws Exception 
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
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return sideOutputs.get(sideConfig[MekanismUtils.getBaseOrientation(side, facing)]).availableSlots;
	}
	
	@Override
	public boolean canSetFacing(int side)
	{
		return side != 0 && side != 1;
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
		MekanismUtils.saveChunk(this);
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
		MekanismUtils.saveChunk(this);
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
	public boolean renderUpdate() 
	{
		return true;
	}

	@Override
	public boolean lightUpdate()
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
		MekanismUtils.saveChunk(this);
	}
	
	@Override
	public TileComponentUpgrade getComponent()
	{
		return upgradeComponent;
	}
	
	@Override
	public IEjector getEjector()
	{
		return ejectorComponent;
	}
	
	@Override
	public GasStack getGas(Object... data) 
	{
		if(secondaryEnergyStored == 0)
		{
			return null;
		}
		
		return new GasStack(GasRegistry.getGas("oxygen"), secondaryEnergyStored);
	}

	@Override
	public void setGas(GasStack stack, Object... data) 
	{
		if(stack == null)
		{
			setSecondaryEnergy(0);
		}
		else if(stack.getGas() == GasRegistry.getGas("oxygen"))
		{
			setSecondaryEnergy(stack.amount);
		}
		
		MekanismUtils.saveChunk(this);
	}
	
	@Override
	public int getMaxGas(Object... data)
	{
		return getMaxSecondaryEnergy();
	}

	@Override
	public int receiveGas(GasStack stack) 
	{
		if(stack.getGas() == GasRegistry.getGas("oxygen"))
		{
			int stored = getGas() != null ? getGas().amount : 0;
			int toUse = Math.min(getMaxGas()-stored, stack.amount);
			
			setGas(new GasStack(stack.getGas(), stored + toUse));
	    	
	    	return toUse;
		}
		
		return 0;
	}

	@Override
	public boolean canReceiveGas(ForgeDirection side, Gas type)
	{
		return type == GasRegistry.getGas("oxygen");
	}

	@Override
	public boolean canTubeConnect(ForgeDirection side)
	{
		return recipeType == RecipeType.PURIFYING.ordinal();
	}
}