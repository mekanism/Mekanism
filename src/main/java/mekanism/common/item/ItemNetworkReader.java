package mekanism.common.item;

import mekanism.api.EnumColor;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.transmitters.TransmitterNetworkRegistry;
import mekanism.common.Mekanism;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

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
			TileEntity tileEntity = world.getTileEntity(x, y, z);

			if(tileEntity instanceof IGridTransmitter)
			{
				if(getEnergy(stack) >= ENERGY_PER_USE)
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
