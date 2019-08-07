package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.api.Pos3D;
import mekanism.common.PacketHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketEntityMove {

    private int entityId;
    private Pos3D pos;

    public PacketEntityMove(Entity e) {
        entityId = e.getEntityId();
        pos = new Pos3D(e);
    }

    private PacketEntityMove(int entityId, Pos3D pos) {
        this.entityId = entityId;
        this.pos = pos;
    }

    public static void handle(PacketEntityMove message, Supplier<Context> context) {
        PlayerEntity player = PacketHandler.getPlayer(context);
        PacketHandler.handlePacket(() -> {
            Entity entity = player.world.getEntityByID(message.entityId);
            if (entity != null) {
                entity.setLocationAndAngles(message.pos.x, message.pos.y, message.pos.z, entity.rotationYaw, entity.rotationPitch);
            }
        }, player);
    }

    public static void encode(PacketEntityMove pkt, PacketBuffer buf) {
        buf.writeInt(pkt.entityId);
        buf.writeFloat((float) pkt.pos.x);
        buf.writeFloat((float) pkt.pos.y);
        buf.writeFloat((float) pkt.pos.z);
    }

    public static PacketEntityMove decode(PacketBuffer buf) {
        int entityId = buf.readInt();
        Pos3D pos = new Pos3D(buf.readFloat(), buf.readFloat(), buf.readFloat());
        return new PacketEntityMove(entityId, pos);
    }
}