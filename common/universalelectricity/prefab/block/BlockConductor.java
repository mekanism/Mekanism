package universalelectricity.prefab.block;

import java.util.List;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import universalelectricity.core.block.IConductor;
import universalelectricity.core.vector.Vector3;
import universalelectricity.prefab.tile.TileEntityConductor;

public abstract class BlockConductor extends BlockContainer
{
	public boolean isWireCollision = true;
	public Vector3 minVector = new Vector3(0.3, 0.3, 0.3);
	public Vector3 maxVector = new Vector3(0.7, 0.7, 0.7);

	public BlockConductor(int id, Material material)
	{
		super(id, material);
	}

	/**
	 * Called whenever the block is added into the world. Args: world, x, y, z
	 */
	@Override
	public void onBlockAdded(World world, int x, int y, int z)
	{
		super.onBlockAdded(world, x, y, z);

		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof IConductor)
		{
			((IConductor) tileEntity).refresh();
		}
	}

	/**
	 * Lets the block know when one of its neighbor changes. Doesn't know which neighbor changed
	 * (coordinates passed are their own) Args: x, y, z, neighbor blockID
	 */
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int blockID)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof IConductor)
		{
			((IConductor) tileEntity).refresh();
		}
	}

	/**
	 * Returns a bounding box from the pool of bounding boxes (this means this box can change after
	 * the pool has been cleared to be reused)
	 */
	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
	{
		this.setBlockBoundsBasedOnState(world, x, y, z);
		return super.getCollisionBoundingBoxFromPool(world, x, y, z);
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z)
	{
		this.setBlockBoundsBasedOnState(world, x, y, z);
		return super.getSelectedBoundingBoxFromPool(world, x, y, z);
	}

	/**
	 * Returns the bounding box of the wired rectangular prism to render.
	 */
	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
	{
		if (this.isWireCollision)
		{
			TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

			if (tileEntity instanceof TileEntityConductor)
			{
				TileEntity[] connectable = ((TileEntityConductor) tileEntity).getAdjacentConnections();

				if (connectable != null)
				{
					float minX = (float) this.minVector.x;
					float minY = (float) this.minVector.y;
					float minZ = (float) this.minVector.z;
					float maxX = (float) this.maxVector.x;
					float maxY = (float) this.maxVector.y;
					float maxZ = (float) this.maxVector.z;

					if (connectable[0] != null)
					{
						minY = 0.0F;
					}

					if (connectable[1] != null)
					{
						maxY = 1.0F;
					}

					if (connectable[2] != null)
					{
						minZ = 0.0F;
					}

					if (connectable[3] != null)
					{
						maxZ = 1.0F;
					}

					if (connectable[4] != null)
					{
						minX = 0.0F;
					}

					if (connectable[5] != null)
					{
						maxX = 1.0F;
					}

					this.setBlockBounds(minX, minY, minZ, maxX, maxY, maxZ);
				}
			}
		}
	}

	@Override
	public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB axisalignedbb, List list, Entity entity)
	{
		if (this.isWireCollision)
		{
			TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

			if (tileEntity instanceof TileEntityConductor)
			{
				TileEntity[] connectable = ((TileEntityConductor) tileEntity).getAdjacentConnections();

				this.setBlockBounds((float) this.minVector.x, (float) this.minVector.y, (float) this.minVector.z, (float) this.maxVector.x, (float) this.maxVector.y, (float) this.maxVector.z);
				super.addCollisionBoxesToList(world, x, y, z, axisalignedbb, list, entity);

				if (connectable[4] != null)
				{
					this.setBlockBounds(0, (float) this.minVector.y, (float) this.minVector.z, (float) this.maxVector.x, (float) this.maxVector.y, (float) this.maxVector.z);
					super.addCollisionBoxesToList(world, x, y, z, axisalignedbb, list, entity);
				}

				if (connectable[5] != null)
				{
					this.setBlockBounds((float) this.minVector.x, (float) this.minVector.y, (float) this.minVector.z, 1, (float) this.maxVector.y, (float) this.maxVector.z);
					super.addCollisionBoxesToList(world, x, y, z, axisalignedbb, list, entity);
				}

				if (connectable[0] != null)
				{
					this.setBlockBounds((float) this.minVector.x, 0, (float) this.minVector.z, (float) this.maxVector.x, (float) this.maxVector.y, (float) this.maxVector.z);
					super.addCollisionBoxesToList(world, x, y, z, axisalignedbb, list, entity);
				}

				if (connectable[1] != null)
				{
					this.setBlockBounds((float) this.minVector.x, (float) this.minVector.y, (float) this.minVector.z, (float) this.maxVector.x, 1, (float) this.maxVector.z);
					super.addCollisionBoxesToList(world, x, y, z, axisalignedbb, list, entity);
				}

				if (connectable[2] != null)
				{
					this.setBlockBounds((float) this.minVector.x, (float) this.minVector.y, 0, (float) this.maxVector.x, (float) this.maxVector.y, (float) this.maxVector.z);
					super.addCollisionBoxesToList(world, x, y, z, axisalignedbb, list, entity);
				}

				if (connectable[3] != null)
				{
					this.setBlockBounds((float) this.minVector.x, (float) this.minVector.y, (float) this.minVector.z, (float) this.maxVector.x, (float) this.maxVector.y, 1);
					super.addCollisionBoxesToList(world, x, y, z, axisalignedbb, list, entity);
				}

				this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
			}
		}
		else
		{
			super.addCollisionBoxesToList(world, x, y, z, axisalignedbb, list, entity);
		}
	}
}
