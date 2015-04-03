package mekanism.generators.common;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.IHeatTransfer;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.api.reactor.IFusionReactor;
import mekanism.api.reactor.INeutronCapture;
import mekanism.api.reactor.IReactorBlock;
import mekanism.api.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.generators.common.item.ItemHohlraum;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemCoal;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

public class FusionReactor implements IFusionReactor
{
	public TileEntityReactorController controller;
	public Set<IReactorBlock> reactorBlocks = new HashSet<IReactorBlock>();
	public Set<INeutronCapture> neutronCaptors = new HashSet<INeutronCapture>();
	public Set<IHeatTransfer> heatTransfers = new HashSet<IHeatTransfer>();

	//Current stores of temperature - internally uses ambient-relative kelvin units
	public double plasmaTemperature;
	public double caseTemperature;

	//Last values of temperature
	public double lastPlasmaTemperature;
	public double lastCaseTemperature;

	public double heatToAbsorb = 0;

	//Reaction characteristics
	public static double burnTemperature = TemperatureUnit.AMBIENT.convertFromK(1E8);
	public static double burnRatio = 1;
	public static double energyPerFuel = 5E6;
	public int injectionRate = 0;

	//Thermal characteristics
	public static double plasmaHeatCapacity = 100;
	public static double caseHeatCapacity = 1;
	public static double enthalpyOfVaporization = 10;
	public static double thermocoupleEfficiency = 0.05;
	public static double steamTransferEfficiency = 0.1;

	//Heat transfer metrics
	public static double plasmaCaseConductivity = 0.2;
	public static double caseWaterConductivity = 0.3;
	public static double caseAirConductivity = 0.1;

	public boolean burning = false;
	public boolean activelyCooled = true;
	
	public boolean updatedThisTick;

	public boolean formed = false;

	public FusionReactor(TileEntityReactorController c)
	{
		controller = c;
	}

	@Override
	public void addTemperatureFromEnergyInput(double energyAdded)
	{
		plasmaTemperature += energyAdded / plasmaHeatCapacity * (isBurning() ? 1 : 10);
	}

	public boolean hasHohlraum()
	{
		if(controller != null)
		{
			ItemStack hohlraum = controller.inventory[0];
			
			if(hohlraum != null && hohlraum.getItem() instanceof ItemHohlraum)
			{
				GasStack gasStack = ((ItemHohlraum)hohlraum.getItem()).getGas(hohlraum);
				return gasStack != null && gasStack.getGas() == GasRegistry.getGas("fusionFuelDT") && gasStack.amount == ItemHohlraum.MAX_GAS;
			}
		}

		return false;
	}

	@Override
	public void simulate()
	{
		if(controller.getWorldObj().isRemote)
		{
			lastPlasmaTemperature = plasmaTemperature;
			lastCaseTemperature = caseTemperature;
			
			return;
		}
		
		updatedThisTick = false;
		
		//Only thermal transfer happens unless we're hot enough to burn.
		if(plasmaTemperature >= burnTemperature)
		{
			//If we're not burning yet we need a hohlraum to ignite
			if(!burning && hasHohlraum())
			{
				vaporiseHohlraum();
			}
			
			//Only inject fuel if we're burning
			if(burning)
			{
				injectFuel();
				int fuelBurned = burnFuel();
				neutronFlux(fuelBurned);
				
				if(fuelBurned == 0)
				{
					burning = false;
				}
			}
		}
		else {
			burning = false;
		}

		//Perform the heat transfer calculations
		transferHeat();

		if(burning)
		{
			kill();
		}

		updateTemperatures();
	}

	@Override
	public void updateTemperatures()
	{
		lastPlasmaTemperature = plasmaTemperature < 1E-1 ? 0 : plasmaTemperature;
		lastCaseTemperature = caseTemperature < 1E-1 ? 0 : caseTemperature;
	}

	public void vaporiseHohlraum()
	{
		getFuelTank().receive(((ItemHohlraum)controller.inventory[0].getItem()).getGas(controller.inventory[0]), true);
		lastPlasmaTemperature = plasmaTemperature;

		controller.inventory[0] = null;

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
		getFuelTank().receive(new GasStack(GasRegistry.getGas("fusionFuelDT"), amountToInject), true);
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
			{
				break;
			}

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
			waterToVaporize = min(waterToVaporize, min(getWaterTank().getFluidAmount(), getSteamTank().getCapacity() - getSteamTank().getFluidAmount()));
			
			if(waterToVaporize > 0)
			{
				getWaterTank().drain(waterToVaporize, true);
				getSteamTank().fill(new FluidStack(FluidRegistry.getFluid("steam"), waterToVaporize), true);
			}
			
			caseWaterHeat = waterToVaporize * enthalpyOfVaporization / steamTransferEfficiency;
			caseTemperature -= caseWaterHeat / caseHeatCapacity;

			for(IHeatTransfer source : heatTransfers)
			{
				source.simulateHeat();
			}
			
			applyTemperatureChange();
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
	public double getPlasmaTemp()
	{
		return lastPlasmaTemperature;
	}

	@Override
	public void setPlasmaTemp(double temp)
	{
		plasmaTemperature = temp;
	}

	@Override
	public double getCaseTemp()
	{
		return lastCaseTemperature;
	}

	@Override
	public void setCaseTemp(double temp)
	{
		caseTemperature = temp;
	}

	@Override
	public double getBufferSize()
	{
		return controller.getMaxEnergy();
	}

	public void kill()
	{
		AxisAlignedBB death_zone = AxisAlignedBB.getBoundingBox(controller.xCoord - 1, controller.yCoord - 3, controller.zCoord - 1 ,controller.xCoord + 2, controller.yCoord, controller.zCoord + 2);
		List<Entity> entitiesToDie = controller.getWorldObj().getEntitiesWithinAABB(Entity.class, death_zone);
		
		for(Entity entity : entitiesToDie)
		{
			entity.attackEntityFrom(DamageSource.magic, 50000F);
		}
	}

	public void unformMultiblock(boolean keepBurning)
	{
		for(IReactorBlock block : reactorBlocks)
		{
			block.setReactor(null);
		}
		
		//Don't remove from controller
		controller.setReactor(this);
		reactorBlocks.clear();
		neutronCaptors.clear();
		formed = false;
		burning = burning && keepBurning;
		
		if(!controller.getWorldObj().isRemote)
		{
			Mekanism.packetHandler.sendToDimension(new TileEntityMessage(Coord4D.get(controller), controller.getNetworkedData(new ArrayList())), controller.getWorldObj().provider.dimensionId);
		}
	}

	@Override
	public void formMultiblock()
	{
		updatedThisTick = true;

		Coord4D controllerPosition = Coord4D.get(controller);
		Coord4D centreOfReactor = controllerPosition.getFromSide(ForgeDirection.DOWN, 2);

		unformMultiblock(true);

		reactorBlocks.add(controller);

		if(!createFrame(centreOfReactor))
		{
			unformMultiblock(false);
			return;
		}
		
		if(!addSides(centreOfReactor))
		{
			unformMultiblock(false);
			return;
		}
		
		if(!centreIsClear(centreOfReactor))
		{
			unformMultiblock(false);
			return;
		}
		
		formed = true;
		
		if(!controller.getWorldObj().isRemote)
		{
			Mekanism.packetHandler.sendToDimension(new TileEntityMessage(Coord4D.get(controller), controller.getNetworkedData(new ArrayList())), controller.getWorldObj().provider.dimensionId);
		}
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
				if(tile instanceof IHeatTransfer)
				{
					heatTransfers.add((IHeatTransfer)tile);
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

	@Override
	public boolean isFormed()
	{
		return formed;
	}

	@Override
	public void setInjectionRate(int rate)
	{
		injectionRate = rate;
	}

	@Override
	public int getInjectionRate()
	{
		return injectionRate;
	}

	@Override
	public boolean isBurning()
	{
		return burning;
	}

	@Override
	public void setBurning(boolean burn)
	{
		burning = burn;
	}

	@Override
	public int getMinInjectionRate(boolean active)
	{
		double k = active ? caseWaterConductivity : 0;
		double aMin = burnTemperature * burnRatio * plasmaCaseConductivity * (k+caseAirConductivity) / (energyPerFuel * burnRatio * (plasmaCaseConductivity+k+caseAirConductivity) - plasmaCaseConductivity * (k + caseAirConductivity));
		return (int)(2 * Math.ceil(aMin/2D));
	}

	@Override
	public double getMaxPlasmaTemperature(boolean active)
	{
		double k = active ? caseWaterConductivity : 0;
		return injectionRate * energyPerFuel/plasmaCaseConductivity * (plasmaCaseConductivity+k+caseAirConductivity) / (k+caseAirConductivity);
	}

	@Override
	public double getMaxCasingTemperature(boolean active)
	{
		double k = active ? caseWaterConductivity : 0;
		return injectionRate * energyPerFuel / (k+caseAirConductivity);
	}

	@Override
	public double getIgnitionTemperature(boolean active)
	{
		double k = active ? caseWaterConductivity : 0;
		return burnTemperature * energyPerFuel * burnRatio * (plasmaCaseConductivity+k+caseAirConductivity) / (energyPerFuel * burnRatio * (plasmaCaseConductivity+k+caseAirConductivity) - plasmaCaseConductivity * (k + caseAirConductivity));
	}

	@Override
	public double getPassiveGeneration(boolean active, boolean current)
	{
		double temperature = current ? caseTemperature : getMaxCasingTemperature(active);

		return thermocoupleEfficiency * caseAirConductivity * temperature;
	}

	@Override
	public int getSteamPerTick(boolean current)
	{
		double temperature = current ? caseTemperature : getMaxCasingTemperature(true);

		return (int)(steamTransferEfficiency * caseWaterConductivity * temperature / enthalpyOfVaporization);
	}

	@Override
	public double getTemp()
	{
		return lastCaseTemperature;
	}

	@Override
	public double getInverseConductionCoefficient()
	{
		return 1 / caseAirConductivity;
	}

	@Override
	public double getInsulationCoefficient(ForgeDirection side)
	{
		return 100000;
	}

	@Override
	public void transferHeatTo(double heat)
	{
		heatToAbsorb += heat;
	}

	@Override
	public double[] simulateHeat()
	{
		return null;
	}

	@Override
	public double applyTemperatureChange()
	{
		caseTemperature += heatToAbsorb / caseHeatCapacity;
		heatToAbsorb = 0;

		return caseTemperature;
	}

	@Override
	public boolean canConnectHeat(ForgeDirection side)
	{
		return false;
	}

	@Override
	public IHeatTransfer getAdjacent(ForgeDirection side)
	{
		return null;
	}
	
	@Override
	public ItemStack[] getInventory()
	{
		return isFormed() ? controller.inventory : null;
	}
}
