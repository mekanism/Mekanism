/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.world.World;

public final class BuildCraftAPI {

	public static ICoreProxy proxy;

	public static final Set<Block> softBlocks = new HashSet<Block>();
	public static final HashMap<String, IWorldProperty> worldProperties = new HashMap<String, IWorldProperty>();

	/**
	 * Deactivate constructor
	 */
	private BuildCraftAPI() {
	}

	public static IWorldProperty getWorldProperty(String name) {
		return worldProperties.get(name);
	}

	public static void registerWorldProperty(String name, IWorldProperty property) {
		if (worldProperties.containsKey(name)) {
			BCLog.logger.warn("The WorldProperty key '" + name + "' is being overidden with " + property.getClass().getSimpleName() + "!");
		}
		worldProperties.put(name, property);
	}

	public static boolean isSoftBlock(World world, int x, int y, int z) {
		return worldProperties.get("soft").get(world, x, y, z);
	}
}
