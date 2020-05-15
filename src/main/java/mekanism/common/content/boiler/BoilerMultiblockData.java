package mekanism.common.content.boiler;

import java.util.Arrays;
import java.util.UUID;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IMekanismGasHandler;
import mekanism.api.chemical.gas.attribute.GasAttributes.CooledCoolant;
import mekanism.api.chemical.gas.attribute.GasAttributes.HeatedCoolant;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.HeatAPI.HeatTransfer;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.MathUtils;
import mekanism.common.capabilities.chemical.MultiblockGasTank;
import mekanism.common.capabilities.fluid.MultiblockFluidTank;
import mekanism.common.capabilities.heat.ITileHeatHandler;
import mekanism.common.capabilities.heat.MultiblockHeatCapacitor;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.multiblock.MultiblockData;
import mekanism.common.registries.MekanismGases;
import mekanism.common.tile.TileEntityBoilerCasing;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.World;

public class BoilerMultiblockData extends MultiblockData implements IMekanismFluidHandler, IMekanismGasHandler, ITileHeatHandler {

    public static Object2BooleanMap<UUID> hotMap = new Object2BooleanOpenHashMap<>();

    public static final double CASING_HEAT_CAPACITY = 50;
    public static final double CASING_INVERSE_INSULATION_COEFFICIENT = 10;
    public static final double CASING_INVERSE_CONDUCTION_COEFFICIENT = 1;

    public static final int WATER_PER_VOLUME = 16_000;
    public static final long STEAM_PER_VOLUME = 160_000;

    public static final int SUPERHEATED_COOLANT_PER_VOLUME = 256_000;
    public static final int COOLED_COOLANT_PER_VOLUME = 256_000;

    public static final double COOLANT_COOLING_EFFICIENCY = 0.4;

    @ContainerSync
    public MultiblockGasTank<BoilerMultiblockData> superheatedCoolantTank, cooledCoolantTank;
    @ContainerSync
    public MultiblockFluidTank<BoilerMultiblockData> waterTank;
    @ContainerSync
    public MultiblockGasTank<BoilerMultiblockData> steamTank;
    @ContainerSync
    public MultiblockHeatCapacitor<BoilerMultiblockData> heatCapacitor;

    @ContainerSync
    public double lastEnvironmentLoss;
    @ContainerSync
    public int lastBoilRate, lastMaxBoil;

    public boolean clientHot;
    @ContainerSync
    public int superheatingElements;

    @ContainerSync(setter = "setWaterVolume")
    private int waterVolume;
    @ContainerSync(setter = "setSteamVolume")
    private int steamVolume;

    private int waterTankCapacity;
    private long superheatedCoolantCapacity, steamTankCapacity, cooledCoolantCapacity;

    public Coord4D upperRenderLocation;

    public float prevWaterScale;
    public float prevSteamScale;

    public BoilerMultiblockData(TileEntityBoilerCasing tile) {
        super(tile);
        superheatedCoolantTank = MultiblockGasTank.create(this, tile, () -> getSuperheatedCoolantTankCapacity(),
            (stack, automationType) -> automationType != AutomationType.EXTERNAL, (stack, automationType) -> automationType != AutomationType.EXTERNAL || isFormed(),
            gas -> gas.has(HeatedCoolant.class));
        waterTank = MultiblockFluidTank.input(this, tile, () -> getWaterTankCapacity(), fluid -> fluid.getFluid().isIn(FluidTags.WATER));
        fluidTanks.add(waterTank);
        steamTank = MultiblockGasTank.create(this, tile, () -> getSteamTankCapacity(),
            (stack, automationType) -> automationType != AutomationType.EXTERNAL || isFormed(), (stack, automationType) -> automationType != AutomationType.EXTERNAL,
            gas -> gas == MekanismGases.STEAM.getGas());
        cooledCoolantTank = MultiblockGasTank.create(this, tile, () -> getCooledCoolantTankCapacity(),
            (stack, automationType) -> automationType != AutomationType.EXTERNAL || isFormed(), (stack, automationType) -> automationType != AutomationType.EXTERNAL,
            gas -> gas.has(CooledCoolant.class));
        gasTanks.addAll(Arrays.asList(steamTank, superheatedCoolantTank, cooledCoolantTank));
        heatCapacitor = MultiblockHeatCapacitor.create(this, tile,
            CASING_HEAT_CAPACITY,
            () -> CASING_INVERSE_INSULATION_COEFFICIENT * locations.size(),
            () -> CASING_INVERSE_INSULATION_COEFFICIENT * locations.size());
        heatCapacitors.add(heatCapacitor);
    }

    @Override
    public void onCreated() {
        super.onCreated();
        // update the heat capacity now that we've read
        heatCapacitor.setHeatCapacity(CASING_HEAT_CAPACITY * locations.size(), true);
    }

    @Override
    public boolean tick(World world) {
        boolean needsPacket = super.tick(world);
        boolean newHot = getTotalTemperature() >= HeatUtils.BASE_BOIL_TEMP - 0.01;
        if (newHot != clientHot) {
            needsPacket = true;
            clientHot = newHot;
            BoilerMultiblockData.hotMap.put(inventoryID, clientHot);
        }
        // external heat dissipation
        HeatTransfer transfer = simulate();
        // update temperature
        updateHeatCapacitors(null);
        lastEnvironmentLoss = transfer.getEnvironmentTransfer();
        // handle coolant heat transfer
        if (!superheatedCoolantTank.isEmpty()) {
            HeatedCoolant coolantType = superheatedCoolantTank.getStack().get(HeatedCoolant.class);
            if (coolantType != null) {
                long toCool = Math.round(BoilerMultiblockData.COOLANT_COOLING_EFFICIENCY * superheatedCoolantTank.getStored());
                toCool *= 1 - heatCapacitor.getTemperature() / HeatUtils.HEATED_COOLANT_TEMP;
                GasStack cooledCoolant = coolantType.getCooledGas().getGasStack(toCool);
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
                steamTank.setStack(MekanismGases.STEAM.getGasStack(amountToBoil));
            } else {
                steamTank.growStack(amountToBoil, Action.EXECUTE);
            }

            handleHeat(-amountToBoil * HeatUtils.getWaterThermalEnthalpy() / HeatUtils.getSteamEnergyEfficiency());
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
    protected int getMultiblockRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(waterTank.getFluidAmount(), waterTank.getCapacity());
    }

    public double getHeatAvailable() {
        double heatAvailable = (heatCapacitor.getTemperature() - HeatUtils.BASE_BOIL_TEMP) * (heatCapacitor.getHeatCapacity() * MekanismConfig.general.boilerWaterConductivity.get());
        return Math.min(heatAvailable, MekanismConfig.general.superheatingHeatTransfer.get() * superheatingElements);
    }

    @Override
    public HeatTransfer simulate() {
        double invConduction = HeatAPI.AIR_INVERSE_COEFFICIENT + (CASING_INVERSE_INSULATION_COEFFICIENT + CASING_INVERSE_CONDUCTION_COEFFICIENT) * locations.size();
        double heatToTransfer = (heatCapacitor.getTemperature() - HeatAPI.AMBIENT_TEMP) / invConduction;

        heatCapacitor.handleHeat(-heatToTransfer * heatCapacitor.getHeatCapacity());
        return new HeatTransfer(0, heatToTransfer);
    }

    public int getWaterTankCapacity() {
        return waterTankCapacity;
    }

    public long getSteamTankCapacity() {
        return steamTankCapacity;
    }

    public long getSuperheatedCoolantTankCapacity() {
        return superheatedCoolantCapacity;
    }

    public long getCooledCoolantTankCapacity() {
        return cooledCoolantCapacity;
    }

    public int getWaterVolume() {
        return waterVolume;
    }

    public void setWaterVolume(int volume) {
        waterVolume = volume;

        waterTankCapacity = getWaterVolume() * BoilerMultiblockData.WATER_PER_VOLUME;
        superheatedCoolantCapacity = getWaterVolume() * BoilerMultiblockData.SUPERHEATED_COOLANT_PER_VOLUME;
    }

    public int getSteamVolume() {
        return steamVolume;
    }

    public void setSteamVolume(int volume) {
        steamVolume = volume;

        steamTankCapacity = getSteamVolume() * BoilerMultiblockData.STEAM_PER_VOLUME;
        cooledCoolantCapacity = getSteamVolume() * BoilerMultiblockData.COOLED_COOLANT_PER_VOLUME;
    }
}