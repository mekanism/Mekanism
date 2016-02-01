package mekanism.client.model.item;

import java.util.List;

import mekanism.common.Tier.EnergyCubeTier;
import mekanism.common.base.IEnergyCube;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.client.model.ISmartItemModel;


public class EnergyCubeItemModel implements ISmartItemModel {

    public IBakedModel[] models;

    public EnergyCubeItemModel(IBakedModel[] models)
    {
        this.models = models;
    }

    @Override
    public IBakedModel handleItemState(ItemStack stack) {
        EnergyCubeTier tier = ((IEnergyCube)stack.getItem()).getEnergyCubeTier(stack);
        if(tier.ordinal() < models.length) return models[tier.ordinal()];
        return models[0];
    }

    @Override
    public List<BakedQuad> getFaceQuads(EnumFacing side)
    {
        return models[0].getFaceQuads(side);
    }

    @Override
    public List<BakedQuad> getGeneralQuads()
    {
        return models[0].getGeneralQuads();
    }

    @Override
    public boolean isAmbientOcclusion()
    {
        return models[0].isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d()
    {
        return models[0].isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer()
    {
        return models[0].isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture()
    {
        return models[0].getParticleTexture();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms()
    {
        return models[0].getItemCameraTransforms();
    }
}