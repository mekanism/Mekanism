package mekanism.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import javax.annotation.Nonnull;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BubbleParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

public class ScubaBubbleParticle extends BubbleParticle {

    private ScubaBubbleParticle(ClientLevel world, double posX, double posY, double posZ, double velX, double velY, double velZ) {
        super(world, posX, posY, posZ, velX, velY, velZ);
        lifetime *= 2;
    }

    @Override
    public void tick() {
        super.tick();
        age++;
    }

    @Override
    public void render(@Nonnull VertexConsumer vertexBuilder, @Nonnull Camera renderInfo, float partialTicks) {
        if (age > 0) {
            super.render(vertexBuilder, renderInfo, partialTicks);
        }
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(@Nonnull SimpleParticleType type, @Nonnull ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            ScubaBubbleParticle particle = new ScubaBubbleParticle(world, x, y, z, xSpeed, ySpeed, zSpeed);
            particle.pickSprite(this.spriteSet);
            return particle;
        }
    }
}