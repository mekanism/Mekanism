package mekanism.common;

import ic2.api.Direction;
import ic2.api.ElectricItem;
import ic2.api.IElectricItem;
import ic2.api.energy.tile.IEnergySink;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import mekanism.api.IActiveState;
import mekanism.api.IConfigurable;
import mekanism.api.IMachineUpgrade;
import mekanism.api.InfusionInput;
import mekanism.api.InfusionOutput;
import mekanism.api.InfusionType;
import mekanism.api.SideData;
import mekanism.client.Sound;
import mekanism.common.RecipeHandler.Recipe;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;
import universalelectricity.core.electricity.ElectricityConnections;
import universalelectricity.core.implement.IConductor;
import universalelectricity.core.implement.IItemElectric;
import universalelectricity.core.implement.IJouleStorage;
import universalelectricity.core.implement.IVoltage;
import universalelectricity.core.vector.Vector3;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IPeripheral;

public class TileEntityMetallurgicInfuser extends TileEntityElectricBlock implements IEnergySink, IJouleStorage, IVoltage, IPeripheral, IActiveState, IConfigurable
{
	/** The Sound instance for this machine. */
	@SideOnly(Side.CLIENT)
	public Sound audio;
	
	public byte[] sideConfig;
	
	public ArrayList<SideData> sideOutputs = new ArrayList<SideData>();
	
	/** The type of infuse this machine stores. */
	public InfusionType type = InfusionType.NONE;
	
	/** The maxiumum amount of infuse this machine can store. */
	public int MAX_INFUSE = 1000;
	
	/** How much energy this machine consumes per-tick. */
	public double ENERGY_PER_TICK = 16;
	
	/** How many ticks it takes to run an operation. */
	public int TICKS_REQUIRED = 200;
	
	/** The current cap of electricity this machine can hold. */
	public double currentMaxElectricity;
	
	/** The current amount of ticks it takes this machine to run an operation. */
	public int currentTicksRequired;
	
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
		super("Metallurgic Infuser", 10000);
		
		sideOutputs.add(new SideData(EnumColor.GREY, 0, 0));
		sideOutputs.add(new SideData(EnumColor.ORANGE, 0, 1));
		sideOutputs.add(new SideData(EnumColor.PURPLE, 1, 1));
		sideOutputs.add(new SideData(EnumColor.DARK_RED, 2, 1));
		sideOutputs.add(new SideData(EnumColor.DARK_BLUE, 3, 1));
		sideOutputs.add(new SideData(EnumColor.DARK_GREEN, 4, 1));
		
		sideConfig = new byte[] {0, 1, 0, 5, 3, 4};
		
		inventory = new ItemStack[5];
		
		currentTicksRequired = TICKS_REQUIRED;
		currentMaxElectricity = MAX_ELECTRICITY;
		
		ElectricityConnections.registerConnector(this, EnumSet.allOf(ForgeDirection.class));
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if(worldObj.isRemote)
		{
			try {
				synchronized(Mekanism.audioHandler.sounds)
				{
					handleSound();
				}
			} catch(NoSuchMethodError e) {}
		}
		
		if(powerProvider != null)
		{
			int received = (int)(powerProvider.useEnergy(0, (float)((currentMaxElectricity-electricityStored)*Mekanism.TO_BC), true)*10);
			setJoules(electricityStored + received);
		}
		
		boolean testActive = operatingTicks > 0;
		
		if(!worldObj.isRemote)
		{
			for(ForgeDirection direction : ForgeDirection.values())
			{
				TileEntity tileEntity = Vector3.getTileEntityFromSide(worldObj, new Vector3(this), direction);
				if(tileEntity != null)
				{
					if(tileEntity instanceof IConductor)
					{
						if(electricityStored < currentMaxElectricity)
						{
							double electricityNeeded = currentMaxElectricity - electricityStored;
							((IConductor)tileEntity).getNetwork().startRequesting(this, electricityNeeded, electricityNeeded >= getVoltage() ? getVoltage() : electricityNeeded);
							setJoules(electricityStored + ((IConductor)tileEntity).getNetwork().consumeElectricity(this).getWatts());
						}
						else if(electricityStored >= currentMaxElectricity)
						{
							((IConductor)tileEntity).getNetwork().stopRequesting(this);
						}
					}
				}
			}
		}
		
		if(inventory[4] != null)
		{
			if(electricityStored < currentMaxElectricity)
			{
				if(inventory[4].getItem() instanceof IItemElectric)
				{
					IItemElectric electricItem = (IItemElectric)inventory[4].getItem();

					if (electricItem.canProduceElectricity())
					{
						double joulesNeeded = currentMaxElectricity-electricityStored;
						double joulesReceived = 0;
						
						if(electricItem.getVoltage(inventory[4]) <= joulesNeeded)
						{
							joulesReceived = electricItem.onUse(electricItem.getVoltage(inventory[4]), inventory[4]);
						}
						else if(electricItem.getVoltage(inventory[4]) > joulesNeeded)
						{
							joulesReceived = electricItem.onUse(joulesNeeded, inventory[4]);
						}
						
						setJoules(electricityStored + joulesReceived);
					}
				}
				else if(inventory[4].getItem() instanceof IElectricItem)
				{
					IElectricItem item = (IElectricItem)inventory[4].getItem();
					if(item.canProvideEnergy())
					{
						double gain = ElectricItem.discharge(inventory[4], (int)((MAX_ELECTRICITY - electricityStored)*Mekanism.TO_IC2), 3, false, false)*Mekanism.FROM_IC2;
						setJoules(electricityStored + gain);
					}
				}
			}
			if(inventory[4].itemID == Item.redstone.itemID && electricityStored <= (currentMaxElectricity-1000))
			{
				setJoules(electricityStored + 1000);
				--inventory[4].stackSize;
				
	            if (inventory[4].stackSize <= 0)
	            {
	                inventory[4] = null;
	            }
			}
		}
		
		if(inventory[0] != null && inventory[0].getItem() instanceof IMachineUpgrade)
		{
			int energyToAdd = 0;
			int ticksToRemove = 0;
			
			if(currentMaxElectricity == MAX_ELECTRICITY)
			{
				energyToAdd = ((IMachineUpgrade)inventory[0].getItem()).getEnergyBoost(inventory[0]);
			}
			
			if(currentTicksRequired == TICKS_REQUIRED)
			{
				ticksToRemove = ((IMachineUpgrade)inventory[0].getItem()).getTickReduction(inventory[0]);
			}
			
			currentMaxElectricity += energyToAdd;
			currentTicksRequired -= ticksToRemove;
		}
		else if(inventory[0] == null)
		{
			currentTicksRequired = TICKS_REQUIRED;
			currentMaxElectricity = MAX_ELECTRICITY;
		}
		
		if(inventory[1] != null && infuseStored+100 <= MAX_INFUSE)
		{
			if(inventory[1].isItemEqual(new ItemStack(Mekanism.CompressedCarbon)))
			{
				if(type == InfusionType.NONE || type == InfusionType.COAL)
				{
					infuseStored += 100;
					inventory[1].stackSize--;
					type = InfusionType.COAL;
					
		            if (inventory[1].stackSize <= 0)
		            {
		                inventory[1] = null;
		            }
				}
			}
			else if(MekanismUtils.oreDictCheck(inventory[1], "dustTin"))
			{
				if(type == InfusionType.NONE || type == InfusionType.TIN)
				{
					infuseStored += 100;
					inventory[1].stackSize--;
					type = InfusionType.TIN;
					
		            if (inventory[1].stackSize <= 0)
		            {
		                inventory[1] = null;
		            }
				}
			}
		}
		
		if(canOperate() && (operatingTicks+1) < currentTicksRequired)
		{
			++operatingTicks;
			electricityStored -= ENERGY_PER_TICK;
		}
		else if(canOperate() && (operatingTicks+1) >= currentTicksRequired)
		{
			if(!worldObj.isRemote)
			{
				operate();
			}
			operatingTicks = 0;
			electricityStored -= ENERGY_PER_TICK;
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
	
	@SideOnly(Side.CLIENT)
	public void handleSound()
	{
		synchronized(Mekanism.audioHandler.sounds)
		{
			if(audio == null && worldObj != null && worldObj.isRemote)
			{
				if(FMLClientHandler.instance().getClient().sndManager.sndSystem != null)
				{
					audio = Mekanism.audioHandler.getSound("MetallurgicInfuser.ogg", worldObj, xCoord, yCoord, zCoord);
				}
			}
			
			if(worldObj != null && worldObj.isRemote && audio != null)
			{
				if(!audio.isPlaying && isActive == true)
				{
					audio.play();
				}
				else if(audio.isPlaying && isActive == false)
				{
					audio.stop();
				}
			}
		}
	}
	
	public void operate()
	{
        if (!canOperate())
        {
            return;
        }

        InfusionOutput output = RecipeHandler.getOutput(InfusionInput.getInfusion(type, infuseStored, inventory[2]), true, Recipe.METALLURGIC_INFUSER.get());
        
        infuseStored -= output.getInfuseRequired();

        if (inventory[2].stackSize <= 0)
        {
            inventory[2] = null;
        }

        if (inventory[3] == null)
        {
            inventory[3] = output.resource.copy();
        }
        else {
            inventory[3].stackSize += output.resource.stackSize;
        }
	}
	
	public boolean canOperate()
	{
        if (inventory[2] == null)
        {
            return false;
        }
        
        if(electricityStored < ENERGY_PER_TICK)
        {
        	return false;
        }

        InfusionOutput output = RecipeHandler.getOutput(InfusionInput.getInfusion(type, infuseStored, inventory[2]), false, Recipe.METALLURGIC_INFUSER.get());

        if (output == null)
        {
            return false;
        }
        
        if(infuseStored-output.getInfuseRequired() < 0)
        {
        	return false;
        }

        if (inventory[3] == null)
        {
            return true;
        }

        if (!inventory[3].isItemEqual(output.resource))
        {
            return false;
        }
        else
        {
            return inventory[3].stackSize + output.resource.stackSize <= inventory[3].getMaxStackSize();
        }
	}
	
	public int getScaledInfuseLevel(int i)
	{
		return infuseStored*i / MAX_INFUSE;
	}
	
	public int getScaledEnergyLevel(int i)
	{
		return (int)(electricityStored*i / currentMaxElectricity);
	}
	
	public int getScaledProgress(int i)
	{
		return operatingTicks*i / currentTicksRequired;
	}
	
	@Override
	public void invalidate()
	{
		super.invalidate();
		
		if(worldObj.isRemote && audio != null)
		{
			audio.remove();
		}
	}
	
    @Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
    	super.readFromNBT(nbtTags);
    	
    	currentTicksRequired = nbtTags.getInteger("currentTicksRequired");
    	currentMaxElectricity = nbtTags.getDouble("currentMaxElectricity");
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
        
        nbtTags.setInteger("currentTicksRequired", currentTicksRequired);
        nbtTags.setDouble("currentMaxElectricity", currentMaxElectricity);
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
	public void handlePacketData(INetworkManager network, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream)
	{
		if(!worldObj.isRemote)
		{
			try {
				infuseStored = dataStream.readInt();
			} catch (Exception e)
			{
				System.out.println("[Mekanism] Error while handling tile entity packet.");
				e.printStackTrace();
			}
			return;
		}
		
		try {
			facing = dataStream.readInt();
			electricityStored = dataStream.readDouble();
			currentTicksRequired = dataStream.readInt();
			currentMaxElectricity = dataStream.readDouble();
			isActive = dataStream.readBoolean();
			operatingTicks = dataStream.readInt();
			infuseStored = dataStream.readInt();
			type = InfusionType.getFromName(dataStream.readUTF());
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
		PacketHandler.sendTileEntityPacketToClients(this, 0, facing, electricityStored, currentTicksRequired, currentMaxElectricity, isActive, operatingTicks, infuseStored, type.name);
	}

	@Override
	public void sendPacketWithRange() 
	{
		PacketHandler.sendTileEntityPacketToClients(this, 50, facing, electricityStored, currentTicksRequired, currentMaxElectricity, isActive, operatingTicks, infuseStored, type.name);
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
				return new Object[] {currentMaxElectricity};
			case 5:
				return new Object[] {(currentMaxElectricity-electricityStored)};
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
	public double getVoltage(Object... data) 
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
}
