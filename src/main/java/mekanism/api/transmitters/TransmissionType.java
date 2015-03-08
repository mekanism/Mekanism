package mekanism.api.transmitters;

import mekanism.api.gas.IGasTransmitter;
import mekanism.common.util.MekanismUtils;
import net.minecraft.tileentity.TileEntity;

public enum TransmissionType
{
	ENERGY("EnergyNetwork", "Energy"),
	FLUID("FluidNetwork", "Fluids"),
	GAS("GasNetwork", "Gases"),
	ITEM("InventoryNetwork", "Items"),
	HEAT("HeatNetwork", "Heat");
	
	private String name;
	private String transmission;
	
	private TransmissionType(String n, String t)
	{
		name = n;
		transmission = t;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getTransmission()
	{
		return transmission;
	}
	
	public String localize()
	{
		return MekanismUtils.localize("transmission." + getTransmission());
	}

	public static boolean checkTransmissionType(TileEntity sideTile, TransmissionType type)
	{
		return checkTransmissionType(sideTile, type, null);
	}

	public static boolean checkTransmissionType(TileEntity sideTile, TransmissionType type, TileEntity currentPipe)
	{
		return type.checkTransmissionType(sideTile, currentPipe);
	}

	public boolean checkTransmissionType(TileEntity sideTile, TileEntity currentTile)
	{
		if(sideTile instanceof ITransmitter)
		{
			if(((ITransmitter)sideTile).getTransmissionType() == this)
			{
				return true;
			}
		}

		if(this == GAS && currentTile instanceof IGasTransmitter)
		{
			if(((IGasTransmitter)currentTile).canTransferGasToTube(sideTile))
			{
				return true;
			}
		}

		return false;
	}
}