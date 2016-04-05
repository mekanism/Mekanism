package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import mekanism.api.Pos3D;
import mekanism.common.PacketHandler;
import mekanism.common.network.PacketEntityMove.EntityMoveMessage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketEntityMove implements IMessageHandler<EntityMoveMessage, IMessage>
{
	@Override
	public IMessage onMessage(EntityMoveMessage message, MessageContext context) 
	{
		EntityPlayer player = PacketHandler.getPlayer(context);
		Entity entity = player.worldObj.getEntityByID(message.entityId);
		
		if(entity != null)
		{
			entity.setLocationAndAngles(message.pos.xPos, message.pos.yPos, message.pos.zPos, entity.rotationYaw, entity.rotationPitch);
		}
		
		return null;
	}
	
	public static class EntityMoveMessage implements IMessage
	{
		public int entityId;
		
		public Pos3D pos;
		
		public EntityMoveMessage() {}
	
		public EntityMoveMessage(Entity e)
		{
			entityId = e.getEntityId();
			pos = new Pos3D(e);
		}
	
		@Override
		public void toBytes(ByteBuf dataStream)
		{
			dataStream.writeInt(entityId);
			
			dataStream.writeFloat((float)pos.xPos);
			dataStream.writeFloat((float)pos.yPos);
			dataStream.writeFloat((float)pos.zPos);
		}
	
		@Override
		public void fromBytes(ByteBuf dataStream)
		{
			entityId = dataStream.readInt();
			
			pos = new Pos3D(dataStream.readFloat(), dataStream.readFloat(), dataStream.readFloat());
		}
	}
}
