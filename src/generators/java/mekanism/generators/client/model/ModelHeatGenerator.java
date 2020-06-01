package mekanism.generators.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import mekanism.client.model.MekanismModel;
import mekanism.client.render.MekanismRenderType;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

public class ModelHeatGenerator extends MekanismModel {

    private static final ResourceLocation GENERATOR_TEXTURE = MekanismGenerators.rl("render/heat_generator.png");
    private static final ResourceLocation OVERLAY_ON = MekanismGenerators.rl("render/heat_generator_overlay_on.png");
    private static final ResourceLocation OVERLAY_OFF = MekanismGenerators.rl("render/heat_generator_overlay_off.png");
    private static final RenderType RENDER_TYPE_ON = MekanismRenderType.mekStandard(OVERLAY_ON);
    private static final RenderType RENDER_TYPE_OFF = MekanismRenderType.mekStandard(OVERLAY_OFF);

    private final RenderType RENDER_TYPE = getRenderType(GENERATOR_TEXTURE);

    private final ModelRenderer drum;
    private final ModelRenderer ring1;
    private final ModelRenderer ring2;
    private final ModelRenderer back;
    private final ModelRenderer bar1;
    private final ModelRenderer bar2;
    private final ModelRenderer plate;
    private final ModelRenderer fin8;
    private final ModelRenderer fin7;
    private final ModelRenderer fin1;
    private final ModelRenderer fin2;
    private final ModelRenderer fin3;
    private final ModelRenderer fin4;
    private final ModelRenderer fin5;
    private final ModelRenderer fin6;
    private final ModelRenderer base;

    public ModelHeatGenerator() {
        super(RenderType::getEntitySolid);
        textureWidth = 128;
        textureHeight = 64;

        drum = new ModelRenderer(this, 0, 22);
        drum.addBox(0F, 0F, 0F, 16, 9, 9, false);
        drum.setRotationPoint(-8F, 8.5F, -7.5F);
        drum.setTextureSize(128, 64);
        drum.mirror = true;
        setRotation(drum, 0F, 0F, 0F);
        ring1 = new ModelRenderer(this, 88, 0);
        ring1.addBox(0F, 0F, 0F, 2, 10, 10, false);
        ring1.setRotationPoint(3F, 8F, -8F);
        ring1.setTextureSize(128, 64);
        ring1.mirror = true;
        setRotation(ring1, 0F, 0F, 0F);
        ring2 = new ModelRenderer(this, 88, 0);
        ring2.addBox(0F, 0F, 0F, 2, 10, 10, false);
        ring2.setRotationPoint(-5F, 8F, -8F);
        ring2.setTextureSize(128, 64);
        ring2.mirror = true;
        setRotation(ring2, 0F, 0F, 0F);
        back = new ModelRenderer(this, 48, 0);
        back.addBox(0F, 0F, 0F, 16, 10, 4, false);
        back.setRotationPoint(-8F, 8F, 2F);
        back.setTextureSize(128, 64);
        back.mirror = true;
        setRotation(back, 0F, 0F, 0F);
        bar1 = new ModelRenderer(this, 88, 0);
        bar1.addBox(0F, 0F, 0F, 2, 9, 1, false);
        bar1.setRotationPoint(3F, 9F, 6F);
        bar1.setTextureSize(128, 64);
        bar1.mirror = true;
        setRotation(bar1, 0F, 0F, 0F);
        bar2 = new ModelRenderer(this, 88, 0);
        bar2.addBox(0F, 0F, 0F, 2, 9, 1, false);
        bar2.setRotationPoint(-5F, 9F, 6F);
        bar2.setTextureSize(128, 64);
        bar2.mirror = true;
        setRotation(bar2, 0F, 0F, 0F);
        plate = new ModelRenderer(this, 41, 22);
        plate.addBox(0F, 0F, 0F, 8, 6, 2, false);
        plate.setRotationPoint(-4F, 12F, 6F);
        plate.setTextureSize(128, 64);
        plate.mirror = true;
        setRotation(plate, 0F, 0F, 0F);
        fin8 = new ModelRenderer(this, 14, 40);
        fin8.addBox(0F, 0F, 0F, 16, 1, 2, false);
        fin8.setRotationPoint(-8F, 8F, 6F);
        fin8.setTextureSize(128, 64);
        fin8.mirror = true;
        setRotation(fin8, 0F, 0F, 0F);
        fin7 = new ModelRenderer(this, 14, 40);
        fin7.addBox(0F, 0F, 0F, 16, 1, 2, false);
        fin7.setRotationPoint(-8F, 10F, 6F);
        fin7.setTextureSize(128, 64);
        fin7.mirror = true;
        setRotation(fin7, 0F, 0F, 0F);
        fin1 = new ModelRenderer(this, 0, 40);
        fin1.addBox(0F, 0F, 0F, 4, 1, 2, false);
        fin1.setRotationPoint(4F, 12F, 6F);
        fin1.setTextureSize(128, 64);
        fin1.mirror = true;
        setRotation(fin1, 0F, 0F, 0F);
        fin1.mirror = false;
        fin2 = new ModelRenderer(this, 0, 40);
        fin2.addBox(0F, 0F, 0F, 4, 1, 2, false);
        fin2.setRotationPoint(4F, 14F, 6F);
        fin2.setTextureSize(128, 64);
        fin2.mirror = true;
        setRotation(fin2, 0F, 0F, 0F);
        fin2.mirror = false;
        fin3 = new ModelRenderer(this, 0, 40);
        fin3.addBox(0F, 0F, 0F, 4, 1, 2, false);
        fin3.setRotationPoint(4F, 16F, 6F);
        fin3.setTextureSize(128, 64);
        fin3.mirror = true;
        setRotation(fin3, 0F, 0F, 0F);
        fin3.mirror = false;
        fin4 = new ModelRenderer(this, 0, 40);
        fin4.addBox(0F, 0F, 0F, 4, 1, 2, false);
        fin4.setRotationPoint(-8F, 12F, 6F);
        fin4.setTextureSize(128, 64);
        fin4.mirror = true;
        setRotation(fin4, 0F, 0F, 0F);
        fin5 = new ModelRenderer(this, 0, 40);
        fin5.addBox(0F, 0F, 0F, 4, 1, 2, false);
        fin5.setRotationPoint(-8F, 14F, 6F);
        fin5.setTextureSize(128, 64);
        fin5.mirror = true;
        setRotation(fin5, 0F, 0F, 0F);
        fin6 = new ModelRenderer(this, 0, 40);
        fin6.addBox(0F, 0F, 0F, 4, 1, 2, false);
        fin6.setRotationPoint(-8F, 16F, 6F);
        fin6.setTextureSize(128, 64);
        fin6.mirror = true;
        setRotation(fin6, 0F, 0F, 0F);
        base = new ModelRenderer(this, 0, 0);
        base.addBox(0F, 0F, 0F, 16, 6, 16, false);
        base.setRotationPoint(-8F, 18F, -8F);
        base.setTextureSize(128, 64);
        base.mirror = true;
        setRotation(base, 0F, 0F, 0F);
    }

    public void render(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight, boolean on, boolean hasEffect) {
        //Render the main model
        render(matrix, getVertexBuilder(renderer, RENDER_TYPE, hasEffect), light, overlayLight, 1, 1, 1, 1);
        //Adjust size/positioning slightly and render the overlay
        matrix.push();
        matrix.scale(1.001F, 1.001F, 1.001F);
        matrix.translate(0, -0.0011, 0);
        render(matrix, getVertexBuilder(renderer, on ? RENDER_TYPE_ON : RENDER_TYPE_OFF, hasEffect), light, overlayLight, 1, 1, 1, 1);
        matrix.pop();
    }

    @Override
    public void render(@Nonnull MatrixStack matrix, @Nonnull IVertexBuilder vertexBuilder, int light, int overlayLight, float red, float green, float blue,
          float alpha) {
        drum.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        ring1.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        ring2.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        back.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        bar1.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        bar2.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        plate.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        fin8.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        fin7.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        fin1.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        fin2.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        fin3.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        fin4.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        fin5.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        fin6.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        base.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
    }
}
