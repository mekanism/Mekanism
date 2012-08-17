package net.uberkat.obsidian.common;

import java.util.ArrayList;
import java.util.Random;

import cpw.mods.fml.common.registry.BlockProxy;
import net.minecraft.src.*;

public class BlockPlatinumCompressor extends BlockContainer
{
    private Random compressorRand = new Random();

    private static boolean keepCompressorInventory = false;
    
    private static int currentTextureIndex;

    public BlockPlatinumCompressor(int par1)
    {
        super(par1, Material.iron);
    }
    
    private void setDefaultDirection(World par1World, int par2, int par3, int par4)
    {
        if (!par1World.isRemote)
        {
            int var5 = par1World.getBlockId(par2, par3, par4 - 1);
            int var6 = par1World.getBlockId(par2, par3, par4 + 1);
            int var7 = par1World.getBlockId(par2 - 1, par3, par4);
            int var8 = par1World.getBlockId(par2 + 1, par3, par4);
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

            par1World.setBlockMetadataWithNotify(par2, par3, par4, var9);
        }
    }
    
    public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLiving par5EntityLiving)
    {
        int var6 = MathHelper.floor_double((double)(par5EntityLiving.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;

        if (var6 == 0)
        {
            par1World.setBlockMetadataWithNotify(par2, par3, par4, 2);
        }

        if (var6 == 1)
        {
            par1World.setBlockMetadataWithNotify(par2, par3, par4, 5);
        }

        if (var6 == 2)
        {
            par1World.setBlockMetadataWithNotify(par2, par3, par4, 3);
        }

        if (var6 == 3)
        {
            par1World.setBlockMetadataWithNotify(par2, par3, par4, 4);
        }
    }

    public int idDropped(int par1, Random par2Random, int par3)
    {
        return ObsidianIngots.platinumCompressorID;
    }

    public void onBlockAdded(World par1World, int par2, int par3, int par4)
    {
    	setDefaultDirection(par1World, par2, par3, par4);
        super.onBlockAdded(par1World, par2, par3, par4);
    }
    
    public int getLightValue(IBlockAccess world, int x, int y, int z) 
    {
	    int metadata = world.getBlockMetadata(x, y, z);
	    if(metadata > 5) return 14;
	    else return 0;
    }

    public int getBlockTexture(IBlockAccess world, int x, int y, int z, int side)
    {
        int metadata = world.getBlockMetadata(x, y, z);
        int sideMeta = (metadata > 5 ? metadata - 8 : metadata);
        return side != sideMeta ? 16 : (metadata > 5 ? currentTextureIndex : 17);
    }

    public void randomDisplayTick(World par1World, int par2, int par3, int par4, Random par5Random)
    {
    	if(currentTextureIndex < 15 && currentTextureIndex > -1)
    	{
    		currentTextureIndex++;
    		par1World.markBlockAsNeedsUpdate(par2, par3, par4);
    	}
    	else if(currentTextureIndex == 15)
    	{
    		currentTextureIndex = 0;
    		par1World.markBlockAsNeedsUpdate(par2, par3, par4);
    	}
    	
    	int var6 = par1World.getBlockMetadata(par2, par3, par4);
        if (var6 > 5)
        {
        	int metadata = (var6 - 8);
            float var7 = (float)par2 + 0.5F;
            float var8 = (float)par3 + 0.0F + par5Random.nextFloat() * 6.0F / 16.0F;
            float var9 = (float)par4 + 0.5F;
            float var10 = 0.52F;
            float var11 = par5Random.nextFloat() * 0.6F - 0.3F;

            if (metadata == 4)
            {
                par1World.spawnParticle("smoke", (double)(var7 - var10), (double)var8, (double)(var9 + var11), 0.0D, 0.0D, 0.0D);
                par1World.spawnParticle("reddust", (double)(var7 - var10), (double)var8, (double)(var9 + var11), 0.0D, 0.0D, 0.0D);
            }
            else if (metadata == 5)
            {
                par1World.spawnParticle("smoke", (double)(var7 + var10), (double)var8, (double)(var9 + var11), 0.0D, 0.0D, 0.0D);
                par1World.spawnParticle("reddust", (double)(var7 + var10), (double)var8, (double)(var9 + var11), 0.0D, 0.0D, 0.0D);
            }
            else if (metadata == 2)
            {
                par1World.spawnParticle("smoke", (double)(var7 + var11), (double)var8, (double)(var9 - var10), 0.0D, 0.0D, 0.0D);
                par1World.spawnParticle("reddust", (double)(var7 + var11), (double)var8, (double)(var9 - var10), 0.0D, 0.0D, 0.0D);
            }
            else if (metadata == 3)
            {
                par1World.spawnParticle("smoke", (double)(var7 + var11), (double)var8, (double)(var9 + var10), 0.0D, 0.0D, 0.0D);
                par1World.spawnParticle("reddust", (double)(var7 + var11), (double)var8, (double)(var9 + var10), 0.0D, 0.0D, 0.0D);
            }
        }
    }
    
    public int getBlockTextureFromSideAndMetadata(int side, int meta)
    {
    	return side != 3 ? 16 : (meta > 5 ? 0 : 17);
    }

    public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int i1, float f1, float f2, float f3)
    {
        if (par1World.isRemote)
        {
            return true;
        }
        else
        {
            TileEntityPlatinumCompressor var6 = (TileEntityPlatinumCompressor)par1World.getBlockTileEntity(par2, par3, par4);

            if (var6 != null)
            {
                par5EntityPlayer.openGui(ObsidianIngots.instance, 22, par1World, par2, par3, par4);
            }

            return true;
        }
    }

    public static void updateCompressorBlockState(boolean par0, World par1World, int par2, int par3, int par4)
    {
        int var5 = par1World.getBlockMetadata(par2, par3, par4);
        TileEntity var6 = par1World.getBlockTileEntity(par2, par3, par4);
        keepCompressorInventory = true;

        if (par0)
        {
        	if(var5 <= 5)
        	{
	        	par1World.setBlockMetadataWithNotify(par2, par3, par4, var5+8);
	        	par1World.markBlockAsNeedsUpdate(par2, par3, par4);
	        	par1World.updateAllLightTypes(par2, par3, par4);
        	}
        }
        else
        {
        	if(var5 > 5)
        	{
	        	par1World.setBlockMetadataWithNotify(par2, par3, par4, var5-8);
	           	par1World.markBlockAsNeedsUpdate(par2, par3, par4);
	        	par1World.updateAllLightTypes(par2, par3, par4);
        	}
        }

        keepCompressorInventory = false;

        if (var6 != null)
        {
            var6.validate();
            par1World.setBlockTileEntity(par2, par3, par4, var6);
        }
    }

    public TileEntity createNewTileEntity(World world)
    {
        return new TileEntityPlatinumCompressor();
    }

    public void breakBlock(World par1World, int par2, int par3, int par4, int i1, int i2)
    {
        if (!keepCompressorInventory)
        {
            TileEntityPlatinumCompressor var5 = (TileEntityPlatinumCompressor)par1World.getBlockTileEntity(par2, par3, par4);

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
                            EntityItem var12 = new EntityItem(par1World, (double)((float)par2 + var8), (double)((float)par3 + var9), (double)((float)par4 + var10), new ItemStack(var7.itemID, var11, var7.getItemDamage()));

                            if (var7.hasTagCompound())
                            {
                                var12.item.setTagCompound((NBTTagCompound)var7.getTagCompound().copy());
                            }

                            float var13 = 0.05F;
                            var12.motionX = (double)((float)this.compressorRand.nextGaussian() * var13);
                            var12.motionY = (double)((float)this.compressorRand.nextGaussian() * var13 + 0.2F);
                            var12.motionZ = (double)((float)this.compressorRand.nextGaussian() * var13);
                            par1World.spawnEntityInWorld(var12);
                        }
                    }
                }
            }
        }

        super.breakBlock(par1World, par2, par3, par4, i1, i2);
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
