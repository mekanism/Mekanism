package mekanism.common;

import java.util.Arrays;
import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import mekanism.api.GasTransmission;
import mekanism.api.IGasAcceptor;
import mekanism.api.ITubeConnection;
import mekanism.client.ClientProxy;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.common.ForgeDirection;

public class BlockPressurizedTube extends Block
{
	public BlockPressurizedTube(int id)
	{
		super(id, Material.wood);
		setHardness(2.0F);
		setResistance(5.0F);
		setBlockBounds(0.3F, 0.3F, 0.3F, 0.7F, 0.7F, 0.7F);
		setCreativeTab(Mekanism.tabMekanism);
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
		
		if(tileEntity != null)
		{
			boolean[] connectable = new boolean[] {false, false, false, false, false, false};
			ITubeConnection[] connections = GasTransmission.getConnections(tileEntity);
			
			for(ITubeConnection connection : connections)
			{
				if(connection !=  null)
				{
					int side = Arrays.asList(connections).indexOf(connection);
					
					if(connection.canTubeConnect(ForgeDirection.getOrientation(side).getOpposite()))
					{
						connectable[side] = true;
					}
				}
			}
			
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
		return ClientProxy.TUBE_RENDER_ID;
	}
	
	@Override
	public boolean hasTileEntity(int metadata)
	{
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(World world, int metadata)
	{
		return new TileEntityPressurizedTube();
	}
}
