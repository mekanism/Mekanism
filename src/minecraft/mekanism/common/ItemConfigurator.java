package mekanism.common;

import universalelectricity.core.electricity.ElectricityPack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import mekanism.api.EnumColor;
import mekanism.api.IConfigurable;

public class ItemConfigurator extends ItemEnergized
{
	public final int ENERGY_PER_USE = 400;
	
    public ItemConfigurator(int id)
    {
        super(id, 60000, 120);
    }
    
    @Override
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
    	if(!world.isRemote)
    	{
    		if(world.getBlockTileEntity(x, y, z) instanceof IConfigurable)
    		{
    			IConfigurable config = (IConfigurable)world.getBlockTileEntity(x, y, z);
    			
    			if(!player.isSneaking())
    			{
        			player.sendChatToPlayer(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " Current color: " + config.getSideData().get(config.getConfiguration()[MekanismUtils.getBaseOrientation(side, config.getOrientation())]).color.getName());
        			return true;
    			}
    			else {
    				if(getJoules(stack) >= ENERGY_PER_USE)
    				{
    					onProvide(new ElectricityPack(ENERGY_PER_USE/120, 120), stack);
	    				MekanismUtils.incrementOutput(config, MekanismUtils.getBaseOrientation(side, config.getOrientation()));
	    				player.sendChatToPlayer(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " Color bumped to: " + config.getSideData().get(config.getConfiguration()[MekanismUtils.getBaseOrientation(side, config.getOrientation())]).color.getName());
	    				return true;
    				}
    			}
    		}
    	}
        return false;
    }
    
	@Override
	public ElectricityPack getProvideRequest(ItemStack itemStack)
	{
		return new ElectricityPack();
	}
}
