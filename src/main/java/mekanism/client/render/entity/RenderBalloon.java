package mekanism.client.render.entity;

import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.client.model.ModelBalloon;
import mekanism.client.render.MekanismRenderHelper;
import mekanism.common.entity.EntityBalloon;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBalloon extends Render<EntityBalloon> {

    public ModelBalloon model = new ModelBalloon();
    private Minecraft mc = Minecraft.getMinecraft();

    public RenderBalloon(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    protected ResourceLocation getEntityTexture(@Nonnull EntityBalloon entity) {
        return MekanismUtils.getResource(ResourceType.RENDER, "Balloon.png");
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
        MekanismRenderHelper renderHelper = new MekanismRenderHelper(true).translate(x, y, z);
        GlStateManager.rotate(180, 1, 0, 0);
        mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "Balloon.png"));
        model.render(0.0625F, color);
        renderHelper.cleanup();
    }
}