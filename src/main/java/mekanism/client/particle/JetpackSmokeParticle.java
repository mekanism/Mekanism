package mekanism.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SmokeParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class JetpackSmokeParticle extends SmokeParticle {

    private JetpackSmokeParticle(World world, double posX, double posY, double posZ, double velX, double velY, double velZ, IAnimatedSprite sprite) {
        super(world, posX, posY, posZ, velX, velY, velZ, 1.0F, sprite);
    }

    @Override
    public int getBrightnessForRender(float partialTick) {
        return 190 + (int) (20F * (1.0F - Minecraft.getInstance().gameSettings.gamma));
    }

    @Override
    public void renderParticle(@Nonnull BufferBuilder buffer, @Nonnull ActiveRenderInfo renderInfo, float partialTicks, float rotationX, float rotationZ, float rotationYZ,
                               float rotationXY, float rotationXZ) {
        if (age > 0) {
            super.renderParticle(buffer, renderInfo, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
        }
    }

    public static class Factory implements IParticleFactory<BasicParticleType> {

        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle makeParticle(@Nonnull BasicParticleType type, @Nonnull World world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new JetpackSmokeParticle(world, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
        }
    }
}