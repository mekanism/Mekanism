package mekanism.common.network;

import mekanism.api.Coord4D;
import mekanism.api.Range4D;
import mekanism.common.Mekanism;
import mekanism.common.ObfuscatedNames;
import mekanism.common.PacketHandler;
import mekanism.common.Teleporter;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.network.PacketPortableTeleport.PortableTeleportMessage;
import mekanism.common.network.PacketPortalFX.PortalFXMessage;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.util.MekanismUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import io.netty.buffer.ByteBuf;

public class PacketPortableTeleport implements IMessageHandler<PortableTeleportMessage, IMessage>
{
	@Override
	public IMessage onMessage(PortableTeleportMessage message, MessageContext context) 
	{
		EntityPlayer player = PacketHandler.getPlayer(context);
		ItemStack itemstack = player.getCurrentEquippedItem();
		World world = player.worldObj;
		
		if(itemstack != null && itemstack.getItem() instanceof ItemPortableTeleporter)
		{
			ItemPortableTeleporter item = (ItemPortableTeleporter)itemstack.getItem();
			
			if(item.getStatus(itemstack) == 1)
			{
				Coord4D coords = MekanismUtils.getClosestCoords(new Teleporter.Code(item.getDigit(itemstack, 0), item.getDigit(itemstack, 1), item.getDigit(itemstack, 2), item.getDigit(itemstack, 3)), player);
				
				World teleWorld = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(coords.dimensionId);
				TileEntityTeleporter teleporter = (TileEntityTeleporter)coords.getTileEntity(teleWorld);
				
				if(teleporter != null)
				{
					try {
						teleporter.didTeleport.add(player);
						teleporter.teleDelay = 5;
						
						item.setEnergy(itemstack, item.getEnergy(itemstack) - item.calculateEnergyCost(player, coords));
						
						if(player instanceof EntityPlayerMP)
						{
							MekanismUtils.setPrivateValue(((EntityPlayerMP)player).playerNetServerHandler, 0, NetHandlerPlayServer.class, ObfuscatedNames.NetHandlerPlayServer_floatingTickCount);
						}
						
						if(world.provider.dimensionId != coords.dimensionId)
						{
							((EntityPlayerMP)player).travelToDimension(coords.dimensionId);
						}
						
						((EntityPlayerMP)player).playerNetServerHandler.setPlayerLocation(coords.getPos().getX()+0.5, coords.getPos().getY()+1, coords.getPos().getZ()+0.5, player.rotationYaw, player.rotationPitch);
						
						world.playSoundAtEntity(player, "mob.endermen.portal", 1.0F, 1.0F);
						Mekanism.packetHandler.sendToReceivers(new PortalFXMessage(coords), new Range4D(coords));
					} catch(Exception e) {}
				}
			}
		}
		
		return null;
	}
	
	public static class PortableTeleportMessage implements IMessage
	{
		public PortableTeleportMessage() {}
		
		@Override
		public void toBytes(ByteBuf buffer)
		{
	
		}
	
		@Override
		public void fromBytes(ByteBuf buffer)
		{
	
		}
	}
}
