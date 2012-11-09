package mekanism.common;

import java.util.List;

import net.minecraft.src.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

public class ItemNuclearDisassembler extends ItemEnergized
{
	public ItemNuclearDisassembler(int id)
	{
		super(id, 16000, 500, 160);
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
    	if(getEnergy(itemstack) > 0)
    	{
			hitEntity.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer)player), 18);
			discharge(itemstack, 40);
    	}
    	else {
    		hitEntity.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer)player), 4);
    	}
        return false;
    }
    
    public float getStrVsBlock(ItemStack itemstack, Block block)
    {
    	return getEnergy(itemstack) != 0 ? 40F : 1F;
    }
    
    @Override
    public boolean onBlockDestroyed(ItemStack itemstack, World world, int id, int x, int y, int z, EntityLiving entityliving)
    {
        if ((double)Block.blocksList[id].getBlockHardness(world, x, y, z) != 0.0D)
        {
            discharge(itemstack, 10);
        }
        else {
        	discharge(itemstack, 5);
        }

        return true;
    }
    
    @Override
    public boolean isFull3D()
    {
        return true;
    }
	
	@Override
	public boolean canBeDischarged()
	{
		return false;
	}
}
