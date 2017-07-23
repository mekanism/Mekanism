package ic2.api.energy.tile;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;

/**
 * Interface, that allows Blocks to be colored and have their connectivity based on that color.
 * This allows for any Energy Tile to be colored. (Conductors, Sinks, Sources)
 * @author Aroma1997
 */
public interface IColoredEnergyTile extends IEnergyTile {

	/**
	 * This is to get a Energy Tile's color for the given side. Mainly used for checking connectivity.
	 * @param side The side you want to get the color from.
	 * @return The color of the Energy Tile at the given side or null, if it's uncolored.
	 */
	EnumDyeColor getColor(EnumFacing side);


}
