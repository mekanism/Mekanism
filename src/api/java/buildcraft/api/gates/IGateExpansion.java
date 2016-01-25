/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.gates;

import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IGateExpansion {

    String getUniqueIdentifier();

    String getDisplayName();

    GateExpansionController makeController(TileEntity pipeTile);

    @SideOnly(Side.CLIENT)
    void textureStitch(TextureMap map);

    @SideOnly(Side.CLIENT)
    IGateStaticRenderState getRenderState();

    public interface IGateStaticRenderState {
        List<BakedQuad> bake(VertexFormat format);
    }
}
