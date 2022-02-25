package mekanism.client.render.obj;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.ModelLoaderRegistry;

public class ContentsModelConfiguration implements IModelConfiguration {

    @Nullable
    @Override
    public UnbakedModel getOwnerModel() {
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
    public boolean isSideLit() {
        return false;
    }

    @Override
    public boolean useSmoothLighting() {
        return false;
    }

    @Nonnull
    @Override
    @Deprecated
    public ItemTransforms getCameraTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

    @Nonnull
    @Override
    public ModelState getCombinedTransform() {
        return BlockModelRotation.X0_Y0;
    }
}