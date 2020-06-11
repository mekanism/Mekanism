package mekanism.common.content.network.transmitter.chemical;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.api.inventory.AutomationType;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.content.network.chemical.ChemicalNetwork;
import mekanism.common.content.network.transmitter.BufferedTransmitter;
import mekanism.common.content.network.transmitter.Transmitter;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.tier.TubeTier;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.Constants.NBT;

public abstract class PressurizedTube<CHEMICAL extends Chemical<CHEMICAL>,
      STACK extends ChemicalStack<CHEMICAL>,
      HANDLER extends IChemicalHandler<CHEMICAL, STACK>,
      TANK extends IChemicalTank<CHEMICAL, STACK>,
      NETWORK extends ChemicalNetwork<CHEMICAL, STACK, HANDLER, TANK, NETWORK, TUBE>,
      TUBE extends PressurizedTube<CHEMICAL, STACK, HANDLER, TANK, NETWORK, TUBE>>
      extends BufferedTransmitter<HANDLER, NETWORK, STACK, TUBE> implements IMekanismChemicalHandler<CHEMICAL, STACK, TANK> {

    public final TubeTier tier;
    @Nonnull
    public STACK saveShare = getEmptyStack();
    private final List<TANK> tanks;
    public final TANK buffer;

    protected PressurizedTube(IBlockProvider blockProvider, TileEntityTransmitter tile) {
        super(tile);
        this.tier = Attribute.getTier(blockProvider.getBlock(), TubeTier.class);
        tanks = Collections.singletonList(buffer = initBuffer());
    }

    protected abstract TANK initBuffer();

    public TubeTier getTier() {
        return tier;
    }

    public abstract String getStoredKey();

    protected abstract STACK readFromNBT(@Nullable CompoundNBT nbtTags);

    @Override
    public void pullFromAcceptors() {
        Set<Direction> connections = getConnections(ConnectionType.PULL);
        if (!connections.isEmpty()) {
            for (HANDLER connectedAcceptor : acceptorCache.getConnectedAcceptors(connections)) {
                STACK received;
                //Note: We recheck the buffer each time in case we ended up accepting chemical somewhere
                // and our buffer changed and is no longer empty
                STACK bufferWithFallback = getBufferWithFallback();
                if (bufferWithFallback.isEmpty()) {
                    //If we don't have a chemical stored try pulling as much as we are able to
                    received = connectedAcceptor.extractChemical(getAvailablePull(), Action.SIMULATE);
                } else {
                    //Otherwise try draining the same type of chemical we have stored requesting up to as much as we are able to pull
                    // We do this to better support multiple tanks in case the chemical we have stored we could pull out of a block's
                    // second tank but just asking to drain a specific amount
                    received = connectedAcceptor.extractChemical(ChemicalUtil.copyWithAmount(bufferWithFallback, getAvailablePull()), Action.SIMULATE);
                }
                if (!received.isEmpty() && takeChemical(received, Action.SIMULATE).isEmpty()) {
                    //If we received some chemical and are able to insert it all
                    STACK remainder = takeChemical(received, Action.EXECUTE);
                    connectedAcceptor.extractChemical(ChemicalUtil.copyWithAmount(received, received.getAmount() - remainder.getAmount()), Action.EXECUTE);
                }
            }
        }
    }

    private long getAvailablePull() {
        if (hasTransmitterNetwork()) {
            return Math.min(tier.getTubePullAmount(), getTransmitterNetwork().tank.getNeeded());
        }
        return Math.min(tier.getTubePullAmount(), buffer.getNeeded());
    }

    @Nonnull
    @Override
    public STACK insertChemical(int tank, @Nonnull STACK stack, @Nullable Direction side, @Nonnull Action action) {
        TANK chemicalTank = getChemicalTank(tank, side);
        if (chemicalTank == null) {
            return stack;
        } else if (side == null) {
            return chemicalTank.insert(stack, action, AutomationType.INTERNAL);
        }
        //If we have a side only allow inserting if our connection allows it
        ConnectionType connectionType = getConnectionType(side);
        if (connectionType == ConnectionType.NORMAL || connectionType == ConnectionType.PULL) {
            return chemicalTank.insert(stack, action, AutomationType.EXTERNAL);
        }
        return stack;
    }

    @Override
    public void read(@Nonnull CompoundNBT nbtTags) {
        super.read(nbtTags);
        if (nbtTags.contains(getStoredKey(), NBT.TAG_COMPOUND)) {
            saveShare = readFromNBT(nbtTags.getCompound(getStoredKey()));
        } else {
            saveShare = getEmptyStack();
        }
        buffer.setStack(saveShare);
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT nbtTags) {
        super.write(nbtTags);
        if (hasTransmitterNetwork()) {
            getTransmitterNetwork().validateSaveShares(getTransmitter());
        }
        if (saveShare.isEmpty()) {
            nbtTags.remove(getStoredKey());
        } else {
            nbtTags.put(getStoredKey(), saveShare.write(new CompoundNBT()));
        }
        return nbtTags;
    }

    @Override
    public boolean isValidAcceptor(TileEntity tile, Direction side) {
        return super.isValidAcceptor(tile, side) && acceptorCache.isAcceptorAndListen(tile, side, ChemicalUtil.getCapabilityForChemical(getEmptyStack()));
    }

    @Override
    public boolean isValidTransmitter(Transmitter<?, ?, ?> transmitter) {
        if (super.isValidTransmitter(transmitter) && transmitter instanceof PressurizedTube && isTubeSameType((PressurizedTube<?, ?, ?, ?, ?, ?>) transmitter)) {
            CHEMICAL buffer = getBufferWithFallback().getType();
            if (buffer.isEmptyType() && hasTransmitterNetwork() && getTransmitterNetwork().getPrevTransferAmount() > 0) {
                buffer = getTransmitterNetwork().lastChemical;
            }
            PressurizedTube<CHEMICAL, ?, ?, ?, ?, ?> other = (PressurizedTube<CHEMICAL, ?, ?, ?, ?, ?>) transmitter;
            CHEMICAL otherBuffer = other.getBufferWithFallback().getType();
            if (otherBuffer.isEmptyType() && other.hasTransmitterNetwork() && other.getTransmitterNetwork().getPrevTransferAmount() > 0) {
                otherBuffer = other.getTransmitterNetwork().lastChemical;
            }
            return buffer.isEmptyType() || otherBuffer.isEmptyType() || buffer == otherBuffer;
        }
        return false;
    }

    protected abstract boolean isTubeSameType(PressurizedTube<?, ?, ?, ?, ?, ?> tube);

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
    public STACK releaseShare() {
        STACK ret = buffer.getStack();
        buffer.setEmpty();
        return ret;
    }

    @Nonnull
    @Override
    public STACK getShare() {
        return buffer.getStack();
    }

    @Override
    public boolean noBufferOrFallback() {
        return getBufferWithFallback().isEmpty();
    }

    @Nonnull
    @Override
    public STACK getBufferWithFallback() {
        STACK buffer = getShare();
        //If we don't have a buffer try falling back to the network's buffer
        if (buffer.isEmpty() && hasTransmitterNetwork()) {
            return getTransmitterNetwork().getBuffer();
        }
        return buffer;
    }

    @Override
    public void takeShare() {
        if (hasTransmitterNetwork()) {
            NETWORK transmitterNetwork = getTransmitterNetwork();
            if (!transmitterNetwork.tank.isEmpty() && !saveShare.isEmpty()) {
                long amount = saveShare.getAmount();
                MekanismUtils.logMismatchedStackSize(transmitterNetwork.tank.shrinkStack(amount, Action.EXECUTE), amount);
                buffer.setStack(saveShare);
            }
        }
    }

    /**
     * @return remainder
     */
    @Nonnull
    public STACK takeChemical(STACK stack, Action action) {
        if (hasTransmitterNetwork()) {
            return getTransmitterNetwork().tank.insert(stack, action, AutomationType.INTERNAL);
        }
        return buffer.insert(stack, action, AutomationType.INTERNAL);
    }

    @Nonnull
    @Override
    public List<TANK> getChemicalTanks(@Nullable Direction side) {
        if (hasTransmitterNetwork()) {
            return getTransmitterNetwork().getChemicalTanks(side);
        }
        return tanks;
    }

    @Override
    public void onContentsChanged() {
        getTransmitterTile().markDirty(false);
    }

    @Override
    protected void handleContentsUpdateTag(@Nonnull NETWORK network, @Nonnull CompoundNBT tag) {
        super.handleContentsUpdateTag(network, tag);
        NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE, scale -> network.currentScale = scale);
    }
}