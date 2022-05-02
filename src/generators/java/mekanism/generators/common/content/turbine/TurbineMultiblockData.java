package mekanism.generators.common.content.turbine;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.MathUtils;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.VariableCapacityEnergyContainer;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.evaporation.EvaporationMultiblockData;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerChemicalTankWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tile.TileEntityChemicalTank.GasMode;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

public class TurbineMultiblockData extends MultiblockData {

    public static final long GAS_PER_TANK = EvaporationMultiblockData.FLUID_PER_TANK;

    public static final float ROTATION_THRESHOLD = 0.001F;
    public static final Object2FloatMap<UUID> clientRotationMap = new Object2FloatOpenHashMap<>();

    @ContainerSync
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getSteam", "getSteamCapacity", "getSteamNeeded", "getSteamFilledPercentage"})
    public IGasTank gasTank;
    @ContainerSync
    public IExtendedFluidTank ventTank;
    public final List<IExtendedFluidTank> ventTanks;
    @ContainerSync
    public IEnergyContainer energyContainer;
    @ContainerSync
    @SyntheticComputerMethod(getter = "getDumpingMode")
    public GasMode dumpMode = GasMode.IDLE;
    private FloatingLong energyCapacity = FloatingLong.ZERO;

    @ContainerSync
    @SyntheticComputerMethod(getter = "getBlades")
    public int blades;
    @ContainerSync
    @SyntheticComputerMethod(getter = "getVents")
    public int vents;
    @ContainerSync
    @SyntheticComputerMethod(getter = "getCoils")
    public int coils;
    @ContainerSync
    @SyntheticComputerMethod(getter = "getCondensers")
    public int condensers;
    @ContainerSync
    public int lowerVolume;

    public BlockPos complex;

    @ContainerSync
    @SyntheticComputerMethod(getter = "getLastSteamInputRate")
    public long lastSteamInput;
    public long newSteamInput;

    @ContainerSync
    @SyntheticComputerMethod(getter = "getFlowRate")
    public long clientFlow;

    public float clientRotation;
    public float prevSteamScale;

    public TurbineMultiblockData(TileEntityTurbineCasing tile) {
        super(tile);
        gasTanks.add(gasTank = new TurbineGasTank(this, tile));
        ventTank = VariableCapacityFluidTank.create(() -> isFormed() ? condensers * MekanismGeneratorsConfig.generators.condenserRate.get() : 1_000,
              (stack, automationType) -> automationType != AutomationType.EXTERNAL || isFormed(), BasicFluidTank.internalOnly,
              fluid -> MekanismTags.Fluids.WATER_LOOKUP.contains(fluid.getFluid()), null);
        ventTanks = Collections.singletonList(ventTank);
        energyContainer = VariableCapacityEnergyContainer.create(this::getEnergyCapacity,
              automationType -> automationType != AutomationType.EXTERNAL || isFormed(), BasicEnergyContainer.internalOnly, null);
        energyContainers.add(energyContainer);
    }

    @Override
    public boolean tick(Level world) {
        boolean needsPacket = super.tick(world);

        lastSteamInput = newSteamInput;
        newSteamInput = 0;
        long stored = gasTank.getStored();
        double flowRate = 0;

        FloatingLong energyNeeded = energyContainer.getNeeded();
        if (stored > 0 && !energyNeeded.isZero()) {
            FloatingLong energyMultiplier = MekanismConfig.general.maxEnergyPerSteam.get().divide(TurbineValidator.MAX_BLADES)
                  .multiply(Math.min(blades, coils * MekanismGeneratorsConfig.generators.turbineBladesPerCoil.get()));
            if (energyMultiplier.isZero()) {
                clientFlow = 0;
            } else {
                double rate = lowerVolume * (getDispersers() * MekanismGeneratorsConfig.generators.turbineDisperserGasFlow.get());
                rate = Math.min(rate, vents * MekanismGeneratorsConfig.generators.turbineVentGasFlow.get());
                double proportion = stored / (double) getSteamCapacity();
                double origRate = rate;
                rate = Math.min(Math.min(stored, rate), energyNeeded.divide(energyMultiplier).doubleValue()) * proportion;
                clientFlow = MathUtils.clampToLong(rate);
                if (clientFlow > 0) {
                    flowRate = rate / origRate;
                    energyContainer.insert(energyMultiplier.multiply(rate), Action.EXECUTE, AutomationType.INTERNAL);
                    gasTank.shrinkStack(clientFlow, Action.EXECUTE);
                    ventTank.setStack(new FluidStack(Fluids.WATER, Math.min(MathUtils.clampToInt(rate), condensers * MekanismGeneratorsConfig.generators.condenserRate.get())));
                }
            }
        } else {
            clientFlow = 0;
        }

        if (dumpMode != GasMode.IDLE && !gasTank.isEmpty()) {
            long amount = gasTank.getStored();
            if (dumpMode == GasMode.DUMPING) {
                gasTank.shrinkStack(getDumpingAmount(amount), Action.EXECUTE);
            } else {//DUMPING_EXCESS
                //Don't allow dumping more than the configured amount
                long targetLevel = MathUtils.clampToLong(gasTank.getCapacity() * MekanismConfig.general.dumpExcessKeepRatio.get());
                if (targetLevel < amount) {
                    gasTank.shrinkStack(Math.min(amount - targetLevel, getDumpingAmount(amount)), Action.EXECUTE);
                }
            }
        }

        float newRotation = (float) flowRate;

        if (Math.abs(newRotation - clientRotation) > TurbineMultiblockData.ROTATION_THRESHOLD) {
            clientRotation = newRotation;
            needsPacket = true;
        }
        float scale = MekanismUtils.getScale(prevSteamScale, gasTank);
        if (scale != prevSteamScale) {
            needsPacket = true;
            prevSteamScale = scale;
        }
        return needsPacket;
    }

    private long getDumpingAmount(long stored) {
        return Math.min(stored, Math.max(stored / 50, lastSteamInput * 2));
    }

    @Override
    public void readUpdateTag(CompoundTag tag) {
        super.readUpdateTag(tag);
        NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE, scale -> prevSteamScale = scale);
        NBTUtils.setIntIfPresent(tag, NBTConstants.VOLUME, this::setVolume);
        NBTUtils.setIntIfPresent(tag, NBTConstants.LOWER_VOLUME, value -> lowerVolume = value);
        NBTUtils.setGasStackIfPresent(tag, NBTConstants.GAS_STORED, value -> gasTank.setStack(value));
        NBTUtils.setBlockPosIfPresent(tag, NBTConstants.COMPLEX, value -> complex = value);
        NBTUtils.setFloatIfPresent(tag, NBTConstants.ROTATION, value -> clientRotation = value);
        clientRotationMap.put(inventoryID, clientRotation);
    }

    @Override
    public void writeUpdateTag(CompoundTag tag) {
        super.writeUpdateTag(tag);
        tag.putFloat(NBTConstants.SCALE, prevSteamScale);
        tag.putInt(NBTConstants.VOLUME, getVolume());
        tag.putInt(NBTConstants.LOWER_VOLUME, lowerVolume);
        tag.put(NBTConstants.GAS_STORED, gasTank.getStack().write(new CompoundTag()));
        tag.put(NBTConstants.COMPLEX, NbtUtils.writeBlockPos(complex));
        tag.putFloat(NBTConstants.ROTATION, clientRotation);
    }

    @ComputerMethod
    public int getDispersers() {
        return (length() - 2) * (width() - 2) - 1;
    }

    public long getSteamCapacity() {
        return lowerVolume * GAS_PER_TANK;
    }

    @Nonnull
    public FloatingLong getEnergyCapacity() {
        return energyCapacity;
    }

    @Override
    public void setVolume(int volume) {
        super.setVolume(volume);
        energyCapacity = FloatingLong.createConst(getVolume() * 16_000_000L); //16 MJ energy capacity per volume
    }

    @Override
    protected int getMultiblockRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(gasTank.getStored(), gasTank.getCapacity());
    }

    @ComputerMethod
    public FloatingLong getProductionRate() {
        FloatingLong energyMultiplier = MekanismConfig.general.maxEnergyPerSteam.get().divide(TurbineValidator.MAX_BLADES)
              .multiply(Math.min(blades, coils * MekanismGeneratorsConfig.generators.turbineBladesPerCoil.get()));
        return energyMultiplier.multiply(clientFlow);
    }

    @ComputerMethod
    public FloatingLong getMaxProduction() {
        FloatingLong energyMultiplier = MekanismConfig.general.maxEnergyPerSteam.get().divide(TurbineValidator.MAX_BLADES)
              .multiply(Math.min(blades, coils * MekanismGeneratorsConfig.generators.turbineBladesPerCoil.get()));
        double rate = lowerVolume * (getDispersers() * MekanismGeneratorsConfig.generators.turbineDisperserGasFlow.get());
        rate = Math.min(rate, vents * MekanismGeneratorsConfig.generators.turbineVentGasFlow.get());
        return energyMultiplier.multiply(rate);
    }

    @ComputerMethod
    public long getMaxFlowRate() {
        double rate = lowerVolume * (getDispersers() * MekanismGeneratorsConfig.generators.turbineDisperserGasFlow.get());
        rate = Math.min(rate, vents * MekanismGeneratorsConfig.generators.turbineVentGasFlow.get());
        return MathUtils.clampToLong(rate);
    }

    @ComputerMethod
    public long getMaxWaterOutput() {
        return (long) condensers * MekanismGeneratorsConfig.generators.condenserRate.get();
    }

    @ComputerMethod(nameOverride = "setDumpingMode")
    public void setDumpMode(GasMode mode) {
        if (dumpMode != mode) {
            dumpMode = mode;
            markDirty();
        }
    }

    //Computer related methods
    @ComputerMethod
    private void incrementDumpingMode() {
        setDumpMode(dumpMode.getNext());
    }

    @ComputerMethod
    private void decrementDumpingMode() {
        setDumpMode(dumpMode.getPrevious());
    }
    //End computer related methods
}