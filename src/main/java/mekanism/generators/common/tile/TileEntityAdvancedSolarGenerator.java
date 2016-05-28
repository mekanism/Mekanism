package mekanism.generators.common.tile;

import java.util.EnumSet;

import mekanism.api.Coord4D;
import mekanism.api.IEvaporationSolar;
import mekanism.api.MekanismConfig.generators;
import mekanism.common.base.IBoundingBlock;
import mekanism.common.util.MekanismUtils;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityAdvancedSolarGenerator extends TileEntitySolarGenerator implements IBoundingBlock, IEvaporationSolar
{
	public TileEntityAdvancedSolarGenerator()
	{
		super("AdvancedSolarGenerator", 200000, generators.advancedSolarGeneration*2);
		GENERATION_RATE = generators.advancedSolarGeneration;
	}

	@Override
	public EnumSet<ForgeDirection> getOutputtingSides()
	{
		return EnumSet.of(ForgeDirection.getOrientation(facing));
	}

	@Override
	public void onPlace()
	{
		MekanismUtils.makeBoundingBlock(worldObj, new Coord4D(xCoord, yCoord+1, zCoord, worldObj.provider.dimensionId), Coord4D.get(this));

		for(int x = -1; x <= 1; x++)
		{
			for(int z = -1; z <= 1; z++)
			{
				MekanismUtils.makeBoundingBlock(worldObj, new Coord4D(xCoord+x, yCoord+2, zCoord+z, worldObj.provider.dimensionId), Coord4D.get(this));
			}
		}
	}

	@Override
	public void onBreak()
	{
		worldObj.setBlockToAir(xCoord, yCoord+1, zCoord);

		for(int x = -1; x <= 1; x++)
		{
			for(int z = -1; z <= 1; z++)
			{
				worldObj.setBlockToAir(xCoord+x, yCoord+2, zCoord+z);
			}
		}

		invalidate();
		worldObj.setBlockToAir(xCoord, yCoord, zCoord);
	}

	@Override
	public boolean seesSun()
	{
		return seesSun;
	}
}
