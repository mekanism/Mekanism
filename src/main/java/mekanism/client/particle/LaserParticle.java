package mekanism.client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import mekanism.api.Pos3D;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.particle.LaserParticleData;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

public class LaserParticle extends SpriteTexturedParticle {

    private final Direction direction;
    private final double length;

    public LaserParticle(World world, Pos3D start, Pos3D end, Direction dir, double energy) {
        super(world, (start.x + end.x) / 2D, (start.y + end.y) / 2D, (start.z + end.z) / 2D);
        maxAge = 5;
        particleRed = 1;
        particleGreen = 0;
        particleBlue = 0;
        //TODO: Figure out why alpha no longer works (Note: If it is set to a low value like 0.1F it just makes the laser invisible)
        //particleAlpha = 0.1F;
        particleScale = (float) Math.min(energy / 50000, 0.6);
        length = end.distance(start);
        direction = dir;
    }

    @Override
    public void renderParticle(@Nonnull BufferBuilder buffer, @Nonnull ActiveRenderInfo renderInfo, float partialTicks, float rotationX, float rotationZ, float rotationYZ,
                               float rotationXY, float rotationXZ) {
        Tessellator tessellator = Tessellator.getInstance();
        MekanismRenderer.RenderState renderState = MekanismRenderer.pauseRenderer(tessellator);

        GlStateManager.pushMatrix();
        float newX = (float) (prevPosX + (posX - prevPosX) * (double) partialTicks - interpPosX);
        float newY = (float) (prevPosY + (posY - prevPosY) * (double) partialTicks - interpPosY);
        float newZ = (float) (prevPosZ + (posZ - prevPosZ) * (double) partialTicks - interpPosZ);

        GlStateManager.translatef(newX, newY, newZ);

        switch (direction) {
            case WEST:
            case EAST:
                GlStateManager.rotatef(90, 0, 0, 1);
                break;
            case NORTH:
            case SOUTH:
                GlStateManager.rotatef(90, 1, 0, 0);
                break;
            default:
                break;
        }
        drawLaser(buffer, tessellator);
        GlStateManager.popMatrix();
        MekanismRenderer.resumeRenderer(tessellator, renderState);
    }

    private void drawLaser(BufferBuilder buffer, Tessellator tessellator) {
        float uMin = getMinU();
        float uMax = getMaxU();
        float vMin = getMinV();
        float vMax = getMaxV();
        GlStateManager.disableCull();
        MekanismRenderer.GlowInfo glowInfo = MekanismRenderer.enableGlow();
        drawComponent(buffer, tessellator, uMin, uMax, vMin, vMax, 45);
        drawComponent(buffer, tessellator, uMin, uMax, vMin, vMax, 90);
        MekanismRenderer.disableGlow(glowInfo);
        GlStateManager.enableCull();
    }

    private void drawComponent(BufferBuilder buffer, Tessellator tessellator, float uMin, float uMax, float vMin, float vMax, float angle) {
        GlStateManager.rotatef(angle, 0, 1, 0);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
        buffer.pos(-particleScale, -length / 2, 0).tex(uMin, vMin).color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(240, 240).endVertex();
        buffer.pos(-particleScale, length / 2, 0).tex(uMin, vMax).color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(240, 240).endVertex();
        buffer.pos(particleScale, length / 2, 0).tex(uMax, vMax).color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(240, 240).endVertex();
        buffer.pos(particleScale, -length / 2, 0).tex(uMax, vMin).color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(240, 240).endVertex();
        tessellator.draw();
    }

    @Nonnull
    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.CUSTOM;
    }

    public static class Factory implements IParticleFactory<LaserParticleData> {

        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public LaserParticle makeParticle(LaserParticleData data, @Nonnull World world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            Pos3D start = new Pos3D(x, y, z);
            Pos3D end = start.translate(data.direction, data.distance);
            LaserParticle particleLaser = new LaserParticle(world, start, end, data.direction, data.energy);
            particleLaser.selectSpriteRandomly(this.spriteSet);
            return particleLaser;
        }
    }
}