package mekanism.client.entity;

import mekanism.api.Coord4D;
import mekanism.api.Pos3D;
import mekanism.client.render.MekanismRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class EntityLaser extends EntityFX
{
	double length;
	ForgeDirection direction;

	public EntityLaser(World world, Coord4D start, Coord4D end, ForgeDirection direction)
	{
		super(world, (start.xCoord + end.xCoord)/2D + 0.5D, (start.yCoord + end.yCoord)/2D + 0.5D, (start.zCoord+end.zCoord)/2D + 0.5D);
		particleMaxAge = 5;
		particleRed = 1;
		particleGreen = 0;
		particleBlue = 0;
		particleAlpha = 0.3F;
		particleScale = 0.1F;
		length = new Pos3D(end).distance(new Pos3D(start));
		this.direction = direction;
	}

	@Override
	public void renderParticle(Tessellator tessellator, float partialTick, float rotationX, float rotationXZ, float rotationZ, float rotationYZ, float rotationXY)
	{
		tessellator.draw();

		GL11.glPushMatrix();
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

		GL11.glPopMatrix();

		Minecraft.getMinecraft().renderEngine.bindTexture(MekanismRenderer.getBlocksTexture());
		tessellator.startDrawingQuads();
	}

	public int getFXLayer()
	{
		return 1;
	}
}
