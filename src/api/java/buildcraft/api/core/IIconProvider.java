/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.core;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface IIconProvider {

	/**
	 * @param iconIndex
	 */
	@SideOnly(Side.CLIENT)
	IIcon getIcon(int iconIndex);

	/**
	 * A call for the provider to register its Icons. This may be called multiple times but should only be executed once per provider
	 * @param iconRegister
	 */
	@SideOnly(Side.CLIENT)
	void registerIcons(IIconRegister iconRegister);

}
