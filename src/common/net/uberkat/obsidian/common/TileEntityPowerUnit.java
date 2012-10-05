package net.uberkat.obsidian.common;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import universalelectricity.UniversalElectricity;
import universalelectricity.electricity.ElectricInfo;
import universalelectricity.electricity.ElectricityManager;
import universalelectricity.implement.IConductor;
import universalelectricity.implement.IElectricityReceiver;
import universalelectricity.implement.IElectricityStorage;
import universalelectricity.implement.IItemElectric;
import universalelectricity.network.ConnectionHandler;
import universalelectricity.network.ConnectionHandler.ConnectionType;
import universalelectricity.network.ISimpleConnectionHandler;
import universalelectricity.prefab.TileEntityConductor;
import universalelectricity.prefab.TileEntityDisableable;
import universalelectricity.prefab.Vector3;

import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerFramework;
import buildcraft.api.power.PowerProvider;
import buildcraft.api.core.Orientations;

import com.google.common.io.ByteArrayDataInput;

import ic2.api.Direction;
import ic2.api.ElectricItem;
import ic2.api.EnergyNet;
import ic2.api.IEnergySink;
import ic2.api.IEnergySource;
import ic2.api.IEnergyStorage;
import ic2.api.IWrenchable;
import ic2.api.IElectricItem;
import net.minecraft.src.*;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;
import net.uberkat.obsidian.api.IEnergizedItem;
import net.uberkat.obsidian.api.INetworkedMachine;

public class TileEntityPowerUnit extends TileEntityDisableable implements IInventory, ISidedInventory, INetworkedMachine, IWrenchable, IEnergySink, IEnergySource, IEnergyStorage, IPowerReceptor, IElectricityStorage, IElectricityReceiver
{
	public ItemStack[] inventory = new ItemStack[2];
	
	/** Maximum amount of energy this unit can hold. */
	public int maxEnergy;
	
	/** Output per tick this machine can transfer. */
	public int output;
	
	/** The amount of energy this unit is storing. */
	public int energyStored = 0;
	
	/** Direction this block is facing. */
	public int facing;
	
	/** A timer used to send packets to clients. */
	public int packetTick = 0;
	
	/** BuildCraft power provider. */
	public IPowerProvider powerProvider;
	
	/** Whether or not this machine has initialized and registered with other mods. */
	public boolean initialized = false;
	
	/**
	 * A block used to store and transfer electricity.
	 */
	public TileEntityPowerUnit()
	{
		this(500000, 256);
	}
	
	/**
	 * A block used to store and transfer electricity.
	 * @param energy - maximum energy this block can hold.
	 * @param i - output per tick this block can handle.
	 */
	public TileEntityPowerUnit(int energy, int i)
	{
		ElectricityManager.instance.registerElectricUnit(this);
		maxEnergy = energy;
		output = i;
		if(PowerFramework.currentFramework != null)
		{
			powerProvider = PowerFramework.currentFramework.createPowerProvider();
			powerProvider.configure(20, 25, 25, 25, maxEnergy/10);
		}
	}
	
	public Packet getDescriptionPacket()
	{
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream output = new DataOutputStream(bytes);
        
        try {
        	output.writeInt(EnumPacketType.TILE_ENTITY.id);
        	output.writeInt(xCoord);
        	output.writeInt(yCoord);
        	output.writeInt(zCoord);
        	output.writeInt(facing);
        	output.writeInt(energyStored);
        } catch (IOException e)
        {
        	System.err.println("[ObsidianIngots] Error while writing tile entity packet.");
        	e.printStackTrace();
        }
        
        Packet250CustomPayload packet = new Packet250CustomPayload();
        packet.channel = "ObsidianIngots";
        packet.data = bytes.toByteArray();
        packet.length = packet.data.length;
        
        return packet;
	}
	
	public void updateEntity()
	{
		if(!worldObj.isRemote)
		{
			if(packetTick == 5)
			{
				PacketHandler.sendPowerUnitPacket(this);
			}
			
			if(packetTick % 100 == 0)
			{
				PacketHandler.sendPowerUnitPacketWithRange(this, 50);
			}
			packetTick++;
		}
		
		if(!initialized)
		{
			if(ObsidianIngots.hooks.IC2Loaded)
			{
				EnergyNet.getForWorld(worldObj).addTileEntity(this);
			}
			
			initialized = true;
		}
		
		if(powerProvider != null)
		{
			int received = (int)(powerProvider.useEnergy(25, 25, true)*100);
			setEnergy(energyStored + received);
		}
		
		if(inventory[0] != null && energyStored > 0)
		{
			if(inventory[0].getItem() instanceof IEnergizedItem)
			{
				IEnergizedItem item = (IEnergizedItem)inventory[0].getItem();
				int rejects = item.charge(inventory[0], item.getRate());
				setEnergy(energyStored - (item.getRate() - rejects));
			}
			else if(inventory[0].getItem() instanceof IItemElectric)
			{
				IItemElectric item = (IItemElectric)inventory[0].getItem();
				int rejects = (int)(item.onReceiveElectricity(item.getTransferRate(), inventory[0])*UniversalElectricity.Wh_IC2_RATIO);
				setEnergy(energyStored - ((int)(item.getTransferRate()*UniversalElectricity.Wh_IC2_RATIO) - rejects));
			}
			else if(inventory[0].getItem() instanceof IElectricItem)
			{
				int sent = ElectricItem.charge(inventory[0], energyStored, 3, false, false);
				setEnergy(energyStored - sent);
			}
		}
		
		if(inventory[1] != null && energyStored < maxEnergy)
		{
			if(inventory[1].getItem() instanceof IEnergizedItem)
			{
				IEnergizedItem item = (IEnergizedItem)inventory[1].getItem();
				int received = item.discharge(inventory[1], item.getRate());
				setEnergy(energyStored + received);
			}
			else if(inventory[1].getItem() instanceof IItemElectric)
			{
				IItemElectric item = (IItemElectric)inventory[1].getItem();
				int received = (int)(item.onUseElectricity(item.getTransferRate(), inventory[1])*UniversalElectricity.Wh_IC2_RATIO);
				setEnergy(energyStored + received);
			}
			else if(inventory[1].getItem() instanceof IElectricItem)
			{
				IElectricItem item = (IElectricItem)inventory[1].getItem();
				if(item.canProvideEnergy())
				{
					int gain = ElectricItem.discharge(inventory[1], maxEnergy - energyStored, 3, false, false);
					setEnergy(energyStored + gain);
				}
			}
			else if(inventory[1].itemID == Item.redstone.shiftedIndex)
			{
				setEnergy(energyStored + 1000);
				--inventory[1].stackSize;
				
                if (inventory[1].stackSize <= 0)
                {
                    inventory[1] = null;
                }
			}
		}
		
		if(energyStored > 0)
		{
			TileEntity tileEntity = Vector3.getTileEntityFromSide(worldObj, Vector3.get(this), ForgeDirection.getOrientation(facing));
			
			if(ObsidianIngots.hooks.IC2Loaded)
			{
				if(energyStored >= output)
				{
					setEnergy(energyStored - (output - EnergyNet.getForWorld(worldObj).emitEnergyFrom(this, output)));
				}
			}
			
			if(isPowerReceptor(tileEntity))
			{
				IPowerReceptor receptor = (IPowerReceptor)tileEntity;
            	int energyNeeded = Math.min(receptor.getPowerProvider().getMinEnergyReceived(), receptor.getPowerProvider().getMaxEnergyReceived())*10;
            	float transferEnergy = Math.max(Math.min(Math.min(energyNeeded, energyStored), 54000), 0);
            	receptor.getPowerProvider().receiveEnergy((float)(transferEnergy/10), Orientations.dirs()[ForgeDirection.getOrientation(facing).getOpposite().ordinal()]);
            	setEnergy(energyStored - (int)transferEnergy);
			}
			
			TileEntity connector = Vector3.getConnectorFromSide(worldObj, Vector3.get(this), ForgeDirection.getOrientation(facing));
			
			if(connector != null && connector instanceof TileEntityConductor)
			{
				double wattsNeeded = ElectricityManager.instance.getElectricityRequired(((IConductor)connector).getConnectionID());
                double transferAmps = Math.max(Math.min(Math.min(ElectricInfo.getAmps(wattsNeeded, getVoltage()), ElectricInfo.getAmpsFromWattHours(energyStored*UniversalElectricity.IC2_RATIO, getVoltage()) ), 15), 0);                        
                ElectricityManager.instance.produceElectricity(this, (IConductor)connector, transferAmps, getVoltage());
                setEnergy(energyStored - (int)(ElectricInfo.getWattHours(transferAmps, getVoltage())*UniversalElectricity.Wh_IC2_RATIO));
			}
		}
		worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
	}
	
	public void setEnergy(int energy)
	{
		energyStored = Math.max(Math.min(energy, maxEnergy), 0);
	}

	public int getStartInventorySide(ForgeDirection side) 
	{
		return 1;
	}

    public int getSizeInventorySide(ForgeDirection side)
    {
        return 1;
    }

    public int getSizeInventory()
    {
        return inventory.length;
    }

    public ItemStack getStackInSlot(int par1)
    {
        return inventory[par1];
    }

    public ItemStack decrStackSize(int par1, int par2)
    {
        if (inventory[par1] != null)
        {
            ItemStack var3;

            if (inventory[par1].stackSize <= par2)
            {
                var3 = inventory[par1];
                inventory[par1] = null;
                return var3;
            }
            else
            {
                var3 = inventory[par1].splitStack(par2);

                if (inventory[par1].stackSize == 0)
                {
                    inventory[par1] = null;
                }

                return var3;
            }
        }
        else
        {
            return null;
        }
    }

    public ItemStack getStackInSlotOnClosing(int par1)
    {
        if (inventory[par1] != null)
        {
            ItemStack var2 = inventory[par1];
            inventory[par1] = null;
            return var2;
        }
        else
        {
            return null;
        }
    }
    
    public void setInventorySlotContents(int par1, ItemStack par2ItemStack)
    {
        inventory[par1] = par2ItemStack;

        if (par2ItemStack != null && par2ItemStack.stackSize > getInventoryStackLimit())
        {
            par2ItemStack.stackSize = getInventoryStackLimit();
        }
    }

	public String getInvName()
	{
		return output == 1024 ? "Advanced Power Unit" : "Power Unit";
	}

	public int getInventoryStackLimit() 
	{
		return 64;
	}

	public void openChest() {}

	public void closeChest() {}
	
    public void readFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readFromNBT(par1NBTTagCompound);
        if(PowerFramework.currentFramework != null)
        {
        	PowerFramework.currentFramework.loadPowerProvider(this, par1NBTTagCompound);
        }
        NBTTagList var2 = par1NBTTagCompound.getTagList("Items");
        inventory = new ItemStack[getSizeInventory()];

        for (int var3 = 0; var3 < var2.tagCount(); ++var3)
        {
            NBTTagCompound var4 = (NBTTagCompound)var2.tagAt(var3);
            byte var5 = var4.getByte("Slot");

            if (var5 >= 0 && var5 < inventory.length)
            {
                inventory[var5] = ItemStack.loadItemStackFromNBT(var4);
            }
        }

        energyStored = par1NBTTagCompound.getInteger("energyStored");
        facing = par1NBTTagCompound.getInteger("facing");
    }

    public void writeToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeToNBT(par1NBTTagCompound);
        if(PowerFramework.currentFramework != null)
        {
        	PowerFramework.currentFramework.savePowerProvider(this, par1NBTTagCompound);
        }
        par1NBTTagCompound.setInteger("energyStored", energyStored);
        par1NBTTagCompound.setInteger("facing", facing);
        NBTTagList var2 = new NBTTagList();

        for (int var3 = 0; var3 < inventory.length; ++var3)
        {
            if (inventory[var3] != null)
            {
                NBTTagCompound var4 = new NBTTagCompound();
                var4.setByte("Slot", (byte)var3);
                inventory[var3].writeToNBT(var4);
                var2.appendTag(var4);
            }
        }

        par1NBTTagCompound.setTag("Items", var2);
    }

	public void handlePacketData(NetworkManager network, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream) 
	{
		try {
			facing = dataStream.readInt();
			energyStored = dataStream.readInt();
			worldObj.markBlockAsNeedsUpdate(xCoord, yCoord, zCoord);
		} catch (Exception e)
		{
			System.out.println("[ObsidianIngots] Error while handling tile entity packet.");
			e.printStackTrace();
		}
	}

	public boolean wrenchCanSetFacing(EntityPlayer entityPlayer, int side)
	{
		return facing != side;
	}

	public short getFacing()
	{
		return (short)facing;
	}

	public void setFacing(short direction) 
	{
		if(initialized)
		{
			if(ObsidianIngots.hooks.IC2Loaded)
			{
				EnergyNet.getForWorld(worldObj).removeTileEntity(this);
			}
		}
		
		initialized = false;
		facing = direction;
		PacketHandler.sendPowerUnitPacket(this);
		if(ObsidianIngots.hooks.IC2Loaded)
		{
			EnergyNet.getForWorld(worldObj).addTileEntity(this);
		}
		initialized = true;
	}

	public boolean wrenchCanRemove(EntityPlayer entityPlayer) 
	{
		return true;
	}

	public float getWrenchDropRate() 
	{
		return 1.0F;
	}

	public boolean acceptsEnergyFrom(TileEntity emitter, Direction direction)
	{
		return direction.toForgeDirection() != ForgeDirection.getOrientation(facing);
	}

	public boolean isAddedToEnergyNet() 
	{
		return initialized;
	}

	public int getStored() 
	{
		return energyStored;
	}

	public int getCapacity() 
	{
		return maxEnergy;
	}

	public int getOutput() 
	{
		return output;
	}

	public boolean demandsEnergy() 
	{
		return energyStored < maxEnergy;
	}

    public int injectEnergy(Direction direction, int i)
    {
        int j = i;

        if (energyStored + i >= maxEnergy + output)
        {
            j = (maxEnergy + output) - energyStored - 1;
        }

        energyStored += j;
        return i - j;
    }

	public boolean emitsEnergyTo(TileEntity receiver, Direction direction)
	{
		return direction.toForgeDirection() == ForgeDirection.getOrientation(facing);
	}

	public int getMaxEnergyOutput()
	{
		return output;
	}

	public double getWattHours(Object... data) 
	{
		return energyStored*UniversalElectricity.IC2_RATIO;
	}

	public void setWattHours(double wattHours, Object... data) 
	{
		setEnergy((int)(wattHours*UniversalElectricity.Wh_IC2_RATIO));
	}

	public double getMaxWattHours() 
	{
		return maxEnergy*UniversalElectricity.IC2_RATIO;
	}

	public void setPowerProvider(IPowerProvider provider)
	{
		powerProvider = provider;
	}

	public IPowerProvider getPowerProvider() 
	{
		return powerProvider;
	}

	public void doWork() {}

	public int powerRequest() 
	{
		return getPowerProvider().getMaxEnergyReceived();
	}

	public boolean canConnect(ForgeDirection side) 
	{
		return true;
	}

	public double getVoltage() 
	{
		return 120;
	}
	
	public boolean isPowerReceptor(TileEntity tileEntity)
	{
		if(tileEntity instanceof IPowerReceptor) 
		{
			IPowerReceptor receptor = (IPowerReceptor) tileEntity;
			IPowerProvider provider = receptor.getPowerProvider();
			return provider != null && provider.getClass().getSuperclass().equals(PowerProvider.class);
		}
		return false;
	}

	public void onReceive(TileEntity sender, double amps, double voltage, ForgeDirection side) 
	{
		System.out.println("Received " + amps + " " + voltage);
		setEnergy(energyStored + (int)(ElectricInfo.getWattHours(amps, voltage)*UniversalElectricity.Wh_IC2_RATIO));
	}

	public double wattRequest() 
	{
		return ElectricInfo.getWatts(maxEnergy*UniversalElectricity.IC2_RATIO) - ElectricInfo.getWatts(energyStored*UniversalElectricity.IC2_RATIO);
	}

	public boolean canReceiveFromSide(ForgeDirection side) 
	{
		return side != ForgeDirection.getOrientation(facing);
	}

	public boolean isUseableByPlayer(EntityPlayer var1)
	{
		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) != this ? false : var1.getDistanceSq((double)xCoord + 0.5D, (double)yCoord + 0.5D, (double)zCoord + 0.5D) <= 64.0D;
	}
}
