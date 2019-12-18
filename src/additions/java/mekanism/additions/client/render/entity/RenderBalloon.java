package mekanism.additions.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import mekanism.additions.client.model.ModelBalloon;
import mekanism.additions.common.entity.EntityBalloon;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;

public class RenderBalloon extends EntityRenderer<EntityBalloon> {

    private static final ResourceLocation BALLOON_TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "balloon.png");
    private static final RenderType BALLOON_RENDER_TYPE = RenderType.func_228634_a_(BALLOON_TEXTURE);
    public ModelBalloon model = new ModelBalloon();

    public RenderBalloon(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Nonnull
    @Override
    public ResourceLocation getEntityTexture(@Nonnull EntityBalloon entity) {
        return MekanismUtils.getResource(ResourceType.RENDER, "balloon.png");
    }

    @Override
    public void func_225623_a_(@Nonnull EntityBalloon balloon, float entityYaw, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light) {
        double x = 0;
        double y = 0;
        double z = 0;
        if (balloon.isLatchedToEntity()) {
            //TODO: 1.15 Verify
            x = balloon.latchedEntity.lastTickPosX + balloon.latchedEntity.func_226277_ct_() * partialTick - balloon.latchedEntity.lastTickPosX * partialTick
                - balloon.lastTickPosX + balloon.func_226277_ct_() * partialTick + balloon.lastTickPosX * partialTick;
            y = balloon.latchedEntity.lastTickPosY + balloon.latchedEntity.func_226277_ct_() * partialTick - balloon.latchedEntity.lastTickPosY * partialTick
                - balloon.lastTickPosY + balloon.func_226277_ct_() * partialTick + balloon.lastTickPosY * partialTick + balloon.getAddedHeight();
            z = balloon.latchedEntity.lastTickPosZ + balloon.latchedEntity.func_226281_cx_() * partialTick - balloon.latchedEntity.lastTickPosZ * partialTick
                - balloon.lastTickPosZ + balloon.func_226281_cx_() * partialTick + balloon.lastTickPosZ * partialTick;
        }

        matrix.func_227860_a_();
        matrix.func_227861_a_(x, y, z);
        matrix.func_227863_a_(Vector3f.field_229179_b_.func_229187_a_(180));
        //TODO: 1.15
        //model.render(0.0625F, balloon.color);
        IVertexBuilder vertexBuilder = renderer.getBuffer(BALLOON_RENDER_TYPE);
        model.func_225598_a_(matrix, vertexBuilder, light, OverlayTexture.field_229196_a_, balloon.color.getColor(0), balloon.color.getColor(1), balloon.color.getColor(2), 1.0F);
        matrix.func_227865_b_();
        super.func_225623_a_(balloon, entityYaw, partialTick, matrix, renderer, light);
    }
}