package mekanism.api;

import net.minecraft.tileentity.TileEntity;

public enum TransmissionType
{
	ENERGY,
	FLUID,
	GAS,
	ITEM;

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
        if (sideTile == null)
        {
            return false;
        }
        
    	if (sideTile instanceof ITransmitter)
    	{
    		if(((ITransmitter<?>)sideTile).getTransmissionType() != this)
    		{
    			return false;
    		}
    	}
    	
    	if (this == GAS && currentTile instanceof IGasTransmitter)
    	{
    	    if (!((IGasTransmitter)currentTile).canTransferGasToTube(sideTile))
    	    {
    	        return false;
    	    }
    	}
    	
    	return true;
    }
}
