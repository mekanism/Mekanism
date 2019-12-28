package mekanism.additions.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.additions.client.model.ModelBalloon;
import mekanism.additions.common.entity.EntityBalloon;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class RenderBalloon extends EntityRenderer<EntityBalloon> {

    private static final ModelBalloon model = new ModelBalloon();

    public RenderBalloon(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Nonnull
    @Override
    public ResourceLocation getEntityTexture(@Nonnull EntityBalloon entity) {
        return ModelBalloon.BALLOON_TEXTURE;
    }

    @Override
    public void func_225623_a_(@Nonnull EntityBalloon balloon, float entityYaw, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light) {
        matrix.func_227860_a_();
        if (balloon.isLatchedToEntity()) {
            //Shift the rendering of the balloon to be over the entity
            double x = balloon.latchedEntity.lastTickPosX + (balloon.latchedEntity.func_226277_ct_() - balloon.latchedEntity.lastTickPosX) * partialTick
                       - (balloon.lastTickPosX + (balloon.func_226277_ct_() - balloon.lastTickPosX) * partialTick);
            double y = balloon.latchedEntity.lastTickPosY + (balloon.latchedEntity.func_226278_cu_() - balloon.latchedEntity.lastTickPosY) * partialTick
                       - (balloon.lastTickPosY + (balloon.func_226278_cu_() - balloon.lastTickPosY) * partialTick)
                       + balloon.getAddedHeight();
            double z = balloon.latchedEntity.lastTickPosZ + (balloon.latchedEntity.func_226281_cx_() - balloon.latchedEntity.lastTickPosZ) * partialTick
                       - (balloon.lastTickPosZ + (balloon.func_226281_cx_() - balloon.lastTickPosZ) * partialTick);
            matrix.func_227861_a_(x, y, z);
        }
        model.render(matrix, renderer, light, balloon.color);
        matrix.func_227865_b_();
        super.func_225623_a_(balloon, entityYaw, partialTick, matrix, renderer, light);
    }

    @Override
    protected int func_225624_a_(EntityBalloon balloon, float partialTick) {
        //We always want our balloon to have full brightness
        return 15;
    }
}