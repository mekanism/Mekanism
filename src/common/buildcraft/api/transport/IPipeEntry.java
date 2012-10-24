/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.api.transport;

import net.minecraft.src.ItemStack;
import buildcraft.api.core.Orientations;

/**
 * Interface used to put objects into pipes, implemented by pipe tile entities.
 */
public interface IPipeEntry {

	void entityEntering(ItemStack payload, Orientations orientation);
	void entityEntering(IPipedItem item, Orientations orientation);

	boolean acceptItems();

}
