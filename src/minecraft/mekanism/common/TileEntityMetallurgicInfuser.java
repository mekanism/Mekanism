package mekanism.common;

import ic2.api.Direction;
import ic2.api.IElectricItem;
import ic2.api.energy.tile.IEnergySink;

import java.util.ArrayList;

import mekanism.api.EnumColor;
import mekanism.api.IConfigurable;
import mekanism.api.IStrictEnergyAcceptor;
import mekanism.api.IUpgradeManagement;
import mekanism.api.InfuseObject;
import mekanism.api.InfusionInput;
import mekanism.api.InfusionOutput;
import mekanism.api.InfusionType;
import mekanism.api.SideData;
import mekanism.client.IHasSound;
import mekanism.common.RecipeHandler.Recipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.item.IItemElectric;

import com.google.common.io.ByteArrayDataInput;

import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IPeripheral;

public class TileEntityMetallurgicInfuser extends TileEntityElectricBlock implements IEnergySink, IPeripheral, IActiveState, IConfigurable, IUpgradeManagement, IHasSound, IStrictEnergyAcceptor
{
	/** This machine's side configuration. */
	public byte[] sideConfig;
	
	/** An arraylist of SideData for this machine. */
	public ArrayList<SideData> sideOutputs = new ArrayList<SideData>();
	
	/** The type of infuse this machine stores. */
	public InfusionType type = InfusionType.NONE;
	
	/** The maxiumum amount of infuse this machine can store. */
	public int MAX_INFUSE = 1000;
	
	/** How much energy this machine consumes per-tick. */
	public double ENERGY_PER_TICK = 10;
	
	/** How many ticks it takes to run an operation. */
	public int TICKS_REQUIRED = 200;
	
	/** This machine's speed multiplier. */
	public int speedMultiplier;
	
	/** This machine's energy multiplier. */
	public int energyMultiplier;
	
	/** How long it takes this machine to install an upgrade. */
	public int UPGRADE_TICKS_REQUIRED = 40;
	
	/** How many upgrade ticks have progressed. */
	public int upgradeTicks;
	
	/** The amount of infuse this machine has stored. */
	public int infuseStored;
	
	/** How many ticks this machine has been operating for. */
	public int operatingTicks;
	
	/** Whether or not this machine is in it's active state. */
	public boolean isActive;
	
	/** This machine's previous active state, used for tick callbacks. */
	public boolean prevActive;
	
	public TileEntityMetallurgicInfuser()
	{
		super("Metallurgic Infuser", 2000);
		
		sideOutputs.add(new SideData(EnumColor.GREY, 0, 0, new int[0]));
		sideOutputs.add(new SideData(EnumColor.ORANGE, 0, 1, new int[] {0}));
		sideOutputs.add(new SideData(EnumColor.PURPLE, 1, 1, new int[] {1}));
		sideOutputs.add(new SideData(EnumColor.DARK_RED, 2, 1, new int[] {2}));
		sideOutputs.add(new SideData(EnumColor.DARK_BLUE, 3, 1, new int[] {3}));
		sideOutputs.add(new SideData(EnumColor.DARK_GREEN, 4, 1, new int[] {4}));
		
		sideConfig = new byte[] {0, 1, 0, 5, 3, 4};
		
		inventory = new ItemStack[5];
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
			ChargeUtils.discharge(4, this);
			
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
			
			if(inventory[1] != null)
			{
				if(MekanismUtils.getInfuseObject(inventory[1]) != null)
				{
					InfuseObject infuse = MekanismUtils.getInfuseObject(inventory[1]);
					
					if(type == InfusionType.NONE || type == infuse.type)
					{
						if(infuseStored+infuse.stored <= MAX_INFUSE)
						{
							infuseStored+=infuse.stored;
							type = infuse.type;
							inventory[1].stackSize--;
							
				            if(inventory[1].stackSize <= 0)
				            {
				                inventory[1] = null;
				            }
						}
					}
				}
			}
			
			if(electricityStored >= ENERGY_PER_TICK)
			{
				if(canOperate() && (operatingTicks+1) < MekanismUtils.getTicks(speedMultiplier, TICKS_REQUIRED))
				{
					operatingTicks++;
					electricityStored -= ENERGY_PER_TICK;
				}
				else if(canOperate() && (operatingTicks+1) >= MekanismUtils.getTicks(speedMultiplier, TICKS_REQUIRED))
				{
					operate();
					
					operatingTicks = 0;
					electricityStored -= ENERGY_PER_TICK;
				}
			}
			
			if(!canOperate())
			{
				operatingTicks = 0;
			}
			
			if(infuseStored <= 0)
			{
				infuseStored = 0;
				type = InfusionType.NONE;
			}
			
			if(canOperate() && electricityStored >= ENERGY_PER_TICK)
			{
				setActive(true);
			}
			else {
				setActive(false);
			}
		}
	}
	
	@Override
	public boolean func_102008_b(int slotID, ItemStack itemstack, int side)
	{
		if(slotID == 4)
		{
			return (itemstack.getItem() instanceof IItemElectric && ((IItemElectric)itemstack.getItem()).getProvideRequest(itemstack).getWatts() == 0) ||
					(itemstack.getItem() instanceof IElectricItem && ((IElectricItem)itemstack.getItem()).canProvideEnergy(itemstack) && 
							(!(itemstack.getItem() instanceof IItemElectric) || 
							((IItemElectric)itemstack.getItem()).getProvideRequest(itemstack).getWatts() == 0));
		}
		else if(slotID == 3)
		{
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean isStackValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 3)
		{
			return false;
		}
		else if(slotID == 1)
		{
			return MekanismUtils.getInfuseObject(itemstack) != null && (type == InfusionType.NONE || type == MekanismUtils.getInfuseObject(itemstack).type);
		}
		else if(slotID == 0)
		{
			return itemstack.itemID == Mekanism.SpeedUpgrade.itemID || itemstack.itemID == Mekanism.EnergyUpgrade.itemID;
		}
		else if(slotID == 2)
		{
	    	if(type != InfusionType.NONE)
	    	{
	    		if(RecipeHandler.getOutput(InfusionInput.getInfusion(type, infuseStored, itemstack), false, Recipe.METALLURGIC_INFUSER.get()) != null)
	    		{
	    			return true;
	    		}
	    	}
	    	else {
	    		for(Object obj : Recipe.METALLURGIC_INFUSER.get().keySet())
	    		{
	    			InfusionInput input = (InfusionInput)obj;
	    			if(input.inputSlot.isItemEqual(itemstack))
	    			{
	    				return true;
	    			}
	    		}
	    	}
		}
		else if(slotID == 4)
		{
			return (itemstack.getItem() instanceof IElectricItem && ((IElectricItem)itemstack.getItem()).canProvideEnergy(itemstack)) || 
					(itemstack.getItem() instanceof IItemElectric && ((IItemElectric)itemstack.getItem()).getProvideRequest(itemstack).amperes != 0) || 
					itemstack.itemID == Item.redstone.itemID;
		}
		return true;
	}
	
	public void operate()
	{
        if(!canOperate())
        {
            return;
        }

        InfusionOutput output = RecipeHandler.getOutput(InfusionInput.getInfusion(type, infuseStored, inventory[2]), true, Recipe.METALLURGIC_INFUSER.get());
        
        infuseStored -= output.getInfuseRequired();

        if(inventory[2].stackSize <= 0)
        {
            inventory[2] = null;
        }

        if(inventory[3] == null)
        {
            inventory[3] = output.resource.copy();
        }
        else {
            inventory[3].stackSize += output.resource.stackSize;
        }
	}
	
	public boolean canOperate()
	{
        if(inventory[2] == null)
        {
            return false;
        }

        InfusionOutput output = RecipeHandler.getOutput(InfusionInput.getInfusion(type, infuseStored, inventory[2]), false, Recipe.METALLURGIC_INFUSER.get());

        if(output == null)
        {
            return false;
        }
        
        if(infuseStored-output.getInfuseRequired() < 0)
        {
        	return false;
        }

        if(inventory[3] == null)
        {
            return true;
        }

        if(!inventory[3].isItemEqual(output.resource))
        {
            return false;
        }
        else {
            return inventory[3].stackSize + output.resource.stackSize <= inventory[3].getMaxStackSize();
        }
	}
	
	public int getScaledInfuseLevel(int i)
	{
		return infuseStored*i / MAX_INFUSE;
	}
	
	public int getScaledEnergyLevel(int i)
	{
		return (int)(electricityStored*i / MekanismUtils.getEnergy(energyMultiplier, MAX_ELECTRICITY));
	}
	
	public int getScaledProgress(int i)
	{
		return operatingTicks*i / MekanismUtils.getTicks(speedMultiplier, TICKS_REQUIRED);
	}
	
	public int getScaledUpgradeProgress(int i)
	{
		return upgradeTicks*i / UPGRADE_TICKS_REQUIRED;
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
	
    @Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
    	super.readFromNBT(nbtTags);
    	
    	speedMultiplier = nbtTags.getInteger("speedMultiplier");
    	energyMultiplier = nbtTags.getInteger("energyMultiplier");
    	isActive = nbtTags.getBoolean("isActive");
    	operatingTicks = nbtTags.getInteger("operatingTicks");
    	infuseStored = nbtTags.getInteger("infuseStored");
    	type = InfusionType.getFromName(nbtTags.getString("type"));
    	
        if(nbtTags.hasKey("sideDataStored"))
        {
        	for(int i = 0; i < 6; i++)
        	{
        		sideConfig[i] = nbtTags.getByte("config"+i);
        	}
        }
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
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setInteger("speedMultiplier", speedMultiplier);
        nbtTags.setInteger("energyMultiplier", energyMultiplier);
        nbtTags.setBoolean("isActive", isActive);
        nbtTags.setInteger("operatingTicks", operatingTicks);
        nbtTags.setInteger("infuseStored", infuseStored);
        nbtTags.setString("type", type.name);
        
        nbtTags.setBoolean("sideDataStored", true);
        
        for(int i = 0; i < 6; i++)
        {
        	nbtTags.setByte("config"+i, sideConfig[i]);
        }
    }
	
	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		if(!worldObj.isRemote)
		{
			infuseStored = dataStream.readInt();
			return;
		}
		
		super.handlePacketData(dataStream);
		speedMultiplier = dataStream.readInt();
		energyMultiplier = dataStream.readInt();
		isActive = dataStream.readBoolean();
		operatingTicks = dataStream.readInt();
		infuseStored = dataStream.readInt();
		type = InfusionType.getFromName(dataStream.readUTF());
		MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		data.add(speedMultiplier);
		data.add(energyMultiplier);
		data.add(isActive);
		data.add(operatingTicks);
		data.add(infuseStored);
		data.add(type.name);
		return data;
	}

	@Override
	public String getType()
	{
		return getInvName();
	}

	@Override
	public String[] getMethodNames() 
	{
		return new String[] {"getStored", "getProgress", "facing", "canOperate", "getMaxEnergy", "getEnergyNeeded", "getInfuse", "getInfuseNeeded"};
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, int method, Object[] arguments) throws Exception 
	{
		switch(method)
		{
			case 0:
				return new Object[] {electricityStored};
			case 1:
				return new Object[] {operatingTicks};
			case 2:
				return new Object[] {facing};
			case 3:
				return new Object[] {canOperate()};
			case 4:
				return new Object[] {MekanismUtils.getEnergy(energyMultiplier, MAX_ELECTRICITY)};
			case 5:
				return new Object[] {(MekanismUtils.getEnergy(energyMultiplier, MAX_ELECTRICITY)-electricityStored)};
			case 6:
				return new Object[] {infuseStored};
			case 7:
				return new Object[] {(MAX_INFUSE-infuseStored)};
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
	public int[] getSizeInventorySide(int side)
	{
		return sideOutputs.get(sideConfig[MekanismUtils.getBaseOrientation(side, facing)]).availableSlots;
	}

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
	public boolean acceptsEnergyFrom(TileEntity emitter, Direction direction) 
	{
		return true;
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
		return "MetallurgicInfuser.ogg";
	}
}
