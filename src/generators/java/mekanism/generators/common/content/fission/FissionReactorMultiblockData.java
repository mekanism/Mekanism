package mekanism.generators.common.content.fission;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.IMekanismGasHandler;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import mekanism.api.chemical.gas.attribute.GasAttributes.CooledCoolant;
import mekanism.api.chemical.gas.attribute.GasAttributes.HeatedCoolant;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.inventory.AutomationType;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.chemical.MultiblockGasTank;
import mekanism.common.capabilities.fluid.MultiblockFluidTank;
import mekanism.common.capabilities.heat.ITileHeatHandler;
import mekanism.common.capabilities.heat.MultiblockHeatCapacitor;
import mekanism.common.multiblock.IValveHandler.ValveData;
import mekanism.common.multiblock.MultiblockData;
import mekanism.common.registries.MekanismGases;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FissionReactorMultiblockData extends MultiblockData<FissionReactorMultiblockData> implements IMekanismFluidHandler, IMekanismGasHandler,
      ITileHeatHandler {

    public static final double INVERSE_INSULATION_COEFFICIENT = 100_000;
    public static final double INVERSE_CONDUCTION_COEFFICIENT = 10;

    private static double waterConductivity = 0.9;

    public static final int COOLANT_PER_VOLUME = 100_000;
    public static final long HEATED_COOLANT_PER_VOLUME = 1_000_000;
    public static final long FUEL_PER_ASSEMBLY = 8_000;

    public static final double MIN_DAMAGE_TEMPERATURE = 1_200;
    public static final double MAX_DAMAGE_TEMPERATURE = 1_800;
    public static final double MAX_DAMAGE = 100;

    public static final long BURN_PER_ASSEMBLY = 1;
    private static final double EXPLOSION_CHANCE = 1D / (512_000);

    public static Object2BooleanMap<UUID> burningMap = new Object2BooleanOpenHashMap<>();

    public Set<ValveData> valves = new ObjectOpenHashSet<>();
    public int fuelAssemblies, surfaceArea;

    public MultiblockGasTank<TileEntityFissionReactorCasing> gasCoolantTank;
    public MultiblockFluidTank<TileEntityFissionReactorCasing> fluidCoolantTank;
    public MultiblockGasTank<TileEntityFissionReactorCasing> fuelTank;

    public MultiblockGasTank<TileEntityFissionReactorCasing> heatedCoolantTank;
    public MultiblockGasTank<TileEntityFissionReactorCasing> wasteTank;
    public MultiblockHeatCapacitor<TileEntityFissionReactorCasing> heatCapacitor;

    private List<IExtendedFluidTank> fluidTanks;
    private List<IGasTank> gasTanks;
    private List<IHeatCapacitor> heatCapacitors;

    public double lastEnvironmentLoss = 0, lastTransferLoss = 0;
    public long lastBoilRate = 0;
    public double lastBurnRate = 0;
    public boolean clientBurning;

    public double reactorDamage = 0;
    public double rateLimit = 0.1;
    public double burnRemaining = 0, partialWaste = 0;
    private boolean active;

    public FissionReactorMultiblockData(TileEntityFissionReactorCasing tile) {
        fluidCoolantTank = MultiblockFluidTank.create(tile, () -> tile.structure == null ? 0 : getVolume() * COOLANT_PER_VOLUME,
            (stack, automationType) -> automationType != AutomationType.EXTERNAL, (stack, automationType) -> tile.structure != null,
            fluid -> fluid.getFluid().isIn(FluidTags.WATER), null);
        fluidTanks = Collections.singletonList(fluidCoolantTank);
        gasCoolantTank = MultiblockGasTank.create(tile, () -> tile.structure == null ? 0 : getVolume() * COOLANT_PER_VOLUME,
            (stack, automationType) -> automationType != AutomationType.EXTERNAL, (stack, automationType) -> tile.structure != null,
            gas -> gas.has(CooledCoolant.class) && fluidCoolantTank.isEmpty());
        fuelTank = MultiblockGasTank.create(tile, () -> tile.structure == null ? 0 : fuelAssemblies * FUEL_PER_ASSEMBLY,
            (stack, automationType) -> automationType != AutomationType.EXTERNAL, (stack, automationType) -> tile.structure != null,
            gas -> gas == MekanismGases.FISSILE_FUEL.getGas(), ChemicalAttributeValidator.ALWAYS_ALLOW, null);
        heatedCoolantTank = MultiblockGasTank.create(tile, () -> tile.structure == null ? 0 : getVolume() * HEATED_COOLANT_PER_VOLUME,
            (stack, automationType) -> tile.structure != null, (stack, automationType) -> automationType != AutomationType.EXTERNAL,
            gas -> gas == MekanismGases.STEAM.get() || gas.has(HeatedCoolant.class));
        wasteTank = MultiblockGasTank.create(tile, () -> tile.structure == null ? 0 : fuelAssemblies * FUEL_PER_ASSEMBLY,
            (stack, automationType) -> tile.structure != null, (stack, automationType) -> automationType != AutomationType.EXTERNAL,
            gas -> gas == MekanismGases.NUCLEAR_WASTE.getGas(), ChemicalAttributeValidator.ALWAYS_ALLOW, null);
        gasTanks = Arrays.asList(fuelTank, heatedCoolantTank, wasteTank, gasCoolantTank);
        heatCapacitor = MultiblockHeatCapacitor.create(tile,
            MekanismGeneratorsConfig.generators.fissionCasingHeatCapacity.get(),
            () -> INVERSE_INSULATION_COEFFICIENT,
            () -> INVERSE_INSULATION_COEFFICIENT);
        heatCapacitors = Collections.singletonList(heatCapacitor);
    }

    public void handleDamage(World world) {
        double temp = heatCapacitor.getTemperature();
        if (temp > MIN_DAMAGE_TEMPERATURE) {
            double damageRate = Math.min(temp, MAX_DAMAGE_TEMPERATURE) / (MIN_DAMAGE_TEMPERATURE * 10);
            reactorDamage += damageRate;
        } else {
            double repairRate = (MIN_DAMAGE_TEMPERATURE - temp) / (MIN_DAMAGE_TEMPERATURE * 100);
            reactorDamage = Math.max(0, reactorDamage - repairRate);
        }
        // consider a meltdown only if it's config-enabled, we're passed the damage threshold and the temperature is still dangerous
        if (MekanismGeneratorsConfig.generators.fissionMeltdownsEnabled.get() && reactorDamage >= MAX_DAMAGE && temp >= MIN_DAMAGE_TEMPERATURE) {
            if (world.rand.nextDouble() < (reactorDamage / MAX_DAMAGE) * MekanismGeneratorsConfig.generators.fissionMeltdownChance.get()) {
                double radiation = 0;
                radiation += wasteTank.getStored() * MekanismGases.NUCLEAR_WASTE.get().get(GasAttributes.Radiation.class).getRadioactivity();
                if (wasteTank.getStack().has(GasAttributes.Radiation.class)) {
                    radiation += wasteTank.getStored() * wasteTank.getStack().get(GasAttributes.Radiation.class).getRadioactivity();
                }
                radiation *= MekanismGeneratorsConfig.generators.fissionMeltdownRadiationMultiplier.get();
                Mekanism.radiationManager.radiate(getCenter(), radiation);
                Mekanism.radiationManager.createMeltdown(world, minLocation, maxLocation, heatCapacitor.getHeat(), EXPLOSION_CHANCE);
            }
        }
    }

    public void handleCoolant() {
        double temp = heatCapacitor.getTemperature();
        double heat = getBoilEfficiency() * (temp - HeatUtils.BASE_BOIL_TEMP) * heatCapacitor.getHeatCapacity();
        long coolantHeated = 0;

        if (!fluidCoolantTank.isEmpty()) {
            double caseCoolantHeat = heat * waterConductivity;
            coolantHeated = (int) (HeatUtils.getFluidThermalEfficiency() * caseCoolantHeat / HeatUtils.getWaterThermalEnthalpy());
            coolantHeated = Math.max(0, Math.min(coolantHeated, fluidCoolantTank.getFluidAmount()));
            if (coolantHeated > 0) {
                if (fluidCoolantTank.shrinkStack((int) coolantHeated, Action.EXECUTE) != coolantHeated) {
                    MekanismUtils.logMismatchedStackSize();
                }
                // extra steam is dumped
                heatedCoolantTank.insert(MekanismGases.STEAM.getGasStack(coolantHeated), Action.EXECUTE, AutomationType.INTERNAL);
                caseCoolantHeat = coolantHeated * HeatUtils.getWaterThermalEnthalpy() / HeatUtils.getFluidThermalEfficiency();
                heatCapacitor.handleHeat(-caseCoolantHeat);
            }
        } else if (!gasCoolantTank.isEmpty()) {
            CooledCoolant coolantType = gasCoolantTank.getStack().get(CooledCoolant.class);
            if (coolantType != null) {
                double caseCoolantHeat = heat * coolantType.getConductivity();
                coolantHeated = (int) (HeatUtils.getFluidThermalEfficiency() * caseCoolantHeat / coolantType.getThermalEnthalpy());
                coolantHeated = Math.max(0, Math.min(coolantHeated, gasCoolantTank.getStored()));
                if (coolantHeated > 0) {
                    if (gasCoolantTank.shrinkStack((int) coolantHeated, Action.EXECUTE) != coolantHeated) {
                        MekanismUtils.logMismatchedStackSize();
                    }
                    heatedCoolantTank.insert(coolantType.getHeatedGas().getGasStack(coolantHeated), Action.EXECUTE, AutomationType.INTERNAL);
                    caseCoolantHeat = coolantHeated * coolantType.getThermalEnthalpy() / HeatUtils.getFluidThermalEfficiency();
                    heatCapacitor.handleHeat(-caseCoolantHeat);
                }
            }
        }
        lastBoilRate = coolantHeated;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isBurning() {
        return lastBurnRate > 0;
    }

    public boolean handlesSound(TileEntityFissionReactorCasing tile) {
        BlockPos pos = tile.getPos();
        return (pos.getX() == minLocation.x || pos.getX() == maxLocation.x) &&
               (pos.getY() == minLocation.y || pos.getY() == maxLocation.y) &&
               (pos.getZ() == minLocation.z || pos.getZ() == maxLocation.z);

    }

    public double getBoilEfficiency() {
        double avgSurfaceArea = (double) surfaceArea / (double) fuelAssemblies;
        return Math.min(1, avgSurfaceArea / MekanismGeneratorsConfig.generators.fissionSurfaceAreaTarget.get());
    }

    public void burnFuel() {
        double storedFuel = fuelTank.getStored() + burnRemaining;
        double toBurn = Math.min(Math.min(rateLimit, storedFuel), fuelAssemblies * BURN_PER_ASSEMBLY);
        storedFuel -= toBurn;
        fuelTank.setStackSize((long) storedFuel, Action.EXECUTE);
        burnRemaining = storedFuel % 1;
        heatCapacitor.handleHeat(toBurn * MekanismGeneratorsConfig.generators.energyPerFissionFuel.get().doubleValue());
        // handle waste
        partialWaste += toBurn;
        long newWaste = (long) Math.floor(partialWaste);
        if (newWaste > 0) {
            partialWaste %= 1;
            long leftoverWaste = Math.max(0, newWaste - wasteTank.getNeeded());
            GasStack wasteToAdd = MekanismGases.NUCLEAR_WASTE.getGasStack(newWaste);
            wasteTank.insert(wasteToAdd, Action.EXECUTE, AutomationType.INTERNAL);
            if (leftoverWaste > 0) {
                double radioactivity = wasteToAdd.getType().get(GasAttributes.Radiation.class).getRadioactivity();
                Mekanism.radiationManager.radiate(getCenter(), leftoverWaste * radioactivity);
            }
        }
        // update previous burn
        lastBurnRate = toBurn;
    }

    public Coord4D getCenter() {
        if (minLocation == null || maxLocation == null)
            return null;
        return new Coord4D((minLocation.x + maxLocation.x) / 2, (minLocation.y + maxLocation.y) / 2, (minLocation.z + maxLocation.z) / 2, minLocation.dimension);
    }

    @Override
    public void onCreated() {
        // update the heat capacity now that we've read
        heatCapacitor.setHeatCapacity(MekanismGeneratorsConfig.generators.fissionCasingHeatCapacity.get() * locations.size(), true);
    }

    @Nonnull
    @Override
    public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
        return fluidTanks;
    }

    @Nonnull
    @Override
    public List<IGasTank> getGasTanks(@Nullable Direction side) {
        return gasTanks;
    }

    @Nonnull
    @Override
    public List<IHeatCapacitor> getHeatCapacitors(Direction side) {
        return heatCapacitors;
    }
}
