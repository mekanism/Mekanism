package mekanism.common.tile;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.heat.HeatAPI.HeatTransfer;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.sustained.ISustainedData;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.Mekanism;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.chemical.QuantumEntangloporterGasTankHolder;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.energy.QuantumEntangloporterEnergyContainerHolder;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.fluid.QuantumEntangloporterFluidTankHolder;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.capabilities.holder.heat.QuantumEntangloporterHeatCapacitorHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.QuantumEntangloporterInventorySlotHolder;
import mekanism.common.chunkloading.IChunkLoader;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.frequency.FrequencyType;
import mekanism.common.frequency.IFrequencyHandler;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableFrequency;
import mekanism.common.inventory.container.sync.list.SyncableFrequencyList;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentChunkLoader;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.tile.component.config.slot.ProxiedSlotInfo;
import mekanism.common.tile.interfaces.IHasFrequency;
import mekanism.common.util.CableUtils;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.common.util.Constants.NBT;

public class TileEntityQuantumEntangloporter extends TileEntityMekanism implements ISideConfiguration, IFrequencyHandler, ISustainedData, IChunkLoader, IHasFrequency {

    public InventoryFrequency frequency;
    public List<Frequency> publicCache = new ArrayList<>();
    public List<Frequency> privateCache = new ArrayList<>();
    public TileComponentEjector ejectorComponent;
    public TileComponentConfig configComponent;
    public TileComponentChunkLoader<TileEntityQuantumEntangloporter> chunkLoaderComponent;

    private double lastTransferLoss;
    private double lastEnvironmentLoss;

    public TileEntityQuantumEntangloporter() {
        super(MekanismBlocks.QUANTUM_ENTANGLOPORTER);
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.FLUID, TransmissionType.GAS, TransmissionType.ENERGY, TransmissionType.HEAT);

        ConfigInfo itemConfig = configComponent.getConfig(TransmissionType.ITEM);
        if (itemConfig != null) {
            Supplier<List<IInventorySlot>> slotSupplier = () -> hasFrequency() ? frequency.getInventorySlots(null) : Collections.emptyList();
            itemConfig.addSlotInfo(DataType.INPUT, new ProxiedSlotInfo.Inventory(true, false, slotSupplier));
            itemConfig.addSlotInfo(DataType.OUTPUT, new ProxiedSlotInfo.Inventory(false, true, slotSupplier));
            //Set default config directions
            itemConfig.fill(DataType.INPUT);
            itemConfig.setDataType(RelativeSide.FRONT, DataType.OUTPUT);
        }

        ConfigInfo fluidConfig = configComponent.getConfig(TransmissionType.FLUID);
        if (fluidConfig != null) {
            Supplier<List<IExtendedFluidTank>> tankSupplier = () -> hasFrequency() ? frequency.getFluidTanks(null) : Collections.emptyList();
            fluidConfig.addSlotInfo(DataType.INPUT, new ProxiedSlotInfo.Fluid(true, false, tankSupplier));
            fluidConfig.addSlotInfo(DataType.OUTPUT, new ProxiedSlotInfo.Fluid(false, true, tankSupplier));
            //Set default config directions
            fluidConfig.fill(DataType.INPUT);
            fluidConfig.setDataType(RelativeSide.FRONT, DataType.OUTPUT);
        }

        ConfigInfo gasConfig = configComponent.getConfig(TransmissionType.GAS);
        if (gasConfig != null) {
            Supplier<List<IGasTank>> tankSupplier = () -> hasFrequency() ? frequency.getGasTanks(null) : Collections.emptyList();
            gasConfig.addSlotInfo(DataType.INPUT, new ProxiedSlotInfo.Gas(true, false, tankSupplier));
            gasConfig.addSlotInfo(DataType.OUTPUT, new ProxiedSlotInfo.Gas(false, true, tankSupplier));
            //Set default config directions
            gasConfig.fill(DataType.INPUT);
            gasConfig.setDataType(RelativeSide.FRONT, DataType.OUTPUT);
        }

        ConfigInfo energyConfig = configComponent.getConfig(TransmissionType.ENERGY);
        if (energyConfig != null) {
            Supplier<List<IEnergyContainer>> containerSupplier = () -> hasFrequency() ? frequency.getEnergyContainers(null) : Collections.emptyList();
            energyConfig.addSlotInfo(DataType.INPUT, new ProxiedSlotInfo.Energy(true, false, containerSupplier));
            energyConfig.addSlotInfo(DataType.OUTPUT, new ProxiedSlotInfo.Energy(false, true, containerSupplier));
            //Set default config directions
            energyConfig.fill(DataType.INPUT);
            energyConfig.setDataType(RelativeSide.FRONT, DataType.OUTPUT);
        }

        ConfigInfo heatConfig = configComponent.getConfig(TransmissionType.HEAT);
        if (heatConfig != null) {
            Supplier<List<IHeatCapacitor>> capacitorSupplier = () -> hasFrequency() ? frequency.getHeatCapacitors(null) : Collections.emptyList();
            heatConfig.addSlotInfo(DataType.INPUT, new ProxiedSlotInfo.Heat(true, false, capacitorSupplier));
            //Set default config directions
            heatConfig.fill(DataType.INPUT);
            heatConfig.setCanEject(false);
            //TODO - V10: look into allowing heat output config, modify getAdjacent as needed rather than just checking canInput
        }

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(TransmissionType.ITEM, itemConfig);
        ejectorComponent.setOutputData(TransmissionType.FLUID, fluidConfig);
        ejectorComponent.setOutputData(TransmissionType.GAS, gasConfig);

        chunkLoaderComponent = new TileComponentChunkLoader<>(this);
    }

    @Nonnull
    @Override
    protected IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks() {
        return new QuantumEntangloporterGasTankHolder(this);
    }

    @Nonnull
    @Override
    protected IFluidTankHolder getInitialFluidTanks() {
        return new QuantumEntangloporterFluidTankHolder(this);
    }

    @Nonnull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers() {
        return new QuantumEntangloporterEnergyContainerHolder(this);
    }

    @Nonnull
    @Override
    protected IHeatCapacitorHolder getInitialHeatCapacitors() {
        return new QuantumEntangloporterHeatCapacitorHolder(this);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        return new QuantumEntangloporterInventorySlotHolder(this);
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (hasFrequency()) {
            ConfigInfo info = configComponent.getConfig(TransmissionType.ENERGY);
            if (info != null && info.isEjecting()) {
                CableUtils.emit(info.getSidesForData(DataType.OUTPUT), frequency.storedEnergy, this);
            }
        }

        HeatTransfer loss = simulate();
        lastTransferLoss = loss.getAdjacentTransfer();
        lastEnvironmentLoss = loss.getEnvironmentTransfer();

        FrequencyManager manager = getManager(frequency);
        Frequency lastFreq = frequency;

        if (manager != null) {
            if (frequency != null && !frequency.valid) {
                frequency = (InventoryFrequency) manager.validateFrequency(getSecurity().getOwnerUUID(), Coord4D.get(this), frequency);
                MekanismUtils.notifyLoadedNeighborsOfTileChange(getWorld(), getPos());
                markDirty(false);
            }
            if (frequency != null) {
                frequency = (InventoryFrequency) manager.update(Coord4D.get(this), frequency);
                if (frequency == null) {
                    MekanismUtils.notifyLoadedNeighborsOfTileChange(getWorld(), getPos());
                    markDirty(false);
                }
            }
        } else {
            frequency = null;
            if (lastFreq != null) {
                MekanismUtils.notifyLoadedNeighborsOfTileChange(getWorld(), getPos());
                markDirty(false);
            }
        }
    }

    public boolean hasFrequency() {
        return frequency != null && frequency.valid;
    }

    @Override
    public void remove() {
        super.remove();
        if (!isRemote()) {
            if (frequency != null) {
                FrequencyManager manager = getManager(frequency);
                if (manager != null) {
                    manager.deactivate(Coord4D.get(this));
                }
            }
        }
    }

    @Override
    public Frequency getFrequency(FrequencyManager manager) {
        if (manager == Mekanism.securityFrequencies) {
            return getSecurity().getFrequency();
        }
        return frequency;
    }

    public FrequencyManager getManager(Frequency freq) {
        if (getSecurity().getOwnerUUID() == null || freq == null) {
            return null;
        }
        if (freq.isPublic()) {
            return Mekanism.publicEntangloporters;
        } else if (!Mekanism.privateEntangloporters.containsKey(getSecurity().getOwnerUUID())) {
            FrequencyManager manager = new FrequencyManager(FrequencyType.INVENTORY, InventoryFrequency.ENTANGLOPORTER, getSecurity().getOwnerUUID());
            Mekanism.privateEntangloporters.put(getSecurity().getOwnerUUID(), manager);
            if (!isRemote()) {
                manager.createOrLoad();
            }
        }
        return Mekanism.privateEntangloporters.get(getSecurity().getOwnerUUID());
    }

    @Override
    public void setFrequency(String name, boolean publicFreq) {
        FrequencyManager manager = getManager(new InventoryFrequency(name, null).setPublic(publicFreq));
        manager.deactivate(Coord4D.get(this));
        for (Frequency freq : manager.getFrequencies()) {
            if (freq.name.equals(name)) {
                frequency = (InventoryFrequency) freq;
                frequency.activeCoords.add(Coord4D.get(this));
                MekanismUtils.notifyLoadedNeighborsOfTileChange(getWorld(), getPos());
                markDirty(false);
                return;
            }
        }

        Frequency freq = new InventoryFrequency(name, getSecurity().getOwnerUUID()).setPublic(publicFreq);
        freq.activeCoords.add(Coord4D.get(this));
        manager.addFrequency(freq);
        frequency = (InventoryFrequency) freq;
        MekanismUtils.notifyLoadedNeighborsOfTileChange(getWorld(), getPos());
        markDirty(false);
    }

    @Override
    public void removeFrequency(String name, boolean publicFreq) {
        FrequencyManager manager = getManager(new InventoryFrequency(name, null).setPublic(publicFreq));
        if (manager != null) {
            manager.remove(name, getSecurity().getOwnerUUID());
        }
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        if (nbtTags.contains(NBTConstants.FREQUENCY, NBT.TAG_COMPOUND)) {
            frequency = new InventoryFrequency(nbtTags.getCompound(NBTConstants.FREQUENCY), false);
            frequency.valid = false;
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        if (frequency != null) {
            CompoundNBT frequencyTag = new CompoundNBT();
            frequency.write(frequencyTag);
            nbtTags.put(NBTConstants.FREQUENCY, frequencyTag);
        }
        return nbtTags;
    }

    @Override
    public boolean persistInventory() {
        return false;
    }

    @Override
    public boolean persists(SubstanceType type) {
        // don't persist ANY substance types
        return false;
    }

    @Nullable
    @Override
    public IHeatHandler getAdjacent(Direction side) {
        if (hasFrequency()) {
            ISlotInfo slotInfo = configComponent.getSlotInfo(TransmissionType.HEAT, side);
            if (slotInfo != null && slotInfo.canInput()) {
                TileEntity adj = MekanismUtils.getTileEntity(getWorld(), getPos().offset(side));
                return MekanismUtils.toOptional(CapabilityUtils.getCapability(adj, Capabilities.HEAT_HANDLER_CAPABILITY, side.getOpposite())).orElse(null);
            }
        }
        return null;
    }

    @Override
    public TileComponentConfig getConfig() {
        return configComponent;
    }

    @Override
    public Direction getOrientation() {
        return getDirection();
    }

    @Override
    public TileComponentEjector getEjector() {
        return ejectorComponent;
    }

    @Override
    public TileComponentChunkLoader<TileEntityQuantumEntangloporter> getChunkLoader() {
        return chunkLoaderComponent;
    }

    @Override
    public Set<ChunkPos> getChunkSet() {
        return Collections.singleton(new ChunkPos(getPos()));
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        if (frequency != null) {
            ItemDataUtils.setCompound(itemStack, NBTConstants.FREQUENCY, frequency.getIdentity().serialize());
        }
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        Frequency.Identity freq = Frequency.Identity.load(ItemDataUtils.getCompound(itemStack, NBTConstants.FREQUENCY));
        if (freq != null) {
            setFrequency(freq.name, freq.publicFreq);
        }
    }

    @Override
    public Map<String, String> getTileDataRemap() {
        Map<String, String> remap = new Object2ObjectOpenHashMap<>();
        remap.put(NBTConstants.FREQUENCY + "." + NBTConstants.NAME, NBTConstants.FREQUENCY + "." + NBTConstants.NAME);
        remap.put(NBTConstants.FREQUENCY + "." + NBTConstants.PUBLIC_FREQUENCY, NBTConstants.FREQUENCY + "." + NBTConstants.PUBLIC_FREQUENCY);
        return remap;
    }

    public double getLastTransferLoss() {
        return lastTransferLoss;
    }

    public double getLastEnvironmentLoss() {
        return lastEnvironmentLoss;
    }

    private List<Frequency> getPublicFrequencies() {
        return isRemote() ? publicCache : Mekanism.publicEntangloporters.getFrequencies();
    }

    private List<Frequency> getPrivateFrequencies() {
        if (isRemote()) {
            return privateCache;
        }
        //Note: This is a cleaned up version of getting the manager via getManager, given we only want
        // to get private frequencies here, and there is no reason to be creating a dummy frequency just to get
        // past the checks for public frequencies
        UUID ownerUUID = getSecurity().getOwnerUUID();
        if (ownerUUID == null) {
            return Collections.emptyList();
        }
        if (Mekanism.privateEntangloporters.containsKey(ownerUUID)) {
            return Mekanism.privateEntangloporters.get(ownerUUID).getFrequencies();
        }
        FrequencyManager manager = new FrequencyManager(FrequencyType.INVENTORY, InventoryFrequency.ENTANGLOPORTER, getSecurity().getOwnerUUID());
        Mekanism.privateEntangloporters.put(getSecurity().getOwnerUUID(), manager);
        if (!isRemote()) {
            manager.createOrLoad();
        }
        return manager.getFrequencies();
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableDouble.create(this::getLastTransferLoss, value -> lastTransferLoss = value));
        container.track(SyncableDouble.create(this::getLastEnvironmentLoss, value -> lastEnvironmentLoss = value));
        container.track(SyncableFrequency.create(() -> frequency, value -> frequency = value));
        container.track(SyncableFrequencyList.create(this::getPublicFrequencies, value -> publicCache = value));
        container.track(SyncableFrequencyList.create(this::getPrivateFrequencies, value -> privateCache = value));
    }
}