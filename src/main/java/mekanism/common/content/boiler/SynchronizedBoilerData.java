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
import mekanism.api.heat.HeatPacket;
import mekanism.api.heat.HeatPacket.TransferType;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.chemical.MultiblockGasTank;
import mekanism.common.capabilities.heat.ITileHeatHandler;
import mekanism.common.capabilities.heat.MultiblockHeatCapacitor;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import mekanism.common.multiblock.SynchronizedData;
import mekanism.common.registries.MekanismGases;
import mekanism.common.tile.TileEntityBoilerCasing;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;

public class SynchronizedBoilerData extends SynchronizedData<SynchronizedBoilerData> implements IMekanismFluidHandler, IMekanismGasHandler, ITileHeatHandler {

    public static Object2BooleanMap<UUID> hotMap = new Object2BooleanOpenHashMap<>();

    public static final FloatingLong CASING_HEAT_CAPACITY = FloatingLong.ONE;
    public static final FloatingLong CASING_INVERSE_INSULATION_COEFFICIENT = FloatingLong.createConst(10);
    public static final FloatingLong CASING_INVERSE_CONDUCTION_COEFFICIENT = FloatingLong.ONE;
    public static final FloatingLong BASE_BOIL_TEMP = FloatingLong.createConst(100).subtract(TemperatureUnit.AMBIENT.zeroOffset.subtract(TemperatureUnit.CELSIUS.zeroOffset)).copyAsConst();

    public BoilerTank waterTank;
    public MultiblockGasTank<TileEntityBoilerCasing> steamTank;
    public MultiblockHeatCapacitor<TileEntityBoilerCasing> heatCapacitor;

    public FloatingLong lastEnvironmentLoss = FloatingLong.ZERO;
    public int lastBoilRate;
    public int lastMaxBoil;

    public boolean clientHot;

    public int superheatingElements;

    private int waterVolume;
    private int steamVolume;
    private int waterTankCapacity;
    private int steamTankCapacity;

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
            () -> CASING_HEAT_CAPACITY.multiply(locations.size()),
            () -> CASING_INVERSE_INSULATION_COEFFICIENT.multiply(locations.size()),
            () -> CASING_INVERSE_INSULATION_COEFFICIENT.multiply(locations.size()),
            true, false);
        heatCapacitors = Collections.singletonList(heatCapacitor);
    }

    public void setFluidTankData(@Nonnull List<IExtendedFluidTank> toCopy) {
        for (int i = 0; i < toCopy.size(); i++) {
            if (i < fluidTanks.size()) {
                //Copy it via NBT to ensure that we set it using the "unsafe" method in case there is a problem with the types somehow
                fluidTanks.get(i).deserializeNBT(toCopy.get(i).serializeNBT());
            }
        }
    }

    public void setGasTankData(@Nonnull List<IChemicalTank<Gas, GasStack>> toCopy) {
        for (int i = 0; i < toCopy.size(); i++) {
            if (i < gasTanks.size()) {
                //Copy it via NBT to ensure that we set it using the "unsafe" method in case there is a problem with the types somehow
                gasTanks.get(i).deserializeNBT(toCopy.get(i).serializeNBT());
            }
        }
    }

    public void setHeatCapacitorData(@Nonnull List<IHeatCapacitor> toCopy) {
        for (int i = 0; i < toCopy.size(); i++) {
            if (i < heatCapacitors.size()) {
                //Copy it via NBT to ensure that we set it using the "unsafe" method in case there is a problem with the types somehow
                heatCapacitors.get(i).deserializeNBT(toCopy.get(i).serializeNBT());
            }
        }
    }

    public FloatingLong getHeatAvailable() {
        FloatingLong heatAvailable = heatCapacitor.getTemperature().subtract(BASE_BOIL_TEMP).multiply(heatCapacitor.getHeatCapacity())
              .multiply(MekanismConfig.general.boilerWaterConductivity.get());
        return heatAvailable.min(MekanismConfig.general.superheatingHeatTransfer.get().multiply(superheatingElements));
    }

    @Override
    public HeatTransfer simulate() {
        FloatingLong invConduction = HeatAPI.AIR_INVERSE_COEFFICIENT.add(CASING_INVERSE_INSULATION_COEFFICIENT.add(CASING_INVERSE_CONDUCTION_COEFFICIENT).multiply(locations.size()));
        FloatingLong heatToTransfer = heatCapacitor.getTemperature().divide(invConduction);

        heatCapacitor.handleHeat(new HeatPacket(TransferType.EMIT, heatToTransfer));
        return new HeatTransfer(FloatingLong.ZERO, heatToTransfer);
    }

    public int getWaterTankCapacity() {
        return waterTankCapacity;
    }

    public int getSteamTankCapacity() {
        return steamTankCapacity;
    }

    public int getWaterVolume() {
        return waterVolume;
    }

    public void setWaterVolume(int volume) {
        waterVolume = volume;
        waterTankCapacity = getWaterVolume() * BoilerUpdateProtocol.WATER_PER_TANK;
    }

    public int getSteamVolume() {
        return steamVolume;
    }

    public void setSteamVolume(int volume) {
        steamVolume = volume;
        steamTankCapacity = getSteamVolume() * BoilerUpdateProtocol.STEAM_PER_TANK;
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