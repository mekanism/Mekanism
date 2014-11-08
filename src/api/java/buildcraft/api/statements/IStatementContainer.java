/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.statements;

import net.minecraft.tileentity.TileEntity;

/**
 * This is implemented by objects containing Statements, such as
 * Gates and TileEntities.
 */
public interface IStatementContainer {
	TileEntity getTile();
}
