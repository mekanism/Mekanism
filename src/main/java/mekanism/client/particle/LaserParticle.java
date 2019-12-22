package mekanism.client.particle;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import mekanism.api.Pos3D;
import mekanism.common.particle.LaserParticleData;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class LaserParticle extends SpriteTexturedParticle {

    private static final float RADIAN_45 = (float) Math.toRadians(45);
    private static final float RADIAN_90 = (float) Math.toRadians(90);

    private final Direction direction;
    private final float halfLength;

    private LaserParticle(World world, Pos3D start, Pos3D end, Direction dir, double energy) {
        super(world, (start.x + end.x) / 2D, (start.y + end.y) / 2D, (start.z + end.z) / 2D);
        maxAge = 5;
        particleRed = 1;
        particleGreen = 0;
        particleBlue = 0;
        particleAlpha = 0.1F;
        //TODO: We probably want the 50,000 to scale with the max energy of the laser?
        particleScale = (float) Math.min(energy / 50_000, 0.6);
        halfLength = (float) (end.distance(start) / 2);
        direction = dir;
    }

    @Override
    public void func_225606_a_(IVertexBuilder vertexBuilder, ActiveRenderInfo renderInfo, float partialTicks) {
        Vec3d view = renderInfo.getProjectedView();
        float newX = (float) (MathHelper.lerp(partialTicks, prevPosX, posX) - view.getX());
        float newY = (float) (MathHelper.lerp(partialTicks, prevPosY, posY) - view.getY());
        float newZ = (float) (MathHelper.lerp(partialTicks, prevPosZ, posZ) - view.getZ());
        float uMin = getMinU();
        float uMax = getMaxU();
        float vMin = getMinV();
        float vMax = getMaxV();
        //TODO: Do we need to disable cull, we previously had it disabled, was that for purposes of rendering when underwater
        // if it even showed under water before or what
        Quaternion quaternion = direction.func_229384_a_();
        quaternion.multiply(Vector3f.field_229181_d_.func_229193_c_(RADIAN_45));
        drawComponent(vertexBuilder, getResultVector(quaternion, newX, newY, newZ), uMin, uMax, vMin, vMax);
        Quaternion quaternion2 = new Quaternion(quaternion);
        quaternion2.multiply(Vector3f.field_229181_d_.func_229193_c_(RADIAN_90));
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
            vec.func_214905_a(quaternion);
            vec.add(newX, newY, newZ);
        }
        return resultVector;
    }

    private void drawComponent(IVertexBuilder vertexBuilder, Vector3f[] resultVector, float uMin, float uMax, float vMin, float vMax) {
        vertexBuilder.func_225582_a_(resultVector[0].getX(), resultVector[0].getY(), resultVector[0].getZ()).func_225583_a_(uMax, vMax).func_227885_a_(particleRed, particleGreen, particleBlue, particleAlpha).func_225587_b_(240, 240).endVertex();
        vertexBuilder.func_225582_a_(resultVector[1].getX(), resultVector[1].getY(), resultVector[1].getZ()).func_225583_a_(uMax, vMin).func_227885_a_(particleRed, particleGreen, particleBlue, particleAlpha).func_225587_b_(240, 240).endVertex();
        vertexBuilder.func_225582_a_(resultVector[2].getX(), resultVector[2].getY(), resultVector[2].getZ()).func_225583_a_(uMin, vMin).func_227885_a_(particleRed, particleGreen, particleBlue, particleAlpha).func_225587_b_(240, 240).endVertex();
        vertexBuilder.func_225582_a_(resultVector[3].getX(), resultVector[3].getY(), resultVector[3].getZ()).func_225583_a_(uMin, vMax).func_227885_a_(particleRed, particleGreen, particleBlue, particleAlpha).func_225587_b_(240, 240).endVertex();
    }

    @Nonnull
    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
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