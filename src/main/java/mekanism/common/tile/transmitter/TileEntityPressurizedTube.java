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
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IGasHandler.IMekanismGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.inventory.AutomationType;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.tier.AlloyTier;
import mekanism.api.tier.BaseTier;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.proxy.ProxyChemicalHandler.ProxyGasHandler;
import mekanism.common.capabilities.resolver.advanced.AdvancedCapabilityResolver;
import mekanism.common.content.transmitter.GasNetwork;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tier.TubeTier;
import mekanism.common.upgrade.transmitter.PressurizedTubeUpgradeData;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;

//TODO - V10: Figure out how to make this work for multiple chemical types
public class TileEntityPressurizedTube extends TileEntityBufferedTransmitter<IGasHandler, GasNetwork, GasStack, TileEntityPressurizedTube> implements IMekanismGasHandler {

    public final TubeTier tier;
    @Nonnull
    public GasStack saveShare = GasStack.EMPTY;
    private final List<IGasTank> tanks;
    public final BasicGasTank buffer;

    public TileEntityPressurizedTube(IBlockProvider blockProvider) {
        super(blockProvider);
        this.tier = Attribute.getTier(blockProvider.getBlock(), TubeTier.class);
        buffer = BasicGasTank.create(getCapacity(), BasicGasTank.alwaysFalse, BasicGasTank.alwaysTrue, BasicGasTank.alwaysTrue, ChemicalAttributeValidator.ALWAYS_ALLOW, this);
        tanks = Collections.singletonList(buffer);
        addCapabilityResolver(AdvancedCapabilityResolver.readOnly(Capabilities.GAS_HANDLER_CAPABILITY, this, () -> new ProxyGasHandler(this, null, null)));
    }

    @Override
    public void tick() {
        if (!isRemote()) {
            Set<Direction> connections = getConnections(ConnectionType.PULL);
            if (!connections.isEmpty()) {
                for (IGasHandler connectedAcceptor : acceptorCache.getConnectedAcceptors(connections, Capabilities.GAS_HANDLER_CAPABILITY)) {
                    GasStack received;
                    //Note: We recheck the buffer each time in case we ended up accepting gas somewhere
                    // and our buffer changed and is no longer empty
                    GasStack bufferWithFallback = getBufferWithFallback();
                    if (bufferWithFallback.isEmpty()) {
                        //If we don't have a gas stored try pulling as much as we are able to
                        received = connectedAcceptor.extractChemical(getAvailablePull(), Action.SIMULATE);
                    } else {
                        //Otherwise try draining the same type of gas we have stored requesting up to as much as we are able to pull
                        // We do this to better support multiple tanks in case the gas we have stored we could pull out of a block's
                        // second tank but just asking to drain a specific amount
                        received = connectedAcceptor.extractChemical(new GasStack(bufferWithFallback, getAvailablePull()), Action.SIMULATE);
                    }
                    if (!received.isEmpty() && takeGas(received, Action.SIMULATE).isEmpty()) {
                        //If we received some gas and are able to insert it all
                        GasStack remainder = takeGas(received, Action.EXECUTE);
                        connectedAcceptor.extractChemical(new GasStack(received, received.getAmount() - remainder.getAmount()), Action.EXECUTE);
                    }
                }
            }
        }
        super.tick();
    }

    private long getAvailablePull() {
        if (hasTransmitterNetwork()) {
            return Math.min(tier.getTubePullAmount(), getTransmitterNetwork().gasTank.getNeeded());
        }
        return Math.min(tier.getTubePullAmount(), buffer.getNeeded());
    }

    @Nonnull
    @Override
    public GasStack insertChemical(int tank, @Nonnull GasStack stack, @Nullable Direction side, @Nonnull Action action) {
        IGasTank gasTank = getChemicalTank(tank, side);
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
    public void read(@Nonnull CompoundNBT nbtTags) {
        super.read(nbtTags);
        if (nbtTags.contains(NBTConstants.GAS_STORED, NBT.TAG_COMPOUND)) {
            saveShare = GasStack.readFromNBT(nbtTags.getCompound(NBTConstants.GAS_STORED));
        } else {
            saveShare = GasStack.EMPTY;
        }
        buffer.setStack(saveShare);
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT nbtTags) {
        super.write(nbtTags);
        if (hasTransmitterNetwork()) {
            getTransmitterNetwork().validateSaveShares(this);
        }
        if (saveShare.isEmpty()) {
            nbtTags.remove(NBTConstants.GAS_STORED);
        } else {
            nbtTags.put(NBTConstants.GAS_STORED, saveShare.write(new CompoundNBT()));
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
        return super.isValidAcceptor(tile, side) && acceptorCache.isAcceptorAndListen(tile, side, Capabilities.GAS_HANDLER_CAPABILITY);
    }

    @Override
    public boolean isValidTransmitter(TileEntityTransmitter<?, ?, ?> tile) {
        if (super.isValidTransmitter(tile) && tile instanceof TileEntityPressurizedTube) {
            Gas buffer = getBufferWithFallback().getType();
            if (buffer.isEmptyType() && hasTransmitterNetwork() && getTransmitterNetwork().getPrevTransferAmount() > 0) {
                buffer = getTransmitterNetwork().lastGas;
            }
            TileEntityPressurizedTube other = (TileEntityPressurizedTube) tile;
            Gas otherBuffer = other.getBufferWithFallback().getType();
            if (otherBuffer.isEmptyType() && other.hasTransmitterNetwork() && other.getTransmitterNetwork().getPrevTransferAmount() > 0) {
                otherBuffer = other.getTransmitterNetwork().lastGas;
            }
            return buffer.isEmptyType() || otherBuffer.isEmptyType() || buffer == otherBuffer;
        }
        return false;
    }

    @Override
    public GasNetwork createEmptyNetwork() {
        return new GasNetwork();
    }

    @Override
    public GasNetwork createEmptyNetworkWithID(UUID networkID) {
        return new GasNetwork(networkID);
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
    public long getCapacity() {
        return tier.getTubeCapacity();
    }

    @Nonnull
    @Override
    public GasStack releaseShare() {
        GasStack ret = buffer.getStack();
        buffer.setEmpty();
        return ret;
    }

    @Nonnull
    @Override
    public GasStack getShare() {
        return buffer.getStack();
    }

    @Override
    public boolean noBufferOrFallback() {
        return getBufferWithFallback().isEmpty();
    }

    @Nonnull
    @Override
    public GasStack getBufferWithFallback() {
        GasStack buffer = getShare();
        //If we don't have a buffer try falling back to the network's buffer
        if (buffer.isEmpty() && hasTransmitterNetwork()) {
            return getTransmitterNetwork().getBuffer();
        }
        return buffer;
    }

    @Override
    public void takeShare() {
        if (hasTransmitterNetwork()) {
            GasNetwork transmitterNetwork = getTransmitterNetwork();
            if (!transmitterNetwork.gasTank.isEmpty() && !saveShare.isEmpty()) {
                long amount = saveShare.getAmount();
                MekanismUtils.logMismatchedStackSize(transmitterNetwork.gasTank.shrinkStack(amount, Action.EXECUTE), amount);
                buffer.setStack(saveShare);
            }
        }
    }

    /**
     * @return remainder
     */
    @Nonnull
    private GasStack takeGas(GasStack gasStack, Action action) {
        if (hasTransmitterNetwork()) {
            return getTransmitterNetwork().gasTank.insert(gasStack, action, AutomationType.INTERNAL);
        }
        return buffer.insert(gasStack, action, AutomationType.INTERNAL);
    }

    @Nonnull
    @Override
    public List<IGasTank> getChemicalTanks(@Nullable Direction side) {
        if (hasTransmitterNetwork()) {
            return getTransmitterNetwork().getChemicalTanks(side);
        }
        return tanks;
    }

    @Override
    public void onContentsChanged() {
        markDirty(false);
    }

    @Nonnull
    @Override
    public LazyOptional<IGasHandler> getAcceptor(Direction side) {
        return acceptorCache.getCachedAcceptor(Capabilities.GAS_HANDLER_CAPABILITY, side);
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
        return new PressurizedTubeUpgradeData(redstoneReactive, connectionTypes, getShare());
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
    public CompoundNBT getUpdateTag() {
        //Note: We add the stored information to the initial update tag and not to the one we sync on side changes which uses getReducedUpdateTag
        CompoundNBT updateTag = super.getUpdateTag();
        if (hasTransmitterNetwork()) {
            updateTag.put(NBTConstants.GAS_STORED, getTransmitterNetwork().lastGas.write(new CompoundNBT()));
            updateTag.putFloat(NBTConstants.SCALE, getTransmitterNetwork().gasScale);
        }
        return updateTag;
    }

    @Override
    protected void handleContentsUpdateTag(@Nonnull GasNetwork network, @Nonnull CompoundNBT tag) {
        super.handleContentsUpdateTag(network, tag);
        NBTUtils.setGasIfPresent(tag, NBTConstants.GAS_STORED, network::setLastGas);
        NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE, scale -> network.gasScale = scale);
    }
}