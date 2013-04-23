package mekanism.common;

import ic2.api.Direction;
import ic2.api.ElectricItem;
import ic2.api.IElectricItem;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.IEnergyStorage;
import ic2.api.energy.event.EnergyTileSourceEvent;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;

import mekanism.api.ICableOutputter;
import mekanism.api.IUniversalCable;
import mekanism.api.IStrictEnergyAcceptor;
import mekanism.common.Tier.EnergyCubeTier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import universalelectricity.core.block.IElectricityStorage;
import universalelectricity.core.block.IVoltage;
import universalelectricity.core.electricity.ElectricityNetworkHelper;
import universalelectricity.core.electricity.IElectricityNetwork;
import universalelectricity.core.item.ElectricItemHelper;
import universalelectricity.core.item.IItemElectric;
import universalelectricity.core.vector.Vector3;
import universalelectricity.core.vector.VectorHelper;
import universalelectricity.core.block.IConductor;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerProvider;

import com.google.common.io.ByteArrayDataInput;

import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IPeripheral;

public class TileEntityEnergyCube extends TileEntityElectricBlock implements IEnergySink, IEnergySource, IEnergyStorage, IPowerReceptor, IElectricityStorage, IVoltage, IPeripheral, ICableOutputter, IStrictEnergyAcceptor
{
	/** This Energy Cube's tier. */
	public EnergyCubeTier tier = EnergyCubeTier.BASIC;
	
	/**
	 * A block used to store and transfer electricity.
	 * @param energy - maximum energy this block can hold.
	 * @param i - output per tick this block can handle.
	 */
	public TileEntityEnergyCube()
	{
		super("Energy Cube", 0);
		
		inventory = new ItemStack[2];
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		ChargeUtils.charge(0, this);
		ChargeUtils.discharge(1, this);
		
		if(!worldObj.isRemote)
		{
			TileEntity tileEntity = VectorHelper.getTileEntityFromSide(worldObj, new Vector3(this), ForgeDirection.getOrientation(facing));
			
			if(electricityStored > 0)
			{
				if(tileEntity instanceof IUniversalCable)
				{
					setEnergy(electricityStored - (Math.min(electricityStored, tier.OUTPUT) - CableUtils.emitEnergyToNetwork(Math.min(electricityStored, tier.OUTPUT), this, ForgeDirection.getOrientation(facing))));
					return;
				}
				else if((tileEntity instanceof IEnergyConductor || tileEntity instanceof IEnergyAcceptor) && Mekanism.hooks.IC2Loaded)
				{
					if(electricityStored >= tier.OUTPUT)
					{
						EnergyTileSourceEvent event = new EnergyTileSourceEvent(this, (int)(tier.OUTPUT*Mekanism.TO_IC2));
						MinecraftForge.EVENT_BUS.post(event);
						setEnergy(electricityStored - (tier.OUTPUT - (event.amount*Mekanism.FROM_IC2)));
					}
				}
				else if(isPowerReceptor(tileEntity) && Mekanism.hooks.BuildCraftLoaded)
				{
					IPowerReceptor receptor = (IPowerReceptor)tileEntity;
	            	double electricityNeeded = Math.min(receptor.powerRequest(ForgeDirection.getOrientation(facing).getOpposite()), receptor.getPowerProvider().getMaxEnergyStored() - receptor.getPowerProvider().getEnergyStored())*Mekanism.FROM_BC;
	            	float transferEnergy = (float)Math.min(electricityStored, Math.min(electricityNeeded, tier.OUTPUT));
	            	receptor.getPowerProvider().receiveEnergy((float)(transferEnergy*Mekanism.TO_BC), ForgeDirection.getOrientation(facing).getOpposite());
	            	setEnergy(electricityStored - transferEnergy);
				}
			}
			
			if(tileEntity instanceof IConductor)
			{
				ForgeDirection outputDirection = ForgeDirection.getOrientation(facing);
				ArrayList<IElectricityNetwork> inputNetworks = new ArrayList<IElectricityNetwork>();
				
				for(ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
				{
					if(direction != outputDirection)
					{
						IElectricityNetwork network = ElectricityNetworkHelper.getNetworkFromTileEntity(VectorHelper.getTileEntityFromSide(worldObj, new Vector3(this), direction), direction);
						if(network != null)
						{
							inputNetworks.add(network);
						}
					}
				}
				
				TileEntity outputTile = VectorHelper.getTileEntityFromSide(worldObj, new Vector3(this), outputDirection);
	
				IElectricityNetwork outputNetwork = ElectricityNetworkHelper.getNetworkFromTileEntity(outputTile, outputDirection);
	
				if(outputNetwork != null && !inputNetworks.contains(outputNetwork))
				{
					double outputWatts = Math.min(outputNetwork.getRequest().getWatts(), Math.min(getEnergy(), 10000));
	
					if(getEnergy() > 0 && outputWatts > 0 && getEnergy()-outputWatts >= 0)
					{
						outputNetwork.startProducing(this, Math.min(outputWatts, getEnergy()) / getVoltage(), getVoltage());
						setEnergy(electricityStored - outputWatts);
					}
					else {
						outputNetwork.stopProducing(this);
					}
				}
			}
		}
	}
	
	@Override
	public boolean isStackValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 0)
		{
			return itemstack.getItem() instanceof IElectricItem || 
					(itemstack.getItem() instanceof IItemElectric && ((IItemElectric)itemstack.getItem()).getReceiveRequest(itemstack).amperes != 0);
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
	protected EnumSet<ForgeDirection> getConsumingSides()
	{
		HashSet<ForgeDirection> set = new HashSet<ForgeDirection>();
		
		for(ForgeDirection dir : ForgeDirection.values())
		{
			if(dir != ForgeDirection.getOrientation(facing))
			{
				set.add(dir);
			}
		}
		
		return EnumSet.copyOf(set);
	}

	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, Direction direction)
	{
		return direction.toForgeDirection() != ForgeDirection.getOrientation(facing);
	}

	@Override
	public int getStored() 
	{
		return (int)(electricityStored*Mekanism.TO_IC2);
	}

	@Override
	public int getCapacity() 
	{
		return (int)(tier.MAX_ELECTRICITY*Mekanism.TO_IC2);
	}

	@Override
	public int getOutput() 
	{
		return tier.OUTPUT;
	}

	@Override
	public int demandsEnergy() 
	{
		return (int)((tier.MAX_ELECTRICITY - electricityStored)*Mekanism.TO_IC2);
	}

	@Override
    public int injectEnergy(Direction direction, int i)
    {
		double givenEnergy = i*Mekanism.FROM_IC2;
    	double rejects = 0;
    	double neededEnergy = tier.MAX_ELECTRICITY-electricityStored;
    	
    	if(givenEnergy <= neededEnergy)
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
	public double transferEnergyToAcceptor(double amount)
	{
    	double rejects = 0;
    	double neededElectricity = tier.MAX_ELECTRICITY-electricityStored;
    	
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
		return side != ForgeDirection.getOrientation(facing);
	}

	@Override
	public boolean emitsEnergyTo(TileEntity receiver, Direction direction)
	{
		return direction.toForgeDirection() == ForgeDirection.getOrientation(facing);
	}

	@Override
	public int getMaxEnergyOutput()
	{
		return tier.OUTPUT;
	}

	@Override
	public double getMaxEnergy() 
	{
		return tier.MAX_ELECTRICITY;
	}
	
	@Override
	public int[] getSizeInventorySide(int side)
	{
		return side == 1 ? new int[] {0} : new int[] {1};
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
		else if(slotID == 0)
		{
			return (itemstack.getItem() instanceof IItemElectric && ((IItemElectric)itemstack.getItem()).getReceiveRequest(itemstack).getWatts() == 0) ||
					(itemstack.getItem() instanceof IElectricItem && (!(itemstack.getItem() instanceof IItemElectric) || 
							((IItemElectric)itemstack.getItem()).getReceiveRequest(itemstack).getWatts() == 0));
		}
		
		return false;
	}
	
	@Override
	public int getStartInventorySide(ForgeDirection side) 
	{
		if(side == ForgeDirection.getOrientation(1))
		{
			return 0;
		}
		
		return 1;
	}

	@Override
	public int getSizeInventorySide(ForgeDirection side)
	{
		return 1;
	}

	@Override
	public double getVoltage() 
	{
		return tier.VOLTAGE;
	}
	
	/**
	 * Whether or not the declared Tile Entity is an instance of a BuildCraft power receptor.
	 * @param tileEntity - tile entity to check
	 * @return if the tile entity is a power receptor
	 */
	public boolean isPowerReceptor(TileEntity tileEntity)
	{
		if(tileEntity instanceof IPowerReceptor) 
		{
			IPowerReceptor receptor = (IPowerReceptor)tileEntity;
			IPowerProvider provider = receptor.getPowerProvider();
			return provider != null && provider.getClass().getSuperclass().equals(PowerProvider.class);
		}
		return false;
	}

	@Override
	public String getType() 
	{
		return getInvName();
	}

	@Override
	public String[] getMethodNames() 
	{
		return new String[] {"getStored", "getOutput", "getMaxEnergy", "getEnergyNeeded"};
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, int method, Object[] arguments) throws Exception 
	{
		switch(method)
		{
			case 0:
				return new Object[] {electricityStored};
			case 1:
				return new Object[] {tier.OUTPUT};
			case 2:
				return new Object[] {tier.MAX_ELECTRICITY};
			case 3:
				return new Object[] {(tier.MAX_ELECTRICITY-electricityStored)};
			default:
				System.err.println("[Mekanism] Attempted to call unknown method with computer ID " + computer.getID());
				return null;
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
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		super.handlePacketData(dataStream);
		tier = EnergyCubeTier.getFromName(dataStream.readUTF());
		MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		data.add(tier.name);
		return data;
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);

        tier = EnergyCubeTier.getFromName(nbtTags.getString("tier"));
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setString("tier", tier.name);
    }
	
	@Override
	public int getMaxSafeInput()
	{
		return 2048;
	}

	@Override
	public void setStored(int energy)
	{
		setEnergy(energy*Mekanism.FROM_IC2);
	}

	@Override
	public int addEnergy(int amount)
	{
		setEnergy(electricityStored + amount*Mekanism.FROM_IC2);
		return (int)electricityStored;
	}

	@Override
	public boolean isTeleporterCompatible(Direction side) 
	{
		return true;
	}
	
	@Override
	public int powerRequest(ForgeDirection side) 
	{
		return side != ForgeDirection.getOrientation(facing) ? Math.min(100, (int)(tier.MAX_ELECTRICITY-electricityStored)) : 0;
	}
	
	@Override
	public boolean canOutputTo(ForgeDirection side)
	{
		return side == ForgeDirection.getOrientation(facing);
	}
}
