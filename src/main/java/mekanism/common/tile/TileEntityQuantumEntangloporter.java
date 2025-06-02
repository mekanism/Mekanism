package mekanism.common.tile;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.heat.HeatAPI.HeatTransfer;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.security.SecurityMode;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.MultiTypeCapability;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.holder.QEConfigHolder;
import mekanism.common.capabilities.holder.container.IContainerHolder;
import mekanism.common.capabilities.holder.container.MekContainerHelper;
import mekanism.common.capabilities.holder.container.QEContainerHolder;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.energy.QEEnergyHolder;
import mekanism.common.capabilities.proxy.ProxyHandler;
import mekanism.common.component.containers.type.IContainerType;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerChemicalTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerFluidTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.lib.chunkloading.IChunkLoader;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyTypes;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.TileComponentChunkLoader;
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
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import org.jspecify.annotations.Nullable;

public class TileEntityQuantumEntangloporter extends TileEntityConfigurableMachine implements IChunkLoader {

    private final Map<TransmissionType, Map<Direction, BlockCapabilityCache<?, @Nullable Direction>>> capabilityCaches = new EnumMap<>(TransmissionType.class);
    private final TileComponentChunkLoader<TileEntityQuantumEntangloporter> chunkLoaderComponent;

    private double lastTransferLoss;
    private double lastEnvironmentLoss;

    public TileEntityQuantumEntangloporter(BlockPos pos, BlockState state) {
        super(MekanismBlocks.QUANTUM_ENTANGLOPORTER, pos, state);

        setupConfig(TransmissionType.ITEM, InventoryProxy::new, () -> {
            InventoryFrequency freq = getFreq();
            return isFrequencyValid(freq) ? freq.getInventorySlots() : Collections.emptyList();
        });
        setupConfig(TransmissionType.FLUID, FluidProxy::new, () -> {
            InventoryFrequency freq = getFreq();
            return isFrequencyValid(freq) ? freq.getFluidTanks() : Collections.emptyList();
        });
        setupConfig(TransmissionType.CHEMICAL, ChemicalProxy::new, () -> {
            InventoryFrequency freq = getFreq();
            return isFrequencyValid(freq) ? freq.getChemicalTanks() : Collections.emptyList();
        });
        setupConfig(TransmissionType.ENERGY, EnergyProxy::new, () -> {
            InventoryFrequency freq = getFreq();
            return isFrequencyValid(freq) ? freq.getEnergyContainer() : null;
        });

        ConfigInfo heatConfig = configComponent.getConfig(TransmissionType.HEAT);
        if (heatConfig != null) {
            Supplier<List<IHeatCapacitor>> capacitorSupplier = () -> {
                InventoryFrequency freq = getFreq();
                return isFrequencyValid(freq) ? freq.getHeatCapacitors() : Collections.emptyList();
            };
            heatConfig.addSlotInfo(DataType.INPUT_OUTPUT, new HeatProxy(true, false, capacitorSupplier));
            heatConfig.setCanEject(false);
        }

        //Note: All eject types except for items is handled by the frequency
        //Only allow trying to eject if we have a frequency, because otherwise all our containers and sides will just be empty anyway
        // also require that we can function before auto ejecting
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM).setCanEject(type -> hasFrequency() && canFunction());

        chunkLoaderComponent = new TileComponentChunkLoader<>(this);
        frequencyComponent.track(FrequencyTypes.INVENTORY, true, true, true);
        cacheCoord();
    }

    private <T extends @Nullable Object> void setupConfig(TransmissionType type, ProxySlotInfoCreator<T> proxyCreator, Supplier<T> supplier) {
        ConfigInfo config = configComponent.getConfig(type);
        if (config != null) {
            config.addSlotInfo(DataType.INPUT, proxyCreator.create(true, false, supplier));
            config.addSlotInfo(DataType.OUTPUT, proxyCreator.create(false, true, supplier));
            config.addSlotInfo(DataType.INPUT_OUTPUT, proxyCreator.create(true, true, supplier));
        }
    }

    @Override
    public IContainerHolder<IChemicalTank> getInitialChemicalTanks(IContentsListener listener) {
        return new QEContainerHolder<>(this, TransmissionType.CHEMICAL, MekContainerHelper.CHEMICAL_SLOT_PARSER, InventoryFrequency::getChemicalTanks);
    }

    @Override
    protected IContainerHolder<IFluidTank> getInitialFluidTanks(IContentsListener listener) {
        return new QEContainerHolder<>(this, TransmissionType.FLUID, MekContainerHelper.FLUID_SLOT_PARSER, InventoryFrequency::getFluidTanks);
    }

    @Override
    protected IEnergyContainerHolder getInitialEnergyContainer(IContentsListener listener) {
        return new QEEnergyHolder(this);
    }

    @Override
    protected IContainerHolder<IHeatCapacitor> getInitialHeatCapacitors(IContentsListener listener, CachedAmbientTemperature ambientTemperature) {
        return new QEContainerHolder<>(this, TransmissionType.HEAT, MekContainerHelper.HEAT_SLOT_PARSER, InventoryFrequency::getHeatCapacitors);
    }

    @Override
    protected IContainerHolder<IInventorySlot> getInitialInventory(IContentsListener listener) {
        return new QEContainerHolder<>(this, TransmissionType.ITEM, MekContainerHelper.ITEM_SLOT_PARSER, InventoryFrequency::getInventorySlots);
    }

    @Override
    protected boolean onUpdateServer(ServerLevel level) {
        boolean sendUpdatePacket = super.onUpdateServer(level);
        InventoryFrequency freq = getFreq();
        if (freq != null && freq.isValid() && !freq.isRemoved()) {
            freq.handleEject(level.getGameTime());
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
        return isFrequencyValid(getFreq());
    }

    private boolean isFrequencyValid(@Nullable InventoryFrequency freq) {
        return freq != null && freq.isValid() && !freq.isRemoved();
    }

    @Override
    public boolean persists(IContainerType<?, ?> type) {
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
    public IHeatHandler getAdjacent(Direction side) {
        if (hasFrequency()) {
            ISlotInfo slotInfo = configComponent.getSlotInfo(TransmissionType.HEAT, side);
            if (slotInfo != null && slotInfo.canInput()) {
                return rejectIfSameFreq(getAdjacentUnchecked(side));
            }
        }
        return null;
    }

    @Nullable
    private <HANDLER> HANDLER rejectIfSameFreq(@Nullable HANDLER otherHandler) {
        if (otherHandler instanceof ProxyHandler<?> proxy) {
            if (proxy.getHolder() instanceof QEConfigHolder<?> entangloporterConfig) {
                if (Objects.equals(getFreq(), entangloporterConfig.getFrequency())) {
                    return null;
                }
            }
        }
        return otherHandler;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <HANDLER> HANDLER getCachedCapability(ServerLevel level, Direction side, TransmissionType transmissionType) {
        if (transmissionType == TransmissionType.HEAT) {
            return (HANDLER) getAdjacentUnchecked(side);
        } else if (transmissionType == TransmissionType.ITEM) {
            //Not currently handled
            return null;
        }
        Map<Direction, BlockCapabilityCache<?, @Nullable Direction>> caches = capabilityCaches.computeIfAbsent(transmissionType, _ -> new EnumMap<>(Direction.class));
        BlockCapabilityCache<?, @Nullable Direction> cache = caches.get(side);
        if (cache == null) {
            MultiTypeCapability<HANDLER> capability = (MultiTypeCapability<HANDLER>) switch (transmissionType) {
                case FLUID -> Capabilities.FLUID;
                case CHEMICAL -> Capabilities.CHEMICAL;
                case ENERGY -> Capabilities.ENERGY;
                default -> null;
            };
            if (capability != null) {
                cache = capability.createCache(level, worldPosition.relative(side), side.getOpposite());
                caches.put(side, cache);
            }
        }
        return cache == null ? null : rejectIfSameFreq((HANDLER) cache.getCapability());
    }

    @Override
    public TileComponentChunkLoader<TileEntityQuantumEntangloporter> getChunkLoader() {
        return chunkLoaderComponent;
    }

    @Override
    public Set<ChunkPos> getChunkSet() {
        return Collections.singleton(ChunkPos.containing(getBlockPos()));
    }

    @Nullable
    public InventoryFrequency getFreq() {
        return getFrequency(FrequencyTypes.INVENTORY);
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
            IEnergyContainer energyContainer = getEnergyContainer();
            return energyContainer == null ? 0L : energyContainer.getAmountAsLong();
        }, energy -> {
            IEnergyContainer energyContainer = getEnergyContainer();
            if (energyContainer != null) {
                energyContainer.setEnergy(energy, null);
            }
        }));
    }

    //Methods relating to IComputerTile
    @ComputerMethod(methodDescription = "Lists public frequencies")
    Collection<InventoryFrequency> getFrequencies() {
        return FrequencyTypes.INVENTORY.getController().getPublicLookup().getFrequencies();
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
        InventoryFrequency frequency = FrequencyTypes.INVENTORY.getController().getPublicLookup().getFrequency(name);
        if (frequency == null) {
            throw new ComputerException("No public inventory frequency with name '%s' found.", name);
        }
        setFrequency(FrequencyTypes.INVENTORY, frequency.getIdentity(), getOwnerUUID());
    }

    @ComputerMethod(requiresPublicSecurity = true, methodDescription = "Requires frequency to not already exist and for it to be public so that it can make it as the player who owns the block. Also sets the frequency after creation")
    void createFrequency(String name) throws ComputerException {
        validateSecurityIsPublic();
        InventoryFrequency frequency = FrequencyTypes.INVENTORY.getController().getPublicLookup().getFrequency(name);
        if (frequency != null) {
            throw new ComputerException("Unable to create public inventory frequency with name '%s' as one already exists.", name);
        }
        setFrequency(FrequencyTypes.INVENTORY, new FrequencyIdentity(name, SecurityMode.PUBLIC, getOwnerUUID()), getOwnerUUID());
    }

    //Note: A bunch of the below buffer getters are rather "hardcoded", but they should be fine unless we decide to add support for more buffers at some point
    // in which case we can just add some overloads while we deprecate these
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getBufferItem", docPlaceholder = "buffer slot")
    IInventorySlot getBufferItemSlot() throws ComputerException {
        return getFrequency().getInventorySlots().getFirst();
    }

    @WrappingComputerMethod(wrapper = ComputerFluidTankWrapper.class, methodNames = {"getBufferFluid", "getBufferFluidCapacity", "getBufferFluidNeeded",
                                                                                     "getBufferFluidFilledPercentage"}, docPlaceholder = "fluid buffer")
    IFluidTank getBufferFluidTank() throws ComputerException {
        return getFrequency().getFluidTanks().getFirst();
    }

    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getBufferChemical", "getBufferChemicalCapacity", "getBufferChemicalNeeded",
                                                                                        "getBufferChemicalFilledPercentage"}, docPlaceholder = "chemical buffer")
    IChemicalTank getBufferChemicalTank() throws ComputerException {
        return getFrequency().getChemicalTanks().getFirst();
    }

    @ComputerMethod(methodDescription = "Requires a frequency to be selected")
    double getTemperature() throws ComputerException {
        return getFrequency().getTotalTemperature();
    }
    //End methods IComputerTile
}
