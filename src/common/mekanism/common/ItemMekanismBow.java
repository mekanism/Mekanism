package mekanism.common;

import java.util.Random;
import net.minecraft.src.*;

public class ItemMekanismBow extends ItemMekanism
{
    public ItemMekanismBow(int par1)
    {
        super(par1);
        maxStackSize = 1;
        setMaxDamage(750);
        setCreativeTab(Mekanism.tabMekanism);
    }
    
    public void onUpdate(ItemStack itemstack, World world, Entity entity, int i, boolean flag)
    {
        EntityPlayer player = (EntityPlayer)entity;
        ItemStack currentItem = player.inventory.getCurrentItem();

        if (player.isUsingItem() && currentItem.itemID == Mekanism.ObsidianBow.shiftedIndex)
        {
            int useTicks = itemstack.getMaxItemUseDuration() - player.getItemInUseCount();

            if (useTicks >= 14)
            {
                iconIndex = Mekanism.BOW_TEXTURE_INDEX+3;
            }
            else if (useTicks > 9)
            {
                iconIndex = Mekanism.BOW_TEXTURE_INDEX+2;
            }
            else if (useTicks > 0)
            {
                iconIndex = Mekanism.BOW_TEXTURE_INDEX+1;
            }
        }
        else
        {
            iconIndex = Mekanism.BOW_TEXTURE_INDEX;
        }
    }

    public void onPlayerStoppedUsing(ItemStack itemstack, World world, EntityPlayer player, int itemUseCount)
    {
        boolean flag = player.capabilities.isCreativeMode || EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, itemstack) > 0;

        if (flag || player.inventory.hasItem(Item.arrow.shiftedIndex))
        {
            int i = getMaxItemUseDuration(itemstack) - itemUseCount;
            float f = (float)i / 20F;
            f = (f * f + f * 2.0F) / 3F;

            if ((double)f < 0.10000000000000001D)
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

            int j = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, itemstack);

            if (j > 0)
            {
                entityarrow.setDamage(entityarrow.getDamage() + (double)j * 0.5D + 0.5D);
            }

            int k = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, itemstack);

            if (k > 0)
            {
                entityarrow.setKnockbackStrength(k);
            }

            if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, itemstack) > 0)
            {
                entityarrow.setFire(100);
            }

            itemstack.damageItem(1, player);
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
                entityarrow.setFire(60);
            }
        }
        
        ItemStack itemStack = player.inventory.getCurrentItem();
        
        if(itemStack.itemID != Mekanism.ObsidianBow.shiftedIndex)
        {
        	iconIndex = Mekanism.BOW_TEXTURE_INDEX;
        }
    }

    public ItemStack onFoodEaten(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
    {
        return par1ItemStack;
    }

    public int getMaxItemUseDuration(ItemStack par1ItemStack)
    {
        return 0x11940;
    }

    public EnumAction getItemUseAction(ItemStack par1ItemStack)
    {
        return EnumAction.bow;
    }

    public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
    {
        if (par3EntityPlayer.capabilities.isCreativeMode || par3EntityPlayer.inventory.hasItem(Item.arrow.shiftedIndex))
        {
            par3EntityPlayer.setItemInUse(par1ItemStack, getMaxItemUseDuration(par1ItemStack));
        }

        return par1ItemStack;
    }

    public int getItemEnchantability()
    {
        return 1;
    }
}
