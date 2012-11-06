package mekanism.common;

import java.util.ArrayList;
import java.util.Random;

import cpw.mods.fml.common.registry.BlockProxy;
import net.minecraft.src.*;

public class BlockObsidianTNT extends Block
{
    public BlockObsidianTNT(int par1)
    {
        super(par1, Material.tnt);
        setStepSound(Block.soundGrassFootstep);
        setHardness(0.0F);
        setResistance(0.0F);
    }

    @Override
    public int getBlockTextureFromSide(int side)
    {
        if(side == 1)
        {
        	return 5;
        }
        if(side == 0)
        {
        	return 7;
        }
        else {
        	return 6;
        }
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z)
    {
        super.onBlockAdded(world, x, y, z);

        if (world.isBlockIndirectlyGettingPowered(x, y, z))
        {
            onBlockDestroyedByPlayer(world, x, y, z, 1);
            world.setBlockWithNotify(x, y, z, 0);
        }
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, int blockID)
    {
        if (blockID > 0 && Block.blocksList[blockID].canProvidePower() && world.isBlockIndirectlyGettingPowered(x, y, z))
        {
            onBlockDestroyedByPlayer(world, x, y, z, 1);
            world.setBlockWithNotify(x, y, z, 0);
        }
    }

    @Override
    public int quantityDropped(Random random)
    {
        return 0;
    }

    @Override
    public void onBlockDestroyedByExplosion(World world, int x, int y, int z)
    {
        if (!world.isRemote)
        {
            EntityObsidianTNT entity = new EntityObsidianTNT(world, (double)((float)x + 0.5F), (double)((float)y + 0.5F), (double)((float)z + 0.5F));
            entity.fuse = world.rand.nextInt(entity.fuse / 4) + entity.fuse / 8;
            world.spawnEntityInWorld(entity);
        }
    }

    @Override
    public void onBlockDestroyedByPlayer(World world, int x, int y, int z, int meta)
    {
        if (!world.isRemote)
        {
            if ((meta & 1) == 0)
            {
                dropBlockAsItem_do(world, x, y, z, new ItemStack(Mekanism.ObsidianTNT, 1, 0));
            }
            else
            {
                EntityObsidianTNT entity = new EntityObsidianTNT(world, (double)((float)x + 0.5F), (double)((float)y + 0.5F), (double)((float)z + 0.5F));
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
        if (entityplayer.getCurrentEquippedItem() != null && entityplayer.getCurrentEquippedItem().itemID == Item.flintAndSteel.shiftedIndex)
        {
            onBlockDestroyedByPlayer(world, x, y, z, 1);
            world.setBlockWithNotify(x, y, z, 0);
            return true;
        }
        else
        {
            return super.onBlockActivated(world, x, y, z, entityplayer, i1, f1, f2, f3);
        }
    }

    @Override
    protected ItemStack createStackedBlock(int i)
    {
        return null;
    }
    
    @Override
    public String getTextureFile()
    {
    	return "/textures/terrain.png";
    }
}
