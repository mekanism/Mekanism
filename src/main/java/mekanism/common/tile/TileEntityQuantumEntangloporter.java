package mekanism.common.tile;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.heat.HeatAPI.HeatTransfer;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.security.SecurityMode;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.IMultiTypeCapability;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.chemical.QuantumEntangloporterChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.energy.QuantumEntangloporterEnergyContainerHolder;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.fluid.QuantumEntangloporterFluidTankHolder;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.capabilities.holder.heat.QuantumEntangloporterHeatCapacitorHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.QuantumEntangloporterInventorySlotHolder;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerChemicalTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerFluidTankWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.integration.energy.BlockEnergyCapabilityCache;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.lib.chunkloading.IChunkLoader;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.TileComponentChunkLoader;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.IProxiedSlotInfo.ChemicalProxy;
import mekanism.common.tile.component.config.slot.IProxiedSlotInfo.EnergyProxy;
import mekanism.common.tile.component.config.slot.IProxiedSlotInfo.FluidProxy;
import mekanism.common.tile.component.config.slot.IProxiedSlotInfo.HeatProxy;
import mekanism.common.tile.component.config.slot.IProxiedSlotInfo.InventoryProxy;
import mekanism.common.tile.component.config.slot.IProxiedSlotInfo.ProxySlotInfoCreator;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityQuantumEntangloporter extends TileEntityConfigurableMachine implements IChunkLoader {

    private final Map<TransmissionType, Map<Direction, BlockCapabilityCache<?, @Nullable Direction>>> capabilityCaches = new EnumMap<>(TransmissionType.class);
    private final Map<Direction, BlockEnergyCapabilityCache> adjacentEnergyCaps = new EnumMap<>(Direction.class);
    private final TileComponentChunkLoader<TileEntityQuantumEntangloporter> chunkLoaderComponent;

    private double lastTransferLoss;
    private double lastEnvironmentLoss;

    public TileEntityQuantumEntangloporter(BlockPos pos, BlockState state) {
        super(MekanismBlocks.QUANTUM_ENTANGLOPORTER, pos, state);

        setupConfig(TransmissionType.ITEM, InventoryProxy::new, () -> hasFrequency() ? getFreq().getInventorySlots(null) : Collections.emptyList());
        setupConfig(TransmissionType.FLUID, FluidProxy::new, () -> hasFrequency() ? getFreq().getFluidTanks(null) : Collections.emptyList());
        setupConfig(TransmissionType.CHEMICAL, ChemicalProxy::new, () -> hasFrequency() ? getFreq().getChemicalTanks(null) : Collections.emptyList());
        setupConfig(TransmissionType.ENERGY, EnergyProxy::new, () -> hasFrequency() ? getFreq().getEnergyContainers(null) : Collections.emptyList());

        ConfigInfo heatConfig = configComponent.getConfig(TransmissionType.HEAT);
        if (heatConfig != null) {
            Supplier<List<IHeatCapacitor>> capacitorSupplier = () -> hasFrequency() ? getFreq().getHeatCapacitors(null) : Collections.emptyList();
            heatConfig.addSlotInfo(DataType.INPUT_OUTPUT, new HeatProxy(true, false, capacitorSupplier));
            heatConfig.setCanEject(false);
        }

        ejectorComponent = new TileComponentEjector(this);
        //Note: All eject types except for items is handled by the frequency
        //Only allow trying to eject if we have a frequency, because otherwise all our containers and sides will just be empty anyway
        // also require that we can function before auto ejecting
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM).setCanEject(type -> hasFrequency() && canFunction());

        chunkLoaderComponent = new TileComponentChunkLoader<>(this);
        frequencyComponent.track(FrequencyType.INVENTORY, true, true, true);
        cacheCoord();
    }

    private <T> void setupConfig(TransmissionType type, ProxySlotInfoCreator<T> proxyCreator, Supplier<List<T>> supplier) {
        ConfigInfo config = configComponent.getConfig(type);
        if (config != null) {
            config.addSlotInfo(DataType.INPUT, proxyCreator.create(true, false, supplier));
            config.addSlotInfo(DataType.OUTPUT, proxyCreator.create(false, true, supplier));
            config.addSlotInfo(DataType.INPUT_OUTPUT, proxyCreator.create(true, true, supplier));
        }
    }

    @NotNull
    @Override
    public IChemicalTankHolder getInitialChemicalTanks(IContentsListener listener) {
        return new QuantumEntangloporterChemicalTankHolder(this, TransmissionType.CHEMICAL, InventoryFrequency::getChemicalTanks);
    }

    @NotNull
    @Override
    protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener) {
        return new QuantumEntangloporterFluidTankHolder(this);
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        return new QuantumEntangloporterEnergyContainerHolder(this);
    }

    @NotNull
    @Override
    protected IHeatCapacitorHolder getInitialHeatCapacitors(IContentsListener listener, CachedAmbientTemperature ambientTemperature) {
        return new QuantumEntangloporterHeatCapacitorHolder(this);
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        return new QuantumEntangloporterInventorySlotHolder(this);
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        InventoryFrequency freq = getFreq();
        if (freq != null && freq.isValid() && !freq.isRemoved()) {
            freq.handleEject(level.getGameTime());
            updateHeatCapacitors(null); // manually trigger heat capacitor update
            HeatTransfer loss = simulate();
            lastTransferLoss = loss.adjacentTransfer();
            lastEnvironmentLoss = loss.environmentTransfer();
        } else {
            lastTransferLoss = 0;
            lastEnvironmentLoss = 0;
        }
        return sendUpdatePacket;
    }

    @ComputerMethod
    public boolean hasFrequency() {
        Frequency freq = getFreq();
        return freq != null && freq.isValid() && !freq.isRemoved();
    }

    @Override
    public boolean persists(ContainerType<?, ?, ?> type) {
        // don't persist ANY substance types
        return false;
    }

    @Override
    public boolean shouldDumpRadiation() {
        //Note: The QE doesn't support radioactive substances but override this method anyway
        return false;
    }

    @Nullable
    @Override
    public IHeatHandler getAdjacent(@NotNull Direction side) {
        if (hasFrequency()) {
            ISlotInfo slotInfo = configComponent.getSlotInfo(TransmissionType.HEAT, side);
            if (slotInfo != null && slotInfo.canInput()) {
                return getAdjacentUnchecked(side);
            }
        }
        return null;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <HANDLER> HANDLER getCachedCapability(@NotNull Direction side, TransmissionType transmissionType) {
        if (transmissionType == TransmissionType.HEAT) {
            return (HANDLER) getAdjacentUnchecked(side);
        } else if (transmissionType == TransmissionType.ENERGY) {
            BlockEnergyCapabilityCache cache = adjacentEnergyCaps.get(side);
            if (cache == null) {
                cache = BlockEnergyCapabilityCache.create((ServerLevel) level, worldPosition.relative(side), side.getOpposite());
                adjacentEnergyCaps.put(side, cache);
            }
            return (HANDLER) cache.getCapability();
        } else if (transmissionType == TransmissionType.ITEM) {
            //Not currently handled
            return null;
        }
        Map<Direction, BlockCapabilityCache<?, @Nullable Direction>> caches = capabilityCaches.computeIfAbsent(transmissionType, type -> new EnumMap<>(Direction.class));
        BlockCapabilityCache<?, @Nullable Direction> cache = caches.get(side);
        if (cache == null) {
            IMultiTypeCapability<HANDLER, ?> capability = (IMultiTypeCapability<HANDLER, ?>) switch (transmissionType) {
                case FLUID -> Capabilities.FLUID;
                case CHEMICAL -> Capabilities.CHEMICAL;
                default -> null;
            };
            if (capability != null) {
                cache = capability.createCache((ServerLevel) level, worldPosition.relative(side), side.getOpposite());
                caches.put(side, cache);
            }
        }
        return cache == null ? null : (HANDLER) cache.getCapability();
    }

    @Override
    public TileComponentChunkLoader<TileEntityQuantumEntangloporter> getChunkLoader() {
        return chunkLoaderComponent;
    }

    @Override
    public Set<ChunkPos> getChunkSet() {
        return Collections.singleton(new ChunkPos(getBlockPos()));
    }

    public InventoryFrequency getFreq() {
        return getFrequency(FrequencyType.INVENTORY);
    }

    @ComputerMethod(nameOverride = "getTransferLoss", methodDescription = "May not be accurate if there is no frequency")
    public double getLastTransferLoss() {
        return lastTransferLoss;
    }

    @ComputerMethod(nameOverride = "getEnvironmentalLoss", methodDescription = "May not be accurate if there is no frequency")
    public double getLastEnvironmentLoss() {
        return lastEnvironmentLoss;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableDouble.create(this::getLastTransferLoss, value -> lastTransferLoss = value));
        container.track(SyncableDouble.create(this::getLastEnvironmentLoss, value -> lastEnvironmentLoss = value));
        //Note: We have to manually sync the energy container as we don't sync it in super and don't even always have one
        trackLastEnergy(container);
        container.track(SyncableLong.create(() -> {
            List<IEnergyContainer> energyContainers = getEnergyContainers(null);
            return energyContainers.isEmpty() ? 0L : energyContainers.getFirst().getEnergy();
        }, energy -> {
            List<IEnergyContainer> energyContainers = getEnergyContainers(null);
            if (!energyContainers.isEmpty()) {
                energyContainers.getFirst().setEnergy(energy);
            }
        }));
    }

    //Methods relating to IComputerTile
    @ComputerMethod(methodDescription = "Lists public frequencies")
    Collection<InventoryFrequency> getFrequencies() {
        return FrequencyType.INVENTORY.getManagerWrapper().getPublicManager().getFrequencies();
    }

    @ComputerMethod(methodDescription = "Requires a frequency to be selected")
    InventoryFrequency getFrequency() throws ComputerException {
        InventoryFrequency frequency = getFreq();
        if (frequency == null || !frequency.isValid() || frequency.isRemoved()) {
            throw new ComputerException("No frequency is currently selected.");
        }
        return frequency;
    }

    @ComputerMethod(requiresPublicSecurity = true, methodDescription = "Requires a public frequency to exist")
    void setFrequency(String name) throws ComputerException {
        validateSecurityIsPublic();
        InventoryFrequency frequency = FrequencyType.INVENTORY.getManagerWrapper().getPublicManager().getFrequency(name);
        if (frequency == null) {
            throw new ComputerException("No public inventory frequency with name '%s' found.", name);
        }
        setFrequency(FrequencyType.INVENTORY, frequency.getIdentity(), getOwnerUUID());
    }

    @ComputerMethod(requiresPublicSecurity = true, methodDescription = "Requires frequency to not already exist and for it to be public so that it can make it as the player who owns the block. Also sets the frequency after creation")
    void createFrequency(String name) throws ComputerException {
        validateSecurityIsPublic();
        InventoryFrequency frequency = FrequencyType.INVENTORY.getManagerWrapper().getPublicManager().getFrequency(name);
        if (frequency != null) {
            throw new ComputerException("Unable to create public inventory frequency with name '%s' as one already exists.", name);
        }
        setFrequency(FrequencyType.INVENTORY, new FrequencyIdentity(name, SecurityMode.PUBLIC, getOwnerUUID()), getOwnerUUID());
    }

    //Note: A bunch of the below buffer getters are rather "hardcoded", but they should be fine unless we decide to add support for more buffers at some point
    // in which case we can just add some overloads while we deprecate these
    @ComputerMethod
    ItemStack getBufferItem() throws ComputerException {
        return getFrequency().getInventorySlots(null).getFirst().getStack();
    }

    @WrappingComputerMethod(wrapper = ComputerFluidTankWrapper.class, methodNames = {"getBufferFluid", "getBufferFluidCapacity", "getBufferFluidNeeded",
                                                                                     "getBufferFluidFilledPercentage"}, docPlaceholder = "fluid buffer")
    IExtendedFluidTank getBufferFluidTank() throws ComputerException {
        return getFrequency().getFluidTanks(null).getFirst();
    }

    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getBufferChemical", "getBufferChemicalCapacity", "getBufferChemicalNeeded",
                                                                                        "getBufferChemicalFilledPercentage"}, docPlaceholder = "chemical buffer")
    IChemicalTank getBufferChemicalTank() throws ComputerException {
        return getFrequency().getChemicalTanks(null).getFirst();
    }

    @ComputerMethod(methodDescription = "Requires a frequency to be selected")
    double getTemperature() throws ComputerException {
        return getFrequency().getTotalTemperature();
    }
    //End methods IComputerTile
}
