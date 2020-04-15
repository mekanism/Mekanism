package mekanism.common.content.boiler;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mekanism.api.Coord4D;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IMekanismGasHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.HeatAPI.HeatTransfer;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.inventory.AutomationType;
import mekanism.common.capabilities.chemical.MultiblockGasTank;
import mekanism.common.capabilities.heat.ITileHeatHandler;
import mekanism.common.capabilities.heat.MultiblockHeatCapacitor;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import mekanism.common.multiblock.SynchronizedData;
import mekanism.common.registries.MekanismGases;
import mekanism.common.tile.TileEntityBoilerCasing;
import mekanism.common.util.HeatUtils;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;

public class SynchronizedBoilerData extends SynchronizedData<SynchronizedBoilerData> implements IMekanismFluidHandler, IMekanismGasHandler, ITileHeatHandler {

    public static Object2BooleanMap<UUID> hotMap = new Object2BooleanOpenHashMap<>();

    public static final double CASING_HEAT_CAPACITY = 5;
    public static final double CASING_INVERSE_INSULATION_COEFFICIENT = 10;
    public static final double CASING_INVERSE_CONDUCTION_COEFFICIENT = 1;

    public static final int WATER_PER_VOLUME = 16_000;
    public static final long STEAM_PER_VOLUME = 160_000;

    public BoilerTank waterTank;
    public MultiblockGasTank<TileEntityBoilerCasing> steamTank;
    public MultiblockHeatCapacitor<TileEntityBoilerCasing> heatCapacitor;

    public double lastEnvironmentLoss;
    public int lastBoilRate;
    public int lastMaxBoil;

    public boolean clientHot;

    public int superheatingElements;

    private int waterVolume;
    private long steamVolume;
    private int waterTankCapacity;
    private long steamTankCapacity;

    public Coord4D upperRenderLocation;

    public Set<ValveData> valves = new ObjectOpenHashSet<>();
    private List<IExtendedFluidTank> fluidTanks;
    private List<IChemicalTank<Gas, GasStack>> gasTanks;
    private List<IHeatCapacitor> heatCapacitors;

    public SynchronizedBoilerData(TileEntityBoilerCasing tile) {
        waterTank = BoilerTank.create(tile, () -> tile.structure == null ? 0 : tile.structure.getWaterTankCapacity(), fluid -> fluid.getFluid().isIn(FluidTags.WATER));
        fluidTanks = Collections.singletonList(waterTank);
        steamTank = MultiblockGasTank.create(tile, () -> tile.structure == null ? 0 : tile.structure.getSteamTankCapacity(),
            (stack, automationType) -> automationType != AutomationType.EXTERNAL || tile.structure != null, (stack, automationType) -> automationType != AutomationType.EXTERNAL,
            gas -> gas == MekanismGases.STEAM.getGas());
        gasTanks = Collections.singletonList(steamTank);
        heatCapacitor = MultiblockHeatCapacitor.create(tile,
            CASING_HEAT_CAPACITY,
            () -> CASING_INVERSE_INSULATION_COEFFICIENT * locations.size(),
            () -> CASING_INVERSE_INSULATION_COEFFICIENT * locations.size());
        heatCapacitors = Collections.singletonList(heatCapacitor);
    }

    @Override
    public void onCreated() {
        // update the heat capacity now that we've read
        heatCapacitor.setHeatCapacity(CASING_HEAT_CAPACITY * locations.size(), true);
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

    public int getWaterVolume() {
        return waterVolume;
    }

    public void setWaterVolume(int volume) {
        waterVolume = volume;
        waterTankCapacity = getWaterVolume() * SynchronizedBoilerData.WATER_PER_VOLUME;
    }

    public long getSteamVolume() {
        return steamVolume;
    }

    public void setSteamVolume(long volume) {
        steamVolume = volume;
        steamTankCapacity = getSteamVolume() * SynchronizedBoilerData.STEAM_PER_VOLUME;
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