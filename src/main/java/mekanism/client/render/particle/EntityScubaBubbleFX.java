package mekanism.client.render.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityBubbleFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.World;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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
	public void renderParticle(Tessellator p_70539_1_, float p_70539_2_, float p_70539_3_, float p_70539_4_, float p_70539_5_, float p_70539_6_, float p_70539_7_) 
	{
		if(particleAge > 0)
		{
			particleAlpha = Math.min(1, (particleAge+p_70539_2_)/20F);
			super.renderParticle(p_70539_1_, p_70539_2_, p_70539_3_, p_70539_4_, p_70539_5_, p_70539_6_, p_70539_7_);
		}
	}
}
