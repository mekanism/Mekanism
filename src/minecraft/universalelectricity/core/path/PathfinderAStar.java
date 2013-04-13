package universalelectricity.core.path;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.vector.Vector3;

/**
 * An advanced version of pathfinding to find the shortest path between two points. Uses the A*
 * Pathfinding algorithm.
 * 
 * @author Calclavia
 * 
 */
public class PathfinderAStar extends Pathfinder
{
	/**
	 * A pathfinding call back interface used to call back on paths.
	 */
	public IPathCallBack callBackCheck;

	/**
	 * The set of tentative nodes to be evaluated, initially containing the start node
	 */
	public Set<Vector3> openSet;

	/**
	 * The map of navigated nodes storing the data of which position came from which in the format
	 * of: X came from Y.
	 */
	public HashMap<Vector3, Vector3> navigationMap;

	/**
	 * Score values, used to determine the score for a path to evaluate how optimal the path is.
	 * G-Score is the cost along the best known path while F-Score is the total cost.
	 */
	public HashMap<Vector3, Double> gScore, fScore;

	/**
	 * The node in which the pathfinder is trying to reach.
	 */
	public Vector3 goal;

	public PathfinderAStar(IPathCallBack callBack, Vector3 goal)
	{
		super(callBack);
		this.goal = goal;
	}

	@Override
	public boolean findNodes(Vector3 start)
	{
		this.openSet.add(start);
		this.gScore.put(start, 0d);
		this.fScore.put(start, this.gScore.get(start) + getHeuristicEstimatedCost(start, this.goal));

		while (!this.openSet.isEmpty())
		{
			// Current is the node in openset having the lowest f_score[] value
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

			if (currentNode == null)
			{
				break;
			}

			if (this.callBackCheck.onSearch(this, currentNode))
			{
				return false;
			}

			if (currentNode.equals(this.goal))
			{
				this.results = reconstructPath(this.navigationMap, goal);
				return true;
			}

			this.openSet.remove(currentNode);
			this.closedSet.add(currentNode);

			for (Vector3 neighbor : getNeighborNodes(currentNode))
			{
				double tentativeGScore = this.gScore.get(currentNode) + currentNode.distanceTo(neighbor);

				if (this.closedSet.contains(neighbor))
				{
					if (tentativeGScore >= this.gScore.get(neighbor))
					{
						continue;
					}
				}

				if (!this.openSet.contains(neighbor) || tentativeGScore < this.gScore.get(neighbor))
				{
					this.navigationMap.put(neighbor, currentNode);
					this.gScore.put(neighbor, tentativeGScore);
					this.fScore.put(neighbor, gScore.get(neighbor) + getHeuristicEstimatedCost(neighbor, goal));
					this.openSet.add(neighbor);
				}
			}
		}

		return false;
	}

	@Override
	public Pathfinder reset()
	{
		this.openSet = new HashSet<Vector3>();
		this.navigationMap = new HashMap<Vector3, Vector3>();
		return super.reset();
	}

	/**
	 * A recursive function to back track and find the path in which we have analyzed.
	 */
	public Set<Vector3> reconstructPath(HashMap<Vector3, Vector3> nagivationMap, Vector3 current_node)
	{
		Set<Vector3> path = new HashSet<Vector3>();
		path.add(current_node);

		if (nagivationMap.containsKey(current_node))
		{
			path.addAll(reconstructPath(nagivationMap, nagivationMap.get(current_node)));
			return path;
		}
		else
		{
			return path;
		}
	}

	/**
	 * @return An estimated cost between two points.
	 */
	public double getHeuristicEstimatedCost(Vector3 start, Vector3 goal)
	{
		return start.distanceTo(goal);
	}

	/**
	 * @return A Set of neighboring Vector3 positions.
	 */
	public Set<Vector3> getNeighborNodes(Vector3 vector)
	{
		if (this.callBackCheck != null)
		{
			return this.callBackCheck.getConnectedNodes(this, vector);
		}
		else
		{
			Set<Vector3> neighbors = new HashSet<Vector3>();

			for (int i = 0; i < 6; i++)
			{
				neighbors.add(vector.clone().modifyPositionFromSide(ForgeDirection.getOrientation(i)));
			}

			return neighbors;
		}
	}
}
