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
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import static java.lang.Math.min;
import static java.lang.Math.max;

public class FusionReactor implements IFusionReactor
{
	public TileEntityReactorController controller;
	public Set<IReactorBlock> reactorBlocks = new HashSet<IReactorBlock>();
	public Set<INeutronCapture> neutronCaptors = new HashSet<INeutronCapture>();

	//Current stores of temperature
	public double plasmaTemperature;
	public double caseTemperature;

	//Last values of temperature
	public double lastPlasmaTemperature;
	public double lastCaseTemperature;

	//Reaction characteristics
	public static double burnTemperature = 1E8;
	public static double burnRatio = 1;
	public static double energyPerFuel = 5E6;
	public int injectionRate = 0;

	//Thermal characteristics
	public static double plasmaHeatCapacity = 100;
	public static double caseHeatCapacity = 1;
	public static double enthalpyOfVaporization = 10;
	public static double thermocoupleEfficiency = 0.001;
	public static double steamTransferEfficiency = 0.01;

	//Heat transfer metrics
	public static double plasmaCaseConductivity = 0.2;
	public static double caseWaterConductivity = 0.3;
	public static double caseAirConductivity = 0.1;

	public boolean burning = false;
	public boolean hasHohlraum = false;
	public boolean activelyCooled = true;

	public boolean formed = false;

	public FusionReactor(TileEntityReactorController c)
	{
		controller = c;
	}

	@Override
	public void addTemperatureFromEnergyInput(double energyAdded)
	{
		plasmaTemperature += energyAdded / plasmaHeatCapacity;
	}

	@Override
	public void simulate()
	{
		//Only thermal transfer happens unless we're hot enough to burn.
		if(plasmaTemperature >= burnTemperature)
		{
			//If we're not burning yet we need a hohlraum to ignite
			if(!burning && hasHohlraum)
			{
				vaporiseHohlraum();
			}
			//Only inject fuel if we're burning
			if(burning)
			{
				injectFuel();

				int fuelBurned = burnFuel();
				neutronFlux(fuelBurned);
			}
		}
		else {
			burning = false;
		}

		//Perform the heat transfer calculations
		transferHeat();

		lastPlasmaTemperature = plasmaTemperature;
		lastCaseTemperature = caseTemperature;

		if(plasmaTemperature > 1E-6 || caseTemperature > 1E-6)
		{
			Mekanism.logger.info("Reactor temperatures: Plasma: " + (int) plasmaTemperature + ",			Casing: " + (int) caseTemperature);
		}
	}

	public void vaporiseHohlraum()
	{
		getFuelTank().receive(new GasStack(GasRegistry.getGas("fusionFuelDT"), 10), true);
		hasHohlraum = false;
		burning = true;
	}

	public void injectFuel()
	{
		int amountNeeded = getFuelTank().getNeeded();
		int amountAvailable = 2*min(getDeuteriumTank().getStored(), getTritiumTank().getStored());
		int amountToInject = min(amountNeeded, min(amountAvailable, injectionRate));
		amountToInject -= amountToInject % 2;
		getDeuteriumTank().draw(amountToInject / 2, true);
		getTritiumTank().draw(amountToInject / 2, true);
		getFuelTank().receive(new GasStack(GasRegistry.getGas("fusionFuel"), amountToInject), true);
	}

	public int burnFuel()
	{
		int fuelBurned = (int)min(getFuelTank().getStored(), max(0, lastPlasmaTemperature - burnTemperature)*burnRatio);
		getFuelTank().draw(fuelBurned, true);
		plasmaTemperature += energyPerFuel * fuelBurned / plasmaHeatCapacity;
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

	public void transferHeat()
	{
		//Transfer from plasma to casing
		double plasmaCaseHeat = plasmaCaseConductivity * (lastPlasmaTemperature - lastCaseTemperature);
		plasmaTemperature -= plasmaCaseHeat / plasmaHeatCapacity;
		caseTemperature += plasmaCaseHeat / caseHeatCapacity;

		//Transfer from casing to water if necessary
		if(activelyCooled)
		{
			double caseWaterHeat = caseWaterConductivity * lastCaseTemperature;
			int waterToVaporize = (int)(steamTransferEfficiency * caseWaterHeat / enthalpyOfVaporization);
			//Mekanism.logger.info("Wanting to vaporise " + waterToVaporize + "mB of water");
			waterToVaporize = min(waterToVaporize, min(getWaterTank().getFluidAmount(), getSteamTank().getCapacity() - getSteamTank().getFluidAmount()));
			if(waterToVaporize > 0)
			{
				//Mekanism.logger.info("Vaporising " + waterToVaporize + "mB of water");
				getWaterTank().drain(waterToVaporize, true);
				getSteamTank().fill(new FluidStack(FluidRegistry.getFluid("steam"), waterToVaporize), true);
			}
			caseWaterHeat = waterToVaporize * enthalpyOfVaporization / steamTransferEfficiency;
			caseTemperature -= caseWaterHeat / caseHeatCapacity;
		}

		//Transfer from casing to environment
		double caseAirHeat = caseAirConductivity * lastCaseTemperature;
		caseTemperature -= caseAirHeat / caseHeatCapacity;
		setBufferedEnergy(getBufferedEnergy() + caseAirHeat * thermocoupleEfficiency);
	}

	@Override
	public FluidTank getWaterTank()
	{
		return controller != null ? controller.waterTank : null;
	}

	@Override
	public FluidTank getSteamTank()
	{
		return controller.steamTank;
	}

	@Override
	public GasTank getDeuteriumTank()
	{
		return controller.deuteriumTank;
	}

	@Override
	public GasTank getTritiumTank()
	{
		return controller.tritiumTank;
	}

	@Override
	public GasTank getFuelTank()
	{
		return controller.fuelTank;
	}

	@Override
	public double getBufferedEnergy()
	{
		return controller.getEnergy();
	}

	@Override
	public void setBufferedEnergy(double energy)
	{
		controller.setEnergy(energy);
	}

	@Override
	public double getBufferSize()
	{
		return controller.getMaxEnergy();
	}

	public void unformMultiblock()
	{
		for(IReactorBlock block: reactorBlocks)
		{
			block.setReactor(null);
		}
		//Don't remove from controller
		controller.setReactor(this);
		reactorBlocks.clear();
		neutronCaptors.clear();
		formed = false;
	}

	@Override
	public void formMultiblock()
	{
		Mekanism.logger.trace("Attempting to form multiblock");

		Coord4D controllerPosition = Coord4D.get(controller);
		Coord4D centreOfReactor = controllerPosition.getFromSide(ForgeDirection.DOWN, 2);

		unformMultiblock();

		reactorBlocks.add(controller);

		Mekanism.logger.trace("Centre at " + centreOfReactor.toString());
		if(!createFrame(centreOfReactor))
		{
			unformMultiblock();
			Mekanism.logger.trace("Reactor failed: Frame not complete.");
			return;
		}
		Mekanism.logger.trace("Frame valid");
		if(!addSides(centreOfReactor))
		{
			unformMultiblock();
			Mekanism.logger.trace("Reactor failed: Sides not complete.");
			return;
		}
		Mekanism.logger.trace("Side Blocks Valid");
		if(!centreIsClear(centreOfReactor))
		{
			unformMultiblock();
			Mekanism.logger.trace("Blocks in chamber.");
			return;
		}
		Mekanism.logger.trace("Centre is clear");
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
				{+0, +0, -2}, {+1, +0, -2}, {+0, +1, -2}, {-1, +0, -2}, {+0, -1, -2}, //NORTH
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
