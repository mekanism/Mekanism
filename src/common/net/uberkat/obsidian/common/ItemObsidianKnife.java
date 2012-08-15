package net.uberkat.obsidian.common;

import java.util.Random;
import net.minecraft.src.*;

public class ItemObsidianKnife extends ItemObsidian
{
    private EnumToolMaterial enumToolMaterial;
    protected int weaponDamage;
    protected float knockBack;
    protected float strVsBlock;
    protected int entityDamage;
    protected int blockDamage;
    protected int enchantability;

    public ItemObsidianKnife(int i, EnumToolMaterial enumtoolmaterial)
    {
        super(i);
        enumToolMaterial = enumtoolmaterial;
        maxStackSize = 1;
        setMaxDamage((int)((float)enumtoolmaterial.getMaxUses() * 0.5F));
        weaponDamage = (int)((float)3 + (float)enumtoolmaterial.getDamageVsEntity() * 1.5F);
        knockBack = 0.2F + (enumtoolmaterial != EnumToolMaterial.GOLD ? 0.0F : 0.2F);
        strVsBlock = 1;
        entityDamage = 2;
        blockDamage = 2;
        enchantability = enumtoolmaterial.getEnchantability();
        setTabToDisplayOn(CreativeTabs.tabCombat);
    }

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
    {
    	entityplayer.setItemInUse(itemstack, getMaxItemUseDuration(itemstack));
        world.playSoundAtEntity(entityplayer, "random.bow", 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 0.8F));

        if (!world.isRemote)
        {
            EntityKnife entityknife = new EntityKnife(world, entityplayer, itemstack);

            if (entityplayer.capabilities.isCreativeMode)
            {
            	entityknife.doesArrowBelongToPlayer = false;
            }

            world.spawnEntityInWorld(entityknife);
        }

        if (entityplayer.capabilities.isCreativeMode)
        {
            return itemstack;
        }
        else
        {
            return new ItemStack(shiftedIndex, 0, 0);
        }
    }

    /**
     * Returns the strength of the stack against a given block. 1.0F base, (Quality+1)*2 if correct blocktype, 1.5F if
     * sword
     */
    public float getStrVsBlock(ItemStack itemstack, Block block)
    {
        return strVsBlock * (block.blockID != Block.web.blockID ? 1.0F : 10F);
    }

    /**
     * Returns if the item (tool) can harvest results from the block type.
     */
    public boolean canHarvestBlock(Block block)
    {
        return block.blockID == Block.web.blockID;
    }
    
    public int getItemEnchantability()
    {
        return enchantability;
    }
    
    public int getMaxItemUseDuration(ItemStack itemstack)
    {
        return 0x11940;
    }
    
    public boolean isFull3D()
    {
        return true;
    }
    
    public int getDamageVsEntity(Entity entity)
    {
        return weaponDamage;
    }
    
    public boolean onBlockDestroyed(ItemStack itemstack, int i, int j, int k, int l, EntityLiving entityliving)
    {
        itemstack.damageItem(blockDamage, entityliving);
        return true;
    }
    
    public boolean hitEntity(ItemStack itemstack, EntityLiving entityliving, EntityLiving entityliving1)
    {
        if (knockBack != 0.0F)
        {
            PhysicsHelper.knockBack(entityliving, entityliving1, knockBack);
        }

        itemstack.damageItem(entityDamage, entityliving1);
        return true;
    }
}
