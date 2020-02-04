package mekanism.common.content.transporter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.TileNetworkList;
import mekanism.api.text.EnumColor;
import mekanism.api.transmitters.IBlockableConnection;
import mekanism.common.base.ILogisticalTransporter;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.transporter.TransitRequest.TransitResponse;
import mekanism.common.content.transporter.TransporterPathfinder.Destination;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import org.apache.commons.lang3.tuple.Pair;

public class TransporterStack {

    public ItemStack itemStack = ItemStack.EMPTY;

    public int progress;

    public EnumColor color = null;

    public boolean initiatedPath = false;

    public Direction idleDir = null;
    public Coord4D originalLocation;
    public Coord4D homeLocation;
    private Coord4D clientNext;
    private Coord4D clientPrev;
    private Path pathType;
    private List<Coord4D> pathToTarget = new ArrayList<>();

    public static TransporterStack readFromNBT(CompoundNBT nbtTags) {
        TransporterStack stack = new TransporterStack();
        stack.read(nbtTags);
        return stack;
    }

    public static TransporterStack readFromPacket(PacketBuffer dataStream) {
        TransporterStack stack = new TransporterStack();
        stack.read(dataStream);
        return stack;
    }

    public void write(ILogisticalTransporter transporter, TileNetworkList data) {
        if (color != null) {
            data.add(TransporterUtils.colors.indexOf(color));
        } else {
            data.add(-1);
        }

        data.add(progress);
        originalLocation.write(data);
        data.add(pathType);

        if (pathToTarget.indexOf(transporter.coord()) > 0) {
            data.add(true);
            getNext(transporter).write(data);
        } else {
            data.add(false);
        }

        getPrev(transporter).write(data);
        data.add(itemStack);
    }

    public void read(PacketBuffer dataStream) {
        int c = dataStream.readInt();
        if (c != -1) {
            color = TransporterUtils.colors.get(c);
        } else {
            color = null;
        }

        progress = dataStream.readInt();
        originalLocation = Coord4D.read(dataStream);
        pathType = dataStream.readEnumValue(Path.class);

        if (dataStream.readBoolean()) {
            clientNext = Coord4D.read(dataStream);
        }
        clientPrev = Coord4D.read(dataStream);
        itemStack = dataStream.readItemStack();
    }

    public void write(CompoundNBT nbtTags) {
        if (color != null) {
            nbtTags.putInt("color", TransporterUtils.colors.indexOf(color));
        }

        nbtTags.putInt("progress", progress);
        nbtTags.put("originalLocation", originalLocation.write(new CompoundNBT()));

        if (idleDir != null) {
            nbtTags.putInt("idleDir", idleDir.ordinal());
        }
        if (homeLocation != null) {
            nbtTags.put("homeLocation", homeLocation.write(new CompoundNBT()));
        }
        nbtTags.putInt("pathType", pathType.ordinal());
        itemStack.write(nbtTags);
    }

    public void read(CompoundNBT nbtTags) {
        if (nbtTags.contains("color")) {
            color = TransporterUtils.colors.get(nbtTags.getInt("color"));
        }

        progress = nbtTags.getInt("progress");
        originalLocation = Coord4D.read(nbtTags.getCompound("originalLocation"));

        if (nbtTags.contains("idleDir")) {
            idleDir = Direction.byIndex(nbtTags.getInt("idleDir"));
        }
        if (nbtTags.contains("homeLocation")) {
            homeLocation = Coord4D.read(nbtTags.getCompound("homeLocation"));
        }
        pathType = Path.values()[nbtTags.getInt("pathType")];
        itemStack = ItemStack.read(nbtTags);
    }

    public void setPath(List<Coord4D> path, Path type) {
        //Make sure old path isn't null
        if (pathType != Path.NONE) {
            TransporterManager.remove(this);
        }
        pathToTarget = path;
        pathType = type;
        if (pathType != Path.NONE) {
            TransporterManager.add(this);
        }
    }

    public boolean hasPath() {
        return pathToTarget != null && pathToTarget.size() >= 2;
    }

    public List<Coord4D> getPath() {
        return pathToTarget;
    }

    public Path getPathType() {
        return pathType;
    }

    public TransitResponse recalculatePath(TransitRequest request, ILogisticalTransporter transporter, int min) {
        Destination newPath = TransporterPathfinder.getNewBasePath(transporter, this, request, min);
        if (newPath == null) {
            return TransitResponse.EMPTY;
        }
        idleDir = null;
        setPath(newPath.getPath(), Path.DEST);
        initiatedPath = true;
        return newPath.getResponse();
    }

    public TransitResponse recalculateRRPath(TransitRequest request, TileEntityLogisticalSorter outputter, ILogisticalTransporter transporter, int min) {
        Destination newPath = TransporterPathfinder.getNewRRPath(transporter, this, request, outputter, min);
        if (newPath == null) {
            return TransitResponse.EMPTY;
        }
        idleDir = null;
        setPath(newPath.getPath(), Path.DEST);
        initiatedPath = true;
        return newPath.getResponse();
    }

    public boolean calculateIdle(ILogisticalTransporter transporter) {
        Pair<List<Coord4D>, Path> newPath = TransporterPathfinder.getIdlePath(transporter, this);
        if (newPath == null) {
            return false;
        }
        if (newPath.getRight() == Path.HOME) {
            idleDir = null;
        }
        setPath(newPath.getLeft(), newPath.getRight());
        originalLocation = transporter.coord();
        initiatedPath = true;
        return true;
    }

    public boolean isFinal(ILogisticalTransporter transporter) {
        return pathToTarget.indexOf(transporter.coord()) == (pathType == Path.NONE ? 0 : 1);
    }

    public Coord4D getNext(ILogisticalTransporter transporter) {
        if (!transporter.world().isRemote) {
            int index = pathToTarget.indexOf(transporter.coord()) - 1;
            if (index < 0) {
                return null;
            }
            return pathToTarget.get(index);
        }
        return clientNext;
    }

    public Coord4D getPrev(ILogisticalTransporter transporter) {
        if (!transporter.world().isRemote) {
            int index = pathToTarget.indexOf(transporter.coord()) + 1;
            if (index < pathToTarget.size()) {
                return pathToTarget.get(index);
            }
            return originalLocation;
        }
        return clientPrev;
    }

    public Direction getSide(ILogisticalTransporter transporter) {
        Direction side = null;
        if (progress < 50) {
            Coord4D prev = getPrev(transporter);
            if (prev != null) {
                side = transporter.coord().sideDifference(prev);
            }
        } else {
            Coord4D next = getNext(transporter);
            if (next != null) {
                side = next.sideDifference(transporter.coord());
            }
        }
        //sideDifference can return null
        //TODO: Look into implications further about what side should be returned.
        // This is mainly to stop a crash I randomly encountered but was unable to reproduce.
        // (I believe the difference returns null when it is the "same" transporter somehow or something)
        return side == null ? Direction.DOWN : side;
    }

    public boolean canInsertToTransporter(TileEntity tile, Direction from, @Nullable TileEntity tileFrom) {
        Direction opposite = from.getOpposite();
        Optional<ILogisticalTransporter> transporterCap = MekanismUtils.toOptional(CapabilityUtils.getCapability(tile, Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, opposite));
        if (transporterCap.isPresent()) {
            ILogisticalTransporter transporter = transporterCap.get();
            if (transporter.getColor() == null || transporter.getColor() == color) {
                //If the color is valid, make sure that the connection is not blocked
                Optional<IBlockableConnection> blockableConnection = MekanismUtils.toOptional(CapabilityUtils.getCapability(tile, Capabilities.BLOCKABLE_CONNECTION_CAPABILITY, opposite));
                if (blockableConnection.isPresent()) {
                    return blockableConnection.get().canConnectMutual(opposite, tileFrom);
                }
            }
        }
        return false;
    }

    public boolean canInsertToTransporter(ILogisticalTransporter transporter, Direction side) {
        //TODO: Check if we ever have a tile already gotten that we can pass as cached
        return (transporter.getColor() == color || transporter.getColor() == null) && transporter.canConnectMutual(side, null);
    }

    public Coord4D getDest() {
        return pathToTarget.get(0);
    }

    public enum Path {
        DEST,
        HOME,
        NONE
    }
}