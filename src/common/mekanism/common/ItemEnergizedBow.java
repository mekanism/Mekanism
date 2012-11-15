package mekanism.common;

import java.util.List;
import java.util.Random;
import net.minecraft.src.*;

public class ItemEnergizedBow extends ItemEnergized
{
	public boolean fireMode = false;
	
    public ItemEnergizedBow(int id)
    {
        super(id, 10000, 100, 100);
    }
    
	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		super.addInformation(itemstack, entityplayer, list, flag);
		list.add("Fire Mode: " + (fireMode ? "ON" : "OFF"));
	}

    @Override
    public void onPlayerStoppedUsing(ItemStack itemstack, World world, EntityPlayer player, int itemUseCount)
    {
    	if(!player.isSneaking() && getEnergy(itemstack) > 0)
    	{
	        boolean flag = player.capabilities.isCreativeMode || EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, itemstack) > 0;
	
	        if (flag || player.inventory.hasItem(Item.arrow.shiftedIndex))
	        {
	            int maxItemUse = getMaxItemUseDuration(itemstack) - itemUseCount;
	            float f = (float)maxItemUse / 20F;
	            f = (f * f + f * 2.0F) / 3F;
	
	            if ((double)f < 0.1D)
	            {
	                return;
	            }
	
	            if (f > 1.0F)
	            {
	                f = 1.0F;
	            }
	
	            EntityArrow entityarrow = new EntityArrow(world, player, f * 2.0F);
	
	            if (f == 1.0F)
	            {
	            	entityarrow.setIsCritical(true);
	            }
	            
	            if(!player.capabilities.isCreativeMode)
	            {
	            	discharge(itemstack, (fireMode ? 100 : 10));
	            }
	            
	            world.playSoundAtEntity(player, "random.bow", 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + f * 0.5F);
	
	            if (flag)
	            {
	            	entityarrow.canBePickedUp = 2;
	            }
	            else
	            {
	            	player.inventory.consumeInventoryItem(Item.arrow.shiftedIndex);
	            }
	
	            if (!world.isRemote)
	            {
	                world.spawnEntityInWorld(entityarrow);
	                entityarrow.setFire(fireMode ? 60 : 0);
	            }
	        }
    	}
    }

    @Override
    public ItemStack onFoodEaten(ItemStack itemstack, World world, EntityPlayer entityplayer)
    {
        return itemstack;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack itemstack)
    {
        return 0x11940;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack itemstack)
    {
        return EnumAction.bow;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
    {
    	if(!entityplayer.isSneaking())
    	{
	        if (entityplayer.capabilities.isCreativeMode || entityplayer.inventory.hasItem(Item.arrow.shiftedIndex))
	        {
	            entityplayer.setItemInUse(itemstack, getMaxItemUseDuration(itemstack));
	        }
    	}
    	else {
    		if(!world.isRemote)
    		{
	    		fireMode = !fireMode;
	    		entityplayer.addChatMessage(EnumColor.DARK_BLUE + "[Mekanism] " + EnumColor.GREY + "Fire Mode: " + (fireMode ? (EnumColor.DARK_GREEN + "ON") : (EnumColor.DARK_RED + "OFF")));
    		}
    	}
        return itemstack;
    }
}
