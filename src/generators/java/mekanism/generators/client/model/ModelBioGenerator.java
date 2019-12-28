package mekanism.generators.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderType;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

public class ModelBioGenerator extends Model {

    private static final ResourceLocation GENERATOR_TEXTURE = MekanismGenerators.rl("render/bio_generator.png");
    private final RenderType RENDER_TYPE = func_228282_a_(GENERATOR_TEXTURE);
    private final RenderType GLASS_RENDER_TYPE = MekanismRenderType.mekStandard(GENERATOR_TEXTURE);

    private final ModelRenderer base;
    private final ModelRenderer sideRight;
    private final ModelRenderer back;
    private final ModelRenderer bar;
    private final ModelRenderer glass;
    private final ModelRenderer sideLeft;

    public ModelBioGenerator() {
        super(RenderType::func_228634_a_);
        textureWidth = 64;
        textureHeight = 64;

        base = new ModelRenderer(this, 0, 0);
        base.func_228304_a_(0F, 0F, 0F, 16, 7, 16, false);
        base.setRotationPoint(-8F, 17F, -8F);
        base.setTextureSize(64, 64);
        base.mirror = true;
        setRotation(base, 0F, 0F, 0F);
        sideRight = new ModelRenderer(this, 0, 40);
        sideRight.func_228304_a_(0F, 0F, 0F, 3, 9, 8, false);
        sideRight.setRotationPoint(5F, 8F, -8F);
        sideRight.setTextureSize(64, 64);
        setRotation(sideRight, 0F, 0F, 0F);
        back = new ModelRenderer(this, 0, 23);
        back.func_228304_a_(0F, 0F, 0F, 16, 9, 8, false);
        back.setRotationPoint(-8F, 8F, 0F);
        back.setTextureSize(64, 64);
        back.mirror = true;
        setRotation(back, 0F, 0F, 0F);
        bar = new ModelRenderer(this, 0, 57);
        bar.func_228304_a_(0F, 0F, 0F, 10, 1, 1, false);
        bar.setRotationPoint(-5F, 8.5F, -7.5F);
        bar.setTextureSize(64, 64);
        bar.mirror = true;
        setRotation(bar, 0F, 0F, 0F);
        glass = new ModelRenderer(this, 22, 40);
        glass.func_228304_a_(0F, 0F, 0F, 12, 8, 7, false);
        glass.setRotationPoint(-6F, 9F, -7F);
        glass.setTextureSize(64, 64);
        glass.mirror = true;
        setRotation(glass, 0F, 0F, 0F);
        sideLeft = new ModelRenderer(this, 0, 40);
        sideLeft.func_228304_a_(0F, 0F, 0F, 3, 9, 8, false);
        sideLeft.setRotationPoint(-8F, 8F, -8F);
        sideLeft.setTextureSize(64, 64);
        sideLeft.mirror = true;
        setRotation(sideLeft, 0F, 0F, 0F);
    }

    public void render(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int otherLight) {
        func_225598_a_(matrix, renderer.getBuffer(RENDER_TYPE), light, otherLight, 1, 1, 1, 1);
        //Render the glass on a more translucent layer
        //Note: The glass makes water, ice etc behind it invisible. This is due to an engine limitation
        glass.func_228309_a_(matrix, renderer.getBuffer(GLASS_RENDER_TYPE), light, otherLight, 1, 1, 1, 1);
    }

    @Override
    public void func_225598_a_(@Nonnull MatrixStack matrix, @Nonnull IVertexBuilder vertexBuilder, int light, int otherLight, float red, float green, float blue, float alpha) {
        base.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        sideRight.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        sideLeft.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        back.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        bar.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}