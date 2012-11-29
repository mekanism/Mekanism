package mekanism.tools.common;

import java.util.List;

import mekanism.common.ItemMekanism;
import net.minecraft.src.Block;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EnumAction;
import net.minecraft.src.EnumToolMaterial;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;

public class ItemMekanismSword extends ItemMekanism
{
    private int weaponDamage;
    private final EnumToolMaterial toolMaterial;

    public ItemMekanismSword(int id, EnumToolMaterial enumtoolmaterial)
    {
        super(id);
        toolMaterial = enumtoolmaterial;
        maxStackSize = 1;
        setMaxDamage(enumtoolmaterial.getMaxUses());
        weaponDamage = 4 + enumtoolmaterial.getDamageVsEntity();
    }

    @Override
    public float getStrVsBlock(ItemStack itemstack, Block block)
    {
        return block.blockID != Block.web.blockID ? 1.5F : 15F;
    }
    
    @Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
    	list.add("HP: " + (itemstack.getMaxDamage() - itemstack.getItemDamage()));
	}

    @Override
    public boolean hitEntity(ItemStack itemstack, EntityLiving entityplayer, EntityLiving entityliving)
    {
        itemstack.damageItem(1, entityliving);
        return true;
    }
    
    @Override
    public boolean onBlockDestroyed(ItemStack itemstack, World world, int x, int y, int z, int facing, EntityLiving entityplayer)
    {
        itemstack.damageItem(2, entityplayer);
        return true;
    }

    @Override
    public int getDamageVsEntity(Entity entity)
    {
        return weaponDamage;
    }

    @Override
    public boolean isFull3D()
    {
        return true;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack itemstack)
    {
        return EnumAction.block;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack itemstack)
    {
        return 0x11940;
    }
    
    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
    {
        entityplayer.setItemInUse(itemstack, getMaxItemUseDuration(itemstack));
        return itemstack;
    }

    @Override
    public boolean canHarvestBlock(Block block)
    {
        return block.blockID == Block.web.blockID;
    }

    @Override
    public int getItemEnchantability()
    {
        return toolMaterial.getEnchantability();
    }
    
    @Override
    public String getTextureFile()
    {
    	return "/resources/mekanism/textures/tools/items.png";
    }
}
