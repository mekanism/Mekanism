/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.blueprints;

import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import buildcraft.api.core.IBox;

/** This interface provide contextual information when building or initializing blueprint slots. */
public interface IBuilderContext {

    Vec3 rotatePositionLeft(Vec3 pos);

    IBox surroundingBox();

    World world();

    MappingRegistry getMappingRegistry();
}
