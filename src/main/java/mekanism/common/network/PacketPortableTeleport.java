package mekanism.common.network;

import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.ObfuscatedNames;
import mekanism.common.Teleporter;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import cpw.mods.fml.common.FMLCommonHandler;

public class PacketPortableTeleport extends MekanismPacket
{
	@Override
	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception 
	{
		ItemStack itemstack = player.getCurrentEquippedItem();
		
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
						
						((EntityPlayerMP)player).playerNetServerHandler.setPlayerLocation(coords.xCoord+0.5, coords.yCoord+1, coords.zCoord+0.5, player.rotationYaw, player.rotationPitch);
						
						world.playSoundAtEntity(player, "mob.endermen.portal", 1.0F, 1.0F);
						Mekanism.packetPipeline.sendToAllAround(new PacketPortalFX(coords), coords.getTargetPoint(40D));
					} catch(Exception e) {}
				}
			}
		}
	}

	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
	{

	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
	{

	}

	@Override
	public void handleClientSide(EntityPlayer player)
	{

	}

	@Override
	public void handleServerSide(EntityPlayer player)
	{

	}
}
