package mekanism.client.particle;

import mekanism.common.particle.LaserParticleData;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class LaserParticle extends Particle {

    private final TextureAtlasSprite sprite;
    private final Direction direction;
    private final float energyScale;
    private final float halfLength;

    private LaserParticle(ClientLevel level, double startX, double startY, double startZ, Direction direction, float distance, float energyScale,
          TextureAtlasSprite sprite) {
        this.direction = direction;
        this.energyScale = energyScale;
        this.halfLength = distance / 2;
        this.sprite = sprite;
        Vec3 distanceAdjustment = direction.getUnitVec3().scale(distance / 2);
        super(level, startX + distanceAdjustment.x(), startY + distanceAdjustment.y(), startZ + distanceAdjustment.z());
        setLifetime(5);
    }

    public Direction direction() {
        return direction;
    }

    public float halfLength() {
        return halfLength;
    }

    public float energyScale() {
        return energyScale;
    }

    public TextureAtlasSprite sprite() {
        return sprite;
    }

    @Override
    public ParticleRenderType getGroup() {
        return LaserParticleGroup.TYPE;
    }

    @Override
    protected void setSize(float particleWidth, float particleHeight) {
        //Ignore calls to setSize
    }

    @Override
    public Particle setPower(float power) {
        //Ignore calls to setPower
        return this;
    }

    @Override
    public void setParticleSpeed(double xd, double yd, double zd) {
        //Ignore calls to setParticleSpeed
    }

    @Override
    protected int getLightCoords(float a) {
        return LightCoordsUtil.FULL_BRIGHT;
    }

    @Override
    public void setPos(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        float halfDiameter = energyScale / 2;
        setBoundingBox(switch (direction) {
            case DOWN, UP -> new AABB(x - halfDiameter, y - halfLength, z - halfDiameter, x + halfDiameter, y + halfLength, z + halfDiameter);
            case NORTH, SOUTH -> new AABB(x - halfDiameter, y - halfDiameter, z - halfLength, x + halfDiameter, y + halfDiameter, z + halfLength);
            case WEST, EAST -> new AABB(x - halfLength, y - halfDiameter, z - halfDiameter, x + halfLength, y + halfDiameter, z + halfDiameter);
        });
    }

    @Override
    public void move(double xa, double ya, double za) {
        //Laser particles don't move, just no-op behavior
    }

    public static class Factory implements ParticleProvider<LaserParticleData> {

        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public LaserParticle createParticle(LaserParticleData data, ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            return new LaserParticle(world, x, y, z, data.direction(), data.distance(), data.energyScale(), spriteSet.get(random));
        }
    }
}