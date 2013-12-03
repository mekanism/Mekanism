/**
 * 
 */
package mekanism.induction.common;

import universalelectricity.core.vector.Vector3;

/**
 * @author Calclavia
 * 
 */
public class ThreadEMPathfinding extends Thread
{
	private boolean isCompleted = false;
	private PathfinderEMContractor pathfinder;
	private Vector3 start;

	public ThreadEMPathfinding(PathfinderEMContractor pathfinder, Vector3 start)
	{
		this.pathfinder = pathfinder;
		this.start = start;
		this.setPriority(Thread.MIN_PRIORITY);
	}

	@Override
	public void run()
	{
		this.pathfinder.find(this.start);
		this.isCompleted = true;
	}

	public PathfinderEMContractor getPath()
	{
		if (this.isCompleted)
		{
			return this.pathfinder;
		}

		return null;
	}
}
