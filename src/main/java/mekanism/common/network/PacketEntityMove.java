package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import mekanism.api.Pos3D;
import mekanism.common.PacketHandler;
import mekanism.common.network.PacketEntityMove.EntityMoveMessage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketEntityMove implements IMessageHandler<EntityMoveMessage, IMessage> {

    @Override
    public IMessage onMessage(EntityMoveMessage message, MessageContext context) {
        EntityPlayer player = PacketHandler.getPlayer(context);

        PacketHandler.handlePacket(() ->
        {
            Entity entity = player.world.getEntityByID(message.entityId);

            if (entity != null) {
                entity.setLocationAndAngles(message.pos.x, message.pos.y, message.pos.z, entity.rotationYaw,
                      entity.rotationPitch);
            }
        }, player);

        return null;
    }

    public static class EntityMoveMessage implements IMessage {

        public int entityId;

        public Pos3D pos;

        public EntityMoveMessage() {
        }

        public EntityMoveMessage(Entity e) {
            entityId = e.getEntityId();
            pos = new Pos3D(e);
        }

        @Override
        public void toBytes(ByteBuf dataStream) {
            dataStream.writeInt(entityId);

            dataStream.writeFloat((float) pos.x);
            dataStream.writeFloat((float) pos.y);
            dataStream.writeFloat((float) pos.z);
        }

        @Override
        public void fromBytes(ByteBuf dataStream) {
            entityId = dataStream.readInt();

            pos = new Pos3D(dataStream.readFloat(), dataStream.readFloat(), dataStream.readFloat());
        }
    }
}
