package mekanism.generators.common.content.turbine;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IMekanismGasHandler;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.AutomationType;
import mekanism.common.capabilities.chemical.MultiblockGasTank;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.VariableCapacityEnergyContainer;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.multiblock.SynchronizedData;
import mekanism.common.tile.TileEntityGasTank.GasMode;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;

public class SynchronizedTurbineData extends SynchronizedData<SynchronizedTurbineData> implements IMekanismGasHandler, IMekanismStrictEnergyHandler {

    public static final float ROTATION_THRESHOLD = 0.001F;
    public static Object2FloatMap<String> clientRotationMap = new Object2FloatOpenHashMap<>();

    public final MultiblockGasTank<TileEntityTurbineCasing> gasTank;
    private final List<IChemicalTank<Gas, GasStack>> gasTanks;

    public IExtendedFluidTank ventTank;
    public List<IExtendedFluidTank> ventTanks;

    public IEnergyContainer energyContainer;
    public List<IEnergyContainer> energyContainers;

    public GasMode dumpMode = GasMode.IDLE;

    public int blades;
    public int vents;
    public int coils;
    public int condensers;

    public int lowerVolume;

    public Coord4D complex;

    public int lastSteamInput;
    public int newSteamInput;

    public int clientDispersers;
    public int clientFlow;
    public float clientRotation;

    public SynchronizedTurbineData(TileEntityTurbineCasing tile) {
        gasTanks = Collections.singletonList(gasTank = new TurbineGasTank(tile));
        ventTank = VariableCapacityFluidTank.create(() -> tile.structure == null ? 1_000 : tile.structure.condensers * MekanismGeneratorsConfig.generators.condenserRate.get(),
              (stack, automationType) -> automationType != AutomationType.EXTERNAL || tile.structure != null, BasicFluidTank.internalOnly,
              fluid -> fluid.getFluid().isIn(FluidTags.WATER), null);
        ventTanks = Collections.singletonList(ventTank);
        energyContainer = VariableCapacityEnergyContainer.create(() -> tile.structure == null ? 0 : getEnergyCapacity(),
              automationType -> automationType != AutomationType.EXTERNAL || tile.structure != null, BasicEnergyContainer.internalOnly, null);
        energyContainers = Collections.singletonList(energyContainer);
    }

    public void setTankData(@Nonnull List<IChemicalTank<Gas, GasStack>> toCopy) {
        for (int i = 0; i < toCopy.size(); i++) {
            if (i < gasTanks.size()) {
                //Copy it via NBT to ensure that we set it using the "unsafe" method in case there is a problem with the types somehow
                gasTanks.get(i).deserializeNBT(toCopy.get(i).serializeNBT());
            }
        }
    }

    public void setContainerData(@Nonnull List<IEnergyContainer> toCopy) {
        for (int i = 0; i < toCopy.size(); i++) {
            if (i < energyContainers.size()) {
                //Copy it via NBT to ensure that we set it using the "unsafe" method in case there is a problem with the types somehow
                energyContainers.get(i).deserializeNBT(toCopy.get(i).serializeNBT());
            }
        }
    }

    public int getDispersers() {
        return (volLength - 2) * (volWidth - 2) - 1;
    }

    public int getSteamCapacity() {
        return lowerVolume * TurbineUpdateProtocol.GAS_PER_TANK;
    }

    public double getEnergyCapacity() {
        return volume * 16_000_000D; //16 MJ energy capacity per volume
    }

    @Nonnull
    @Override
    public List<? extends IChemicalTank<Gas, GasStack>> getGasTanks(@Nullable Direction side) {
        return gasTanks;
    }

    @Nonnull
    @Override
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
        return energyContainers;
    }
}