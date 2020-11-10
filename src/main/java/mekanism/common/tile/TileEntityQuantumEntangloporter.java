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
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.heat.HeatAPI.HeatTransfer;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IHeatHandler;
import mekanism.common.capabilities.Capabilities;
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
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.lib.chunkloading.IChunkLoader;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IFrequencyHandler;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentChunkLoader;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.IProxiedSlotInfo.EnergyProxy;
import mekanism.common.tile.component.config.slot.IProxiedSlotInfo.FluidProxy;
import mekanism.common.tile.component.config.slot.IProxiedSlotInfo.GasProxy;
import mekanism.common.tile.component.config.slot.IProxiedSlotInfo.HeatProxy;
import mekanism.common.tile.component.config.slot.IProxiedSlotInfo.InfusionProxy;
import mekanism.common.tile.component.config.slot.IProxiedSlotInfo.InventoryProxy;
import mekanism.common.tile.component.config.slot.IProxiedSlotInfo.PigmentProxy;
import mekanism.common.tile.component.config.slot.IProxiedSlotInfo.ProxySlotInfoCreator;
import mekanism.common.tile.component.config.slot.IProxiedSlotInfo.SlurryProxy;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.tile.interfaces.ISustainedData;
import mekanism.common.util.CableUtils;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.ChunkPos;

public class TileEntityQuantumEntangloporter extends TileEntityMekanism implements ISideConfiguration, IFrequencyHandler, ISustainedData, IChunkLoader {

    public final TileComponentEjector ejectorComponent;
    public final TileComponentConfig configComponent;
    public final TileComponentChunkLoader<TileEntityQuantumEntangloporter> chunkLoaderComponent;

    private double lastTransferLoss;
    private double lastEnvironmentLoss;

    public TileEntityQuantumEntangloporter() {
        super(MekanismBlocks.QUANTUM_ENTANGLOPORTER);
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.FLUID, TransmissionType.GAS, TransmissionType.INFUSION,
              TransmissionType.PIGMENT, TransmissionType.SLURRY, TransmissionType.ENERGY, TransmissionType.HEAT);

        setupConfig(TransmissionType.ITEM, InventoryProxy::new, () -> hasFrequency() ? getFreq().getInventorySlots(null) : Collections.emptyList());
        setupConfig(TransmissionType.FLUID, FluidProxy::new, () -> hasFrequency() ? getFreq().getFluidTanks(null) : Collections.emptyList());
        setupConfig(TransmissionType.GAS, GasProxy::new, () -> hasFrequency() ? getFreq().getGasTanks(null) : Collections.emptyList());
        setupConfig(TransmissionType.INFUSION, InfusionProxy::new, () -> hasFrequency() ? getFreq().getInfusionTanks(null) : Collections.emptyList());
        setupConfig(TransmissionType.PIGMENT, PigmentProxy::new, () -> hasFrequency() ? getFreq().getPigmentTanks(null) : Collections.emptyList());
        setupConfig(TransmissionType.SLURRY, SlurryProxy::new, () -> hasFrequency() ? getFreq().getSlurryTanks(null) : Collections.emptyList());
        setupConfig(TransmissionType.ENERGY, EnergyProxy::new, () -> hasFrequency() ? getFreq().getEnergyContainers(null) : Collections.emptyList());

        ConfigInfo heatConfig = configComponent.getConfig(TransmissionType.HEAT);
        if (heatConfig != null) {
            Supplier<List<IHeatCapacitor>> capacitorSupplier = () -> hasFrequency() ? getFreq().getHeatCapacitors(null) : Collections.emptyList();
            heatConfig.addSlotInfo(DataType.INPUT_OUTPUT, new HeatProxy(true, false, capacitorSupplier));
            //Set default config directions
            heatConfig.fill(DataType.INPUT_OUTPUT);
            heatConfig.setCanEject(false);
        }

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM, TransmissionType.FLUID, TransmissionType.GAS, TransmissionType.INFUSION,
              TransmissionType.PIGMENT, TransmissionType.SLURRY);

        chunkLoaderComponent = new TileComponentChunkLoader<>(this);
        frequencyComponent.track(FrequencyType.INVENTORY, true, true, true);
    }

    private <T> void setupConfig(TransmissionType type, ProxySlotInfoCreator<T> proxyCreator, Supplier<List<T>> supplier) {
        ConfigInfo config = configComponent.getConfig(type);
        if (config != null) {
            config.addSlotInfo(DataType.INPUT, proxyCreator.create(true, false, supplier));
            config.addSlotInfo(DataType.OUTPUT, proxyCreator.create(false, true, supplier));
            config.addSlotInfo(DataType.INPUT_OUTPUT, proxyCreator.create(true, true, supplier));
            //Set default config directions
            config.fill(DataType.INPUT);
            config.setDataType(DataType.OUTPUT, RelativeSide.FRONT);
        }
    }

    @Nonnull
    @Override
    public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks() {
        return new QuantumEntangloporterChemicalTankHolder<>(this, TransmissionType.GAS, InventoryFrequency::getGasTanks);
    }

    @Nonnull
    @Override
    public IChemicalTankHolder<InfuseType, InfusionStack, IInfusionTank> getInitialInfusionTanks() {
        return new QuantumEntangloporterChemicalTankHolder<>(this, TransmissionType.INFUSION, InventoryFrequency::getInfusionTanks);
    }

    @Nonnull
    @Override
    public IChemicalTankHolder<Pigment, PigmentStack, IPigmentTank> getInitialPigmentTanks() {
        return new QuantumEntangloporterChemicalTankHolder<>(this, TransmissionType.PIGMENT, InventoryFrequency::getPigmentTanks);
    }

    @Nonnull
    @Override
    public IChemicalTankHolder<Slurry, SlurryStack, ISlurryTank> getInitialSlurryTanks() {
        return new QuantumEntangloporterChemicalTankHolder<>(this, TransmissionType.SLURRY, InventoryFrequency::getSlurryTanks);
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
                TileEntity adj = WorldUtils.getTileEntity(getWorld(), getPos().offset(side));
                return CapabilityUtils.getCapability(adj, Capabilities.HEAT_HANDLER_CAPABILITY, side.getOpposite()).resolve().orElse(null);
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