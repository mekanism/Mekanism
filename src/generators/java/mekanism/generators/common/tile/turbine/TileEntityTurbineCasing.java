package mekanism.generators.common.tile.turbine;

import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.MathUtils;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.container.sync.SyncableGasStack;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.multiblock.MultiblockCache;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.multiblock.UpdateProtocol;
import mekanism.common.tile.TileEntityGasTank.GasMode;
import mekanism.common.tile.TileEntityMultiblock;
import mekanism.common.tile.interfaces.IHasGasMode;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.turbine.SynchronizedTurbineData;
import mekanism.generators.common.content.turbine.TurbineCache;
import mekanism.generators.common.content.turbine.TurbineUpdateProtocol;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.FluidStack;

public class TileEntityTurbineCasing extends TileEntityMultiblock<SynchronizedTurbineData> implements IHasGasMode {

    public float prevSteamScale;

    public TileEntityTurbineCasing() {
        this(GeneratorsBlocks.TURBINE_CASING);
    }

    public TileEntityTurbineCasing(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (structure != null && isRendering) {
            structure.lastSteamInput = structure.newSteamInput;
            structure.newSteamInput = 0;
            long stored = structure.gasTank.getStored();
            double flowRate = 0;

            FloatingLong energyNeeded = structure.energyContainer.getNeeded();
            if (stored > 0 && !energyNeeded.isZero()) {
                FloatingLong energyMultiplier = MekanismConfig.general.maxEnergyPerSteam.get().divide(TurbineUpdateProtocol.MAX_BLADES)
                                                .multiply(Math.min(structure.blades, structure.coils * MekanismGeneratorsConfig.generators.turbineBladesPerCoil.get()));
                double rate = structure.lowerVolume * (structure.getDispersers() * MekanismGeneratorsConfig.generators.turbineDisperserGasFlow.get());
                rate = Math.min(rate, structure.vents * MekanismGeneratorsConfig.generators.turbineVentGasFlow.get());

                double proportion = stored / (double) structure.getSteamCapacity();
                double origRate = rate;
                rate = Math.min(Math.min(stored, rate), energyNeeded.divide(energyMultiplier).doubleValue()) * proportion;

                flowRate = rate / origRate;
                structure.energyContainer.insert(energyMultiplier.multiply(rate), Action.EXECUTE, AutomationType.INTERNAL);

                if (!structure.gasTank.isEmpty()) {
                    structure.gasTank.shrinkStack((long) rate, Action.EXECUTE);
                }
                structure.clientFlow = (long) rate;
                structure.ventTank.setStack(new FluidStack(Fluids.WATER, Math.min(MathUtils.clampToInt(rate), structure.condensers * MekanismGeneratorsConfig.generators.condenserRate.get())));
            } else {
                structure.clientFlow = 0;
            }

            if (structure.dumpMode == GasMode.DUMPING && !structure.gasTank.isEmpty()) {
                long amount = structure.gasTank.getStored();
                structure.gasTank.shrinkStack(Math.min(amount, Math.max(amount / 50, structure.lastSteamInput * 2)), Action.EXECUTE);
            }

            float newRotation = (float) flowRate;
            boolean needsPacket = false;

            if (Math.abs(newRotation - structure.clientRotation) > SynchronizedTurbineData.ROTATION_THRESHOLD) {
                structure.clientRotation = newRotation;
                needsPacket = true;
            }
            float scale = MekanismUtils.getScale(prevSteamScale, structure.gasTank);
            if (scale != prevSteamScale) {
                needsPacket = true;
                prevSteamScale = scale;
            }
            if (needsPacket) {
                sendUpdatePacket();
            }
        }
    }

    @Override
    public void nextMode(int tank) {
        if (tank == 0 && structure != null) {
            structure.dumpMode = structure.dumpMode.getNext();
        }
    }

    @Nonnull
    @Override
    protected SynchronizedTurbineData getNewStructure() {
        return new SynchronizedTurbineData(this);
    }

    @Override
    public MultiblockCache<SynchronizedTurbineData> getNewCache() {
        return new TurbineCache();
    }

    @Override
    protected UpdateProtocol<SynchronizedTurbineData> getProtocol() {
        return new TurbineUpdateProtocol(this);
    }

    @Override
    public MultiblockManager<SynchronizedTurbineData> getManager() {
        return MekanismGenerators.turbineManager;
    }

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        if (structure != null && isRendering) {
            updateTag.putFloat(NBTConstants.SCALE, prevSteamScale);
            updateTag.putInt(NBTConstants.VOLUME, structure.getVolume());
            updateTag.putInt(NBTConstants.LOWER_VOLUME, structure.lowerVolume);
            updateTag.put(NBTConstants.GAS_STORED, structure.gasTank.getStack().write(new CompoundNBT()));
            updateTag.put(NBTConstants.COMPLEX, structure.complex.write(new CompoundNBT()));
            updateTag.putFloat(NBTConstants.ROTATION, structure.clientRotation);
        }
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundNBT tag) {
        super.handleUpdateTag(tag);
        if (clientHasStructure && isRendering && structure != null) {
            NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE, scale -> prevSteamScale = scale);
            NBTUtils.setIntIfPresent(tag, NBTConstants.VOLUME, value -> structure.setVolume(value));
            NBTUtils.setIntIfPresent(tag, NBTConstants.LOWER_VOLUME, value -> structure.lowerVolume = value);
            NBTUtils.setGasStackIfPresent(tag, NBTConstants.GAS_STORED, value -> structure.gasTank.setStack(value));
            NBTUtils.setCoord4DIfPresent(tag, NBTConstants.COMPLEX, value -> structure.complex = value);
            NBTUtils.setFloatIfPresent(tag, NBTConstants.ROTATION, value -> structure.clientRotation = value);
            SynchronizedTurbineData.clientRotationMap.put(structure.inventoryID, structure.clientRotation);
        }
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableInt.create(() -> structure == null ? 0 : structure.getVolume(), value -> {
            if (structure != null) {
                structure.setVolume(value);
            }
        }));
        container.track(SyncableInt.create(() -> structure == null ? 0 : structure.lowerVolume, value -> {
            if (structure != null) {
                structure.lowerVolume = value;
            }
        }));
        container.track(SyncableGasStack.create(() -> structure == null ? GasStack.EMPTY : structure.gasTank.getStack(), value -> {
            if (structure != null) {
                structure.gasTank.setStack(value);
            }
        }));
        container.track(SyncableInt.create(() -> structure == null ? 0 : structure.vents, value -> {
            if (structure != null) {
                structure.vents = value;
            }
        }));
        container.track(SyncableInt.create(() -> structure == null ? 0 : structure.blades, value -> {
            if (structure != null) {
                structure.blades = value;
            }
        }));
        container.track(SyncableInt.create(() -> structure == null ? 0 : structure.coils, value -> {
            if (structure != null) {
                structure.coils = value;
            }
        }));
        container.track(SyncableInt.create(() -> structure == null ? 0 : structure.condensers, value -> {
            if (structure != null) {
                structure.condensers = value;
            }
        }));
        container.track(SyncableInt.create(() -> structure == null ? 0 : structure.getDispersers(), value -> {
            if (structure != null) {
                structure.clientDispersers = value;
            }
        }));
        container.track(SyncableFloatingLong.create(() -> structure == null ? FloatingLong.ZERO : structure.energyContainer.getEnergy(), value -> {
            if (structure != null) {
                structure.energyContainer.setEnergy(value);
            }
        }));
        container.track(SyncableLong.create(() -> structure == null ? 0 : structure.clientFlow, value -> {
            if (structure != null) {
                structure.clientFlow = value;
            }
        }));
        container.track(SyncableLong.create(() -> structure == null ? 0 : structure.lastSteamInput, value -> {
            if (structure != null) {
                structure.lastSteamInput = value;
            }
        }));
        container.track(SyncableEnum.create(GasMode::byIndexStatic, GasMode.IDLE, () -> structure == null ? GasMode.IDLE : structure.dumpMode, value -> {
            if (structure != null) {
                structure.dumpMode = value;
            }
        }));
    }
}