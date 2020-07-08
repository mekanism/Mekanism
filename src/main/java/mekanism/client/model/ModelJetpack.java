package mekanism.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

public class ModelJetpack extends MekanismJavaModel {

    private static final ResourceLocation JETPACK_TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "jetpack.png");
    private static final RenderType WING_RENDER_TYPE = MekanismRenderType.mekStandard(JETPACK_TEXTURE);
    private final RenderType frameRenderType;
    private final RenderType wingRenderType;

    private final ModelRenderer packTop;
    private final ModelRenderer packBottom;
    private final ModelRenderer thrusterLeft;
    private final ModelRenderer thrusterRight;
    private final ModelRenderer fuelTubeRight;
    private final ModelRenderer fuelTubeLeft;
    private final ModelRenderer packMid;
    private final ModelRenderer packCore;
    private final ModelRenderer wingSupportL;
    private final ModelRenderer wingSupportR;
    private final ModelRenderer packTopRear;
    private final ModelRenderer extendoSupportL;
    private final ModelRenderer extendoSupportR;
    private final ModelRenderer wingBladeL;
    private final ModelRenderer wingBladeR;
    private final ModelRenderer packDoodad2;
    private final ModelRenderer packDoodad3;
    private final ModelRenderer bottomThruster;
    private final ModelRenderer light1;
    private final ModelRenderer light2;
    private final ModelRenderer light3;

    public ModelJetpack() {
        this(JETPACK_TEXTURE, WING_RENDER_TYPE, -3);
    }

    /**
     * @param fuelZ Z offset for the Fuel Tubes, thrusters are offset by {@code fuelZ - 0.5}
     */
    protected ModelJetpack(ResourceLocation texture, RenderType wingRenderType, float fuelZ) {
        super(RenderType::getEntitySolid);
        this.frameRenderType = getRenderType(texture);
        this.wingRenderType = wingRenderType;
        textureWidth = 128;
        textureHeight = 64;

        packTop = new ModelRenderer(this, 92, 28);
        packTop.addBox(-4F, 0F, 4F, 8, 4, 1, false);
        packTop.setRotationPoint(0F, 0F, 0F);
        packTop.setTextureSize(128, 64);
        packTop.mirror = true;
        setRotation(packTop, 0.2094395F, 0F, 0F);
        packBottom = new ModelRenderer(this, 92, 42);
        packBottom.addBox(-4F, 4.1F, 1.5F, 8, 4, 4, false);
        packBottom.setRotationPoint(0F, 0F, 0F);
        packBottom.setTextureSize(128, 64);
        packBottom.mirror = true;
        setRotation(packBottom, -0.0872665F, 0F, 0F);
        thrusterLeft = new ModelRenderer(this, 69, 30);
        thrusterLeft.addBox(7.8F, 1.5F, fuelZ - 0.5F, 3, 3, 3, false);
        thrusterLeft.setRotationPoint(0F, 0F, 0F);
        thrusterLeft.setTextureSize(128, 64);
        thrusterLeft.mirror = true;
        setRotation(thrusterLeft, 0.7853982F, -0.715585F, 0.3490659F);
        thrusterRight = new ModelRenderer(this, 69, 30);
        thrusterRight.addBox(-10.8F, 1.5F, fuelZ - 0.5F, 3, 3, 3, false);
        thrusterRight.setRotationPoint(0F, 0F, 0F);
        thrusterRight.setTextureSize(128, 64);
        thrusterRight.mirror = true;
        setRotation(thrusterRight, 0.7853982F, 0.715585F, -0.3490659F);
        fuelTubeRight = new ModelRenderer(this, 92, 23);
        fuelTubeRight.addBox(-11.2F, 2F, fuelZ, 8, 2, 2, false);
        fuelTubeRight.setRotationPoint(0F, 0F, 0F);
        fuelTubeRight.setTextureSize(128, 64);
        fuelTubeRight.mirror = true;
        setRotation(fuelTubeRight, 0.7853982F, 0.715585F, -0.3490659F);
        fuelTubeLeft = new ModelRenderer(this, 92, 23);
        fuelTubeLeft.addBox(3.2F, 2F, fuelZ, 8, 2, 2, false);
        fuelTubeLeft.setRotationPoint(0F, 0F, 0F);
        fuelTubeLeft.setTextureSize(128, 64);
        fuelTubeLeft.mirror = true;
        setRotation(fuelTubeLeft, 0.7853982F, -0.715585F, 0.3490659F);
        packMid = new ModelRenderer(this, 92, 34);
        packMid.addBox(-4F, 3.3F, 1.5F, 8, 1, 4, false);
        packMid.setRotationPoint(0F, 0F, 0F);
        packMid.setTextureSize(128, 64);
        packMid.mirror = true;
        setRotation(packMid, 0F, 0F, 0F);
        packCore = new ModelRenderer(this, 69, 2);
        packCore.addBox(-3.5F, 3F, 2F, 7, 1, 3, false);
        packCore.setRotationPoint(0F, 0F, 0F);
        packCore.setTextureSize(128, 64);
        packCore.mirror = true;
        setRotation(packCore, 0F, 0F, 0F);
        wingSupportL = new ModelRenderer(this, 71, 55);
        wingSupportL.addBox(3F, -1F, 2.2F, 7, 2, 2, false);
        wingSupportL.setRotationPoint(0F, 0F, 0F);
        wingSupportL.setTextureSize(128, 64);
        wingSupportL.mirror = true;
        setRotation(wingSupportL, 0F, 0F, 0.2792527F);
        wingSupportR = new ModelRenderer(this, 71, 55);
        wingSupportR.addBox(-10F, -1F, 2.2F, 7, 2, 2, false);
        wingSupportR.setRotationPoint(0F, 0F, 0F);
        wingSupportR.setTextureSize(128, 64);
        wingSupportR.mirror = true;
        setRotation(wingSupportR, 0F, 0F, -0.2792527F);
        packTopRear = new ModelRenderer(this, 106, 28);
        packTopRear.addBox(-4F, 1F, 1F, 8, 3, 3, false);
        packTopRear.setRotationPoint(0F, 0F, 0F);
        packTopRear.setTextureSize(128, 64);
        packTopRear.mirror = true;
        setRotation(packTopRear, 0.2094395F, 0F, 0F);
        extendoSupportL = new ModelRenderer(this, 94, 16);
        extendoSupportL.addBox(8F, -0.2F, 2.5F, 9, 1, 1, false);
        extendoSupportL.setRotationPoint(0F, 0F, 0F);
        extendoSupportL.setTextureSize(128, 64);
        extendoSupportL.mirror = true;
        setRotation(extendoSupportL, 0F, 0F, 0.2792527F);
        extendoSupportR = new ModelRenderer(this, 94, 16);
        extendoSupportR.addBox(-17F, -0.2F, 2.5F, 9, 1, 1, false);
        extendoSupportR.setRotationPoint(0F, 0F, 0F);
        extendoSupportR.setTextureSize(128, 64);
        extendoSupportR.mirror = true;
        setRotation(extendoSupportR, 0F, 0F, -0.2792527F);
        wingBladeL = new ModelRenderer(this, 62, 5);
        wingBladeL.addBox(3.3F, 1.1F, 3F, 14, 2, 0, false);
        wingBladeL.setRotationPoint(0F, 0F, 0F);
        wingBladeL.setTextureSize(128, 64);
        wingBladeL.mirror = true;
        setRotation(wingBladeL, 0F, 0F, 0.2094395F);
        wingBladeR = new ModelRenderer(this, 62, 5);
        wingBladeR.addBox(-17.3F, 1.1F, 3F, 14, 2, 0, false);
        wingBladeR.setRotationPoint(0F, 0F, 0F);
        wingBladeR.setTextureSize(128, 64);
        wingBladeR.mirror = true;
        setRotation(wingBladeR, 0F, 0F, -0.2094395F);
        packDoodad2 = new ModelRenderer(this, 116, 0);
        packDoodad2.addBox(1F, 0.5F, 4.2F, 2, 1, 1, false);
        packDoodad2.setRotationPoint(0F, 0F, 0F);
        packDoodad2.setTextureSize(128, 64);
        packDoodad2.mirror = true;
        setRotation(packDoodad2, 0.2094395F, 0F, 0F);
        packDoodad3 = new ModelRenderer(this, 116, 0);
        packDoodad3.addBox(1F, 2F, 4.2F, 2, 1, 1, false);
        packDoodad3.setRotationPoint(0F, 0F, 0F);
        packDoodad3.setTextureSize(128, 64);
        packDoodad3.mirror = true;
        setRotation(packDoodad3, 0.2094395F, 0F, 0F);
        bottomThruster = new ModelRenderer(this, 68, 26);
        bottomThruster.addBox(-3F, 8F, 2.333333F, 6, 1, 2, false);
        bottomThruster.setRotationPoint(0F, 0F, 0F);
        bottomThruster.setTextureSize(128, 64);
        bottomThruster.mirror = true;
        setRotation(bottomThruster, 0F, 0F, 0F);
        light1 = new ModelRenderer(this, 55, 2);
        light1.addBox(2F, 6.55F, 4F, 1, 1, 1, false);
        light1.setRotationPoint(0F, 0F, 0F);
        light1.setTextureSize(128, 64);
        light1.mirror = true;
        setRotation(light1, 0F, 0F, 0F);
        light2 = new ModelRenderer(this, 55, 2);
        light2.addBox(0F, 6.55F, 4F, 1, 1, 1, false);
        light2.setRotationPoint(0F, 0F, 0F);
        light2.setTextureSize(128, 64);
        light2.mirror = true;
        setRotation(light2, 0F, 0F, 0F);
        light3 = new ModelRenderer(this, 55, 2);
        light3.addBox(-3F, 6.55F, 4F, 1, 1, 1, false);
        light3.setRotationPoint(0F, 0F, 0F);
        light3.setTextureSize(128, 64);
        light3.mirror = true;
        setRotation(light3, 0F, 0F, 0F);
    }

    public void render(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight, boolean hasEffect) {
        render(matrix, getVertexBuilder(renderer, frameRenderType, hasEffect), light, overlayLight, 1, 1, 1, 1);
        renderWings(matrix, getVertexBuilder(renderer, wingRenderType, hasEffect), MekanismRenderer.FULL_LIGHT, overlayLight, 1, 1, 1, 0.2F);
    }

    @Override
    public void render(@Nonnull MatrixStack matrix, @Nonnull IVertexBuilder vertexBuilder, int light, int overlayLight, float red, float green, float blue, float alpha) {
        packTop.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        packBottom.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        thrusterLeft.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        thrusterRight.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        fuelTubeRight.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        fuelTubeLeft.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        packMid.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        wingSupportL.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        wingSupportR.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        packTopRear.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        extendoSupportL.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        extendoSupportR.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        packDoodad2.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        packDoodad3.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        bottomThruster.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);

        //Stuff below here uses full bright for the lighting
        light = MekanismRenderer.FULL_LIGHT;
        light1.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        light2.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        light3.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        packCore.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
    }

    public void renderWings(@Nonnull MatrixStack matrix, @Nonnull IVertexBuilder vertexBuilder, int light, int overlayLight, float red, float green, float blue, float alpha) {
        wingBladeL.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        wingBladeR.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
    }
}