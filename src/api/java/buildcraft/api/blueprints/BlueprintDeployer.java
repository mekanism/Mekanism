/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.blueprints;

import java.io.File;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

/** This class is provided as a utility class for third-party mods that would like to easily deploy structures that are
 * written in blueprints. It does not offer control on material that needs to get in, or how the structure is deployed,
 * but allows to create contents of a blueprint in one cycle. Note that these functionalities will only work if
 * BuildCraft is installed. */
public abstract class BlueprintDeployer {

    /** The deployed instantiated by BuildCraft. This is set by the BuildCraft builder mod. Mods that want to work with
     * BuildCraft not installed should check for this value to be not null. */
    public static BlueprintDeployer instance;

    /** Deploy the contents of the blueprints as if the builder was located at {pos} facing the direction dir. */
    public abstract void deployBlueprint(World world, BlockPos pos, EnumFacing facing, File file);

    /** Deploy the contents of the byte array as if the builder was located at {pos} facing the direction dir. */

    public abstract void deployBlueprintFromFileStream(World world, BlockPos pos, EnumFacing dir, byte[] data);

}
