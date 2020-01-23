package mekanism.generators.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

public class ModelGasGenerator extends Model {

    private static final ResourceLocation GENERATOR_TEXTURE = MekanismGenerators.rl("render/gas_burning_generator.png");
    private final RenderType RENDER_TYPE = getRenderType(GENERATOR_TEXTURE);

    private final ModelRenderer port4;
    private final ModelRenderer baseStand;
    private final ModelRenderer pillar4;
    private final ModelRenderer port3;
    private final ModelRenderer port2;
    private final ModelRenderer connectorAngle1;
    private final ModelRenderer pillar3;
    private final ModelRenderer pillar2;
    private final ModelRenderer pillar1;
    private final ModelRenderer center;
    private final ModelRenderer connector3;
    private final ModelRenderer port1;
    private final ModelRenderer connector4;
    private final ModelRenderer connectorAngle4;
    private final ModelRenderer base;
    private final ModelRenderer connectorAngle3;
    private final ModelRenderer connector2;
    private final ModelRenderer connectorAngle2;
    private final ModelRenderer connector1;

    public ModelGasGenerator() {
        super(RenderType::entityCutout);
        textureWidth = 128;
        textureHeight = 64;

        port4 = new ModelRenderer(this, 40, 34);
        port4.addBox(0F, 0F, 0F, 1, 8, 8, false);
        port4.setRotationPoint(7F, 12F, -4F);
        port4.setTextureSize(128, 64);
        port4.mirror = true;
        setRotation(port4, 0F, 0F, 0F);
        baseStand = new ModelRenderer(this, 0, 20);
        baseStand.addBox(0F, 0F, 0F, 13, 1, 13, false);
        baseStand.setRotationPoint(-6.5F, 19F, -6.5F);
        baseStand.setTextureSize(128, 64);
        baseStand.mirror = true;
        setRotation(baseStand, 0F, 0F, 0F);
        pillar4 = new ModelRenderer(this, 0, 0);
        pillar4.addBox(0F, 0F, 0F, 3, 9, 3, false);
        pillar4.setRotationPoint(4F, 10F, 4F);
        pillar4.setTextureSize(128, 64);
        pillar4.mirror = true;
        setRotation(pillar4, 0F, 0F, 0F);
        port3 = new ModelRenderer(this, 40, 50);
        port3.addBox(0F, 0F, 0F, 8, 8, 1, false);
        port3.setRotationPoint(-4F, 12F, 7F);
        port3.setTextureSize(128, 64);
        port3.mirror = true;
        setRotation(port3, 0F, 0F, 0F);
        port2 = new ModelRenderer(this, 40, 34);
        port2.addBox(0F, 0F, 0F, 1, 8, 8, false);
        port2.setRotationPoint(-8F, 12F, -4F);
        port2.setTextureSize(128, 64);
        port2.mirror = true;
        setRotation(port2, 0F, 0F, 0F);
        connectorAngle1 = new ModelRenderer(this, 48, 13);
        connectorAngle1.addBox(0F, 0F, 0.5F, 8, 1, 2, false);
        connectorAngle1.setRotationPoint(-4F, 13.5F, -6F);
        connectorAngle1.setTextureSize(128, 64);
        connectorAngle1.mirror = true;
        setRotation(connectorAngle1, 0.986111F, 0F, 0F);
        pillar3 = new ModelRenderer(this, 0, 0);
        pillar3.addBox(0F, 0F, 0F, 3, 9, 3, false);
        pillar3.setRotationPoint(-7F, 10F, 4F);
        pillar3.setTextureSize(128, 64);
        pillar3.mirror = true;
        setRotation(pillar3, 0F, 0F, 0F);
        pillar2 = new ModelRenderer(this, 0, 0);
        pillar2.addBox(0F, 0F, 0F, 3, 9, 3, false);
        pillar2.setRotationPoint(4F, 10F, -7F);
        pillar2.setTextureSize(128, 64);
        pillar2.mirror = true;
        setRotation(pillar2, 0F, 0F, 0F);
        pillar1 = new ModelRenderer(this, 0, 0);
        pillar1.addBox(0F, 0F, 0F, 3, 9, 3, false);
        pillar1.setRotationPoint(-7F, 10F, -7F);
        pillar1.setTextureSize(128, 64);
        pillar1.mirror = true;
        setRotation(pillar1, 0F, 0F, 0F);
        center = new ModelRenderer(this, 0, 34);
        center.addBox(0F, 0F, 0F, 10, 12, 10, false);
        center.setRotationPoint(-5F, 8F, -5F);
        center.setTextureSize(128, 64);
        center.mirror = true;
        setRotation(center, 0F, 0F, 0F);
        connector3 = new ModelRenderer(this, 39, 20);
        connector3.addBox(0F, 0F, 0F, 1, 1, 8, false);
        connector3.setRotationPoint(5F, 11F, -4F);
        connector3.setTextureSize(128, 64);
        connector3.mirror = true;
        setRotation(connector3, 0F, 0F, 0F);
        port1 = new ModelRenderer(this, 40, 50);
        port1.addBox(0F, 0F, 0F, 8, 8, 1, false);
        port1.setRotationPoint(-4F, 12F, -8F);
        port1.setTextureSize(128, 64);
        port1.mirror = true;
        setRotation(port1, 0F, 0F, 0F);
        connector4 = new ModelRenderer(this, 39, 29);
        connector4.addBox(0F, 0F, 0F, 8, 1, 1, false);
        connector4.setRotationPoint(-4F, 11F, 5F);
        connector4.setTextureSize(128, 64);
        connector4.mirror = true;
        setRotation(connector4, 0F, 0F, 0F);
        connectorAngle4 = new ModelRenderer(this, 48, 10);
        connectorAngle4.addBox(0F, 0F, -1F, 8, 2, 1, false);
        connectorAngle4.setRotationPoint(-4F, 11F, 6F);
        connectorAngle4.setTextureSize(128, 64);
        connectorAngle4.mirror = true;
        setRotation(connectorAngle4, 0.7941248F, 0F, 0F);
        base = new ModelRenderer(this, 0, 0);
        base.addBox(0F, 0F, 0F, 16, 4, 16, false);
        base.setRotationPoint(-8F, 20F, -8F);
        base.setTextureSize(128, 64);
        base.mirror = true;
        setRotation(base, 0F, 0F, 0F);
        connectorAngle3 = new ModelRenderer(this, 48, 0);
        connectorAngle3.addBox(-1F, 0F, 0F, 1, 2, 8, false);
        connectorAngle3.setRotationPoint(6F, 11F, -4F);
        connectorAngle3.setTextureSize(128, 64);
        connectorAngle3.mirror = true;
        setRotation(connectorAngle3, 0F, 0F, -0.7941248F);
        connector2 = new ModelRenderer(this, 39, 20);
        connector2.addBox(0F, 0F, 0F, 1, 1, 8, false);
        connector2.setRotationPoint(-6F, 11F, -4F);
        connector2.setTextureSize(128, 64);
        connector2.mirror = true;
        setRotation(connector2, 0F, 0F, 0F);
        connectorAngle2 = new ModelRenderer(this, 48, 0);
        connectorAngle2.addBox(0F, 0F, 0F, 1, 2, 8, false);
        connectorAngle2.setRotationPoint(-6F, 11F, -4F);
        connectorAngle2.setTextureSize(128, 64);
        connectorAngle2.mirror = true;
        setRotation(connectorAngle2, 0F, 0F, 0.7941248F);
        connector1 = new ModelRenderer(this, 48, 13);
        connector1.addBox(0F, 0F, 0F, 8, 1, 2, false);
        connector1.setRotationPoint(-4F, 13F, -7.5F);
        connector1.setTextureSize(128, 64);
        connector1.mirror = true;
        setRotation(connector1, 0F, 0F, 0F);
    }

    public void render(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight) {
        render(matrix, renderer.getBuffer(RENDER_TYPE), light, overlayLight, 1, 1, 1, 1);
    }

    @Override
    public void render(@Nonnull MatrixStack matrix, @Nonnull IVertexBuilder vertexBuilder, int light, int overlayLight, float red, float green, float blue, float alpha) {
        port4.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        baseStand.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        pillar4.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        port3.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        port2.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        connectorAngle1.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        pillar3.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        pillar2.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        pillar1.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        center.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        connector3.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        port1.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        connector4.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        connectorAngle4.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        base.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        connectorAngle3.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        connector2.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        connectorAngle2.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        connector1.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}