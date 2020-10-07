package mekanism.additions.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.additions.client.model.AdditionsModelCache;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.entity.EntityBalloon;
import mekanism.api.text.EnumColor;
import mekanism.client.model.BaseModelCache.JSONModelData;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;

public class RenderBalloon extends EntityRenderer<EntityBalloon> {

    public static final ResourceLocation BALLOON_TEXTURE = MekanismAdditions.rl("textures/item/balloon.png");

    public RenderBalloon(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Nonnull
    @Override
    public ResourceLocation getEntityTexture(@Nonnull EntityBalloon entity) {
        return BALLOON_TEXTURE;
    }

    @Override
    public void render(@Nonnull EntityBalloon balloon, float entityYaw, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light) {
        matrix.push();
        matrix.translate(-0.5, -1, -0.5);

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

        JSONModelData model = balloon.isLatched() ? AdditionsModelCache.INSTANCE.BALLOON : AdditionsModelCache.INSTANCE.BALLOON_FREE;

        List<BakedQuad> quads = model.getBakedModel().getQuads(null, null, balloon.world.rand);
        RenderType renderType = RenderType.getEntityTranslucent(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        IVertexBuilder builder = renderer.getBuffer(renderType);
        MatrixStack.Entry last = matrix.getLast();
        for (BakedQuad quad : quads) {
            float[] color = new float[]{1, 1, 1, 1};
            if (quad.getTintIndex() == 0) {
                EnumColor balloonColor = balloon.getColor();
                color[0] = balloonColor.getColor(0);
                color[1] = balloonColor.getColor(1);
                color[2] = balloonColor.getColor(2);
            }
            builder.addVertexData(last, quad, color[0], color[1], color[2], color[3], light, OverlayTexture.NO_OVERLAY);
        }
        ((IRenderTypeBuffer.Impl) renderer).finish(renderType);
        matrix.pop();
        super.render(balloon, entityYaw, partialTick, matrix, renderer, light);
    }
}