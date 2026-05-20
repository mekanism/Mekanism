package mekanism.common.content.boiler;

import com.google.common.primitives.Ints;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.datamaps.IMekanismDataMapTypes;
import mekanism.api.datamaps.chemical.attribute.HeatedCoolant;
import mekanism.api.heat.HeatAPI;
import mekanism.api.math.MathUtils;
import mekanism.common.block.attribute.AttributeStateBoilerValveMode.BoilerValveMode;
import mekanism.common.capabilities.chemical.VariableCapacityChemicalTank;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.capabilities.heat.VariableHeatCapacitor;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerChemicalTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerFluidTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerHeatCapacitorWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.lib.multiblock.IValveHandler;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.lib.multiblock.Structure;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.tile.multiblock.TileEntityBoilerCasing;
import mekanism.common.tile.multiblock.TileEntityBoilerValve;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.ResourceUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BoilerMultiblockData extends MultiblockData implements IValveHandler {

    public static final Predicate<ChemicalResource> IS_HEATED_COOLANT = type -> type.getData(IMekanismDataMapTypes.INSTANCE.heatedChemicalCoolant()) != null;
    public static final Predicate<ChemicalResource> IS_COOLED_COOLANT = type -> type.getData(IMekanismDataMapTypes.INSTANCE.cooledChemicalCoolant()) != null;
    public static final Object2BooleanMap<UUID> hotMap = new Object2BooleanOpenHashMap<>();

    public static final double CASING_HEAT_CAPACITY = 50;
    private static final double CASING_INVERSE_INSULATION_COEFFICIENT = 100_000;
    private static final double CASING_INVERSE_CONDUCTION_COEFFICIENT = 1;

    private final List<AdvancedCapabilityOutputTarget<ResourceHandler<ChemicalResource>, BoilerValveMode>> chemicalOutputTargets = new ArrayList<>();
    private final List<IChemicalTank> inputTanks;
    private final List<IChemicalTank> outputSteamTanks;
    private final List<IChemicalTank> outputCoolantTanks;

    @ContainerSync
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getHeatedCoolant", "getHeatedCoolantCapacity", "getHeatedCoolantNeeded",
                                                                                        "getHeatedCoolantFilledPercentage"}, docPlaceholder = "heated coolant tank")
    public IChemicalTank superheatedCoolantTank;
    @ContainerSync
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getCooledCoolant", "getCooledCoolantCapacity", "getCooledCoolantNeeded",
                                                                                        "getCooledCoolantFilledPercentage"}, docPlaceholder = "cooled coolant tank")
    public IChemicalTank cooledCoolantTank;
    @ContainerSync
    @WrappingComputerMethod(wrapper = ComputerFluidTankWrapper.class, methodNames = {"getWater", "getWaterCapacity", "getWaterNeeded",
                                                                                     "getWaterFilledPercentage"}, docPlaceholder = "water tank")
    public VariableCapacityFluidTank waterTank;
    @ContainerSync
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getSteam", "getSteamCapacity", "getSteamNeeded",
                                                                                        "getSteamFilledPercentage"}, docPlaceholder = "steam tank")
    public IChemicalTank steamTank;
    @ContainerSync
    @WrappingComputerMethod(wrapper = ComputerHeatCapacitorWrapper.class, methodNames = "getTemperature", docPlaceholder = "boiler")
    public VariableHeatCapacitor heatCapacitor;

    private double biomeAmbientTemp;
    @ContainerSync
    @SyntheticComputerMethod(getter = "getEnvironmentalLoss", getterDescription = "Get the amount of heat lost to the environment in the last tick (Kelvin)")
    public double lastEnvironmentLoss;
    @ContainerSync
    @SyntheticComputerMethod(getter = "getBoilRate", getterDescription = "Get the rate of boiling (mB/t)")
    public int lastBoilRate;
    @ContainerSync
    @SyntheticComputerMethod(getter = "getMaxBoilRate", getterDescription = "Get the maximum rate of boiling seen (mB/t)")
    public int lastMaxBoil;

    @ContainerSync
    @SyntheticComputerMethod(getter = "getSuperheaters", getterDescription = "How many superheaters this Boiler has")
    public int superheatingElements;

    @ContainerSync(setter = "setWaterVolume")
    private int waterVolume;
    @ContainerSync(setter = "setSteamVolume")
    private int steamVolume;

    private long waterTankCapacity, superheatedCoolantCapacity, steamTankCapacity, cooledCoolantCapacity;

    public BlockPos upperRenderLocation;

    public float prevWaterScale;
    public float prevSteamScale;

    public BoilerMultiblockData(TileEntityBoilerCasing tile) {
        super(tile);
        //Default biome temp to the ambient temperature at the block we are at
        biomeAmbientTemp = HeatAPI.getAmbientTemp(tile.getLevel(), tile.getBlockPos());
        superheatedCoolantTank = VariableCapacityChemicalTank.input(this, () -> superheatedCoolantCapacity, IS_HEATED_COOLANT, this);
        waterTank = VariableCapacityFluidTank.input(this, () -> waterTankCapacity, fluid -> fluid.is(FluidTags.WATER),
              createSaveAndComparator());
        fluidTanks.add(waterTank);
        steamTank = VariableCapacityChemicalTank.output(this, () -> steamTankCapacity, chemical -> chemical.is(MekanismChemicals.STEAM), this);
        cooledCoolantTank = VariableCapacityChemicalTank.output(this, () -> cooledCoolantCapacity, IS_COOLED_COOLANT, this);
        inputTanks = List.of(superheatedCoolantTank);
        outputSteamTanks = List.of(steamTank);
        outputCoolantTanks = List.of(cooledCoolantTank);
        Collections.addAll(chemicalTanks, steamTank, superheatedCoolantTank, cooledCoolantTank);
        heatCapacitor = VariableHeatCapacitor.create(CASING_HEAT_CAPACITY, () -> CASING_INVERSE_CONDUCTION_COEFFICIENT, () -> CASING_INVERSE_INSULATION_COEFFICIENT,
              () -> biomeAmbientTemp, this);
        heatCapacitors.add(heatCapacitor);
    }

    @Override
    public void onCreated(Level world) {
        super.onCreated(world);
        biomeAmbientTemp = calculateAverageAmbientTemperature(world);
        // update the heat capacity now that we've read
        heatCapacitor.setHeatCapacity(CASING_HEAT_CAPACITY * locations.size(), true);
    }

    @Override
    public void remove(Level world, Structure oldStructure) {
        hotMap.removeBoolean(inventoryID);
        super.remove(world, oldStructure);
    }

    @Nullable
    private HeatedCoolant getHeatedCoolant() {
        ChemicalResource resource = superheatedCoolantTank.getResource();
        return resource.isEmpty() ? null : resource.getData(IMekanismDataMapTypes.INSTANCE.heatedChemicalCoolant());
    }

    @Override
    public boolean tick(ServerLevel world) {
        boolean needsPacket = super.tick(world);
        hotMap.put(inventoryID, getTotalTemperature() >= HeatUtils.BASE_BOIL_TEMP - 0.01);
        // external heat dissipation
        lastEnvironmentLoss = simulateEnvironment();
        // update temperature
        updateHeatCapacitors(null);
        // handle coolant heat transfer
        if (!superheatedCoolantTank.isEmpty()) {
            HeatedCoolant coolantType = getHeatedCoolant();
            if (coolantType != null) {
                try (Transaction transaction = Transaction.openRoot()) {
                    ChemicalResource heatedTankType = superheatedCoolantTank.getResource();
                    double portionToCool = coolantType.conductivity() * superheatedCoolantTank.amountAsLong();
                    //TODO - 26.1: Re-evaluate this cast
                    int toCool = Ints.saturatedCast(Math.round(portionToCool * (1 - heatCapacitor.getTemperature() / coolantType.temperature())));
                    ChemicalResource cooledCoolant = coolantType.cool();
                    int amountCooled = cooledCoolantTank.insert(cooledCoolant, toCool, transaction, AutomationType.INTERNAL);
                    if (amountCooled > 0 && superheatedCoolantTank.extract(heatedTankType, amountCooled, transaction, AutomationType.INTERNAL) == amountCooled) {
                        heatCapacitor.handleHeat(amountCooled * coolantType.thermalEnthalpy());
                        transaction.commit();
                    }
                }
            }
        }
        // handle water heat transfer
        if (getTotalTemperature() >= HeatUtils.BASE_BOIL_TEMP && !waterTank.isEmpty()) {
            double heatAvailable = getHeatAvailable();
            lastMaxBoil = Mth.floor(HeatUtils.getSteamEnergyEfficiency() * heatAvailable / HeatUtils.getWaterThermalEnthalpy());
            FluidResource water = waterTank.getResource();
            if (water.isEmpty()) {
                lastBoilRate = 0;
            } else {
                try (Transaction transaction = Transaction.openRoot()) {
                    ChemicalResource steam = MekanismChemicals.STEAM.asResource();
                    int amountToBoil = Math.min(lastMaxBoil, steamTank.getNeededAsInt(steam));
                    int boiled = waterTank.extract(water, amountToBoil, transaction, AutomationType.INTERNAL);
                    if (boiled > 0 && steamTank.insert(steam, boiled, transaction, AutomationType.INTERNAL) == boiled) {
                        heatCapacitor.handleHeat(-boiled * HeatUtils.getWaterThermalEnthalpy() / HeatUtils.getSteamEnergyEfficiency());
                        transaction.commit();
                    }
                    lastBoilRate = boiled;
                }
            }
        } else {
            lastBoilRate = 0;
            lastMaxBoil = 0;
        }
        if (!chemicalOutputTargets.isEmpty()) {
            if (!steamTank.isEmpty()) {
                ResourceUtils.emit(getActiveOutputs(chemicalOutputTargets, BoilerValveMode.OUTPUT_STEAM), steamTank, null);
            }
            if (!cooledCoolantTank.isEmpty()) {
                ResourceUtils.emit(getActiveOutputs(chemicalOutputTargets, BoilerValveMode.OUTPUT_COOLANT), cooledCoolantTank, null);
            }
        }
        float waterScale = MekanismUtils.getScale(prevWaterScale, waterTank);
        if (MekanismUtils.scaleChanged(waterScale, prevWaterScale)) {
            needsPacket = true;
            prevWaterScale = waterScale;
        }
        float steamScale = MekanismUtils.getScale(prevSteamScale, steamTank);
        if (MekanismUtils.scaleChanged(steamScale, prevSteamScale)) {
            needsPacket = true;
            prevSteamScale = steamScale;
        }
        return needsPacket;
    }

    @Override
    protected void updateEjectors(Level world) {
        chemicalOutputTargets.clear();
        for (Map.Entry<BlockPos, ValveData> entry : valves.entrySet()) {
            TileEntityBoilerValve tile = WorldUtils.getTileEntity(TileEntityBoilerValve.class, world, entry.getKey());
            if (tile != null) {
                ValveData valve = entry.getValue();
                tile.addChemicalTargetCapability(chemicalOutputTargets, valve.side);
                valve.addTank(waterTank, true);
            }
        }
    }

    @Override
    protected boolean hasFluidValveHandling() {
        return true;
    }

    public List<IChemicalTank> getChemicalTanks(BoilerValveMode mode) {
        return switch (mode) {
            case INPUT -> inputTanks;
            case OUTPUT_STEAM -> outputSteamTanks;
            case OUTPUT_COOLANT -> outputCoolantTanks;
        };
    }

    @Override
    public void readUpdateTag(@NotNull ValueInput input) {
        super.readUpdateTag(input);
        prevWaterScale = input.getFloatOr(SerializationConstants.SCALE, prevWaterScale);
        prevSteamScale = input.getFloatOr(SerializationConstants.SCALE_ALT, prevSteamScale);
        input.getInt(SerializationConstants.VOLUME).ifPresent(this::setWaterVolume);
        input.getInt(SerializationConstants.LOWER_VOLUME).ifPresent(this::setSteamVolume);
        NBTUtils.readOrEmpty(input, SerializationConstants.FLUID, waterTank);
        NBTUtils.readOrEmpty(input, SerializationConstants.CHEMICAL, steamTank);
        input.read(SerializationConstants.RENDER_Y, BlockPos.CODEC).ifPresent(value -> upperRenderLocation = value);
        readValves(input);
    }

    @Override
    public void writeUpdateTag(@NotNull ValueOutput output) {
        super.writeUpdateTag(output);
        output.putFloat(SerializationConstants.SCALE, prevWaterScale);
        output.putFloat(SerializationConstants.SCALE_ALT, prevSteamScale);
        output.putInt(SerializationConstants.VOLUME, getWaterVolume());
        output.putInt(SerializationConstants.LOWER_VOLUME, getSteamVolume());
        NBTUtils.storeNonEmpty(output, SerializationConstants.FLUID, waterTank);
        NBTUtils.storeNonEmpty(output, SerializationConstants.CHEMICAL, steamTank);
        output.store(SerializationConstants.RENDER_Y, BlockPos.CODEC, upperRenderLocation);
        writeValves(output);
    }

    @Override
    protected int getMultiblockRedstoneLevel() {
        return ResourceUtils.getRedstoneSignalFromContainer(waterTank);
    }

    private double getHeatAvailable() {
        double heatAvailable = (heatCapacitor.getTemperature() - HeatUtils.BASE_BOIL_TEMP) * (heatCapacitor.getHeatCapacity() * MekanismConfig.general.boilerWaterConductivity.get());
        return Math.min(heatAvailable, MekanismConfig.general.superheatingHeatTransfer.get() * superheatingElements);
    }

    @Override
    public double simulateEnvironment() {
        double invConduction = HeatAPI.AIR_INVERSE_COEFFICIENT + (CASING_INVERSE_INSULATION_COEFFICIENT + CASING_INVERSE_CONDUCTION_COEFFICIENT);
        double tempToTransfer = (heatCapacitor.getTemperature() - biomeAmbientTemp) / invConduction;
        heatCapacitor.handleHeat(-tempToTransfer * heatCapacitor.getHeatCapacity());
        return Math.max(tempToTransfer, 0);
    }

    public int getWaterVolume() {
        return waterVolume;
    }

    public void setWaterVolume(int volume) {
        if (waterVolume != volume) {
            waterVolume = volume;
            waterTankCapacity = volume * MekanismConfig.general.boilerWaterPerTank.get();
            superheatedCoolantCapacity = volume * MekanismConfig.general.boilerHeatedCoolantPerTank.get();
        }
    }

    public int getSteamVolume() {
        return steamVolume;
    }

    public void setSteamVolume(int volume) {
        if (steamVolume != volume) {
            steamVolume = volume;
            steamTankCapacity = volume * MekanismConfig.general.boilerSteamPerTank.get();
            cooledCoolantCapacity = volume * MekanismConfig.general.boilerCooledCoolantPerTank.get();
        }
    }

    @ComputerMethod(methodDescription = "Get the maximum possible boil rate for this Boiler, based on the number of Superheating Elements")
    public int getBoilCapacity() {
        double boilCapacity = MekanismConfig.general.superheatingHeatTransfer.get() * superheatingElements / HeatUtils.getWaterThermalEnthalpy();
        return MathUtils.clampToInt(boilCapacity * HeatUtils.getSteamEnergyEfficiency());
    }
}
