package mekanism.common.lib.transmitter;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.MekanismUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.IChunk;

public class NetworkAcceptorCache<ACCEPTOR> {

    //TODO: Move these to being private
    public final Set<BlockPos> possibleAcceptors = new ObjectOpenHashSet<>();
    public final Map<BlockPos, Set<Direction>> acceptorDirections = new Object2ObjectOpenHashMap<>();

    public boolean hasAcceptor(BlockPos acceptorPos) {
        return possibleAcceptors.contains(acceptorPos);
    }

    public void updateTransmitterOnSide(TileEntityTransmitter<ACCEPTOR, ?, ?> transmitter, Direction side) {
        //TODO: Rework transmitter.getAcceptor to do it via this cache class
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

    public void adoptAcceptors(NetworkAcceptorCache<ACCEPTOR> other) {
        possibleAcceptors.addAll(other.possibleAcceptors);
        for (Entry<BlockPos, Set<Direction>> entry : other.acceptorDirections.entrySet()) {
            BlockPos pos = entry.getKey();
            if (acceptorDirections.containsKey(pos)) {
                acceptorDirections.get(pos).addAll(entry.getValue());
            } else {
                acceptorDirections.put(pos, entry.getValue());
            }
        }
    }

    public List<ACCEPTOR> calculateAcceptors(IWorld world, Long2ObjectMap<IChunk> chunkMap) {
        List<ACCEPTOR> acceptors = new ArrayList<>();
        for (BlockPos pos : possibleAcceptors) {
            //TODO: Don't allow null in possibleAcceptors??
            if (pos == null) {
                continue;
            }
            Set<Direction> sides = acceptorDirections.get(pos);
            if (sides == null || sides.isEmpty()) {
                continue;
            }
            //TODO: Use and lookup from a cache
            TileEntity acceptor = MekanismUtils.getTileEntity(world, chunkMap, pos);
            if (acceptor == null) {
                continue;
            }
            for (Direction side : sides) {
                Direction opposite = side.getOpposite();
                //TODO: Add to acceptors??
            }
        }
        return acceptors;
    }
}