package mekanism.common.content.network;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
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
import mekanism.api.Coord4D;
import mekanism.api.RelativeSide;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.PathfinderCache;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.content.transporter.TransporterStack.Path;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.lib.inventory.TransitRequest.TransitResponse;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.lib.transmitter.DynamicNetwork;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InventoryNetwork extends DynamicNetwork<IItemHandler, InventoryNetwork, LogisticalTransporterBase> {

    private final Map<BlockPos, LogisticalTransporterBase> positionedTransmitters = new Object2ObjectOpenHashMap<>();
    //TODO: Potentially keep track of in flight items in the network instead on individual transporters
    // That way we can properly handle cases where the speed of a transporter has been configured to be fast enough it ends up skipping
    // over individual transporters (would need to have network or something tick on client side as well or figure out how to best sync the data for rendering)
    //TODO: Issues which mainly would have to do with path invalidation (so that we can skip checking every tile in the path to make sure it can still travel)
    // - Need to come up with a good way to know when transmitters change connectivity (I believe network currently unforms but even without that)
    // - Need to come up with a good way to know when transmitters get removed (network unforms)
    // - See how it currently handles transmitters being added (as if there is a better path does it take it? If so we definitely need to make sure to handle it)
    // - Handle when the color of transmitters change, can skip if matches/clear or something?, (also maybe see if better path?)
    // - Destination is removed?? (I don't know when it currently realizes it and attempts to repath but if we track destinations then we should easily be able to know)
    //TODO: I think current system might be recalculating the path every time it gets out of current transmitter??? Seems super inefficient
    // even just recalculating the paths of all stacks in the network when the transporters or destination change seems like it would be better than current

    //TODO: Can we make use of PathfinderCache that already keeps track of paths based on network to then just know which ones are in need of an update?
    // Given then we calculate/keep a list of changed paths and then can just do instance comparison (==) to see if the stack has one of the cached paths
    // and if so then recalculate it. The one issue that may not handle properly is IDLE paths

    //TODO: Should it be a list or a Int2Object map like it used to be
    private final Map<BlockPos, List<TrackedTransporterStack>> transit = new Object2ObjectOpenHashMap<>();

    private record TrackedTransporterStack(int id, TransporterStack stack) {
    }

    public InventoryNetwork(UUID networkID) {
        super(networkID);
    }

    public InventoryNetwork(Collection<InventoryNetwork> networks) {
        this(UUID.randomUUID());
        adoptAllAndRegister(networks);
    }

    //TODO: Rename, also move below calculateAcceptors, and just clean up in general
    private void simulate() {
        if (transit.isEmpty()) {
            return;
        }
        //Update stack positions
        IntSet deletes = new IntOpenHashSet();
        //TODO: Re-evalaute this comment from before
        //Note: Our calls to getTileEntity are not done with a chunkMap as we don't tend to have that many tiles we
        // are checking at once from here and given this gets called each tick, it would cause unnecessary garbage
        // collection to occur actually causing the tick time to go up slightly.

        for (Map.Entry<BlockPos, List<TrackedTransporterStack>> entry : transit.entrySet()) {
            BlockPos pos = entry.getKey();
            LogisticalTransporterBase transporter = positionedTransmitters.get(pos);
            //TODO: Should world be pulled from transporter
            for (TrackedTransporterStack trackedTransporterStack : entry.getValue()) {
                int stackId = trackedTransporterStack.id();
                TransporterStack stack = trackedTransporterStack.stack();
                if (!stack.initiatedPath) {//Initiate any paths and remove things that can't go places
                    if (stack.itemStack.isEmpty() || !recalculate(transporter, stackId, stack, null)) {
                        deletes.add(stackId);
                        continue;
                    }
                }

                int prevProgress = stack.progress;
                stack.progress += transporter.tier.getSpeed();
                if (stack.progress >= 100) {
                    BlockPos prevSet = null;
                    if (stack.hasPath()) {
                        //TODO: In theory we could and potentially replace this with stack#getNext, but we could also
                        // instead take advantage of having the index to skip a lookup check for calculating stack#isFinal
                        List<BlockPos> path = stack.getPath();
                        int currentIndex = path.indexOf(pos);
                        if (currentIndex == 0) { //Necessary for transition reasons, not sure why
                            //TODO: Potential reason for why is so it removes it when inserting it into a block?
                            // as it seems to only be index 0 when it is length zero OR actually also potentially
                            // when it is just the first transmitter it was in.
                            //TODO: Which realistically means that this is only really needed to add explicitly to deletes
                            // if there is no next
                            //TODO: Wait actually current index == 0 means that this tile is the destination, but can transporters
                            // be the destination of a path? Maybe especially for when it is idling
                            deletes.add(stackId);
                            continue;
                        }
                        //TODO - Network: If speed is configured in such a way that progress might be >= 200 we should potentially try to make it
                        // skip over the intermediary transporters that due to not having invalidating SHOULD be valid to go through

                        int distTravelled = stack.progress / 100;
                        //Get next position (or if it is the final position)
                        int nextIndex = Math.max(currentIndex - distTravelled, 0);

                        BlockPos next = path.get(nextIndex);
                        //TODO - NETWORK: Instead of checking current transporter it should check the transporter before next index
                        // if dist travelled is > 1 and it gets to the end
                        if (stack.isFinal(transporter)) {
                            if (stack.getPathType().hasTarget()) {
                                //Otherwise, try to insert it into the destination inventory

                                //TODO - Network: Can we get the destination tile via the cached acceptors?
                                BlockEntity tile = WorldUtils.getTileEntity(world, next);
                                if (tile != null) {
                                    //TODO: Check proper transporter in case dist travelled is > 1, also see if we can somehow used the acceptor cache
                                    // the one case we care about the tile is for if it is a logistical sorter but maybe we can somehow get that data
                                    // to be in the acceptor cache?
                                    //TODO: One alternative might be to have the sorter give a different iitemhandler if there is a transporter in front of it
                                    // and then the transporter is the only one that will see it and can try to force it to insert back to home
                                    // and the transporter checks that somehow? Seems kind of meh

                                    TransitResponse response = TransitRequest.simple(stack.itemStack).addToInventory(tile, stack.getSide(transporter), 0,
                                          stack.getPathType().isHome());
                                    if (!response.isEmpty()) {
                                        //We were able to add at least part of the stack to the inventory
                                        ItemStack rejected = response.getRejected();
                                        if (rejected.isEmpty()) {
                                            //Nothing was rejected (it was all accepted); remove the stack from the prediction
                                            // tracker and schedule this stack for deletion. Continue the loop thereafter
                                            TransporterManager.remove(world, stack);
                                            deletes.add(stackId);
                                            continue;
                                        }
                                        //Some portion of the stack got rejected; save the remainder and
                                        // recalculate below to sort out what to do next
                                        stack.itemStack = rejected;
                                    }//else the entire stack got rejected (Note: we don't need to update the stack to point to itself)
                                    prevSet = next;
                                    //TODO - Network: Recalculate with remainder
                                }
                                //TODO - Network: Presuming that prevSet should actually be set to next if failed,
                                // then we should do so when we can't find the tile given we were trying to insert into it

                            }//TODO - Network: If it is final but there is no target then we need to recalculate (we can collapse the if with one of final probably?)
                        } else {
                            //TODO - Network: I think the body of this if can be removed?
                            // As we should be invalidating/checking this stuff when things change
                            // so that this means we don't need to recalculate as we aren't at the end
                            //If this is not the final transporter try transferring it to the next one
                            LogisticalTransporterBase transmitter = getTransmitter(next);
                            if (stack.canInsertToTransporter(transmitter, stack.getSide(transporter), transporter)) {
                                entityEntering(transmitter, stack, stack.progress % 100);
                                deletes.add(stackId);
                                continue;
                            }
                            //TODO??: If can't insert into next transmitter make original location be the one of the next transmitter? NO THAT IS DUMB
                            prevSet = next;
                        }
                    }
                    //TODO - Network: I think recalculating here can be skipped (unless no path?) and instead we just need to do a progress %= 100
                    // which is basically what the above non final code does

                    //TODO: Re-evaluate prevSet being set to next in places? I think it potentially should be our position rather than the next one???
                    // Or is it that that is the previous set next one so then given it has to recalculate we treat it as coming from there
                    // We need to look at how getSide is used as it could be it has to do with deciding which side to go on. My guess is that actually
                    // it is the latter and that it is so that when it "bounces" off of a piece then it starts in the correct spot
                    if (!recalculate(transporter, stackId, stack, prevSet)) {
                        deletes.add(stackId);
                    } else if (prevSet == null) {
                        //Start in middle
                        stack.progress = 50;
                    } else {
                        //Start at edge
                        stack.progress = 0;
                    }
                } else if (prevProgress < 50 && stack.progress >= 50) {
                    //TODO - Network: What if we go from < 50 to say like 160, that gets caught by above one but if we have logic
                    // that actually is supposed to get run here, we may need to do it then as well


                    //If the stack has transitioned past the halfway point
                    //TODO: Finish above comment
                    boolean tryRecalculate;
                    if (stack.isFinal(transporter)) {
                        //TODO: If this is the final transporter: ??
                        Path pathType = stack.getPathType();
                        if (pathType.hasTarget()) {
                            Direction side = stack.getSide(transporter);
                            ConnectionType connectionType = transporter.getConnectionType(side);
                            //TODO - Network: Basically recalculates if we can't emit/insert it into the destination, but is there much harm in
                            // instead of having this check just continuing to progress and hit the destination and then see that it can't insert
                            // it and recalculating then? Though I am not actually sure if we do check all this stuff again when inserting
                            tryRecalculate = !connectionType.canEmit() || !TransporterUtils.canInsert(WorldUtils.getTileEntity(world, stack.getDest()),
                                  stack.color, stack.itemStack, side, pathType.isHome());
                        } else {
                            //Try to recalculate idles once they reach their destination
                            tryRecalculate = true;
                        }
                    } else {
                        //TODO - Network: I think this else branch potentially can be removed other than maybe handling when it was an idle path?
                        // though we may be able to get idle paths to be "final" like happens above
                        BlockPos nextPos = stack.getNext(transporter);
                        Direction nextSide = WorldUtils.sideDifference(nextPos, pos);
                        if (nextSide == null) {//TODO: RE-EVALUATE in theory only would happen if they aren't actually sequential or if the next pos is this pos?
                            nextSide = Direction.DOWN;
                        }
                        LogisticalTransporterBase nextTransmitter = getTransmitter(nextPos);
                        //TODO: In theory because we already have next and know which side of progress it is on we could simplify the getSide logic
                        // which is what we now are doing a few lines above
                        if (nextTransmitter == null && !stack.getPathType().hasTarget() && stack.getPath().size() == 2) {
                            //If there is no next transmitter, and it was an idle path, assume that we are idling
                            // in a single length transmitter, in which case we only recalculate it at 50 if it won't
                            // be able to go into that connection type
                            tryRecalculate = !transporter.getConnectionType(nextSide).canEmit();
                        } else {
                            tryRecalculate = !stack.canInsertToTransporter(nextTransmitter, nextSide, transporter);
                        }
                    }
                    if (tryRecalculate && !recalculate(transporter, stackId, stack, null)) {
                        deletes.add(stackId);
                    }
                }
            }
            //TODO- Network: Optimize and pull out of loop. And reduce what needs to sync to do it based on path and then extrapolating game time?

            //TODO: Needs sync gets set on neighboring ones not on this, so re-evaluate how exactly we want to handle this
            // also should we remove any entries in delete from needs sync potentially
            // (aka see if there are cases when there may be overlap)
            //todo:
            /*if (!deletes.isEmpty() || !transporter.needsSync.isEmpty()) {
                //Notify clients, so that we send the information before we start clearing our lists
                Mekanism.packetHandler().sendToAllTracking(new PacketTransporterUpdate(transporter, transporter.needsSync, deletes), transporter.getTransmitterTile());
                // Now remove any entries from transit that have been deleted
                //TODO: Delete from proper spot in network instead
                deletes.forEach((IntConsumer) (transporter::deleteStack));

                // Clear the pending sync packets
                transporter.needsSync.clear();

                // Finally, mark chunk for save
                transporter.getTransmitterTile().markForSave();
            }*/
        }
    }

    //TODO - NETWORK: Re-evaluate
    private boolean recalculate(LogisticalTransporterBase transporter, int stackId, TransporterStack stack, BlockPos from) {
        boolean noPath = !stack.getPathType().hasTarget() || stack.recalculatePath(TransitRequest.simple(stack.itemStack), transporter, 0).isEmpty();
        if (noPath && !stack.calculateIdle(transporter)) {
            TransporterUtils.drop(transporter, stack);
            return false;
        }

        //Only add to needsSync if true is being returned; otherwise it gets added to deletes
        //todo: transporter.needsSync.put(stackId, stack);
        if (from != null) {
            stack.originalLocation = from;
        }
        return true;
    }

    //TODO - NETWORK: Re-evaluate
    private void entityEntering(LogisticalTransporterBase transporter, TransporterStack stack, int progress) {
        // Update the progress of the stack and add it as something that's both
        // in transit and needs sync down to the client.
        //
        // This code used to generate a sync message at this point, but that was a LOT
        // of bandwidth in a busy server, so by adding to needsSync, the sync will happen
        // in a batch on a per-tick basis.
        //todo:
        /*int stackId = transporter.nextId++;
        stack.progress = progress;
        transporter.addStack(stackId, stack);
        transporter.needsSync.put(stackId, stack);*/

        // N.B. We are not marking the chunk as dirty here! I don't believe it's needed, since
        // the next tick will generate the necessary save and if we crash before the next tick,
        // it's unlikely the data will be saved anyway (since chunks aren't saved until the end of
        // a tick).
    }

    public List<AcceptorData> calculateAcceptors(TransitRequest request, TransporterStack stack, Long2ObjectMap<ChunkAccess> chunkMap,
          Map<Coord4D, Set<TransporterStack>> additionalFlowingStacks) {
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
                        TransitResponse response = TransporterManager.getPredictedInsert(position, side, handler.get(), request, additionalFlowingStacks);
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
        LogisticalTransporterBase currentTransmitter = getTransmitter(pos);
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