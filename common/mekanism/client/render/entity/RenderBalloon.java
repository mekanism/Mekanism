package mekanism.client.render.entity;

import mekanism.client.model.ModelBalloon;
import mekanism.common.EntityBalloon;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBalloon extends Render
{
	public ModelBalloon model = new ModelBalloon();
	
	@Override
	protected ResourceLocation getEntityTexture(Entity entity) 
	{
		return MekanismUtils.getResource(ResourceType.RENDER, "Balloon.png");
	}

	@Override
	public void doRender(Entity entity, double x, double y, double z, float f, float f1)
	{
		EntityBalloon balloon = (EntityBalloon)entity;
		
		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);
		GL11.glRotatef(180, 1, 0, 0);
		GL11.glTranslatef(0, 0.9F, 0);
		
		bindTexture(getEntityTexture(entity));
		
		model.render(0.0625F, balloon.color);
		
		GL11.glPopMatrix();
	}
}
