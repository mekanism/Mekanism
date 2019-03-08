package mekanism.common.item;

import java.util.HashSet;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.IHeatTransfer;
import mekanism.api.MekanismAPI;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.transmitters.TransmitterNetworkRegistry;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.util.CapabilityUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class ItemNetworkReader extends ItemEnergized
{
	public static double ENERGY_PER_USE = 400;

	public ItemNetworkReader()
	{
		super(60000);
	}

	@Nonnull
	@Override
	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
	{
		ItemStack stack = player.getHeldItem(hand);
		
		if(!world.isRemote)
		{
			TileEntity tileEntity = world.getTileEntity(pos);
			boolean drain = !player.capabilities.isCreativeMode;

			if(getEnergy(stack) >= ENERGY_PER_USE && tileEntity != null)
			{
				if(CapabilityUtils.hasCapability(tileEntity, Capabilities.GRID_TRANSMITTER_CAPABILITY, side.getOpposite()))
				{
					if(drain) setEnergy(stack, getEnergy(stack)-ENERGY_PER_USE);
	
					IGridTransmitter transmitter = CapabilityUtils.getCapability(tileEntity, Capabilities.GRID_TRANSMITTER_CAPABILITY, side.getOpposite());
	
					player.sendMessage(new TextComponentString(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " -------------"));
					player.sendMessage(new TextComponentString(EnumColor.GREY + " *Transmitters: " + EnumColor.DARK_GREY + transmitter.getTransmitterNetworkSize()));
					player.sendMessage(new TextComponentString(EnumColor.GREY + " *Acceptors: " + EnumColor.DARK_GREY + transmitter.getTransmitterNetworkAcceptorSize()));
					player.sendMessage(new TextComponentString(EnumColor.GREY + " *Needed: " + EnumColor.DARK_GREY + transmitter.getTransmitterNetworkNeeded()));
					player.sendMessage(new TextComponentString(EnumColor.GREY + " *Buffer: " + EnumColor.DARK_GREY + transmitter.getTransmitterNetworkBuffer()));
					player.sendMessage(new TextComponentString(EnumColor.GREY + " *Throughput: " + EnumColor.DARK_GREY + transmitter.getTransmitterNetworkFlow()));
					player.sendMessage(new TextComponentString(EnumColor.GREY + " *Capacity: " + EnumColor.DARK_GREY + transmitter.getTransmitterNetworkCapacity()));
					
					if(CapabilityUtils.hasCapability(tileEntity, Capabilities.HEAT_TRANSFER_CAPABILITY, side.getOpposite()))
					{
						IHeatTransfer transfer = CapabilityUtils.getCapability(tileEntity, Capabilities.HEAT_TRANSFER_CAPABILITY, side.getOpposite());
						player.sendMessage(new TextComponentString(EnumColor.GREY + " *Temperature: " + EnumColor.DARK_GREY + transfer.getTemp() + "K above ambient"));
					}
					
					player.sendMessage(new TextComponentString(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[=======]" + EnumColor.GREY + " -------------"));
					
					return EnumActionResult.SUCCESS;
				}
				else if(CapabilityUtils.hasCapability(tileEntity, Capabilities.HEAT_TRANSFER_CAPABILITY, side.getOpposite()))
				{
					if(drain) setEnergy(stack, getEnergy(stack)-ENERGY_PER_USE);

					IHeatTransfer transfer = CapabilityUtils.getCapability(tileEntity, Capabilities.HEAT_TRANSFER_CAPABILITY, side.getOpposite());
					player.sendMessage(new TextComponentString(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " -------------"));
					player.sendMessage(new TextComponentString(EnumColor.GREY + " *Temperature: " + EnumColor.DARK_GREY + transfer.getTemp() + "K above ambient"));
					player.sendMessage(new TextComponentString(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[=======]" + EnumColor.GREY + " -------------"));

					return EnumActionResult.SUCCESS;
				}
				else {
					if(drain) setEnergy(stack, getEnergy(stack)-ENERGY_PER_USE);
					
					Set<DynamicNetwork> iteratedNetworks = new HashSet<>();
					
					for(EnumFacing iterSide : EnumFacing.VALUES)
					{
						Coord4D coord = Coord4D.get(tileEntity).offset(iterSide);

						TileEntity tile = coord.getTileEntity(world);
						
						if(CapabilityUtils.hasCapability(tile, Capabilities.GRID_TRANSMITTER_CAPABILITY, iterSide.getOpposite()))
						{
							IGridTransmitter transmitter = CapabilityUtils.getCapability(tile, Capabilities.GRID_TRANSMITTER_CAPABILITY, iterSide.getOpposite());
							
							if(transmitter.getTransmitterNetwork().possibleAcceptors.containsKey(coord.offset(iterSide.getOpposite())) && !iteratedNetworks.contains(transmitter.getTransmitterNetwork()))
							{
								player.sendMessage(new TextComponentString(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[" + transmitter.getTransmissionType().getName() + "]" + EnumColor.GREY + " -------------"));
								player.sendMessage(new TextComponentString(EnumColor.GREY + " *Connected sides: " + EnumColor.DARK_GREY + transmitter.getTransmitterNetwork().acceptorDirections.get(coord.offset(iterSide.getOpposite()))));
								player.sendMessage(new TextComponentString(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[=======]" + EnumColor.GREY + " -------------"));
								
								iteratedNetworks.add(transmitter.getTransmitterNetwork());
							}
						}
					}
					
					return EnumActionResult.SUCCESS;
				}
			}

			if(player.isSneaking() && MekanismAPI.debug)
			{
				String[] strings = TransmitterNetworkRegistry.getInstance().toStrings();
				player.sendMessage(new TextComponentString(EnumColor.GREY + "---------- " + EnumColor.DARK_BLUE + "[Mekanism Debug]" + EnumColor.GREY + " ----------"));

				for(String s : strings)
				{
					player.sendMessage(new TextComponentString(EnumColor.DARK_GREY + s));
				}

				player.sendMessage(new TextComponentString(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[=======]" + EnumColor.GREY + " -------------"));
			}
		}

		return EnumActionResult.PASS;
	}

	@Override
	public boolean canSend(ItemStack itemstack)
	{
		return false;
	}
}
