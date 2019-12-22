//TODO: Fix this, probably just needs an AT to expose superclass constructor
/*package mekanism.client.render.particle;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.particle.BubbleParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.world.World;

public class EntityScubaBubbleFX extends BubbleParticle {

    public EntityScubaBubbleFX(World world, double posX, double posY, double posZ, double velX, double velY, double velZ) {
        super(world, posX, posY, posZ, velX, velY, velZ);
        particleScale = (rand.nextFloat() * 0.2F) + 0.3F;
        maxAge *= 2;
    }

    @Override
    public void tick() {
        super.tick();
        age++;
    }

    @Override
    public void func_225606_a_(IVertexBuilder vertexBuilder, ActiveRenderInfo renderInfo, float partialTicks) {
        if (age > 0) {
            super.func_225606_a_(vertexBuilder, renderInfo, partialTicks);
        }
    }
}*/