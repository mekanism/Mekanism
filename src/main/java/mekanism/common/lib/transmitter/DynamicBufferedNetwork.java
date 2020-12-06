package mekanism.common.lib.transmitter;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.LongConsumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Range3D;
import mekanism.common.content.network.transmitter.BufferedTransmitter;
import mekanism.common.util.WorldUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.eventbus.api.Event;

public abstract class DynamicBufferedNetwork<ACCEPTOR, NETWORK extends DynamicBufferedNetwork<ACCEPTOR, NETWORK, BUFFER, TRANSMITTER>, BUFFER,
      TRANSMITTER extends BufferedTransmitter<ACCEPTOR, NETWORK, BUFFER, TRANSMITTER>> extends DynamicNetwork<ACCEPTOR, NETWORK, TRANSMITTER> {

    protected final LongSet chunks = new LongOpenHashSet();
    @Nullable
    protected Range3D packetRange;
    protected long capacity;
    protected boolean needsUpdate;
    private boolean forceScaleUpdate;
    private long lastSaveShareWriteTime;
    private long lastMarkDirtyTime;
    public float currentScale;

    protected DynamicBufferedNetwork() {
    }

    protected DynamicBufferedNetwork(UUID networkID) {
        super(networkID);
    }

    protected abstract float computeContentScale();

    @Override
    public void onUpdate() {
        super.onUpdate();
        float scale = computeContentScale();
        if (scale != currentScale) {
            currentScale = scale;
            needsUpdate = true;
        }
    }

    @Override
    public void addNewTransmitters(Collection<TRANSMITTER> newTransmitters) {
        super.addNewTransmitters(newTransmitters);
        if (!forceScaleUpdate) {
            //If we currently have no transmitters, mark that we want to force our scale to update to the target after the initial adding
            forceScaleUpdate = isEmpty();
        }
    }

    @Override
    protected void addTransmitterFromCommit(TRANSMITTER transmitter) {
        //Update the capacity here, to make sure that we can actually absorb the buffer properly
        updateCapacity(transmitter);
        absorbBuffer(transmitter);
        super.addTransmitterFromCommit(transmitter);
        chunks.add(WorldUtils.getChunkPosAsLong(transmitter.getTilePos()));
    }

    @Override
    protected void validTransmittersAdded() {
        super.validTransmittersAdded();
        clampBuffer();
        if (forceScaleUpdate) {
            forceScaleUpdate = false;
            forceScaleUpdate();
        }
        needsUpdate = true;
        //Flush the cached packet range. Eventually we may want to improve how it is cached some
        packetRange = null;
    }

    @Override
    public List<TRANSMITTER> adoptTransmittersAndAcceptorsFrom(NETWORK net) {
        List<TRANSMITTER> transmittersToUpdate = super.adoptTransmittersAndAcceptorsFrom(net);
        //Update the capacity
        updateCapacity();
        return transmittersToUpdate;
    }

    @Override
    protected void removeInvalid(@Nullable TRANSMITTER triggerTransmitter) {
        super.removeInvalid(triggerTransmitter);
        //Clamp the new buffer
        clampBuffer();
        //Update all shares
        updateSaveShares(triggerTransmitter);
    }

    protected abstract void forceScaleUpdate();

    @Nonnull
    public abstract BUFFER getBuffer();

    public abstract void absorbBuffer(TRANSMITTER transmitter);

    public abstract void clampBuffer();

    public boolean compatibleWithBuffer(BUFFER buffer) {
        return true;
    }

    public boolean isCompatibleWith(NETWORK other) {
        return true;
    }

    /**
     * @param transmitter The transmitter that was added
     */
    protected synchronized void updateCapacity(TRANSMITTER transmitter) {
        long transmitterCapacity = transmitter.getCapacity();
        if (transmitterCapacity > Long.MAX_VALUE - capacity) {
            //Ensure we don't overflow
            capacity = Long.MAX_VALUE;
        } else {
            capacity += transmitterCapacity;
        }
    }

    public synchronized void updateCapacity() {
        long sum = 0;
        for (TRANSMITTER transmitter : transmitters) {
            long transmitterCapacity = transmitter.getCapacity();
            if (transmitterCapacity > Long.MAX_VALUE - capacity) {
                //Ensure we don't overflow
                sum = Long.MAX_VALUE;
                break;
            } else {
                sum += transmitterCapacity;
            }
        }
        if (capacity != sum) {
            capacity = sum;
        }
    }

    public long getCapacity() {
        return capacity;
    }

    @Override
    public Object getNetworkReaderCapacity() {
        return getCapacity();
    }

    protected void updateSaveShares(@Nullable TRANSMITTER triggerTransmitter) {
    }

    public final void validateSaveShares(@Nonnull TRANSMITTER triggerTransmitter) {
        if (world == null) {
            //If the world is null, try falling back to the trigger transmitter's world.
            // Note: This also in theory could be null so we double check it is not before grabbing the game time
            world = triggerTransmitter.getTileWorld();
        }
        if (world != null && world.getGameTime() != lastSaveShareWriteTime) {
            lastSaveShareWriteTime = world.getGameTime();
            updateSaveShares(triggerTransmitter);
        }
    }

    public void markDirty() {
        if (world != null && !world.isRemote && world.getGameTime() != lastMarkDirtyTime) {
            lastMarkDirtyTime = world.getGameTime();
            chunks.forEach((LongConsumer) chunk -> WorldUtils.markChunkDirty(world, WorldUtils.getBlockPosFromChunkPos(chunk)));
        }
    }

    public Range3D getPacketRange() {
        if (packetRange == null) {
            packetRange = genPacketRange();
        }
        return packetRange;
    }

    private Range3D genPacketRange() {
        if (isEmpty()) {
            deregister();
            return null;
        }
        boolean initialized = false;
        int minX = 0;
        int minZ = 0;
        int maxX = 0;
        int maxZ = 0;
        for (TRANSMITTER transmitter : transmitters) {
            BlockPos pos = transmitter.getTilePos();
            if (initialized) {
                if (pos.getX() < minX) {
                    minX = pos.getX();
                } else if (pos.getX() > maxX) {
                    maxX = pos.getX();
                }
                if (pos.getZ() < minZ) {
                    minZ = pos.getZ();
                } else if (pos.getZ() > maxZ) {
                    maxZ = pos.getZ();
                }
            } else {
                minX = pos.getX();
                minZ = pos.getZ();
                maxX = minX;
                maxZ = minZ;
                initialized = true;
            }
        }
        return new Range3D(minX, minZ, maxX, maxZ, world.getDimensionKey());
    }

    public static class TransferEvent<NETWORK extends DynamicBufferedNetwork<?, NETWORK, ?, ?>> extends Event {

        public final NETWORK network;

        public TransferEvent(NETWORK network) {
            this.network = network;
        }
    }
}