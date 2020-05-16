package mekanism.api.transmitters;

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
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.thread.EffectiveSide;

public abstract class DynamicNetwork<ACCEPTOR, NETWORK extends DynamicNetwork<ACCEPTOR, NETWORK, BUFFER>, BUFFER> implements INetworkDataHandler, IHasTextComponent {

    /**
     * Cached value of {@link Direction#values()}. DO NOT MODIFY THIS LIST.
     */
    private static final Direction[] DIRECTIONS = Direction.values();

    protected Set<IGridTransmitter<ACCEPTOR, NETWORK, BUFFER>> transmitters = new ObjectLinkedOpenHashSet<>();
    protected Set<IGridTransmitter<ACCEPTOR, NETWORK, BUFFER>> transmittersToAdd = new ObjectOpenHashSet<>();

    protected Set<Coord4D> possibleAcceptors = new ObjectOpenHashSet<>();
    protected Map<Coord4D, EnumSet<Direction>> acceptorDirections = new Object2ObjectOpenHashMap<>();
    protected Map<IGridTransmitter<ACCEPTOR, NETWORK, BUFFER>, EnumSet<Direction>> changedAcceptors = new Object2ObjectOpenHashMap<>();
    protected Range3D packetRange = null;
    protected long capacity;
    protected boolean needsUpdate = false;
    protected boolean updateSaveShares;
    @Nullable
    protected World world = null;
    private boolean forceScaleUpdate = false;

    private final UUID uuid;

    public DynamicNetwork() {
        this(UUID.randomUUID());
    }

    public DynamicNetwork(UUID networkID) {
        this.uuid = networkID;
    }

    public void addNewTransmitters(Collection<IGridTransmitter<ACCEPTOR, NETWORK, BUFFER>> newTransmitters) {
        transmittersToAdd.addAll(newTransmitters);
        if (!forceScaleUpdate) {
            //If we currently have no transmitters, mark that we want to force our scale to update to the target after the initial adding
            forceScaleUpdate = isEmpty();
        }
    }

    public void commit() {
        if (!transmittersToAdd.isEmpty()) {
            boolean addedValidTransmitters = false;
            for (IGridTransmitter<ACCEPTOR, NETWORK, BUFFER> transmitter : transmittersToAdd) {
                //Note: Transmitter should not be able to be null here, but I ran into a null pointer
                // pointing to it being null that I could not reproduce, so just added this as a safety check
                if (transmitter != null && transmitter.isValid()) {
                    addedValidTransmitters = true;
                    if (world == null) {
                        world = transmitter.world();
                    }

                    for (Direction side : DIRECTIONS) {
                        updateTransmitterOnSide(transmitter, side);
                    }

                    transmitter.setTransmitterNetwork((NETWORK) this);
                    //Update the capacity here, to make sure that we can actually absorb the buffer properly
                    updateCapacity(transmitter);
                    absorbBuffer(transmitter);
                    transmitters.add(transmitter);
                }
            }
            transmittersToAdd.clear();
            if (addedValidTransmitters) {
                clampBuffer();
                updateSaveShares = true;
                if (forceScaleUpdate) {
                    forceScaleUpdate = false;
                    forceScaleUpdate();
                }
                needsUpdate = true;
            }
        }

        if (!changedAcceptors.isEmpty()) {
            for (Entry<IGridTransmitter<ACCEPTOR, NETWORK, BUFFER>, EnumSet<Direction>> entry : changedAcceptors.entrySet()) {
                IGridTransmitter<ACCEPTOR, NETWORK, BUFFER> transmitter = entry.getKey();
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

    public void updateTransmitterOnSide(IGridTransmitter<ACCEPTOR, NETWORK, BUFFER> transmitter, Direction side) {
        ACCEPTOR acceptor = transmitter.getAcceptor(side);
        Coord4D acceptorCoord = transmitter.coord().offset(side);
        EnumSet<Direction> directions = acceptorDirections.get(acceptorCoord);

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

    public abstract void absorbBuffer(IGridTransmitter<ACCEPTOR, NETWORK, BUFFER> transmitter);

    public abstract void clampBuffer();

    protected void forceScaleUpdate() {
    }

    protected void onLastTransmitterRemoved(@Nullable IGridTransmitter<?, ?, ?> triggerTransmitter) {
    }

    public void invalidate(@Nullable IGridTransmitter<?, ?, ?> triggerTransmitter) {
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
        for (IGridTransmitter<ACCEPTOR, NETWORK, BUFFER> transmitter : transmitters) {
            invalidateTransmitter(transmitter);
        }

        transmitters.clear();
        deregister();
    }

    public void invalidateTransmitter(IGridTransmitter<ACCEPTOR, NETWORK, BUFFER> transmitter) {
        if (!isRemote() && transmitter.isValid()) {
            transmitter.takeShare();
            transmitter.setTransmitterNetwork(null);
            TransmitterNetworkRegistry.registerOrphanTransmitter(transmitter);
        }
    }

    public void acceptorChanged(IGridTransmitter<ACCEPTOR, NETWORK, BUFFER> transmitter, Direction side) {
        EnumSet<Direction> directions = changedAcceptors.get(transmitter);
        if (directions == null) {
            changedAcceptors.put(transmitter, EnumSet.of(side));
        } else {
            directions.add(side);
        }
        TransmitterNetworkRegistry.registerChangedNetwork(this);
    }

    public void adoptTransmittersAndAcceptorsFrom(NETWORK net) {
        for (IGridTransmitter<ACCEPTOR, NETWORK, BUFFER> transmitter : net.transmitters) {
            transmitter.setTransmitterNetwork((NETWORK) this);
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
        return packetRange == null ? genPacketRange() : packetRange;
    }

    private Range3D genPacketRange() {
        if (isEmpty()) {
            deregister();
            return null;
        }
        IGridTransmitter<ACCEPTOR, NETWORK, BUFFER> initTransmitter = firstTransmitter();
        Coord4D initCoord = initTransmitter.coord();
        int minX = initCoord.x;
        int minZ = initCoord.z;
        int maxX = initCoord.x;
        int maxZ = initCoord.z;
        for (IGridTransmitter<ACCEPTOR, NETWORK, BUFFER> transmitter : transmitters) {
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
        return new Range3D(minX, minZ, maxX, maxZ, initTransmitter.world().getDimension().getType());
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
    protected synchronized void updateCapacity(IGridTransmitter<ACCEPTOR, NETWORK, BUFFER> transmitter) {
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
        for (IGridTransmitter<ACCEPTOR, NETWORK, BUFFER> transmitter : transmitters) {
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
            updateSaveShares = true;
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

    public void onUpdate() {
        if (!isRemote()) {
            if (updateSaveShares) {
                //Update the save shares
                updateSaveShares(null);
            }
        }
    }

    protected void updateSaveShares(@Nullable IGridTransmitter<?, ?, ?> triggerTransmitter) {
        updateSaveShares = false;
    }

    public boolean isCompatibleWith(NETWORK other) {
        return true;
    }

    public boolean compatibleWithBuffer(BUFFER buffer) {
        return true;
    }

    public Set<IGridTransmitter<ACCEPTOR, NETWORK, BUFFER>> getTransmitters() {
        return transmitters;
    }

    public boolean addTransmitter(IGridTransmitter<ACCEPTOR, NETWORK, BUFFER> transmitter) {
        return transmitters.add(transmitter);
    }

    public boolean removeTransmitter(IGridTransmitter<ACCEPTOR, NETWORK, BUFFER> transmitter) {
        boolean removed = transmitters.remove(transmitter);
        if (transmitters.isEmpty()) {
            deregister();
        }
        return removed;
    }

    public IGridTransmitter<ACCEPTOR, NETWORK, BUFFER> firstTransmitter() {
        return transmitters.iterator().next();
    }

    public int transmittersSize() {
        return transmitters.size();
    }

    public Set<IGridTransmitter<ACCEPTOR, NETWORK, BUFFER>> getTransmittersToAdd() {
        return transmittersToAdd;
    }

    public Set<Coord4D> getPossibleAcceptors() {
        return possibleAcceptors;
    }

    public Map<Coord4D, EnumSet<Direction>> getAcceptorDirections() {
        return acceptorDirections;
    }

    public Map<IGridTransmitter<ACCEPTOR, NETWORK, BUFFER>, EnumSet<Direction>> getChangedAcceptors() {
        return changedAcceptors;
    }

    public UUID getUUID() {
        return uuid;
    }
}