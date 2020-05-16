package mekanism.common.content.transporter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import org.apache.commons.lang3.tuple.Pair;
import mekanism.api.Coord4D;
import mekanism.api.NBTConstants;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.transmitters.IBlockableConnection;
import mekanism.common.base.ILogisticalTransporter;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.transporter.TransitRequest.TransitResponse;
import mekanism.common.content.transporter.TransporterPathfinder.Destination;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;

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

    public static TransporterStack readFromUpdate(CompoundNBT nbtTags) {
        TransporterStack stack = new TransporterStack();
        stack.readFromUpdateTag(nbtTags);
        return stack;
    }

    public static TransporterStack readFromPacket(PacketBuffer dataStream) {
        TransporterStack stack = new TransporterStack();
        stack.read(dataStream);
        if (stack.progress == 0) {
            stack.progress = 5;
        }
        return stack;
    }

    public void write(ILogisticalTransporter transporter, PacketBuffer buf) {
        buf.writeVarInt(TransporterUtils.getColorIndex(color));
        buf.writeVarInt(progress);
        originalLocation.write(buf);
        buf.writeEnumValue(pathType);
        if (pathToTarget.indexOf(transporter.coord()) > 0) {
            buf.writeBoolean(true);
            getNext(transporter).write(buf);
        } else {
            buf.writeBoolean(false);
        }
        getPrev(transporter).write(buf);
        buf.writeItemStack(itemStack);
    }

    public void read(PacketBuffer dataStream) {
        color = TransporterUtils.readColor(dataStream.readVarInt());
        progress = dataStream.readVarInt();
        originalLocation = Coord4D.read(dataStream);
        pathType = dataStream.readEnumValue(Path.class);

        if (dataStream.readBoolean()) {
            clientNext = Coord4D.read(dataStream);
        }
        clientPrev = Coord4D.read(dataStream);
        itemStack = dataStream.readItemStack();
    }

    public void writeToUpdateTag(ILogisticalTransporter transporter, CompoundNBT updateTag) {
        updateTag.putInt(NBTConstants.COLOR, TransporterUtils.getColorIndex(color));
        updateTag.putInt(NBTConstants.PROGRESS, progress);
        updateTag.put(NBTConstants.ORIGINAL_LOCATION, originalLocation.write(new CompoundNBT()));
        updateTag.putInt(NBTConstants.PATH_TYPE, pathType.ordinal());
        if (pathToTarget.indexOf(transporter.coord()) > 0) {
            updateTag.put(NBTConstants.CLIENT_NEXT, getNext(transporter).write(new CompoundNBT()));
        }
        updateTag.put(NBTConstants.CLIENT_PREVIOUS, getPrev(transporter).write(new CompoundNBT()));
        itemStack.write(updateTag);
    }

    public void readFromUpdateTag(CompoundNBT updateTag) {
        NBTUtils.setEnumIfPresent(updateTag, NBTConstants.COLOR, TransporterUtils::readColor, color -> this.color = color);
        progress = updateTag.getInt(NBTConstants.PROGRESS);
        NBTUtils.setCoord4DIfPresent(updateTag, NBTConstants.ORIGINAL_LOCATION, coord -> originalLocation = coord);
        NBTUtils.setEnumIfPresent(updateTag, NBTConstants.PATH_TYPE, Path::byIndexStatic, type -> pathType = type);
        NBTUtils.setCoord4DIfPresent(updateTag, NBTConstants.CLIENT_NEXT, coord -> clientNext = coord);
        NBTUtils.setCoord4DIfPresent(updateTag, NBTConstants.CLIENT_PREVIOUS, coord -> clientPrev = coord);
        itemStack = ItemStack.read(updateTag);
    }

    public void write(CompoundNBT nbtTags) {
        nbtTags.putInt(NBTConstants.COLOR, TransporterUtils.getColorIndex(color));

        nbtTags.putInt(NBTConstants.PROGRESS, progress);
        nbtTags.put(NBTConstants.ORIGINAL_LOCATION, originalLocation.write(new CompoundNBT()));

        if (idleDir != null) {
            nbtTags.putInt(NBTConstants.IDLE_DIR, idleDir.ordinal());
        }
        if (homeLocation != null) {
            nbtTags.put(NBTConstants.HOME_LOCATION, homeLocation.write(new CompoundNBT()));
        }
        nbtTags.putInt(NBTConstants.PATH_TYPE, pathType.ordinal());
        itemStack.write(nbtTags);
    }

    public void read(CompoundNBT nbtTags) {
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.COLOR, TransporterUtils::readColor, color -> this.color = color);
        progress = nbtTags.getInt(NBTConstants.PROGRESS);
        NBTUtils.setCoord4DIfPresent(nbtTags, NBTConstants.ORIGINAL_LOCATION, coord -> originalLocation = coord);
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.IDLE_DIR, Direction::byIndex, dir -> idleDir = dir);
        NBTUtils.setCoord4DIfPresent(nbtTags, NBTConstants.HOME_LOCATION, coord -> homeLocation = coord);
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.PATH_TYPE, Path::byIndexStatic, type -> pathType = type);
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
            return request.getEmptyResponse();
        }
        idleDir = null;
        setPath(newPath.getPath(), Path.DEST);
        initiatedPath = true;
        return newPath.getResponse();
    }

    public TransitResponse recalculateRRPath(TransitRequest request, TileEntityLogisticalSorter outputter, ILogisticalTransporter transporter, int min) {
        Destination newPath = TransporterPathfinder.getNewRRPath(transporter, this, request, outputter, min);
        if (newPath == null) {
            return request.getEmptyResponse();
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

    public boolean canInsertToTransporter(ILogisticalTransporter transporter, Direction side, @Nullable TileEntity tileFrom) {
        return (transporter.getColor() == color || transporter.getColor() == null) && transporter.canConnectMutual(side, tileFrom);
    }

    public Coord4D getDest() {
        return pathToTarget.get(0);
    }

    public enum Path {
        DEST,
        HOME,
        NONE;

        private static final Path[] PATHS = values();

        public static Path byIndexStatic(int index) {
            return MathUtils.getByIndexMod(PATHS, index);
        }
    }
}