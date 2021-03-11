package mekanism.common.network.to_client;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import mekanism.common.content.network.transmitter.DiversionTransporter;
import mekanism.common.content.network.transmitter.DiversionTransporter.DiversionControl;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporterBase;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

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
            if (isDiversion && transporter instanceof DiversionTransporter) {
                //Copy the values of modes, without replacing the actual array
                System.arraycopy(modes, 0, ((DiversionTransporter) transporter).modes, 0, modes.length);
            }
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeBoolean(isSync);
        buffer.writeBoolean(isDiversion);
        if (isSync) {
            //Sync
            buffer.writeVarInt(stackId);
            stack.write(transporter, buffer);
        } else {
            //Batch
            buffer.writeVarInt(updates.size());
            for (Int2ObjectMap.Entry<TransporterStack> entry : updates.int2ObjectEntrySet()) {
                buffer.writeVarInt(entry.getIntKey());
                entry.getValue().write(transporter, buffer);
            }
            buffer.writeVarInt(deletes.size());
            for (int toDelete : deletes) {
                buffer.writeVarInt(toDelete);
            }
        }
        if (isDiversion) {
            for (DiversionControl mode : modes) {
                buffer.writeEnum(mode);
            }
        }
    }

    public static PacketTransporterUpdate decode(PacketBuffer buffer) {
        PacketTransporterUpdate packet = new PacketTransporterUpdate(buffer.readBlockPos(), buffer.readBoolean(), buffer.readBoolean());
        if (packet.isSync) {
            //Sync
            packet.stackId = buffer.readVarInt();
            packet.stack = TransporterStack.readFromPacket(buffer);
        } else {
            //Batch
            int updatesSize = buffer.readVarInt();
            packet.updates = new Int2ObjectOpenHashMap<>(updatesSize);
            for (int i = 0; i < updatesSize; i++) {
                packet.updates.put(buffer.readVarInt(), TransporterStack.readFromPacket(buffer));
            }
            int deletesSize = buffer.readVarInt();
            packet.deletes = new IntOpenHashSet(deletesSize);
            for (int i = 0; i < deletesSize; i++) {
                packet.deletes.add(buffer.readVarInt());
            }
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