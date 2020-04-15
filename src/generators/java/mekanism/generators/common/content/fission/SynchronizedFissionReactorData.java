package mekanism.generators.common.content.fission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IMekanismGasHandler;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.chemical.MultiblockGasTank;
import mekanism.common.capabilities.fluid.MultiblockFluidTank;
import mekanism.common.capabilities.heat.ITileHeatHandler;
import mekanism.common.capabilities.heat.MultiblockHeatCapacitor;
import mekanism.common.multiblock.SynchronizedData;
import mekanism.common.registries.MekanismGases;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;

public class SynchronizedFissionReactorData extends SynchronizedData<SynchronizedFissionReactorData> implements IMekanismFluidHandler, IMekanismGasHandler,
      ITileHeatHandler {

    public static final double CASING_HEAT_CAPACITY = 5;

    public static final double INVERSE_INSULATION_COEFFICIENT = 100_000;
    public static final double INVERSE_CONDUCTION_COEFFICIENT = 10;

    public static final int WATER_PER_VOLUME = 100_000;
    public static final long STEAM_PER_VOLUME = 1_000_000;
    public static final long FUEL_PER_ASSEMBLY = 8_000;

    public static final double MIN_DAMAGE_TEMPERATURE = 1_200;
    public static final double MAX_DAMAGE_TEMPERATURE = 1_800;
    public static final double MAX_DAMAGE = 100;

    public static final long BURN_PER_ASSEMBLY = 1;

    public static final FloatingLong MAX_ENERGY = FloatingLong.create(1_000_000_000);

    public List<Coord4D> portLocations = new ArrayList<>();
    public int fuelAssemblies;

    public MultiblockFluidTank<TileEntityFissionReactorCasing> waterTank;
    public MultiblockGasTank<TileEntityFissionReactorCasing> fuelTank;

    public MultiblockGasTank<TileEntityFissionReactorCasing> steamTank;
    public MultiblockGasTank<TileEntityFissionReactorCasing> wasteTank;
    public MultiblockHeatCapacitor<TileEntityFissionReactorCasing> heatCapacitor;

    private List<IExtendedFluidTank> fluidTanks;
    private List<IChemicalTank<Gas, GasStack>> gasTanks;
    private List<IHeatCapacitor> heatCapacitors;

    private static double steamTransferEfficiency = 0.2;
    private static double waterConductivity = 0.3;

    public double lastEnvironmentLoss = 0, lastAdjacentLoss = 0;

    public double reactorDamage = 0;
    public boolean active;

    public SynchronizedFissionReactorData(TileEntityFissionReactorCasing tile) {
        waterTank = MultiblockFluidTank.input(tile, () -> tile.structure == null ? 0 : getVolume() * WATER_PER_VOLUME, fluid -> fluid.getFluid().isIn(FluidTags.WATER));
        fluidTanks = Collections.singletonList(waterTank);
        fuelTank = MultiblockGasTank.create(tile, () -> tile.structure == null ? 0 : fuelAssemblies * FUEL_PER_ASSEMBLY,
            (stack, automationType) -> automationType != AutomationType.EXTERNAL, (stack, automationType) -> tile.structure != null,
            gas -> gas == MekanismGases.FISSILE_FUEL.getGas(), ChemicalAttributeValidator.ALWAYS_ALLOW, null);
        steamTank = MultiblockGasTank.create(tile, () -> tile.structure == null ? 0 : getVolume() * STEAM_PER_VOLUME,
            (stack, automationType) -> tile.structure != null, (stack, automationType) -> automationType != AutomationType.EXTERNAL,
            gas -> gas == MekanismGases.STEAM.getGas());
        wasteTank = MultiblockGasTank.create(tile, () -> tile.structure == null ? 0 : fuelAssemblies * FUEL_PER_ASSEMBLY,
            (stack, automationType) -> tile.structure != null, (stack, automationType) -> automationType != AutomationType.EXTERNAL,
            gas -> gas == MekanismGases.NUCLEAR_WASTE.getGas(), ChemicalAttributeValidator.ALWAYS_ALLOW, null);
        gasTanks = Arrays.asList(fuelTank, steamTank, wasteTank);
        heatCapacitor = MultiblockHeatCapacitor.create(tile,
            CASING_HEAT_CAPACITY,
            () -> INVERSE_INSULATION_COEFFICIENT,
            () -> INVERSE_INSULATION_COEFFICIENT);
        heatCapacitors = Collections.singletonList(heatCapacitor);
    }

    public void handleDamage() {
        double temp = heatCapacitor.getTemperature();
        if (temp > MIN_DAMAGE_TEMPERATURE) {
            double damageRate = Math.min(temp, MAX_DAMAGE_TEMPERATURE) / (MIN_DAMAGE_TEMPERATURE * 10);
            reactorDamage += damageRate;
        } else {
            double repairRate = (MIN_DAMAGE_TEMPERATURE - temp) / (MIN_DAMAGE_TEMPERATURE * 20);
            reactorDamage = Math.max(0, reactorDamage - repairRate);
        }

        if (reactorDamage >= MAX_DAMAGE) {
            // meltdown scaling on stored fuel
        }
    }

    public void handleCoolant() {
        double temp = heatCapacitor.getTemperature();
        double caseWaterHeat = waterConductivity * (temp - HeatUtils.BASE_BOIL_TEMP) * heatCapacitor.getHeatCapacity();
        int waterToVaporize = (int) (steamTransferEfficiency * caseWaterHeat / HeatUtils.getVaporizationEnthalpy());
        waterToVaporize = Math.min(waterToVaporize, waterTank.getFluidAmount());
        if (waterToVaporize > 0) {
            if (waterTank.shrinkStack(waterToVaporize, Action.EXECUTE) != waterToVaporize) {
                MekanismUtils.logMismatchedStackSize();
            }
            // extra steam is dumped
            steamTank.insert(MekanismGases.STEAM.getGasStack(Math.min(waterToVaporize, steamTank.getNeeded())), Action.EXECUTE, AutomationType.INTERNAL);
            caseWaterHeat = waterToVaporize * HeatUtils.getVaporizationEnthalpy() / steamTransferEfficiency;
            heatCapacitor.handleHeat(-caseWaterHeat);
        }
    }

    public void burnFuel() {
        long toBurn = Math.min(fuelTank.getStored(), fuelAssemblies * BURN_PER_ASSEMBLY);
        fuelTank.shrinkStack(toBurn, Action.EXECUTE);
        heatCapacitor.handleHeat(toBurn * MekanismGeneratorsConfig.generators.energyPerFissionFuel.get().doubleValue());
        // handle waste
        long leftoverWaste = Math.max(0, toBurn - wasteTank.getNeeded());
        GasStack wasteToAdd = MekanismGases.NUCLEAR_WASTE.getGasStack(toBurn);
        wasteTank.insert(wasteToAdd, Action.EXECUTE, AutomationType.INTERNAL);
        if (leftoverWaste > 0) {
            double radioactivity = wasteToAdd.getType().get(GasAttributes.Radiation.class).getRadioactivity();
            Mekanism.radiationManager.radiate(getCenter(), leftoverWaste * radioactivity);
        }
    }

    public Coord4D getCenter() {
        return new Coord4D((minLocation.x + maxLocation.x) / 2, (minLocation.y + maxLocation.y) / 2, (minLocation.z + maxLocation.z) / 2, minLocation.dimension);
    }

    @Override
    public void onCreated() {
        // update the heat capacity now that we've read
        heatCapacitor.setHeatCapacity(CASING_HEAT_CAPACITY * locations.size(), true);
    }

    @Nonnull
    @Override
    public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
        return fluidTanks;
    }

    @Nonnull
    @Override
    public List<? extends IChemicalTank<Gas, GasStack>> getGasTanks(@Nullable Direction side) {
        return gasTanks;
    }

    @Nonnull
    @Override
    public List<IHeatCapacitor> getHeatCapacitors(Direction side) {
        return heatCapacitors;
    }
}
