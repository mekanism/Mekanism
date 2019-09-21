package mekanism.common.tile.transmitter;

import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.TileNetworkList;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.block.transmitter.BlockPressurizedTube;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tier.BaseTier;
import mekanism.common.tier.TubeTier;
import mekanism.common.transmitters.grid.GasNetwork;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.GasUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class TileEntityPressurizedTube extends TileEntityTransmitter<IGasHandler, GasNetwork, GasStack> implements IGasHandler {

    public TubeTier tier;

    public float currentScale;

    public GasTank buffer;

    @Nonnull
    public GasStack lastWrite = GasStack.EMPTY;

    //Read only handler for support with TOP and getting network data instead of this tube's data
    private IGasHandler nullHandler = new IGasHandler() {
        @Override
        public int receiveGas(Direction side, @Nonnull GasStack stack, boolean doTransfer) {
            return 0;
        }

        @Nonnull
        @Override
        public GasStack drawGas(Direction side, int amount, boolean doTransfer) {
            return null;
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
    public BaseTier getBaseTier() {
        return tier.getBaseTier();
    }

    @Override
    public void setBaseTier(BaseTier baseTier) {
        tier = TubeTier.get(baseTier);
        buffer.setMaxGas(getCapacity());
    }

    @Override
    public void tick() {
        if (!getWorld().isRemote) {
            updateShare();
            IGasHandler[] connectedAcceptors = GasUtils.getConnectedAcceptors(getPos(), getWorld());
            for (Direction side : getConnections(ConnectionType.PULL)) {
                IGasHandler container = connectedAcceptors[side.ordinal()];
                if (container != null) {
                    GasStack received = container.drawGas(side.getOpposite(), getAvailablePull(), false);
                    if (!received.isEmpty() && takeGas(received, false) == received.getAmount()) {
                        container.drawGas(side.getOpposite(), takeGas(received, true), true);
                    }
                }
            }
        } else {
            float targetScale = getTransmitter().hasTransmitterNetwork() ? getTransmitter().getTransmitterNetwork().gasScale : (float) buffer.getStored() / (float) buffer.getMaxGas();
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
            if ((!last.isEmpty() && !(!lastWrite.isEmpty() && lastWrite.getAmount() == last.getAmount() && lastWrite.getGas() == last.getGas())) || (last.isEmpty() && !lastWrite.isEmpty())) {
                lastWrite = last;
                markDirty();
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
        if (!getWorld().isRemote && getTransmitter().hasTransmitterNetwork()) {
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
        if (nbtTags.contains("tier")) {
            tier = TubeTier.values()[nbtTags.getInt("tier")];
        }
        buffer.setMaxGas(getCapacity());
        if (nbtTags.contains("cacheGas")) {
            buffer.setGas(GasStack.readFromNBT(nbtTags.getCompound("cacheGas")));
        } else {
            buffer.setGas(GasStack.EMPTY);
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
        nbtTags.putInt("tier", tier.ordinal());
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
    public boolean isValidTransmitter(TileEntity tileEntity) {
        if (!super.isValidTransmitter(tileEntity)) {
            return false;
        }
        if (!(tileEntity instanceof TileEntityPressurizedTube)) {
            return true;
        }
        GasStack buffer = getBufferWithFallback();
        GasStack otherBuffer = ((TileEntityPressurizedTube) tileEntity).getBufferWithFallback();
        return buffer == null || otherBuffer == null || buffer.isGasEqual(otherBuffer);
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
        if (buffer == null) {
            return GasStack.EMPTY;
        }
        return buffer.getGas();
    }

    @Override
    public void takeShare() {
        if (getTransmitter().hasTransmitterNetwork()) {
            GasNetwork transmitterNetwork = getTransmitter().getTransmitterNetwork();
            if (!transmitterNetwork.buffer.isEmpty() && !lastWrite.isEmpty()) {
                transmitterNetwork.buffer.shrink(lastWrite.getAmount());
                buffer.setGas(lastWrite);
            }
        }
    }

    @Override
    public int receiveGas(Direction side, @Nonnull GasStack stack, boolean doTransfer) {
        if (getConnectionType(side) == ConnectionType.NORMAL || getConnectionType(side) == ConnectionType.PULL) {
            return takeGas(stack, doTransfer);
        }
        return 0;
    }

    @Nonnull
    @Override
    public GasStack drawGas(Direction side, int amount, boolean doTransfer) {
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

    public int takeGas(GasStack gasStack, boolean doEmit) {
        if (getTransmitter().hasTransmitterNetwork()) {
            return getTransmitter().getTransmitterNetwork().emit(gasStack, doEmit);
        }
        return buffer.receive(gasStack, doEmit);
    }

    @Nonnull
    @Override
    public GasTankInfo[] getTankInfo() {
        if (getTransmitter().hasTransmitterNetwork()) {
            GasNetwork network = getTransmitter().getTransmitterNetwork();
            GasTank networkTank = new GasTank(network.getCapacity());
            networkTank.setGas(network.getBuffer());
            return new GasTankInfo[]{networkTank};
        }
        return new GasTankInfo[]{buffer};
    }

    @Override
    public IGasHandler getCachedAcceptor(Direction side) {
        return CapabilityUtils.getCapabilityHelper(getCachedTile(side), Capabilities.GAS_HANDLER_CAPABILITY, side.getOpposite()).getValue();
    }

    @Override
    public boolean upgrade(int tierOrdinal) {
        if (tier.ordinal() < BaseTier.ULTIMATE.ordinal() && tierOrdinal == tier.ordinal() + 1) {
            tier = TubeTier.values()[tier.ordinal() + 1];
            markDirtyTransmitters();
            sendDesc = true;
            return true;
        }
        return false;
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) throws Exception {
        tier = TubeTier.values()[dataStream.readInt()];
        super.handlePacketData(dataStream);
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        data.add(tier.ordinal());
        super.getNetworkedData(data);
        return data;
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