package mekanism.generators.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.api.reactor.IFusionReactor;
import mekanism.api.reactor.INeutronCapture;
import mekanism.api.reactor.IReactorBlock;
import mekanism.common.Mekanism;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidTank;

import static java.lang.Math.exp;
import static java.lang.Math.min;
import static java.lang.Math.max;

public class FusionReactor implements IFusionReactor
{
	public static final int MAX_WATER = 100 * FluidContainerRegistry.BUCKET_VOLUME;

	public static final int MAX_FUEL = 100 * FluidContainerRegistry.BUCKET_VOLUME;

	public FluidTank waterTank = new FluidTank(MAX_WATER);
	public FluidTank steamTank = new FluidTank(MAX_WATER*1000);

	public GasTank deuteriumTank = new GasTank(MAX_FUEL);
	public GasTank tritiumTank = new GasTank(MAX_FUEL);

	public GasTank fuelTank = new GasTank(MAX_FUEL);

	public TileEntityReactorController controller;
	public Set<IReactorBlock> reactorBlocks = new HashSet<IReactorBlock>();
	public Set<INeutronCapture> neutronCaptors = new HashSet<INeutronCapture>();

	public double temperature;
	public static double burnTemperature = 1E8;

	public static double burnRatio = 1;
	public static double tempPerFuel = 5E6;
	public int injectionRate = 0;
	
	public static double coolingCoefficient = 0.1;
	
	public static double waterRatio = 1E-14;
	public static double inverseHeatCapacity = 1;

	public boolean burning = false;
	public boolean hasHohlraum = false;

	public boolean formed = false;

	public FusionReactor(TileEntityReactorController c)
	{
		controller = c;
	}

	@Override
	public void addTemperatureFromEnergyInput(double energyAdded)
	{
		temperature += energyAdded * inverseHeatCapacity;
	}

	@Override
	public void simulate()
	{
		if(temperature >= burnTemperature)
		{
			if(!burning && hasHohlraum)
			{
				vaporiseHohlraum();
			}
			injectFuel();

			int fuelBurned = burnFuel();
			neutronFlux(fuelBurned);
		}
		else {
			burning = false;
		}
		boilWater();
		ambientLoss();
		if(temperature > 0)
			Mekanism.logger.info("Reactor temperature: " + (int)temperature);
	}

	public void vaporiseHohlraum()
	{
		fuelTank.receive(new GasStack(GasRegistry.getGas("fusionFuel"), 1000), true);
		burning = true;
	}

	public void injectFuel()
	{
		int amountNeeded = fuelTank.getNeeded();
		int amountAvailable = 2*min(deuteriumTank.getStored(), tritiumTank.getStored());
		int amountToInject = min(amountNeeded, min(amountAvailable, injectionRate));
		amountToInject -= amountToInject % 2;
		deuteriumTank.draw(amountToInject/2, true);
		tritiumTank.draw(amountToInject/2, true);
		fuelTank.receive(new GasStack(GasRegistry.getGas("fusionFuel"), amountToInject), true);
	}

	public int burnFuel()
	{
		int fuelBurned = (int)min(fuelTank.getStored(), max(0, temperature-burnTemperature)*burnRatio);
		fuelTank.draw(fuelBurned, true);
		temperature += tempPerFuel * fuelBurned;
		return fuelBurned;
	}

	public void neutronFlux(int fuelBurned)
	{
		int neutronsRemaining = fuelBurned;
		List<INeutronCapture> list = new ArrayList<INeutronCapture>(neutronCaptors);
		Collections.shuffle(list);
		for(INeutronCapture captor: neutronCaptors)
		{
			if(neutronsRemaining <= 0)
				break;

			neutronsRemaining = captor.absorbNeutrons(neutronsRemaining);
		}
		controller.radiateNeutrons(neutronsRemaining);
	}

	public void boilWater()
	{
		int waterToBoil = (int)min(waterTank.getFluidAmount(), temperature*temperature*waterRatio);
	}

	public void ambientLoss()
	{
		temperature -= coolingCoefficient*temperature;
		if(temperature < 1E-6)
		{
			temperature = 0;
		}
	}

	@Override
	public FluidTank getWaterTank()
	{
		return waterTank;
	}

	@Override
	public FluidTank getSteamTank()
	{
		return steamTank;
	}

	@Override
	public GasTank getDeuteriumTank()
	{
		return deuteriumTank;
	}

	@Override
	public GasTank getTritiumTank()
	{
		return tritiumTank;
	}

	@Override
	public GasTank getFuelTank()
	{
		return fuelTank;
	}

	public void formMultiblock()
	{
		Coord4D controllerPosition = Coord4D.get(controller);
		Coord4D centreOfReactor = controllerPosition.getFromSide(ForgeDirection.DOWN, 2);

		Mekanism.logger.info("Centre at " + centreOfReactor.toString());
		if(!createFrame(centreOfReactor))
		{
			for(IReactorBlock block: reactorBlocks)
			{
				block.setReactor(null);
			}
			reactorBlocks.clear();
			return;
		}
		Mekanism.logger.info("Frame valid");
		if(!addSides(centreOfReactor))
		{
			for(IReactorBlock block: reactorBlocks)
			{
				block.setReactor(null);
			}
			reactorBlocks.clear();
			neutronCaptors.clear();
			return;
		}
		Mekanism.logger.info("Side Blocks Valid");
		if(!centreIsClear(centreOfReactor))
		{
			for(IReactorBlock block: reactorBlocks)
			{
				block.setReactor(null);
			}
			reactorBlocks.clear();
			neutronCaptors.clear();
			return;
		}
		Mekanism.logger.info("Centre is clear");
		formed = true;
	}

	public boolean createFrame(Coord4D centre)
	{
		int[][] positions = new int[][] {
				{+2, +2, +0}, {+2, +1, +1}, {+2, +0, +2}, {+2, -1, +1}, {+2, -2, +0}, {+2, -1, -1}, {+2, +0, -2}, {+2, +1, -1},
				{+1, +2, +1}, {+1, +1, +2}, {+1, -1, +2}, {+1, -2, +1}, {+1, -2, -1}, {+1, -1, -2}, {+1, +1, -2}, {+1, +2, -1},
				{+0, +2, +2}, {+0, -2, +2}, {+0, -2, -2}, {+0, +2, -2},
				{-1, +2, +1}, {-1, +1, +2}, {-1, -1, +2}, {-1, -2, +1}, {-1, -2, -1}, {-1, -1, -2}, {-1, +1, -2}, {-1, +2, -1},
				{-2, +2, +0}, {-2, +1, +1}, {-2, +0, +2}, {-2, -1, +1}, {-2, -2, +0}, {-2, -1, -1}, {-2, +0, -2}, {-2, +1, -1},
		};

		for(int[] coords : positions)
		{
			TileEntity tile = centre.clone().translate(coords[0], coords[1], coords[2]).getTileEntity(controller.getWorldObj());

			if(tile instanceof IReactorBlock && ((IReactorBlock)tile).isFrame())
			{
				reactorBlocks.add((IReactorBlock)tile);
				((IReactorBlock)tile).setReactor(this);
			}
			else {
				return false;
			}
		}

		return true;
	}

	public boolean addSides(Coord4D centre)
	{
		int[][] positions = new int[][] {
				{+2, +0, +0}, {+2, +1, +0}, {+2, +0, +1}, {+2, -1, +0}, {+2, +0, -1}, //EAST
				{-2, +0, +0}, {-2, +1, +0}, {-2, +0, +1}, {-2, -1, +0}, {-2, +0, -1}, //WEST
				{+0, +2, +0}, {+1, +2, +0}, {+0, +2, +1}, {-1, +2, +0}, {+0, +2, -1}, //TOP
				{+0, -2, +0}, {+1, -2, +0}, {+0, -2, +1}, {-1, -2, +0}, {+0, -2, -1}, //BOTTOM
				{+0, +0, +2}, {+1, +0, +2}, {+0, +1, +2}, {-1, +0, +2}, {+0, -1, +2}, //SOUTH
				{+0, +0, +2}, {+1, +0, +2}, {+0, +1, +2}, {-1, +0, +2}, {+0, -1, +2}, //NORTH
		};

		for(int[] coords : positions)
		{
			TileEntity tile = centre.clone().translate(coords[0], coords[1], coords[2]).getTileEntity(controller.getWorldObj());

			if(tile instanceof ILaserReceptor && !(coords[1] == 0 && (coords[0] == 0 || coords[2] == 0)))
			{
				return false;
			}

			if(tile instanceof IReactorBlock)
			{
				reactorBlocks.add((IReactorBlock)tile);
				((IReactorBlock)tile).setReactor(this);
				if(tile instanceof INeutronCapture)
				{
					neutronCaptors.add((INeutronCapture)tile);
				}
			}
			else {
				return false;
			}
		}

		return true;
	}

	public boolean centreIsClear(Coord4D centre)
	{
		for(int x = -1; x <= 1; x++)
		{
			for(int y = -1; x <= 1; x++)
			{
				for(int z = -1; x <= 1; x++)
				{
					Block tile = centre.clone().translate(x, y, z).getBlock(controller.getWorldObj());

					if(!tile.isAir(controller.getWorldObj(), x, y, z))
					{
						return false;
					}
				}
			}
		}

		return true;
	}
}
