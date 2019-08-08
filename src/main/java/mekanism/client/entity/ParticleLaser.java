package mekanism.client.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import javax.annotation.Nonnull;
import mekanism.api.Pos3D;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.GlowInfo;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

@OnlyIn(Dist.CLIENT)
public class ParticleLaser extends SpriteTexturedParticle {

    private double length;
    private Direction direction;

    public ParticleLaser(World world, Pos3D start, Pos3D end, Direction dir, double energy) {
        super(world, (start.x + end.x) / 2D, (start.y + end.y) / 2D, (start.z + end.z) / 2D);
        maxAge = 5;
        particleRed = 1;
        particleGreen = 0;
        particleBlue = 0;
        particleAlpha = 0.1F;
        particleScale = (float) Math.min(energy / 50000, 0.6);
        length = end.distance(start);
        direction = dir;
        sprite = MekanismRenderer.laserIcon;
    }

    @Override
    public void renderParticle(@Nonnull BufferBuilder buffer, @Nonnull ActiveRenderInfo renderInfo, float partialTicks, float rotationX, float rotationZ, float rotationYZ,
          float rotationXY, float rotationXZ) {
        Tessellator tessellator = Tessellator.getInstance();
        tessellator.draw();

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
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
    }

    private void drawLaser(BufferBuilder buffer, Tessellator tessellator) {
        float uMin = sprite.getInterpolatedU(0);
        float uMax = sprite.getInterpolatedU(16);
        float vMin = sprite.getInterpolatedV(0);
        float vMax = sprite.getInterpolatedV(16);
        GlStateManager.disableCull();
        GlowInfo glowInfo = MekanismRenderer.enableGlow();
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
        //TODO: Check this, the FX layer returned 1
        return IParticleRenderType.CUSTOM;
    }
}