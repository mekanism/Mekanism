package mekanism.common.content.boiler;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.IHeatTransfer;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import mekanism.common.multiblock.SynchronizedData;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tile.TileEntityBoilerCasing;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;

public class SynchronizedBoilerData extends SynchronizedData<SynchronizedBoilerData> implements IHeatTransfer, IMekanismFluidHandler {

    public static Object2BooleanMap<String> clientHotMap = new Object2BooleanOpenHashMap<>();

    public static double CASING_INSULATION_COEFFICIENT = 1;
    public static double CASING_INVERSE_CONDUCTION_COEFFICIENT = 1;
    public static double BASE_BOIL_TEMP = 100 - (TemperatureUnit.AMBIENT.zeroOffset - TemperatureUnit.CELSIUS.zeroOffset);

    public BoilerTank waterTank;
    //TODO: Do we want to make the boiler have steam be a gas instead of a fluid?
    public BoilerTank steamTank;

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

    public SynchronizedBoilerData(TileEntityBoilerCasing tile) {
        waterTank = BoilerTank.create(tile, () -> tile.structure == null ? 0 : tile.structure.waterVolume * BoilerUpdateProtocol.WATER_PER_TANK,
              fluid -> fluid.getFluid().isIn(FluidTags.WATER));
        steamTank = BoilerTank.create(tile, () -> tile.structure == null ? 0 : tile.structure.steamVolume * BoilerUpdateProtocol.STEAM_PER_TANK,
              fluid -> fluid.getFluid().isIn(MekanismTags.Fluids.STEAM));
        fluidTanks = Arrays.asList(waterTank, steamTank);
    }

    public void setTankData(@Nonnull List<IExtendedFluidTank> toCopy) {
        for (int i = 0; i < toCopy.size(); i++) {
            if (i < fluidTanks.size()) {
                //Copy it via NBT to ensure that we set it using the "unsafe" method in case there is a problem with the types somehow
                fluidTanks.get(i).deserializeNBT(toCopy.get(i).serializeNBT());
            }
        }
    }

    /**
     * @return how much heat energy is needed to convert one unit of water into steam
     */
    public static double getHeatEnthalpy() {
        return MekanismConfig.general.maxEnergyPerSteam.get() / MekanismConfig.general.energyPerHeat.get();
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
}