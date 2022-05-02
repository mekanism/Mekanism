package mekanism.additions.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.additions.client.model.AdditionsModelCache;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.entity.EntityBalloon;
import mekanism.api.text.EnumColor;
import mekanism.client.model.BaseModelCache.JSONModelData;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

public class RenderBalloon extends EntityRenderer<EntityBalloon> {

    public static final ResourceLocation BALLOON_TEXTURE = MekanismAdditions.rl("textures/item/balloon.png");

    public RenderBalloon(EntityRendererProvider.Context context) {
        super(context);
    }

    @Nonnull
    @Override
    public ResourceLocation getTextureLocation(@Nonnull EntityBalloon entity) {
        return BALLOON_TEXTURE;
    }

    @Override
    public void render(@Nonnull EntityBalloon balloon, float entityYaw, float partialTick, @Nonnull PoseStack matrix, @Nonnull MultiBufferSource renderer, int light) {
        matrix.pushPose();
        matrix.translate(-0.5, -1, -0.5);

        if (balloon.isLatchedToEntity()) {
            //Shift the rendering of the balloon to be over the entity
            double x = balloon.latchedEntity.xOld + (balloon.latchedEntity.getX() - balloon.latchedEntity.xOld) * partialTick
                       - (balloon.xOld + (balloon.getX() - balloon.xOld) * partialTick);
            double y = balloon.latchedEntity.yOld + (balloon.latchedEntity.getY() - balloon.latchedEntity.yOld) * partialTick
                       - (balloon.yOld + (balloon.getY() - balloon.yOld) * partialTick)
                       + balloon.getAddedHeight();
            double z = balloon.latchedEntity.zOld + (balloon.latchedEntity.getZ() - balloon.latchedEntity.zOld) * partialTick
                       - (balloon.zOld + (balloon.getZ() - balloon.zOld) * partialTick);
            matrix.translate(x, y, z);
        }

        JSONModelData model = balloon.isLatched() ? AdditionsModelCache.INSTANCE.BALLOON : AdditionsModelCache.INSTANCE.BALLOON_FREE;

        List<BakedQuad> quads = model.getBakedModel().getQuads(null, null, balloon.level.random);
        RenderType renderType = RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS);
        VertexConsumer builder = renderer.getBuffer(renderType);
        PoseStack.Pose last = matrix.last();
        for (BakedQuad quad : quads) {
            float[] color = {1, 1, 1, 1};
            if (quad.getTintIndex() == 0) {
                EnumColor balloonColor = balloon.getColor();
                color[0] = balloonColor.getColor(0);
                color[1] = balloonColor.getColor(1);
                color[2] = balloonColor.getColor(2);
            }
            builder.putBulkData(last, quad, color[0], color[1], color[2], color[3], light, OverlayTexture.NO_OVERLAY);
        }
        ((MultiBufferSource.BufferSource) renderer).endBatch(renderType);
        matrix.popPose();
        super.render(balloon, entityYaw, partialTick, matrix, renderer, light);
    }
}