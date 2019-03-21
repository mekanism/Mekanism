package mekanism.common.content.transporter;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.common.PacketHandler;
import mekanism.common.base.ILogisticalTransporter;
import mekanism.api.TileNetworkList;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.transporter.TransitRequest.TransitResponse;
import mekanism.common.content.transporter.TransporterPathfinder.Destination;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.tuple.Pair;

public class TransporterStack {

    public ItemStack itemStack = ItemStack.EMPTY;

    public int progress;

    public EnumColor color = null;

    public boolean initiatedPath = false;

    public EnumFacing idleDir = null;
    public Coord4D originalLocation;
    public Coord4D homeLocation;
    public Coord4D clientNext;
    public Coord4D clientPrev;
    public Path pathType;
    private List<Coord4D> pathToTarget = new ArrayList<>();

    public static TransporterStack readFromNBT(NBTTagCompound nbtTags) {
        TransporterStack stack = new TransporterStack();
        stack.read(nbtTags);

        return stack;
    }

    public static TransporterStack readFromPacket(ByteBuf dataStream) {
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
        data.add(pathType.ordinal());

        if (pathToTarget.indexOf(transporter.coord()) > 0) {
            data.add(true);
            getNext(transporter).write(data);
        } else {
            data.add(false);
        }

        getPrev(transporter).write(data);

        data.add(itemStack);
    }

    public void read(ByteBuf dataStream) {
        int c = dataStream.readInt();

        if (c != -1) {
            color = TransporterUtils.colors.get(c);
        } else {
            color = null;
        }

        progress = dataStream.readInt();
        originalLocation = Coord4D.read(dataStream);
        pathType = Path.values()[dataStream.readInt()];

        if (dataStream.readBoolean()) {
            clientNext = Coord4D.read(dataStream);
        }

        clientPrev = Coord4D.read(dataStream);

        itemStack = PacketHandler.readStack(dataStream);
    }

    public void write(NBTTagCompound nbtTags) {
        if (color != null) {
            nbtTags.setInteger("color", TransporterUtils.colors.indexOf(color));
        }

        nbtTags.setInteger("progress", progress);
        nbtTags.setTag("originalLocation", originalLocation.write(new NBTTagCompound()));

        if (idleDir != null) {
            nbtTags.setInteger("idleDir", idleDir.ordinal());
        }

        if (homeLocation != null) {
            nbtTags.setTag("homeLocation", homeLocation.write(new NBTTagCompound()));
        }

        nbtTags.setInteger("pathType", pathType.ordinal());
        itemStack.writeToNBT(nbtTags);
    }

    public void read(NBTTagCompound nbtTags) {
        if (nbtTags.hasKey("color")) {
            color = TransporterUtils.colors.get(nbtTags.getInteger("color"));
        }

        progress = nbtTags.getInteger("progress");
        originalLocation = Coord4D.read(nbtTags.getCompoundTag("originalLocation"));

        if (nbtTags.hasKey("idleDir")) {
            idleDir = EnumFacing.values()[nbtTags.getInteger("idleDir")];
        }

        if (nbtTags.hasKey("homeLocation")) {
            homeLocation = Coord4D.read(nbtTags.getCompoundTag("homeLocation"));
        }

        pathType = Path.values()[nbtTags.getInteger("pathType")];
        itemStack = InventoryUtils.loadFromNBT(nbtTags);
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
        return getPath() != null && getPath().size() >= 2;
    }

    public List<Coord4D> getPath() {
        return pathToTarget;
    }

    public TransitResponse recalculatePath(TransitRequest request, ILogisticalTransporter transporter, int min) {
        Destination newPath = TransporterPathfinder.getNewBasePath(transporter, this, request, min);

        if (newPath == null) {
            return TransitResponse.EMPTY;
        }

        idleDir = null;
        setPath(newPath.path, Path.DEST);
        initiatedPath = true;

        return newPath.response;
    }

    public TransitResponse recalculateRRPath(TransitRequest request, TileEntityLogisticalSorter outputter,
          ILogisticalTransporter transporter, int min) {
        Destination newPath = TransporterPathfinder.getNewRRPath(transporter, this, request, outputter, min);

        if (newPath == null) {
            return TransitResponse.EMPTY;
        }

        idleDir = null;
        setPath(newPath.path, Path.DEST);
        initiatedPath = true;

        return newPath.response;
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
        } else {
            return clientNext;
        }
    }

    public Coord4D getPrev(ILogisticalTransporter transporter) {
        if (!transporter.world().isRemote) {
            int index = pathToTarget.indexOf(transporter.coord()) + 1;

            if (index < pathToTarget.size()) {
                return pathToTarget.get(index);
            } else {
                return originalLocation;
            }
        } else {
            return clientPrev;
        }
    }

    public EnumFacing getSide(ILogisticalTransporter transporter) {
        if (progress < 50) {
            if (getPrev(transporter) != null) {
                return transporter.coord().sideDifference(getPrev(transporter));
            }
        } else if (progress == 50) {
            if (getNext(transporter) != null) {
                return getNext(transporter).sideDifference(transporter.coord());
            }
        } else {
            if (getNext(transporter) != null) {
                return getNext(transporter).sideDifference(transporter.coord());
            }
        }

        return EnumFacing.DOWN;
    }

    public boolean canInsertToTransporter(TileEntity tileEntity, EnumFacing from) {
        if (!(CapabilityUtils
              .hasCapability(tileEntity, Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, from.getOpposite()))) {
            return false;
        }

        ILogisticalTransporter transporter = CapabilityUtils
              .getCapability(tileEntity, Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, from.getOpposite());

        if (!CapabilityUtils.getCapability(tileEntity, Capabilities.BLOCKABLE_CONNECTION_CAPABILITY, from.getOpposite())
              .canConnectMutual(from.getOpposite())) {
            return false;
        }

        return transporter.getColor() == color || transporter.getColor() == null;
    }

    public boolean canInsertToTransporter(ILogisticalTransporter transporter, EnumFacing side) {
        if (!transporter.canConnectMutual(side)) {
            return false;
        }

        return transporter.getColor() == color || transporter.getColor() == null;
    }

    public Coord4D getDest() {
        return getPath().get(0);
    }

    public enum Path {
        DEST, HOME, NONE
    }
}
