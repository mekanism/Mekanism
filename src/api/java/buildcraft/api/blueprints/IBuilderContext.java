/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.blueprints;

import net.minecraft.world.World;
import buildcraft.api.core.IBox;
import buildcraft.api.core.Position;

/**
 * This interface provide contextual information when building or initializing
 * blueprint slots.
 */
public interface IBuilderContext {

	Position rotatePositionLeft(Position pos);

	IBox surroundingBox();

	World world();

	MappingRegistry getMappingRegistry();
}
