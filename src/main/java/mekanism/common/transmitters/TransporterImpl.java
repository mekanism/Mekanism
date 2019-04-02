package mekanism.common.transmitters;

import io.netty.buffer.ByteBuf;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.Range4D;
import mekanism.api.TileNetworkList;
import mekanism.common.Mekanism;
import mekanism.common.base.ILogisticalTransporter;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.transporter.TransitRequest;
import mekanism.common.content.transporter.TransitRequest.TransitResponse;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.content.transporter.TransporterStack.Path;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporter;
import mekanism.common.tile.transmitter.TileEntitySidedPipe.ConnectionType;
import mekanism.common.transmitters.grid.InventoryNetwork;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.Constants.NBT;

public class TransporterImpl extends TransmitterImpl<TileEntity, InventoryNetwork> implements ILogisticalTransporter {

    private Map<Integer, TransporterStack> transit = new HashMap<>();

    private int nextId = 0;

    private EnumColor color;

    private Map<Integer, TransporterStack> needsSync = new HashMap<>();

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
        for (Entry<Integer, TransporterStack> entry : transit.entrySet()) {
            data.add(entry.getKey());
            entry.getValue().write(this, data);
        }
    }

    public void readFromPacket(ByteBuf dataStream) {
        transit.clear();

        int count = dataStream.readInt();
        for (int i = 0; i < count; i++) {
            int id = dataStream.readInt();
            TransporterStack s = TransporterStack.readFromPacket(dataStream);
            transit.put(id, s);
        }
    }

    public void readFromNBT(NBTTagCompound nbtTags) {
        if (nbtTags.hasKey("color")) {
            setColor(TransporterUtils.colors.get(nbtTags.getInteger("color")));
        }

        if (nbtTags.hasKey("stacks")) {
            NBTTagList tagList = nbtTags.getTagList("stacks", NBT.TAG_COMPOUND);

            for (int i = 0; i < tagList.tagCount(); i++) {
                TransporterStack stack = TransporterStack.readFromNBT(tagList.getCompoundTagAt(i));
                transit.put(nextId++, stack);
            }
        }
    }

    public void update() {
        if (world().isRemote) {
            for (TransporterStack stack : transit.values()) {
                stack.progress = Math.min(100, stack.progress + getTileEntity().tier.speed);
            }
        } else {
            if (getTransmitterNetwork() == null) {
                return;
            }

            Set<Integer> deletes = new HashSet<>();

            getTileEntity().pullItems();

            for (Map.Entry<Integer, TransporterStack> entry : transit.entrySet()) {
                int stackId = entry.getKey();
                TransporterStack stack = entry.getValue();

                if (!stack.initiatedPath) {
                    if (stack.itemStack.isEmpty() || !recalculate(stackId, stack, null)) {
                        deletes.add(stackId);
                        continue;
                    }
                }

                stack.progress += getTileEntity().tier.speed;

                if (stack.progress >= 100) {
                    Coord4D prevSet = null;

                    if (stack.hasPath()) {
                        int currentIndex = stack.getPath().indexOf(coord());

                        if (currentIndex == 0) //Necessary for transition reasons, not sure why
                        {
                            deletes.add(stackId);
                            continue;
                        }

                        Coord4D next = stack.getPath().get(currentIndex - 1);

                        if (!stack.isFinal(this)) {
                            if (next != null && stack.canInsertToTransporter(next.getTileEntity(world()),
                                  stack.getSide(this))) {
                                ILogisticalTransporter nextTile = CapabilityUtils
                                      .getCapability(next.getTileEntity(world()),
                                            Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, null);
                                nextTile.entityEntering(stack, stack.progress % 100);
                                deletes.add(stackId);
                                continue;

                            } else if (next != null) {
                                prevSet = next;
                            }
                        } else if (stack.pathType != Path.NONE) {
                            TileEntity tile = next.getTileEntity(world());

                            if (tile != null) {
                                TransitResponse response = InventoryUtils
                                      .putStackInInventory(tile, TransitRequest.getFromTransport(stack),
                                            stack.getSide(this), stack.pathType == Path.HOME);

                                // Nothing was rejected; remove the stack from the prediction tracker and
                                // schedule this stack for deletion. Continue the loop thereafter
                                if (response.getRejected(stack.itemStack).isEmpty()) {
                                    TransporterManager.remove(stack);
                                    deletes.add(stackId);
                                    continue;
                                } else {
                                    // Some portion of the stack got rejected; save the remainder and
                                    // let the recalculate below sort out what to do next
                                    stack.itemStack = response.getRejected(stack.itemStack);
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
                    if (stack.isFinal(this)) {
                        if (checkPath(stack, Path.DEST, false) || checkPath(stack, Path.HOME, true)
                              || stack.pathType == Path.NONE) {
                            if (!recalculate(stackId, stack, null)) {
                                deletes.add(stackId);
                            }
                        }
                    } else {
                        TileEntity next = stack.getNext(this).getTileEntity(world());
                        if (!stack.canInsertToTransporter(next, stack.getSide(this))) {
                            if (!recalculate(stackId, stack, null)) {
                                deletes.add(stackId);
                            }
                        }
                    }
                }
            }

            if (deletes.size() > 0 || needsSync.size() > 0) {
                TileEntityMessage msg = new TileEntityMessage(coord(),
                      getTileEntity().makeBatchPacket(needsSync, deletes));

                // Now remove any entries from transit that have been deleted
                deletes.forEach(id -> transit.remove(id));

                // Clear the pending sync packets
                needsSync.clear();

                // Finally, notify clients and mark chunk for save
                Mekanism.packetHandler.sendToReceivers(msg, new Range4D(coord()));
                MekanismUtils.saveChunk(getTileEntity());
            }
        }
    }

    private boolean checkPath(TransporterStack stack, Path dest, boolean home) {
        return stack.pathType == dest && (!checkSideForInsert(stack) || !InventoryUtils
              .canInsert(stack.getDest().getTileEntity(world()), stack.color, stack.itemStack,
                    stack.getSide(this), home));
    }

    private boolean checkSideForInsert(TransporterStack stack) {
        EnumFacing side = stack.getSide(this);

        return getTileEntity().getConnectionType(side) == ConnectionType.NORMAL
              || getTileEntity().getConnectionType(side) == ConnectionType.PUSH;
    }

    private boolean recalculate(int stackId, TransporterStack stack, Coord4D from) {
        if (stack.pathType != Path.NONE) {
            TransitResponse ret = stack.recalculatePath(TransitRequest.getFromTransport(stack), this, 0);

            if (ret.isEmpty()) {
                if (!stack.calculateIdle(this)) {
                    TransporterUtils.drop(this, stack);
                    return false;
                }
            }
        } else {
            if (!stack.calculateIdle(this)) {
                TransporterUtils.drop(this, stack);
                return false;
            }
        }

        //Only add to needsSync if true is being returned; otherwise it gets added to deletes
        needsSync.put(stackId, stack);
        if (from != null) {
            stack.originalLocation = from;
        }

        return true;
    }

    @Override
    public TransitResponse insert(Coord4D original, TransitRequest request, EnumColor color, boolean doEmit, int min) {
        return insert_do(original, request, color, doEmit, min, false);
    }

    private TransitResponse insert_do(Coord4D original, TransitRequest request, EnumColor color, boolean doEmit,
          int min, boolean force) {
        EnumFacing from = coord().sideDifference(original).getOpposite();

        TransporterStack stack = new TransporterStack();
        stack.originalLocation = original;
        stack.homeLocation = original;
        stack.color = color;

        if ((force && !canReceiveFrom(original.getTileEntity(world()), from)) || !stack
              .canInsertToTransporter(this, from)) {
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
                Mekanism.packetHandler
                      .sendToReceivers(new TileEntityMessage(coord(), getTileEntity().makeSyncPacket(stackId, stack)),
                            new Range4D(coord()));
                MekanismUtils.saveChunk(getTileEntity());
            }

            return response;
        }

        return TransitResponse.EMPTY;
    }

    @Override
    public TransitResponse insertRR(TileEntityLogisticalSorter outputter, TransitRequest request, EnumColor color,
          boolean doEmit, int min) {
        EnumFacing from = coord().sideDifference(Coord4D.get(outputter)).getOpposite();

        TransporterStack stack = new TransporterStack();
        stack.originalLocation = Coord4D.get(outputter);
        stack.homeLocation = Coord4D.get(outputter);
        stack.color = color;

        if (!canReceiveFrom(outputter, from) || !stack.canInsertToTransporter(this, from)) {
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
    public boolean canEmitTo(TileEntity tileEntity, EnumFacing side) {
        if (!getTileEntity().canConnect(side)) {
            return false;
        }

        return getTileEntity().getConnectionType(side) == ConnectionType.NORMAL
              || getTileEntity().getConnectionType(side) == ConnectionType.PUSH;
    }

    @Override
    public boolean canReceiveFrom(TileEntity tileEntity, EnumFacing side) {
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
    public boolean canConnectMutual(EnumFacing side) {
        return getTileEntity().canConnectMutual(side);
    }

    @Override
    public boolean canConnect(EnumFacing side) {
        return getTileEntity().canConnect(side);
    }

    @Override
    public TileEntityLogisticalTransporter getTileEntity() {
        return (TileEntityLogisticalTransporter) containingTile;
    }
}
