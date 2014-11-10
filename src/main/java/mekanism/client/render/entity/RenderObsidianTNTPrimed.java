package mekanism.client.render.entity;

import mekanism.client.model.ModelObsidianTNT;
import mekanism.common.entity.EntityObsidianTNT;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderObsidianTNTPrimed extends Render
{
	private RenderBlocks blockRenderer = new RenderBlocks();
	private ModelObsidianTNT model = new ModelObsidianTNT();

	public RenderObsidianTNTPrimed()
	{
		shadowSize = 0.5F;
	}

	@Override
	public void doRender(Entity entity, double x, double y, double z, float f, float f1)
	{
		renderObsidianTNT((EntityObsidianTNT)entity, x, y, z, f, f1);
	}

	public void renderObsidianTNT(EntityObsidianTNT entityobsidiantnt, double x, double y, double z, float f, float f1)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef((float)x, (float)y+1.2F, (float)z);
		GL11.glScalef(0.8F, 0.8F, 0.8F);
		GL11.glRotatef(180, 1, 0, 0);

		if((entityobsidiantnt.fuse - f1) + 1.0F < 10F)
		{
			float scale = 1.0F - ((entityobsidiantnt.fuse - f1) + 1.0F) / 10F;

			if(scale < 0.0F)
			{
				scale = 0.0F;
			}

			if(scale > 1.0F)
			{
				scale = 1.0F;
			}

			scale *= scale;
			scale *= scale;
			float renderScale = 1.0F + scale * 0.3F;
			GL11.glScalef(renderScale, renderScale, renderScale);
		}

		float f3 = (1.0F - ((entityobsidiantnt.fuse - f1) + 1.0F) / 100F) * 0.8F;
		bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ObsidianTNT.png"));
		model.render(0.0625F);

		if(entityobsidiantnt.fuse / 5 % 2 == 0)
		{
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_DST_ALPHA);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, f3);
			model.render(0.0625F);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}

		GL11.glPopMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity)
	{
		return TextureMap.locationBlocksTexture;
	}
}
