package mekanism.common;

import java.util.ArrayList;

import universalelectricity.core.electricity.ElectricityDisplay;
import universalelectricity.core.electricity.ElectricityDisplay.ElectricUnit;

import mekanism.api.EnumColor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
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
	    			
	    			player.sendChatToPlayer(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " -------------");
	                player.sendChatToPlayer(EnumColor.GREY + " *Cables: " + EnumColor.DARK_GREY + cable.getNetwork().cables.size());
	                player.sendChatToPlayer(EnumColor.GREY + " *Acceptors: " + EnumColor.DARK_GREY + cable.getNetwork().possibleAcceptors.size());
	                player.sendChatToPlayer(EnumColor.GREY + " *Needed energy: " + EnumColor.DARK_GREY + ElectricityDisplay.getDisplay(cable.getNetwork().getEnergyNeeded(new ArrayList()), ElectricUnit.JOULES));
	                player.sendChatToPlayer(EnumColor.GREY + " *Power: " + EnumColor.DARK_GREY + ElectricityDisplay.getDisplay(cable.getNetwork().getPower(), ElectricUnit.WATT));
	                player.sendChatToPlayer(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[=======]" + EnumColor.GREY + " -------------");
    			}
    		}
    	}
    	
    	return false;
    }
}
