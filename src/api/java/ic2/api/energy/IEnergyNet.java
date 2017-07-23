package ic2.api.energy;

import java.io.PrintStream;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import ic2.api.energy.tile.IEnergyTile;
import ic2.api.info.ILocatable;

/**
 * Interface representing the methods provided by the global EnergyNet class.
 *
 * See ic2/api/energy/usage.txt for an overall description of the energy net api.
 */
public interface IEnergyNet {
	/**
	 * Get the EnergyNet-registered tile for the specified position.
	 *
	 * The returned tile is always the main tile implementing IEnergySource/-Sink/-Conductor, which
	 * is also what was registered to the energy net.
	 *
	 * For multi-blocks (IMetaDelegate) the connectivity handling is being done by sub tiles that
	 * may or may not include the main tile. Sub tiles have to be queried separately through the
	 * getSubTile method.
	 *
	 * As a consequence of always returning the main tile, the supplied position may not match the
	 * returned tile's position.
	 *
	 * Connectivity checking -> IEnergyEmitter/IEnergyAcceptor -> getSubTile
	 * Logic like energy I/O -> IEnergySource/IEnergySink/IEnergyConductor -> getTile
	 *
	 * @param world World containing the tile
	 * @param pos position
	 * @return main tile for the specified position or null if none is registered
	 */
	IEnergyTile getTile(World world, BlockPos pos);

	/**
	 * Get the sub tile at the specified position.
	 *
	 * See the description of {@link #getTile} about whether getTile or getSubTile is applicable.
	 *
	 * @param world World containing the tile
	 * @param pos position
	 * @return sub tile at the specified position or null if none is registered
	 */
	IEnergyTile getSubTile(World world, BlockPos pos);

	<T extends TileEntity & IEnergyTile> void addTile(T tile);
	<T extends ILocatable & IEnergyTile> void addTile(T tile);

	void removeTile(IEnergyTile tile);

	World getWorld(IEnergyTile tile);

	BlockPos getPos(IEnergyTile tile);

	/**
	 * Retrieve statistics for the tile entity specified.
	 *
	 * The statistics apply to the last simulated tick.
	 *
	 * @param tile Tile entity to check.
	 * @return Statistics for the tile entity.
	 */
	NodeStats getNodeStats(IEnergyTile tile);

	boolean dumpDebugInfo(World world, BlockPos pos, PrintStream console, PrintStream chat);

	/**
	 * Determine the typical power used by the specific tier, e.g. 128 eu/t for tier 2.
	 *
	 * @param tier tier
	 * @return power in eu/t
	 */
	double getPowerFromTier(int tier);

	/**
	 * Determine minimum tier required to handle the specified power, e.g. tier 2 for 128 eu/t.
	 *
	 * @param power in eu/t
	 * @return tier
	 */
	int getTierFromPower(double power);

	void registerEventReceiver(IEnergyNetEventReceiver receiver);
	void unregisterEventReceiver(IEnergyNetEventReceiver receiver);
}
