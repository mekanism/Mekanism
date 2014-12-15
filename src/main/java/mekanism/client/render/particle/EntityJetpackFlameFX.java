package mekanism.client.render.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFlameFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntityJetpackFlameFX extends EntityFlameFX 
{
	private static Minecraft mc = FMLClientHandler.instance().getClient();

	public EntityJetpackFlameFX(World world, double posX, double posY, double posZ, double velX, double velY, double velZ) 
	{
		super(world, posX, posY, posZ, velX, velY, velZ);
		
		noClip = false;
	}

	@Override
	public int getBrightnessForRender(float p_70013_1_) 
	{
		return 190 + (int)(20F * (1.0F - mc.gameSettings.gammaSetting));
	}

	@Override
	public void func_180434_a(WorldRenderer p_70539_1_, Entity e, float p_70539_2_, float p_70539_3_, float p_70539_4_, float p_70539_5_, float p_70539_6_, float p_70539_7_)
	{
		if(particleAge > 0)
		{
			super.func_180434_a(p_70539_1_, e, p_70539_2_, p_70539_3_, p_70539_4_, p_70539_5_, p_70539_6_, p_70539_7_);
		}
	}
}
