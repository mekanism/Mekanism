package mekanism.api.transmitters;

import mekanism.api.gas.IGasTransmitter;
import net.minecraft.tileentity.TileEntity;

public enum TransmissionType
{
	ENERGY(Size.SMALL),
	FLUID(Size.LARGE),
	GAS(Size.SMALL),
    ITEM(Size.LARGE);

    public Size transmitterSize;

    private TransmissionType(Size size)
    {
        transmitterSize = size;
    }

	public static TransmissionType[] metaArray = {GAS, ENERGY, FLUID, ITEM};

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
    		if(((ITransmitter<?>)sideTile).getTransmissionType() == this)
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

	public static TransmissionType fromOldMeta(int meta)
	{
		if(meta <= metaArray.length)
			return metaArray[meta];
		return null;
	}

    public static enum Size
    {
        SMALL(6),
        LARGE(8);

        public int centreSize;

        private Size(int size) {
            centreSize = size;
        }
    }

}