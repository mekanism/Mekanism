package mekanism.common.lib.transmitter.acceptor;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import mekanism.common.content.network.transmitter.Transmitter;
import mekanism.common.lib.collection.EnumArray;
import mekanism.common.lib.transmitter.TransmitterNetworkRegistry;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public class NetworkAcceptorCache<ACCEPTOR> {
    private static final Direction[] DIRECTIONS = Direction.values();

    private final Long2ObjectMap<EnumArray<Direction, ACCEPTOR>> cachedAcceptors = new Long2ObjectOpenHashMap<>();
    private final Map<Transmitter<ACCEPTOR, ?, ?>, Set<Direction>> changedAcceptors = new Object2ObjectOpenHashMap<>();

    public void updateTransmitterOnSide(Transmitter<ACCEPTOR, ?, ?> transmitter, Direction side) {
        transmitter.refreshAcceptorConnections(side);
        ACCEPTOR acceptor = transmitter.canConnectToAcceptor(side) ? transmitter.getAcceptor(side) : null;
        long acceptorPos = WorldUtils.relativePos(transmitter.getWorldPositionLong(), side);
        if (acceptor == null) {
            EnumArray<Direction, ACCEPTOR> cached = cachedAcceptors.get(acceptorPos);
            if (cached != null) {
                cached.set(side.getOpposite(),  null);
                if (cached.isEmpty()) {
                    cachedAcceptors.remove(acceptorPos);
                }
            }
        } else {
            cachedAcceptors.computeIfAbsent(acceptorPos, pos -> new EnumArray<>(DIRECTIONS, Direction.class)).set(side.getOpposite(), acceptor);
        }
    }

    public void adoptAcceptors(NetworkAcceptorCache<ACCEPTOR> other) {
        for (Long2ObjectMap.Entry<EnumArray<Direction, ACCEPTOR>> entry : other.cachedAcceptors.long2ObjectEntrySet()) {
            long pos = entry.getLongKey();
            EnumArray<Direction, ACCEPTOR> otherAcceptors = entry.getValue();
            EnumArray<Direction, ACCEPTOR> myCachedAcceptors = this.cachedAcceptors.get(pos);
            if (myCachedAcceptors != null) {
                myCachedAcceptors.putAll(otherAcceptors);
            } else {
                cachedAcceptors.put(pos, otherAcceptors);
            }
        }
        for (Entry<Transmitter<ACCEPTOR, ?, ?>, Set<Direction>> entry : other.changedAcceptors.entrySet()) {
            Transmitter<ACCEPTOR, ?, ?> transmitter = entry.getKey();
            if (changedAcceptors.containsKey(transmitter)) {
                changedAcceptors.get(transmitter).addAll(entry.getValue());
            } else {
                changedAcceptors.put(transmitter, entry.getValue());
            }
        }
    }

    public void acceptorChanged(Transmitter<ACCEPTOR, ?, ?> transmitter, Direction side) {
        changedAcceptors.computeIfAbsent(transmitter, t -> EnumSet.noneOf(Direction.class)).add(side);
        TransmitterNetworkRegistry.registerChangedNetwork(transmitter.getTransmitterNetwork());
    }

    public void commit() {
        if (!changedAcceptors.isEmpty()) {
            for (Entry<Transmitter<ACCEPTOR, ?, ?>, Set<Direction>> entry : changedAcceptors.entrySet()) {
                Transmitter<ACCEPTOR, ?, ?> transmitter = entry.getKey();
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

    public void deregister() {
        cachedAcceptors.clear();
        changedAcceptors.clear();
    }

    /**
     * @apiNote Listeners should not be added to these LazyOptionals here as they may not correspond to an actual handler and may not get invalidated.
     */
    public Set<Long2ObjectMap.Entry<EnumArray<Direction, ACCEPTOR>>> getAcceptorEntrySet() {
        return cachedAcceptors.long2ObjectEntrySet();
    }

    /**
     * @apiNote Listeners should not be added to these LazyOptionals here as they may not correspond to an actual handler and may not get invalidated.
     */
    public Collection<EnumArray<Direction, ACCEPTOR>> getAcceptorValues() {
        return cachedAcceptors.values();
    }

    public int getAcceptorCount() {
        //Count multiple connections to the same position as multiple acceptors
        return cachedAcceptors.values().stream().mapToInt(EnumArray::countNonEmpty).sum();
    }

    public boolean hasAcceptor(long acceptorPos) {
        return cachedAcceptors.containsKey(acceptorPos);
    }

    public boolean hasAcceptor(BlockPos acceptorPos) {
        return hasAcceptor(acceptorPos.asLong());
    }

    @Nullable
    public ACCEPTOR getCachedAcceptor(long acceptorPos, Direction side) {
        EnumArray<Direction, ACCEPTOR> acceptors = cachedAcceptors.get(acceptorPos);
        return acceptors != null ? acceptors.get(side) : null;
    }

    public Set<Direction> getAcceptorDirections(long pos) {
        //TODO: Do this better?
        EnumArray<Direction, ACCEPTOR> acceptors = cachedAcceptors.get(pos);
        return acceptors != null ? acceptors.usedKeys() : Collections.emptySet();
    }
}