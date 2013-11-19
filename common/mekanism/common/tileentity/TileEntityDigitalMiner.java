package mekanism.common.tileentity;

import ic2.api.energy.tile.IEnergySink;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import mekanism.api.Object3D;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.common.HashList;
import mekanism.common.IRedstoneControl;
import mekanism.common.IUpgradeTile;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.TileComponentUpgrade;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.miner.MinerFilter;
import mekanism.common.miner.ThreadMinerSearch;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

import com.google.common.io.ByteArrayDataInput;

public class TileEntityDigitalMiner extends TileEntityElectricBlock implements IEnergySink, IStrictEnergyAcceptor, IUpgradeTile, IRedstoneControl
{
	public Set<Object3D> oresToMine = new HashSet<Object3D>();
	
	public HashList<MinerFilter> filters = new HashList<MinerFilter>();
	
	public int radius;
	
	public int minY;
	public int maxY;
	
	public boolean doEject = false;
	public boolean doPull = false;

	public ThreadMinerSearch searcher;
	
	public int clientToMine;
	
	/** This machine's current RedstoneControl type. */
	public RedstoneControl controlType = RedstoneControl.DISABLED;
	
	public TileComponentUpgrade upgradeComponent = new TileComponentUpgrade(this, 28);
	
	public TileEntityDigitalMiner()
	{
		super("Digital Miner", MachineType.DIGITAL_MINER.baseEnergy);
		inventory = new ItemStack[29];
		radius = 10;
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if(!worldObj.isRemote)
		{
			ChargeUtils.discharge(27, this);
		}
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);
        
        radius = nbtTags.getInteger("radius");
        minY = nbtTags.getInteger("minY");
        maxY = nbtTags.getInteger("maxY");
        doEject = nbtTags.getBoolean("doEject");
        doPull = nbtTags.getBoolean("doPull");
        controlType = RedstoneControl.values()[nbtTags.getInteger("controlType")];
        
    	if(nbtTags.hasKey("filters"))
    	{
    		NBTTagList tagList = nbtTags.getTagList("filters");
    		
    		for(int i = 0; i < tagList.tagCount(); i++)
    		{
    			filters.add(MinerFilter.readFromNBT((NBTTagCompound)tagList.tagAt(i)));
    		}
    	}
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setInteger("radius", radius);
        nbtTags.setInteger("minY", minY);
        nbtTags.setInteger("maxY", maxY);
        nbtTags.setBoolean("doEject", doEject);
        nbtTags.setBoolean("doPull", doPull);
        nbtTags.setInteger("controlType", controlType.ordinal());
        
        NBTTagList filterTags = new NBTTagList();
        
        for(MinerFilter filter : filters)
        {
        	NBTTagCompound tagCompound = new NBTTagCompound();
        	filter.write(new NBTTagCompound());
        	filterTags.appendTag(tagCompound);
        }
        
        if(filterTags.tagCount() != 0)
        {
        	nbtTags.setTag("filters", filterTags);
        }
        
        NBTTagList miningOres = new NBTTagList();
        
        for(Object3D obj : oresToMine)
        {
        	miningOres.appendTag(obj.write(new NBTTagCompound()));
        }
        
        if(miningOres.tagCount() != 0)
        {
        	nbtTags.setTag("oresToMine", miningOres);
        }
    }
	
	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		super.handlePacketData(dataStream);
		
		int type = dataStream.readInt();
		
		if(type == 0)
		{
			radius = dataStream.readInt();
			minY = dataStream.readInt();
			maxY = dataStream.readInt();
			doEject = dataStream.readBoolean();
			doPull = dataStream.readBoolean();
			clientToMine = dataStream.readInt();
			controlType = RedstoneControl.values()[dataStream.readInt()];
		}
		else if(type == 1)
		{
			filters.clear();
			
			int amount = dataStream.readInt();
			
			for(int i = 0; i < amount; i++)
			{
				filters.add(MinerFilter.readFromPacket(dataStream));
			}
		}
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		data.add(0);
		data.add(radius);
		data.add(minY);
		data.add(maxY);
		data.add(doEject);
		data.add(doPull);
		data.add(oresToMine.size());
		data.add(controlType.ordinal());
		
		return data;
	}
	
	@Override
	public void openChest()
	{
		if(!worldObj.isRemote)
		{
			PacketHandler.sendPacket(Transmission.CLIENTS_RANGE, new PacketTileEntity().setParams(Object3D.get(this), getFilterPacket(new ArrayList())), Object3D.get(this), 50D);
		}
	}
	
	public ArrayList getFilterPacket(ArrayList data)
	{
		super.getNetworkedData(data);
		
		data.add(1);
		
		data.add(filters.size());
		
		for(MinerFilter filter : filters)
		{
			filter.write(data);
		}
		
		return data;
	}

	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction)
	{
		return true;
	}
	
	public double demandedEnergyUnits()
	{
		return (getMaxEnergy() - getEnergy())*Mekanism.TO_IC2;
	}

	@Override
	public double injectEnergyUnits(ForgeDirection direction, double amount)
	{
		if(Object3D.get(this).getFromSide(direction).getTileEntity(worldObj) instanceof TileEntityUniversalCable)
		{
			return amount;
		}
		
		double givenEnergy = amount*Mekanism.FROM_IC2;
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
	public int getMaxSafeInput()
	{
		return 2048;
	}
	
	@Override
	public double transferEnergyToAcceptor(double amount)
	{
    	double rejects = 0;
    	double neededGas = getMaxEnergy()-getEnergy();
    	
    	if(amount <= neededGas)
    	{
    		electricityStored += amount;
    	}
    	else {
    		electricityStored += neededGas;
    		rejects = amount-neededGas;
    	}
    	
    	return rejects;
	}
	
	@Override
	public double getMaxEnergy() 
	{
		return MekanismUtils.getEnergy(getEnergyMultiplier(), MAX_ELECTRICITY);
	}
	
	@Override
	public boolean canReceiveEnergy(ForgeDirection side)
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
}
