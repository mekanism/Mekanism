package mekanism.api.transmitters;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;
import mekanism.api.Coord4D;
import mekanism.api.IClientTicker;
import mekanism.api.Range4D;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.apache.commons.lang3.tuple.Pair;

public abstract class DynamicNetwork<ACCEPTOR, NETWORK extends DynamicNetwork<ACCEPTOR, NETWORK>> implements IClientTicker, INetworkDataHandler {

    public LinkedHashSet<IGridTransmitter<ACCEPTOR, NETWORK>> transmitters = Sets.newLinkedHashSet();
    public LinkedHashSet<IGridTransmitter<ACCEPTOR, NETWORK>> transmittersToAdd = Sets.newLinkedHashSet();
    public LinkedHashSet<IGridTransmitter<ACCEPTOR, NETWORK>> transmittersAdded = Sets.newLinkedHashSet();

    public HashMap<Coord4D, ACCEPTOR> possibleAcceptors = new HashMap<>();
    public HashMap<Coord4D, EnumSet<EnumFacing>> acceptorDirections = new HashMap<>();
    public HashMap<IGridTransmitter<ACCEPTOR, NETWORK>, EnumSet<EnumFacing>> changedAcceptors = Maps.newHashMap();
    protected Range4D packetRange = null;
    protected int capacity = 0;
    protected double meanCapacity = 0;
    protected boolean needsUpdate = false;
    protected int updateDelay = 0;
    protected boolean firstUpdate = true;
    protected World world = null;
    private Set<DelayQueue> updateQueue = new LinkedHashSet<>();

    public void addNewTransmitters(Collection<IGridTransmitter<ACCEPTOR, NETWORK>> newTransmitters) {
        transmittersToAdd.addAll(newTransmitters);
    }

    public void commit() {
        if (!transmittersToAdd.isEmpty()) {
            for (IGridTransmitter<ACCEPTOR, NETWORK> transmitter : transmittersToAdd) {
                if (transmitter.isValid()) {
                    if (world == null) {
                        world = transmitter.world();
                    }

                    for (EnumFacing side : EnumFacing.VALUES) {
                        updateTransmitterOnSide(transmitter, side);
                    }

                    transmitter.setTransmitterNetwork((NETWORK) this);
                    absorbBuffer(transmitter);
                    transmitters.add(transmitter);
                }
            }

            updateCapacity();
            clampBuffer();
            queueClientUpdate(Lists.newArrayList(transmittersToAdd));
            transmittersToAdd.clear();
        }

        if (!changedAcceptors.isEmpty()) {
            for (Entry<IGridTransmitter<ACCEPTOR, NETWORK>, EnumSet<EnumFacing>> entry : changedAcceptors.entrySet()) {
                IGridTransmitter<ACCEPTOR, NETWORK> transmitter = entry.getKey();

                if (transmitter.isValid()) {
                    EnumSet<EnumFacing> directionsChanged = entry.getValue();

                    for (EnumFacing side : directionsChanged) {
                        updateTransmitterOnSide(transmitter, side);
                    }
                }
            }

            changedAcceptors.clear();
        }
    }

    public void updateTransmitterOnSide(IGridTransmitter<ACCEPTOR, NETWORK> transmitter, EnumFacing side) {
        ACCEPTOR acceptor = transmitter.getAcceptor(side);
        Coord4D acceptorCoord = transmitter.coord().offset(side);
        EnumSet<EnumFacing> directions = acceptorDirections.get(acceptorCoord);

        if (acceptor != null) {
            possibleAcceptors.put(acceptorCoord, acceptor);

            if (directions != null) {
                directions.add(side.getOpposite());
            } else {
                acceptorDirections.put(acceptorCoord, EnumSet.of(side.getOpposite()));
            }
        } else {
            if (directions != null) {
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

    }

    public abstract void absorbBuffer(IGridTransmitter<ACCEPTOR, NETWORK> transmitter);

    public abstract void clampBuffer();

    public void invalidate() {
        //Remove invalid transmitters first for share calculations
        transmitters.removeIf(transmitter -> !transmitter.isValid());

        //Clamp the new buffer
        clampBuffer();

        //Update all shares
        for (IGridTransmitter<ACCEPTOR, NETWORK> transmitter : transmitters) {
            transmitter.updateShare();
        }

        //Now invalidate the transmitters
        for (IGridTransmitter<ACCEPTOR, NETWORK> transmitter : transmitters) {
            invalidateTransmitter(transmitter);
        }

        transmitters.clear();
        deregister();
    }

    public void invalidateTransmitter(IGridTransmitter<ACCEPTOR, NETWORK> transmitter) {
        if (!world.isRemote && transmitter.isValid()) {
            transmitter.takeShare();
            transmitter.setTransmitterNetwork(null);
            TransmitterNetworkRegistry.registerOrphanTransmitter(transmitter);
        }
    }

    public void acceptorChanged(IGridTransmitter<ACCEPTOR, NETWORK> transmitter, EnumFacing side) {
        EnumSet<EnumFacing> directions = changedAcceptors.get(transmitter);

        if (directions != null) {
            directions.add(side);
        } else {
            changedAcceptors.put(transmitter, EnumSet.of(side));
        }

        TransmitterNetworkRegistry.registerChangedNetwork(this);
    }

    public void adoptTransmittersAndAcceptorsFrom(NETWORK net) {
        for (IGridTransmitter<ACCEPTOR, NETWORK> transmitter : net.transmitters) {
            transmitter.setTransmitterNetwork((NETWORK) this);
            transmitters.add(transmitter);
            transmittersAdded.add(transmitter);
        }

        transmittersToAdd.addAll(net.transmittersToAdd);

        possibleAcceptors.putAll(net.possibleAcceptors);

        for (Entry<Coord4D, EnumSet<EnumFacing>> entry : net.acceptorDirections.entrySet()) {
            Coord4D coord = entry.getKey();

            if (acceptorDirections.containsKey(coord)) {
                acceptorDirections.get(coord).addAll(entry.getValue());
            } else {
                acceptorDirections.put(coord, entry.getValue());
            }
        }

    }

    public Range4D getPacketRange() {
        if (packetRange == null) {
            return genPacketRange();
        }

        return packetRange;
    }

    protected Range4D genPacketRange() {
        if (getSize() == 0) {
            deregister();
            return null;
        }

        IGridTransmitter<ACCEPTOR, NETWORK> initTransmitter = transmitters.iterator().next();
        Coord4D initCoord = initTransmitter.coord();

        int minX = initCoord.x;
        int minY = initCoord.y;
        int minZ = initCoord.z;
        int maxX = initCoord.x;
        int maxY = initCoord.y;
        int maxZ = initCoord.z;

        for (IGridTransmitter transmitter : transmitters) {
            Coord4D coord = transmitter.coord();

            if (coord.x < minX) {
                minX = coord.x;
            }
            if (coord.y < minY) {
                minY = coord.y;
            }
            if (coord.z < minZ) {
                minZ = coord.z;
            }
            if (coord.x > maxX) {
                maxX = coord.x;
            }
            if (coord.y > maxY) {
                maxY = coord.y;
            }
            if (coord.x > maxZ) {
                maxZ = coord.z;
            }
        }

        return new Range4D(minX, minY, minZ, maxX, maxY, maxZ, initTransmitter.world().provider.getDimension());
    }

    public void register() {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            TransmitterNetworkRegistry.getInstance().registerNetwork(this);
        } else {
            MinecraftForge.EVENT_BUS.post(new ClientTickUpdate(this, (byte) 1));
        }
    }

    public void deregister() {
        transmitters.clear();
        transmittersToAdd.clear();
        transmittersAdded.clear();

        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            TransmitterNetworkRegistry.getInstance().removeNetwork(this);
        } else {
            MinecraftForge.EVENT_BUS.post(new ClientTickUpdate(this, (byte) 0));
        }
    }

    public int getSize() {
        return transmitters.size();
    }

    public int getAcceptorSize() {
        return possibleAcceptors.size();
    }

    public synchronized void updateCapacity() {
        updateMeanCapacity();
        capacity = (int) meanCapacity * transmitters.size();
    }

    /**
     * Override this if things can have variable capacity along the network. An 'average' value of capacity. Calculate
     * it how you will.
     */
    protected synchronized void updateMeanCapacity() {
        if (transmitters.size() > 0) {
            meanCapacity = transmitters.iterator().next().getCapacity();
        } else {
            meanCapacity = 0;
        }
    }

    public int getCapacity() {
        return capacity;
    }

    public World getWorld() {
        return world;
    }

    public abstract Set<Pair<Coord4D, ACCEPTOR>> getAcceptors(Object data);

    public void tick() {
        onUpdate();
    }

    public void onUpdate() {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            Iterator<DelayQueue> i = updateQueue.iterator();

            try {
                while (i.hasNext()) {
                    DelayQueue q = i.next();

                    if (q.delay > 0) {
                        q.delay--;
                    } else {
                        transmittersAdded.addAll(transmitters);
                        updateDelay = 1;
                        i.remove();
                    }
                }
            } catch (Exception ignored) {
            }

            if (updateDelay > 0) {
                updateDelay--;

                if (updateDelay == 0) {
                    MinecraftForge.EVENT_BUS
                          .post(new TransmittersAddedEvent(this, firstUpdate, (Collection) transmittersAdded));
                    firstUpdate = false;
                    transmittersAdded.clear();
                    needsUpdate = true;
                }
            }
        }
    }

    @Override
    public boolean needsTicks() {
        return getSize() > 0;
    }

    @Override
    public void clientTick() {
    }

    public void queueClientUpdate(Collection<IGridTransmitter<ACCEPTOR, NETWORK>> newTransmitters) {
        transmittersAdded.addAll(newTransmitters);
        updateDelay = 5;
    }

    public void addUpdate(EntityPlayer player) {
        updateQueue.add(new DelayQueue(player));
    }

    public boolean isCompatibleWith(NETWORK other) {
        return true;
    }

    public boolean compatibleWithBuffer(Object buffer) {
        return true;
    }

    public static class TransmittersAddedEvent extends Event {

        public DynamicNetwork<?, ?> network;
        public boolean newNetwork;
        public Collection<IGridTransmitter> newTransmitters;

        public TransmittersAddedEvent(DynamicNetwork net, boolean newNet, Collection<IGridTransmitter> added) {
            network = net;
            newNetwork = newNet;
            newTransmitters = added;
        }
    }

    public static class ClientTickUpdate extends Event {

        public DynamicNetwork network;
        public byte operation; /*0 remove, 1 add*/

        public ClientTickUpdate(DynamicNetwork net, byte b) {
            network = net;
            operation = b;
        }
    }

    public static class NetworkClientRequest extends Event {

        public TileEntity tileEntity;

        public NetworkClientRequest(TileEntity tile) {
            tileEntity = tile;
        }
    }

    public static class DelayQueue {

        public EntityPlayer player;
        public int delay;

        public DelayQueue(EntityPlayer p) {
            player = p;
            delay = 5;
        }

        @Override
        public int hashCode() {
            return player.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof DelayQueue && ((DelayQueue) o).player.equals(this.player);
        }
    }
}
