package mekanism.client.particle;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BubbleParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class ScubaBubbleParticle extends BubbleParticle {

    private ScubaBubbleParticle(ClientLevel world, double posX, double posY, double posZ, double velX, double velY, double velZ, TextureAtlasSprite sprite) {
        super(world, posX, posY, posZ, velX, velY, velZ, sprite);
        lifetime *= 2;
    }

    @Override
    public void tick() {
        super.tick();
        age++;
    }

    @Override
    public void extract(QuadParticleRenderState particleTypeRenderState, Camera camera, float partialTickTime) {
        if (age > 0) {
            super.extract(particleTypeRenderState, camera, partialTickTime);
        }
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            return new ScubaBubbleParticle(world, x, y, z, xSpeed, ySpeed, zSpeed, spriteSet.get(random));
        }
    }
}