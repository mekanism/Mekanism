package universalelectricity.core.grid;

import java.util.ArrayList;
import java.util.Set;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

/**
 * Implement this in your network class/interface if you plan to have your own network defined by
 * specific conductors and acceptors.
 * 
 * @author aidancbrady
 * 
 * @param <N> - the class/interface Type value in which you implement this
 * @param <C> - the class/interface Type which makes up the network's conductor Set
 * @param <A> - the class/interface Type which makes up the network's acceptor Set
 */
public interface IGridNetwork<N, C, A>
{
	/**
	 * Refreshes and cleans up conductor references of this network, as well as updating the
	 * acceptor set.
	 */
	public void refresh();

	/**
	 * Gets the Set of conductors that make up this network.
	 * 
	 * @return conductor set
	 */
	public Set<C> getConductors();

	/**
	 * Gets the Set of AVAILABLE acceptors in this network. Make sure this doesn't include any stray
	 * acceptors which cannot accept resources.
	 * 
	 * @return available acceptor set
	 */
	public Set<A> getAcceptors();

	/**
	 * Gets the list of possible connection directions for the provided TileEntity. Tile must be in
	 * this network.
	 * 
	 * @param tile The tile to get connections for
	 * @return The list of directions that can be connected to for the provided tile
	 */
	public ArrayList<ForgeDirection> getPossibleDirections(TileEntity tile);

	/**
	 * Creates a new network that makes up the current network and the network defined in the
	 * parameters. Be sure to refresh the new network inside this method.
	 * 
	 * @param network - network to merge
	 */
	public void merge(N network);

	/**
	 * Splits a network by the conductor referenced in the parameters. It will then create and
	 * refresh the new independent networks possibly created by this operation.
	 * 
	 * @param connection
	 */
	public void split(C connection);
}
