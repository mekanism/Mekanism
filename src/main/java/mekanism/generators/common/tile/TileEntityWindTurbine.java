package mekanism.generators.common.tile;

import mekanism.api.Coord4D;
import mekanism.common.IBoundingBlock;
import mekanism.common.Mekanism;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.Optional.Method;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;

public class TileEntityWindTurbine extends TileEntityGenerator implements IBoundingBlock
{
	/** The angle the blades of this Wind Turbine are currently at. */
	public int angle;

	public TileEntityWindTurbine()
	{
		super("WindTurbine", 200000, (MekanismGenerators.windGeneration*8)*2);
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
				setEnergy(electricityStored + (MekanismGenerators.windGeneration*getMultiplier()));
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
	public float getVolumeMultiplier()
	{
		return 1.5F;
	}

	@Override
	@Method(modid = "ComputerCraft")
	public String[] getMethodNames()
	{
		return new String[] {"getStored", "getOutput", "getMaxEnergy", "getEnergyNeeded", "getMultiplier"};
	}

	@Override
	@Method(modid = "ComputerCraft")
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws Exception
	{
		switch(method)
		{
			case 0:
				return new Object[] {electricityStored};
			case 1:
				return new Object[] {output};
			case 2:
				return new Object[] {MAX_ELECTRICITY};
			case 3:
				return new Object[] {(MAX_ELECTRICITY-electricityStored)};
			case 4:
				return new Object[] {getMultiplier()};
			default:
				Mekanism.logger.error("Attempted to call unknown method with computer ID " + computer.getID());
				return null;
		}
	}

	@Override
	public boolean canOperate()
	{
		return electricityStored < MAX_ELECTRICITY && getMultiplier() > 0 && MekanismUtils.canFunction(this);
	}

	@Override
	public void onPlace()
	{
		MekanismUtils.makeBoundingBlock(worldObj, xCoord, yCoord+1, zCoord, Coord4D.get(this));
		MekanismUtils.makeBoundingBlock(worldObj, xCoord, yCoord+2, zCoord, Coord4D.get(this));
		MekanismUtils.makeBoundingBlock(worldObj, xCoord, yCoord+3, zCoord, Coord4D.get(this));
		MekanismUtils.makeBoundingBlock(worldObj, xCoord, yCoord+4, zCoord, Coord4D.get(this));
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
