package mekanism.common.tile;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.lib.chunkloading.IChunkLoader;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IFrequencyHandler;
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
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.util.CableUtils;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.ChunkPos;

public class TileEntityQuantumEntangloporter extends TileEntityMekanism implements ISideConfiguration, IFrequencyHandler, ISustainedData, IChunkLoader {

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
            Supplier<List<IInventorySlot>> slotSupplier = () -> hasFrequency() ? getFreq().getInventorySlots(null) : Collections.emptyList();
            itemConfig.addSlotInfo(DataType.INPUT, new ProxiedSlotInfo.Inventory(true, false, slotSupplier));
            itemConfig.addSlotInfo(DataType.OUTPUT, new ProxiedSlotInfo.Inventory(false, true, slotSupplier));
            itemConfig.addSlotInfo(DataType.INPUT_OUTPUT, new ProxiedSlotInfo.Inventory(true, true, slotSupplier));
            //Set default config directions
            itemConfig.fill(DataType.INPUT);
            itemConfig.setDataType(DataType.OUTPUT, RelativeSide.FRONT);
        }

        ConfigInfo fluidConfig = configComponent.getConfig(TransmissionType.FLUID);
        if (fluidConfig != null) {
            Supplier<List<IExtendedFluidTank>> tankSupplier = () -> hasFrequency() ? getFreq().getFluidTanks(null) : Collections.emptyList();
            fluidConfig.addSlotInfo(DataType.INPUT, new ProxiedSlotInfo.Fluid(true, false, tankSupplier));
            fluidConfig.addSlotInfo(DataType.OUTPUT, new ProxiedSlotInfo.Fluid(false, true, tankSupplier));
            fluidConfig.addSlotInfo(DataType.INPUT_OUTPUT, new ProxiedSlotInfo.Fluid(true, true, tankSupplier));
            //Set default config directions
            fluidConfig.fill(DataType.INPUT);
            fluidConfig.setDataType(DataType.OUTPUT, RelativeSide.FRONT);
        }

        ConfigInfo gasConfig = configComponent.getConfig(TransmissionType.GAS);
        if (gasConfig != null) {
            Supplier<List<IGasTank>> tankSupplier = () -> hasFrequency() ? getFreq().getGasTanks(null) : Collections.emptyList();
            gasConfig.addSlotInfo(DataType.INPUT, new ProxiedSlotInfo.Gas(true, false, tankSupplier));
            gasConfig.addSlotInfo(DataType.OUTPUT, new ProxiedSlotInfo.Gas(false, true, tankSupplier));
            gasConfig.addSlotInfo(DataType.INPUT_OUTPUT, new ProxiedSlotInfo.Gas(true, true, tankSupplier));
            //Set default config directions
            gasConfig.fill(DataType.INPUT);
            gasConfig.setDataType(DataType.OUTPUT, RelativeSide.FRONT);
        }

        ConfigInfo energyConfig = configComponent.getConfig(TransmissionType.ENERGY);
        if (energyConfig != null) {
            Supplier<List<IEnergyContainer>> containerSupplier = () -> hasFrequency() ? getFreq().getEnergyContainers(null) : Collections.emptyList();
            energyConfig.addSlotInfo(DataType.INPUT, new ProxiedSlotInfo.Energy(true, false, containerSupplier));
            energyConfig.addSlotInfo(DataType.OUTPUT, new ProxiedSlotInfo.Energy(false, true, containerSupplier));
            energyConfig.addSlotInfo(DataType.INPUT_OUTPUT, new ProxiedSlotInfo.Energy(true, true, containerSupplier));
            //Set default config directions
            energyConfig.fill(DataType.INPUT);
            energyConfig.setDataType(DataType.OUTPUT, RelativeSide.FRONT);
        }

        ConfigInfo heatConfig = configComponent.getConfig(TransmissionType.HEAT);
        if (heatConfig != null) {
            Supplier<List<IHeatCapacitor>> capacitorSupplier = () -> hasFrequency() ? getFreq().getHeatCapacitors(null) : Collections.emptyList();
            heatConfig.addSlotInfo(DataType.INPUT_OUTPUT, new ProxiedSlotInfo.Heat(true, false, capacitorSupplier));
            //Set default config directions
            heatConfig.fill(DataType.INPUT_OUTPUT);
            heatConfig.setCanEject(false);
            //TODO - V10: look into allowing heat output config, modify getAdjacent as needed rather than just checking canInput
        }

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM, TransmissionType.FLUID, TransmissionType.GAS);

        chunkLoaderComponent = new TileComponentChunkLoader<>(this);
        frequencyComponent.track(FrequencyType.INVENTORY, true, true, true);
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
                CableUtils.emit(info.getAllOutputtingSides(), getFreq().storedEnergy, this);
            }
        }
        updateHeatCapacitors(null); // manually trigger heat capacitor update
        HeatTransfer loss = simulate();
        lastTransferLoss = loss.getAdjacentTransfer();
        lastEnvironmentLoss = loss.getEnvironmentTransfer();
    }

    public boolean hasFrequency() {
        Frequency freq = getFreq();
        return freq != null && freq.isValid();
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
        InventoryFrequency freq = frequencyComponent.getFrequency(FrequencyType.INVENTORY);
        if (freq != null) {
            ItemDataUtils.setCompound(itemStack, NBTConstants.FREQUENCY, freq.serializeIdentity());
        }
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        FrequencyIdentity freq = FrequencyIdentity.load(FrequencyType.INVENTORY, ItemDataUtils.getCompound(itemStack, NBTConstants.FREQUENCY));
        if (freq != null) {
            setFrequency(FrequencyType.INVENTORY, freq);
        }
    }

    @Override
    public Map<String, String> getTileDataRemap() {
        Map<String, String> remap = new Object2ObjectOpenHashMap<>();
        remap.put(NBTConstants.FREQUENCY + "." + NBTConstants.NAME, NBTConstants.FREQUENCY + "." + NBTConstants.NAME);
        remap.put(NBTConstants.FREQUENCY + "." + NBTConstants.PUBLIC_FREQUENCY, NBTConstants.FREQUENCY + "." + NBTConstants.PUBLIC_FREQUENCY);
        return remap;
    }

    public InventoryFrequency getFreq() {
        return getFrequency(FrequencyType.INVENTORY);
    }

    public double getLastTransferLoss() {
        return lastTransferLoss;
    }

    public double getLastEnvironmentLoss() {
        return lastEnvironmentLoss;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableDouble.create(this::getLastTransferLoss, value -> lastTransferLoss = value));
        container.track(SyncableDouble.create(this::getLastEnvironmentLoss, value -> lastEnvironmentLoss = value));
    }
}