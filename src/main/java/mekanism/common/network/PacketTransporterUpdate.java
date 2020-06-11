package mekanism.common.network;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.function.Supplier;
import mekanism.common.content.network.transmitter.DiversionTransporter;
import mekanism.common.content.network.transmitter.DiversionTransporter.DiversionControl;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporterBase;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketTransporterUpdate {

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

    public static void handle(PacketTransporterUpdate message, Supplier<Context> context) {
        PlayerEntity player = BasePacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            TileEntityLogisticalTransporterBase tile = MekanismUtils.getTileEntity(TileEntityLogisticalTransporterBase.class, player.world, message.pos);
            if (tile != null) {
                LogisticalTransporterBase transporter = tile.getTransmitter();
                if (message.isSync) {
                    transporter.addStack(message.stackId, message.stack);
                } else {
                    for (Int2ObjectMap.Entry<TransporterStack> entry : message.updates.int2ObjectEntrySet()) {
                        transporter.addStack(entry.getIntKey(), entry.getValue());
                    }
                    for (int toDelete : message.deletes) {
                        transporter.deleteStack(toDelete);
                    }
                }
                if (message.isDiversion && transporter instanceof DiversionTransporter) {
                    //Copy the values of modes, without replacing the actual array
                    System.arraycopy(message.modes, 0, ((DiversionTransporter) transporter).modes, 0, message.modes.length);
                }
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketTransporterUpdate pkt, PacketBuffer buf) {
        buf.writeBlockPos(pkt.pos);
        buf.writeBoolean(pkt.isSync);
        buf.writeBoolean(pkt.isDiversion);
        if (pkt.isSync) {
            //Sync
            buf.writeVarInt(pkt.stackId);
            pkt.stack.write(pkt.transporter, buf);
        } else {
            //Batch
            buf.writeVarInt(pkt.updates.size());
            for (Int2ObjectMap.Entry<TransporterStack> entry : pkt.updates.int2ObjectEntrySet()) {
                buf.writeVarInt(entry.getIntKey());
                entry.getValue().write(pkt.transporter, buf);
            }
            buf.writeVarInt(pkt.deletes.size());
            for (int toDelete : pkt.deletes) {
                buf.writeVarInt(toDelete);
            }
        }
        if (pkt.isDiversion) {
            for (int i = 0; i < pkt.modes.length; i++) {
                buf.writeEnumValue(pkt.modes[i]);
            }
        }
    }

    public static PacketTransporterUpdate decode(PacketBuffer buf) {
        PacketTransporterUpdate packet = new PacketTransporterUpdate(buf.readBlockPos(), buf.readBoolean(), buf.readBoolean());
        if (packet.isSync) {
            //Sync
            packet.stackId = buf.readVarInt();
            packet.stack = TransporterStack.readFromPacket(buf);
        } else {
            //Batch
            int updatesSize = buf.readVarInt();
            packet.updates = new Int2ObjectOpenHashMap<>(updatesSize);
            for (int i = 0; i < updatesSize; i++) {
                packet.updates.put(buf.readVarInt(), TransporterStack.readFromPacket(buf));
            }
            int deletesSize = buf.readVarInt();
            packet.deletes = new IntOpenHashSet(deletesSize);
            for (int i = 0; i < deletesSize; i++) {
                packet.deletes.add(buf.readVarInt());
            }
        }
        if (packet.isDiversion) {
            packet.modes = new DiversionControl[EnumUtils.DIRECTIONS.length];
            for (int i = 0; i < packet.modes.length; i++) {
                packet.modes[i] = buf.readEnumValue(DiversionControl.class);
            }
        }
        return packet;
    }
}