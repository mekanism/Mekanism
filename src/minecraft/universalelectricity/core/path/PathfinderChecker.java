package universalelectricity.core.path;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.block.IConductor;
import universalelectricity.core.block.IConnectionProvider;
import universalelectricity.core.vector.Vector3;

/**
 * Check if a conductor connects with another.
 * 
 * @author Calclavia
 * 
 */
public class PathfinderChecker extends Pathfinder
{
	public PathfinderChecker(final World world, final IConnectionProvider targetConnector, final IConnectionProvider... ignoreConnector)
	{
		super(new IPathCallBack()
		{
			@Override
			public Set<Vector3> getConnectedNodes(Pathfinder finder, Vector3 currentNode)
			{
				Set<Vector3> neighbors = new HashSet<Vector3>();

				for (int i = 0; i < 6; i++)
				{
					ForgeDirection direction = ForgeDirection.getOrientation(i);
					Vector3 position = currentNode.clone().modifyPositionFromSide(direction);
					TileEntity connectedBlock = position.getTileEntity(world);

					if (connectedBlock instanceof IConductor && !Arrays.asList(ignoreConnector).contains(connectedBlock))
					{
						if (((IConductor) connectedBlock).canConnect(direction.getOpposite()))
						{
							neighbors.add(position);
						}
					}
				}

				return neighbors;
			}

			@Override
			public boolean onSearch(Pathfinder finder, Vector3 node)
			{
				if (node.getTileEntity(world) == targetConnector)
				{
					finder.results.add(node);
					return true;
				}

				return false;
			}
		});
	}
}
