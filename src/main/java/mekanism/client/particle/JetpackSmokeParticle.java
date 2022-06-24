package mekanism.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SmokeParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.NotNull;

public class JetpackSmokeParticle extends SmokeParticle {

    private JetpackSmokeParticle(ClientLevel world, double posX, double posY, double posZ, double velX, double velY, double velZ, SpriteSet sprite) {
        super(world, posX, posY, posZ, velX, velY, velZ, 1.0F, sprite);
    }

    @Override
    public int getLightColor(float partialTick) {
        return 190 + (int) (20F * (1.0F - Minecraft.getInstance().options.gamma().get().floatValue()));
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
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new JetpackSmokeParticle(world, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
        }
    }
}