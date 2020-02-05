package mekanism.common.transmitters;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Collection;
import java.util.Collections;
import java.util.function.IntConsumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.TileNetworkList;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.base.ILogisticalTransporter;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.transporter.TransitRequest;
import mekanism.common.content.transporter.TransitRequest.TransitResponse;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.content.transporter.TransporterStack.Path;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporter;
import mekanism.common.tile.transmitter.TileEntitySidedPipe.ConnectionType;
import mekanism.common.transmitters.grid.InventoryNetwork;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.Constants.NBT;

public class TransporterImpl extends TransmitterImpl<TileEntity, InventoryNetwork, Void> implements ILogisticalTransporter {

    private Int2ObjectOpenHashMap<TransporterStack> transit = new Int2ObjectOpenHashMap<>();

    private int nextId = 0;

    private EnumColor color;

    private Int2ObjectMap<TransporterStack> needsSync = new Int2ObjectOpenHashMap<>();

    public TransporterImpl(TileEntityLogisticalTransporter multiPart) {
        super(multiPart);
    }

    public Collection<TransporterStack> getTransit() {
        return Collections.unmodifiableCollection(transit.values());
    }

    public void deleteStack(int id) {
        transit.remove(id);
    }

    public void addStack(int id, TransporterStack s) {
        transit.put(id, s);
    }

    public void writeToPacket(TileNetworkList data) {
        data.add(transit.size());
        for (Int2ObjectMap.Entry<TransporterStack> entry : transit.int2ObjectEntrySet()) {
            data.add(entry.getIntKey());
            entry.getValue().write(this, data);
        }
    }

    public void readFromPacket(PacketBuffer dataStream) {
        transit.clear();
        int count = dataStream.readInt();
        for (int i = 0; i < count; i++) {
            int id = dataStream.readInt();
            TransporterStack s = TransporterStack.readFromPacket(dataStream);
            transit.put(id, s);
        }
    }

    public void readFromNBT(CompoundNBT nbtTags) {
        if (nbtTags.contains("color")) {
            setColor(TransporterUtils.colors.get(nbtTags.getInt("color")));
        }
        if (nbtTags.contains("stacks")) {
            ListNBT tagList = nbtTags.getList("stacks", NBT.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++) {
                TransporterStack stack = TransporterStack.readFromNBT(tagList.getCompound(i));
                transit.put(nextId++, stack);
            }
        }
    }

    public void update() {
        if (world().isRemote) {
            for (TransporterStack stack : transit.values()) {
                stack.progress = Math.min(100, stack.progress + getTileEntity().tier.getSpeed());
            }
        } else if (getTransmitterNetwork() != null) {
            IntSet deletes = new IntOpenHashSet();
            getTileEntity().pullItems();
            Coord4D coord = coord();
            //Note: Our calls to getTileEntity are not done with a chunkMap as we don't tend to have that many tiles we
            // are checking at once from here and given this gets called each tick, it would cause unnecessary garbage
            // collection to occur actually causing the tick time to go up slightly.
            for (Int2ObjectMap.Entry<TransporterStack> entry : transit.int2ObjectEntrySet()) {
                int stackId = entry.getIntKey();
                TransporterStack stack = entry.getValue();
                if (!stack.initiatedPath) {
                    if (stack.itemStack.isEmpty() || !recalculate(stackId, stack, null)) {
                        deletes.add(stackId);
                        continue;
                    }
                }

                stack.progress += getTileEntity().tier.getSpeed();
                if (stack.progress >= 100) {
                    Coord4D prevSet = null;
                    if (stack.hasPath()) {
                        int currentIndex = stack.getPath().indexOf(coord);
                        if (currentIndex == 0) { //Necessary for transition reasons, not sure why
                            deletes.add(stackId);
                            continue;
                        }

                        Coord4D next = stack.getPath().get(currentIndex - 1);
                        //TODO: Can next ever even be null? We moved this check out given theoretically both branches could maybe have it be null
                        // if it can even be null. I did not bother looking into if the null check that already existed was valid or unneeded
                        if (next != null) {
                            if (!stack.isFinal(this)) {
                                TileEntity tile = MekanismUtils.getTileEntity(world(), next.getPos());
                                if (stack.canInsertToTransporter(tile, stack.getSide(this), containingTile)) {
                                    CapabilityUtils.getCapability(tile, Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, null).ifPresent(nextTile ->
                                          nextTile.entityEntering(stack, stack.progress % 100));
                                    deletes.add(stackId);
                                    continue;
                                }
                                prevSet = next;
                            } else if (stack.getPathType() != Path.NONE) {
                                TileEntity tile = MekanismUtils.getTileEntity(world(), next.getPos());
                                if (tile != null) {
                                    TransitResponse response = InventoryUtils.putStackInInventory(tile, TransitRequest.getFromTransport(stack), stack.getSide(this),
                                          stack.getPathType() == Path.HOME);
                                    // Nothing was rejected; remove the stack from the prediction tracker and
                                    // schedule this stack for deletion. Continue the loop thereafter
                                    ItemStack rejected = response.getRejected(stack.itemStack);
                                    if (rejected.isEmpty()) {
                                        TransporterManager.remove(stack);
                                        deletes.add(stackId);
                                        continue;
                                    }
                                    // Some portion of the stack got rejected; save the remainder and
                                    // let the recalculate below sort out what to do next
                                    stack.itemStack = rejected;
                                    prevSet = next;
                                }
                            }
                        }
                    }
                    if (!recalculate(stackId, stack, prevSet)) {
                        deletes.add(stackId);
                    } else if (prevSet != null) {
                        stack.progress = 0;
                    } else {
                        stack.progress = 50;
                    }
                } else if (stack.progress == 50) {
                    boolean tryRecalculate;
                    if (stack.isFinal(this)) {
                        tryRecalculate = checkPath(stack, Path.DEST, false) || checkPath(stack, Path.HOME, true) || stack.getPathType() == Path.NONE;
                    } else {
                        tryRecalculate = !stack.canInsertToTransporter(MekanismUtils.getTileEntity(world(), stack.getNext(this).getPos()), stack.getSide(this), containingTile);
                    }
                    if (tryRecalculate && !recalculate(stackId, stack, null)) {
                        deletes.add(stackId);
                    }
                }
            }

            if (deletes.size() > 0 || needsSync.size() > 0) {
                PacketTileEntity msg = new PacketTileEntity(coord, getTileEntity().makeBatchPacket(needsSync, deletes));
                // Now remove any entries from transit that have been deleted
                deletes.forEach((IntConsumer) (id -> transit.remove(id)));

                // Clear the pending sync packets
                needsSync.clear();

                // Finally, notify clients and mark chunk for save
                //TODO: Check
                Mekanism.packetHandler.sendToAllTracking(msg, world(), coord.getPos());
                MekanismUtils.saveChunk(getTileEntity());
            }
        }
    }

    private boolean checkPath(TransporterStack stack, Path dest, boolean home) {
        return stack.getPathType() == dest && (!checkSideForInsert(stack) || !InventoryUtils.canInsert(MekanismUtils.getTileEntity(world(), stack.getDest().getPos()),
              stack.color, stack.itemStack, stack.getSide(this), home));
    }

    private boolean checkSideForInsert(TransporterStack stack) {
        Direction side = stack.getSide(this);
        return getTileEntity().getConnectionType(side) == ConnectionType.NORMAL || getTileEntity().getConnectionType(side) == ConnectionType.PUSH;
    }

    private boolean recalculate(int stackId, TransporterStack stack, Coord4D from) {
        boolean noPath = stack.getPathType() == Path.NONE;
        if (!noPath) {
            noPath = stack.recalculatePath(TransitRequest.getFromTransport(stack), this, 0).isEmpty();
        }
        if (noPath && !stack.calculateIdle(this)) {
            TransporterUtils.drop(this, stack);
            return false;
        }

        //Only add to needsSync if true is being returned; otherwise it gets added to deletes
        needsSync.put(stackId, stack);
        if (from != null) {
            stack.originalLocation = from;
        }
        return true;
    }

    @Override
    public TransitResponse insert(TileEntity outputter, TransitRequest request, EnumColor color, boolean doEmit, int min) {
        Coord4D original = Coord4D.get(outputter);
        Direction from = coord().sideDifference(original).getOpposite();
        TransporterStack stack = new TransporterStack();
        stack.originalLocation = original;
        stack.homeLocation = original;
        stack.color = color;
        if (!stack.canInsertToTransporter(this, from, outputter)) {
            return TransitResponse.EMPTY;
        }
        TransitResponse response = stack.recalculatePath(request, this, min);
        return getTransitResponse(doEmit, stack, response);
    }

    @Nonnull
    private TransitResponse getTransitResponse(boolean doEmit, TransporterStack stack, TransitResponse response) {
        if (!response.isEmpty()) {
            stack.itemStack = response.getStack();
            if (doEmit) {
                int stackId = nextId++;
                transit.put(stackId, stack);
                Coord4D coord = coord();
                TileEntityLogisticalTransporter tile = getTileEntity();
                //TODO: Check
                Mekanism.packetHandler.sendToAllTracking(new PacketTileEntity(coord, tile.makeSyncPacket(stackId, stack)), tile.getWorld(), coord.getPos());
                MekanismUtils.saveChunk(tile);
            }
            return response;
        }
        return TransitResponse.EMPTY;
    }

    @Override
    public TransitResponse insertRR(TileEntityLogisticalSorter outputter, TransitRequest request, EnumColor color, boolean doEmit, int min) {
        Direction from = coord().sideDifference(Coord4D.get(outputter)).getOpposite();
        TransporterStack stack = new TransporterStack();
        stack.originalLocation = Coord4D.get(outputter);
        stack.homeLocation = Coord4D.get(outputter);
        stack.color = color;
        if (!canReceiveFrom(outputter, from) || !stack.canInsertToTransporter(this, from, outputter)) {
            return TransitResponse.EMPTY;
        }
        TransitResponse response = stack.recalculateRRPath(request, outputter, this, min);
        return getTransitResponse(doEmit, stack, response);
    }

    @Override
    public void entityEntering(TransporterStack stack, int progress) {
        // Update the progress of the stack and add it as something that's both
        // in transit and needs sync down to the client.
        //
        // This code used to generate a sync message at this point, but that was a LOT
        // of bandwidth in a busy server, so by adding to needsSync, the sync will happen
        // in a batch on a per-tick basis.
        int stackId = nextId++;
        stack.progress = progress;
        transit.put(stackId, stack);
        needsSync.put(stackId, stack);

        // N.B. We are not marking the chunk as dirty here! I don't believe it's needed, since
        // the next tick will generate the necessary save and if we crash before the next tick,
        // it's unlikely the data will be save anyways (since chunks aren't saved until the end of
        // a tick).
    }

    @Override
    public EnumColor getColor() {
        return color;
    }

    @Override
    public void setColor(EnumColor c) {
        color = c;
    }

    @Override
    public boolean canEmitTo(TileEntity tile, Direction side) {
        if (!getTileEntity().canConnect(side)) {
            return false;
        }
        return getTileEntity().getConnectionType(side) == ConnectionType.NORMAL || getTileEntity().getConnectionType(side) == ConnectionType.PUSH;
    }

    @Override
    public boolean canReceiveFrom(TileEntity tile, Direction side) {
        if (!getTileEntity().canConnect(side)) {
            return false;
        }
        return getTileEntity().getConnectionType(side) == ConnectionType.NORMAL;
    }

    @Override
    public double getCost() {
        return getTileEntity().getCost();
    }

    @Override
    public boolean canConnectMutual(Direction side, @Nullable TileEntity cachedTile) {
        return getTileEntity().canConnectMutual(side, cachedTile);
    }

    @Override
    public boolean canConnect(Direction side) {
        return getTileEntity().canConnect(side);
    }

    @Override
    public TileEntityLogisticalTransporter getTileEntity() {
        return (TileEntityLogisticalTransporter) containingTile;
    }
}