/**
 * 
 */
package mekanism.induction.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mekanism.api.Object3D;
import mekanism.induction.common.tileentity.TileEntityEMContractor;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

/**
 * Uses the well known A* Pathfinding algorithm.
 * 
 * @author Calclavia
 * 
 */
public class PathfinderEMContractor
{
	public final Set<Object3D> openSet, closedSet;

	public final HashMap<Object3D, Object3D> navMap;

	public final HashMap<Object3D, Double> gScore, fScore;

	public final Object3D target;

	public List<Object3D> results;

	private World world;

	public PathfinderEMContractor(World w, Object3D t)
	{
		world = w;
		target = t;

		openSet = new HashSet<Object3D>();
		closedSet = new HashSet<Object3D>();
		navMap = new HashMap<Object3D, Object3D>();
		gScore = new HashMap<Object3D, Double>();
		fScore = new HashMap<Object3D, Double>();
		results = new ArrayList<Object3D>();
	}

	public boolean find(final Object3D start)
	{
		openSet.add(start);
		gScore.put(start, 0d);
		fScore.put(start, gScore.get(start) + getEstimate(start, target));

		int blockCount = 0;

		for (int i = 0; i < 6; i++)
		{
			ForgeDirection direction = ForgeDirection.getOrientation(i);
			Object3D neighbor = target.translate(direction.offsetX, direction.offsetY, direction.offsetZ);

			if(!TileEntityEMContractor.canBePath(world, neighbor))
			{
				blockCount++;
			}
		}

		if(blockCount >= 6)
		{
			return false;
		}

		double maxSearchDistance = start.distanceTo(target) * 2;

		while(!openSet.isEmpty())
		{
			Object3D currentNode = null;
			double lowestFScore = 0;

			for(Object3D node : openSet)
			{
				if(currentNode == null || fScore.get(node) < lowestFScore)
				{
					currentNode = node;
					lowestFScore = fScore.get(node);
				}
			}

			if(currentNode == null && start.distanceTo(currentNode) > maxSearchDistance)
			{
				break;
			}

			if(currentNode.equals(target))
			{
				results = reconstructPath(navMap, target);
				return true;
			}

			openSet.remove(currentNode);
			closedSet.add(currentNode);

			for(int i = 0; i < 6; i++)
			{
				ForgeDirection direction = ForgeDirection.getOrientation(i);
				Object3D neighbor = currentNode.getFromSide(direction);

				if(TileEntityEMContractor.canBePath(world, neighbor))
				{
					double tentativeG = gScore.get(currentNode) + currentNode.distanceTo(neighbor);

					if(closedSet.contains(neighbor))
					{
						if(tentativeG >= gScore.get(neighbor))
						{
							continue;
						}
					}

					if(!openSet.contains(neighbor) || tentativeG < gScore.get(neighbor))
					{
						navMap.put(neighbor, currentNode);
						gScore.put(neighbor, tentativeG);
						fScore.put(neighbor, gScore.get(neighbor) + getEstimate(neighbor, target));
						openSet.add(neighbor);
					}
				}
			}
		}

		return false;
	}

	private List<Object3D> reconstructPath(HashMap<Object3D, Object3D> naviMap, Object3D currentNode)
	{
		List<Object3D> path = new ArrayList<Object3D>();
		path.add(currentNode);

		if(naviMap.containsKey(currentNode))
		{
			path.addAll(reconstructPath(naviMap, naviMap.get(currentNode)));
		}

		return path;
	}

	private double getEstimate(Object3D start, Object3D target2)
	{
		return start.distanceTo(target2);
	}
}
