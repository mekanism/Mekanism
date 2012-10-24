/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.api.core;

import net.minecraftforge.common.ForgeDirection;

public enum Orientations {
	YNeg, // 0
	YPos, // 1
	ZNeg, // 2
	ZPos, // 3
	XNeg, // 4
	XPos, // 5
	Unknown;

	public Orientations reverse() {
		switch (this) {
		case YPos:
			return Orientations.YNeg;
		case YNeg:
			return Orientations.YPos;
		case ZPos:
			return Orientations.ZNeg;
		case ZNeg:
			return Orientations.ZPos;
		case XPos:
			return Orientations.XNeg;
		case XNeg:
			return Orientations.XPos;
		default:
			return Orientations.Unknown;
		}
	}
	
	public ForgeDirection toDirection(){
		switch(this){
		case YNeg:
			return ForgeDirection.DOWN;
		case YPos:
			return ForgeDirection.UP;
		case ZNeg:
			return ForgeDirection.NORTH;
		case ZPos:
			return ForgeDirection.SOUTH;
		case XNeg:
			return ForgeDirection.WEST;
		case XPos:
			return ForgeDirection.EAST;
		default:
			return ForgeDirection.UNKNOWN;
			
		}
	}

	public Orientations rotateLeft() {
		switch (this) {
		case XPos:
			return ZPos;
		case ZNeg:
			return XPos;
		case XNeg:
			return ZNeg;
		case ZPos:
			return XNeg;
		default:
			return this;
		}
	}

	public static Orientations[] dirs() {
		return new Orientations[] { YNeg, YPos, ZNeg, ZPos, XNeg, XPos };
	}
}
