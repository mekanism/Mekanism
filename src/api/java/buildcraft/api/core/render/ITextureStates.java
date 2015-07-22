/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.core.render;

import net.minecraft.block.Block;
import net.minecraft.util.IIcon;

public interface ITextureStates extends ICullable {

	ITextureStateManager getTextureState();
	
	IIcon getIcon(int side, int meta);
	
	Block getBlock();
	
}
