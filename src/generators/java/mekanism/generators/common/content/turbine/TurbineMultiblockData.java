package mekanism.generators.common.content.turbine;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import mekanism.api.AutomationType;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.math.MathUtils;
import mekanism.common.capabilities.energy.VariableCapacityEnergyContainer;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerChemicalTankWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.lib.multiblock.IValveHandler.ValveData;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.lib.transaction.SimpleLongJournal;
import mekanism.common.tile.TileEntityChemicalTank.GasMode;
import mekanism.common.util.ChemicalUtils;
import mekanism.common.util.EnergyUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.ResourceUtils;
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
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class TurbineMultiblockData extends MultiblockData {

    public static final float ROTATION_THRESHOLD = 0.001F;
    public static final Object2FloatMap<UUID> clientRotationMap = new Object2FloatOpenHashMap<>();

    private final List<BlockCapabilityCache<ResourceHandler<FluidResource>, @Nullable Direction>> fluidOutputTargets = new ArrayList<>();
    private final List<BlockCapabilityCache<EnergyHandler, @Nullable Direction>> energyOutputTargets = new ArrayList<>();
    @ContainerSync
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getSteam", "getSteamCapacity", "getSteamNeeded",
                                                                                        "getSteamFilledPercentage"}, docPlaceholder = "steam tank")
    public IChemicalTank chemicalTank;
    @ContainerSync
    public IFluidTank ventTank;
    @ContainerSync
    private final IEnergyContainer energyContainer;
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

    @Nullable
    public BlockPos complex;

    @ContainerSync
    @SyntheticComputerMethod(getter = "getLastSteamInputRate")
    public long lastSteamInput;
    public final SteamInput steamInputJournal = new SteamInput();

    @ContainerSync
    @SyntheticComputerMethod(getter = "getFlowRate")
    public int clientFlow;

    public float clientRotation;
    public float prevSteamScale;

    public TurbineMultiblockData(TileEntityTurbineCasing tile) {
        super(tile);
        chemicalTanks.add(chemicalTank = new TurbineChemicalTank(this, createSaveAndComparator()));
        fluidTanks.add(ventTank = VariableCapacityFluidTank.output(this, () -> isFormed() ? (long) condensers * MekanismGeneratorsConfig.generators.condenserRate.get() : FluidType.BUCKET_VOLUME,
              fluid -> fluid.is(FluidTags.WATER), this));
        energyContainer = VariableCapacityEnergyContainer.create(this::getEnergyCapacity, _ -> isFormed(), automationType -> automationType.isInternal() && isFormed(), this);
    }

    @Override
    public IEnergyContainer energyContainer() {
        return energyContainer;
    }

    @Override
    protected void updateEjectors(Level world) {
        fluidOutputTargets.clear();
        energyOutputTargets.clear();
        for (Map.Entry<BlockPos, ValveData> entry : valves.entrySet()) {
            TileEntityTurbineValve tile = WorldUtils.getTileEntity(TileEntityTurbineValve.class, world, entry.getKey());
            if (tile != null) {
                tile.addEnergyTargetCapability(energyOutputTargets, entry.getValue().side);
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

        lastSteamInput = steamInputJournal.getSteamInputAndReset();
        long stored = chemicalTank.amountAsLong();
        double flowRate = 0;

        long energyNeeded = energyContainer.getNeededAsLong();
        int flow = 0;
        if (stored > 0 && energyNeeded > 0) {
            double energyMultiplier = getEnergyMultiplier();
            if (energyMultiplier >= Mth.EPSILON) {
                int steamDivisor = MekanismGeneratorsConfig.generators.turbineSteamDivisor.get();
                double proportion = stored / (double) getSteamCapacity();
                double origRate = getMaxFlowRateDouble();
                double rate = Math.min(Math.min(stored, origRate), (energyNeeded / energyMultiplier) * steamDivisor) * proportion;
                int amountGenerated = MathUtils.clampToInt(energyMultiplier * (rate / steamDivisor));
                if (rate > Mth.EPSILON && amountGenerated > 0) {
                    flow = MathUtils.clampToInt(rate);
                    flowRate = rate / origRate;
                    try (Transaction transaction = Transaction.openRoot()) {
                        //TODO - 26.1: Re-evaluate these checks and the above math
                        if (energyContainer.insert(amountGenerated, transaction, AutomationType.INTERNAL) == amountGenerated &&
                            chemicalTank.extract(chemicalTank.resource(), flow, transaction, AutomationType.INTERNAL) == flow) {
                            int waterProduced = Math.min(flow, condensers * MekanismGeneratorsConfig.generators.condenserRate.get());
                            if (ventTank.insert(FluidResource.of(Fluids.WATER), waterProduced, transaction, AutomationType.INTERNAL) == waterProduced) {
                                transaction.commit();
                            }
                        }
                    }
                }
            }
        }
        clientFlow = flow;
        if (!fluidOutputTargets.isEmpty() && !ventTank.isEmpty()) {
            //Note: We know that the tank has whatever amount it has stored, we can just perform the simulated extraction
            ResourceUtils.emit(fluidOutputTargets, ventTank, null);
        }
        EnergyUtils.emit(energyOutputTargets, energyContainer, null);

        if (dumpMode != GasMode.IDLE && !chemicalTank.isEmpty()) {
            ChemicalUtils.dump(chemicalTank, dumpMode, getDumpingAmount(chemicalTank.amountAsLong()));
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
        return Math.clamp(lastSteamInput * 2, stored / 50, stored);
    }

    public void updateVentData(List<VentData> vents) {
        this.ventData = vents;
        this.vents = this.ventData.size();
    }

    private double getMaxFlowRateDouble() {
        double rate = lowerVolume * (getDispersers() * MekanismGeneratorsConfig.generators.turbineDisperserChemicalFlow.get());
        return Math.min(rate, vents * MekanismGeneratorsConfig.generators.turbineVentChemicalFlow.get());
    }

    private double getEnergyMultiplier() {
        return ((double) MekanismGeneratorsConfig.generators.turbineJoulesPerSteam.get() / TurbineValidator.MAX_BLADES)
               * Math.min(blades, coils * MekanismGeneratorsConfig.generators.turbineBladesPerCoil.get());
    }

    @Override
    public void readUpdateTag(ValueInput input) {
        super.readUpdateTag(input);
        prevSteamScale = input.getFloatOr(SerializationConstants.SCALE, prevSteamScale);
        input.getInt(SerializationConstants.VOLUME).ifPresent(this::setVolume);
        lowerVolume = input.getIntOr(SerializationConstants.LOWER_VOLUME, lowerVolume);
        NBTUtils.readOrEmpty(input, SerializationConstants.CHEMICAL, chemicalTank);
        NBTUtils.readOrEmpty(input, SerializationConstants.FLUID, ventTank);
        input.read(SerializationConstants.COMPLEX, BlockPos.CODEC).ifPresent(value -> complex = value);
        clientRotation = input.getFloatOr(SerializationConstants.ROTATION, clientRotation);
        clientRotationMap.put(inventoryID, clientRotation);
    }

    @Override
    public void writeUpdateTag(ValueOutput output) {
        super.writeUpdateTag(output);
        output.putFloat(SerializationConstants.SCALE, prevSteamScale);
        output.putInt(SerializationConstants.VOLUME, getVolume());
        output.putInt(SerializationConstants.LOWER_VOLUME, lowerVolume);
        NBTUtils.storeNonEmpty(output, SerializationConstants.CHEMICAL, chemicalTank);
        NBTUtils.storeNonEmpty(output, SerializationConstants.FLUID, ventTank);
        if (complex != null) {
            output.store(SerializationConstants.COMPLEX, BlockPos.CODEC, complex);
        }
        output.putFloat(SerializationConstants.ROTATION, clientRotation);
    }

    @ComputerMethod
    public int getDispersers() {
        return (length() - 2) * (width() - 2) - 1;
    }

    public long getSteamCapacity() {
        return lowerVolume * MekanismGeneratorsConfig.generators.turbineChemicalPerTank.get();
    }

    private long getEnergyCapacity() {
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
        return ContainerType.CHEMICAL.getRedstoneSignalFromContainer(chemicalTank);
    }

    @ComputerMethod
    public int getProductionRate() {
        return MathUtils.clampToInt(getEnergyMultiplier() * clientFlow / MekanismGeneratorsConfig.generators.turbineSteamDivisor.getAsInt());
    }

    @ComputerMethod
    public int getMaxProduction() {
        return MathUtils.clampToInt(getEnergyMultiplier() * getMaxFlowRateDouble());
    }

    @ComputerMethod
    public int getMaxFlowRate() {
        return MathUtils.clampToInt(getMaxFlowRateDouble());
    }

    @ComputerMethod
    public int getMaxWaterOutput() {
        return condensers * MekanismGeneratorsConfig.generators.condenserRate.get();
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

    public static class SteamInput extends SimpleLongJournal {

        public void addSteam(long steamInput, TransactionContext transaction) {
            updateSnapshots(transaction);
            value += steamInput;
        }

        public long getSteamInputAndReset() {
            long steamInput = value;
            value = 0;
            return steamInput;
        }
    }
}
