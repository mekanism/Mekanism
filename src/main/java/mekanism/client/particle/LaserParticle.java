package mekanism.client.particle;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import mekanism.common.lib.math.Pos3D;
import mekanism.common.particle.LaserParticleData;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import org.lwjgl.opengl.GL11;

public class LaserParticle extends SpriteTexturedParticle {

    private static final IParticleRenderType LASER_TYPE = new IParticleRenderType() {
        @Override
        public void beginRender(BufferBuilder buffer, TextureManager manager) {
            //Copy of PARTICLE_SHEET_TRANSLUCENT but with cull disabled
            RenderSystem.depthMask(true);
            manager.bindTexture(AtlasTexture.LOCATION_PARTICLES_TEXTURE);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.alphaFunc(GL11.GL_GREATER, 0.003921569F);
            RenderSystem.disableCull();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
        }

        @Override
        public void finishRender(Tessellator tesselator) {
            tesselator.draw();
        }

        public String toString() {
            return "MEK_LASER_PARTICLE_TYPE";
        }
    };

    private static final float RADIAN_45 = (float) Math.toRadians(45);
    private static final float RADIAN_90 = (float) Math.toRadians(90);

    private final Direction direction;
    private final float halfLength;

    private LaserParticle(ClientWorld world, Pos3D start, Pos3D end, Direction dir, float energyScale) {
        super(world, (start.x + end.x) / 2D, (start.y + end.y) / 2D, (start.z + end.z) / 2D);
        maxAge = 5;
        particleRed = 1;
        particleGreen = 0;
        particleBlue = 0;
        particleAlpha = 0.1F;
        particleScale = energyScale;
        halfLength = (float) (end.distance(start) / 2);
        direction = dir;
        updateBoundingBox();
    }

    @Override
    public void renderParticle(@Nonnull IVertexBuilder vertexBuilder, ActiveRenderInfo renderInfo, float partialTicks) {
        Vector3d view = renderInfo.getProjectedView();
        float newX = (float) (MathHelper.lerp(partialTicks, prevPosX, posX) - view.getX());
        float newY = (float) (MathHelper.lerp(partialTicks, prevPosY, posY) - view.getY());
        float newZ = (float) (MathHelper.lerp(partialTicks, prevPosZ, posZ) - view.getZ());
        float uMin = getMinU();
        float uMax = getMaxU();
        float vMin = getMinV();
        float vMax = getMaxV();
        Quaternion quaternion = direction.getRotation();
        quaternion.multiply(Vector3f.YP.rotation(RADIAN_45));
        drawComponent(vertexBuilder, getResultVector(quaternion, newX, newY, newZ), uMin, uMax, vMin, vMax);
        Quaternion quaternion2 = new Quaternion(quaternion);
        quaternion2.multiply(Vector3f.YP.rotation(RADIAN_90));
        drawComponent(vertexBuilder, getResultVector(quaternion2, newX, newY, newZ), uMin, uMax, vMin, vMax);
    }

    private Vector3f[] getResultVector(Quaternion quaternion, float newX, float newY, float newZ) {
        Vector3f[] resultVector = new Vector3f[]{
              new Vector3f(-particleScale, -halfLength, 0),
              new Vector3f(-particleScale, halfLength, 0),
              new Vector3f(particleScale, halfLength, 0),
              new Vector3f(particleScale, -halfLength, 0)
        };
        for (Vector3f vec : resultVector) {
            vec.transform(quaternion);
            vec.add(newX, newY, newZ);
        }
        return resultVector;
    }

    private void drawComponent(IVertexBuilder vertexBuilder, Vector3f[] resultVector, float uMin, float uMax, float vMin, float vMax) {
        vertexBuilder.pos(resultVector[0].getX(), resultVector[0].getY(), resultVector[0].getZ()).tex(uMax, vMax).color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(240, 240).endVertex();
        vertexBuilder.pos(resultVector[1].getX(), resultVector[1].getY(), resultVector[1].getZ()).tex(uMax, vMin).color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(240, 240).endVertex();
        vertexBuilder.pos(resultVector[2].getX(), resultVector[2].getY(), resultVector[2].getZ()).tex(uMin, vMin).color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(240, 240).endVertex();
        vertexBuilder.pos(resultVector[3].getX(), resultVector[3].getY(), resultVector[3].getZ()).tex(uMin, vMax).color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(240, 240).endVertex();
    }

    @Nonnull
    @Override
    public IParticleRenderType getRenderType() {
        return LASER_TYPE;
    }

    @Override
    protected void setSize(float particleWidth, float particleHeight) {
        if (particleWidth != this.width || particleHeight != this.height) {
            //Note: We don't actually have width or height affect our bounding box
            //TODO: Eventually we maybe should have it affect it at least to an extent?
            this.width = particleWidth;
            this.height = particleHeight;
        }
    }

    @Override
    public void setPosition(double x, double y, double z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        if (direction != null) {
            //Direction can be null when the super constructor is calling this method
            updateBoundingBox();
        }
    }

    private void updateBoundingBox() {
        float halfDiameter = particleScale / 2;
        switch (direction) {
            case DOWN:
            case UP:
                setBoundingBox(new AxisAlignedBB(posX - halfDiameter, posY - halfLength, posZ - halfDiameter, posX + halfDiameter, posY + halfLength, posZ + halfDiameter));
                break;
            case NORTH:
            case SOUTH:
                setBoundingBox(new AxisAlignedBB(posX - halfDiameter, posY - halfDiameter, posZ - halfLength, posX + halfDiameter, posY + halfDiameter, posZ + halfLength));
                break;
            case WEST:
            case EAST:
                setBoundingBox(new AxisAlignedBB(posX - halfLength, posY - halfDiameter, posZ - halfDiameter, posX + halfLength, posY + halfDiameter, posZ + halfDiameter));
                break;
        }
    }

    public static class Factory implements IParticleFactory<LaserParticleData> {

        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public LaserParticle makeParticle(LaserParticleData data, @Nonnull ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            Pos3D start = new Pos3D(x, y, z);
            Pos3D end = start.translate(data.direction, data.distance);
            LaserParticle particleLaser = new LaserParticle(world, start, end, data.direction, data.energyScale);
            particleLaser.selectSpriteRandomly(this.spriteSet);
            return particleLaser;
        }
    }
}