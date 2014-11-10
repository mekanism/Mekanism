/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.blueprints;

import buildcraft.api.core.Position;

public class Translation {

	public double x = 0;
	public double y = 0;
	public double z = 0;

	public Position translate (Position p) {
		Position p2 = new Position (p);

		p2.x = p.x + x;
		p2.y = p.y + y;
		p2.z = p.z + z;

		return p2;
	}

	@Override
	public String toString () {
		return "{" + x + ", " + y + ", " + z + "}";
	}

}
