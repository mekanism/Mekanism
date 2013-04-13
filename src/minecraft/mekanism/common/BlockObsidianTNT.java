package mekanism.common;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class BlockObsidianTNT extends Block
{
	public Icon[] icons = new Icon[256];
	
    public BlockObsidianTNT(int id)
    {
        super(id, Material.tnt);
        setCreativeTab(Mekanism.tabMekanism);
    }
    
	@Override
	public void registerIcons(IconRegister register)
	{
		icons[0] = register.registerIcon("mekanism:ObsidianTNTBottom");
		icons[1] = register.registerIcon("mekanism:ObsidianTNTTop");
		icons[2] = register.registerIcon("mekanism:ObsidianTNTSide");
	}

    @Override
    public Icon getIcon(int side, int meta)
    {
        if(side == 1)
        {
        	return icons[1];
        }
        if(side == 0)
        {
        	return icons[0];
        }
        else {
        	return icons[2];
        }
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z)
    {
        super.onBlockAdded(world, x, y, z);

        if(world.isBlockIndirectlyGettingPowered(x, y, z))
        {
            onBlockDestroyedByPlayer(world, x, y, z, 1);
            world.setBlockToAir(x, y, z);
        }
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, int blockID)
    {
        if(world.isBlockIndirectlyGettingPowered(x, y, z))
        {
            onBlockDestroyedByPlayer(world, x, y, z, 1);
            world.setBlockToAir(x, y, z);
        }
    }

    @Override
    public int quantityDropped(Random random)
    {
        return 1;
    }

    @Override
    public void onBlockDestroyedByExplosion(World world, int x, int y, int z, Explosion explosion)
    {
        if(!world.isRemote)
        {
            EntityObsidianTNT entity = new EntityObsidianTNT(world, x + 0.5F, y + 0.5F, z + 0.5F);
            entity.fuse = world.rand.nextInt(entity.fuse / 4) + entity.fuse / 8;
            world.spawnEntityInWorld(entity);
        }
    }

    @Override
    public void onBlockDestroyedByPlayer(World world, int x, int y, int z, int meta)
    {
        if(!world.isRemote)
        {
            if((meta & 1) == 0)
            {
                dropBlockAsItem_do(world, x, y, z, new ItemStack(Mekanism.ObsidianTNT, 1, 0));
            }
            else {
                EntityObsidianTNT entity = new EntityObsidianTNT(world, x + 0.5F, y + 0.5F, z + 0.5F);
                world.spawnEntityInWorld(entity);
                world.playSoundAtEntity(entity, "random.fuse", 1.0F, 1.0F);
            }
        }
    }

    @Override
    public void onBlockClicked(World world, int x, int y, int z, EntityPlayer entityplayer)
    {
        super.onBlockClicked(world, x, y, z, entityplayer);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int i1, float f1, float f2, float f3)
    {
        if(entityplayer.getCurrentEquippedItem() != null && entityplayer.getCurrentEquippedItem().itemID == Item.flintAndSteel.itemID)
        {
            onBlockDestroyedByPlayer(world, x, y, z, 1);
            world.setBlockToAir(x, y, z);
            return true;
        }
        else
        {
            return super.onBlockActivated(world, x, y, z, entityplayer, i1, f1, f2, f3);
        }
    }
    
    @Override
    public boolean canDropFromExplosion(Explosion explosion)
    {
        return false;
    }
    
    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity)
    {
        if(entity instanceof EntityArrow && !world.isRemote)
        {
            EntityArrow entityarrow = (EntityArrow)entity;

            if(entityarrow.isBurning())
            {
                onBlockDestroyedByPlayer(world, x, y, z, 1);
                world.setBlockToAir(x, y, z);
            }
        }
    }

    @Override
    protected ItemStack createStackedBlock(int i)
    {
        return null;
    }
}
