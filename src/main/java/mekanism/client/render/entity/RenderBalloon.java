package mekanism.client.render.entity;

import mekanism.api.EnumColor;
import mekanism.client.model.ModelBalloon;
import mekanism.common.entity.EntityBalloon;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderBalloon extends Render
{
	private Minecraft mc = Minecraft.getMinecraft();

	public ModelBalloon model = new ModelBalloon();

	@Override
	protected ResourceLocation getEntityTexture(Entity entity)
	{
		return MekanismUtils.getResource(ResourceType.RENDER, "Balloon.png");
	}

	@Override
	public void doRender(Entity entity, double x, double y, double z, float f, float partialTick)
	{
		EntityBalloon balloon = (EntityBalloon)entity;

		if(balloon.isLatchedToEntity())
		{
			x = (balloon.latchedEntity.lastTickPosX + (balloon.latchedEntity.posX - balloon.latchedEntity.lastTickPosX)*partialTick);
			y = (balloon.latchedEntity.lastTickPosY + (balloon.latchedEntity.posY - balloon.latchedEntity.lastTickPosY)*partialTick);
			z = (balloon.latchedEntity.lastTickPosZ + (balloon.latchedEntity.posZ - balloon.latchedEntity.lastTickPosZ)*partialTick);

			x -= RenderManager.renderPosX;
			y -= RenderManager.renderPosY;
			z -= RenderManager.renderPosZ;

			y += balloon.latchedEntity.height + 1.7F;
		}

		render(((EntityBalloon)entity).color, x, y, z);
	}

	public void render(EnumColor color, double x, double y, double z)
	{
		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);
		GL11.glRotatef(180, 1, 0, 0);
		GL11.glTranslatef(0, 0.9F, 0);

		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "Balloon.png"));

		model.render(0.0625F, color);

		GL11.glPopMatrix();
	}
}
