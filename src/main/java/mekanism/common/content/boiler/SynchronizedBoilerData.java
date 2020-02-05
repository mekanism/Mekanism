package mekanism.common.content.boiler;

import it.unimi.dsi.fastutil.objects.AbstractObject2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.IHeatTransfer;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import mekanism.common.multiblock.SynchronizedData;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;

public class SynchronizedBoilerData extends SynchronizedData<SynchronizedBoilerData> implements IHeatTransfer {

    public static AbstractObject2BooleanMap<String> clientHotMap = new Object2BooleanOpenHashMap<>();

    public static double CASING_INSULATION_COEFFICIENT = 1;
    public static double CASING_INVERSE_CONDUCTION_COEFFICIENT = 1;
    public static double BASE_BOIL_TEMP = 100 - (TemperatureUnit.AMBIENT.zeroOffset - TemperatureUnit.CELSIUS.zeroOffset);

    @Nonnull
    public FluidStack waterStored = FluidStack.EMPTY;
    @Nonnull
    public FluidStack prevWater = FluidStack.EMPTY;
    //TODO: Do we want to make the boiler have steam be a gas instead of a fluid?
    @Nonnull
    public FluidStack steamStored = FluidStack.EMPTY;
    @Nonnull
    public FluidStack prevSteam = FluidStack.EMPTY;

    public double lastEnvironmentLoss;
    public int lastBoilRate;
    public int lastMaxBoil;

    public boolean clientHot;

    public double temperature;

    public double heatToAbsorb;

    public double heatCapacity = 1000;

    public int superheatingElements;

    public int waterVolume;

    public int steamVolume;

    public Coord4D upperRenderLocation;

    public Set<ValveData> valves = new ObjectOpenHashSet<>();

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

    public boolean needsRenderUpdate() {
        if ((waterStored.isEmpty() && !prevWater.isEmpty()) || (!waterStored.isEmpty() && prevWater.isEmpty())) {
            return true;
        }
        if (!waterStored.isEmpty()) {
            if ((waterStored.getFluid() != prevWater.getFluid()) || (waterStored.getAmount() != prevWater.getAmount())) {
                return true;
            }
        }
        if ((steamStored.isEmpty() && !prevSteam.isEmpty()) || (!steamStored.isEmpty() && prevSteam.isEmpty())) {
            return true;
        }
        if (!steamStored.isEmpty()) {
            return (steamStored.getFluid() != prevSteam.getFluid()) || (steamStored.getAmount() != prevSteam.getAmount());
        }
        return false;
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
}