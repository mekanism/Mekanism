package mekanism.tools.common;

import java.util.Random;

import mekanism.common.ItemMekanism;
import mekanism.common.Mekanism;
import net.minecraft.src.*;

public class ItemMekanismKnife extends ItemMekanism
{
    private EnumToolMaterial enumToolMaterial;
    protected int weaponDamage;
    protected float strVsBlock;
    protected int entityDamage;
    protected int blockDamage;
    protected int enchantability;

    public ItemMekanismKnife(int i, EnumToolMaterial enumtoolmaterial)
    {
        super(i);
        enumToolMaterial = enumtoolmaterial;
        maxStackSize = 1;
        setMaxDamage((int)((float)enumtoolmaterial.getMaxUses() * 0.5F));
        weaponDamage = (int)((float)3 + (float)enumtoolmaterial.getDamageVsEntity() * 1.5F);
        strVsBlock = 1;
        entityDamage = 2;
        blockDamage = 2;
        enchantability = enumtoolmaterial.getEnchantability();
        setCreativeTab(Mekanism.tabMekanism);
    }

    @Override
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

    @Override
    public float getStrVsBlock(ItemStack itemstack, Block block)
    {
        return strVsBlock * (block.blockID != Block.web.blockID ? 1.0F : 10F);
    }

    @Override
    public boolean canHarvestBlock(Block block)
    {
        return block.blockID == Block.web.blockID;
    }
    
    @Override
    public int getItemEnchantability()
    {
        return enchantability;
    }
    
    @Override
    public int getMaxItemUseDuration(ItemStack itemstack)
    {
        return 0x11940;
    }
    
    @Override
    public boolean isFull3D()
    {
        return true;
    }
    
    @Override
    public int getDamageVsEntity(Entity entity)
    {
        return weaponDamage;
    }
    
    @Override
    public boolean onBlockDestroyed(ItemStack itemstack, World world, int i, int j, int k, int l, EntityLiving entityliving)
    {
        itemstack.damageItem(blockDamage, entityliving);
        return true;
    }
    
    @Override
    public boolean hitEntity(ItemStack itemstack, EntityLiving entityliving, EntityLiving entityliving1)
    {
        itemstack.damageItem(entityDamage, entityliving1);
        return true;
    }
    
    public String getTextureFile()
    {
    	return "/resources/mekanism/textures/tools/items.png";
    }
}
