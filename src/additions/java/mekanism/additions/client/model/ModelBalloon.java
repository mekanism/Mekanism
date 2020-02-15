package mekanism.additions.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import mekanism.additions.common.MekanismAdditions;
import mekanism.api.text.EnumColor;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;

public class ModelBalloon extends Model {

    public static final ResourceLocation BALLOON_TEXTURE = MekanismAdditions.rl("textures/item/balloon.png");
    private final RenderType RENDER_TYPE = getRenderType(BALLOON_TEXTURE);

    private final ModelRenderer Balloon2;
    private final ModelRenderer Balloon1;
    private final ModelRenderer Balloon3;
    private final ModelRenderer Balloonnub;
    private final ModelRenderer String;

    public ModelBalloon() {
        super(RenderType::getEntitySolid);
        textureWidth = 64;
        textureHeight = 32;

        Balloon2 = new ModelRenderer(this, 0, 0);
        Balloon2.addBox(-2.5F, -2, -2, 5, 4, 4, false);
        Balloon2.setRotationPoint(0F, 0F, 0F);
        Balloon2.setTextureSize(64, 32);
        Balloon2.mirror = true;
        setRotation(Balloon2, 0F, 0F, 0F);
        Balloon1 = new ModelRenderer(this, 0, 8);
        Balloon1.addBox(-2F, -2F, -2.5F, 4, 4, 5, false);
        Balloon1.setRotationPoint(0F, 0F, 0F);
        Balloon1.setTextureSize(64, 32);
        Balloon1.mirror = true;
        setRotation(Balloon1, 0F, 0F, 0F);
        Balloon3 = new ModelRenderer(this, 18, 0);
        Balloon3.addBox(-2F, -2.5F, -2F, 4, 5, 4, false);
        Balloon3.setRotationPoint(0F, 0F, 0F);
        Balloon3.setTextureSize(64, 32);
        Balloon3.mirror = true;
        setRotation(Balloon3, 0F, 0F, 0F);
        Balloonnub = new ModelRenderer(this, 18, 9);
        Balloonnub.addBox(-0.5F, 2.5F, -0.5F, 1, 1, 1, false);
        Balloonnub.setRotationPoint(0F, 0F, 0F);
        Balloonnub.setTextureSize(64, 32);
        Balloonnub.mirror = true;
        setRotation(Balloonnub, 0F, 0F, 0F);
        String = new ModelRenderer(this, 34, 0);
        String.addBox(-0.5F, 3.5F, -0.5F, 1, 11, 1, false);
        String.setRotationPoint(0F, 0F, 0F);
        String.setTextureSize(64, 32);
        String.mirror = true;
        setRotation(String, 0F, 0F, 0F);
    }

    public void render(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, EnumColor color) {
        matrix.push();
        //Flip the balloon so it is facing up instead of down
        matrix.rotate(Vector3f.XP.rotationDegrees(180));
        IVertexBuilder vertexBuilder = renderer.getBuffer(RENDER_TYPE);
        render(matrix, vertexBuilder, light, OverlayTexture.NO_OVERLAY, color.getColor(0), color.getColor(1), color.getColor(2), 1);
        matrix.pop();
    }

    @Override
    public void render(@Nonnull MatrixStack matrix, @Nonnull IVertexBuilder vertexBuilder, int light, int overlayLight, float red, float green, float blue, float alpha) {
        matrix.push();
        matrix.scale(1.5F, 1.5F, 1.5F);
        matrix.translate(0, -0.07, 0);

        Balloon2.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        Balloon1.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        Balloon3.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        Balloonnub.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        matrix.pop();

        matrix.push();
        matrix.scale(0.2F, 1, 0.2F);
        String.render(matrix, vertexBuilder, light, overlayLight, 1, 1, 1, 1);
        matrix.pop();
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}