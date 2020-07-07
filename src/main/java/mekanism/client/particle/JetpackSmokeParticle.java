package mekanism.client.particle;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SmokeParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;

public class JetpackSmokeParticle extends SmokeParticle {

    private JetpackSmokeParticle(ClientWorld world, double posX, double posY, double posZ, double velX, double velY, double velZ, IAnimatedSprite sprite) {
        super(world, posX, posY, posZ, velX, velY, velZ, 1.0F, sprite);
    }

    @Override
    public int getBrightnessForRender(float partialTick) {
        return 190 + (int) (20F * (1.0F - Minecraft.getInstance().gameSettings.gamma));
    }

    @Override
    public void renderParticle(@Nonnull IVertexBuilder vertexBuilder, @Nonnull ActiveRenderInfo renderInfo, float partialTicks) {
        if (age > 0) {
            super.renderParticle(vertexBuilder, renderInfo, partialTicks);
        }
    }

    public static class Factory implements IParticleFactory<BasicParticleType> {

        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle makeParticle(@Nonnull BasicParticleType type, @Nonnull ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new JetpackSmokeParticle(world, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
        }
    }
}