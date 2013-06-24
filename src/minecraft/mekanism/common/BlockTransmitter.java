package mekanism.common;

import java.util.Arrays;
import java.util.List;

import mekanism.api.GasTransmission;
import mekanism.api.ITubeConnection;
import mekanism.api.IUniversalCable;
import mekanism.client.ClientProxy;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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
				TileEntity[] connectedAcceptors = CableUtils.getConnectedEnergyAcceptors(tileEntity);
				TileEntity[] connectedCables = CableUtils.getConnectedCables(tileEntity);
				TileEntity[] connectedOutputters = CableUtils.getConnectedOutputters(tileEntity);
				
				for(TileEntity tile : connectedAcceptors)
				{
					int side = Arrays.asList(connectedAcceptors).indexOf(tile);
					
					if(CableUtils.canConnectToAcceptor(ForgeDirection.getOrientation(side), tileEntity))
					{
						connectable[side] = true;
					}
				}
				
				for(TileEntity tile : connectedOutputters)
				{
					if(tile != null)
					{
						int side = Arrays.asList(connectedOutputters).indexOf(tile);
						
						connectable[side] = true;
					}
				}
				
				for(TileEntity tile : connectedCables)
				{
					if(tile != null)
					{
						int side = Arrays.asList(connectedCables).indexOf(tile);
						
						connectable[side] = true;
					}
				}
			}
			else if(world.getBlockMetadata(x, y, z) == 2)
			{
				TileEntity[] connectedPipes = PipeUtils.getConnectedPipes(tileEntity);
				ITankContainer[] connectedAcceptors = PipeUtils.getConnectedAcceptors(tileEntity);
				
				for(ITankContainer container : connectedAcceptors)
				{
					if(container != null)
					{
						int side = Arrays.asList(connectedAcceptors).indexOf(container);
						
						if(container.getTanks(ForgeDirection.getOrientation(side).getOpposite()) != null && container.getTanks(ForgeDirection.getOrientation(side).getOpposite()).length != 0)
						{
							connectable[side] = true;
						}
						else if(container.getTank(ForgeDirection.getOrientation(side).getOpposite(), new LiquidStack(-1, 1000)) != null)
						{
							connectable[side] = true;
						}
					}
				}
				
				for(TileEntity tile : connectedPipes)
				{
					if(tile != null)
					{
						int side = Arrays.asList(connectedPipes).indexOf(tile);
						
						connectable[side] = true;
					}
				}
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
			if(tileEntity instanceof IUniversalCable)
			{
				((IUniversalCable)tileEntity).refreshNetwork();
			}
		}
	}
	
	@Override
	public void onBlockAdded(World world, int x, int y, int z)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if(!world.isRemote)
		{
			if(tileEntity instanceof IUniversalCable)
			{
				((IUniversalCable)tileEntity).refreshNetwork();
			}
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
			default:
				return null;
		}
	}
}
