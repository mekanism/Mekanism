/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.api.power;

import net.minecraftforge.common.ForgeDirection;

public interface IPowerReceptor {

	public void setPowerProvider(IPowerProvider provider);

	public IPowerProvider getPowerProvider();

	public void doWork();

	public int powerRequest(ForgeDirection from);

}
