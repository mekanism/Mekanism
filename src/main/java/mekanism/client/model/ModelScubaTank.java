package mekanism.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

public class ModelScubaTank extends MekanismJavaModel {

    private static final ResourceLocation TANK_TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "scuba_set.png");
    private final RenderType RENDER_TYPE = getRenderType(TANK_TEXTURE);

    private final ModelRenderer tankL;
    private final ModelRenderer tankR;
    private final ModelRenderer tankDock;
    private final ModelRenderer capL;
    private final ModelRenderer capR;
    private final ModelRenderer tankBridge;
    private final ModelRenderer tankPipeLower;
    private final ModelRenderer tankPipeUpper;
    private final ModelRenderer tankBackBrace;

    public ModelScubaTank() {
        super(RenderType::getEntitySolid);
        textureWidth = 128;
        textureHeight = 64;

        tankL = new ModelRenderer(this, 23, 54);
        tankL.addBox(-1F, 2F, 4F, 3, 7, 3, false);
        tankL.setRotationPoint(0F, 0F, 0F);
        tankL.setTextureSize(128, 64);
        tankL.mirror = true;
        setRotation(tankL, -0.2443461F, 0.5235988F, 0F);
        tankR = new ModelRenderer(this, 23, 54);
        tankR.addBox(-2F, 2F, 4F, 3, 7, 3, false);
        tankR.setRotationPoint(0F, 0F, 0F);
        tankR.setTextureSize(128, 64);
        tankR.mirror = true;
        setRotation(tankR, -0.2443461F, -0.5235988F, 0F);
        tankR.mirror = false;
        tankDock = new ModelRenderer(this, 0, 55);
        tankDock.addBox(-2F, 5F, 1F, 4, 4, 5, false);
        tankDock.setRotationPoint(0F, 0F, 0F);
        tankDock.setTextureSize(128, 64);
        tankDock.mirror = true;
        setRotation(tankDock, 0F, 0F, 0F);
        capL = new ModelRenderer(this, 23, 51);
        capL.addBox(-0.5F, 1F, 4.5F, 2, 1, 2, false);
        capL.setRotationPoint(0F, 0F, 0F);
        capL.setTextureSize(128, 64);
        capL.mirror = true;
        setRotation(capL, -0.2443461F, 0.5235988F, 0F);
        capR = new ModelRenderer(this, 23, 51);
        capR.addBox(-1.5F, 1F, 4.5F, 2, 1, 2, false);
        capR.setRotationPoint(0F, 0F, 0F);
        capR.setTextureSize(128, 64);
        capR.mirror = true;
        setRotation(capR, -0.2443461F, -0.5235988F, 0F);
        tankBridge = new ModelRenderer(this, 0, 47);
        tankBridge.addBox(-1F, 3F, -1.5F, 2, 5, 3, false);
        tankBridge.setRotationPoint(0F, 0F, 0F);
        tankBridge.setTextureSize(128, 64);
        tankBridge.mirror = true;
        setRotation(tankBridge, 0.5934119F, 0F, 0F);
        tankPipeLower = new ModelRenderer(this, 0, 37);
        tankPipeLower.addBox(-0.5F, 2F, 3F, 1, 4, 1, false);
        tankPipeLower.setRotationPoint(0F, 0F, 0F);
        tankPipeLower.setTextureSize(128, 64);
        tankPipeLower.mirror = true;
        setRotation(tankPipeLower, 0.2094395F, 0F, 0F);
        tankPipeUpper = new ModelRenderer(this, 4, 38);
        tankPipeUpper.addBox(-0.5F, 1F, 1.5F, 1, 1, 3, false);
        tankPipeUpper.setRotationPoint(0F, 0F, 0F);
        tankPipeUpper.setTextureSize(128, 64);
        tankPipeUpper.mirror = true;
        setRotation(tankPipeUpper, 0F, 0F, 0F);
        tankBackBrace = new ModelRenderer(this, 0, 42);
        tankBackBrace.addBox(-3F, 2F, 0.5F, 6, 3, 2, false);
        tankBackBrace.setRotationPoint(0F, 0F, 0F);
        tankBackBrace.setTextureSize(128, 64);
        tankBackBrace.mirror = true;
        setRotation(tankBackBrace, 0.2443461F, 0F, 0F);
    }

    public void render(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight, boolean hasEffect) {
        render(matrix, getVertexBuilder(renderer, RENDER_TYPE, hasEffect), light, overlayLight, 1, 1, 1, 1);
    }

    @Override
    public void render(@Nonnull MatrixStack matrix, @Nonnull IVertexBuilder vertexBuilder, int light, int overlayLight, float red, float green, float blue, float alpha) {
        tankL.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        tankR.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        tankDock.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        capL.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        capR.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        tankBridge.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        tankPipeLower.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        tankPipeUpper.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        tankBackBrace.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
    }
}