package mekanism.common.tile.transmitter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.NBTConstants;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.tier.AlloyTier;
import mekanism.api.tier.BaseTier;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.resolver.advanced.AdvancedEnergyCapabilityResolver;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tier.CableTier;
import mekanism.common.transmitters.TransmitterImpl;
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
import net.minecraftforge.common.util.Constants.NBT;

public class TileEntityUniversalCable extends TileEntityTransmitter<IStrictEnergyHandler, EnergyNetwork, FloatingLong> implements IMekanismStrictEnergyHandler {

    public final CableTier tier;

    private final List<IEnergyContainer> energyContainers;
    public BasicEnergyContainer buffer;
    public FloatingLong lastWrite = FloatingLong.ZERO;

    public TileEntityUniversalCable(IBlockProvider blockProvider) {
        super(blockProvider);
        this.tier = Attribute.getTier(blockProvider.getBlock(), CableTier.class);
        buffer = BasicEnergyContainer.create(getCapacityAsFloatingLong(), BasicEnergyContainer.alwaysFalse, BasicEnergyContainer.alwaysTrue, this);
        energyContainers = Collections.singletonList(buffer);
        addCapabilityResolver(new AdvancedEnergyCapabilityResolver(this));
    }

    @Override
    public void tick() {
        if (!isRemote()) {
            Set<Direction> connections = getConnections(ConnectionType.PULL);
            if (!connections.isEmpty()) {
                for (IStrictEnergyHandler connectedAcceptor : CableUtils.getConnectedAcceptors(getPos(), getWorld(), connections)) {
                    if (connectedAcceptor != null) {
                        FloatingLong received = connectedAcceptor.extractEnergy(getAvailablePull(), Action.SIMULATE);
                        if (!received.isZero() && takeEnergy(received, Action.SIMULATE).isZero()) {
                            //If we received some energy and are able to insert it all
                            FloatingLong remainder = takeEnergy(received, Action.EXECUTE);
                            connectedAcceptor.extractEnergy(received.subtract(remainder), Action.EXECUTE);
                        }
                    }
                }
            }
        }
        super.tick();
    }

    private FloatingLong getAvailablePull() {
        if (getTransmitter().hasTransmitterNetwork()) {
            return getCapacityAsFloatingLong().min(getTransmitter().getTransmitterNetwork().energyContainer.getNeeded());
        }
        return getCapacityAsFloatingLong().min(buffer.getNeeded());
    }

    @Nonnull
    @Override
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
        if (getTransmitter().hasTransmitterNetwork()) {
            return getTransmitter().getTransmitterNetwork().getEnergyContainers(side);
        }
        return energyContainers;
    }

    @Override
    public void onContentsChanged() {
        markDirty(false);
    }

    @Nonnull
    @Override
    public FloatingLong insertEnergy(int container, @Nonnull FloatingLong amount, @Nullable Direction side, @Nonnull Action action) {
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
    public TransmitterType getTransmitterType() {
        return TransmitterType.UNIVERSAL_CABLE;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        if (nbtTags.contains(NBTConstants.ENERGY_STORED, NBT.TAG_STRING)) {
            try {
                lastWrite = FloatingLong.parseFloatingLong(nbtTags.getString(NBTConstants.ENERGY_STORED));
            } catch (NumberFormatException e) {
                lastWrite = FloatingLong.ZERO;
            }
        } else {
            lastWrite = FloatingLong.ZERO;
        }
        buffer.setEnergy(lastWrite);
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        if (lastWrite.isZero()) {
            nbtTags.remove(NBTConstants.ENERGY_STORED);
        } else {
            nbtTags.putString(NBTConstants.ENERGY_STORED, lastWrite.toString());
        }
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
    public boolean isValidAcceptor(TileEntity tile, Direction side) {
        if (CapabilityUtils.getCapability(tile, Capabilities.GRID_TRANSMITTER_CAPABILITY, null).filter(transmitter ->
              TransmissionType.checkTransmissionType(transmitter, TransmissionType.ENERGY)).isPresent()) {
            return false;
        }
        return EnergyCompatUtils.hasStrictEnergyHandlerAndListen(tile, side.getOpposite(), pos -> {
            if (world != null && world.isBlockPresent(pos)) {
                refreshConnections(side);
            }
        });
    }

    @Override
    public EnergyNetwork createNewNetwork() {
        return new EnergyNetwork();
    }

    @Override
    public EnergyNetwork createNewNetworkWithID(UUID networkID) {
        return new EnergyNetwork(networkID);
    }

    @Nonnull
    @Override
    public FloatingLong getBuffer() {
        return buffer.getEnergy();
    }

    @Override
    public boolean noBufferOrFallback() {
        return getBufferWithFallback().isZero();
    }

    @Nonnull
    @Override
    public FloatingLong getBufferWithFallback() {
        FloatingLong buffer = getBuffer();
        //If we don't have a buffer try falling back to the network's buffer
        if (buffer.isZero() && getTransmitter().hasTransmitterNetwork()) {
            return getTransmitter().getTransmitterNetwork().getBuffer();
        }
        return buffer;
    }

    @Override
    public void takeShare() {
        if (getTransmitter().hasTransmitterNetwork()) {
            EnergyNetwork transmitterNetwork = getTransmitter().getTransmitterNetwork();
            if (!transmitterNetwork.energyContainer.isEmpty() && !lastWrite.isZero()) {
                transmitterNetwork.energyContainer.setEnergy(transmitterNetwork.energyContainer.getEnergy().subtract(lastWrite));
                buffer.setEnergy(lastWrite);
            }
        }
    }

    @Nonnull
    @Override
    public FloatingLong getCapacityAsFloatingLong() {
        return tier.getCableCapacity();
    }

    @Override
    public long getCapacity() {
        return getCapacityAsFloatingLong().longValue();
    }

    /**
     * @return remainder
     */
    private FloatingLong takeEnergy(FloatingLong amount, Action action) {
        if (getTransmitter().hasTransmitterNetwork()) {
            return getTransmitter().getTransmitterNetwork().energyContainer.insert(amount, action, AutomationType.INTERNAL);
        }
        return buffer.insert(amount, action, AutomationType.INTERNAL);
    }

    @Override
    public IStrictEnergyHandler getCachedAcceptor(Direction side) {
        return EnergyCompatUtils.getStrictEnergyHandler(getCachedTile(side), side.getOpposite());
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
    public CompoundNBT getUpdateTag() {
        //Note: We add the stored information to the initial update tag and not to the one we sync on side changes which uses getReducedUpdateTag
        CompoundNBT updateTag = super.getUpdateTag();
        TransmitterImpl<IStrictEnergyHandler, EnergyNetwork, FloatingLong> transmitter = getTransmitter();
        if (transmitter.hasTransmitterNetwork()) {
            updateTag.putString(NBTConstants.ENERGY_STORED, transmitter.getTransmitterNetwork().energyContainer.getEnergy().toString());
            updateTag.putFloat(NBTConstants.SCALE, transmitter.getTransmitterNetwork().energyScale);
        }
        return updateTag;
    }

    @Override
    protected void handleContentsUpdateTag(@Nonnull EnergyNetwork network, @Nonnull CompoundNBT tag) {
        NBTUtils.setFloatingLongIfPresent(tag, NBTConstants.ENERGY_STORED, network.energyContainer::setEnergy);
        NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE, scale -> network.energyScale = scale);
    }
}