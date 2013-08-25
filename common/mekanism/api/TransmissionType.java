package mekanism.api;

import net.minecraft.tileentity.TileEntity;

public enum TransmissionType
{
	ENERGY,
	FLUID,
	GAS,
	ITEM;
	
    public static boolean checkTransmissionType(TileEntity tileEntity, TransmissionType type)
    {
    	return type.checkTransmissionType(tileEntity);
    }
    
    public boolean checkTransmissionType(TileEntity tileEntity)
    {
    	if(tileEntity instanceof ITransmitter)
    	{
    		if(((ITransmitter<?>)tileEntity).getTransmissionType() == this)
    		{
    			return true;
    		}
    	}
    	
    	return false;
    }
}
