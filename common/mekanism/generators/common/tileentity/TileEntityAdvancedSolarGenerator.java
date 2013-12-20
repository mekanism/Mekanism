package mekanism.generators.common.tileentity;

import java.util.EnumSet;

import mekanism.api.Coord4D;
import mekanism.common.IBoundingBlock;
import mekanism.common.Mekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.MekanismGenerators;
import net.minecraftforge.common.ForgeDirection;

public class TileEntityAdvancedSolarGenerator extends TileEntitySolarGenerator implements IBoundingBlock
{
	public TileEntityAdvancedSolarGenerator()
	{
		super("AdvancedSolarGenerator", 200000, 360, MekanismGenerators.advancedSolarGeneration*2);
		GENERATION_RATE = MekanismGenerators.advancedSolarGeneration;
	}
	
	@Override
	public EnumSet<ForgeDirection> getOutputtingSides()
	{
		return EnumSet.of(ForgeDirection.getOrientation(facing));
	}

	@Override
	public void onPlace() 
	{
		MekanismUtils.makeBoundingBlock(worldObj, xCoord, yCoord+1, zCoord, Coord4D.get(this));
		
		for(int x=-1; x<=1; x++)
		{
			for(int z=-1; z<=1; z++)
			{
				MekanismUtils.makeBoundingBlock(worldObj, xCoord+x, yCoord+2, zCoord+z, Coord4D.get(this));
			}
		}
	}

	@Override
	public void onBreak() 
	{
		worldObj.setBlockToAir(xCoord, yCoord+1, zCoord);
		
		for(int x=-1; x<=1; x++)
		{
			for(int z=-1; z<=1; z++)
			{
				worldObj.setBlockToAir(xCoord+x, yCoord+2, zCoord+z);
			}
		}
		
		worldObj.setBlockToAir(xCoord, yCoord, zCoord);
	}
}
