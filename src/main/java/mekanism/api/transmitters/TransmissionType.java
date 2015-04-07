package mekanism.api.transmitters;

import mekanism.api.gas.IGasTransmitter;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;

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
		return StatCollector.translateToLocal("transmission." + getTransmission());
	}

	public static boolean checkTransmissionType(ITransmitter sideTile, TransmissionType type)
	{
		return type.checkTransmissionType(sideTile);
	}

	public static boolean checkTransmissionType(TileEntity tile1, TransmissionType type)
	{
		return checkTransmissionType(tile1, type, null);
	}

	public static boolean checkTransmissionType(TileEntity tile1, TransmissionType type, TileEntity tile2)
	{
		return type.checkTransmissionType(tile1, tile2);
	}

	public boolean checkTransmissionType(ITransmitter transmitter)
	{
		return transmitter.getTransmissionType() == this;
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