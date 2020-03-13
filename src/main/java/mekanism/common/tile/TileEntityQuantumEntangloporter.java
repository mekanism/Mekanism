package mekanism.common.tile;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.IHeatTransfer;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.sustained.ISustainedData;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ITankManager;
import mekanism.common.base.ITileNetwork;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.chunkloading.IChunkLoader;
import mekanism.common.config.MekanismConfig;
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
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentChunkLoader;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.EnergySlotInfo;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.tile.component.config.slot.ProxiedSlotInfo;
import mekanism.common.util.CableUtils;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;

public class TileEntityQuantumEntangloporter extends TileEntityMekanism implements ISideConfiguration, ITankManager, IFrequencyHandler, IHeatTransfer, ISustainedData,
      IChunkLoader, ITileNetwork {

    public InventoryFrequency frequency;
    public double heatToAbsorb = 0;
    //TODO: These seem to be used, do we want to have some sort of stats thing for the quantum entangloporter
    private double lastTransferLoss;
    private double lastEnvironmentLoss;
    public List<Frequency> publicCache = new ArrayList<>();
    public List<Frequency> privateCache = new ArrayList<>();
    public TileComponentEjector ejectorComponent;
    public TileComponentConfig configComponent;
    public TileComponentChunkLoader<TileEntityQuantumEntangloporter> chunkLoaderComponent;

    public TileEntityQuantumEntangloporter() {
        super(MekanismBlocks.QUANTUM_ENTANGLOPORTER);
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.FLUID, TransmissionType.GAS, TransmissionType.ENERGY, TransmissionType.HEAT);

        ConfigInfo itemConfig = configComponent.getConfig(TransmissionType.ITEM);
        if (itemConfig != null) {
            Supplier<List<IInventorySlot>> slotSupplier = () -> hasFrequency() ? frequency.inventorySlots : Collections.emptyList();
            itemConfig.addSlotInfo(DataType.INPUT, new ProxiedSlotInfo.Inventory(true, false, slotSupplier));
            itemConfig.addSlotInfo(DataType.OUTPUT, new ProxiedSlotInfo.Inventory(false, true, slotSupplier));
            //Set default config directions
            itemConfig.fill(DataType.INPUT);
            itemConfig.setDataType(RelativeSide.FRONT, DataType.OUTPUT);
        }

        ConfigInfo fluidConfig = configComponent.getConfig(TransmissionType.FLUID);
        if (fluidConfig != null) {
            Supplier<List<IExtendedFluidTank>> tankSupplier = () -> hasFrequency() ? frequency.fluidTanks : Collections.emptyList();
            fluidConfig.addSlotInfo(DataType.INPUT, new ProxiedSlotInfo.Fluid(true, false, tankSupplier));
            fluidConfig.addSlotInfo(DataType.OUTPUT, new ProxiedSlotInfo.Fluid(false, true, tankSupplier));
            //Set default config directions
            fluidConfig.fill(DataType.INPUT);
            fluidConfig.setDataType(RelativeSide.FRONT, DataType.OUTPUT);
        }

        ConfigInfo gasConfig = configComponent.getConfig(TransmissionType.GAS);
        if (gasConfig != null) {
            Supplier<List<? extends IChemicalTank<Gas, GasStack>>> tankSupplier = () -> hasFrequency() ? frequency.gasTanks : Collections.emptyList();
            gasConfig.addSlotInfo(DataType.INPUT, new ProxiedSlotInfo.Gas(true, false, tankSupplier));
            gasConfig.addSlotInfo(DataType.OUTPUT, new ProxiedSlotInfo.Gas(false, true, tankSupplier));
            //Set default config directions
            gasConfig.fill(DataType.INPUT);
            gasConfig.setDataType(RelativeSide.FRONT, DataType.OUTPUT);
        }

        ConfigInfo energyConfig = configComponent.getConfig(TransmissionType.ENERGY);
        if (energyConfig != null) {
            energyConfig.addSlotInfo(DataType.INPUT, new ProxiedSlotInfo.Energy(true, false));
            energyConfig.addSlotInfo(DataType.OUTPUT, new ProxiedSlotInfo.Energy(false, true));
            //Set default config directions
            energyConfig.fill(DataType.INPUT);
            energyConfig.setDataType(RelativeSide.FRONT, DataType.OUTPUT);
        }

        ConfigInfo heatConfig = configComponent.getConfig(TransmissionType.HEAT);
        if (heatConfig != null) {
            //TODO: Figure out why the quantum entangloporter doesn't seem to allow extracting heat, and can only accept it?
            heatConfig.addSlotInfo(DataType.INPUT, new ProxiedSlotInfo.Heat(true, false));
            //Set default config directions
            heatConfig.fill(DataType.INPUT);
            heatConfig.setCanEject(false);
        }

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(TransmissionType.ITEM, itemConfig);
        ejectorComponent.setOutputData(TransmissionType.FLUID, fluidConfig);
        ejectorComponent.setOutputData(TransmissionType.GAS, gasConfig);

        chunkLoaderComponent = new TileComponentChunkLoader<>(this);
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (configComponent.isEjecting(TransmissionType.ENERGY)) {
            CableUtils.emit(this);
        }
        double[] loss = simulateHeat();
        applyTemperatureChange();

        lastTransferLoss = loss[0];
        lastEnvironmentLoss = loss[1];

        FrequencyManager manager = getManager(frequency);
        Frequency lastFreq = frequency;

        if (manager != null) {
            if (frequency != null && !frequency.valid) {
                frequency = (InventoryFrequency) manager.validateFrequency(getSecurity().getOwnerUUID(), Coord4D.get(this), frequency);
                MekanismUtils.notifyLoadedNeighborsOfTileChange(getWorld(), Coord4D.get(this));
                markDirty();
            }

            if (frequency != null) {
                frequency = (InventoryFrequency) manager.update(Coord4D.get(this), frequency);
                if (frequency == null) {
                    MekanismUtils.notifyLoadedNeighborsOfTileChange(getWorld(), Coord4D.get(this));
                    markDirty();
                }
            }
        } else {
            frequency = null;
            if (lastFreq != null) {
                MekanismUtils.notifyLoadedNeighborsOfTileChange(getWorld(), Coord4D.get(this));
                markDirty();
            }
        }
    }

    private boolean hasFrequency() {
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

    public void setFrequency(String name, boolean publicFreq) {
        FrequencyManager manager = getManager(new InventoryFrequency(name, null).setPublic(publicFreq));
        manager.deactivate(Coord4D.get(this));
        for (Frequency freq : manager.getFrequencies()) {
            if (freq.name.equals(name)) {
                frequency = (InventoryFrequency) freq;
                frequency.activeCoords.add(Coord4D.get(this));
                MekanismUtils.notifyLoadedNeighborsOfTileChange(getWorld(), Coord4D.get(this));
                markDirty();
                return;
            }
        }

        Frequency freq = new InventoryFrequency(name, getSecurity().getOwnerUUID()).setPublic(publicFreq);
        freq.activeCoords.add(Coord4D.get(this));
        manager.addFrequency(freq);
        frequency = (InventoryFrequency) freq;
        MekanismUtils.notifyLoadedNeighborsOfTileChange(getWorld(), Coord4D.get(this));
        markDirty();
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
    public void handlePacketData(PacketBuffer dataStream) {
        if (!isRemote()) {
            int type = dataStream.readInt();
            if (type == 0) {
                String name = PacketHandler.readString(dataStream);
                boolean isPublic = dataStream.readBoolean();
                setFrequency(name, isPublic);
            } else if (type == 1) {
                String freq = PacketHandler.readString(dataStream);
                boolean isPublic = dataStream.readBoolean();
                FrequencyManager manager = getManager(new InventoryFrequency(freq, null).setPublic(isPublic));
                if (manager != null) {
                    manager.remove(freq, getSecurity().getOwnerUUID());
                }
            }
        }
    }

    @Override
    public boolean canOutputEnergy(Direction side) {
        if (!hasFrequency()) {
            return false;
        }
        ISlotInfo slotInfo = configComponent.getSlotInfo(TransmissionType.ENERGY, side);
        return slotInfo instanceof EnergySlotInfo && slotInfo.canOutput();
    }

    @Override
    public boolean canReceiveEnergy(Direction side) {
        if (!hasFrequency()) {
            return false;
        }
        ISlotInfo slotInfo = configComponent.getSlotInfo(TransmissionType.ENERGY, side);
        return slotInfo instanceof EnergySlotInfo && slotInfo.canInput();
    }

    @Override
    public double getMaxOutput() {
        return hasFrequency() ? MekanismConfig.general.quantumEntangloporterEnergyTransfer.get() : 0;
    }

    @Override
    public double getEnergy() {
        return hasFrequency() ? frequency.storedEnergy : 0;
    }

    @Override
    public void setEnergy(double energy) {
        if (hasFrequency()) {
            frequency.storedEnergy = Math.min(MekanismConfig.general.quantumEntangloporterEnergyTransfer.get(), energy);
        }
    }

    @Override
    public double getMaxEnergy() {
        return hasFrequency() ? MekanismConfig.general.quantumEntangloporterEnergyTransfer.get() : 0;
    }

    @Override
    public boolean persistInventory() {
        return false;
    }

    @Override
    public boolean canHandleGas() {
        return true;
    }

    @Override
    public boolean persistGas() {
        return false;
    }

    @Override
    public boolean canHandleFluid() {
        return true;
    }

    @Override
    public boolean persistFluid() {
        return false;
    }

    @Override
    public double getTemp() {
        return hasFrequency() ? frequency.temperature : 0;
    }

    @Override
    public double getInverseConductionCoefficient() {
        return 1;
    }

    @Override
    public double getInsulationCoefficient(Direction side) {
        return 1000;
    }

    @Override
    public void transferHeatTo(double heat) {
        heatToAbsorb += heat;
    }

    @Override
    public double[] simulateHeat() {
        return HeatUtils.simulate(this);
    }

    @Override
    public double applyTemperatureChange() {
        if (hasFrequency()) {
            frequency.temperature += heatToAbsorb;
        }
        heatToAbsorb = 0;
        return hasFrequency() ? frequency.temperature : 0;
    }

    @Nullable
    @Override
    public IHeatTransfer getAdjacent(Direction side) {
        if (hasFrequency()) {
            ISlotInfo slotInfo = configComponent.getSlotInfo(TransmissionType.HEAT, side);
            if (slotInfo != null && slotInfo.canInput()) {
                TileEntity adj = MekanismUtils.getTileEntity(getWorld(), getPos().offset(side));
                Optional<IHeatTransfer> capability = MekanismUtils.toOptional(CapabilityUtils.getCapability(adj, Capabilities.HEAT_TRANSFER_CAPABILITY, side.getOpposite()));
                if (capability.isPresent()) {
                    return capability.get();
                }
            }
        }
        return null;
    }

    @Override
    public Object[] getManagedTanks() {
        //TODO: Given these don't show in the GUI it may make more sense to not have this be implemented
        // at all as it is only really used for the dropper
        return hasFrequency() ? new Object[]{frequency.storedFluid, frequency.storedGas} : null;
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

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapabilityIfEnabled(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.HEAT_TRANSFER_CAPABILITY) {
            return Capabilities.HEAT_TRANSFER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapabilityIfEnabled(capability, side);
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        if (configComponent.isCapabilityDisabled(capability, side)) {
            return true;
        } else if (capability == Capabilities.HEAT_TRANSFER_CAPABILITY && side != null && !hasFrequency()) {
            return true;
        }
        return super.isCapabilityDisabled(capability, side);
    }

    @Override
    public TileComponentChunkLoader<TileEntityQuantumEntangloporter> getChunkLoader() {
        return chunkLoaderComponent;
    }

    @Override
    public Set<ChunkPos> getChunkSet() {
        return Collections.singleton(new ChunkPos(getPos()));
    }

    @Nonnull
    @Override
    public List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
        return hasFrequency() && hasInventory() ? frequency.getInventorySlots(side) : Collections.emptyList();
    }

    @Nonnull
    @Override
    public List<? extends IChemicalTank<Gas, GasStack>> getGasTanks(@Nullable Direction side) {
        return hasFrequency() && canHandleGas() ? frequency.getGasTanks(side) : Collections.emptyList();
    }

    @Nonnull
    @Override
    public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
        return hasFrequency() && canHandleFluid() ? frequency.getFluidTanks(side) : Collections.emptyList();
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