package mekanism.common.tileentity;

import ic2.api.energy.tile.IEnergySink;

import java.util.ArrayList;

import mekanism.api.EnumColor;
import mekanism.api.IConfigurable;
import mekanism.api.IUpgradeManagement;
import mekanism.api.Object3D;
import mekanism.api.SideData;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.api.infuse.InfuseObject;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.api.infuse.InfuseType;
import mekanism.api.infuse.InfusionInput;
import mekanism.api.infuse.InfusionOutput;
import mekanism.client.sound.IHasSound;
import mekanism.common.IActiveState;
import mekanism.common.IRedstoneControl;
import mekanism.common.IUpgradeTile;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.RecipeHandler;
import mekanism.common.TileComponentUpgrade;
import mekanism.common.IRedstoneControl.RedstoneControl;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.RecipeHandler.Recipe;
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

public class TileEntityMetallurgicInfuser extends TileEntityElectricBlock implements IEnergySink, IPeripheral, IActiveState, IConfigurable, IUpgradeTile, IHasSound, IStrictEnergyAcceptor, IRedstoneControl
{
	/** This machine's side configuration. */
	public byte[] sideConfig = new byte[] {2, 1, 0, 5, 3, 4};
	
	/** An arraylist of SideData for this machine. */
	public ArrayList<SideData> sideOutputs = new ArrayList<SideData>();
	
	/** The type of infuse this machine stores. */
	public InfuseType type = null;
	
	/** The maxiumum amount of infuse this machine can store. */
	public int MAX_INFUSE = 1000;
	
	/** How much energy this machine consumes per-tick. */
	public double ENERGY_PER_TICK = Mekanism.metallurgicInfuserUsage;
	
	/** How many ticks it takes to run an operation. */
	public int TICKS_REQUIRED = 200;
	
	/** The amount of infuse this machine has stored. */
	public int infuseStored;
	
	/** How many ticks this machine has been operating for. */
	public int operatingTicks;
	
	/** Whether or not this machine is in it's active state. */
	public boolean isActive;
	
	/** The client's current active state. */
	public boolean clientActive;
	
	/** How many ticks must pass until this block's active state can sync with the client. */
	public int updateDelay;
	
	/** This machine's current RedstoneControl type. */
	public RedstoneControl controlType = RedstoneControl.DISABLED;
	
	public TileComponentUpgrade upgradeComponent = new TileComponentUpgrade(this, 0);
	
	public TileEntityMetallurgicInfuser()
	{
		super("Metallurgic Infuser", MachineType.METALLURGIC_INFUSER.baseEnergy);
		
		sideOutputs.add(new SideData(EnumColor.GREY, new int[0]));
		sideOutputs.add(new SideData(EnumColor.ORANGE, new int[] {0}));
		sideOutputs.add(new SideData(EnumColor.PURPLE, new int[] {1}));
		sideOutputs.add(new SideData(EnumColor.DARK_RED, new int[] {2}));
		sideOutputs.add(new SideData(EnumColor.DARK_BLUE, new int[] {3}));
		sideOutputs.add(new SideData(EnumColor.DARK_GREEN, new int[] {4}));
		
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
			if(updateDelay > 0)
			{
				updateDelay--;
					
				if(updateDelay == 0 && clientActive != isActive)
				{
					PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity().setParams(Object3D.get(this), getNetworkedData(new ArrayList())));
				}
			}
			
			ChargeUtils.discharge(4, this);
			
			if(inventory[1] != null)
			{
				if(InfuseRegistry.getObject(inventory[1]) != null)
				{
					InfuseObject infuse = InfuseRegistry.getObject(inventory[1]);
					
					if(type == null || type == infuse.type)
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
			
			if(canOperate() && MekanismUtils.canFunction(this) && getEnergy() >= MekanismUtils.getEnergyPerTick(getSpeedMultiplier(), getEnergyMultiplier(), ENERGY_PER_TICK))
			{
				setActive(true);
				
				if((operatingTicks+1) < MekanismUtils.getTicks(getSpeedMultiplier(), TICKS_REQUIRED))
				{
					operatingTicks++;
					electricityStored -= MekanismUtils.getEnergyPerTick(getSpeedMultiplier(), getEnergyMultiplier(), ENERGY_PER_TICK);
				}
				else if((operatingTicks+1) >= MekanismUtils.getTicks(getSpeedMultiplier(), TICKS_REQUIRED))
				{
					operate();
					
					operatingTicks = 0;
					electricityStored -= MekanismUtils.getEnergyPerTick(getSpeedMultiplier(), getEnergyMultiplier(), ENERGY_PER_TICK);
				}
			}
			else {
				setActive(false);
			}
			
			if(!canOperate())
			{
				operatingTicks = 0;
			}
			
			if(infuseStored <= 0)
			{
				infuseStored = 0;
				type = null;
			}
		}
	}
	
	@Override
	public boolean canExtractItem(int slotID, ItemStack itemstack, int side)
	{
		if(slotID == 4)
		{
			return ChargeUtils.canBeOutputted(itemstack, false);
		}
		else if(slotID == 3)
		{
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 3)
		{
			return false;
		}
		else if(slotID == 1)
		{
			return InfuseRegistry.getObject(itemstack) != null && (type == null || type == InfuseRegistry.getObject(itemstack).type);
		}
		else if(slotID == 0)
		{
			return itemstack.itemID == Mekanism.SpeedUpgrade.itemID || itemstack.itemID == Mekanism.EnergyUpgrade.itemID;
		}
		else if(slotID == 2)
		{
	    	if(type != null)
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
	    			if(input.inputStack.isItemEqual(itemstack))
	    			{
	    				return true;
	    			}
	    		}
	    	}
		}
		else if(slotID == 4)
		{
			return ChargeUtils.canBeDischarged(itemstack);
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
	
	public int getScaledProgress(int i)
	{
		return operatingTicks*i / MekanismUtils.getTicks(getSpeedMultiplier(), TICKS_REQUIRED);
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
    	
    	upgradeComponent.read(nbtTags);
    	
    	clientActive = isActive = nbtTags.getBoolean("isActive");
    	operatingTicks = nbtTags.getInteger("operatingTicks");
    	infuseStored = nbtTags.getInteger("infuseStored");
    	controlType = RedstoneControl.values()[nbtTags.getInteger("controlType")];
    	type = InfuseRegistry.get(nbtTags.getString("type"));
    	
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
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        upgradeComponent.write(nbtTags);
        
        nbtTags.setBoolean("isActive", isActive);
        nbtTags.setInteger("operatingTicks", operatingTicks);
        nbtTags.setInteger("infuseStored", infuseStored);
        nbtTags.setInteger("controlType", controlType.ordinal());
        
        if(type != null)
        {
        	nbtTags.setString("type", type.name);
        }
        else {
        	nbtTags.setString("type", "null");
        }
        
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
		
		upgradeComponent.read(dataStream);
		
		isActive = dataStream.readBoolean();
		operatingTicks = dataStream.readInt();
		infuseStored = dataStream.readInt();
		controlType = RedstoneControl.values()[dataStream.readInt()];
		type = InfuseRegistry.get(dataStream.readUTF());
		
		for(int i = 0; i < 6; i++)
		{
			sideConfig[i] = dataStream.readByte();
		}
		
		MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		upgradeComponent.write(data);
		
		data.add(isActive);
		data.add(operatingTicks);
		data.add(infuseStored);
		data.add(controlType.ordinal());
		
		if(type != null)
		{
			data.add(type.name);
		}
		else {
			data.add("null");
		}
		
		data.add(sideConfig);
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
				return new Object[] {getEnergy()};
			case 1:
				return new Object[] {operatingTicks};
			case 2:
				return new Object[] {facing};
			case 3:
				return new Object[] {canOperate()};
			case 4:
				return new Object[] {getMaxEnergy()};
			case 5:
				return new Object[] {getMaxEnergy()-getEnergy()};
			case 6:
				return new Object[] {infuseStored};
			case 7:
				return new Object[] {MAX_INFUSE-infuseStored};
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
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return sideOutputs.get(sideConfig[MekanismUtils.getBaseOrientation(side, facing)]).availableSlots;
	}

	@Override
	public double getMaxEnergy() 
	{
		return MekanismUtils.getEnergy(getEnergyMultiplier(), MAX_ELECTRICITY);
	}

	@Override
	public double demandedEnergyUnits() 
	{
		return (getMaxEnergy() - getEnergy())*Mekanism.TO_IC2;
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
	public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction) 
	{
		return true;
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
    	
    	if(givenEnergy < neededEnergy)
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
		return "MetallurgicInfuser.ogg";
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