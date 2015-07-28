package mekanism.generators.common.tile;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mekanism.api.Coord4D;
import mekanism.api.MekanismConfig.generators;
import mekanism.common.base.IBoundingBlock;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityWindTurbine extends TileEntityGenerator implements IBoundingBlock
{
	/** The angle the blades of this Wind Turbine are currently at. */
	public double angle;

	public TileEntityWindTurbine()
	{
		super("wind", "WindTurbine", 200000, (generators.windGenerationMax)*2);
		inventory = new ItemStack[1];
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if(!worldObj.isRemote)
		{
			ChargeUtils.charge(0, this);
			
			if(canOperate())
			{
				setActive(true);
				setEnergy(electricityStored + (generators.windGenerationMin*getMultiplier()));
			}
			else {
				setActive(false);
			}
		}
	}

	/** Determines the current output multiplier, taking sky visibility and height into account. **/
	public float getMultiplier()
	{
		if(worldObj.canBlockSeeTheSky(xCoord, yCoord+4, zCoord)) 
		{
			final float minY = (float)generators.windGenerationMinY;
			final float maxY = (float)generators.windGenerationMaxY;
			final float minG = (float)generators.windGenerationMin;
			final float maxG = (float)generators.windGenerationMax;

			final float slope = (maxG - minG) / (maxY - minY);
			final float intercept = minG - slope * minY;

			final float clampedY = Math.min(maxY, Math.max(minY, (float)(yCoord+4)));
			final float toGen = slope * clampedY + intercept;

			return toGen / minG;
		} 
		else {
			return 0;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getVolume()
	{
		return 1.5F;
	}

    private static final String[] methods = new String[] {"getStored", "getOutput", "getMaxEnergy", "getEnergyNeeded", "getMultiplier"};

	@Override
	public String[] getMethods()
	{
		return methods;
	}

	@Override
	public Object[] invoke(int method, Object[] arguments) throws Exception
	{
		switch(method)
		{
			case 0:
				return new Object[] {electricityStored};
			case 1:
				return new Object[] {output};
			case 2:
				return new Object[] {BASE_MAX_ENERGY};
			case 3:
				return new Object[] {(BASE_MAX_ENERGY -electricityStored)};
			case 4:
				return new Object[] {getMultiplier()};
			default:
				throw new NoSuchMethodException();
		}
	}

	@Override
	public boolean canOperate()
	{
		return electricityStored < BASE_MAX_ENERGY && getMultiplier() > 0 && MekanismUtils.canFunction(this);
	}

	@Override
	public void onPlace()
	{
		Coord4D pos = Coord4D.get(this);
		MekanismUtils.makeBoundingBlock(worldObj, pos.getFromSide(ForgeDirection.UP, 1), pos);
		MekanismUtils.makeBoundingBlock(worldObj, pos.getFromSide(ForgeDirection.UP, 2), pos);
		MekanismUtils.makeBoundingBlock(worldObj, pos.getFromSide(ForgeDirection.UP, 3), pos);
		MekanismUtils.makeBoundingBlock(worldObj, pos.getFromSide(ForgeDirection.UP, 4), pos);
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

	@Override
	public boolean renderUpdate()
	{
		return false;
	}

	@Override
	public boolean lightUpdate()
	{
		return false;
	}
}
