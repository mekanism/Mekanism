/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.blueprints;

import java.io.File;

import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * This class is provided as a utility class for third-party mods that would
 * like to easily deploy structures that are written in blueprints. It does
 * not offer control on material that needs to get in, or how the structure
 * is deployed, but allows to create contents of a blueprint in one cycle.
 * Note that these functionalities will only work if BuildCraft is installed.
 */
public abstract class BlueprintDeployer {

	/**
	 * The deployed instantiated by BuildCraft. This is set by the BuildCraft
	 * builder mod. Mods that want to work with BuildCraft not installed should
	 * check for this value to be not null.
	 */
	public static BlueprintDeployer instance;

	/**
	 * Deploy the contents of the blueprints as if the builder was located at
	 * {x, y, z} facing the direction dir.
	 */
	public abstract void deployBlueprint(World world, int x, int y, int z,
			ForgeDirection dir, File file);
			
	/**
	*Deploy the contents of the byte array as if the builder was located at
	*{x, y, z} facing the direction dir.
	*/
	
	public abstract void deployBlueprintFromFileStream(World world, int x, int y,
	int z, ForgeDirection dir, byte [] data);

}
