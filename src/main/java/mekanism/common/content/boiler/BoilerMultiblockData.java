package mekanism.common.content.boiler;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.Collections;
import java.util.UUID;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.attribute.GasAttributes.CooledCoolant;
import mekanism.api.chemical.gas.attribute.GasAttributes.HeatedCoolant;
import mekanism.api.heat.HeatAPI;
import mekanism.api.math.MathUtils;
import mekanism.common.capabilities.chemical.multiblock.MultiblockChemicalTankBuilder;
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
import mekanism.common.registries.MekanismGases;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tile.multiblock.TileEntityBoilerCasing;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;

public class BoilerMultiblockData extends MultiblockData implements IValveHandler {

    public static final Object2BooleanMap<UUID> hotMap = new Object2BooleanOpenHashMap<>();

    public static final double CASING_HEAT_CAPACITY = 50;
    private static final double CASING_INVERSE_INSULATION_COEFFICIENT = 100_000;
    private static final double CASING_INVERSE_CONDUCTION_COEFFICIENT = 1;

    private static final int WATER_PER_VOLUME = 16_000;
    private static final long STEAM_PER_VOLUME = 160_000;

    private static final int SUPERHEATED_COOLANT_PER_VOLUME = 256_000;
    private static final int COOLED_COOLANT_PER_VOLUME = 256_000;

    private static final double COOLANT_COOLING_EFFICIENCY = 0.4;

    @ContainerSync
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getHeatedCoolant", "getHeatedCoolantCapacity", "getHeatedCoolantNeeded",
                                                                                        "getHeatedCoolantFilledPercentage"})
    public IGasTank superheatedCoolantTank;
    @ContainerSync
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getCooledCoolant", "getCooledCoolantCapacity", "getCooledCoolantNeeded",
                                                                                        "getCooledCoolantFilledPercentage"})
    public IGasTank cooledCoolantTank;
    @ContainerSync
    @WrappingComputerMethod(wrapper = ComputerFluidTankWrapper.class, methodNames = {"getWater", "getWaterCapacity", "getWaterNeeded", "getWaterFilledPercentage"})
    public VariableCapacityFluidTank waterTank;
    @ContainerSync
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getSteam", "getSteamCapacity", "getSteamNeeded", "getSteamFilledPercentage"})
    public IGasTank steamTank;
    @ContainerSync
    @WrappingComputerMethod(wrapper = ComputerHeatCapacitorWrapper.class, methodNames = "getTemperature")
    public VariableHeatCapacitor heatCapacitor;

    private double biomeAmbientTemp;
    @ContainerSync
    @SyntheticComputerMethod(getter = "getEnvironmentalLoss")
    public double lastEnvironmentLoss;
    @ContainerSync
    @SyntheticComputerMethod(getter = "getBoilRate")
    public int lastBoilRate;
    @ContainerSync
    @SyntheticComputerMethod(getter = "getMaxBoilRate")
    public int lastMaxBoil;

    @ContainerSync
    @SyntheticComputerMethod(getter = "getSuperheaters")
    public int superheatingElements;

    @ContainerSync(setter = "setWaterVolume")
    private int waterVolume;
    @ContainerSync(setter = "setSteamVolume")
    private int steamVolume;

    private int waterTankCapacity;
    private long superheatedCoolantCapacity, steamTankCapacity, cooledCoolantCapacity;

    public BlockPos upperRenderLocation;

    public float prevWaterScale;
    public float prevSteamScale;

    public BoilerMultiblockData(TileEntityBoilerCasing tile) {
        super(tile);
        //Default biome temp to the ambient temperature at the block we are at
        biomeAmbientTemp = HeatAPI.getAmbientTemp(tile.getLevel(), tile.getTilePos());
        superheatedCoolantTank = MultiblockChemicalTankBuilder.GAS.input(this, () -> superheatedCoolantCapacity, gas -> gas.has(HeatedCoolant.class), this);
        waterTank = VariableCapacityFluidTank.input(this, () -> waterTankCapacity, fluid -> MekanismTags.Fluids.WATER_LOOKUP.contains(fluid.getFluid()),
              createSaveAndComparator());
        fluidTanks.add(waterTank);
        steamTank = MultiblockChemicalTankBuilder.GAS.output(this, () -> steamTankCapacity, gas -> gas == MekanismGases.STEAM.getChemical(), this);
        cooledCoolantTank = MultiblockChemicalTankBuilder.GAS.output(this, () -> cooledCoolantCapacity, gas -> gas.has(CooledCoolant.class), this);
        Collections.addAll(gasTanks, steamTank, superheatedCoolantTank, cooledCoolantTank);
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
    public void remove(Level world) {
        hotMap.removeBoolean(inventoryID);
        super.remove(world);
    }

    @Override
    public boolean tick(Level world) {
        boolean needsPacket = super.tick(world);
        hotMap.put(inventoryID, getTotalTemperature() >= HeatUtils.BASE_BOIL_TEMP - 0.01);
        // external heat dissipation
        lastEnvironmentLoss = simulateEnvironment();
        // update temperature
        updateHeatCapacitors(null);
        // handle coolant heat transfer
        if (!superheatedCoolantTank.isEmpty()) {
            HeatedCoolant coolantType = superheatedCoolantTank.getStack().get(HeatedCoolant.class);
            if (coolantType != null) {
                long toCool = Math.round(BoilerMultiblockData.COOLANT_COOLING_EFFICIENCY * superheatedCoolantTank.getStored());
                toCool = MathUtils.clampToLong(toCool * (1 - heatCapacitor.getTemperature() / HeatUtils.HEATED_COOLANT_TEMP));
                GasStack cooledCoolant = coolantType.getCooledGas().getStack(toCool);
                toCool = Math.min(toCool, toCool - cooledCoolantTank.insert(cooledCoolant, Action.EXECUTE, AutomationType.INTERNAL).getAmount());
                if (toCool > 0) {
                    double heatEnergy = toCool * coolantType.getThermalEnthalpy();
                    heatCapacitor.handleHeat(heatEnergy);
                    superheatedCoolantTank.shrinkStack(toCool, Action.EXECUTE);
                }
            }
        }
        // handle water heat transfer
        if (getTotalTemperature() >= HeatUtils.BASE_BOIL_TEMP && !waterTank.isEmpty()) {
            double heatAvailable = getHeatAvailable();
            lastMaxBoil = (int) Math.floor(HeatUtils.getSteamEnergyEfficiency() * heatAvailable / HeatUtils.getWaterThermalEnthalpy());

            int amountToBoil = Math.min(lastMaxBoil, waterTank.getFluidAmount());
            amountToBoil = Math.min(amountToBoil, MathUtils.clampToInt(steamTank.getNeeded()));
            if (!waterTank.isEmpty()) {
                waterTank.shrinkStack(amountToBoil, Action.EXECUTE);
            }
            if (steamTank.isEmpty()) {
                steamTank.setStack(MekanismGases.STEAM.getStack(amountToBoil));
            } else {
                steamTank.growStack(amountToBoil, Action.EXECUTE);
            }

            heatCapacitor.handleHeat(-amountToBoil * HeatUtils.getWaterThermalEnthalpy() / HeatUtils.getSteamEnergyEfficiency());
            lastBoilRate = amountToBoil;
        } else {
            lastBoilRate = 0;
            lastMaxBoil = 0;
        }
        float waterScale = MekanismUtils.getScale(prevWaterScale, waterTank);
        if (waterScale != prevWaterScale) {
            needsPacket = true;
            prevWaterScale = waterScale;
        }
        float steamScale = MekanismUtils.getScale(prevSteamScale, steamTank);
        if (steamScale != prevSteamScale) {
            needsPacket = true;
            prevSteamScale = steamScale;
        }
        return needsPacket;
    }

    @Override
    public void readUpdateTag(CompoundTag tag) {
        super.readUpdateTag(tag);
        NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE, scale -> prevWaterScale = scale);
        NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE_ALT, scale -> prevSteamScale = scale);
        NBTUtils.setIntIfPresent(tag, NBTConstants.VOLUME, this::setWaterVolume);
        NBTUtils.setIntIfPresent(tag, NBTConstants.LOWER_VOLUME, this::setSteamVolume);
        NBTUtils.setFluidStackIfPresent(tag, NBTConstants.FLUID_STORED, value -> waterTank.setStack(value));
        NBTUtils.setGasStackIfPresent(tag, NBTConstants.GAS_STORED, value -> steamTank.setStack(value));
        NBTUtils.setBlockPosIfPresent(tag, NBTConstants.RENDER_Y, value -> upperRenderLocation = value);
        readValves(tag);
    }

    @Override
    public void writeUpdateTag(CompoundTag tag) {
        super.writeUpdateTag(tag);
        tag.putFloat(NBTConstants.SCALE, prevWaterScale);
        tag.putFloat(NBTConstants.SCALE_ALT, prevSteamScale);
        tag.putInt(NBTConstants.VOLUME, getWaterVolume());
        tag.putInt(NBTConstants.LOWER_VOLUME, getSteamVolume());
        tag.put(NBTConstants.FLUID_STORED, waterTank.getFluid().writeToNBT(new CompoundTag()));
        tag.put(NBTConstants.GAS_STORED, steamTank.getStack().write(new CompoundTag()));
        tag.put(NBTConstants.RENDER_Y, NbtUtils.writeBlockPos(upperRenderLocation));
        writeValves(tag);
    }

    @Override
    protected int getMultiblockRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(waterTank.getFluidAmount(), waterTank.getCapacity());
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
        waterVolume = volume;

        waterTankCapacity = getWaterVolume() * BoilerMultiblockData.WATER_PER_VOLUME;
        superheatedCoolantCapacity = (long) getWaterVolume() * BoilerMultiblockData.SUPERHEATED_COOLANT_PER_VOLUME;
    }

    public int getSteamVolume() {
        return steamVolume;
    }

    public void setSteamVolume(int volume) {
        steamVolume = volume;

        steamTankCapacity = getSteamVolume() * BoilerMultiblockData.STEAM_PER_VOLUME;
        cooledCoolantCapacity = (long) getSteamVolume() * BoilerMultiblockData.COOLED_COOLANT_PER_VOLUME;
    }

    @ComputerMethod
    public long getBoilCapacity() {
        double boilCapacity = MekanismConfig.general.superheatingHeatTransfer.get() * superheatingElements / HeatUtils.getWaterThermalEnthalpy();
        return MathUtils.clampToLong(boilCapacity * HeatUtils.getSteamEnergyEfficiency());
    }
}