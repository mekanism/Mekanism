package mekanism.common;

import java.util.ArrayList;
import java.util.EnumSet;

import ic2.api.Direction;
import ic2.api.ElectricItem;
import ic2.api.IElectricItem;
import ic2.api.energy.tile.IEnergySink;
import mekanism.api.IActiveState;
import mekanism.api.IConfigurable;
import mekanism.api.IUpgradeManagement;
import mekanism.api.SideData;
import mekanism.api.Tier.SmeltingFactoryTier;
import mekanism.client.Sound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
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

public class TileEntitySmeltingFactory extends TileEntityElectricBlock implements IEnergySink, IJouleStorage, IVoltage, IPeripheral, IActiveState, IConfigurable, IUpgradeManagement
{	
	/** This Smelting Factory's tier. */
	public SmeltingFactoryTier tier;
	
	/** This machine's side configuration. */
	public byte[] sideConfig;
	
	/** An arraylist of SideData for this machine. */
	public ArrayList<SideData> sideOutputs = new ArrayList<SideData>();
	
	/** The Sound instance for this machine. */
	@SideOnly(Side.CLIENT)
	public Sound audio;
	
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
	
	/** This machine's previous active state, used for calculating packets. */
	public boolean prevActive;
	
	/** This machine's active state. */
	public boolean isActive;
	
	public TileEntitySmeltingFactory()
	{
		this(SmeltingFactoryTier.BASIC);
		
		sideOutputs.add(new SideData(EnumColor.GREY, 0, 0));
		sideOutputs.add(new SideData(EnumColor.ORANGE, 0, 1));
		sideOutputs.add(new SideData(EnumColor.DARK_GREEN, 1, 1));
		sideOutputs.add(new SideData(EnumColor.DARK_RED, 2, 3));
		sideOutputs.add(new SideData(EnumColor.DARK_BLUE, 5, 3));
		
		sideConfig = new byte[] {4, 3, 0, 0, 2, 1};
	}
	
	public TileEntitySmeltingFactory(SmeltingFactoryTier type)
	{
		super(type.name + " Smelting Factory", type.processes*2000);
		ElectricityConnections.registerConnector(this, EnumSet.allOf(ForgeDirection.class));
		tier = type;
		inventory = new ItemStack[2+type.processes*2];
		progress = new int[type.processes];
		isActive = false;
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if(worldObj.isRemote)
		{
			try {
				if(Mekanism.audioHandler != null)
				{
					synchronized(Mekanism.audioHandler.sounds)
					{
						handleSound();
					}
				}
			} catch(NoSuchMethodError e) {}
		}
		
		if(powerProvider != null)
		{
			int received = (int)(powerProvider.useEnergy(0, (float)((MekanismUtils.getEnergy(energyMultiplier, MAX_ELECTRICITY)-electricityStored)*Mekanism.TO_BC), true)*Mekanism.FROM_BC);
			setJoules(electricityStored + received);
		}
		
		boolean testActive = false;
		
		if(!worldObj.isRemote)
		{
			for(ForgeDirection direction : ForgeDirection.values())
			{
				TileEntity tileEntity = Vector3.getTileEntityFromSide(worldObj, new Vector3(this), direction);
				if(tileEntity != null)
				{
					if(tileEntity instanceof IConductor)
					{
						if(electricityStored < MekanismUtils.getEnergy(energyMultiplier, MAX_ELECTRICITY))
						{
							double electricityNeeded = MekanismUtils.getEnergy(energyMultiplier, MAX_ELECTRICITY) - electricityStored;
							((IConductor)tileEntity).getNetwork().startRequesting(this, electricityNeeded, electricityNeeded >= getVoltage() ? getVoltage() : electricityNeeded);
							setJoules(electricityStored + ((IConductor)tileEntity).getNetwork().consumeElectricity(this).getWatts());
						}
						else if(electricityStored >= MekanismUtils.getEnergy(energyMultiplier, MAX_ELECTRICITY))
						{
							((IConductor)tileEntity).getNetwork().stopRequesting(this);
						}
					}
				}
			}
		}
		
		for(int i : progress)
		{
			if(i > 0)
			{
				testActive = true;
			}
		}
		
		if(inventory[1] != null)
		{
			if(electricityStored < MekanismUtils.getEnergy(energyMultiplier, MAX_ELECTRICITY))
			{
				if(inventory[1].getItem() instanceof IItemElectric)
				{
					IItemElectric electricItem = (IItemElectric)inventory[1].getItem();

					if (electricItem.canProduceElectricity())
					{
						double joulesNeeded = MekanismUtils.getEnergy(energyMultiplier, MAX_ELECTRICITY)-electricityStored;
						double joulesReceived = electricItem.onUse(Math.min(electricItem.getMaxJoules(inventory[1])*0.005, joulesNeeded), inventory[1]);
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
			if(inventory[1].itemID == Item.redstone.itemID && electricityStored+1000 <= MekanismUtils.getEnergy(energyMultiplier, MAX_ELECTRICITY))
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
			if(inventory[0].isItemEqual(new ItemStack(Mekanism.EnergyUpgrade)) && speedMultiplier < 8)
			{
				if(upgradeTicks < UPGRADE_TICKS_REQUIRED)
				{
					upgradeTicks++;
				}
				else if(upgradeTicks == UPGRADE_TICKS_REQUIRED)
				{
					upgradeTicks = 0;
					energyMultiplier+=1;
					
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
					speedMultiplier+=1;
					
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
		
		for(int mainSlot = 0; mainSlot < tier.processes; mainSlot++)
		{
			if(canOperate(getInputSlot(mainSlot), getOutputSlot(mainSlot)) && (progress[mainSlot]+1) < MekanismUtils.getTicks(speedMultiplier))
			{
				++progress[mainSlot];
				electricityStored -= ENERGY_PER_TICK;
			}
			else if(canOperate(getInputSlot(mainSlot), getOutputSlot(mainSlot)) && (progress[mainSlot]+1) >= MekanismUtils.getTicks(speedMultiplier))
			{
				if(!worldObj.isRemote)
				{
					operate(getInputSlot(mainSlot), getOutputSlot(mainSlot));
				}
				progress[mainSlot] = 0;
				electricityStored -= ENERGY_PER_TICK;
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
	
	@SideOnly(Side.CLIENT)
	public void handleSound()
	{
		if(Mekanism.audioHandler != null)
		{
			synchronized(Mekanism.audioHandler.sounds)
			{
				if(audio == null && worldObj != null && worldObj.isRemote)
				{
					if(FMLClientHandler.instance().getClient().sndManager.sndSystem != null)
					{
						audio = Mekanism.audioHandler.getSound("SmeltingFactory.ogg", worldObj, xCoord, yCoord, zCoord);
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
						audio.stopLoop();
					}
				}
			}
		}
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
	
	public int getScaledProgress(int i, int process)
	{
		return progress[process]*i / MekanismUtils.getTicks(speedMultiplier);
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

        ItemStack itemstack = FurnaceRecipes.smelting().getSmeltingResult(inventory[inputSlot]);

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
		
		for(int i = 0; i < tier.processes; i++)
		{
			progress[i] = dataStream.readInt();
		}
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);
        
        speedMultiplier = nbtTags.getInteger("speedMultiplier");
        energyMultiplier = nbtTags.getInteger("energyMultiplier");
        isActive = nbtTags.getBoolean("isActive");
        
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
		return operation+2;
	}
	
	public int getOutputSlot(int operation)
	{
		return tier.processes+2+operation;
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
	public int powerRequest() 
	{
		return (int)((MekanismUtils.getEnergy(energyMultiplier, MAX_ELECTRICITY)-electricityStored)*Mekanism.TO_BC);
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
}
