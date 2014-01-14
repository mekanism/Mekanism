package mekanism.common.block;

import java.util.Random;

import mekanism.common.Mekanism;
import mekanism.common.item.ItemBlockCardboardBox;
import mekanism.common.tile.TileEntityCardboardBox;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockCardboardBox extends BlockContainer
{
	public Icon[] icons = new Icon[6];
	
	public BlockCardboardBox(int id)
	{
		super(id, Material.cloth);
		setCreativeTab(Mekanism.tabMekanism);
		setHardness(0.5F);
		setResistance(1F);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register)
	{
		icons[0] = register.registerIcon("mekanism:CardboardBoxTop");
		icons[1] = register.registerIcon("mekanism:CardboardBoxSide");
		icons[2] = register.registerIcon("mekanism:CardboardBoxSideStorage");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta)
	{
		if(side == 0 || side == 1)
		{
			return icons[0];
		}
		else {
			return meta == 0 ? icons[1] : icons[2];
		}
	}
	
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int facing, float hitX, float hitY, float hitZ)
    {
    	if(!world.isRemote && entityplayer.isSneaking())
    	{
    		ItemStack itemStack = new ItemStack(Mekanism.CardboardBox);
    		TileEntityCardboardBox tileEntity = (TileEntityCardboardBox)world.getBlockTileEntity(x, y, z);
    		
    		if(tileEntity.storedData != null)
    		{
    			BlockData data = tileEntity.storedData;
    			
    			if(Block.blocksList[data.id] != null)
    			{
    				data.meta = Block.blocksList[data.id].onBlockPlaced(world, x, y, z, facing, hitX, hitY, hitZ, data.meta);
    			}
    			
    			world.setBlock(x, y, z, data.id, data.meta, 3);
    			
    			if(data.tileTag != null && world.getBlockTileEntity(x, y, z) != null)
    			{
    				data.updateLocation(x, y, z);
    				world.getBlockTileEntity(x, y, z).readFromNBT(data.tileTag);
    			}
    			
    			if(Block.blocksList[data.id] != null)
    			{
    				Block.blocksList[data.id].onBlockPlacedBy(world, x, y, z, entityplayer, new ItemStack(data.id, 1, data.meta));
    				Block.blocksList[data.id].onPostBlockPlaced(world, x, y, z, data.meta);
    			}
        		
        	    float motion = 0.7F;
                double motionX = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
                double motionY = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
                double motionZ = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
                
                EntityItem entityItem = new EntityItem(world, x + motionX, y + motionY, z + motionZ, itemStack);
    	        
                world.spawnEntityInWorld(entityItem);
    		}
    	}
    	
    	return false;
    }
    
	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityCardboardBox();
	}
	
	public ItemStack dismantleBlock(World world, int x, int y, int z, boolean returnBlock) 
	{
        ItemStack itemStack = getPickBlock(null, world, x, y, z);

        world.setBlockToAir(x, y, z);
        
        if(!returnBlock)
        {
            float motion = 0.7F;
            double motionX = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
            double motionY = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
            double motionZ = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
            
            EntityItem entityItem = new EntityItem(world, x + motionX, y + motionY, z + motionZ, itemStack);
	        
            world.spawnEntityInWorld(entityItem);
        }
        
        return itemStack;
	}
	
	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
	{
		TileEntityCardboardBox tileEntity = (TileEntityCardboardBox)world.getBlockTileEntity(x, y, z);
    	ItemStack itemStack = new ItemStack(Mekanism.CardboardBox, 1, world.getBlockMetadata(x, y, z));
    	
    	if(tileEntity.storedData != null)
    	{
    		((ItemBlockCardboardBox)itemStack.getItem()).setBlockData(itemStack, tileEntity.storedData);
    	}
        
		return itemStack;
	}
	
    @Override
    public boolean removeBlockByPlayer(World world, EntityPlayer player, int x, int y, int z)
    {
    	if(!player.capabilities.isCreativeMode && !world.isRemote && canHarvestBlock(player, world.getBlockMetadata(x, y, z)))
    	{
            float motion = 0.7F;
            double motionX = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
            double motionY = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
            double motionZ = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
            
            EntityItem entityItem = new EntityItem(world, x + motionX, y + motionY, z + motionZ, getPickBlock(null, world, x, y, z));
	        
	        world.spawnEntityInWorld(entityItem);
    	}
    	
        return world.setBlockToAir(x, y, z);
    }
    
	@Override
	public int quantityDropped(Random random) 
	{
		return 0;
	}

	@Override
	public int idDropped(int i, Random random, int j)
	{
		return 0;
	}
	
	public static class BlockData
	{
		public int id;
		public int meta;
		public NBTTagCompound tileTag;
		
		public BlockData(int i, int j, NBTTagCompound nbtTags)
		{
			id = i;
			meta = j;
			tileTag = nbtTags;
		}
		
		public BlockData() {}
		
		public void updateLocation(int x, int y, int z)
		{
			if(tileTag != null)
			{
				tileTag.setInteger("x", x);
				tileTag.setInteger("y", y);
				tileTag.setInteger("z", z);
			}
		}
		
		public NBTTagCompound write(NBTTagCompound nbtTags)
		{
			nbtTags.setInteger("id", id);
			nbtTags.setInteger("meta", meta);
			
			if(tileTag != null)
			{
				nbtTags.setCompoundTag("tileTag", tileTag);
			}
			
			return nbtTags;
		}
		
		public static BlockData read(NBTTagCompound nbtTags)
		{
			BlockData data = new BlockData();
			
			data.id = nbtTags.getInteger("id");
			data.meta = nbtTags.getInteger("meta");
			
			if(nbtTags.hasKey("tileTag"))
			{
				data.tileTag = nbtTags.getCompoundTag("tileTag");
			}
			
			return data;
		}
	}
}
