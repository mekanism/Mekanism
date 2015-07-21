/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.gates;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;

public interface IGateExpansion {

	String getUniqueIdentifier();

	String getDisplayName();

	GateExpansionController makeController(TileEntity pipeTile);

	void registerBlockOverlay(IIconRegister iconRegister);

	void registerItemOverlay(IIconRegister iconRegister);

	IIcon getOverlayBlock();

	IIcon getOverlayItem();
}
