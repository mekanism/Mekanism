package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.IHeatTransfer;
import mekanism.api.MekanismConfig.general;
import mekanism.api.Range4D;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityResistiveHeater extends TileEntityNoisyElectricBlock implements IHeatTransfer
{
	public double energyUsage = 100;
	
	public double temperature;
	public double heatToAbsorb = 0;
	
	/** Whether or not this machine is in it's active state. */
	public boolean isActive;

	/** The client's current active state. */
	public boolean clientActive;

	/** How many ticks must pass until this block's active state can sync with the client. */
	public int updateDelay;
	
	public TileEntityResistiveHeater()
	{
		super("machine.resistiveheater", "ResistiveHeater", MachineType.RESISTIVE_HEATER.baseEnergy);
		inventory = new ItemStack[1];
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if(worldObj.isRemote && updateDelay > 0)
		{
			updateDelay--;

			if(updateDelay == 0 && clientActive != isActive)
			{
				isActive = clientActive;
				MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
			}
		}
		
		if(!worldObj.isRemote)
		{
			if(updateDelay > 0)
			{
				updateDelay--;

				if(updateDelay == 0 && clientActive != isActive)
				{
					Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));
				}
			}
			
			ChargeUtils.discharge(0, this);
			
			double toUse = Math.min(getEnergy(), energyUsage);
			heatToAbsorb += toUse/general.energyPerHeat;
			setEnergy(getEnergy() - toUse);
			
			simulateHeat();
			applyTemperatureChange();
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		energyUsage = nbtTags.getDouble("energyUsage");
		temperature = nbtTags.getDouble("temperature");
		clientActive = isActive = nbtTags.getBoolean("isActive");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setDouble("energyUsage", energyUsage);
		nbtTags.setDouble("temperature", temperature);
		nbtTags.setBoolean("isActive", isActive);
	}
	
	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		if(!worldObj.isRemote)
		{
			energyUsage = dataStream.readDouble();
			
			return;
		}
		
		super.handlePacketData(dataStream);
		
		energyUsage = dataStream.readDouble();
		temperature = dataStream.readDouble();
		clientActive = dataStream.readBoolean();
		maxEnergy = dataStream.readDouble();
		
		if(updateDelay == 0 && clientActive != isActive)
		{
			updateDelay = general.UPDATE_DELAY;
			isActive = clientActive;
			MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
		}
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		data.add(energyUsage);
		data.add(temperature);
		data.add(isActive);
		data.add(maxEnergy);
		
		return data;
	}

	@Override
	public double getTemp() 
	{
		return temperature;
	}

	@Override
	public double getInverseConductionCoefficient() 
	{
		return 1;
	}

	@Override
	public double getInsulationCoefficient(ForgeDirection side) 
	{
		return 1000;
	}

	@Override
	public void transferHeatTo(double heat)
	{
		heatToAbsorb += heat;
	}

	@Override
	public double[] simulateHeat() 
	{
		return HeatUtils.simulate(this);
	}

	@Override
	public double applyTemperatureChange() 
	{
		temperature += heatToAbsorb;
		heatToAbsorb = 0;
		
		return temperature;
	}

	@Override
	public boolean canConnectHeat(ForgeDirection side) 
	{
		return true;
	}

	@Override
	public IHeatTransfer getAdjacent(ForgeDirection side) 
	{
		return null;
	}
	
	@Override
	public void setActive(boolean active)
	{
		isActive = active;

		if(clientActive != active && updateDelay == 0)
		{
			Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));

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
	public boolean renderUpdate()
	{
		return true;
	}

	@Override
	public boolean lightUpdate()
	{
		return true;
	}
}
