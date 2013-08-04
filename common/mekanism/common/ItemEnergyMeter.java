package mekanism.common;

import java.util.ArrayList;

import universalelectricity.core.electricity.ElectricityDisplay;
import universalelectricity.core.electricity.ElectricityDisplay.ElectricUnit;

import mekanism.api.EnumColor;
import mekanism.api.TransmitterNetworkRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.world.World;

public class ItemEnergyMeter extends ItemEnergized
{
	public static double ENERGY_PER_USE = 400;
	
	public ItemEnergyMeter(int id)
    {
        super(id, 60000, 120);
    }
	
    @Override
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
    	if(!world.isRemote)
    	{
    		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
    		
    		if(tileEntity instanceof IUniversalCable)
    		{
    			if(getEnergy(stack) >= ENERGY_PER_USE)
    			{
    				setEnergy(stack, getEnergy(stack)-ENERGY_PER_USE);
    				
	    			IUniversalCable cable = (IUniversalCable)tileEntity;
	    			
	    			player.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " -------------"));
	                player.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.GREY + " *Cables: " + EnumColor.DARK_GREY + cable.getNetwork().cables.size()));
	                player.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.GREY + " *Acceptors: " + EnumColor.DARK_GREY + cable.getNetwork().possibleAcceptors.size()));
	                player.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.GREY + " *Needed energy: " + EnumColor.DARK_GREY + ElectricityDisplay.getDisplay((float)cable.getNetwork().getEnergyNeeded(new ArrayList()), ElectricUnit.JOULES)));
	                player.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.GREY + " *Power: " + EnumColor.DARK_GREY + ElectricityDisplay.getDisplay((float)cable.getNetwork().getPower(), ElectricUnit.WATT)));
	                player.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[=======]" + EnumColor.GREY + " -------------"));
    			}
    		}
    		if(player.isSneaking() && Mekanism.debug){
    			player.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.GREY + "---------- " + EnumColor.DARK_BLUE + "[Mekanism Debug]" + EnumColor.GREY + " ----------"));
    			player.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.GREY + " *Networks: " + EnumColor.DARK_GREY + TransmitterNetworkRegistry.getInstance().toString()));
    			player.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[=======]" + EnumColor.GREY + " -------------"));
    		}
    	}
    	
    	return false;
    }
}
