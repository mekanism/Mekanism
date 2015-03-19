package mekanism.client.entity;

import mekanism.api.Pos3D;
import mekanism.client.render.MekanismRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class EntityLaser extends EntityFX
{
	double length;
	ForgeDirection direction;

	public EntityLaser(World world, Pos3D start, Pos3D end, ForgeDirection dir, double energy)
	{
		super(world, (start.xPos + end.xPos)/2D, (start.yPos + end.yPos)/2D, (start.zPos+end.zPos)/2D);
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
	public void renderParticle(Tessellator tessellator, float partialTick, float rotationX, float rotationXZ, float rotationZ, float rotationYZ, float rotationXY)
	{
		tessellator.draw();

		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_POLYGON_BIT + GL11.GL_ENABLE_BIT);
		GL11.glDisable(GL11.GL_CULL_FACE);
		MekanismRenderer.glowOn();
		Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("mekanism", "particles/laser.png"));

		float newX = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTick - interpPosX);
		float newY = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTick - interpPosY);
		float newZ = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTick - interpPosZ);

		GL11.glTranslatef(newX, newY, newZ);

		switch(direction)
		{
			case UP:
			case DOWN:
			default:
				break;
			case WEST:
			case EAST:
				GL11.glRotated(90, 0, 0, 1);
				break;
			case NORTH:
			case SOUTH:
				GL11.glRotated(90, 1, 0, 0);
				break;
		}
		GL11.glRotated(45, 0, 1, 0);
		tessellator.startDrawingQuads();
		tessellator.setColorRGBA_F(particleRed, particleGreen, particleBlue, particleAlpha);
		tessellator.addVertexWithUV(-particleScale, -length/2, 0, 0, 0);
		tessellator.addVertexWithUV(-particleScale, length/2, 0, 0, 1);
		tessellator.addVertexWithUV(particleScale, length/2, 0, 1, 1);
		tessellator.addVertexWithUV(particleScale, -length/2, 0, 1, 0);
		tessellator.draw();

		GL11.glRotated(90, 0, 1, 0);
		tessellator.startDrawingQuads();
		tessellator.setColorRGBA_F(particleRed, particleGreen, particleBlue, particleAlpha);
		tessellator.addVertexWithUV(-particleScale, -length/2, 0, 0, 0);
		tessellator.addVertexWithUV(-particleScale, length/2, 0, 0, 1);
		tessellator.addVertexWithUV(particleScale, length/2, 0, 1, 1);
		tessellator.addVertexWithUV(particleScale, -length/2, 0, 1, 0);
		tessellator.draw();
		
		MekanismRenderer.glowOff();
		GL11.glPopAttrib();
		GL11.glPopMatrix();

		Minecraft.getMinecraft().renderEngine.bindTexture(MekanismRenderer.getBlocksTexture());
		tessellator.startDrawingQuads();
	}

	@Override
	public int getFXLayer()
	{
		return 1;
	}
}
