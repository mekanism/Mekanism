package mekanism.common.tile;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.IHeatTransfer;
import mekanism.api.RelativeSide;
import mekanism.api.TileNetworkList;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.api.inventory.slot.IInventorySlot;
import mekanism.api.sustained.ISustainedData;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ITankManager;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.chunkloading.IChunkLoader;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.frequency.IFrequencyHandler;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentChunkLoader;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.EnergySlotInfo;
import mekanism.common.tile.component.config.slot.FluidSlotInfo;
import mekanism.common.tile.component.config.slot.GasSlotInfo;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.tile.component.config.slot.ProxiedSlotInfo;
import mekanism.common.util.CableUtils;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class TileEntityQuantumEntangloporter extends TileEntityMekanism implements ISideConfiguration, ITankManager, IFluidHandlerWrapper, IFrequencyHandler,
      IGasHandler, IHeatTransfer, IComputerIntegration, IChunkLoader, ISustainedData {

    private static final String[] methods = new String[]{"setFrequency"};
    public InventoryFrequency frequency;
    public double heatToAbsorb = 0;
    public double lastTransferLoss;
    public double lastEnvironmentLoss;
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
            Supplier<List<FluidTank>> tankSupplier = () -> hasFrequency() ? Collections.singletonList(frequency.storedFluid) : Collections.emptyList();
            fluidConfig.addSlotInfo(DataType.INPUT, new ProxiedSlotInfo.Fluid(true, false, tankSupplier));
            fluidConfig.addSlotInfo(DataType.OUTPUT, new ProxiedSlotInfo.Fluid(false, true, tankSupplier));
            //Set default config directions
            fluidConfig.fill(DataType.INPUT);
            fluidConfig.setDataType(RelativeSide.FRONT, DataType.OUTPUT);
        }

        ConfigInfo gasConfig = configComponent.getConfig(TransmissionType.GAS);
        if (gasConfig != null) {
            Supplier<List<GasTank>> tankSupplier = () -> hasFrequency() ? Collections.singletonList(frequency.storedGas) : Collections.emptyList();
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
    public void onUpdate() {
        if (!isRemote()) {
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
                    markDirty();
                }

                if (frequency != null) {
                    frequency = (InventoryFrequency) manager.update(Coord4D.get(this), frequency);
                    if (frequency == null) {
                        markDirty();
                    }
                }
            } else {
                frequency = null;
                if (lastFreq != null) {
                    markDirty();
                }
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
            FrequencyManager manager = new FrequencyManager(InventoryFrequency.class, InventoryFrequency.ENTANGLOPORTER, getSecurity().getOwnerUUID());
            Mekanism.privateEntangloporters.put(getSecurity().getOwnerUUID(), manager);
            manager.createOrLoad(getWorld());
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
                markDirty();
                return;
            }
        }

        Frequency freq = new InventoryFrequency(name, getSecurity().getOwnerUUID()).setPublic(publicFreq);
        freq.activeCoords.add(Coord4D.get(this));
        manager.addFrequency(freq);
        frequency = (InventoryFrequency) freq;
        MekanismUtils.saveChunk(this);
        markDirty();
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        if (nbtTags.contains("frequency")) {
            frequency = new InventoryFrequency(nbtTags.getCompound("frequency"));
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
            nbtTags.put("frequency", frequencyTag);
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
            return;
        }

        super.handlePacketData(dataStream);

        if (isRemote()) {
            lastTransferLoss = dataStream.readDouble();
            lastEnvironmentLoss = dataStream.readDouble();
            if (dataStream.readBoolean()) {
                frequency = new InventoryFrequency(dataStream);
            } else {
                frequency = null;
            }

            publicCache.clear();
            privateCache.clear();

            int amount = dataStream.readInt();
            for (int i = 0; i < amount; i++) {
                publicCache.add(new InventoryFrequency(dataStream));
            }
            amount = dataStream.readInt();
            for (int i = 0; i < amount; i++) {
                privateCache.add(new InventoryFrequency(dataStream));
            }
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(lastTransferLoss);
        data.add(lastEnvironmentLoss);

        if (frequency != null) {
            data.add(true);
            frequency.write(data);
        } else {
            data.add(false);
        }

        data.add(Mekanism.publicEntangloporters.getFrequencies().size());
        for (Frequency freq : Mekanism.publicEntangloporters.getFrequencies()) {
            freq.write(data);
        }

        FrequencyManager manager = getManager(new InventoryFrequency(null, null).setPublic(false));
        if (manager != null) {
            data.add(manager.getFrequencies().size());
            for (Frequency freq : manager.getFrequencies()) {
                freq.write(data);
            }
        } else {
            data.add(0);
        }
        return data;
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
        return !hasFrequency() ? 0 : MekanismConfig.general.quantumEntangloporterEnergyTransfer.get();
    }

    @Override
    public double getEnergy() {
        return !hasFrequency() ? 0 : frequency.storedEnergy;
    }

    @Override
    public void setEnergy(double energy) {
        if (hasFrequency()) {
            frequency.storedEnergy = Math.min(MekanismConfig.general.quantumEntangloporterEnergyTransfer.get(), energy);
        }
    }

    @Override
    public double getMaxEnergy() {
        return !hasFrequency() ? 0 : MekanismConfig.general.quantumEntangloporterEnergyTransfer.get();
    }

    @Override
    public int fill(Direction from, @Nonnull FluidStack resource, FluidAction fluidAction) {
        return frequency.storedFluid.fill(resource, fluidAction);
    }

    @Nonnull
    @Override
    public FluidStack drain(Direction from, int maxDrain, FluidAction fluidAction) {
        return frequency.storedFluid.drain(maxDrain, fluidAction);
    }

    @Override
    public boolean canFill(Direction from, @Nonnull FluidStack fluid) {
        if (hasFrequency()) {
            ISlotInfo slotInfo = configComponent.getSlotInfo(TransmissionType.FLUID, from);
            if (slotInfo != null && slotInfo.canInput()) {
                return FluidContainerUtils.canFill(frequency.storedFluid.getFluid(), fluid);
            }
        }
        return false;
    }

    @Override
    public boolean canDrain(Direction from, @Nonnull FluidStack fluid) {
        if (hasFrequency()) {
            ISlotInfo slotInfo = configComponent.getSlotInfo(TransmissionType.FLUID, from);
            if (slotInfo instanceof FluidSlotInfo && slotInfo.canOutput()) {
                return FluidContainerUtils.canDrain(frequency.storedFluid.getFluid(), fluid);
            }
        }
        return false;
    }

    @Override
    public IFluidTank[] getTankInfo(Direction from) {
        if (hasFrequency()) {
            ISlotInfo slotInfo = configComponent.getSlotInfo(TransmissionType.FLUID, from);
            if (slotInfo instanceof FluidSlotInfo && slotInfo.isEnabled()) {
                return new IFluidTank[]{frequency.storedFluid};
            }
        }
        return PipeUtils.EMPTY;
    }

    @Override
    public IFluidTank[] getAllTanks() {
        return hasFrequency() ? new IFluidTank[]{frequency.storedFluid} : PipeUtils.EMPTY;
    }

    @Override
    public int receiveGas(Direction side, @Nonnull GasStack stack, Action action) {
        return !hasFrequency() ? 0 : frequency.storedGas.fill(stack, action);
    }

    @Nonnull
    @Override
    public GasStack drawGas(Direction side, int amount, Action action) {
        return !hasFrequency() ? GasStack.EMPTY : frequency.storedGas.drain(amount, action);
    }

    @Override
    public boolean canReceiveGas(Direction side, @Nonnull Gas type) {
        if (hasFrequency()) {
            ISlotInfo slotInfo = configComponent.getSlotInfo(TransmissionType.GAS, side);
            if (slotInfo instanceof GasSlotInfo && slotInfo.canInput()) {
                return frequency.storedGas.isEmpty() || type == frequency.storedGas.getType();
            }
        }
        return false;
    }

    @Override
    public boolean canDrawGas(Direction side, @Nonnull Gas type) {
        if (hasFrequency()) {
            ISlotInfo slotInfo = configComponent.getSlotInfo(TransmissionType.GAS, side);
            if (slotInfo instanceof GasSlotInfo && slotInfo.canOutput()) {
                return frequency.storedGas.isEmpty() || type == frequency.storedGas.getType();
            }
        }
        return false;
    }

    @Nonnull
    @Override
    public GasTankInfo[] getTankInfo() {
        return hasFrequency() ? new GasTankInfo[]{frequency.storedGas} : IGasHandler.NONE;
    }

    @Override
    public boolean handleInventory() {
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
    public Object[] getTanks() {
        if (!hasFrequency()) {
            return null;
        }
        return new Object[]{frequency.storedFluid, frequency.storedGas};
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
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (isCapabilityDisabled(capability, side)) {
            return LazyOptional.empty();
        }
        if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            return Capabilities.GAS_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        if (capability == Capabilities.HEAT_TRANSFER_CAPABILITY) {
            return Capabilities.HEAT_TRANSFER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> new FluidHandlerWrapper(this, side)));
        }
        return super.getCapability(capability, side);
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        if (configComponent.isCapabilityDisabled(capability, side)) {
            return true;
        } else if (capability == Capabilities.GAS_HANDLER_CAPABILITY || capability == Capabilities.HEAT_TRANSFER_CAPABILITY ||
                   capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return side != null && !hasFrequency();
        }
        return super.isCapabilityDisabled(capability, side);
    }

    @Override
    public String[] getMethods() {
        return methods;
    }

    @Override
    public Object[] invoke(int method, Object[] arguments) throws NoSuchMethodException {
        if (method == 0) {
            if (!(arguments[0] instanceof String) || !(arguments[1] instanceof Boolean)) {
                return new Object[]{"Invalid parameters."};
            }
            String freq = ((String) arguments[0]).trim();
            boolean isPublic = (Boolean) arguments[1];
            setFrequency(freq, isPublic);
            return new Object[]{"Frequency set."};
        }
        throw new NoSuchMethodException();
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
        if (!hasInventory() || !hasFrequency()) {
            return Collections.emptyList();
        }
        return frequency.inventorySlots;
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        if (frequency != null) {
            ItemDataUtils.setCompound(itemStack, "frequency", frequency.getIdentity().serialize());
        }
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        Frequency.Identity freq = Frequency.Identity.load(ItemDataUtils.getCompound(itemStack, "frequency"));
        if (freq != null) {
            setFrequency(freq.name, freq.publicFreq);
        }
    }

    @Override
    public Map<String, String> getTileDataRemap() {
        Map<String, String> remap = new Object2ObjectOpenHashMap<>();
        remap.put("frequency.name", "frequency.name");
        remap.put("frequency.publicFreq", "frequency.publicFreq");
        return remap;
    }
}