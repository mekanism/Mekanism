package mekanism.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

public class ModelJetpack extends Model {

    private static final ResourceLocation JETPACK_TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "jetpack.png");
    private static final RenderType WING_RENDER_TYPE = MekanismRenderType.mekStandard(JETPACK_TEXTURE);
    private final RenderType RENDER_TYPE = getRenderType(JETPACK_TEXTURE);

    private final ModelRenderer Packtop;
    private final ModelRenderer Packbottom;
    private final ModelRenderer Thrusterleft;
    private final ModelRenderer Thrusterright;
    private final ModelRenderer Fueltuberight;
    private final ModelRenderer Fueltubeleft;
    private final ModelRenderer Packmid;
    private final ModelRenderer Packcore;
    private final ModelRenderer WingsupportL;
    private final ModelRenderer WingsupportR;
    private final ModelRenderer Packtoprear;
    private final ModelRenderer ExtendosupportL;
    private final ModelRenderer ExtendosupportR;
    private final ModelRenderer WingbladeL;
    private final ModelRenderer WingbladeR;
    private final ModelRenderer Packdoodad2;
    private final ModelRenderer Packdoodad3;
    private final ModelRenderer Bottomthruster;
    private final ModelRenderer light1;
    private final ModelRenderer light2;
    private final ModelRenderer light3;

    public ModelJetpack() {
        super(RenderType::getEntitySolid);
        textureWidth = 128;
        textureHeight = 64;

        Packtop = new ModelRenderer(this, 92, 28);
        Packtop.addBox(-4F, 0F, 4F, 8, 4, 1, false);
        Packtop.setRotationPoint(0F, 0F, 0F);
        Packtop.setTextureSize(128, 64);
        Packtop.mirror = true;
        setRotation(Packtop, 0.2094395F, 0F, 0F);
        Packbottom = new ModelRenderer(this, 92, 42);
        Packbottom.addBox(-4F, 4.1F, 1.5F, 8, 4, 4, false);
        Packbottom.setRotationPoint(0F, 0F, 0F);
        Packbottom.setTextureSize(128, 64);
        Packbottom.mirror = true;
        setRotation(Packbottom, -0.0872665F, 0F, 0F);
        Thrusterleft = new ModelRenderer(this, 69, 30);
        Thrusterleft.addBox(7.8F, 1.5F, -3.5F, 3, 3, 3, false);
        Thrusterleft.setRotationPoint(0F, 0F, 0F);
        Thrusterleft.setTextureSize(128, 64);
        Thrusterleft.mirror = true;
        setRotation(Thrusterleft, 0.7853982F, -0.715585F, 0.3490659F);
        Thrusterright = new ModelRenderer(this, 69, 30);
        Thrusterright.addBox(-10.8F, 1.5F, -3.5F, 3, 3, 3, false);
        Thrusterright.setRotationPoint(0F, 0F, 0F);
        Thrusterright.setTextureSize(128, 64);
        Thrusterright.mirror = true;
        setRotation(Thrusterright, 0.7853982F, 0.715585F, -0.3490659F);
        Fueltuberight = new ModelRenderer(this, 92, 23);
        Fueltuberight.addBox(-11.2F, 2F, -3F, 8, 2, 2, false);
        Fueltuberight.setRotationPoint(0F, 0F, 0F);
        Fueltuberight.setTextureSize(128, 64);
        Fueltuberight.mirror = true;
        setRotation(Fueltuberight, 0.7853982F, 0.715585F, -0.3490659F);
        Fueltubeleft = new ModelRenderer(this, 92, 23);
        Fueltubeleft.addBox(3.2F, 2F, -3F, 8, 2, 2, false);
        Fueltubeleft.setRotationPoint(0F, 0F, 0F);
        Fueltubeleft.setTextureSize(128, 64);
        Fueltubeleft.mirror = true;
        setRotation(Fueltubeleft, 0.7853982F, -0.715585F, 0.3490659F);
        Packmid = new ModelRenderer(this, 92, 34);
        Packmid.addBox(-4F, 3.3F, 1.5F, 8, 1, 4, false);
        Packmid.setRotationPoint(0F, 0F, 0F);
        Packmid.setTextureSize(128, 64);
        Packmid.mirror = true;
        setRotation(Packmid, 0F, 0F, 0F);
        Packcore = new ModelRenderer(this, 69, 2);
        Packcore.addBox(-3.5F, 3F, 2F, 7, 1, 3, false);
        Packcore.setRotationPoint(0F, 0F, 0F);
        Packcore.setTextureSize(128, 64);
        Packcore.mirror = true;
        setRotation(Packcore, 0F, 0F, 0F);
        WingsupportL = new ModelRenderer(this, 71, 55);
        WingsupportL.addBox(3F, -1F, 2.2F, 7, 2, 2, false);
        WingsupportL.setRotationPoint(0F, 0F, 0F);
        WingsupportL.setTextureSize(128, 64);
        WingsupportL.mirror = true;
        setRotation(WingsupportL, 0F, 0F, 0.2792527F);
        WingsupportR = new ModelRenderer(this, 71, 55);
        WingsupportR.addBox(-10F, -1F, 2.2F, 7, 2, 2, false);
        WingsupportR.setRotationPoint(0F, 0F, 0F);
        WingsupportR.setTextureSize(128, 64);
        WingsupportR.mirror = true;
        setRotation(WingsupportR, 0F, 0F, -0.2792527F);
        Packtoprear = new ModelRenderer(this, 106, 28);
        Packtoprear.addBox(-4F, 1F, 1F, 8, 3, 3, false);
        Packtoprear.setRotationPoint(0F, 0F, 0F);
        Packtoprear.setTextureSize(128, 64);
        Packtoprear.mirror = true;
        setRotation(Packtoprear, 0.2094395F, 0F, 0F);
        ExtendosupportL = new ModelRenderer(this, 94, 16);
        ExtendosupportL.addBox(8F, -0.2F, 2.5F, 9, 1, 1, false);
        ExtendosupportL.setRotationPoint(0F, 0F, 0F);
        ExtendosupportL.setTextureSize(128, 64);
        ExtendosupportL.mirror = true;
        setRotation(ExtendosupportL, 0F, 0F, 0.2792527F);
        ExtendosupportR = new ModelRenderer(this, 94, 16);
        ExtendosupportR.addBox(-17F, -0.2F, 2.5F, 9, 1, 1, false);
        ExtendosupportR.setRotationPoint(0F, 0F, 0F);
        ExtendosupportR.setTextureSize(128, 64);
        ExtendosupportR.mirror = true;
        setRotation(ExtendosupportR, 0F, 0F, -0.2792527F);
        WingbladeL = new ModelRenderer(this, 62, 5);
        WingbladeL.addBox(3.3F, 1.1F, 3F, 14, 2, 0, false);
        WingbladeL.setRotationPoint(0F, 0F, 0F);
        WingbladeL.setTextureSize(128, 64);
        WingbladeL.mirror = true;
        setRotation(WingbladeL, 0F, 0F, 0.2094395F);
        WingbladeR = new ModelRenderer(this, 62, 5);
        WingbladeR.addBox(-17.3F, 1.1F, 3F, 14, 2, 0, false);
        WingbladeR.setRotationPoint(0F, 0F, 0F);
        WingbladeR.setTextureSize(128, 64);
        WingbladeR.mirror = true;
        setRotation(WingbladeR, 0F, 0F, -0.2094395F);
        Packdoodad2 = new ModelRenderer(this, 116, 0);
        Packdoodad2.addBox(1F, 0.5F, 4.2F, 2, 1, 1, false);
        Packdoodad2.setRotationPoint(0F, 0F, 0F);
        Packdoodad2.setTextureSize(128, 64);
        Packdoodad2.mirror = true;
        setRotation(Packdoodad2, 0.2094395F, 0F, 0F);
        Packdoodad3 = new ModelRenderer(this, 116, 0);
        Packdoodad3.addBox(1F, 2F, 4.2F, 2, 1, 1, false);
        Packdoodad3.setRotationPoint(0F, 0F, 0F);
        Packdoodad3.setTextureSize(128, 64);
        Packdoodad3.mirror = true;
        setRotation(Packdoodad3, 0.2094395F, 0F, 0F);
        Bottomthruster = new ModelRenderer(this, 68, 26);
        Bottomthruster.addBox(-3F, 8F, 2.333333F, 6, 1, 2, false);
        Bottomthruster.setRotationPoint(0F, 0F, 0F);
        Bottomthruster.setTextureSize(128, 64);
        Bottomthruster.mirror = true;
        setRotation(Bottomthruster, 0F, 0F, 0F);
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
        render(matrix, getVertexBuilder(renderer, RENDER_TYPE, hasEffect), light, overlayLight, 1, 1, 1, 1);
        //TODO: Should our wing render type have cull enabled? It previously was enabled
        renderWings(matrix, getVertexBuilder(renderer, WING_RENDER_TYPE, hasEffect), MekanismRenderer.FULL_LIGHT, overlayLight, 1, 1, 1, 0.2F);
    }

    private IVertexBuilder getVertexBuilder(@Nonnull IRenderTypeBuffer renderer, RenderType renderType, boolean hasEffect) {
        return ItemRenderer.getBuffer(renderer, renderType, false, hasEffect);
    }

    @Override
    public void render(@Nonnull MatrixStack matrix, @Nonnull IVertexBuilder vertexBuilder, int light, int overlayLight, float red, float green, float blue, float alpha) {
        Packtop.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        Packbottom.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        Thrusterleft.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        Thrusterright.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        Fueltuberight.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        Fueltubeleft.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        Packmid.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);

        WingsupportL.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        WingsupportR.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        Packtoprear.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        ExtendosupportL.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        ExtendosupportR.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);

        Packdoodad2.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        Packdoodad3.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        Bottomthruster.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);

        //Stuff below here uses full bright for the lighting
        light = MekanismRenderer.FULL_LIGHT;
        Packcore.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);

        light1.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        light2.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        light3.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        Packcore.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
    }

    public void renderWings(@Nonnull MatrixStack matrix, @Nonnull IVertexBuilder vertexBuilder, int light, int overlayLight, float red, float green, float blue, float alpha) {
        WingbladeL.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        WingbladeR.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}