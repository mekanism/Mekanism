/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.core;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IIconProvider {

	/**
	 * @param iconIndex
	 */
	@SideOnly(Side.CLIENT)
	TextureAtlasSprite getIcon(int iconIndex);

	/**
	 * A call for the provider to register its Icons. This may be called multiple times but should only be executed once per provider
	 * @param iconRegister
	 */
	@SideOnly(Side.CLIENT)
	void registerIcons(TextureMap iconRegister);

}
