package mekanism.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BubbleParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;

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
    public void render(@NotNull VertexConsumer vertexBuilder, @NotNull Camera renderInfo, float partialTicks) {
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
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, @NotNull RandomSource random) {
            return new ScubaBubbleParticle(world, x, y, z, xSpeed, ySpeed, zSpeed, spriteSet.get(random));
        }
    }
}