package net.uberkat.obsidian.common;

import net.minecraft.src.*;

public class ItemLightningRod extends ItemObsidian 
{
	public ItemLightningRod(int i)
	{
		super(i);
		setMaxStackSize(1);
		setMaxDamage(100);
		setCreativeTab(CreativeTabs.tabTools);
	}
	
    public boolean hasEffect(ItemStack par1ItemStack)
    {
        return true;
    }
	
	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
	{
		if(itemstack.getItemDamage() == 0)
		{
			MovingObjectPosition movingobjectposition = entityplayer.rayTrace(75.0D, 1.0F);
			if(movingobjectposition == null)
			{
				return itemstack;
			}
            Vec3 vec3 = movingobjectposition.hitVec;
            double x = vec3.xCoord;
            double y = vec3.yCoord;
            double z = vec3.zCoord;
            int i = MathHelper.floor_double(x);
            int j = MathHelper.floor_double(y);
            int k = MathHelper.floor_double(z);
            if(world.canBlockSeeTheSky(i, j, k) == false)
            {
            	return itemstack;
            }
            EntityLightningBolt entitybolt = new EntityLightningBolt(world, 0D, 0D, 0D);
            entitybolt.setLocationAndAngles(x, y, z, 0, 0.0F);
            world.spawnEntityInWorld(entitybolt);
            if(!entityplayer.capabilities.isCreativeMode)
            {
            	itemstack.damageItem(99, entityplayer);
            }
		}
		return itemstack;
	}
	
	public void onUpdate(ItemStack itemstack, World world, Entity entity, int i, boolean flag)
	{
		if(itemstack.getItemDamage() > 0)
		{
			itemstack.damageItem(-1, (EntityLiving)entity);
		}
	}
}
