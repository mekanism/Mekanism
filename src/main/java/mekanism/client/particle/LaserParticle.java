package mekanism.client.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class LaserParticle extends TextureSheetParticle {

    private static final ParticleRenderType LASER_TYPE = new ParticleRenderType() {
        @Override
        public void begin(@Nonnull BufferBuilder buffer, @Nonnull TextureManager manager) {
            RenderSystem.disableCull();
            ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT.begin(buffer, manager);
        }

        @Override
        public void end(@Nonnull Tesselator tesselator) {
            ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT.end(tesselator);
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
        //Note: Vanilla discards pieces from particles that are under the alpha of 0.1, due to floating point differences
        // of float and double if we set this to 0.1F, then it ends up getting discarded, so we just set this to 0.11F
        alpha = 0.11F;
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
        Vector3f[] resultVector = {
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
        setBoundingBox(switch (direction) {
            case DOWN, UP -> new AABB(x - halfDiameter, y - halfLength, z - halfDiameter, x + halfDiameter, y + halfLength, z + halfDiameter);
            case NORTH, SOUTH -> new AABB(x - halfDiameter, y - halfDiameter, z - halfLength, x + halfDiameter, y + halfDiameter, z + halfLength);
            case WEST, EAST -> new AABB(x - halfLength, y - halfDiameter, z - halfDiameter, x + halfLength, y + halfDiameter, z + halfDiameter);
        });
    }

    public static class Factory implements ParticleProvider<LaserParticleData> {

        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public LaserParticle createParticle(LaserParticleData data, @Nonnull ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            Pos3D start = new Pos3D(x, y, z);
            Pos3D end = start.translate(data.direction(), data.distance());
            LaserParticle particleLaser = new LaserParticle(world, start, end, data.direction(), data.energyScale());
            particleLaser.pickSprite(this.spriteSet);
            return particleLaser;
        }
    }
}