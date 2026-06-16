package mekanism.client.particle;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;

public class JetpackFlameParticle extends FlameParticle {

    private JetpackFlameParticle(ClientLevel world, double posX, double posY, double posZ, double velX, double velY, double velZ, TextureAtlasSprite sprite) {
        super(world, posX, posY, posZ, velX, velY, velZ, sprite);
    }

    @Override
    public int getLightCoords(float partialTick) {
        return LightCoordsUtil.withBlock(super.getLightCoords(partialTick), 190 + (int) (20F * (1.0F - Minecraft.getInstance().options.gamma().get().floatValue())));
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
            //TODO - 26.2: Do we need to scale the particle like flame particle's provider does?
            return new JetpackFlameParticle(world, x, y, z, xSpeed, ySpeed, zSpeed, spriteSet.get(random));
        }
    }
}