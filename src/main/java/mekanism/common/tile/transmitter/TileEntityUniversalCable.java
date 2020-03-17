package mekanism.common.tile.transmitter;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.NBTConstants;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.inventory.AutomationType;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.tier.AlloyTier;
import mekanism.api.tier.BaseTier;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.proxy.ProxyStrictEnergyHandler;
import mekanism.common.integration.EnergyCompatUtils;
import mekanism.common.integration.forgeenergy.ForgeEnergyIntegration;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tier.CableTier;
import mekanism.common.transmitters.grid.EnergyNetwork;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;
import mekanism.common.upgrade.transmitter.UniversalCableUpgradeData;
import mekanism.common.util.CableUtils;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;

public class TileEntityUniversalCable extends TileEntityTransmitter<IStrictEnergyHandler, EnergyNetwork, Double> implements IMekanismStrictEnergyHandler {

    public final CableTier tier;

    private ProxyStrictEnergyHandler readOnlyStrictEnergyHandler;
    private final Map<Direction, ProxyStrictEnergyHandler> strictEnergyHandlers;
    private final List<IEnergyContainer> energyContainers;
    public BasicEnergyContainer buffer;
    public double currentPower = 0;
    public double lastWrite = 0;

    private CapabilityWrapperManager<IStrictEnergyHandler, ForgeEnergyIntegration> forgeEnergyManager = new CapabilityWrapperManager<>(IStrictEnergyHandler.class, ForgeEnergyIntegration.class);

    public TileEntityUniversalCable(IBlockProvider blockProvider) {
        super(((IHasTileEntity<TileEntityUniversalCable>) blockProvider.getBlock()).getTileType());
        this.tier = Attribute.getTier(blockProvider.getBlock(), CableTier.class);
        strictEnergyHandlers = new EnumMap<>(Direction.class);
        buffer = BasicEnergyContainer.create(getCapacity(), BasicEnergyContainer.alwaysFalse, BasicEnergyContainer.alwaysTrue, this);
        energyContainers = Collections.singletonList(buffer);
    }

    /**
     * Lazily get and cache an IStrictEnergyHandler instance for the given side, and make it be read only if something else is trying to interact with us using the null
     * side
     */
    private IStrictEnergyHandler getEnergyHandler(@Nullable Direction side) {
        if (!canHandleEnergy()) {
            return null;
        }
        if (side == null) {
            if (readOnlyStrictEnergyHandler == null) {
                readOnlyStrictEnergyHandler = new ProxyStrictEnergyHandler(this, null, null);
            }
            return readOnlyStrictEnergyHandler;
        }
        ProxyStrictEnergyHandler energyHandler = strictEnergyHandlers.get(side);
        if (energyHandler == null) {
            strictEnergyHandlers.put(side, energyHandler = new ProxyStrictEnergyHandler(this, side, null));
        }
        return energyHandler;
    }

    @Override
    public void tick() {
        if (isRemote()) {
            double targetPower = getTransmitter().hasTransmitterNetwork() ? getTransmitter().getTransmitterNetwork().clientEnergyScale : 0;
            if (Math.abs(currentPower - targetPower) > 0.01) {
                currentPower = (9 * currentPower + targetPower) / 10;
            }
        } else {
            updateShare();
            List<Direction> connections = getConnections(ConnectionType.PULL);
            if (!connections.isEmpty()) {
                TileEntity[] connectedOutputters = CableUtils.getConnectedOutputters(this, getPos(), getWorld());
                for (Direction side : connections) {
                    IStrictEnergyHandler strictEnergyHandler = EnergyCompatUtils.get(connectedOutputters[side.ordinal()], side.getOpposite());
                    if (strictEnergyHandler != null) {
                        double received = strictEnergyHandler.extractEnergy(getAvailablePull(), Action.SIMULATE);
                        if (received > 0 && takeEnergy(received, Action.SIMULATE) == 0) {
                            //If we received some energy and are able to insert it all
                            double remainder = takeEnergy(received, Action.EXECUTE);
                            strictEnergyHandler.extractEnergy(received - remainder, Action.EXECUTE);
                        }
                    }
                }
            }
        }
        super.tick();
    }

    private double getAvailablePull() {
        if (getTransmitter().hasTransmitterNetwork()) {
            return Math.min(tier.getCableCapacity(), getTransmitter().getTransmitterNetwork().energyContainer.getNeeded());
        }
        return Math.min(tier.getCableCapacity(), buffer.getNeeded());
    }

    @Override
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
        if (getTransmitter().hasTransmitterNetwork()) {
            //TODO: Do we want this to fallback to local if the one on the network is empty?
            return getTransmitter().getTransmitterNetwork().getEnergyContainers(side);
        }
        return energyContainers;
    }

    @Override
    public void onContentsChanged() {
        markDirty();
    }

    @Override
    public double insertEnergy(int container, double amount, @Nullable Direction side, @Nonnull Action action) {
        IEnergyContainer energyContainer = getEnergyContainer(container, side);
        if (energyContainer == null) {
            return amount;
        } else if (side == null) {
            return energyContainer.insert(amount, action, AutomationType.INTERNAL);
        }
        //If we have a side only allow inserting if our connection allows it
        ConnectionType connectionType = getConnectionType(side);
        if (connectionType == ConnectionType.NORMAL || connectionType == ConnectionType.PULL) {
            return energyContainer.insert(amount, action, AutomationType.EXTERNAL);
        }
        return amount;
    }

    @Override
    public void updateShare() {
        if (getTransmitter().hasTransmitterNetwork() && getTransmitter().getTransmitterNetworkSize() > 0) {
            double last = getSaveShare();
            if (last != lastWrite) {
                lastWrite = last;
                markDirty();
            }
        }
    }

    private double getSaveShare() {
        if (getTransmitter().hasTransmitterNetwork()) {
            return EnergyNetwork.round(getTransmitter().getTransmitterNetwork().energyContainer.getEnergy() * (1F / getTransmitter().getTransmitterNetwork().transmittersSize()));
        }
        return 0;
    }

    @Override
    public TransmitterType getTransmitterType() {
        return TransmitterType.UNIVERSAL_CABLE;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        NBTUtils.setDoubleIfPresent(nbtTags, NBTConstants.ENERGY_STORED, buffer::setEnergy);
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putDouble(NBTConstants.ENERGY_STORED, lastWrite);
        return nbtTags;
    }

    @Override
    public TransmissionType getTransmissionType() {
        return TransmissionType.ENERGY;
    }

    @Override
    public EnergyNetwork createNetworkByMerging(Collection<EnergyNetwork> networks) {
        return new EnergyNetwork(networks);
    }

    @Override
    public boolean isValidAcceptor(TileEntity acceptor, Direction side) {
        return CableUtils.isValidAcceptorOnSide(this, acceptor, side);
    }

    @Override
    public EnergyNetwork createNewNetwork() {
        return new EnergyNetwork();
    }

    @Nonnull
    @Override
    public Double getBuffer() {
        return buffer.getEnergy();
    }

    @Override
    public boolean noBufferOrFallback() {
        return getBufferWithFallback() == 0;
    }

    @Nonnull
    @Override
    public Double getBufferWithFallback() {
        Double buffer = getBuffer();
        //If we don't have a buffer try falling back to the network's buffer
        if (buffer == 0 && getTransmitter().hasTransmitterNetwork()) {
            return getTransmitter().getTransmitterNetwork().getBuffer();
        }
        return buffer;
    }

    @Override
    public void takeShare() {
        if (getTransmitter().hasTransmitterNetwork()) {
            EnergyNetwork transmitterNetwork = getTransmitter().getTransmitterNetwork();
            if (!transmitterNetwork.energyContainer.isEmpty() && lastWrite > 0) {
                transmitterNetwork.energyContainer.setEnergy(transmitterNetwork.energyContainer.getEnergy() - lastWrite);
                buffer.setEnergy(lastWrite);
            }
        }
    }

    @Override
    public int getCapacity() {
        return tier.getCableCapacity();
    }

    /**
     * @return remainder
     */
    private double takeEnergy(double amount, Action action) {
        if (getTransmitter().hasTransmitterNetwork()) {
            return getTransmitter().getTransmitterNetwork().energyContainer.insert(amount, action, AutomationType.INTERNAL);
        }
        return buffer.insert(amount, action, AutomationType.INTERNAL);
    }

    @Override
    public IStrictEnergyHandler getCachedAcceptor(Direction side) {
        return EnergyCompatUtils.get(getCachedTile(side), side.getOpposite());
    }

    @Override
    protected boolean canUpgrade(AlloyTier alloyTier) {
        return alloyTier.getBaseTier().ordinal() == tier.getBaseTier().ordinal() + 1;
    }

    @Nonnull
    @Override
    protected BlockState upgradeResult(@Nonnull BlockState current, @Nonnull BaseTier tier) {
        switch (tier) {
            case BASIC:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.BASIC_UNIVERSAL_CABLE.getBlock().getDefaultState());
            case ADVANCED:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.ADVANCED_UNIVERSAL_CABLE.getBlock().getDefaultState());
            case ELITE:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.ELITE_UNIVERSAL_CABLE.getBlock().getDefaultState());
            case ULTIMATE:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.ULTIMATE_UNIVERSAL_CABLE.getBlock().getDefaultState());
        }
        return current;
    }

    @Nullable
    @Override
    protected UniversalCableUpgradeData getUpgradeData() {
        return new UniversalCableUpgradeData(redstoneReactive, connectionTypes, buffer);
    }

    @Override
    protected void parseUpgradeData(@Nonnull TransmitterUpgradeData upgradeData) {
        if (upgradeData instanceof UniversalCableUpgradeData) {
            UniversalCableUpgradeData data = (UniversalCableUpgradeData) upgradeData;
            redstoneReactive = data.redstoneReactive;
            connectionTypes = data.connectionTypes;
            buffer.setEnergy(data.buffer.getEnergy());
        } else {
            super.parseUpgradeData(upgradeData);
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (CapabilityUtils.isEnergyCapability(capability)) {
            List<IEnergyContainer> energyContainers = getEnergyContainers(side);
            if (energyContainers.isEmpty()) {
                return LazyOptional.empty();
            }
            if (capability == Capabilities.STRICT_ENERGY_CAPABILITY) {
                //Don't return an energy handler if we don't actually even have any containers for that side
                return Capabilities.STRICT_ENERGY_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> getEnergyHandler(side)));
            } else if (capability == CapabilityEnergy.ENERGY) {
                return CapabilityEnergy.ENERGY.orEmpty(capability, LazyOptional.of(() -> forgeEnergyManager.getWrapper(this, side)));
            }
        }
        return super.getCapability(capability, side);
    }
}