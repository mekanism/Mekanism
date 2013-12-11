/**
 * 
 */
package mekanism.induction.common;

import mekanism.api.Object3D;

/**
 * @author Calclavia
 * 
 */
public class ThreadEMPathfinding extends Thread
{
	private boolean isCompleted = false;
	private PathfinderEMContractor pathfinder;
	private Object3D start;

	public ThreadEMPathfinding(PathfinderEMContractor p, Object3D s)
	{
		pathfinder = p;
		start = s;
		setPriority(Thread.MIN_PRIORITY);
	}

	@Override
	public void run()
	{
		pathfinder.find(start);
		isCompleted = true;
	}

	public PathfinderEMContractor getPath()
	{
		if(isCompleted)
		{
			return pathfinder;
		}

		return null;
	}
}
