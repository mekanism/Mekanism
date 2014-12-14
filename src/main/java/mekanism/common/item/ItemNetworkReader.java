package mekanism.common.item;

import java.util.HashSet;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.transmitters.ITransmitterNetwork;
import mekanism.api.transmitters.TransmitterNetworkRegistry;
import mekanism.common.Mekanism;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;

public class ItemNetworkReader extends ItemEnergized
{
	public static double ENERGY_PER_USE = 400;

	public ItemNetworkReader()
	{
		super(60000);
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		if(!world.isRemote)
		{
			TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));

			if(getEnergy(stack) >= ENERGY_PER_USE)
			{
				if(tileEntity instanceof IGridTransmitter)
				{
					setEnergy(stack, getEnergy(stack)-ENERGY_PER_USE);
	
					IGridTransmitter<?> transmitter = (IGridTransmitter<?>)tileEntity;
	
					player.addChatMessage(new ChatComponentText(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " -------------"));
					player.addChatMessage(new ChatComponentText(EnumColor.GREY + " *Transmitters: " + EnumColor.DARK_GREY + transmitter.getTransmitterNetwork().getSize()));
					player.addChatMessage(new ChatComponentText(EnumColor.GREY + " *Acceptors: " + EnumColor.DARK_GREY + transmitter.getTransmitterNetwork().getAcceptorSize()));
					player.addChatMessage(new ChatComponentText(EnumColor.GREY + " *Needed: " + EnumColor.DARK_GREY + transmitter.getTransmitterNetwork().getNeededInfo()));
					player.addChatMessage(new ChatComponentText(EnumColor.GREY + " *Buffer: " + EnumColor.DARK_GREY + transmitter.getTransmitterNetwork().getStoredInfo()));
					player.addChatMessage(new ChatComponentText(EnumColor.GREY + " *Throughput: " + EnumColor.DARK_GREY + transmitter.getTransmitterNetwork().getFlowInfo()));
					player.addChatMessage(new ChatComponentText(EnumColor.GREY + " *Capacity: " + EnumColor.DARK_GREY + transmitter.getTransmitterNetwork().getCapacity()));
					player.addChatMessage(new ChatComponentText(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[=======]" + EnumColor.GREY + " -------------"));
					
					return true;
				}
				else if(tileEntity != null)
				{
					setEnergy(stack, getEnergy(stack)-ENERGY_PER_USE);
					
					Set<ITransmitterNetwork> iteratedNetworks = new HashSet<ITransmitterNetwork>();
					
					for(EnumFacing iterSide : EnumFacing.values())
					{
						Coord4D coord = Coord4D.get(tileEntity).offset(iterSide);
						
						if(coord.getTileEntity(world) instanceof IGridTransmitter)
						{
							IGridTransmitter<?> transmitter = (IGridTransmitter<?>)coord.getTileEntity(world);
							
							if(transmitter.getTransmitterNetwork().possibleAcceptors.containsKey(coord.offset(iterSide.getOpposite())) && !iteratedNetworks.contains(transmitter.getTransmitterNetwork()))
							{
								player.addChatMessage(new ChatComponentText(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[" + transmitter.getTransmissionType().getName() + "]" + EnumColor.GREY + " -------------"));
								player.addChatMessage(new ChatComponentText(EnumColor.GREY + " *Connected sides: " + EnumColor.DARK_GREY + transmitter.getTransmitterNetwork().acceptorDirections.get(coord.offset(iterSide.getOpposite()))));
								player.addChatMessage(new ChatComponentText(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[=======]" + EnumColor.GREY + " -------------"));
								
								iteratedNetworks.add(transmitter.getTransmitterNetwork());
							}
						}
					}
					
					return true;
				}
			}

			if(player.isSneaking() && Mekanism.debug)
			{
				String[] strings = TransmitterNetworkRegistry.getInstance().toStrings();
				player.addChatMessage(new ChatComponentText(EnumColor.GREY + "---------- " + EnumColor.DARK_BLUE + "[Mekanism Debug]" + EnumColor.GREY + " ----------"));

				for(String s : strings)
				{
					player.addChatMessage(new ChatComponentText(EnumColor.DARK_GREY + s));
				}

				player.addChatMessage(new ChatComponentText(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[=======]" + EnumColor.GREY + " -------------"));
			}
		}

		return false;
	}

	@Override
	public boolean canSend(ItemStack itemstack)
	{
		return false;
	}
}
