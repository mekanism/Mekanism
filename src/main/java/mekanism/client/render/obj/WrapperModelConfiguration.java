package mekanism.client.render.obj;

import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WrapperModelConfiguration implements IGeometryBakingContext {

    protected final IGeometryBakingContext internal;

    protected WrapperModelConfiguration(IGeometryBakingContext internal) {
        this.internal = internal;
    }

    @NotNull
    @Override
    public String getModelName() {
        return internal.getModelName();
    }

    @Override
    public boolean hasMaterial(@NotNull String name) {
        return internal.hasMaterial(name);
    }

    @NotNull
    @Override
    public Material getMaterial(@NotNull String name) {
        return internal.getMaterial(name);
    }

    @Override
    public boolean isGui3d() {
        return internal.isGui3d();
    }

    @Override
    public boolean useBlockLight() {
        return false;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return internal.useAmbientOcclusion();
    }

    @NotNull
    @Override
    public ItemTransforms getTransforms() {
        return internal.getTransforms();
    }

    @Override
    public Transformation getRootTransform() {
        return internal.getRootTransform();
    }

    @Override
    public boolean isComponentVisible(String component, boolean fallback) {
        return internal.isComponentVisible(component, fallback);
    }

    @Nullable
    @Override
    public ResourceLocation getRenderTypeHint() {
        return internal.getRenderTypeHint();
    }

    @Override
    public RenderTypeGroup getRenderType(ResourceLocation name) {
        return internal.getRenderType(name);
    }
}