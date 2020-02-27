package mekanism.common.tile.transmitter;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.gas.BasicGasTank;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.IMekanismGasHandler;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.tier.AlloyTier;
import mekanism.api.tier.BaseTier;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.block.transmitter.BlockPressurizedTube;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.proxy.ProxyGasHandler;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tier.TubeTier;
import mekanism.common.transmitters.grid.GasNetwork;
import mekanism.common.upgrade.transmitter.PressurizedTubeUpgradeData;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.GasUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class TileEntityPressurizedTube extends TileEntityTransmitter<IGasHandler, GasNetwork, GasStack> implements IMekanismGasHandler {

    public final TubeTier tier;

    public float currentScale;

    @Nonnull
    private GasStack lastWrite = GasStack.EMPTY;
    private ProxyGasHandler readOnlyHandler;
    private Map<Direction, ProxyGasHandler> gasHandlers;
    private List<IChemicalTank<Gas, GasStack>> tanks;
    public BasicGasTank buffer;

    public TileEntityPressurizedTube(IBlockProvider blockProvider) {
        super(((IHasTileEntity<TileEntityPressurizedTube>) blockProvider.getBlock()).getTileType());
        this.tier = ((BlockPressurizedTube) blockProvider.getBlock()).getTier();
        gasHandlers = new EnumMap<>(Direction.class);
        buffer = BasicGasTank.create(getCapacity(), gas -> false, gas -> true, this);
        tanks = Collections.singletonList(buffer);
    }

    /**
     * Lazily get and cache an IGasHandler instance for the given side, and make it be read only if something else is trying to interact with us using the null side
     */
    private IGasHandler getGasHandler(@Nullable Direction side) {
        if (side == null) {
            if (readOnlyHandler == null) {
                readOnlyHandler = new ProxyGasHandler(this, null);
            }
            return readOnlyHandler;
        }
        ProxyGasHandler gasHandler = gasHandlers.get(side);
        if (gasHandler == null) {
            gasHandlers.put(side, gasHandler = new ProxyGasHandler(this, side));
        }
        return gasHandler;
    }

    @Override
    public void tick() {
        if (!isRemote()) {
            updateShare();
            List<Direction> connections = getConnections(ConnectionType.PULL);
            if (!connections.isEmpty()) {
                IGasHandler[] connectedAcceptors = GasUtils.getConnectedAcceptors(getPos(), getWorld());
                for (Direction side : connections) {
                    IGasHandler container = connectedAcceptors[side.ordinal()];
                    if (container != null) {
                        //TODO: GasHandler - make this more like the mechanical pipe one in that it tries to use the type of gas we have stored
                        GasStack received = container.drain(getAvailablePull(), Action.SIMULATE);
                        if (!received.isEmpty() && takeGas(received, Action.SIMULATE) == received.getAmount()) {
                            container.drain(takeGas(received, Action.EXECUTE), Action.EXECUTE);
                        }
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
        return buffer.isEmpty() || otherBuffer.isEmpty() || buffer.isTypeEqual(otherBuffer);
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

    @Nonnull
    @Override
    public GasStack getStack() {
        return getBufferWithFallback();
    }

    @Override
    public int getCapacity() {
        return tier.getTubeCapacity();
    }

    @Override
    public int fill(@Nonnull GasStack stack, @Nonnull Action action) {
        if (getConnectionType(side) == ConnectionType.NORMAL || getConnectionType(side) == ConnectionType.PULL) {
            return takeGas(stack, action);
        }
        return 0;
    }

    @Nonnull
    @Override
    public GasStack getBuffer() {
        return buffer == null ? GasStack.EMPTY : buffer.getStack();
    }

    @Override
    public boolean noBufferOrFallback() {
        return getBufferWithFallback().isEmpty();
    }

    @Nonnull
    @Override
    public GasStack getBufferWithFallback() {
        GasStack buffer = getBuffer();
        //If we don't have a buffer try falling back to the network's buffer
        if (buffer.isEmpty() && getTransmitter().hasTransmitterNetwork()) {
            return getTransmitter().getTransmitterNetwork().getBuffer();
        }
        return buffer;
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

    public int takeGas(GasStack gasStack, Action action) {
        //TODO: GasHandler - inline this into fill?
        if (getTransmitter().hasTransmitterNetwork()) {
            return getTransmitter().getTransmitterNetwork().emit(gasStack, action);
        }
        return buffer.insert(gasStack, action);
    }

    @Nonnull
    @Override
    public GasTankInfo[] getTankInfo() {
        if (getTransmitter().hasTransmitterNetwork()) {
            GasNetwork network = getTransmitter().getTransmitterNetwork();
            BasicGasTank networkTank = new BasicGasTank(network.getCapacity());
            networkTank.setStack(network.getBuffer());
            return new GasTankInfo[]{networkTank};
        }
        return new GasTankInfo[]{buffer};
    }

    @Nonnull
    @Override
    public List<IChemicalTank<Gas, GasStack>> getGasTanks(@Nullable Direction side) {
        //TODO: Give access to network
        return tanks;
    }

    @Override
    public void onContentsChanged() {
        markDirty();
    }

    @Override
    public IGasHandler getCachedAcceptor(Direction side) {
        return MekanismUtils.toOptional(CapabilityUtils.getCapability(getCachedTile(side), Capabilities.GAS_HANDLER_CAPABILITY, side.getOpposite())).orElse(null);
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
            List<IChemicalTank<Gas, GasStack>> gasTanks = getGasTanks(side);
            //Don't return an item handler if we don't actually even have any slots for that side
            //TODO: Should we actually return the item handler regardless??? And then just everything fails?
            LazyOptional<IGasHandler> lazyGasHandler = gasTanks.isEmpty() ? LazyOptional.empty() : LazyOptional.of(() -> getGasHandler(side));
            return Capabilities.GAS_HANDLER_CAPABILITY.orEmpty(capability, lazyGasHandler);
        }
        return super.getCapability(capability, side);
    }
}