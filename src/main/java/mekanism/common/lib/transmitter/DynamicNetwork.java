package mekanism.common.lib.transmitter;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import mekanism.api.text.IHasTextComponent;
import mekanism.common.Mekanism;
import mekanism.common.content.network.transmitter.Transmitter;
import mekanism.common.lib.transmitter.acceptor.NetworkAcceptorCache;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.fml.util.thread.EffectiveSide;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class DynamicNetwork<ACCEPTOR, NETWORK extends DynamicNetwork<ACCEPTOR, NETWORK, TRANSMITTER>,
      TRANSMITTER extends Transmitter<ACCEPTOR, NETWORK, TRANSMITTER>> implements INetworkDataHandler, IHasTextComponent {

    protected final Long2ObjectMap<TRANSMITTER> positionedTransmitters = new Long2ObjectOpenHashMap<>();
    protected final Set<TRANSMITTER> transmittersToAdd = new ObjectOpenHashSet<>();
    protected final NetworkAcceptorCache<ACCEPTOR> acceptorCache = new NetworkAcceptorCache<>();
    @Nullable
    protected Level world;
    private final UUID uuid;
    @Nullable
    private CompatibleTransmitterValidator<ACCEPTOR, NETWORK, TRANSMITTER> transmitterValidator;

    protected DynamicNetwork(UUID networkID) {
        this.uuid = networkID;
    }

    public UUID getUUID() {
        return uuid;
    }

    @SuppressWarnings("unchecked")
    protected NETWORK getNetwork() {
        return (NETWORK) this;
    }

    public void commit() {
        if (!transmittersToAdd.isEmpty()) {
            boolean addedValidTransmitters = false;
            List<TRANSMITTER> transmittersToUpdate = new ArrayList<>();
            for (TRANSMITTER transmitter : transmittersToAdd) {
                //Note: Transmitter should not be able to be null here, but I ran into a null pointer
                // pointing to it being null that I could not reproduce, so just added this as a safety check
                if (transmitter != null && transmitter.isValid()) {
                    addedValidTransmitters = true;
                    if (world == null) {
                        world = transmitter.getLevel();
                    }
                    for (Direction side : EnumUtils.DIRECTIONS) {
                        acceptorCache.updateTransmitterOnSide(transmitter, side);
                    }
                    if (transmitter.setTransmitterNetwork(getNetwork(), false)) {
                        transmittersToUpdate.add(transmitter);
                    }
                    addTransmitterFromCommit(transmitter);
                }
            }
            transmittersToAdd.clear();
            if (addedValidTransmitters) {
                validTransmittersAdded();
                transmittersToUpdate.forEach(Transmitter::requestsUpdate);
            }
        }
        acceptorCache.commit();
        transmitterValidator = null;
    }

    @Nullable
    public CompatibleTransmitterValidator<ACCEPTOR, NETWORK, TRANSMITTER> getTransmitterValidator() {
        return transmitterValidator;
    }

    public void addNewTransmitters(Collection<TRANSMITTER> newTransmitters, CompatibleTransmitterValidator<ACCEPTOR, NETWORK, TRANSMITTER> transmitterValidator) {
        transmittersToAdd.addAll(newTransmitters);
        //Cache the transmitter validator in the network, so that if we have a case of orphans being on either side of
        // an existing network, and the orphans are what have contents stored, that then we don't try merging them all
        // together when they may not actually be able to have both sets of orphans connect. After the network is
        // updated (committed), this cached validator will be unset
        this.transmitterValidator = transmitterValidator;
    }

    @Nullable
    public TRANSMITTER getTransmitter(long pos) {
        return positionedTransmitters.get(pos);
    }

    @Nullable
    public TRANSMITTER getTransmitter(BlockPos pos) {
        return getTransmitter(pos.asLong());
    }

    protected void addTransmitterFromCommit(TRANSMITTER transmitter) {
        positionedTransmitters.put(transmitter.getBlockPos().asLong(), transmitter);
    }

    protected void validTransmittersAdded() {
    }

    public boolean isRemote() {
        return world == null ? EffectiveSide.get().isClient() : world.isClientSide;
    }

    public void invalidate(@Nullable TRANSMITTER triggerTransmitter) {
        if (transmittersSize() == 1 && triggerTransmitter != null && !triggerTransmitter.isValid()) {
            //We're destroying the last transmitter in the network
            //Note: We check it isn't valid to make sure we are destroying it and not just changing redstone sensitivity
            onLastTransmitterRemoved(triggerTransmitter);
        }
        removeInvalid(triggerTransmitter);
        //Now invalidate the transmitters
        if (!isRemote()) {
            for (TRANSMITTER transmitter : getTransmitters()) {
                if (transmitter.isValid()) {
                    transmitter.takeShare();
                    transmitter.setTransmitterNetwork(null);
                    TransmitterNetworkRegistry.registerOrphanTransmitter(transmitter);
                }
            }
        }
        deregister();
    }

    protected void onLastTransmitterRemoved(@NotNull TRANSMITTER triggerTransmitter) {
    }

    protected void removeInvalid(@Nullable TRANSMITTER triggerTransmitter) {
        //Remove invalid transmitters first for share calculations
        getTransmitters().removeIf(transmitter -> !transmitter.isValid());
    }

    public void acceptorChanged(TRANSMITTER transmitter, Direction side) {
        acceptorCache.acceptorChanged(transmitter, side);
    }

    public List<TRANSMITTER> adoptTransmittersAndAcceptorsFrom(NETWORK net) {
        positionedTransmitters.putAll(net.positionedTransmitters);
        List<TRANSMITTER> transmittersToUpdate = new ArrayList<>();
        for (ObjectIterator<Long2ObjectMap.Entry<TRANSMITTER>> iterator = Long2ObjectMaps.fastIterator(net.positionedTransmitters); iterator.hasNext(); ) {
            Long2ObjectMap.Entry<TRANSMITTER> entry = iterator.next();
            TRANSMITTER transmitter = entry.getValue();
            positionedTransmitters.put(entry.getLongKey(), transmitter);
            if (transmitter.setTransmitterNetwork(getNetwork(), false)) {
                transmittersToUpdate.add(transmitter);
            }
        }
        transmittersToAdd.addAll(net.transmittersToAdd);
        acceptorCache.adoptAcceptors(net.acceptorCache);
        return transmittersToUpdate;
    }

    protected void adoptAllAndRegister(Collection<NETWORK> networks) {
        List<TRANSMITTER> transmittersToUpdate = new ArrayList<>();
        for (NETWORK net : networks) {
            if (net != null) {
                transmittersToUpdate.addAll(adoptTransmittersAndAcceptorsFrom(net));
                net.deregister();
            }
        }
        register();
        transmittersToUpdate.forEach(Transmitter::requestsUpdate);
    }

    public void register() {
        if (isRemote()) {
            TransmitterNetworkRegistry.addClientNetwork(getUUID(), this);
        } else {
            TransmitterNetworkRegistry.registerNetwork(this);
        }
    }

    public void deregister() {
        positionedTransmitters.clear();
        transmittersToAdd.clear();
        acceptorCache.deregister();
        transmitterValidator = null;
        if (isRemote()) {
            TransmitterNetworkRegistry.removeClientNetwork(this);
        } else {
            TransmitterNetworkRegistry.removeNetwork(this);
        }
    }

    public boolean isEmpty() {
        return positionedTransmitters.isEmpty();
    }

    public int getAcceptorCount() {
        return acceptorCache.getAcceptorCount();
    }

    @Nullable
    public Level getWorld() {
        return world;
    }

    /**
     * @apiNote Only called on the server
     */
    public void onUpdate() {
    }

    public Collection<TRANSMITTER> getTransmitters() {
        return positionedTransmitters.values();
    }

    public void addTransmitter(TRANSMITTER transmitter) {
        positionedTransmitters.put(transmitter.getBlockPos().asLong(), transmitter);
    }

    public void removeTransmitter(TRANSMITTER transmitter) {
        removePositionedTransmitter(transmitter);
        if (isEmpty()) {
            deregister();
        }
    }

    private void removePositionedTransmitter(TRANSMITTER transmitter) {
        BlockPos pos = transmitter.getBlockPos();
        TRANSMITTER currentTransmitter = getTransmitter(pos);
        if (currentTransmitter != null) {
            //This shouldn't be null but if it is, don't bother attempting to remove
            if (currentTransmitter != transmitter) {
                Level world = this.world;
                if (world == null) {
                    //If the world is null, grab it from the transmitter
                    world = transmitter.getLevel();
                }
                if (world != null && world.isClientSide()) {
                    //On the client just exit instead of warning and then removing the unexpected transmitter.
                    // When the client dies at spawn in single player the order of operations is:
                    // - new tiles get added/loaded (so the positioned transmitter gets overridden with the correct one)
                    // - The old one unloads which causes this removedPositionedTransmitter call to take place
                    return;
                }
                Mekanism.logger.warn("Removed transmitter at position: {} in {} was different than expected.", pos, world == null ? null : world.dimension().location());
            }
            positionedTransmitters.remove(pos.asLong());
        }
    }

    public int transmittersSize() {
        return positionedTransmitters.size();
    }

    public boolean hasAcceptor(BlockPos acceptorPos) {
        return acceptorCache.hasAcceptor(acceptorPos);
    }

    public ACCEPTOR getCachedAcceptor(long acceptorPos, Direction side) {
        return acceptorCache.getCachedAcceptor(acceptorPos, side);
    }

    public Set<Direction> getAcceptorDirections(long pos) {
        return acceptorCache.getAcceptorDirections(pos);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof DynamicNetwork<?, ?, ?> other) {
            return uuid.equals(other.uuid);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}