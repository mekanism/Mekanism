package net.uberkat.obsidian.common;

import java.util.List;
import java.util.Random;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;
import net.minecraft.src.*;
import net.minecraftforge.common.ForgeDirection;

/**
 * Block class for handling multiple machine block IDs.
 * 0: Enrichment Chamber
 * 1: Platinum Compressor
 * 2: Combiner
 * 3: Crusher
 * 4: Theoretical Elementizer
 * @author AidanBrady
 *
 */
public class BlockMachine extends BlockContainer
{
	public Random machineRand = new Random();
	
    public int textureIndex = 32;
	
	public BlockMachine(int id)
	{
		super(id, Material.iron);
		setHardness(3.5F);
		setResistance(8F);
		setCreativeTab(CreativeTabs.tabDecorations);
		setRequiresSelfNotify();
	}
	
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving entityliving)
    {
    	TileEntityBasicMachine tileEntity = (TileEntityBasicMachine)world.getBlockTileEntity(x, y, z);
        int side = MathHelper.floor_double((double)(entityliving.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        int change = 3;
        
        switch(side)
        {
        	case 0: change = 2; break;
        	case 1: change = 5; break;
        	case 2: change = 3; break;
        	case 3: change = 4; break;
        }
        
        tileEntity.setFacing((short)change);
    }
	
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World world, int x, int y, int z, Random random)
    {
    	TileEntityBasicMachine tileEntity = (TileEntityBasicMachine)world.getBlockTileEntity(x, y, z);
        if (isActive(world, x, y, z))
        {
            float var7 = (float)x + 0.5F;
            float var8 = (float)y + 0.0F + random.nextFloat() * 6.0F / 16.0F;
            float var9 = (float)z + 0.5F;
            float var10 = 0.52F;
            float var11 = random.nextFloat() * 0.6F - 0.3F;

            if (tileEntity.facing == 4)
            {
                world.spawnParticle("smoke", (double)(var7 - var10), (double)var8, (double)(var9 + var11), 0.0D, 0.0D, 0.0D);
                world.spawnParticle("reddust", (double)(var7 - var10), (double)var8, (double)(var9 + var11), 0.0D, 0.0D, 0.0D);
            }
            else if (tileEntity.facing == 5)
            {
                world.spawnParticle("smoke", (double)(var7 + var10), (double)var8, (double)(var9 + var11), 0.0D, 0.0D, 0.0D);
                world.spawnParticle("reddust", (double)(var7 + var10), (double)var8, (double)(var9 + var11), 0.0D, 0.0D, 0.0D);
            }
            else if (tileEntity.facing == 2)
            {
                world.spawnParticle("smoke", (double)(var7 + var11), (double)var8, (double)(var9 - var10), 0.0D, 0.0D, 0.0D);
                world.spawnParticle("reddust", (double)(var7 + var11), (double)var8, (double)(var9 - var10), 0.0D, 0.0D, 0.0D);
            }
            else if (tileEntity.facing == 3)
            {
                world.spawnParticle("smoke", (double)(var7 + var11), (double)var8, (double)(var9 + var10), 0.0D, 0.0D, 0.0D);
                world.spawnParticle("reddust", (double)(var7 + var11), (double)var8, (double)(var9 + var10), 0.0D, 0.0D, 0.0D);
            }
        }
    }
    
    public int getBlockTextureFromSideAndMetadata(int side, int meta)
    {
    	if(meta == 0)
    	{
        	if(side == 3)
        	{
        		return 9;
        	}
        	else {
        		return 2;
        	}
    	}
    	else if(meta == 1)
    	{
        	if(side == 3)
        	{
        		return 14;
        	}
        	else {
        		return 2;
        	}
    	}
    	else if(meta == 2)
    	{
        	if(side == 3)
        	{
        		return 15;
        	}
        	else {
        		return 2;
        	}
    	}
    	else if(meta == 3)
    	{
        	if(side == 3)
        	{
        		return 13;
        	}
        	else {
        		return 2;
        	}
    	}
    	else if(meta == 4)
    	{
        	if(side == 0 || side == 1)
        	{
        		return 18;
        	}
        	else if(side == 3)
        	{
        		return 16;
        	}
        	else {
        		return 19;
        	}
    	}
    	else {
    		return 0;
    	}
    }
    
    @SideOnly(Side.CLIENT)
    public int getBlockTexture(IBlockAccess world, int x, int y, int z, int side)
    {
    	int metadata = world.getBlockMetadata(x, y, z);
    	TileEntityBasicMachine tileEntity = (TileEntityBasicMachine)world.getBlockTileEntity(x, y, z);
        
    	if(metadata == 0)
    	{
	        if(side == tileEntity.facing)
	        {
	        	return isActive(world, x, y, z) ? 8 : 9;
	        }
	        else {
	        	return 2;
	        }
    	}
    	else if(metadata == 1)
    	{
            if(side == tileEntity.facing)
            {
            	return isActive(world, x, y, z) ? ObsidianIngots.ANIMATED_TEXTURE_INDEX : 14;
            	
            }
            else {
            	return 2;
            }
    	}
    	else if(metadata == 2)
    	{
            if(side == tileEntity.facing)
            {
            	return isActive(world, x, y, z) ? ObsidianIngots.ANIMATED_TEXTURE_INDEX+1 : 15;
            }
            else {
            	return 2;
            }
    	}
    	else if(metadata == 3)
    	{
            if(side == tileEntity.facing)
            {
            	return isActive(world, x, y, z) ? 12 : 13;
            }
            else {
            	return 2;
            }
    	}
    	else if(metadata == 4)
    	{
            if(side == 0 || side == 1)
            {
            	return isActive(world, x, y, z) ? 20 : 18;
            }
            else {
            	if(side == tileEntity.facing)
            	{
            		return isActive(world, x, y, z) ? ObsidianIngots.ANIMATED_TEXTURE_INDEX+2 : 16;
            	}
            	else if(side == ForgeDirection.getOrientation(tileEntity.facing).getOpposite().ordinal())
            	{
            		return isActive(world, x, y, z) ? ObsidianIngots.ANIMATED_TEXTURE_INDEX+3 : 17;
            	}
            	else {
            		return isActive(world, x, y, z) ? ObsidianIngots.ANIMATED_TEXTURE_INDEX+4 : 19;
            	}
            }
    	}
    	else {
    		return 0;
    	}
    }
    
    public int damageDropped(int i)
    {
    	return i;
    }
    
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(int i, CreativeTabs creativetabs, List list)
	{
		list.add(new ItemStack(i, 1, 0));
		list.add(new ItemStack(i, 1, 1));
		list.add(new ItemStack(i, 1, 2));
		list.add(new ItemStack(i, 1, 3));
		list.add(new ItemStack(i, 1, 4));
	}
    
	/**
	 * Checks if a machine is in it's active state.
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @return if machine is active
	 */
    public boolean isActive(IBlockAccess world, int x, int y, int z)
    {
    	TileEntityBasicMachine tileEntity = (TileEntityBasicMachine)world.getBlockTileEntity(x, y, z);
    	if(tileEntity != null)
    	{
    		return tileEntity.isActive;
    	}
    	return false;
    }
    
    public void breakBlock(World world, int par2, int par3, int par4, int i1, int i2)
    {
        TileEntityBasicMachine var5 = (TileEntityBasicMachine)world.getBlockTileEntity(par2, par3, par4);

        if (var5 != null)
        {
            for (int var6 = 0; var6 < var5.getSizeInventory(); ++var6)
            {
                ItemStack var7 = var5.getStackInSlot(var6);

                if (var7 != null)
                {
                    float var8 = machineRand.nextFloat() * 0.8F + 0.1F;
                    float var9 = machineRand.nextFloat() * 0.8F + 0.1F;
                    float var10 = machineRand.nextFloat() * 0.8F + 0.1F;

                    while (var7.stackSize > 0)
                    {
                        int var11 = machineRand.nextInt(21) + 10;

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
                        var12.motionX = (double)((float)machineRand.nextGaussian() * var13);
                        var12.motionY = (double)((float)machineRand.nextGaussian() * var13 + 0.2F);
                        var12.motionZ = (double)((float)machineRand.nextGaussian() * var13);
                        world.spawnEntityInWorld(var12);
                    }
                }
            }
            var5.invalidate();
        }
	        
    	super.breakBlock(world, par2, par3, par4, i1, i2);
    }
    
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int i1, float f1, float f2, float f3)
    {
        if (world.isRemote)
        {
            return true;
        }
        else
        {
        	TileEntityBasicMachine tileEntity = (TileEntityBasicMachine)world.getBlockTileEntity(x, y, z);
        	int metadata = world.getBlockMetadata(x, y, z);
        	
            if (tileEntity != null)
            {
            	if(!entityplayer.isSneaking())
            	{
            		int id = 0;
            		
            		if(metadata == 0) id = 3;
            		else if(metadata == 1) id = 4;
            		else if(metadata == 2) id = 5;
            		else if(metadata == 3) id = 6;
            		else if(metadata == 4) id = 7;
            		
            		entityplayer.openGui(ObsidianIngots.instance, id, world, x, y, z);
            	}
            	else {
            		return false;
            	}
            }
            return true;
        }
    }
    
    public String getTextureFile()
    {
    	return "/obsidian/terrain.png";
    }
    
    public TileEntity createNewTileEntity(World world, int metadata)
    {
    	if(metadata == MachineType.ENRICHMENT_CHAMBER.index)
    	{
    		return new TileEntityEnrichmentChamber();
    	}
    	else if(metadata == MachineType.PLATINUM_COMPRESSOR.index)
    	{
    		return new TileEntityPlatinumCompressor();
    	}
    	else if(metadata == MachineType.COMBINER.index)
    	{
    		return new TileEntityCombiner();
    	}
    	else if(metadata == MachineType.CRUSHER.index)
    	{
    		return new TileEntityCrusher();
    	}
    	else if(metadata == MachineType.THEORETICAL_ELEMENTIZER.index)
    	{
    		return new TileEntityTheoreticalElementizer();
    	}
    	else {
    		return null;
    	}
    }
	
    //This method is not used, metadata manipulation is required to create a Tile Entity.
	public TileEntity createNewTileEntity(World world)
	{
		return null;
	}
	
	public static enum MachineType
	{
		ENRICHMENT_CHAMBER(0),
		PLATINUM_COMPRESSOR(1),
		COMBINER(2),
		CRUSHER(3),
		THEORETICAL_ELEMENTIZER(4);
		
		private int index;
		
		private MachineType(int i)
		{
			index = i;
		}
	}
}
