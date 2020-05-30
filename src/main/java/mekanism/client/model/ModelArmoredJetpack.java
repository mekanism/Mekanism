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

    private final ModelRenderer Chestplate;
    private final ModelRenderer Leftguardtop;
    private final ModelRenderer Rightguardtop;
    private final ModelRenderer middleplate;
    private final ModelRenderer Rightguardbot;
    private final ModelRenderer Leftguardbot;
    private final ModelRenderer Rightlight;
    private final ModelRenderer Leftlight;

    public ModelArmoredJetpack() {
        super(JETPACK_TEXTURE, WING_RENDER_TYPE, -1.9F);
        Chestplate = new ModelRenderer(this, 104, 22);
        Chestplate.addBox(-4F, 1.333333F, -3F, 8, 4, 3, false);
        Chestplate.setRotationPoint(0F, 0F, 0F);
        Chestplate.setTextureSize(128, 64);
        Chestplate.mirror = true;
        setRotation(Chestplate, -0.3665191F, 0F, 0F);
        Leftguardtop = new ModelRenderer(this, 87, 31);
        Leftguardtop.addBox(0.95F, 3F, -5F, 3, 4, 2, false);
        Leftguardtop.setRotationPoint(0F, 0F, 0F);
        Leftguardtop.setTextureSize(128, 64);
        Leftguardtop.mirror = true;
        setRotation(Leftguardtop, 0.2094395F, 0F, 0F);
        Leftguardtop.mirror = false;
        Rightguardtop = new ModelRenderer(this, 87, 31);
        Rightguardtop.addBox(-3.95F, 3F, -5F, 3, 4, 2, false);
        Rightguardtop.setRotationPoint(0F, 0F, 0F);
        Rightguardtop.setTextureSize(128, 64);
        Rightguardtop.mirror = true;
        setRotation(Rightguardtop, 0.2094395F, 0F, 0F);
        middleplate = new ModelRenderer(this, 93, 20);
        middleplate.addBox(-1.5F, 3F, -6.2F, 3, 5, 3, false);
        middleplate.setRotationPoint(0F, 0F, 0F);
        middleplate.setTextureSize(128, 64);
        middleplate.mirror = true;
        setRotation(middleplate, 0.2094395F, 0F, 0F);
        middleplate.mirror = false;
        Rightguardbot = new ModelRenderer(this, 84, 30);
        Rightguardbot.addBox(-3.5F, 5.5F, -6.5F, 2, 2, 2, false);
        Rightguardbot.setRotationPoint(0F, 0F, 0F);
        Rightguardbot.setTextureSize(128, 64);
        Rightguardbot.mirror = true;
        setRotation(Rightguardbot, 0.4712389F, 0F, 0F);
        Rightguardbot.mirror = false;
        Leftguardbot = new ModelRenderer(this, 84, 30);
        Leftguardbot.addBox(1.5F, 5.5F, -6.5F, 2, 2, 2, false);
        Leftguardbot.setRotationPoint(0F, 0F, 0F);
        Leftguardbot.setTextureSize(128, 64);
        Leftguardbot.mirror = true;
        setRotation(Leftguardbot, 0.4712389F, 0F, 0F);
        Rightlight = new ModelRenderer(this, 81, 0);
        Rightlight.addBox(-3F, 4F, -4.5F, 1, 3, 1, false);
        Rightlight.setRotationPoint(0F, 0F, 0F);
        Rightlight.setTextureSize(128, 64);
        Rightlight.mirror = true;
        setRotation(Rightlight, 0F, 0F, 0F);
        Leftlight = new ModelRenderer(this, 81, 0);
        Leftlight.addBox(2F, 4F, -4.5F, 1, 3, 1, false);
        Leftlight.setRotationPoint(0F, 0F, 0F);
        Leftlight.setTextureSize(128, 64);
        Leftlight.mirror = true;
        setRotation(Leftlight, 0F, 0F, 0F);
    }

    @Override
    public void render(@Nonnull MatrixStack matrix, @Nonnull IVertexBuilder vertexBuilder, int light, int overlayLight, float red, float green, float blue, float alpha) {
        super.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        matrix.push();
        matrix.translate(0, 0, -0.0625);
        Chestplate.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        Leftguardtop.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        Rightguardtop.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        middleplate.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        Rightguardbot.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        Leftguardbot.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        //Stuff below here uses full bright for the lighting
        light = MekanismRenderer.FULL_LIGHT;
        Rightlight.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        Leftlight.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        matrix.pop();
    }
}