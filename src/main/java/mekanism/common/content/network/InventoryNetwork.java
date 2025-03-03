package mekanism.common.content.network;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import mekanism.api.RelativeSide;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.PathfinderCache;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.content.transporter.TransporterPathfinder;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.lib.inventory.TransitRequest.TransitResponse;
import mekanism.common.lib.transmitter.DynamicNetwork;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class InventoryNetwork extends DynamicNetwork<IItemHandler, InventoryNetwork, LogisticalTransporterBase> {

    public InventoryNetwork(UUID networkID) {
        super(networkID);
    }

    public InventoryNetwork(Collection<InventoryNetwork> networks) {
        this(UUID.randomUUID());
        adoptAllAndRegister(networks);
    }

    public List<AcceptorData> calculateAcceptors(TransitRequest request, TransporterStack stack, Long2ObjectMap<ChunkAccess> chunkMap,
          Map<GlobalPos, Set<TransporterStack>> additionalFlowingStacks, LogisticalTransporterBase start) {
        List<AcceptorData> toReturn = new ArrayList<>();
        for (ObjectIterator<Long2ObjectMap.Entry<Map<Direction, IItemHandler>>> iterator = acceptorCache.getAcceptorFastIterator(); iterator.hasNext(); ) {
            Long2ObjectMap.Entry<Map<Direction, IItemHandler>> entry = iterator.next();
            long pos = entry.getLongKey();
            if (pos != stack.homeLocation) {
                BlockPos blockPos = BlockPos.of(pos);
                BlockEntity acceptor = WorldUtils.getTileEntity(getWorld(), chunkMap, blockPos);
                Map<TransitResponse, AcceptorData> dataMap = new HashMap<>();
                GlobalPos position = GlobalPos.of(getWorld().dimension(), blockPos);
                for (Map.Entry<Direction, IItemHandler> acceptorEntry : entry.getValue().entrySet()) {
                    IItemHandler handler = acceptorEntry.getValue();
                    Direction side = acceptorEntry.getKey();
                    PathfinderCache.CachedPath cachedPath = PathfinderCache.getSingleCache(start, blockPos, side);
                    if (cachedPath != null && !TransporterPathfinder.checkPath(this, cachedPath.path(), stack)) {
                        continue;//invalid path, no need to simulate
                    }
                    //TODO: Figure out how we want to best handle the color check, as without doing it here we don't
                    // actually need to even query the TE
                    if (acceptor instanceof ISideConfiguration config) {
                        //If the acceptor in question implements the mekanism interface, check that the color matches and bail fast if it doesn't
                        if (config.getEjector().hasStrictInput()) {
                            EnumColor configColor = config.getEjector().getInputColor(RelativeSide.fromDirections(config.getDirection(), side));
                            if (configColor != null && configColor != stack.color) {
                                continue;
                            }
                        }
                    }
                    TransitResponse response = TransporterManager.getPredictedInsert(position, side, handler, request, additionalFlowingStacks);
                    if (!response.isEmpty()) {
                        Direction opposite = side.getOpposite();
                        //If the response isn't empty, check if we already have acceptor data for
                        // a matching response at the destination
                        AcceptorData data = dataMap.get(response);
                        if (data == null) {
                            //If we don't, add a new acceptor data for the response and position with side
                            data = new AcceptorData(blockPos, response, opposite);
                            dataMap.put(response, data);
                            toReturn.add(data);
                            //Note: In theory this shouldn't cause any issues if some exposed slots overlap but are for
                            // different acceptor data/sides as our predicted insert takes into account all en-route
                            // items to the destination, and only checks about the side if none are actually able to be
                            // inserted in the first place
                        } else {
                            //If we do, add our side as one of the sides it can accept things from for that response
                            // This equates to the destination being the same
                            data.sides.add(opposite);
                        }
                    }
                }
            }
        }
        return toReturn;
    }

    @Override
    public void commit() {
        super.commit();
        // update the cache when the network has been changed (called when transmitters are added)
        PathfinderCache.onChanged(this);
    }

    @Override
    public void deregister() {
        super.deregister();
        // update the cache when the network has been removed (when transmitters are removed)
        PathfinderCache.onChanged(this);
    }

    @Override
    public String toString() {
        return "[InventoryNetwork] " + transmittersSize() + " transmitters, " + getAcceptorCount() + " acceptors.";
    }

    @NotNull
    @Override
    public Component getTextComponent() {
        return MekanismLang.NETWORK_DESCRIPTION.translate(MekanismLang.INVENTORY_NETWORK, transmittersSize(), getAcceptorCount());
    }

    public static class AcceptorData {

        private final BlockPos location;
        private final TransitResponse response;
        private final Set<Direction> sides;

        protected AcceptorData(BlockPos pos, TransitResponse ret, Direction side) {
            location = pos;
            response = ret;
            sides = EnumSet.of(side);
        }

        public TransitResponse getResponse() {
            return response;
        }

        public BlockPos getLocation() {
            return location;
        }

        public Set<Direction> getSides() {
            return sides;
        }
    }
}