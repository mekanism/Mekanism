package mekanism.client.entity;

import mekanism.api.Pos3D;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class EntityLaser extends EntityFX
{
	double length;
	EnumFacing direction;

	public EntityLaser(World world, Pos3D start, Pos3D end, EnumFacing dir, double energy)
	{
		super(world, (start.xCoord + end.xCoord)/2D, (start.yCoord + end.yCoord)/2D, (start.zCoord+end.zCoord)/2D);
		particleMaxAge = 5;
		particleRed = 1;
		particleGreen = 0;
		particleBlue = 0;
		particleAlpha = 0.1F;
		particleScale = (float) Math.min(energy / 50000, 0.6);
		length = end.distance(start);
		direction = dir;
	}

	@Override
	public void renderParticle(WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, float p_180434_4_, float p_180434_5_, float p_180434_6_, float p_180434_7_, float p_180434_8_)
	{
		worldRendererIn.finishDrawing();

		GlStateManager.pushMatrix();
		GL11.glPushAttrib(GL11.GL_POLYGON_BIT + GL11.GL_ENABLE_BIT);
		GL11.glDisable(GL11.GL_CULL_FACE);
		MekanismRenderer.glowOn();
		Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("mekanism", "particles/laser.png"));

		float newX = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
		float newY = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
		float newZ = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);

		GlStateManager.translate(newX, newY, newZ);

		switch(direction)
		{
			case UP:
			case DOWN:
			default:
				break;
			case WEST:
			case EAST:
				GlStateManager.rotate(90, 0, 0, 1);
				break;
			case NORTH:
			case SOUTH:
				GlStateManager.rotate(90, 1, 0, 0);
				break;
		}
		
		GlStateManager.rotate(45, 0, 1, 0);
		worldRendererIn.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		worldRendererIn.pos(-particleScale, -length/2, 0).tex(0, 0).color(particleRed, particleGreen, particleBlue, particleAlpha).endVertex();
		worldRendererIn.pos(-particleScale, length/2, 0).tex(0, 1).color(particleRed, particleGreen, particleBlue, particleAlpha).endVertex();
		worldRendererIn.pos(particleScale, length/2, 0).tex(1, 1).color(particleRed, particleGreen, particleBlue, particleAlpha).endVertex();
		worldRendererIn.pos(particleScale, -length/2, 0).tex(1, 0).color(particleRed, particleGreen, particleBlue, particleAlpha).endVertex();
		worldRendererIn.finishDrawing();

		GlStateManager.rotate(90, 0, 1, 0);
		worldRendererIn.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		worldRendererIn.pos(-particleScale, -length/2, 0).tex(0, 0).color(particleRed, particleGreen, particleBlue, particleAlpha).endVertex();
		worldRendererIn.pos(-particleScale, length/2, 0).tex(0, 1).color(particleRed, particleGreen, particleBlue, particleAlpha).endVertex();
		worldRendererIn.pos(particleScale, length/2, 0).tex(1, 1).color(particleRed, particleGreen, particleBlue, particleAlpha).endVertex();
		worldRendererIn.pos(particleScale, -length/2, 0).tex(1, 0).color(particleRed, particleGreen, particleBlue, particleAlpha).endVertex();
		worldRendererIn.finishDrawing();
		
		MekanismRenderer.glowOff();
		GL11.glPopAttrib();
		GlStateManager.popMatrix();

		Minecraft.getMinecraft().renderEngine.bindTexture(MekanismRenderer.getBlocksTexture());
		worldRendererIn.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
	}

	@Override
	public int getFXLayer()
	{
		return 1;
	}
}
