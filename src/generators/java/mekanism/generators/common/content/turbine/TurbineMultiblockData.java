package mekanism.generators.common.content.turbine;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.math.MathUtils;
import mekanism.common.capabilities.energy.VariableCapacityEnergyContainer;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerChemicalTankWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.integration.energy.BlockEnergyCapabilityCache;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.lib.multiblock.IValveHandler.ValveData;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.tile.TileEntityChemicalTank.GasMode;
import mekanism.common.util.CableUtils;
import mekanism.common.util.FluidUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import mekanism.generators.common.tile.turbine.TileEntityTurbineValve;
import mekanism.generators.common.tile.turbine.TileEntityTurbineVent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TurbineMultiblockData extends MultiblockData {

    public static final float ROTATION_THRESHOLD = 0.001F;
    public static final Object2FloatMap<UUID> clientRotationMap = new Object2FloatOpenHashMap<>();

    private final List<BlockCapabilityCache<IFluidHandler, @Nullable Direction>> fluidOutputTargets = new ArrayList<>();
    private final List<BlockEnergyCapabilityCache> energyOutputTargets = new ArrayList<>();
    @ContainerSync
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getSteam", "getSteamCapacity", "getSteamNeeded",
                                                                                        "getSteamFilledPercentage"}, docPlaceholder = "steam tank")
    public IChemicalTank chemicalTank;
    @ContainerSync
    public IExtendedFluidTank ventTank;
    @ContainerSync
    public IEnergyContainer energyContainer;
    @ContainerSync
    @SyntheticComputerMethod(getter = "getDumpingMode")
    public GasMode dumpMode = GasMode.IDLE;
    private long energyCapacity = 0;

    @ContainerSync
    @SyntheticComputerMethod(getter = "getBlades")
    public int blades;
    @ContainerSync
    @SyntheticComputerMethod(getter = "getVents")
    public int vents;
    private List<VentData> ventData = Collections.emptyList();
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
        chemicalTanks.add(chemicalTank = new TurbineChemicalTank(this, createSaveAndComparator()));
        fluidTanks.add(ventTank = VariableCapacityFluidTank.output(this, () -> isFormed() ? condensers * MekanismGeneratorsConfig.generators.condenserRate.get() : FluidType.BUCKET_VOLUME,
              fluid -> fluid.is(FluidTags.WATER), this));
        energyContainer = VariableCapacityEnergyContainer.create(this::getEnergyCapacity, automationType -> isFormed(),
              automationType -> automationType == AutomationType.INTERNAL && isFormed(), this);
        energyContainers.add(energyContainer);
    }

    @Override
    protected void updateEjectors(Level world) {
        fluidOutputTargets.clear();
        energyOutputTargets.clear();
        for (ValveData valve : valves) {
            TileEntityTurbineValve tile = WorldUtils.getTileEntity(TileEntityTurbineValve.class, world, valve.location);
            if (tile != null) {
                tile.addEnergyTargetCapability(energyOutputTargets, valve.side);
            }
        }
        for (VentData data : ventData) {
            TileEntityTurbineVent vent = WorldUtils.getTileEntity(TileEntityTurbineVent.class, world, data.location);
            if (vent != null) {
                vent.addFluidTargetCapability(fluidOutputTargets, data.side);
            }
        }
    }

    @Override
    public boolean tick(ServerLevel world) {
        boolean needsPacket = super.tick(world);

        lastSteamInput = newSteamInput;
        newSteamInput = 0;
        long stored = chemicalTank.getStored();
        double flowRate = 0;

        long energyNeeded = energyContainer.getNeeded();
        if (stored > 0 && energyNeeded > 0L) {
            double energyMultiplier = (MekanismGeneratorsConfig.generators.turbineJoulesPerSteam.get() / (double) TurbineValidator.MAX_BLADES)
                                      * (Math.min(blades, coils * MekanismGeneratorsConfig.generators.turbineBladesPerCoil.get()));
            if (energyMultiplier < Mth.EPSILON) {
                clientFlow = 0;
            } else {
                double rate = getMaxFlowRateDouble();
                double proportion = stored / (double) getSteamCapacity();
                double origRate = rate;
                rate = Math.min(Math.min(stored, rate), (energyNeeded / energyMultiplier) * MekanismGeneratorsConfig.generators.turbineSteamDivisor.getAsInt()) * proportion;
                long amountGenerated = MathUtils.clampToLong(energyMultiplier * (rate / MekanismGeneratorsConfig.generators.turbineSteamDivisor.getAsInt()));
                if (rate > Mth.EPSILON && amountGenerated > 0) {
                    clientFlow = MathUtils.clampToLong(rate);
                    flowRate = rate / origRate;
                    energyContainer.insert(amountGenerated, Action.EXECUTE, AutomationType.INTERNAL);
                    chemicalTank.shrinkStack(clientFlow, Action.EXECUTE);
                    ventTank.insert(new FluidStack(Fluids.WATER, Math.min(MathUtils.clampToInt(rate), condensers * MekanismGeneratorsConfig.generators.condenserRate.get())), Action.EXECUTE, AutomationType.INTERNAL);
                } else {
                    clientFlow = 0;
                }
            }
        } else {
            clientFlow = 0;
        }
        if (!fluidOutputTargets.isEmpty() && !ventTank.isEmpty()) {
            //Note: We know that the tank has whatever amount it has stored, we can the simulated extraction
            ventTank.extract(FluidUtils.emit(fluidOutputTargets, ventTank.getFluid()), Action.EXECUTE, AutomationType.INTERNAL);
        }
        CableUtils.emit(energyOutputTargets, energyContainer);

        if (dumpMode != GasMode.IDLE && !chemicalTank.isEmpty()) {
            long amount = chemicalTank.getStored();
            if (dumpMode == GasMode.DUMPING) {
                chemicalTank.shrinkStack(getDumpingAmount(amount), Action.EXECUTE);
            } else {//DUMPING_EXCESS
                //Don't allow dumping more than the configured amount
                long targetLevel = MathUtils.clampToLong(chemicalTank.getCapacity() * MekanismConfig.general.dumpExcessKeepRatio.get());
                if (targetLevel < amount) {
                    chemicalTank.shrinkStack(Math.min(amount - targetLevel, getDumpingAmount(amount)), Action.EXECUTE);
                }
            }
        }

        float newRotation = (float) flowRate;

        if (Math.abs(newRotation - clientRotation) > ROTATION_THRESHOLD) {
            clientRotation = newRotation;
            needsPacket = true;
        }
        float scale = MekanismUtils.getScale(prevSteamScale, chemicalTank);
        if (MekanismUtils.scaleChanged(scale, prevSteamScale)) {
            needsPacket = true;
            prevSteamScale = scale;
        }
        return needsPacket;
    }

    private long getDumpingAmount(long stored) {
        return Math.min(stored, Math.max(stored / 50, lastSteamInput * 2));
    }

    public void updateVentData(List<VentData> vents) {
        this.ventData = vents;
        this.vents = this.ventData.size();
    }

    private double getMaxFlowRateDouble() {
        double rate = lowerVolume * (getDispersers() * MekanismGeneratorsConfig.generators.turbineDisperserChemicalFlow.get());
        rate = Math.min(rate, vents * MekanismGeneratorsConfig.generators.turbineVentChemicalFlow.get());
        return rate;
    }

    @Override
    public void readUpdateTag(@NotNull ValueInput input) {
        super.readUpdateTag(input);
        prevSteamScale = input.getFloatOr(SerializationConstants.SCALE, prevSteamScale);
        input.getInt(SerializationConstants.VOLUME).ifPresent(this::setVolume);
        lowerVolume = input.getIntOr(SerializationConstants.LOWER_VOLUME, lowerVolume);
        //TODO - 1.21.11: Should this be an orElse empty and then set it regardless?
        input.read(SerializationConstants.CHEMICAL, ChemicalStack.OPTIONAL_CODEC).ifPresent(chemicalTank::setStack);
        input.read(SerializationConstants.FLUID, FluidStack.OPTIONAL_CODEC).ifPresent(ventTank::setStack);
        input.read(SerializationConstants.COMPLEX, BlockPos.CODEC).ifPresent(value -> complex = value);
        clientRotation = input.getFloatOr(SerializationConstants.ROTATION, clientRotation);
        clientRotationMap.put(inventoryID, clientRotation);
    }

    @Override
    public void writeUpdateTag(@NotNull ValueOutput output) {
        super.writeUpdateTag(output);
        output.putFloat(SerializationConstants.SCALE, prevSteamScale);
        output.putInt(SerializationConstants.VOLUME, getVolume());
        output.putInt(SerializationConstants.LOWER_VOLUME, lowerVolume);
        output.store(SerializationConstants.CHEMICAL, ChemicalStack.OPTIONAL_CODEC, chemicalTank.getStack());
        output.store(SerializationConstants.FLUID, FluidStack.OPTIONAL_CODEC, ventTank.getFluid());
        output.store(SerializationConstants.COMPLEX, BlockPos.CODEC, complex);
        output.putFloat(SerializationConstants.ROTATION, clientRotation);
    }

    @ComputerMethod
    public int getDispersers() {
        return (length() - 2) * (width() - 2) - 1;
    }

    public long getSteamCapacity() {
        return lowerVolume * MekanismGeneratorsConfig.generators.turbineChemicalPerTank.get();
    }

    public long getEnergyCapacity() {
        return energyCapacity;
    }

    @Override
    public void setVolume(int volume) {
        if (getVolume() != volume) {
            super.setVolume(volume);
            energyCapacity = volume * MekanismGeneratorsConfig.generators.turbineEnergyCapacityPerVolume.get();
        }
    }

    @Override
    protected int getMultiblockRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(chemicalTank.getStored(), chemicalTank.getCapacity());
    }

    @ComputerMethod
    public long getProductionRate() {
        double energyMultiplier = ((double) MekanismGeneratorsConfig.generators.turbineJoulesPerSteam.get() / TurbineValidator.MAX_BLADES)
                                  * (Math.min(blades, coils * MekanismGeneratorsConfig.generators.turbineBladesPerCoil.get()));
        return MathUtils.clampToLong(energyMultiplier * clientFlow / MekanismGeneratorsConfig.generators.turbineSteamDivisor.getAsInt());
    }

    @ComputerMethod
    public long getMaxProduction() {
        double energyMultiplier = ((double) MekanismGeneratorsConfig.generators.turbineJoulesPerSteam.get() / TurbineValidator.MAX_BLADES)
                                  * (Math.min(blades, coils * MekanismGeneratorsConfig.generators.turbineBladesPerCoil.get()));
        double rate = getMaxFlowRateDouble();
        return MathUtils.clampToLong(energyMultiplier * rate);
    }

    @ComputerMethod
    public long getMaxFlowRate() {
        double rate = getMaxFlowRateDouble();
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
    void incrementDumpingMode() {
        setDumpMode(dumpMode.getNext());
    }

    @ComputerMethod
    void decrementDumpingMode() {
        setDumpMode(dumpMode.getPrevious());
    }
    //End computer related methods

    public record VentData(BlockPos location, Direction side) {
    }
}
