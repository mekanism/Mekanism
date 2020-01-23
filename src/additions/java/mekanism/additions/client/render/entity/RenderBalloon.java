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
    public void render(@Nonnull EntityBalloon balloon, float entityYaw, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light) {
        matrix.push();
        if (balloon.isLatchedToEntity()) {
            //Shift the rendering of the balloon to be over the entity
            double x = balloon.latchedEntity.lastTickPosX + (balloon.latchedEntity.getPosX() - balloon.latchedEntity.lastTickPosX) * partialTick
                       - (balloon.lastTickPosX + (balloon.getPosX() - balloon.lastTickPosX) * partialTick);
            double y = balloon.latchedEntity.lastTickPosY + (balloon.latchedEntity.getPosY() - balloon.latchedEntity.lastTickPosY) * partialTick
                       - (balloon.lastTickPosY + (balloon.getPosY() - balloon.lastTickPosY) * partialTick)
                       + balloon.getAddedHeight();
            double z = balloon.latchedEntity.lastTickPosZ + (balloon.latchedEntity.getPosZ() - balloon.latchedEntity.lastTickPosZ) * partialTick
                       - (balloon.lastTickPosZ + (balloon.getPosZ() - balloon.lastTickPosZ) * partialTick);
            matrix.translate(x, y, z);
        }
        model.render(matrix, renderer, light, balloon.color);
        matrix.pop();
        super.render(balloon, entityYaw, partialTick, matrix, renderer, light);
    }

    @Override
    protected int getBlockLight(EntityBalloon balloon, float partialTick) {
        //We always want our balloon to have full brightness
        return 15;
    }
}