package mekanism.common;

import mekanism.api.Object3D;
import mekanism.common.SynchronizedTankData.ValveData;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.LiquidStack;

public class DynamicLiquidTank implements ILiquidTank
{
	public TileEntityDynamicTank dynamicTank;
	
	public DynamicLiquidTank(TileEntityDynamicTank tileEntity)
	{
		dynamicTank = tileEntity;
	}

	@Override
	public LiquidStack getLiquid() 
	{
		return dynamicTank.structure != null ? dynamicTank.structure.liquidStored : null;
	}

	@Override
	public int getCapacity()
	{
		return dynamicTank.structure != null ? dynamicTank.structure.volume*16000 : 0;
	}

    @Override
    public int fill(LiquidStack resource, boolean doFill)
    {
    	if(dynamicTank.structure != null && !dynamicTank.worldObj.isRemote)
    	{
	        if(resource == null || resource.itemID <= 0) 
        	{
	        	return 0;
        	}
	
	        if(dynamicTank.structure.liquidStored == null || dynamicTank.structure.liquidStored.itemID <= 0)
	        {
	            if(resource.amount <= getCapacity())
	            {
	                if(doFill)
	                {
	                	 dynamicTank.structure.liquidStored = resource.copy();
	                }
	                
	    	        if(resource.amount > 0 && doFill)
	    	        {
	    	        	updateValveData(true);
	    	        	dynamicTank.sendPacketToRenderer();
	    	        	updateValveData(false);
	    	        }
	                
	                return resource.amount;
	            }
	            else {
	                if(doFill)
	                {
	                    dynamicTank.structure.liquidStored = resource.copy();
	                    dynamicTank.structure.liquidStored.amount = getCapacity();
	                }
	                
	    	        if(getCapacity() > 0 && doFill)
	    	        {
	    	        	updateValveData(true);
	    	        	dynamicTank.sendPacketToRenderer();
	    	        	updateValveData(false);
	    	        }
	                
	                return getCapacity();
	            }
	        }
	
	        if(!dynamicTank.structure.liquidStored.isLiquidEqual(resource)) 
	        {
	        	return 0;
	        }
	
	        int space = getCapacity() - dynamicTank.structure.liquidStored.amount;
	        
	        if(resource.amount <= space)
	        {
	            if(doFill)
	            {
	            	dynamicTank.structure.liquidStored.amount += resource.amount;
	            }
	            
		        if(resource.amount > 0 && doFill)
		        {
		        	updateValveData(true);
    	        	dynamicTank.sendPacketToRenderer();
    	        	updateValveData(false);
		        }
	            
	            return resource.amount;
	        }
	        else {
	            if(doFill)
	            {
	            	dynamicTank.structure.liquidStored.amount = getCapacity();
	            }
	            
		        if(space > 0 && doFill)
		        {
		        	updateValveData(true);
    	        	dynamicTank.sendPacketToRenderer();
    	        	updateValveData(false);
		        }
	            
	            return space;
	        }
    	}
    	
    	return 0;
    }
    
    public void updateValveData(boolean value)
    {
    	if(dynamicTank.structure != null)
    	{
    		for(ValveData data : dynamicTank.structure.valves)
    		{
    			if(data.location.equals(Object3D.get(dynamicTank)))
    			{
    				data.serverLiquid = value;
    			}
    		}
    	}
    }

    @Override
    public LiquidStack drain(int maxDrain, boolean doDrain)
    {
    	if(dynamicTank.structure != null && !dynamicTank.worldObj.isRemote)
    	{
	        if(dynamicTank.structure.liquidStored == null || dynamicTank.structure.liquidStored.itemID <= 0)
        	{
	        	return null;
        	}
	        
	        if(dynamicTank.structure.liquidStored.amount <= 0)
	        {
	        	return null;
	        }
	
	        int used = maxDrain;
	        
	        if(dynamicTank.structure.liquidStored.amount < used)
        	{
	        	used = dynamicTank.structure.liquidStored.amount;
        	}
	
	        if(doDrain)
	        {
	        	dynamicTank.structure.liquidStored.amount -= used;
	        }
	
	        LiquidStack drained = new LiquidStack(dynamicTank.structure.liquidStored.itemID, used, dynamicTank.structure.liquidStored.itemMeta);
	
	        if(dynamicTank.structure.liquidStored.amount <= 0)
        	{
	        	dynamicTank.structure.liquidStored = null;
        	}
	        
	        if(drained.amount > 0 && doDrain)
	        {
	        	dynamicTank.sendPacketToRenderer();
	        }
	
	        return drained;
    	}
    	
    	return null;
    }

	@Override
	public int getTankPressure() 
	{
		return 0;
	}
}
