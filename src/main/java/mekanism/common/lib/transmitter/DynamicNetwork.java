package mekanism.common.lib.transmitter;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.Range3D;
import mekanism.api.text.IHasTextComponent;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.EnumUtils;
import net.minecraft.util.Direction;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.thread.EffectiveSide;

public abstract class DynamicNetwork<ACCEPTOR, NETWORK extends DynamicNetwork<ACCEPTOR, NETWORK, BUFFER>, BUFFER> implements INetworkDataHandler, IHasTextComponent {

    protected final Set<TileEntityTransmitter<ACCEPTOR, NETWORK, BUFFER>> transmitters = new ObjectLinkedOpenHashSet<>();
    protected final Set<TileEntityTransmitter<ACCEPTOR, NETWORK, BUFFER>> transmittersToAdd = new ObjectOpenHashSet<>();

    protected final Set<Coord4D> possibleAcceptors = new ObjectOpenHashSet<>();
    protected final Map<Coord4D, EnumSet<Direction>> acceptorDirections = new Object2ObjectOpenHashMap<>();
    protected final Map<TileEntityTransmitter<ACCEPTOR, NETWORK, BUFFER>, EnumSet<Direction>> changedAcceptors = new Object2ObjectOpenHashMap<>();
    protected Range3D packetRange = null;
    protected final Set<ChunkPos> chunks = new ObjectOpenHashSet<>();
    protected long capacity;
    protected boolean needsUpdate = false;
    @Nullable
    protected World world = null;
    private boolean forceScaleUpdate = false;
    private long lastSaveShareWriteTime;
    private long lastMarkDirtyTime;

    private final UUID uuid;

    public DynamicNetwork() {
        this(UUID.randomUUID());
    }

    public DynamicNetwork(UUID networkID) {
        this.uuid = networkID;
    }

    protected NETWORK getNetwork() {
        return (NETWORK) this;
    }

    public void addNewTransmitters(Collection<TileEntityTransmitter<ACCEPTOR, NETWORK, BUFFER>> newTransmitters) {
        transmittersToAdd.addAll(newTransmitters);
        if (!forceScaleUpdate) {
            //If we currently have no transmitters, mark that we want to force our scale to update to the target after the initial adding
            forceScaleUpdate = isEmpty();
        }
    }

    public void commit() {
        if (!transmittersToAdd.isEmpty()) {
            boolean addedValidTransmitters = false;
            for (TileEntityTransmitter<ACCEPTOR, NETWORK, BUFFER> transmitter : transmittersToAdd) {
                //Note: Transmitter should not be able to be null here, but I ran into a null pointer
                // pointing to it being null that I could not reproduce, so just added this as a safety check
                if (transmitter != null && transmitter.isValid()) {
                    addedValidTransmitters = true;
                    if (world == null) {
                        world = transmitter.getWorld();
                    }

                    for (Direction side : EnumUtils.DIRECTIONS) {
                        updateTransmitterOnSide(transmitter, side);
                    }

                    transmitter.setTransmitterNetwork(getNetwork());
                    //Update the capacity here, to make sure that we can actually absorb the buffer properly
                    updateCapacity(transmitter);
                    absorbBuffer(transmitter);
                    transmitters.add(transmitter);
                    chunks.add(new ChunkPos(transmitter.getPos()));
                }
            }
            transmittersToAdd.clear();
            if (addedValidTransmitters) {
                clampBuffer();
                if (forceScaleUpdate) {
                    forceScaleUpdate = false;
                    forceScaleUpdate();
                }
                needsUpdate = true;
            }
        }

        if (!changedAcceptors.isEmpty()) {
            for (Entry<TileEntityTransmitter<ACCEPTOR, NETWORK, BUFFER>, EnumSet<Direction>> entry : changedAcceptors.entrySet()) {
                TileEntityTransmitter<ACCEPTOR, NETWORK, BUFFER> transmitter = entry.getKey();
                if (transmitter.isValid()) {
                    //Update all the changed directions
                    for (Direction side : entry.getValue()) {
                        updateTransmitterOnSide(transmitter, side);
                    }
                }
            }
            changedAcceptors.clear();
        }
    }

    public void updateTransmitterOnSide(TileEntityTransmitter<ACCEPTOR, NETWORK, BUFFER> transmitter, Direction side) {
        ACCEPTOR acceptor = transmitter.getAcceptor(side);
        Coord4D acceptorCoord = transmitter.coord().offset(side);
        Set<Direction> directions = acceptorDirections.get(acceptorCoord);

        if (acceptor != null) {
            possibleAcceptors.add(acceptorCoord);
            if (directions != null) {
                directions.add(side.getOpposite());
            } else {
                acceptorDirections.put(acceptorCoord, EnumSet.of(side.getOpposite()));
            }
        } else if (directions != null) {
            directions.remove(side.getOpposite());

            if (directions.isEmpty()) {
                possibleAcceptors.remove(acceptorCoord);
                acceptorDirections.remove(acceptorCoord);
            }
        } else {
            possibleAcceptors.remove(acceptorCoord);
            acceptorDirections.remove(acceptorCoord);
        }
    }

    @Nullable
    public BUFFER getBuffer() {
        return null;
    }

    public boolean isRemote() {
        return world == null ? EffectiveSide.get().isClient() : world.isRemote;
    }

    public abstract void absorbBuffer(TileEntityTransmitter<ACCEPTOR, NETWORK, BUFFER> transmitter);

    public abstract void clampBuffer();

    protected void forceScaleUpdate() {
    }

    protected void onLastTransmitterRemoved(@Nullable TileEntityTransmitter<?, ?, ?> triggerTransmitter) {
    }

    public void invalidate(@Nullable TileEntityTransmitter<?, ?, ?> triggerTransmitter) {
        if (transmitters.size() == 1 && triggerTransmitter != null) {
            //We're destroying the last transmitter in the network
            onLastTransmitterRemoved(triggerTransmitter);
        }

        //Remove invalid transmitters first for share calculations
        transmitters.removeIf(transmitter -> !transmitter.isValid());

        //Clamp the new buffer
        clampBuffer();

        //Update all shares
        updateSaveShares(triggerTransmitter);

        //Now invalidate the transmitters
        for (TileEntityTransmitter<ACCEPTOR, NETWORK, BUFFER> transmitter : transmitters) {
            invalidateTransmitter(transmitter);
        }

        transmitters.clear();
        deregister();
    }

    public void invalidateTransmitter(TileEntityTransmitter<ACCEPTOR, NETWORK, BUFFER> transmitter) {
        if (!isRemote() && transmitter.isValid()) {
            transmitter.takeShare();
            transmitter.setTransmitterNetwork(null);
            TransmitterNetworkRegistry.registerOrphanTransmitter(transmitter);
        }
    }

    public void acceptorChanged(TileEntityTransmitter<ACCEPTOR, NETWORK, BUFFER> transmitter, Direction side) {
        EnumSet<Direction> directions = changedAcceptors.get(transmitter);
        if (directions == null) {
            changedAcceptors.put(transmitter, EnumSet.of(side));
        } else {
            directions.add(side);
        }
        TransmitterNetworkRegistry.registerChangedNetwork(this);
    }

    public void adoptTransmittersAndAcceptorsFrom(NETWORK net) {
        for (TileEntityTransmitter<ACCEPTOR, NETWORK, BUFFER> transmitter : net.transmitters) {
            transmitter.setTransmitterNetwork(getNetwork());
            transmitters.add(transmitter);
        }

        transmittersToAdd.addAll(net.transmittersToAdd);
        possibleAcceptors.addAll(net.possibleAcceptors);

        for (Entry<Coord4D, EnumSet<Direction>> entry : net.acceptorDirections.entrySet()) {
            Coord4D coord = entry.getKey();
            if (acceptorDirections.containsKey(coord)) {
                acceptorDirections.get(coord).addAll(entry.getValue());
            } else {
                acceptorDirections.put(coord, entry.getValue());
            }
        }
        //Update the capacity
        updateCapacity();
    }

    public Range3D getPacketRange() {
        //TODO: FIXME? It never updates the value of packetRange
        return packetRange == null ? genPacketRange() : packetRange;
    }

    private Range3D genPacketRange() {
        if (isEmpty()) {
            deregister();
            return null;
        }
        TileEntityTransmitter<ACCEPTOR, NETWORK, BUFFER> initTransmitter = firstTransmitter();
        Coord4D initCoord = initTransmitter.coord();
        int minX = initCoord.x;
        int minZ = initCoord.z;
        int maxX = initCoord.x;
        int maxZ = initCoord.z;
        for (TileEntityTransmitter<ACCEPTOR, NETWORK, BUFFER> transmitter : transmitters) {
            Coord4D coord = transmitter.coord();
            if (coord.x < minX) {
                minX = coord.x;
            } else if (coord.x > maxX) {
                maxX = coord.x;
            }
            if (coord.z < minZ) {
                minZ = coord.z;
            } else if (coord.z > maxZ) {
                maxZ = coord.z;
            }
        }
        return new Range3D(minX, minZ, maxX, maxZ, initTransmitter.getWorld().getDimension().getType());
    }

    public void register() {
        if (isRemote()) {
            TransmitterNetworkRegistry.getInstance().addClientNetwork(getUUID(), this);
        } else {
            TransmitterNetworkRegistry.getInstance().registerNetwork(this);
        }
    }

    public void deregister() {
        transmitters.clear();
        transmittersToAdd.clear();
        if (isRemote()) {
            TransmitterNetworkRegistry.getInstance().removeClientNetwork(this);
        } else {
            TransmitterNetworkRegistry.getInstance().removeNetwork(this);
        }
    }

    public boolean isEmpty() {
        return transmitters.isEmpty();
    }

    public int getAcceptorSize() {
        return possibleAcceptors.size();
    }

    /**
     * @param transmitter The transmitter that was added
     */
    protected synchronized void updateCapacity(TileEntityTransmitter<ACCEPTOR, NETWORK, BUFFER> transmitter) {
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
        for (TileEntityTransmitter<ACCEPTOR, NETWORK, BUFFER> transmitter : transmitters) {
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

    @Nullable
    public World getWorld() {
        return world;
    }

    public void tick() {
        onUpdate();
    }

    public void onUpdate() {}

    protected void updateSaveShares(@Nullable TileEntityTransmitter<?, ?, ?> triggerTransmitter) {}

    public final void validateSaveShares(@Nullable TileEntityTransmitter<?, ?, ?> triggerTransmitter) {
        if (world.getGameTime() != lastSaveShareWriteTime) {
            lastSaveShareWriteTime = world.getGameTime();
            updateSaveShares(triggerTransmitter);
        }
    }

    public void markDirty() {
        if (world != null && !world.isRemote && world.getGameTime() != lastMarkDirtyTime) {
            lastMarkDirtyTime = world.getGameTime();
            chunks.forEach(chunk -> world.markChunkDirty(chunk.asBlockPos(), null));
        }
    }

    public boolean isCompatibleWith(NETWORK other) {
        return true;
    }

    public boolean compatibleWithBuffer(BUFFER buffer) {
        return true;
    }

    public Set<TileEntityTransmitter<ACCEPTOR, NETWORK, BUFFER>> getTransmitters() {
        return transmitters;
    }

    public boolean addTransmitter(TileEntityTransmitter<ACCEPTOR, NETWORK, BUFFER> transmitter) {
        return transmitters.add(transmitter);
    }

    public boolean removeTransmitter(TileEntityTransmitter<ACCEPTOR, NETWORK, BUFFER> transmitter) {
        boolean removed = transmitters.remove(transmitter);
        if (transmitters.isEmpty()) {
            deregister();
        }
        return removed;
    }

    public TileEntityTransmitter<ACCEPTOR, NETWORK, BUFFER> firstTransmitter() {
        return transmitters.iterator().next();
    }

    public int transmittersSize() {
        return transmitters.size();
    }

    public Set<Coord4D> getPossibleAcceptors() {
        return possibleAcceptors;
    }

    public Map<Coord4D, EnumSet<Direction>> getAcceptorDirections() {
        return acceptorDirections;
    }

    public UUID getUUID() {
        return uuid;
    }
}