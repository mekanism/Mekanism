package mekanism.client.render.obj;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.client.renderer.model.ModelRotation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.ModelLoaderRegistry;

public class ContentsModelConfiguration implements IModelConfiguration {

    @Nullable
    @Override
    public IUnbakedModel getOwnerModel() {
        return null;
    }

    @Nonnull
    @Override
    public String getModelName() {
        return "transmitter_contents";
    }

    @Override
    public boolean isTexturePresent(@Nonnull String name) {
        return false;
    }

    @Nonnull
    @Override
    public Material resolveTexture(@Nonnull String name) {
        return ModelLoaderRegistry.blockMaterial(name);
    }

    @Override
    public boolean isShadedInGui() {
        return false;
    }

    @Override
    public boolean useSmoothLighting() {
        return false;
    }

    @Nonnull
    @Override
    public ItemCameraTransforms getCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }

    @Nonnull
    @Override
    public IModelTransform getCombinedTransform() {
        return ModelRotation.X0_Y0;
    }
}