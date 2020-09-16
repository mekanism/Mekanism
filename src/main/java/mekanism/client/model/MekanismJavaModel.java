package mekanism.client.model;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import java.util.function.Function;
import javax.annotation.Nonnull;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

public abstract class MekanismJavaModel extends Model {

    public MekanismJavaModel(Function<ResourceLocation, RenderType> renderType) {
        super(renderType);
    }

    protected IVertexBuilder getVertexBuilder(@Nonnull IRenderTypeBuffer renderer, @Nonnull RenderType renderType, boolean hasEffect) {
        return ItemRenderer.getEntityGlintVertexBuilder(renderer, renderType, false, hasEffect);
    }

    protected void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}