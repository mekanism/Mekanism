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
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IMekanismGasHandler;
import mekanism.api.inventory.AutomationType;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.tier.AlloyTier;
import mekanism.api.tier.BaseTier;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.TransmitterType;
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
import mekanism.common.util.NBTUtils;
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
    private final Map<Direction, ProxyGasHandler> gasHandlers;
    private final List<IChemicalTank<Gas, GasStack>> tanks;
    public BasicGasTank buffer;

    public TileEntityPressurizedTube(IBlockProvider blockProvider) {
        super(((IHasTileEntity<TileEntityPressurizedTube>) blockProvider.getBlock()).getTileType());
        this.tier = Attribute.getTier(blockProvider.getBlock(), TubeTier.class);
        gasHandlers = new EnumMap<>(Direction.class);
        buffer = BasicGasTank.create(getCapacity(), BasicGasTank.alwaysFalse, BasicGasTank.alwaysTrue, this);
        tanks = Collections.singletonList(buffer);
    }

    /**
     * Lazily get and cache an IGasHandler instance for the given side, and make it be read only if something else is trying to interact with us using the null side
     */
    private IGasHandler getGasHandler(@Nullable Direction side) {
        if (side == null) {
            if (readOnlyHandler == null) {
                readOnlyHandler = new ProxyGasHandler(this, null, null);
            }
            return readOnlyHandler;
        }
        ProxyGasHandler gasHandler = gasHandlers.get(side);
        if (gasHandler == null) {
            gasHandlers.put(side, gasHandler = new ProxyGasHandler(this, side, null));
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
                        GasStack received;
                        //Note: We recheck the buffer each time in case we ended up accepting gas somewhere
                        // and our buffer changed and is no longer empty
                        GasStack bufferWithFallback = getBufferWithFallback();
                        if (bufferWithFallback.isEmpty()) {
                            //If we don't have a gas stored try pulling as much as we are able to
                            received = container.extractGas(getAvailablePull(), Action.SIMULATE);
                        } else {
                            //Otherwise try draining the same type of gas we have stored requesting up to as much as we are able to pull
                            // We do this to better support multiple tanks in case the gas we have stored we could pull out of a block's
                            // second tank but just asking to drain a specific amount
                            received = container.extractGas(new GasStack(bufferWithFallback, getAvailablePull()), Action.SIMULATE);
                        }
                        if (!received.isEmpty() && takeGas(received, Action.SIMULATE).isEmpty()) {
                            //If we received some gas and are able to insert it all
                            GasStack remainder = takeGas(received, Action.EXECUTE);
                            container.extractGas(new GasStack(received, received.getAmount() - remainder.getAmount()), Action.EXECUTE);
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

    private int getAvailablePull() {
        if (getTransmitter().hasTransmitterNetwork()) {
            return Math.min(tier.getTubePullAmount(), getTransmitter().getTransmitterNetwork().gasTank.getNeeded());
        }
        return Math.min(tier.getTubePullAmount(), buffer.getNeeded());
    }

    @Nonnull
    @Override
    public GasStack insertGas(int tank, @Nonnull GasStack stack, @Nullable Direction side, @Nonnull Action action) {
        IChemicalTank<Gas, GasStack> gasTank = getGasTank(tank, side);
        if (gasTank == null) {
            return stack;
        } else if (side == null) {
            return gasTank.insert(stack, action, AutomationType.INTERNAL);
        }
        //If we have a side only allow inserting if our connection allows it
        ConnectionType connectionType = getConnectionType(side);
        if (connectionType == ConnectionType.NORMAL || connectionType == ConnectionType.PULL) {
            return gasTank.insert(stack, action, AutomationType.EXTERNAL);
        }
        return stack;
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
            if (!transmitterNetwork.gasTank.isEmpty()) {
                int remain = transmitterNetwork.gasTank.getStored() % transmitterNetwork.transmittersSize();
                int toSave = transmitterNetwork.gasTank.getStored() / transmitterNetwork.transmittersSize();
                if (transmitterNetwork.firstTransmitter().equals(getTransmitter())) {
                    toSave += remain;
                }
                return new GasStack(transmitterNetwork.getBuffer(), toSave);
            }
        }
        return GasStack.EMPTY;
    }

    @Override
    public void onChunkUnloaded() {
        if (!isRemote() && getTransmitter().hasTransmitterNetwork()) {
            GasNetwork transmitterNetwork = getTransmitter().getTransmitterNetwork();
            if (!transmitterNetwork.gasTank.isEmpty() && !lastWrite.isEmpty()) {
                int amount = lastWrite.getAmount();
                if (transmitterNetwork.gasTank.shrinkStack(amount, Action.EXECUTE) != amount) {
                    //TODO: Print warning/error
                }
            }
        }
        super.onChunkUnloaded();
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        NBTUtils.setGasStackIfPresent(nbtTags, NBTConstants.GAS_STORED, buffer::setStack);
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        if (lastWrite.isEmpty()) {
            nbtTags.remove(NBTConstants.GAS_STORED);
        } else {
            nbtTags.put(NBTConstants.GAS_STORED, lastWrite.write(new CompoundNBT()));
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
            if (!transmitterNetwork.gasTank.isEmpty() && !lastWrite.isEmpty()) {
                int amount = lastWrite.getAmount();
                if (transmitterNetwork.gasTank.shrinkStack(amount, Action.EXECUTE) != amount) {
                    //TODO: Print warning/error
                }
                buffer.setStack(lastWrite);
            }
        }
    }

    /**
     * @return remainder
     */
    @Nonnull
    private GasStack takeGas(GasStack gasStack, Action action) {
        if (getTransmitter().hasTransmitterNetwork()) {
            return getTransmitter().getTransmitterNetwork().emit(gasStack, action);
        }
        return buffer.insert(gasStack, action, AutomationType.INTERNAL);
    }

    @Nonnull
    @Override
    public List<? extends IChemicalTank<Gas, GasStack>> getGasTanks(@Nullable Direction side) {
        if (getTransmitter().hasTransmitterNetwork()) {
            //TODO: Do we want this to fallback to local if the one on the network is empty?
            return getTransmitter().getTransmitterNetwork().getGasTanks(side);
        }
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
            List<? extends IChemicalTank<Gas, GasStack>> gasTanks = getGasTanks(side);
            //Don't return a gas handler if we don't actually even have any tanks for that side
            LazyOptional<IGasHandler> lazyGasHandler = gasTanks.isEmpty() ? LazyOptional.empty() : LazyOptional.of(() -> getGasHandler(side));
            return Capabilities.GAS_HANDLER_CAPABILITY.orEmpty(capability, lazyGasHandler);
        }
        return super.getCapability(capability, side);
    }
}