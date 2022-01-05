package mekanism.client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import javax.annotation.Nonnull;
import mekanism.common.lib.math.Pos3D;
import mekanism.common.particle.LaserParticleData;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class LaserParticle extends TextureSheetParticle {

    private static final ParticleRenderType LASER_TYPE = new ParticleRenderType() {
        @Override
        public void begin(@Nonnull BufferBuilder buffer, @Nonnull TextureManager manager) {
            //Copy of PARTICLE_SHEET_TRANSLUCENT but with cull disabled
            RenderSystem.depthMask(true);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.disableCull();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
        }

        public String toString() {
            return "MEK_LASER_PARTICLE_TYPE";
        }
    };

    private static final float RADIAN_45 = (float) Math.toRadians(45);
    private static final float RADIAN_90 = (float) Math.toRadians(90);

    private final Direction direction;
    private final float halfLength;

    private LaserParticle(ClientLevel world, Vec3 start, Vec3 end, Direction dir, float energyScale) {
        super(world, (start.x + end.x) / 2D, (start.y + end.y) / 2D, (start.z + end.z) / 2D);
        lifetime = 5;
        rCol = 1;
        gCol = 0;
        bCol = 0;
        alpha = 0.1F;
        quadSize = energyScale;
        halfLength = (float) (end.distanceTo(start) / 2);
        direction = dir;
        updateBoundingBox();
    }

    @Override
    public void render(@Nonnull VertexConsumer vertexBuilder, Camera renderInfo, float partialTicks) {
        Vec3 view = renderInfo.getPosition();
        float newX = (float) (Mth.lerp(partialTicks, xo, x) - view.x());
        float newY = (float) (Mth.lerp(partialTicks, yo, y) - view.y());
        float newZ = (float) (Mth.lerp(partialTicks, zo, z) - view.z());
        float uMin = getU0();
        float uMax = getU1();
        float vMin = getV0();
        float vMax = getV1();
        Quaternion quaternion = direction.getRotation();
        quaternion.mul(Vector3f.YP.rotation(RADIAN_45));
        drawComponent(vertexBuilder, getResultVector(quaternion, newX, newY, newZ), uMin, uMax, vMin, vMax);
        Quaternion quaternion2 = new Quaternion(quaternion);
        quaternion2.mul(Vector3f.YP.rotation(RADIAN_90));
        drawComponent(vertexBuilder, getResultVector(quaternion2, newX, newY, newZ), uMin, uMax, vMin, vMax);
    }

    private Vector3f[] getResultVector(Quaternion quaternion, float newX, float newY, float newZ) {
        Vector3f[] resultVector = new Vector3f[]{
              new Vector3f(-quadSize, -halfLength, 0),
              new Vector3f(-quadSize, halfLength, 0),
              new Vector3f(quadSize, halfLength, 0),
              new Vector3f(quadSize, -halfLength, 0)
        };
        for (Vector3f vec : resultVector) {
            vec.transform(quaternion);
            vec.add(newX, newY, newZ);
        }
        return resultVector;
    }

    private void drawComponent(VertexConsumer vertexBuilder, Vector3f[] resultVector, float uMin, float uMax, float vMin, float vMax) {
        vertexBuilder.vertex(resultVector[0].x(), resultVector[0].y(), resultVector[0].z()).uv(uMax, vMax).color(rCol, gCol, bCol, alpha).uv2(240, 240).endVertex();
        vertexBuilder.vertex(resultVector[1].x(), resultVector[1].y(), resultVector[1].z()).uv(uMax, vMin).color(rCol, gCol, bCol, alpha).uv2(240, 240).endVertex();
        vertexBuilder.vertex(resultVector[2].x(), resultVector[2].y(), resultVector[2].z()).uv(uMin, vMin).color(rCol, gCol, bCol, alpha).uv2(240, 240).endVertex();
        vertexBuilder.vertex(resultVector[3].x(), resultVector[3].y(), resultVector[3].z()).uv(uMin, vMax).color(rCol, gCol, bCol, alpha).uv2(240, 240).endVertex();
    }

    @Nonnull
    @Override
    public ParticleRenderType getRenderType() {
        return LASER_TYPE;
    }

    @Override
    protected void setSize(float particleWidth, float particleHeight) {
        if (particleWidth != this.bbWidth || particleHeight != this.bbHeight) {
            //Note: We don't actually have width or height affect our bounding box
            //TODO: Eventually we maybe should have it affect it at least to an extent?
            this.bbWidth = particleWidth;
            this.bbHeight = particleHeight;
        }
    }

    @Override
    public void setPos(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        if (direction != null) {
            //Direction can be null when the super constructor is calling this method
            updateBoundingBox();
        }
    }

    private void updateBoundingBox() {
        float halfDiameter = quadSize / 2;
        switch (direction) {
            case DOWN, UP -> setBoundingBox(new AABB(x - halfDiameter, y - halfLength, z - halfDiameter, x + halfDiameter, y + halfLength, z + halfDiameter));
            case NORTH, SOUTH -> setBoundingBox(new AABB(x - halfDiameter, y - halfDiameter, z - halfLength, x + halfDiameter, y + halfDiameter, z + halfLength));
            case WEST, EAST -> setBoundingBox(new AABB(x - halfLength, y - halfDiameter, z - halfDiameter, x + halfLength, y + halfDiameter, z + halfDiameter));
        }
    }

    public static class Factory implements ParticleProvider<LaserParticleData> {

        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public LaserParticle createParticle(LaserParticleData data, @Nonnull ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            Pos3D start = new Pos3D(x, y, z);
            Pos3D end = start.translate(data.direction, data.distance);
            LaserParticle particleLaser = new LaserParticle(world, start, end, data.direction, data.energyScale);
            particleLaser.pickSprite(this.spriteSet);
            return particleLaser;
        }
    }
}