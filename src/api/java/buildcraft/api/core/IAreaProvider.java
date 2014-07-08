/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.core;

/**
 * To be implemented by TileEntities able to provide a square area on the world, typically BuildCraft markers.
 */
public interface IAreaProvider {

	int xMin();

	int yMin();

	int zMin();

	int xMax();

	int yMax();

	int zMax();

	/**
	 * Remove from the world all objects used to define the area.
	 */
	void removeFromWorld();

}
