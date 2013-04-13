package universalelectricity.prefab.vector;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import universalelectricity.core.vector.Vector3;

/**
 * A cubical region class.
 * 
 * @author Calclavia
 */
public class Region3
{
	public Vector3 min;
	public Vector3 max;

	public Region3()
	{
		this(new Vector3(), new Vector3());
	}

	public Region3(Vector3 min, Vector3 max)
	{
		this.min = min;
		this.max = max;
	}

	public Region3(AxisAlignedBB aabb)
	{
		this.min = new Vector3(aabb.minX, aabb.minY, aabb.minZ);
		this.max = new Vector3(aabb.maxX, aabb.maxY, aabb.maxZ);
	}

	public AxisAlignedBB toAABB()
	{
		return AxisAlignedBB.getBoundingBox(this.min.x, this.min.y, this.min.z, this.max.x, this.max.y, this.max.z);
	}

	public Region2 toRegion2()
	{
		return new Region2(this.min.toVector2(), this.max.toVector2());
	}

	/**
	 * Checks if a point is located inside a region
	 */
	public boolean isIn(Vector3 point)
	{
		return (point.x > this.min.x && point.x < this.max.x) && (point.y > this.min.y && point.y < this.max.y) && (point.z > this.min.z && point.z < this.max.z);
	}

	/**
	 * Returns whether the given region intersects with this one.
	 */
	public boolean isIn(Region3 region)
	{
		return region.max.x > this.min.x && region.min.x < this.max.x ? (region.max.y > this.min.y && region.min.y < this.max.y ? region.max.z > this.min.z && region.min.z < this.max.z : false) : false;
	}

	public void expand(Vector3 difference)
	{
		this.min.subtract(difference);
		this.max.add(difference);
	}

	/**
	 * @return List of vectors within this region.
	 */
	public List<Vector3> getVectors()
	{
		List<Vector3> vectors = new ArrayList<Vector3>();

		for (int x = this.min.intX(); x < this.max.intX(); x++)
		{
			for (int y = this.min.intY(); x < this.max.intY(); y++)
			{
				for (int z = this.min.intZ(); x < this.max.intZ(); z++)
				{
					vectors.add(new Vector3(x, y, z));
				}
			}
		}

		return vectors;
	}

	public List<Vector3> getVectors(Vector3 center, int radius)
	{
		List<Vector3> vectors = new ArrayList<Vector3>();

		for (int x = this.min.intX(); x < this.max.intX(); x++)
		{
			for (int y = this.min.intY(); x < this.max.intY(); y++)
			{
				for (int z = this.min.intZ(); x < this.max.intZ(); z++)
				{
					Vector3 vector3 = new Vector3(x, y, z);

					if (center.distanceTo(vector3) <= radius)
					{
						vectors.add(vector3);
					}
				}
			}
		}

		return vectors;
	}

	/**
	 * Returns all entities in this region.
	 */
	public List<Entity> getEntities(World world, Class<? extends Entity> entityClass)
	{
		return world.getEntitiesWithinAABB(entityClass, this.toAABB());
	}

	public List<Entity> getEntitiesExlude(World world, Entity entity)
	{
		return world.getEntitiesWithinAABBExcludingEntity(entity, this.toAABB());
	}

	public List<Entity> getEntities(World world)
	{
		return this.getEntities(world, Entity.class);
	}
}
