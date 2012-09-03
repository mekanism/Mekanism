package net.uberkat.obsidian.common;

import java.util.Random;

import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;
import net.minecraft.src.*;

public class BlockMachine extends BlockContainer
{
	public Random machineRand = new Random();
	
    public static int currentFrontTextureIndex = 0;
    public static int currentBackTextureIndex = 16;
    public static int currentSideTextureIndex = 32;
    
    public String textureFile;
	
	public BlockMachine(int id, String texture)
	{
		super(id, Material.iron);
		textureFile = "/obsidian/" + texture;
	}
	
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving entityliving)
    {
        int side = MathHelper.floor_double((double)(entityliving.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        int change = 3;
        
        switch(side)
        {
        	case 0: change = 2; break;
        	case 1: change = 5; break;
        	case 2: change = 3; break;
        	case 3: change = 4; break;
        }
        
        world.setBlockMetadataWithNotify(x, y, z, change);
    }
	
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World world, int x, int y, int z, Random random)
    {
    	int metadata = world.getBlockMetadata(x, y, z);
        if (ObsidianUtils.isActive(world, x, y, z))
        {
            float var7 = (float)x + 0.5F;
            float var8 = (float)y + 0.0F + random.nextFloat() * 6.0F / 16.0F;
            float var9 = (float)z + 0.5F;
            float var10 = 0.52F;
            float var11 = random.nextFloat() * 0.6F - 0.3F;

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
    
    public static void updateTexture(World world, int x, int y, int z)
    {
    	if(currentFrontTextureIndex < 15 && currentFrontTextureIndex > -1)
    	{
    		currentFrontTextureIndex++;
    	}
    	if(currentFrontTextureIndex == 15)
    	{
    		currentFrontTextureIndex = 0;
    	}
    	
    	if(currentBackTextureIndex < 31 && currentBackTextureIndex > 15)
    	{
    		currentBackTextureIndex++;
    	}
    	if(currentBackTextureIndex == 31)
    	{
    		currentBackTextureIndex = 16;
    	}
    	
    	if(currentSideTextureIndex < 47 && currentSideTextureIndex > 31)
    	{
    		currentSideTextureIndex++;
    	}
    	if(currentSideTextureIndex == 47)
    	{
    		currentSideTextureIndex = 32;
    	}
    	
    	world.markBlockAsNeedsUpdate(x, y, z);
    }
    
    public void breakBlock(World world, int par2, int par3, int par4, int i1, int i2)
    {
        TileEntityMachine var5 = (TileEntityMachine)world.getBlockTileEntity(par2, par3, par4);

        if (var5 != null)
        {
            for (int var6 = 0; var6 < var5.getSizeInventory(); ++var6)
            {
                ItemStack var7 = var5.getStackInSlot(var6);

                if (var7 != null)
                {
                    float var8 = this.machineRand.nextFloat() * 0.8F + 0.1F;
                    float var9 = this.machineRand.nextFloat() * 0.8F + 0.1F;
                    float var10 = this.machineRand.nextFloat() * 0.8F + 0.1F;

                    while (var7.stackSize > 0)
                    {
                        int var11 = this.machineRand.nextInt(21) + 10;

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
                        var12.motionX = (double)((float)this.machineRand.nextGaussian() * var13);
                        var12.motionY = (double)((float)this.machineRand.nextGaussian() * var13 + 0.2F);
                        var12.motionZ = (double)((float)this.machineRand.nextGaussian() * var13);
                        world.spawnEntityInWorld(var12);
                    }
                }
            }
            var5.invalidate();
        }
	        
    	super.breakBlock(world, par2, par3, par4, i1, i2);
    }
    
    public String getTextureFile()
    {
    	return textureFile;
    }
	
	public TileEntity createNewTileEntity(World world)
	{
		return null;
	}
}
