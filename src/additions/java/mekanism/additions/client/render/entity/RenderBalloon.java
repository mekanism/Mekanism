package mekanism.additions.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.client.model.ModelBalloon;
import mekanism.client.render.MekanismRenderer;
import mekanism.additions.common.entity.EntityBalloon;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderBalloon extends EntityRenderer<EntityBalloon> {

    public ModelBalloon model = new ModelBalloon();

    public RenderBalloon(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    protected ResourceLocation getEntityTexture(@Nonnull EntityBalloon entity) {
        return MekanismUtils.getResource(ResourceType.RENDER, "balloon.png");
    }

    @Override
    public void doRender(@Nonnull EntityBalloon balloon, double x, double y, double z, float f, float partialTick) {
        double renderPosX = x - (balloon.lastTickPosX + (balloon.posX - balloon.lastTickPosX) * partialTick);
        double renderPosY = y - (balloon.lastTickPosY + (balloon.posY - balloon.lastTickPosY) * partialTick);
        double renderPosZ = z - (balloon.lastTickPosZ + (balloon.posZ - balloon.lastTickPosZ) * partialTick);

        if (balloon.isLatchedToEntity()) {
            x = balloon.latchedEntity.lastTickPosX + (balloon.latchedEntity.posX - balloon.latchedEntity.lastTickPosX) * partialTick;
            y = balloon.latchedEntity.lastTickPosY + (balloon.latchedEntity.posY - balloon.latchedEntity.lastTickPosY) * partialTick;
            z = balloon.latchedEntity.lastTickPosZ + (balloon.latchedEntity.posZ - balloon.latchedEntity.lastTickPosZ) * partialTick;

            x += renderPosX;
            y += renderPosY;
            z += renderPosZ;

            y += balloon.getAddedHeight();
        }

        render(balloon.color, x, y, z);
    }

    public void render(EnumColor color, double x, double y, double z) {
        GlStateManager.pushMatrix();
        GlStateManager.translatef((float) x, (float) y, (float) z);
        GlStateManager.rotatef(180, 1, 0, 0);
        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "balloon.png"));
        model.render(0.0625F, color);
        GlStateManager.popMatrix();
    }
}