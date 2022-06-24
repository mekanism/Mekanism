package mekanism.common.network.to_client;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import mekanism.common.content.network.transmitter.DiversionTransporter;
import mekanism.common.content.network.transmitter.DiversionTransporter.DiversionControl;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.network.BasePacketHandler;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporterBase;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class PacketTransporterUpdate implements IMekanismPacket {

    //Generic
    private final boolean isDiversion;
    private final boolean isSync;
    private final BlockPos pos;

    private LogisticalTransporterBase transporter;
    private DiversionControl[] modes;

    //Sync
    private int stackId;
    private TransporterStack stack;
    //Batch
    private Int2ObjectMap<TransporterStack> updates;
    private IntSet deletes;

    public PacketTransporterUpdate(LogisticalTransporterBase tile, int stackId, TransporterStack stack) {
        this(tile, true);
        this.stackId = stackId;
        this.stack = stack;
    }

    public PacketTransporterUpdate(LogisticalTransporterBase tile, Int2ObjectMap<TransporterStack> updates, IntSet deletes) {
        this(tile, false);
        this.updates = updates;
        this.deletes = deletes;
    }

    private PacketTransporterUpdate(LogisticalTransporterBase transporter, boolean isSync) {
        this.isSync = isSync;
        this.pos = transporter.getTilePos();
        this.isDiversion = transporter instanceof DiversionTransporter;
        if (this.isDiversion) {
            this.modes = ((DiversionTransporter) transporter).modes;
        }
        this.transporter = transporter;
    }

    private PacketTransporterUpdate(BlockPos pos, boolean isSync, boolean isDiversion) {
        this.pos = pos;
        this.isSync = isSync;
        this.isDiversion = isDiversion;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        TileEntityLogisticalTransporterBase tile = WorldUtils.getTileEntity(TileEntityLogisticalTransporterBase.class, Minecraft.getInstance().level, pos);
        if (tile != null) {
            LogisticalTransporterBase transporter = tile.getTransmitter();
            if (isSync) {
                transporter.addStack(stackId, stack);
            } else {
                for (Int2ObjectMap.Entry<TransporterStack> entry : updates.int2ObjectEntrySet()) {
                    transporter.addStack(entry.getIntKey(), entry.getValue());
                }
                for (int toDelete : deletes) {
                    transporter.deleteStack(toDelete);
                }
            }
            if (isDiversion && transporter instanceof DiversionTransporter diversionTransporter) {
                //Copy the values of modes, without replacing the actual array
                System.arraycopy(modes, 0, diversionTransporter.modes, 0, modes.length);
            }
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeBoolean(isSync);
        buffer.writeBoolean(isDiversion);
        if (isSync) {
            //Sync
            buffer.writeVarInt(stackId);
            stack.write(transporter, buffer);
        } else {
            //Batch
            BasePacketHandler.writeMap(buffer, updates, (key, value, buf) -> {
                buf.writeVarInt(key);
                value.write(transporter, buf);
            });
            buffer.writeCollection(deletes, FriendlyByteBuf::writeVarInt);
        }
        if (isDiversion) {
            //Note: Doesn't make use of read/write array as we know the size so can skip sending it
            for (DiversionControl mode : modes) {
                buffer.writeEnum(mode);
            }
        }
    }

    public static PacketTransporterUpdate decode(FriendlyByteBuf buffer) {
        PacketTransporterUpdate packet = new PacketTransporterUpdate(buffer.readBlockPos(), buffer.readBoolean(), buffer.readBoolean());
        if (packet.isSync) {
            //Sync
            packet.stackId = buffer.readVarInt();
            packet.stack = TransporterStack.readFromPacket(buffer);
        } else {
            //Batch
            packet.updates = BasePacketHandler.readMap(buffer, Int2ObjectOpenHashMap::new, FriendlyByteBuf::readVarInt, TransporterStack::readFromPacket);
            packet.deletes = buffer.readCollection(IntOpenHashSet::new, FriendlyByteBuf::readVarInt);
        }
        if (packet.isDiversion) {
            packet.modes = new DiversionControl[EnumUtils.DIRECTIONS.length];
            for (int i = 0; i < packet.modes.length; i++) {
                packet.modes[i] = buffer.readEnum(DiversionControl.class);
            }
        }
        return packet;
    }
}