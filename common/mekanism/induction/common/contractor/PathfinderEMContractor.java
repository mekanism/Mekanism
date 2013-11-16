/**
 * 
 */
package mekanism.induction.common.contractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.vector.Vector3;

/**
 * Uses the well known A* Pathfinding algorithm.
 * 
 * @author Calclavia
 * 
 */
public class PathfinderEMContractor
{
	public final Set<Vector3> openSet, closedSet;

	public final HashMap<Vector3, Vector3> navMap;

	public final HashMap<Vector3, Double> gScore, fScore;

	public final Vector3 target;

	public List<Vector3> results;

	private World world;

	public PathfinderEMContractor(World world, Vector3 target)
	{
		this.world = world;
		this.target = target;

		/**
		 * Instantiate Variables
		 */
		this.openSet = new HashSet<Vector3>();
		this.closedSet = new HashSet<Vector3>();
		this.navMap = new HashMap<Vector3, Vector3>();
		this.gScore = new HashMap<Vector3, Double>();
		this.fScore = new HashMap<Vector3, Double>();
		this.results = new ArrayList<Vector3>();
	}

	public boolean find(final Vector3 start)
	{
		this.openSet.add(start);
		this.gScore.put(start, 0d);
		this.fScore.put(start, this.gScore.get(start) + getEstimate(start, this.target));

		int blockCount = 0;

		for (int i = 0; i < 6; i++)
		{
			ForgeDirection direction = ForgeDirection.getOrientation(i);
			Vector3 neighbor = this.target.clone().translate(new Vector3(direction.offsetX, direction.offsetY, direction.offsetZ));

			if (!TileEntityEMContractor.canBePath(this.world, neighbor))
			{
				blockCount++;
			}
		}

		if (blockCount >= 6)
		{
			return false;
		}

		double maxSearchDistance = start.distance(this.target) * 2;

		while (!this.openSet.isEmpty())
		{
			Vector3 currentNode = null;
			double lowestFScore = 0;

			for (Vector3 node : this.openSet)
			{
				if (currentNode == null || this.fScore.get(node) < lowestFScore)
				{
					currentNode = node;
					lowestFScore = this.fScore.get(node);
				}
			}

			if (currentNode == null && start.distance(currentNode) > maxSearchDistance)
			{
				break;
			}

			if (currentNode.equals(this.target))
			{
				this.results = this.reconstructPath(this.navMap, this.target);
				return true;
			}

			this.openSet.remove(currentNode);
			this.closedSet.add(currentNode);

			for (int i = 0; i < 6; i++)
			{
				ForgeDirection direction = ForgeDirection.getOrientation(i);
				Vector3 neighbor = currentNode.clone().modifyPositionFromSide(direction);

				if (TileEntityEMContractor.canBePath(this.world, neighbor))
				{
					double tentativeG = this.gScore.get(currentNode) + currentNode.distance(neighbor);

					if (this.closedSet.contains(neighbor))
					{
						if (tentativeG >= this.gScore.get(neighbor))
						{
							continue;
						}
					}

					if (!this.openSet.contains(neighbor) || tentativeG < this.gScore.get(neighbor))
					{
						this.navMap.put(neighbor, currentNode);
						this.gScore.put(neighbor, tentativeG);
						this.fScore.put(neighbor, this.gScore.get(neighbor) + this.getEstimate(neighbor, this.target));
						this.openSet.add(neighbor);
					}
				}
			}
		}

		return false;
	}

	private List<Vector3> reconstructPath(HashMap<Vector3, Vector3> naviMap, Vector3 currentNode)
	{
		List<Vector3> path = new ArrayList<Vector3>();
		path.add(currentNode);

		if (naviMap.containsKey(currentNode))
		{
			path.addAll(this.reconstructPath(naviMap, naviMap.get(currentNode)));
		}

		return path;
	}

	private double getEstimate(Vector3 start, Vector3 target2)
	{
		return start.distance(target2);
	}
}
