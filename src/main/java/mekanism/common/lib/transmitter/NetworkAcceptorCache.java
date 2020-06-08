package mekanism.common.lib.transmitter;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.LazyOptional;

public class NetworkAcceptorCache<ACCEPTOR> {

    //TODO: Move these to being private
    //TODO: Replace acceptorDirections with cachedAcceptors?
    public final Map<BlockPos, Set<Direction>> acceptorDirections = new Object2ObjectOpenHashMap<>();
    public final Map<BlockPos, Map<Direction, LazyOptional<ACCEPTOR>>> cachedAcceptors = new Object2ObjectOpenHashMap<>();
    private final Map<TileEntityTransmitter<ACCEPTOR, ?, ?>, Set<Direction>> changedAcceptors = new Object2ObjectOpenHashMap<>();

    public boolean hasAcceptor(BlockPos acceptorPos) {
        return acceptorDirections.containsKey(acceptorPos);
    }

    public void updateTransmitterOnSide(TileEntityTransmitter<ACCEPTOR, ?, ?> transmitter, Direction side) {
        //Note: getAcceptor does not cache the LazyOptional so that we can force re-retrieve it when necessary
        // (Aka it got changed, and marked as changed via changedAcceptors)
        LazyOptional<ACCEPTOR> acceptor = transmitter.canConnectToAcceptor(side) ? transmitter.getAcceptor(side) : LazyOptional.empty();
        BlockPos acceptorPos = transmitter.getPos().offset(side);
        if (acceptor.isPresent()) {
            acceptorDirections.computeIfAbsent(acceptorPos, pos -> EnumSet.noneOf(Direction.class)).add(side.getOpposite());
        } else if (acceptorDirections.containsKey(acceptorPos)) {
            Set<Direction> directions = acceptorDirections.get(acceptorPos);
            directions.remove(side.getOpposite());
            if (directions.isEmpty()) {
                acceptorDirections.remove(acceptorPos);
            }
        } else {
            acceptorDirections.remove(acceptorPos);
        }
    }

    public void adoptAcceptors(NetworkAcceptorCache<ACCEPTOR> other) {
        for (Entry<BlockPos, Set<Direction>> entry : other.acceptorDirections.entrySet()) {
            BlockPos pos = entry.getKey();
            if (acceptorDirections.containsKey(pos)) {
                acceptorDirections.get(pos).addAll(entry.getValue());
            } else {
                acceptorDirections.put(pos, entry.getValue());
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