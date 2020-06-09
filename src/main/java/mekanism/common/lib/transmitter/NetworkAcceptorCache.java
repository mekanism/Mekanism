package mekanism.common.lib.transmitter;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.LazyOptional;

public class NetworkAcceptorCache<ACCEPTOR> {

    //TODO: Move this to being private??
    //TODO: Validate that we are properly redoing the lazy optional if it becomes invalid
    public final Map<BlockPos, Map<Direction, LazyOptional<ACCEPTOR>>> cachedAcceptors = new Object2ObjectOpenHashMap<>();
    private final Map<TileEntityTransmitter<ACCEPTOR, ?, ?>, Set<Direction>> changedAcceptors = new Object2ObjectOpenHashMap<>();

    public boolean hasAcceptor(BlockPos acceptorPos) {
        return cachedAcceptors.containsKey(acceptorPos);
    }

    public void updateTransmitterOnSide(TileEntityTransmitter<ACCEPTOR, ?, ?> transmitter, Direction side) {
        //Note: getAcceptor does not cache the LazyOptional so that we can force re-retrieve it when necessary
        // (Aka it got changed, and marked as changed via changedAcceptors)
        LazyOptional<ACCEPTOR> acceptor = transmitter.canConnectToAcceptor(side) ? transmitter.getAcceptor(side) : LazyOptional.empty();
        BlockPos acceptorPos = transmitter.getPos().offset(side);
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

    public void acceptorChanged(TileEntityTransmitter<ACCEPTOR, ?, ?> transmitter, Direction side) {
        changedAcceptors.computeIfAbsent(transmitter, t -> EnumSet.noneOf(Direction.class)).add(side);
    }

    public void commit() {
        if (!changedAcceptors.isEmpty()) {
            for (Entry<TileEntityTransmitter<ACCEPTOR, ?, ?>, Set<Direction>> entry : changedAcceptors.entrySet()) {
                TileEntityTransmitter<ACCEPTOR, ?, ?> transmitter = entry.getKey();
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
}