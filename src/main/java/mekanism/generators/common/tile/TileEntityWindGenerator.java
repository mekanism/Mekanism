package mekanism.generators.common.tile;

import buildcraft.api.core.Position;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.MekanismConfig.generators;
import mekanism.common.base.IBoundingBlock;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import rpcore.api.DimensionAPI;
import rpcore.constants.CelestialType;
import rpcore.module.dimension.ForgeDimension;

public class TileEntityWindGenerator extends TileEntityGenerator implements IBoundingBlock
{
	/** The angle the blades of this Wind Turbine are currently at. */
	public double angle;
	
	public float currentMultiplier;

	public TileEntityWindGenerator()
	{
		super("wind", "WindGenerator", 200000, (generators.windGenerationMax)*2);
		inventory = new ItemStack[1];
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if(!worldObj.isRemote)
		{
			ChargeUtils.charge(0, this);
			
                            ForgeDimension dim = ((ForgeDimension)DimensionAPI.getForgeDimension(this.worldObj.provider.dimensionId));
                            if (dim != null && (dim.getType().equals(CelestialType.System) || dim.getType().equals(CelestialType.Cluster)) && getActive()) {
                                setActive(false);
                            } else {
                                if(ticker % 20 == 0)
                                {
                                        setActive((currentMultiplier = getMultiplier()) > 0);
                                }

                                if(getActive())
                                {
                                        setEnergy(electricityStored + (generators.windGenerationMin*currentMultiplier));
                                }
                            }
		}
	}
	
	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		super.handlePacketData(dataStream);

		if(worldObj.isRemote)
		{
			currentMultiplier = dataStream.readFloat();
		}
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);

		data.add(currentMultiplier);

		return data;
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
		return 1.5F*super.getVolume();
	}

    private static final String[] methods = new String[] {"getEnergy", "getOutput", "getMaxEnergy", "getEnergyNeeded", "getMultiplier"};

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
