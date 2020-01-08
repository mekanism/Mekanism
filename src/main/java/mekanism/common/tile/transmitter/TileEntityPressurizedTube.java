package mekanism.common.tile.transmitter;

import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.tier.AlloyTier;
import mekanism.api.tier.BaseTier;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.block.transmitter.BlockPressurizedTube;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tier.TubeTier;
import mekanism.common.transmitters.grid.GasNetwork;
import mekanism.common.upgrade.transmitter.PressurizedTubeUpgradeData;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.GasUtils;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class TileEntityPressurizedTube extends TileEntityTransmitter<IGasHandler, GasNetwork, GasStack> implements IGasHandler {

    public final TubeTier tier;

    public float currentScale;

    public GasTank buffer;

    @Nonnull
    private GasStack lastWrite = GasStack.EMPTY;

    //Read only handler for support with TOP and getting network data instead of this tube's data
    private IGasHandler nullHandler = new IGasHandler() {
        @Override
        public int receiveGas(Direction side, @Nonnull GasStack stack, Action action) {
            return 0;
        }

        @Nonnull
        @Override
        public GasStack drawGas(Direction side, int amount, Action action) {
            return GasStack.EMPTY;
        }

        @Override
        public boolean canReceiveGas(Direction side, @Nonnull Gas type) {
            return false;
        }

        @Override
        public boolean canDrawGas(Direction side, @Nonnull Gas type) {
            return false;
        }

        @Nonnull
        @Override
        public GasTankInfo[] getTankInfo() {
            return TileEntityPressurizedTube.this.getTankInfo();
        }
    };

    public TileEntityPressurizedTube(IBlockProvider blockProvider) {
        super(((IHasTileEntity<TileEntityPressurizedTube>) blockProvider.getBlock()).getTileType());
        this.tier = ((BlockPressurizedTube) blockProvider.getBlock()).getTier();
        buffer = new GasTank(getCapacity());
    }

    @Override
    public void tick() {
        if (!isRemote()) {
            updateShare();
            IGasHandler[] connectedAcceptors = GasUtils.getConnectedAcceptors(getPos(), getWorld());
            for (Direction side : getConnections(ConnectionType.PULL)) {
                IGasHandler container = connectedAcceptors[side.ordinal()];
                if (container != null) {
                    GasStack received = container.drawGas(side.getOpposite(), getAvailablePull(), Action.SIMULATE);
                    if (!received.isEmpty() && takeGas(received, Action.SIMULATE) == received.getAmount()) {
                        container.drawGas(side.getOpposite(), takeGas(received, Action.EXECUTE), Action.EXECUTE);
                    }
                }
            }
        } else {
            float targetScale = getTransmitter().hasTransmitterNetwork() ? getTransmitter().getTransmitterNetwork().gasScale : (float) buffer.getStored() / (float) buffer.getCapacity();
            if (Math.abs(currentScale - targetScale) > 0.01) {
                currentScale = (9 * currentScale + targetScale) / 10;
            }
        }
        super.tick();
    }

    public int getAvailablePull() {
        if (getTransmitter().hasTransmitterNetwork()) {
            return Math.min(tier.getTubePullAmount(), getTransmitter().getTransmitterNetwork().getGasNeeded());
        }
        return Math.min(tier.getTubePullAmount(), buffer.getNeeded());
    }

    @Override
    public void updateShare() {
        if (getTransmitter().hasTransmitterNetwork() && getTransmitter().getTransmitterNetworkSize() > 0) {
            GasStack last = getSaveShare();
            //TODO: Look to see if this can be cleaned up further
            if ((!last.isEmpty() && (lastWrite.isEmpty() || !lastWrite.isStackIdentical(last))) || (last.isEmpty() && !lastWrite.isEmpty())) {
                lastWrite = last;
                //markDirty();
                //TODO: Revert this if chunk boundaries break again
                //TODO: Evaluate instead overwriting markDirty to not fire neighbor updates given we don't have any comparator interaction anyways
                world.markChunkDirty(pos, this);
            }
        }
    }

    @Nonnull
    private GasStack getSaveShare() {
        if (getTransmitter().hasTransmitterNetwork()) {
            GasNetwork transmitterNetwork = getTransmitter().getTransmitterNetwork();
            if (!transmitterNetwork.buffer.isEmpty()) {
                int remain = transmitterNetwork.buffer.getAmount() % transmitterNetwork.transmittersSize();
                int toSave = transmitterNetwork.buffer.getAmount() / transmitterNetwork.transmittersSize();
                if (transmitterNetwork.firstTransmitter().equals(getTransmitter())) {
                    toSave += remain;
                }
                return new GasStack(transmitterNetwork.buffer, toSave);
            }
        }
        return GasStack.EMPTY;
    }

    @Override
    public void onChunkUnloaded() {
        if (!isRemote() && getTransmitter().hasTransmitterNetwork()) {
            GasNetwork transmitterNetwork = getTransmitter().getTransmitterNetwork();
            if (!transmitterNetwork.buffer.isEmpty() && !lastWrite.isEmpty()) {
                transmitterNetwork.buffer.shrink(lastWrite.getAmount());
                if (transmitterNetwork.buffer.getAmount() <= 0) {
                    transmitterNetwork.buffer = GasStack.EMPTY;
                }
            }
        }
        super.onChunkUnloaded();
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        if (nbtTags.contains("cacheGas")) {
            buffer.setStack(GasStack.readFromNBT(nbtTags.getCompound("cacheGas")));
        } else {
            buffer.setEmpty();
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        if (!lastWrite.isEmpty()) {
            nbtTags.put("cacheGas", lastWrite.write(new CompoundNBT()));
        } else {
            nbtTags.remove("cacheGas");
        }
        return nbtTags;
    }

    @Override
    public TransmissionType getTransmissionType() {
        return TransmissionType.GAS;
    }

    @Override
    public TransmitterType getTransmitterType() {
        return TransmitterType.PRESSURIZED_TUBE;
    }

    @Override
    public boolean isValidAcceptor(TileEntity tile, Direction side) {
        return GasUtils.isValidAcceptorOnSide(tile, side);
    }

    @Override
    public boolean isValidTransmitter(TileEntity tile) {
        if (!super.isValidTransmitter(tile)) {
            return false;
        }
        if (!(tile instanceof TileEntityPressurizedTube)) {
            return true;
        }
        GasStack buffer = getBufferWithFallback();
        GasStack otherBuffer = ((TileEntityPressurizedTube) tile).getBufferWithFallback();
        return buffer == null || otherBuffer == null || buffer.isTypeEqual(otherBuffer);
    }

    @Override
    public GasNetwork createNewNetwork() {
        return new GasNetwork();
    }

    @Override
    public GasNetwork createNetworkByMerging(Collection<GasNetwork> networks) {
        return new GasNetwork(networks);
    }

    @Override
    protected boolean canHaveIncompatibleNetworks() {
        return true;
    }

    @Override
    public int getCapacity() {
        return tier.getTubeCapacity();
    }

    @Nonnull
    @Override
    public GasStack getBuffer() {
        return buffer == null ? GasStack.EMPTY : buffer.getStack();
    }

    @Override
    public void takeShare() {
        if (getTransmitter().hasTransmitterNetwork()) {
            GasNetwork transmitterNetwork = getTransmitter().getTransmitterNetwork();
            if (!transmitterNetwork.buffer.isEmpty() && !lastWrite.isEmpty()) {
                transmitterNetwork.buffer.shrink(lastWrite.getAmount());
                buffer.setStack(lastWrite);
            }
        }
    }

    @Override
    public int receiveGas(Direction side, @Nonnull GasStack stack, Action action) {
        if (getConnectionType(side) == ConnectionType.NORMAL || getConnectionType(side) == ConnectionType.PULL) {
            return takeGas(stack, action);
        }
        return 0;
    }

    @Nonnull
    @Override
    public GasStack drawGas(Direction side, int amount, Action action) {
        return GasStack.EMPTY;
    }

    @Override
    public boolean canReceiveGas(Direction side, @Nonnull Gas type) {
        return getConnectionType(side) == ConnectionType.NORMAL || getConnectionType(side) == ConnectionType.PULL;
    }

    @Override
    public boolean canDrawGas(Direction side, @Nonnull Gas type) {
        return false;
    }

    public int takeGas(GasStack gasStack, Action action) {
        if (getTransmitter().hasTransmitterNetwork()) {
            return getTransmitter().getTransmitterNetwork().emit(gasStack, action);
        }
        return buffer.fill(gasStack, action);
    }

    @Nonnull
    @Override
    public GasTankInfo[] getTankInfo() {
        if (getTransmitter().hasTransmitterNetwork()) {
            GasNetwork network = getTransmitter().getTransmitterNetwork();
            GasTank networkTank = new GasTank(network.getCapacity());
            networkTank.setStack(network.getBuffer());
            return new GasTankInfo[]{networkTank};
        }
        return new GasTankInfo[]{buffer};
    }

    @Override
    public IGasHandler getCachedAcceptor(Direction side) {
        return CapabilityUtils.getCapabilityHelper(getCachedTile(side), Capabilities.GAS_HANDLER_CAPABILITY, side.getOpposite()).getValue();
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
                return BlockStateHelper.copyStateData(current, MekanismBlocks.BASIC_PRESSURIZED_TUBE.getBlock().getDefaultState());
            case ADVANCED:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.ADVANCED_PRESSURIZED_TUBE.getBlock().getDefaultState());
            case ELITE:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.ELITE_PRESSURIZED_TUBE.getBlock().getDefaultState());
            case ULTIMATE:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.ULTIMATE_PRESSURIZED_TUBE.getBlock().getDefaultState());
        }
        return current;
    }

    @Nullable
    @Override
    protected PressurizedTubeUpgradeData getUpgradeData() {
        return new PressurizedTubeUpgradeData(redstoneReactive, connectionTypes, getBuffer());
    }

    @Override
    protected void parseUpgradeData(@Nonnull TransmitterUpgradeData upgradeData) {
        if (upgradeData instanceof PressurizedTubeUpgradeData) {
            PressurizedTubeUpgradeData data = (PressurizedTubeUpgradeData) upgradeData;
            redstoneReactive = data.redstoneReactive;
            connectionTypes = data.connectionTypes;
            takeGas(data.contents, Action.EXECUTE);
        } else {
            super.parseUpgradeData(upgradeData);
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            return Capabilities.GAS_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> side == null ? nullHandler : this));
        }
        return super.getCapability(capability, side);
    }
}