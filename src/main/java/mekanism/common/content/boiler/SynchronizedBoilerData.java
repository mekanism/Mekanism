package mekanism.common.content.boiler;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.IHeatTransfer;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IMekanismGasHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.common.capabilities.chemical.MultiblockGasTank;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import mekanism.common.multiblock.SynchronizedData;
import mekanism.common.registries.MekanismGases;
import mekanism.common.tile.TileEntityBoilerCasing;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;

public class SynchronizedBoilerData extends SynchronizedData<SynchronizedBoilerData> implements IHeatTransfer, IMekanismFluidHandler, IMekanismGasHandler {

    public static Object2BooleanMap<String> hotMap = new Object2BooleanOpenHashMap<>();

    public static double CASING_INSULATION_COEFFICIENT = 1;
    public static double CASING_INVERSE_CONDUCTION_COEFFICIENT = 1;
    public static double BASE_BOIL_TEMP = 100 - (TemperatureUnit.AMBIENT.zeroOffset - TemperatureUnit.CELSIUS.zeroOffset);

    public BoilerTank waterTank;
    public MultiblockGasTank<TileEntityBoilerCasing> steamTank;

    public double lastEnvironmentLoss;
    public int lastBoilRate;
    public int lastMaxBoil;

    public boolean clientHot;

    public double temperature;

    public double heatToAbsorb;

    public double heatCapacity = 1_000;

    public int superheatingElements;

    public int waterVolume;

    public int steamVolume;

    public Coord4D upperRenderLocation;

    public Set<ValveData> valves = new ObjectOpenHashSet<>();
    private List<IExtendedFluidTank> fluidTanks;
    private List<IChemicalTank<Gas, GasStack>> gasTanks;

    public SynchronizedBoilerData(TileEntityBoilerCasing tile) {
        waterTank = BoilerTank.create(tile, () -> tile.structure == null ? 0 : tile.structure.waterVolume * BoilerUpdateProtocol.WATER_PER_TANK,
              fluid -> fluid.getFluid().isIn(FluidTags.WATER));
        fluidTanks = Collections.singletonList(waterTank);
        steamTank = MultiblockGasTank.create(tile, () -> tile.structure == null ? 0 : tile.structure.steamVolume * BoilerUpdateProtocol.STEAM_PER_TANK,
              gas -> gas == MekanismGases.STEAM.getGas());
        gasTanks = Collections.singletonList(steamTank);
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

    /**
     * @return how much heat energy is needed to convert one unit of water into steam
     */
    public static double getHeatEnthalpy() {
        return MekanismConfig.general.maxEnergyPerSteam.get().divideToLevel(MekanismConfig.general.energyPerHeat.get());
    }

    public double getHeatAvailable() {
        double heatAvailable = (temperature - BASE_BOIL_TEMP) * locations.size();
        return Math.min(heatAvailable, superheatingElements * MekanismConfig.general.superheatingHeatTransfer.get());
    }

    @Override
    public double getTemp() {
        return temperature;
    }

    @Override
    public double getInverseConductionCoefficient() {
        return CASING_INVERSE_CONDUCTION_COEFFICIENT * locations.size();
    }

    @Override
    public double getInsulationCoefficient(Direction side) {
        return CASING_INSULATION_COEFFICIENT * locations.size();
    }

    @Override
    public void transferHeatTo(double heat) {
        heatToAbsorb += heat;
    }

    @Override
    public double[] simulateHeat() {
        double invConduction = IHeatTransfer.AIR_INVERSE_COEFFICIENT + (CASING_INSULATION_COEFFICIENT + CASING_INVERSE_CONDUCTION_COEFFICIENT) * locations.size();
        double heatToTransfer = temperature / invConduction;
        transferHeatTo(-heatToTransfer);
        return new double[]{0, heatToTransfer};
    }

    @Override
    public double applyTemperatureChange() {
        temperature += heatToAbsorb / locations.size();
        heatToAbsorb = 0;
        return temperature;
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
}