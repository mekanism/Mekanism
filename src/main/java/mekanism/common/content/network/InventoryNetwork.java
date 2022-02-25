package mekanism.common.content.network;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.RelativeSide;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.PathfinderCache;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.lib.inventory.TransitRequest.TransitResponse;
import mekanism.common.lib.transmitter.DynamicNetwork;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public class InventoryNetwork extends DynamicNetwork<IItemHandler, InventoryNetwork, LogisticalTransporterBase> {

    private final Map<BlockPos, LogisticalTransporterBase> positionedTransmitters = new Object2ObjectOpenHashMap<>();

    public InventoryNetwork(UUID networkID) {
        super(networkID);
    }

    public InventoryNetwork(Collection<InventoryNetwork> networks) {
        this(UUID.randomUUID());
        adoptAllAndRegister(networks);
    }

    public List<AcceptorData> calculateAcceptors(TransitRequest request, TransporterStack stack, Long2ObjectMap<ChunkAccess> chunkMap) {
        List<AcceptorData> toReturn = new ArrayList<>();
        for (Map.Entry<BlockPos, Map<Direction, LazyOptional<IItemHandler>>> entry : acceptorCache.getAcceptorEntrySet()) {
            BlockPos pos = entry.getKey();
            if (!pos.equals(stack.homeLocation)) {
                BlockEntity acceptor = WorldUtils.getTileEntity(getWorld(), chunkMap, pos);
                if (acceptor == null) {
                    continue;
                }
                Map<TransitResponse, AcceptorData> dataMap = new HashMap<>();
                Coord4D position = new Coord4D(pos, getWorld());
                for (Map.Entry<Direction, LazyOptional<IItemHandler>> acceptorEntry : entry.getValue().entrySet()) {
                    Optional<IItemHandler> handler = acceptorEntry.getValue().resolve();
                    if (handler.isPresent()) {
                        Direction side = acceptorEntry.getKey();
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
                        TransitResponse response = TransporterManager.getPredictedInsert(position, side, handler.get(), request);
                        if (!response.isEmpty()) {
                            Direction opposite = side.getOpposite();
                            //If the response isn't empty, check if we already have acceptor data for
                            // a matching response at the destination
                            AcceptorData data = dataMap.get(response);
                            if (data == null) {
                                //If we don't, add a new acceptor data for the response and position with side
                                data = new AcceptorData(pos, response, opposite);
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
        }
        return toReturn;
    }

    @Nullable
    public LogisticalTransporterBase getTransmitter(BlockPos pos) {
        return positionedTransmitters.get(pos);
    }

    @Override
    protected void addTransmitterFromCommit(LogisticalTransporterBase transmitter) {
        super.addTransmitterFromCommit(transmitter);
        positionedTransmitters.put(transmitter.getTilePos(), transmitter);
    }

    @Override
    public void addTransmitter(LogisticalTransporterBase transmitter) {
        super.addTransmitter(transmitter);
        positionedTransmitters.put(transmitter.getTilePos(), transmitter);
    }

    @Override
    public void removeTransmitter(LogisticalTransporterBase transmitter) {
        removePositionedTransmitter(transmitter);
        super.removeTransmitter(transmitter);
    }

    private void removePositionedTransmitter(LogisticalTransporterBase transmitter) {
        BlockPos pos = transmitter.getTilePos();
        LogisticalTransporterBase currentTransmitter = positionedTransmitters.get(pos);
        if (currentTransmitter != null) {
            //This shouldn't be null but if it is, don't bother attempting to remove
            if (currentTransmitter != transmitter) {
                Level world = this.world;
                if (world == null) {
                    //If the world is null, grab it from the transmitter
                    world = transmitter.getTileWorld();
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
            positionedTransmitters.remove(pos);
        }
    }

    @Override
    protected void removeInvalid(@Nullable LogisticalTransporterBase triggerTransmitter) {
        //Remove invalid transmitters first for share calculations
        Iterator<LogisticalTransporterBase> iterator = transmitters.iterator();
        while (iterator.hasNext()) {
            LogisticalTransporterBase transmitter = iterator.next();
            if (!transmitter.isValid()) {
                iterator.remove();
                removePositionedTransmitter(transmitter);
            }
        }
    }

    @Override
    public List<LogisticalTransporterBase> adoptTransmittersAndAcceptorsFrom(InventoryNetwork net) {
        positionedTransmitters.putAll(net.positionedTransmitters);
        return super.adoptTransmittersAndAcceptorsFrom(net);
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
        positionedTransmitters.clear();
        // update the cache when the network has been removed (when transmitters are removed)
        PathfinderCache.onChanged(this);
    }

    @Override
    public String toString() {
        return "[InventoryNetwork] " + transmittersSize() + " transmitters, " + getAcceptorCount() + " acceptors.";
    }

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