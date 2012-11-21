package mekanism.common;

import ic2.api.ElectricItem;
import ic2.api.IElectricItem;
import universalelectricity.core.UniversalElectricity;
import universalelectricity.core.electricity.ElectricInfo;
import universalelectricity.core.implement.IItemElectric;

import com.google.common.io.ByteArrayDataInput;

import dan200.computer.api.IComputerAccess;
import mekanism.api.IEnergizedItem;
import net.minecraft.src.*;

public class TileEntitySolarGenerator extends TileEntityGenerator
{
	public boolean seesSun = false;
	
	public int GENERATION_RATE;
	
	public TileEntitySolarGenerator()
	{
		super("Solar Generator", 16000, 32);
		GENERATION_RATE = 32;
		inventory = new ItemStack[1];
	}
	
	public TileEntitySolarGenerator(String name, int maxEnergy, int output, int generation)
	{
		super(name, maxEnergy, output);
		GENERATION_RATE = generation;
		inventory = new ItemStack[1];
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if(inventory[0] != null && energyStored > 0)
		{
			if(inventory[0].getItem() instanceof IEnergizedItem)
			{
				IEnergizedItem item = (IEnergizedItem)inventory[0].getItem();
				int sendingEnergy = 0;
				
				if(item.getRate() <= energyStored)
				{
					sendingEnergy = item.getRate();
				}
				else if(item.getRate() > energyStored)
				{
					sendingEnergy = energyStored;
				}
				
				int rejects = item.charge(inventory[0], sendingEnergy);
				setEnergy(energyStored - (sendingEnergy - rejects));
			}
			else if(inventory[0].getItem() instanceof IItemElectric)
			{
				IItemElectric electricItem = (IItemElectric) inventory[0].getItem();
				double ampsToGive = Math.min(ElectricInfo.getAmps(electricItem.getMaxJoules() * 0.005, getVoltage()), (energyStored*UniversalElectricity.IC2_RATIO));
				double joules = electricItem.onReceive(ampsToGive, getVoltage(), inventory[0]);
				setJoules((energyStored*UniversalElectricity.IC2_RATIO) - (ElectricInfo.getJoules(ampsToGive, getVoltage(), 1) - joules));
			}
			else if(inventory[0].getItem() instanceof IElectricItem)
			{
				int sent = ElectricItem.charge(inventory[0], energyStored, 3, false, false);
				setEnergy(energyStored - sent);
			}
		}
		
		if(!worldObj.isRemote)
		{
			if(worldObj.isDaytime() && !worldObj.isRaining() && !worldObj.isThundering() && !worldObj.provider.hasNoSky && worldObj.canBlockSeeTheSky(xCoord, yCoord+1, zCoord))
			{
				seesSun = true;
			}
			else {
				seesSun = false;
			}
		}
		
		if(canOperate())
		{
			setEnergy(energyStored + getEnvironmentBoost());
		}
	}
	
	@Override
	public boolean canOperate()
	{
		return energyStored < MAX_ENERGY && seesSun;
	}
	
	@Override
	public int getEnvironmentBoost()
	{
		return seesSun ? GENERATION_RATE : 0;
	}

	@Override
	public String[] getMethodNames() 
	{
		return new String[] {"getStored", "getOutput", "getMaxEnergy", "getEnergyNeeded", "getSeesSun"};
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, int method, Object[] arguments) throws Exception 
	{
		switch(method)
		{
			case 0:
				return new Object[] {energyStored};
			case 1:
				return new Object[] {output};
			case 2:
				return new Object[] {MAX_ENERGY};
			case 3:
				return new Object[] {(MAX_ENERGY-energyStored)};
			case 4:
				return new Object[] {seesSun};
			default:
				System.err.println("[Mekanism] Attempted to call unknown method with computer ID " + computer.getID());
				return null;
		}
	}

	@Override
	public void handlePacketData(INetworkManager network, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream)
	{
		try {
			facing = dataStream.readInt();
			energyStored = dataStream.readInt();
			isActive = dataStream.readBoolean();
			seesSun = dataStream.readBoolean();
			worldObj.markBlockForRenderUpdate(xCoord, yCoord, zCoord);
		} catch (Exception e)
		{
			System.out.println("[Mekanism] Error while handling tile entity packet.");
			e.printStackTrace();
		}
	}

	@Override
	public void sendPacket()
	{
		PacketHandler.sendTileEntityPacketToClients(this, 0, facing, energyStored, isActive, seesSun);
	}

	@Override
	public void sendPacketWithRange() 
	{
		PacketHandler.sendTileEntityPacketToClients(this, 50, facing, energyStored, isActive, seesSun);
	}
}
