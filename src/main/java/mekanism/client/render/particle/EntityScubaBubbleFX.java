package mekanism.client.render.particle;

import net.minecraft.client.particle.ParticleBubble;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EntityScubaBubbleFX extends ParticleBubble {

    public EntityScubaBubbleFX(World world, double posX, double posY, double posZ, double velX, double velY, double velZ) {
        super(world, posX, posY, posZ, velX, velY, velZ);
        particleScale = (rand.nextFloat() * 0.2F) + 0.3F;
        particleMaxAge *= 2;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        particleAge++;
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        if (particleAge > 0) {
            super.renderParticle(buffer, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
        }
    }
}