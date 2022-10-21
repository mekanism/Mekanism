package mekanism.common.tile;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import mekanism.api.IContentsListener;
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
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.heat.HeatAPI.HeatTransfer;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IHeatHandler;
import mekanism.common.capabilities.Capabilities;
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
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.lib.chunkloading.IChunkLoader;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.SubstanceType;
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
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityQuantumEntangloporter extends TileEntityConfigurableMachine implements IChunkLoader {

    private final TileComponentChunkLoader<TileEntityQuantumEntangloporter> chunkLoaderComponent;

    private double lastTransferLoss;
    private double lastEnvironmentLoss;

    public TileEntityQuantumEntangloporter(BlockPos pos, BlockState state) {
        super(MekanismBlocks.QUANTUM_ENTANGLOPORTER, pos, state);
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
        //Note: All eject types except for items is handled by the frequency
        //Only allow trying to eject if we have a frequency, because otherwise all our containers and sides will just be empty anyway
        // also require that we can function before auto ejecting
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM).setCanEject(type -> hasFrequency() && MekanismUtils.canFunction(this));

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

    @NotNull
    @Override
    public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(IContentsListener listener) {
        return new QuantumEntangloporterChemicalTankHolder<>(this, TransmissionType.GAS, InventoryFrequency::getGasTanks);
    }

    @NotNull
    @Override
    public IChemicalTankHolder<InfuseType, InfusionStack, IInfusionTank> getInitialInfusionTanks(IContentsListener listener) {
        return new QuantumEntangloporterChemicalTankHolder<>(this, TransmissionType.INFUSION, InventoryFrequency::getInfusionTanks);
    }

    @NotNull
    @Override
    public IChemicalTankHolder<Pigment, PigmentStack, IPigmentTank> getInitialPigmentTanks(IContentsListener listener) {
        return new QuantumEntangloporterChemicalTankHolder<>(this, TransmissionType.PIGMENT, InventoryFrequency::getPigmentTanks);
    }

    @NotNull
    @Override
    public IChemicalTankHolder<Slurry, SlurryStack, ISlurryTank> getInitialSlurryTanks(IContentsListener listener) {
        return new QuantumEntangloporterChemicalTankHolder<>(this, TransmissionType.SLURRY, InventoryFrequency::getSlurryTanks);
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
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (hasFrequency()) {
            getFreq().handleEject(level.getGameTime());
            updateHeatCapacitors(null); // manually trigger heat capacitor update
            HeatTransfer loss = simulate();
            lastTransferLoss = loss.adjacentTransfer();
            lastEnvironmentLoss = loss.environmentTransfer();
        } else {
            lastTransferLoss = 0;
            lastEnvironmentLoss = 0;
        }
    }

    @ComputerMethod
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
                BlockEntity adj = WorldUtils.getTileEntity(getLevel(), getBlockPos().relative(side));
                return CapabilityUtils.getCapability(adj, Capabilities.HEAT_HANDLER, side.getOpposite()).resolve().orElse(null);
            }
        }
        return null;
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

    @ComputerMethod(nameOverride = "getTransferLoss")
    public double getLastTransferLoss() {
        return lastTransferLoss;
    }

    @ComputerMethod(nameOverride = "getEnvironmentalLoss")
    public double getLastEnvironmentLoss() {
        return lastEnvironmentLoss;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableDouble.create(this::getLastTransferLoss, value -> lastTransferLoss = value));
        container.track(SyncableDouble.create(this::getLastEnvironmentLoss, value -> lastEnvironmentLoss = value));
    }

    //Methods relating to IComputerTile
    @ComputerMethod
    private Collection<InventoryFrequency> getFrequencies() {
        return FrequencyType.INVENTORY.getManagerWrapper().getPublicManager().getFrequencies();
    }

    @ComputerMethod
    private InventoryFrequency getFrequency() throws ComputerException {
        InventoryFrequency frequency = getFreq();
        if (frequency == null || !frequency.isValid()) {
            throw new ComputerException("No frequency is currently selected.");
        }
        return frequency;
    }

    @ComputerMethod
    private void setFrequency(String name) throws ComputerException {
        validateSecurityIsPublic();
        InventoryFrequency frequency = FrequencyType.INVENTORY.getManagerWrapper().getPublicManager().getFrequency(name);
        if (frequency == null) {
            throw new ComputerException("No public inventory frequency with name '%s' found.", name);
        }
        setFrequency(FrequencyType.INVENTORY, frequency.getIdentity(), getOwnerUUID());
    }

    @ComputerMethod
    private void createFrequency(String name) throws ComputerException {
        validateSecurityIsPublic();
        InventoryFrequency frequency = FrequencyType.INVENTORY.getManagerWrapper().getPublicManager().getFrequency(name);
        if (frequency != null) {
            throw new ComputerException("Unable to create public inventory frequency with name '%s' as one already exists.", name);
        }
        setFrequency(FrequencyType.INVENTORY, new FrequencyIdentity(name, true), getOwnerUUID());
    }

    //Note: A bunch of the below buffer getters are rather "hardcoded", but they should be fine unless we decide to add support for more buffers at some point
    // in which case we can just add some overloads while we deprecate these
    @ComputerMethod
    private ItemStack getBufferItem() throws ComputerException {
        return getFrequency().getInventorySlots(null).get(0).getStack();
    }

    @WrappingComputerMethod(wrapper = ComputerFluidTankWrapper.class, methodNames = {"getBufferFluid", "getBufferFluidCapacity", "getBufferFluidNeeded",
                                                                                     "getBufferFluidFilledPercentage"})
    private IExtendedFluidTank getBufferFluidTank() throws ComputerException {
        return getFrequency().getFluidTanks(null).get(0);
    }

    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getBufferGas", "getBufferGasCapacity", "getBufferGasNeeded",
                                                                                        "getBufferGasFilledPercentage"})
    private IGasTank getBufferGasTank() throws ComputerException {
        return getFrequency().getGasTanks(null).get(0);
    }

    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getBufferInfuseType", "getBufferInfuseTypeCapacity", "getBufferInfuseTypeNeeded",
                                                                                        "getBufferInfuseTypeFilledPercentage"})
    private IInfusionTank getBufferInfuseTypeTank() throws ComputerException {
        return getFrequency().getInfusionTanks(null).get(0);
    }

    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getBufferPigment", "getBufferPigmentCapacity", "getBufferPigmentNeeded",
                                                                                        "getBufferPigmentFilledPercentage"})
    private IPigmentTank getBufferPigmentTank() throws ComputerException {
        return getFrequency().getPigmentTanks(null).get(0);
    }

    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getBufferSlurry", "getBufferSlurryCapacity", "getBufferSlurryNeeded",
                                                                                        "getBufferSlurryFilledPercentage"})
    private ISlurryTank getBufferSlurryTank() throws ComputerException {
        return getFrequency().getSlurryTanks(null).get(0);
    }

    @ComputerMethod
    private double getTemperature() throws ComputerException {
        return getFrequency().getTotalTemperature();
    }
    //End methods IComputerTile
}