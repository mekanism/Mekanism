package net.uberkat.obsidian.common;

import java.util.ArrayList;
import java.util.Random;

import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;
import cpw.mods.fml.common.registry.BlockProxy;
import net.minecraft.src.*;

public class BlockPlatinumCompressor extends BlockContainer
{
    private Random compressorRand = new Random();
    
    private static int currentTextureIndex;
    
    public static boolean isActive = false;

    public BlockPlatinumCompressor(int par1)
    {
        super(par1, Material.iron);
    }
    
    private void setDefaultDirection(World world, int par2, int par3, int par4)
    {
        if (!world.isRemote)
        {
            int var5 = world.getBlockId(par2, par3, par4 - 1);
            int var6 = world.getBlockId(par2, par3, par4 + 1);
            int var7 = world.getBlockId(par2 - 1, par3, par4);
            int var8 = world.getBlockId(par2 + 1, par3, par4);
            byte var9 = 3;

            if (Block.opaqueCubeLookup[var5] && !Block.opaqueCubeLookup[var6])
            {
                var9 = 3;
            }

            if (Block.opaqueCubeLookup[var6] && !Block.opaqueCubeLookup[var5])
            {
                var9 = 2;
            }

            if (Block.opaqueCubeLookup[var7] && !Block.opaqueCubeLookup[var8])
            {
                var9 = 5;
            }

            if (Block.opaqueCubeLookup[var8] && !Block.opaqueCubeLookup[var7])
            {
                var9 = 4;
            }

            world.setBlockMetadataWithNotify(par2, par3, par4, var9);
        }
    }
    
    public void onBlockPlacedBy(World world, int par2, int par3, int par4, EntityLiving par5EntityLiving)
    {
        int var6 = MathHelper.floor_double((double)(par5EntityLiving.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;

        if (var6 == 0)
        {
            world.setBlockMetadataWithNotify(par2, par3, par4, 2);
        }

        if (var6 == 1)
        {
            world.setBlockMetadataWithNotify(par2, par3, par4, 5);
        }

        if (var6 == 2)
        {
            world.setBlockMetadataWithNotify(par2, par3, par4, 3);
        }

        if (var6 == 3)
        {
            world.setBlockMetadataWithNotify(par2, par3, par4, 4);
        }
    }

    public int idDropped(int par1, Random random, int par3)
    {
        return blockID;
    }

    public void onBlockAdded(World world, int par2, int par3, int par4)
    {
    	TileEntityPlatinumCompressor tileEntity = (TileEntityPlatinumCompressor)world.getBlockTileEntity(par2, par3, par4);
    	if(tileEntity != null)
    	{
    		tileEntity.machineBurnTime = 0;
    	}
    	setDefaultDirection(world, par2, par3, par4);
        super.onBlockAdded(world, par2, par3, par4);
    }
    
    public int getLightValue(IBlockAccess world, int x, int y, int z) 
    {
    	if(isActive) return 14;
	    else return 0;
    }

    @SideOnly(Side.CLIENT)
    public int getBlockTexture(IBlockAccess world, int x, int y, int z, int side)
    {
        int metadata = world.getBlockMetadata(x, y, z);
        
        if(side == metadata)
        {
        	return isActive ? currentTextureIndex : 17;
        }
        else {
        	return 16;
        }
    }

    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World world, int par2, int par3, int par4, Random par5Random)
    {
    	if(currentTextureIndex < 15 && currentTextureIndex > -1)
    	{
    		currentTextureIndex++;
    		world.markBlockAsNeedsUpdate(par2, par3, par4);
    	}
    	else if(currentTextureIndex == 15)
    	{
    		currentTextureIndex = 0;
    		world.markBlockAsNeedsUpdate(par2, par3, par4);
    	}
    	
    	int metadata = world.getBlockMetadata(par2, par3, par4);
        if (isActive)
        {
            float var7 = (float)par2 + 0.5F;
            float var8 = (float)par3 + 0.0F + par5Random.nextFloat() * 6.0F / 16.0F;
            float var9 = (float)par4 + 0.5F;
            float var10 = 0.52F;
            float var11 = par5Random.nextFloat() * 0.6F - 0.3F;

            if (metadata == 4)
            {
                world.spawnParticle("smoke", (double)(var7 - var10), (double)var8, (double)(var9 + var11), 0.0D, 0.0D, 0.0D);
                world.spawnParticle("reddust", (double)(var7 - var10), (double)var8, (double)(var9 + var11), 0.0D, 0.0D, 0.0D);
            }
            else if (metadata == 5)
            {
                world.spawnParticle("smoke", (double)(var7 + var10), (double)var8, (double)(var9 + var11), 0.0D, 0.0D, 0.0D);
                world.spawnParticle("reddust", (double)(var7 + var10), (double)var8, (double)(var9 + var11), 0.0D, 0.0D, 0.0D);
            }
            else if (metadata == 2)
            {
                world.spawnParticle("smoke", (double)(var7 + var11), (double)var8, (double)(var9 - var10), 0.0D, 0.0D, 0.0D);
                world.spawnParticle("reddust", (double)(var7 + var11), (double)var8, (double)(var9 - var10), 0.0D, 0.0D, 0.0D);
            }
            else if (metadata == 3)
            {
                world.spawnParticle("smoke", (double)(var7 + var11), (double)var8, (double)(var9 + var10), 0.0D, 0.0D, 0.0D);
                world.spawnParticle("reddust", (double)(var7 + var11), (double)var8, (double)(var9 + var10), 0.0D, 0.0D, 0.0D);
            }
        }
    }
    
    public int getBlockTextureFromSide(int side)
    {
    	if(side == 3)
    	{
    		return 17;
    	}
    	else {
    		return 16;
    	}
    }

    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int i1, float f1, float f2, float f3)
    {
        if (world.isRemote)
        {
            return true;
        }
        else
        {
            TileEntityPlatinumCompressor tileEntity = (TileEntityPlatinumCompressor)world.getBlockTileEntity(x, y, z);

            if (tileEntity != null)
            {
            	if(!entityplayer.isSneaking())
            	{
            		entityplayer.openGui(ObsidianIngots.instance, 22, world, x, y, z);
            	}
            	else {
            		return false;
            	}
            }

            return true;
        }
    }

    public static void updateBlock(boolean active, World world, int x, int y, int z)
    {
    	isActive = active;
    	
        TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
        
       	world.markBlockAsNeedsUpdate(x, y, z);
    	world.updateAllLightTypes(x, y, z);
        
        if (tileEntity != null)
        {
            tileEntity.validate();
            world.setBlockTileEntity(x, y, z, tileEntity);
        }
    }

    public TileEntity createNewTileEntity(World world)
    {
        return new TileEntityPlatinumCompressor();
    }

    public void breakBlock(World world, int par2, int par3, int par4, int i1, int i2)
    {
        TileEntityPlatinumCompressor var5 = (TileEntityPlatinumCompressor)world.getBlockTileEntity(par2, par3, par4);

        if (var5 != null)
        {
            for (int var6 = 0; var6 < var5.getSizeInventory(); ++var6)
            {
                ItemStack var7 = var5.getStackInSlot(var6);

                if (var7 != null)
                {
                    float var8 = this.compressorRand.nextFloat() * 0.8F + 0.1F;
                    float var9 = this.compressorRand.nextFloat() * 0.8F + 0.1F;
                    float var10 = this.compressorRand.nextFloat() * 0.8F + 0.1F;

                    while (var7.stackSize > 0)
                    {
                        int var11 = this.compressorRand.nextInt(21) + 10;

                        if (var11 > var7.stackSize)
                        {
                            var11 = var7.stackSize;
                        }

                        var7.stackSize -= var11;
                        EntityItem var12 = new EntityItem(world, (double)((float)par2 + var8), (double)((float)par3 + var9), (double)((float)par4 + var10), new ItemStack(var7.itemID, var11, var7.getItemDamage()));

                        if (var7.hasTagCompound())
                        {
                            var12.item.setTagCompound((NBTTagCompound)var7.getTagCompound().copy());
                        }

                        float var13 = 0.05F;
                        var12.motionX = (double)((float)this.compressorRand.nextGaussian() * var13);
                        var12.motionY = (double)((float)this.compressorRand.nextGaussian() * var13 + 0.2F);
                        var12.motionZ = (double)((float)this.compressorRand.nextGaussian() * var13);
                        world.spawnEntityInWorld(var12);
                    }
                }
            }
        }

        super.breakBlock(world, par2, par3, par4, i1, i2);
    }
    
    public void addCreativeItems(ArrayList itemList)
    {
    	itemList.add(new ItemStack(this));
    }
    
    public String getTextureFile()
    {
    	return "/obsidian/Compressor.png";
    }
}
