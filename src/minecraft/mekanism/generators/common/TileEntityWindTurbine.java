package mekanism.generators.common;

import mekanism.api.Object3D;
import mekanism.common.ChargeUtils;
import mekanism.common.IBoundingBlock;
import mekanism.common.MekanismUtils;
import mekanism.common.TileEntityBoundingBlock;
import net.minecraft.item.ItemStack;
import dan200.computer.api.IComputerAccess;

public class TileEntityWindTurbine extends TileEntityGenerator implements IBoundingBlock
{
	public boolean canSpin;
	
	public int angle;
	
	public int GENERATION_RATE = 30;
	
	public TileEntityWindTurbine() 
	{
		super("Wind Turbine", 200000, 400);
		inventory = new ItemStack[1];
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		ChargeUtils.charge(0, this);
		
		if(!worldObj.isRemote)
		{
			if(canOperate())
			{
				setActive(true);
				setEnergy(electricityStored + (GENERATION_RATE*getMultiplier()));
			}
			else {
				setActive(false);
			}
		}
	}
	
	/** 0 - 8 **/
	public float getMultiplier()
	{
		return worldObj.canBlockSeeTheSky(xCoord, yCoord+4, zCoord) ? (((float)yCoord+4)/(float)256)*8 : 0;
	}

	@Override
	public String[] getMethodNames() 
	{
		return null;
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, int method, Object[] arguments) throws Exception 
	{
		return null;
	}

	@Override
	public int getEnvironmentBoost() 
	{
		return (int)getMultiplier();
	}

	@Override
	public boolean canOperate() 
	{
		return electricityStored < MAX_ELECTRICITY && getMultiplier() > 0 && checkBounds();
	}
	
	public boolean checkBounds()
	{
		Object3D obj = new Object3D(xCoord, yCoord+4, zCoord);
		
		for(int x = obj.xCoord-2; x <= obj.xCoord+2; x++)
		{
			for(int y = obj.yCoord-2; y <= obj.yCoord+2; y++)
			{
				for(int z = obj.zCoord-2; z <= obj.zCoord+2; z++)
				{
					if(worldObj.getBlockId(x, y, z) == 0)
					{
						continue;
					}
					
					if(worldObj.getBlockTileEntity(x, y, z) instanceof TileEntityBoundingBlock)
					{
						TileEntityBoundingBlock tileEntity = (TileEntityBoundingBlock)worldObj.getBlockTileEntity(x, y, z);
						
						if(tileEntity.mainX == xCoord && tileEntity.mainY == yCoord && tileEntity.mainZ == zCoord)
						{
							continue;
						}
					}
					
					return false;
				}
			}
		}
		
		return true;
	}

	@Override
	public void onPlace() 
	{
		MekanismUtils.makeBoundingBlock(worldObj, xCoord, yCoord+1, zCoord, Object3D.get(this));
		MekanismUtils.makeBoundingBlock(worldObj, xCoord, yCoord+2, zCoord, Object3D.get(this));
		MekanismUtils.makeBoundingBlock(worldObj, xCoord, yCoord+3, zCoord, Object3D.get(this));
		MekanismUtils.makeBoundingBlock(worldObj, xCoord, yCoord+4, zCoord, Object3D.get(this));
	}

	@Override
	public void onBreak() 
	{
		worldObj.setBlockToAir(xCoord, yCoord+1, zCoord);
		worldObj.setBlockToAir(xCoord, yCoord+2, zCoord);
		worldObj.setBlockToAir(xCoord, yCoord+3, zCoord);
		worldObj.setBlockToAir(xCoord, yCoord+4, zCoord);
		
		worldObj.setBlockToAir(xCoord, yCoord, zCoord);
	}
}
