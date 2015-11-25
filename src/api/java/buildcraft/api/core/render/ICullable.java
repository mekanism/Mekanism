/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.core.render;

import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

/*
 * This interface designates a block as a state machine responsible for culling
 * For implementation details look into FakeBlock implementation
 */
public interface ICullable {

	//Side Rendering States
	//They are used to effectively cull obstructed sides while processing facades.
	//Make sure your implementation is correct otherwise expect FPS drop
	
	void setRenderSide(ForgeDirection side, boolean render);
	
	void setRenderAllSides();
	
	boolean shouldSideBeRendered(IBlockAccess blockAccess, int x, int y, int z, int side);
	
	void setRenderMask(int mask);	
	
}
