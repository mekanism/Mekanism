package mekanism.common;

import java.util.Arrays;
import java.util.List;

import mekanism.api.GasTransmission;
import mekanism.api.ITransmitter;
import mekanism.api.ITubeConnection;
import mekanism.client.ClientProxy;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.tools.IToolWrench;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Block class for handling multiple transmitter IDs.
 * 0: Pressurized Tube
 * 1: Universal Cable
 * 2: Mechanical Pipe
 * 3: Logistical Transporter
 * @author AidanBrady
 *
 */
public class BlockTransmitter extends Block
{
	public BlockTransmitter(int id)
	{
		super(id, Material.wood);
		setHardness(2.0F);
		setResistance(5.0F);
		setBlockBounds(0.3F, 0.3F, 0.3F, 0.7F, 0.7F, 0.7F);
		setCreativeTab(Mekanism.tabMekanism);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register) {}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(int i, CreativeTabs creativetabs, List list)
	{
		list.add(new ItemStack(i, 1, 0));
		list.add(new ItemStack(i, 1, 1));
		list.add(new ItemStack(i, 1, 2));
		list.add(new ItemStack(i, 1, 3));
	}
	
	@Override
	public int damageDropped(int i)
	{
		return i;
	}
	
	@Override
	public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB axisalignedbb, List list, Entity entity) 
	{
		boolean[] connectable = getConnectable(world, x, y, z);
		
		setBlockBounds(0.3F, 0.3F, 0.3F, 0.7F, 0.7F, 0.7F);
		super.addCollisionBoxesToList(world, x, y, z, axisalignedbb, list, entity);

		if(connectable[4]) 
		{
			setBlockBounds(0.0F, 0.3F, 0.3F, 0.7F, 0.7F, 0.7F);
			super.addCollisionBoxesToList(world, x, y, z, axisalignedbb, list, entity);
		}

		if(connectable[5]) 
		{
			setBlockBounds(0.3F, 0.3F, 0.3F, 1.0F, 0.7F, 0.7F);
			super.addCollisionBoxesToList(world, x, y, z, axisalignedbb, list, entity);
		}

		if(connectable[0]) 
		{
			setBlockBounds(0.3F, 0.0F, 0.3F, 0.7F, 0.7F, 0.7F);
			super.addCollisionBoxesToList(world, x, y, z, axisalignedbb, list, entity);
		}

		if(connectable[1])
		{
			setBlockBounds(0.3F, 0.3F, 0.3F, 0.7F, 1.0F, 0.7F);
			super.addCollisionBoxesToList(world, x, y, z, axisalignedbb, list, entity);
		}

		if(connectable[2])
		{
			setBlockBounds(0.3F, 0.3F, 0.0F, 0.7F, 0.7F, 0.7F);
			super.addCollisionBoxesToList(world, x, y, z, axisalignedbb, list, entity);
		}

		if(connectable[3])
		{
			setBlockBounds(0.3F, 0.3F, 0.3F, 0.7F, 0.7F, 1.0F);
			super.addCollisionBoxesToList(world, x, y, z, axisalignedbb, list, entity);
		}

		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) 
	{
		boolean[] connectable = getConnectable(world, x, y, z);
		
		if(connectable != null)
		{
			float minX = 0.3F;
			float minY = 0.3F;
			float minZ = 0.3F;
			float maxX = 0.7F;
			float maxY = 0.7F;
			float maxZ = 0.7F;
	
			if(connectable[0])
			{
				minY = 0.0F;
			}
			
			if(connectable[1])
			{
				maxY = 1.0F;
			}
			
			if(connectable[2])
			{
				minZ = 0.0F;
			}
			
			if(connectable[3])
			{
				maxZ = 1.0F;
			}
			
			if(connectable[4])
			{
				minX = 0.0F;
			}
			
			if(connectable[5])
			{
				maxX = 1.0F;
			}
	
			return AxisAlignedBB.getBoundingBox(x + minX, y + minY, z + minZ, x + maxX, y + maxY, z + maxZ);
		}
		
		return super.getSelectedBoundingBoxFromPool(world, x, y, z);
	}
	
	public boolean[] getConnectable(IBlockAccess world, int x, int y, int z)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		
		boolean[] connectable = null;
		
		if(tileEntity != null)
		{	
			connectable = new boolean[] {false, false, false, false, false, false};
			
			if(world.getBlockMetadata(x, y, z) == 0)
			{
				ITubeConnection[] connections = GasTransmission.getConnections(tileEntity);
				
				for(ITubeConnection connection : connections)
				{
					if(connection != null)
					{
						int side = Arrays.asList(connections).indexOf(connection);
						
						if(connection.canTubeConnect(ForgeDirection.getOrientation(side).getOpposite()))
						{
							connectable[side] = true;
						}
					}
				}
			}
			else if(world.getBlockMetadata(x, y, z) == 1)
			{
				connectable = CableUtils.getConnections(tileEntity);
			}
			else if(world.getBlockMetadata(x, y, z) == 2)
			{
				connectable = PipeUtils.getConnections(tileEntity);
			}
			else if(world.getBlockMetadata(x, y, z) == 3)
			{
				connectable = TransporterUtils.getConnections(tileEntity);
			}
		}
		
		return connectable;
	}
	
	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		
		float minX = 0.3F;
		float minY = 0.3F;
		float minZ = 0.3F;
		float maxX = 0.7F;
		float maxY = 0.7F;
		float maxZ = 0.7F;
		
		boolean[] connectable = getConnectable(world, x, y, z);
			
		if(connectable != null)
		{
			if(connectable[0])
			{
				minY = 0.0F;
			}
			
			if(connectable[1])
			{
				maxY = 1.0F;
			}
			
			if(connectable[2])
			{
				minZ = 0.0F;
			}
			
			if(connectable[3])
			{
				maxZ = 1.0F;
			}
			
			if(connectable[4])
			{
				minX = 0.0F;
			}
			
			if(connectable[5])
			{
				maxX = 1.0F;
			}
			
			setBlockBounds(minX, minY, minZ, maxX, maxY, maxZ);
		}
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int blockID)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if(!world.isRemote)
		{
			((ITransmitter)tileEntity).refreshNetwork();
		}
	}
	
	@Override
	public void onBlockAdded(World world, int x, int y, int z)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if(!world.isRemote)
		{
			((ITransmitter)tileEntity).refreshNetwork();
		}
	}
	
	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}
	
	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderType()
	{
		return ClientProxy.TRANSMITTER_RENDER_ID;
	}
	
	@Override
	public boolean hasTileEntity(int metadata)
	{
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(World world, int metadata)
	{
		switch(metadata)
		{
			case 0:
				return new TileEntityPressurizedTube();
			case 1:
				return new TileEntityUniversalCable();
			case 2:
				return new TileEntityMechanicalPipe();
			case 3:
				return new TileEntityLogisticalTransporter();
			default:
				return null;
		}
	}
	
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int facing, float playerX, float playerY, float playerZ)
    {
    	if(world.isRemote)
    	{
    		return true;
    	}
    	
    	if(entityplayer.getCurrentEquippedItem() != null)
    	{
    		Item tool = entityplayer.getCurrentEquippedItem().getItem();
    		
	    	if(tool instanceof IToolWrench && ((IToolWrench)tool).canWrench(entityplayer, x, y, z))
	    	{
	    		if(entityplayer.isSneaking())
	    		{
	    			dismantleBlock(world, x, y, z, false);
	    		}
	    		
	    		return true;
	    	}
    	}
    	
    	return false;
    }
    
	public ItemStack dismantleBlock(World world, int x, int y, int z, boolean returnBlock) 
	{
		int meta = world.getBlockMetadata(x, y, z);
		ItemStack itemStack = new ItemStack(blockID, 1, meta);
        
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
}
