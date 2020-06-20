package mekanism.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

public class ModelArmoredJetpack extends ModelJetpack {

    private static final ResourceLocation JETPACK_TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "jetpack.png");
    private static final RenderType WING_RENDER_TYPE = MekanismRenderType.mekStandard(JETPACK_TEXTURE);

    private final ModelRenderer chestplate;
    private final ModelRenderer leftGuardTop;
    private final ModelRenderer rightGuardTop;
    private final ModelRenderer middlePlate;
    private final ModelRenderer rightGuardBot;
    private final ModelRenderer leftGuardBot;
    private final ModelRenderer rightLight;
    private final ModelRenderer leftLight;

    public ModelArmoredJetpack() {
        super(JETPACK_TEXTURE, WING_RENDER_TYPE, -1.9F);
        chestplate = new ModelRenderer(this, 104, 22);
        chestplate.addBox(-4F, 1.333333F, -3F, 8, 4, 3, false);
        chestplate.setRotationPoint(0F, 0F, 0F);
        chestplate.setTextureSize(128, 64);
        chestplate.mirror = true;
        setRotation(chestplate, -0.3665191F, 0F, 0F);
        leftGuardTop = new ModelRenderer(this, 87, 31);
        leftGuardTop.addBox(0.95F, 3F, -5F, 3, 4, 2, false);
        leftGuardTop.setRotationPoint(0F, 0F, 0F);
        leftGuardTop.setTextureSize(128, 64);
        leftGuardTop.mirror = true;
        setRotation(leftGuardTop, 0.2094395F, 0F, 0F);
        leftGuardTop.mirror = false;
        rightGuardTop = new ModelRenderer(this, 87, 31);
        rightGuardTop.addBox(-3.95F, 3F, -5F, 3, 4, 2, false);
        rightGuardTop.setRotationPoint(0F, 0F, 0F);
        rightGuardTop.setTextureSize(128, 64);
        rightGuardTop.mirror = true;
        setRotation(rightGuardTop, 0.2094395F, 0F, 0F);
        middlePlate = new ModelRenderer(this, 93, 20);
        middlePlate.addBox(-1.5F, 3F, -6.2F, 3, 5, 3, false);
        middlePlate.setRotationPoint(0F, 0F, 0F);
        middlePlate.setTextureSize(128, 64);
        middlePlate.mirror = true;
        setRotation(middlePlate, 0.2094395F, 0F, 0F);
        middlePlate.mirror = false;
        rightGuardBot = new ModelRenderer(this, 84, 30);
        rightGuardBot.addBox(-3.5F, 5.5F, -6.5F, 2, 2, 2, false);
        rightGuardBot.setRotationPoint(0F, 0F, 0F);
        rightGuardBot.setTextureSize(128, 64);
        rightGuardBot.mirror = true;
        setRotation(rightGuardBot, 0.4712389F, 0F, 0F);
        rightGuardBot.mirror = false;
        leftGuardBot = new ModelRenderer(this, 84, 30);
        leftGuardBot.addBox(1.5F, 5.5F, -6.5F, 2, 2, 2, false);
        leftGuardBot.setRotationPoint(0F, 0F, 0F);
        leftGuardBot.setTextureSize(128, 64);
        leftGuardBot.mirror = true;
        setRotation(leftGuardBot, 0.4712389F, 0F, 0F);
        rightLight = new ModelRenderer(this, 81, 0);
        rightLight.addBox(-3F, 4F, -4.5F, 1, 3, 1, false);
        rightLight.setRotationPoint(0F, 0F, 0F);
        rightLight.setTextureSize(128, 64);
        rightLight.mirror = true;
        setRotation(rightLight, 0F, 0F, 0F);
        leftLight = new ModelRenderer(this, 81, 0);
        leftLight.addBox(2F, 4F, -4.5F, 1, 3, 1, false);
        leftLight.setRotationPoint(0F, 0F, 0F);
        leftLight.setTextureSize(128, 64);
        leftLight.mirror = true;
        setRotation(leftLight, 0F, 0F, 0F);
    }

    @Override
    public void render(@Nonnull MatrixStack matrix, @Nonnull IVertexBuilder vertexBuilder, int light, int overlayLight, float red, float green, float blue, float alpha) {
        super.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        matrix.push();
        matrix.translate(0, 0, -0.0625);
        chestplate.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        leftGuardTop.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        rightGuardTop.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        middlePlate.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        rightGuardBot.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        leftGuardBot.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        //Stuff below here uses full bright for the lighting
        light = MekanismRenderer.FULL_LIGHT;
        rightLight.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        leftLight.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        matrix.pop();
    }
}