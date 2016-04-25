package mekanism.client.render.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityBubbleFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntityScubaBubbleFX extends EntityBubbleFX
{
	private static Minecraft mc = FMLClientHandler.instance().getClient();
	
	public EntityScubaBubbleFX(World world, double posX, double posY, double posZ, double velX, double velY, double velZ) 
	{
		super(world, posX, posY, posZ, velX, velY, velZ);
		
		particleScale = (rand.nextFloat()*0.2F)+0.5F;
		particleMaxAge *= 2;
	}
	
	@Override
	public void onUpdate()
    {
		super.onUpdate();
		
		particleAge++;
    }

	@Override
	public void renderParticle(WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, float p_180434_4_, float p_180434_5_, float p_180434_6_, float p_180434_7_, float p_180434_8_)
	{
		if(particleAge > 0)
		{
			super.renderParticle(worldRendererIn, entityIn, partialTicks, p_180434_4_, p_180434_5_, p_180434_6_, p_180434_7_, p_180434_8_);
		}
	}
}
