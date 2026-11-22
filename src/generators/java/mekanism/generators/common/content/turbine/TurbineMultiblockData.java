package mekanism.generators.common.content.turbine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
import mekanism.common.util.EnergyUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.ResourceUtils;
import mekanism.common.util.ValueUtils;
import mekanism.common.util.WorldUtils;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import mekanism.generators.common.tile.turbine.TileEntityTurbineValve;
import mekanism.generators.common.tile.turbine.TileEntityTurbineVent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
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
    protected void updateEjectors(ServerLevel level) {
        fluidOutputTargets.clear();
        energyOutputTargets.clear();
        for (Map.Entry<BlockPos, ValveData> entry : valves.entrySet()) {
            TileEntityTurbineValve tile = WorldUtils.getTileEntity(TileEntityTurbineValve.class, level, entry.getKey());
            if (tile != null) {
                tile.addEnergyTargetCapability(level, energyOutputTargets, entry.getValue().side);
            }
        }
        for (VentData data : ventData) {
            TileEntityTurbineVent vent = WorldUtils.getTileEntity(TileEntityTurbineVent.class, level, data.location);
            if (vent != null) {
                vent.addFluidTargetCapability(level, fluidOutputTargets, data.side);
            }
        }
    }

    @Override
    public boolean tick(ServerLevel world) {
        boolean needsPacket = super.tick(world);

        lastSteamInput = steamInputJournal.getSteamInputAndReset();
        long stored = chemicalTank.amountAsLong();
        double flowRate = 0;
        int flow = 0;
        if (stored > 0) {
            try (Transaction transaction = Transaction.openRoot()) {
                double proportion = stored / (double) chemicalTank.capacityAsLong(chemicalTank.resource());
                double origRate = getMaxFlowRateDouble();
                double availableRate = Math.min(stored, origRate);
                double rate = availableRate * proportion;
                if (rate >= 1) {
                    flow = MathUtils.clampToInt(rate);
                    if (chemicalTank.extract(chemicalTank.resource(), flow, transaction, AutomationType.INTERNAL) == flow) {
                        int energyProduced = MathUtils.clampToInt(rate * getEnergyMultiplier());
                        if (energyProduced > 0) {
                            //Insert energy allowing any excess to be voided/discharged into the world
                            energyContainer.insert(energyProduced, transaction, AutomationType.INTERNAL);
                        }
                        flowRate = rate / origRate;
                        int waterProduced = Math.min(flow, getMaxWaterOutput());
                        if (ventTank.insert(FluidResource.of(Fluids.WATER), waterProduced, transaction, AutomationType.INTERNAL) == waterProduced) {
                            //This should always return true
                            transaction.commit();
                        }
                    } else {
                        //We were unable to actually have anything flow, so mark that as the case
                        flow = 0;
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

    public void updateVentData(List<VentData> vents) {
        this.ventData = vents;
        this.vents = this.ventData.size();
    }

    private double getMaxFlowRateDouble() {
        double rate = lowerVolume * (getDispersers() * MekanismGeneratorsConfig.generators.turbineDisperserChemicalFlow.get());
        return Math.min(rate, vents * MekanismGeneratorsConfig.generators.turbineVentChemicalFlow.get());
    }

    private double getEnergyMultiplier() {
        double baseMultiplier = ((double) MekanismGeneratorsConfig.generators.turbineEnergyPerSteam.get() / TurbineValidator.MAX_BLADES)
                                * Math.min(blades, coils * MekanismGeneratorsConfig.generators.turbineBladesPerCoil.get());
        return baseMultiplier / MekanismGeneratorsConfig.generators.turbineSteamDivisor.getAsInt();
    }

    @Override
    public void readUpdateTag(ValueInput input) {
        super.readUpdateTag(input);
        prevSteamScale = input.getFloatOr(SerializationConstants.SCALE, 0);
        setVolume(input.getIntOr(SerializationConstants.VOLUME, 0));
        lowerVolume = input.getIntOr(SerializationConstants.LOWER_VOLUME, 0);
        ValueUtils.readOrEmpty(input, SerializationConstants.CHEMICAL, chemicalTank);
        ValueUtils.readOrEmpty(input, SerializationConstants.FLUID, ventTank);
        complex = input.read(SerializationConstants.COMPLEX, BlockPos.CODEC).orElse(null);
        clientRotation = input.getFloatOr(SerializationConstants.ROTATION, 0);
    }

    @Override
    public void writeUpdateTag(ValueOutput output) {
        super.writeUpdateTag(output);
        output.putFloat(SerializationConstants.SCALE, prevSteamScale);
        output.putInt(SerializationConstants.VOLUME, getVolume());
        output.putInt(SerializationConstants.LOWER_VOLUME, lowerVolume);
        ValueUtils.storeNonEmpty(output, SerializationConstants.CHEMICAL, chemicalTank);
        ValueUtils.storeNonEmpty(output, SerializationConstants.FLUID, ventTank);
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
        return MathUtils.clampToInt(getEnergyMultiplier() * clientFlow);
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
