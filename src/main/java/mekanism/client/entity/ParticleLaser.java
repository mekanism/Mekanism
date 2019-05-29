package mekanism.client.entity;

import mekanism.api.Pos3D;
import mekanism.client.render.MekanismRenderHelper;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ParticleLaser extends Particle {

    private double length;
    private EnumFacing direction;

    public ParticleLaser(World world, Pos3D start, Pos3D end, EnumFacing dir, double energy) {
        super(world, (start.x + end.x) / 2D, (start.y + end.y) / 2D, (start.z + end.z) / 2D);
        particleMaxAge = 5;
        particleRed = 1;
        particleGreen = 0;
        particleBlue = 0;
        particleAlpha = 0.1F;
        particleScale = (float) Math.min(energy / 50000, 0.6);
        length = end.distance(start);
        direction = dir;
        particleTexture = MekanismRenderer.laserIcon;
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        Tessellator tessellator = Tessellator.getInstance();
        tessellator.draw();

        MekanismRenderHelper renderHelper = new MekanismRenderHelper(true).disableCull().enableGlow();
        //TODO: Double check if GL11.GL_POLYGON_BIT is needed. It didn't seem to be used and removing it doesn't seem to have broken anything

        float newX = (float) (prevPosX + (posX - prevPosX) * (double) partialTicks - interpPosX);
        float newY = (float) (prevPosY + (posY - prevPosY) * (double) partialTicks - interpPosY);
        float newZ = (float) (prevPosZ + (posZ - prevPosZ) * (double) partialTicks - interpPosZ);

        renderHelper.translate(newX, newY, newZ);

        switch (direction) {
            case UP:
            case DOWN:
            default:
                break;
            case WEST:
            case EAST:
                renderHelper.rotateZ(90, 1);
                break;
            case NORTH:
            case SOUTH:
                renderHelper.rotateX(90, 1);
                break;
        }

        float uMin = particleTexture.getInterpolatedU(0);
        float uMax = particleTexture.getInterpolatedU(16);
        float vMin = particleTexture.getInterpolatedV(0);
        float vMax = particleTexture.getInterpolatedV(16);

        renderHelper.rotateY(45, 1);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
        buffer.pos(-particleScale, -length / 2, 0).tex(uMin, vMin)
              .color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(240, 240).endVertex();
        buffer.pos(-particleScale, length / 2, 0).tex(uMin, vMax)
              .color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(240, 240).endVertex();
        buffer.pos(particleScale, length / 2, 0).tex(uMax, vMax)
              .color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(240, 240).endVertex();
        buffer.pos(particleScale, -length / 2, 0).tex(uMax, vMin)
              .color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(240, 240).endVertex();
        tessellator.draw();

        renderHelper.rotateY(90, 1);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
        buffer.pos(-particleScale, -length / 2, 0).tex(uMin, vMin)
              .color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(240, 240).endVertex();
        buffer.pos(-particleScale, length / 2, 0).tex(uMin, vMax)
              .color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(240, 240).endVertex();
        buffer.pos(particleScale, length / 2, 0).tex(uMax, vMax)
              .color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(240, 240).endVertex();
        buffer.pos(particleScale, -length / 2, 0).tex(uMax, vMin)
              .color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(240, 240).endVertex();
        tessellator.draw();

        renderHelper.cleanup();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
    }

    @Override
    public int getFXLayer() {
        return 1;
    }
}