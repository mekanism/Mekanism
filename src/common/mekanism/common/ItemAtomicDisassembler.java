package mekanism.common;

import java.util.List;

import universalelectricity.prefab.ItemElectric;

import net.minecraft.src.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

public class ItemAtomicDisassembler extends ItemEnergized
{
	public ItemAtomicDisassembler(int id)
	{
		super(id, 120000, 512, 1200);
	}
	
    @Override
    public boolean canHarvestBlock(Block block)
    {
    	return block != Block.bedrock;
    }
    
    @Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
    	super.addInformation(itemstack, entityplayer, list, flag);
    	
    	list.add("Block efficiency: 40");
	}
    
    @Override
    public boolean hitEntity(ItemStack itemstack, EntityLiving hitEntity, EntityLiving player)
    {
    	if(getJoules(itemstack) > 0)
    	{
			hitEntity.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer)player), 18);
			onUse(2000, itemstack);
    	}
    	else {
    		hitEntity.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer)player), 4);
    	}
        return false;
    }
    
    public float getStrVsBlock(ItemStack itemstack, Block block)
    {
    	return getJoules(itemstack) != 0 ? 40F : 1F;
    }
    
    @Override
    public boolean onBlockDestroyed(ItemStack itemstack, World world, int id, int x, int y, int z, EntityLiving entityliving)
    {
        if ((double)Block.blocksList[id].getBlockHardness(world, x, y, z) != 0.0D)
        {
            onUse(120, itemstack);
        }
        else {
        	onUse(60, itemstack);
        }

        return true;
    }
    
    @Override
    public boolean isFull3D()
    {
        return true;
    }
    
    @Override
    public boolean canProduceElectricity()
    {
    	return false;
    }
}
