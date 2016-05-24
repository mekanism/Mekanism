package mekanism.common.multipart;

import java.util.List;

import mcmultipart.client.multipart.ISmartMultipartModel;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.ISmartBlockModel;

/**
 * Created by ben on 21/05/16.
 */
public class BlockToMultipartModel implements ISmartMultipartModel
{
    ISmartBlockModel parent;

    public BlockToMultipartModel(ISmartBlockModel model)
    {
        parent = model;
    }

    @Override
    public IBakedModel handlePartState(IBlockState state)
    {
        return parent.handleBlockState(state);
    }

    @Override
    public List<BakedQuad> getFaceQuads(EnumFacing facing)
    {
        return parent.getFaceQuads(facing);
    }

    @Override
    public List<BakedQuad> getGeneralQuads()
    {
        return parent.getGeneralQuads();
    }

    @Override
    public boolean isAmbientOcclusion()
    {
        return parent.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d()
    {
        return parent.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer()
    {
        return parent.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture()
    {
        return parent.getParticleTexture();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms()
    {
        return parent.getItemCameraTransforms();
    }
}
