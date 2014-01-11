package mekanism.common.tile;

import java.rmi.registry.Registry;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.tile.TileEntityAdvancedSolarGenerator;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidTank;

public class TileEntitySalinationController extends TileEntityContainerBlock
{
	public static int MAX_WATER = 100000;
	public static int MAX_BRINE = 1000;

	public FluidTank waterTank = new FluidTank(MAX_WATER);

	public FluidTank brineTank = new FluidTank(MAX_BRINE);

	public boolean temperatureSet = false;
	public double partialWater = 0;
	public double partialBrine = 0;
	public float temperature = 0;

	public TileEntitySalinationController()
	{
		super("SalinationController");
	}

	@Override
	public void onUpdate()
	{
		buildStructure();
		setTemperature();

		if(canOperate())
		{
			partialWater += temperature;
			if(partialWater >= 1)
			{
				int waterInt = (int)Math.floor(partialWater);
				waterTank.drain(waterInt, true);
				partialWater %= 1;
				partialBrine += ((double)waterInt)/100D;
			}
			if(partialBrine >= 1)
			{
				int brineInt = (int)Math.floor(partialBrine);
				brineTank.fill(FluidRegistry.getFluidStack("brine", brineInt), true);
				partialBrine %= 1;
			}
		}
	}

	public boolean canOperate()
	{
		if(waterTank.getFluid() == null || !waterTank.getFluid().containsFluid(FluidRegistry.getFluidStack("water", 100)))
		{
			return false;
		}

		TileEntity backTile = Coord4D.get(this).getFromSide(ForgeDirection.getOrientation(facing).getOpposite()).getTileEntity(worldObj);
		if(backTile instanceof TileEntityAdvancedSolarGenerator)
		{
			TileEntityAdvancedSolarGenerator heater = (TileEntityAdvancedSolarGenerator)backTile;
			return heater.seesSun;
		}
		return false;
	}

	public void setTemperature()
	{
		if(!temperatureSet)
		{
			temperature = worldObj.getBiomeGenForCoordsBody(xCoord, zCoord).getFloatTemperature();
			temperatureSet = true;
		}
	}

	public void buildStructure()
	{
		TileEntity leftTile = Coord4D.get(this).getFromSide(MekanismUtils.getLeft(facing)).getTileEntity(worldObj);
		TileEntity rightTile = Coord4D.get(this).getFromSide(MekanismUtils.getRight(facing)).getTileEntity(worldObj);
		if(leftTile instanceof TileEntitySalinationValve)
		{
			((TileEntitySalinationValve)leftTile).master = this;
		}
		if(rightTile instanceof TileEntitySalinationValve)
		{
			((TileEntitySalinationValve)rightTile).master = this;
		}
	}
}
