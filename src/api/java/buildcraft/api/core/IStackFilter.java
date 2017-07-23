/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.api.core;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

/** This interface provides a convenient means of dealing with entire classes of items without having to specify each
 * item individually. */
public interface IStackFilter {

    /** Check to see if a given stack matches this filter.
     * 
     * @param stack The stack to test. stack.isEmpty will always return false.
     * @return True if it does match, false otherwise. */
    boolean matches(@Nonnull ItemStack stack);
}
