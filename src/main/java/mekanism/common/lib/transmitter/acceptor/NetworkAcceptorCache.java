package mekanism.common.lib.transmitter.acceptor;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import mekanism.common.content.network.transmitter.Transmitter;
import mekanism.common.lib.transmitter.TransmitterNetworkRegistry;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.LazyOptional;

public class NetworkAcceptorCache<ACCEPTOR> {

    private final Map<BlockPos, Map<Direction, LazyOptional<ACCEPTOR>>> cachedAcceptors = new Object2ObjectOpenHashMap<>();
    private final Map<Transmitter<ACCEPTOR, ?, ?>, Set<Direction>> changedAcceptors = new Object2ObjectOpenHashMap<>();

    public void updateTransmitterOnSide(Transmitter<ACCEPTOR, ?, ?> transmitter, Direction side) {
        LazyOptional<ACCEPTOR> acceptor = transmitter.canConnectToAcceptor(side) ? transmitter.getAcceptor(side) : LazyOptional.empty();
        BlockPos acceptorPos = transmitter.getTilePos().offset(side);
        if (acceptor.isPresent()) {
            cachedAcceptors.computeIfAbsent(acceptorPos, pos -> new EnumMap<>(Direction.class)).put(side.getOpposite(), acceptor);
        } else if (cachedAcceptors.containsKey(acceptorPos)) {
            Map<Direction, LazyOptional<ACCEPTOR>> cached = cachedAcceptors.get(acceptorPos);
            cached.remove(side.getOpposite());
            if (cached.isEmpty()) {
                cachedAcceptors.remove(acceptorPos);
            }
        } else {
            cachedAcceptors.remove(acceptorPos);
        }
    }

    public void adoptAcceptors(NetworkAcceptorCache<ACCEPTOR> other) {
        for (Entry<BlockPos, Map<Direction, LazyOptional<ACCEPTOR>>> entry : other.cachedAcceptors.entrySet()) {
            BlockPos pos = entry.getKey();
            if (cachedAcceptors.containsKey(pos)) {
                Map<Direction, LazyOptional<ACCEPTOR>> cached = cachedAcceptors.get(pos);
                entry.getValue().forEach(cached::put);
            } else {
                cachedAcceptors.put(pos, entry.getValue());
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

    /**
     * @apiNote Listeners should not be added to these LazyOptionals here as they may not correspond to an actual handler and may not get invalidated.
     */
    public Set<Map.Entry<BlockPos, Map<Direction, LazyOptional<ACCEPTOR>>>> getAcceptorEntrySet() {
        return cachedAcceptors.entrySet();
    }

    public int getAcceptorCount() {
        //Count multiple connections to the same position as multiple acceptors
        return cachedAcceptors.values().stream().mapToInt(Map::size).sum();
    }

    public boolean hasAcceptor(BlockPos acceptorPos) {
        return cachedAcceptors.containsKey(acceptorPos);
    }

    public Set<Direction> getAcceptorDirections(BlockPos pos) {
        //TODO: Do this better?
        return cachedAcceptors.get(pos).keySet();
    }
}