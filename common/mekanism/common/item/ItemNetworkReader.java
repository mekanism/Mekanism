package mekanism.common.item;

import mekanism.api.EnumColor;
import mekanism.api.transmitters.ITransmitter;
import mekanism.api.transmitters.TransmitterNetworkRegistry;
import mekanism.common.Mekanism;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.world.World;

public class ItemNetworkReader extends ItemEnergized
{
	public static double ENERGY_PER_USE = 400;
	
	public ItemNetworkReader(int id)
    {
        super(id, 60000, 120);
    }
	
    @Override
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
    	if(!world.isRemote)
    	{
    		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
    		
    		if(tileEntity instanceof ITransmitter)
    		{
    			if(getEnergy(stack) >= ENERGY_PER_USE)
    			{
    				setEnergy(stack, getEnergy(stack)-ENERGY_PER_USE);
    				
	    			ITransmitter transmitter = (ITransmitter)tileEntity;
	    			
	    			player.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " -------------"));
	                player.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.GREY + " *Transmitters: " + EnumColor.DARK_GREY + transmitter.getTransmitterNetworkSize()));
	                player.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.GREY + " *Acceptors: " + EnumColor.DARK_GREY + transmitter.getTransmitterNetworkAcceptorSize()));
	                player.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.GREY + " *Needed: " + EnumColor.DARK_GREY + transmitter.getTransmitterNetworkNeeded()));
	                player.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.GREY + " *Power: " + EnumColor.DARK_GREY + transmitter.getTransmitterNetworkFlow() ));
	                player.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[=======]" + EnumColor.GREY + " -------------"));
    			}
    		}
    		
    		if(player.isSneaking() && Mekanism.debug)
    		{
    			player.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.GREY + "---------- " + EnumColor.DARK_BLUE + "[Mekanism Debug]" + EnumColor.GREY + " ----------"));
    			player.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.GREY + " *Networks: " + EnumColor.DARK_GREY + TransmitterNetworkRegistry.getInstance().toString()));
    			player.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[=======]" + EnumColor.GREY + " -------------"));
    		}
    	}
    	
    	return false;
    }
}
