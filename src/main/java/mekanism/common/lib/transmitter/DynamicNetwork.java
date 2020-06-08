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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.text.IHasTextComponent;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.EnumUtils;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.thread.EffectiveSide;

public abstract class DynamicNetwork<ACCEPTOR, NETWORK extends DynamicNetwork<ACCEPTOR, NETWORK, TRANSMITTER>,
      TRANSMITTER extends TileEntityTransmitter<ACCEPTOR, NETWORK, TRANSMITTER>> implements INetworkDataHandler, IHasTextComponent {

    protected final Set<TRANSMITTER> transmitters = new ObjectLinkedOpenHashSet<>();
    protected final Set<TRANSMITTER> transmittersToAdd = new ObjectOpenHashSet<>();
    protected final Set<BlockPos> possibleAcceptors = new ObjectOpenHashSet<>();
    protected final Map<BlockPos, Set<Direction>> acceptorDirections = new Object2ObjectOpenHashMap<>();
    protected final Map<TRANSMITTER, Set<Direction>> changedAcceptors = new Object2ObjectOpenHashMap<>();
    @Nullable
    protected World world;
    private final UUID uuid;

    protected DynamicNetwork() {
        this(UUID.randomUUID());
    }

    protected DynamicNetwork(UUID networkID) {
        this.uuid = networkID;
    }

    public UUID getUUID() {
        return uuid;
    }

    protected NETWORK getNetwork() {
        return (NETWORK) this;
    }

    public void addNewTransmitters(Collection<TRANSMITTER> newTransmitters) {
        transmittersToAdd.addAll(newTransmitters);
    }

    public void commit() {
        if (!transmittersToAdd.isEmpty()) {
            boolean addedValidTransmitters = false;
            for (TRANSMITTER transmitter : transmittersToAdd) {
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
                    addTransmitterFromCommit(transmitter);
                }
            }
            transmittersToAdd.clear();
            if (addedValidTransmitters) {
                validTransmittersAdded();
            }
        }

        if (!changedAcceptors.isEmpty()) {
            for (Entry<TRANSMITTER, Set<Direction>> entry : changedAcceptors.entrySet()) {
                TRANSMITTER transmitter = entry.getKey();
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

    protected void addTransmitterFromCommit(TRANSMITTER transmitter) {
        transmitters.add(transmitter);
    }

    protected void validTransmittersAdded() {
    }

    private void updateTransmitterOnSide(TRANSMITTER transmitter, Direction side) {
        ACCEPTOR acceptor = transmitter.getAcceptor(side);
        BlockPos acceptorCoord = transmitter.getPos().offset(side);
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

    public boolean isRemote() {
        return world == null ? EffectiveSide.get().isClient() : world.isRemote;
    }

    protected void onLastTransmitterRemoved(@Nonnull TRANSMITTER triggerTransmitter) {
    }

    public void invalidate(@Nullable TRANSMITTER triggerTransmitter) {
        if (transmitters.size() == 1 && triggerTransmitter != null) {
            //We're destroying the last transmitter in the network
            onLastTransmitterRemoved(triggerTransmitter);
        }
        removeInvalid(triggerTransmitter);
        //Now invalidate the transmitters
        if (!isRemote()) {
            for (TRANSMITTER transmitter : transmitters) {
                if (transmitter.isValid()) {
                    transmitter.takeShare();
                    transmitter.setTransmitterNetwork(null);
                    TransmitterNetworkRegistry.registerOrphanTransmitter(transmitter);
                }
            }
        }
        transmitters.clear();
        deregister();
    }

    protected void removeInvalid(@Nullable TRANSMITTER triggerTransmitter) {
        //Remove invalid transmitters first for share calculations
        transmitters.removeIf(transmitter -> !transmitter.isValid());
    }

    public void acceptorChanged(TRANSMITTER transmitter, Direction side) {
        Set<Direction> directions = changedAcceptors.get(transmitter);
        if (directions == null) {
            changedAcceptors.put(transmitter, EnumSet.of(side));
        } else {
            directions.add(side);
        }
        TransmitterNetworkRegistry.registerChangedNetwork(this);
    }

    public void adoptTransmittersAndAcceptorsFrom(NETWORK net) {
        for (TRANSMITTER transmitter : net.transmitters) {
            transmitter.setTransmitterNetwork(getNetwork());
            transmitters.add(transmitter);
        }

        transmittersToAdd.addAll(net.transmittersToAdd);
        possibleAcceptors.addAll(net.possibleAcceptors);

        for (Entry<BlockPos, Set<Direction>> entry : net.acceptorDirections.entrySet()) {
            BlockPos pos = entry.getKey();
            if (acceptorDirections.containsKey(pos)) {
                acceptorDirections.get(pos).addAll(entry.getValue());
            } else {
                acceptorDirections.put(pos, entry.getValue());
            }
        }
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

    @Nullable
    public World getWorld() {
        return world;
    }

    /**
     * @apiNote Only called on the server
     */
    public void onUpdate() {
    }

    public Set<TRANSMITTER> getTransmitters() {
        return transmitters;
    }

    public void addTransmitter(TRANSMITTER transmitter) {
        transmitters.add(transmitter);
    }

    public void removeTransmitter(TRANSMITTER transmitter) {
        transmitters.remove(transmitter);
        if (transmitters.isEmpty()) {
            deregister();
        }
    }

    public int transmittersSize() {
        return transmitters.size();
    }

    public boolean hasAcceptor(BlockPos acceptorPos) {
        return possibleAcceptors.contains(acceptorPos);
    }

    public Set<Direction> getAcceptorDirections(BlockPos pos) {
        return acceptorDirections.get(pos);
    }
}