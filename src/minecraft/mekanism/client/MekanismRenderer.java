package mekanism.client;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

/*
 * Credit to BuildCraft
 */
public class MekanismRenderer 
{
	private static RenderBlocks renderBlocks = new RenderBlocks();
	
	private static float lightmapLastX;
    private static float lightmapLastY;
	
	public static class Model3D
	{
		public double minX;
		public double minY;
		public double minZ;
		public double maxX;
		public double maxY;
		public double maxZ;

		public Block baseBlock = Block.sand;

		public Icon texture = null;

		public Icon getBlockTextureFromSide(int i) 
		{
			if(texture == null)
			{
				return baseBlock.getBlockTextureFromSide(i);
			}
			else {
				return texture;
			}
		}

		public float getBlockBrightness(IBlockAccess iblockaccess, int i, int j, int k)
		{
			return baseBlock.getBlockBrightness(iblockaccess, i, j, k);
		}
	}
	
	public static void renderObject(Model3D object, IBlockAccess blockAccess, int i, int j, int k, boolean doLight, boolean doTessellating)
	{
		float f = 0.5F;
		float f1 = 1.0F;
		float f2 = 0.8F;
		float f3 = 0.6F;

        renderBlocks.renderMaxX = object.maxX;
        renderBlocks.renderMinX = object.minX;
        renderBlocks.renderMaxY = object.maxY;
        renderBlocks.renderMinY = object.minY;
        renderBlocks.renderMaxZ = object.maxZ;
        renderBlocks.renderMinZ = object.minZ;

        renderBlocks.enableAO = false;


		Tessellator tessellator = Tessellator.instance;

		if(doTessellating) 
		{
			tessellator.startDrawingQuads();
		}

		float f4 = 0, f5 = 0;

		if(doLight)
		{
			f4 = object.getBlockBrightness(blockAccess, i, j, k);
			f5 = object.getBlockBrightness(blockAccess, i, j, k);
			
			if(f5 < f4) 
			{
				f5 = f4;
			}
			
			tessellator.setColorOpaque_F(f * f5, f * f5, f * f5);
		}

		renderBlocks.renderFaceYNeg(null, 0, 0, 0, object.getBlockTextureFromSide(0));

		if(doLight) 
		{
			f5 = object.getBlockBrightness(blockAccess, i, j, k);
			
			if(f5 < f4)
			{
				f5 = f4;
			}
			
			tessellator.setColorOpaque_F(f1 * f5, f1 * f5, f1 * f5);
		}

		renderBlocks.renderFaceYPos(null, 0, 0, 0, object.getBlockTextureFromSide(1));

		if(doLight) 
		{
			f5 = object.getBlockBrightness(blockAccess, i, j, k);
			
			if(f5 < f4) 
			{
				f5 = f4;
			}
			
			tessellator.setColorOpaque_F(f2 * f5, f2 * f5, f2 * f5);
		}

		renderBlocks.renderFaceZNeg(null, 0, 0, 0, object.getBlockTextureFromSide(2));

		if(doLight)
		{
			f5 = object.getBlockBrightness(blockAccess, i, j, k);
			
			if(f5 < f4) 
			{
				f5 = f4;
			}
			
			tessellator.setColorOpaque_F(f2 * f5, f2 * f5, f2 * f5);
		}

		renderBlocks.renderFaceZPos(null, 0, 0, 0, object.getBlockTextureFromSide(3));

		if(doLight)
		{
			f5 = object.getBlockBrightness(blockAccess, i, j, k);
			
			if(f5 < f4)
			{
				f5 = f4;
			}
			
			tessellator.setColorOpaque_F(f3 * f5, f3 * f5, f3 * f5);
		}

		renderBlocks.renderFaceXNeg(null, 0, 0, 0, object.getBlockTextureFromSide(4));

		if(doLight) 
		{
			f5 = object.getBlockBrightness(blockAccess, i, j, k);
			
			if(f5 < f4)
			{
				f5 = f4;
			}
			
			tessellator.setColorOpaque_F(f3 * f5, f3 * f5, f3 * f5);
		}

		renderBlocks.renderFaceXPos(null, 0, 0, 0, object.getBlockTextureFromSide(5));

		if(doTessellating) 
		{
			tessellator.draw();
		}
	}
	
    public static void glowOn() 
    {
        GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
        lightmapLastX = OpenGlHelper.lastBrightnessX;
        lightmapLastY = OpenGlHelper.lastBrightnessY;
        RenderHelper.disableStandardItemLighting();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
    }

    public static void glowOff() 
    {
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightmapLastX, lightmapLastY);
        GL11.glPopAttrib();
    }
    
    public static void renderOverlay(int x, int y, int z, int side, double minX, double maxX, double minY, double maxY)
    {
    	Tessellator tessellator = Tessellator.instance;
    	
    	double[] bounds = new double[] {x - .01, x + 1.01, y - .01, y + 1.01, z - .01, z + 1.01};
    	
        switch(side)
        {
			case 0:
				tessellator.addVertexWithUV(bounds[0], bounds[2], bounds[4], minX, minY);
				tessellator.addVertexWithUV(bounds[1], bounds[2], bounds[4], maxX, minY);
				tessellator.addVertexWithUV(bounds[1], bounds[2], bounds[5], maxX, maxY);
				tessellator.addVertexWithUV(bounds[0], bounds[2], bounds[5], minX, maxY);
				break;
			case 1:
				tessellator.addVertexWithUV(bounds[1], bounds[3], bounds[4], maxX, minY);
				tessellator.addVertexWithUV(bounds[0], bounds[3], bounds[4], minX, minY);
				tessellator.addVertexWithUV(bounds[0], bounds[3], bounds[5], minX, maxY);
				tessellator.addVertexWithUV(bounds[1], bounds[3], bounds[5], maxX, maxY);
				break;
			case 2:
				tessellator.addVertexWithUV(bounds[0], bounds[3], bounds[4], maxX, minY);
				tessellator.addVertexWithUV(bounds[1], bounds[3], bounds[4], minX, minY);
				tessellator.addVertexWithUV(bounds[1], bounds[2], bounds[4], minX, maxY);
				tessellator.addVertexWithUV(bounds[0], bounds[2], bounds[4], maxX, maxY);
				break;
			case 3:
				tessellator.addVertexWithUV(bounds[1], bounds[3], bounds[5], maxX, minY);
				tessellator.addVertexWithUV(bounds[0], bounds[3], bounds[5], minX, minY);
				tessellator.addVertexWithUV(bounds[0], bounds[2], bounds[5], minX, maxY);
				tessellator.addVertexWithUV(bounds[1], bounds[2], bounds[5], maxX, maxY);
				break;
			case 4:
				tessellator.addVertexWithUV(bounds[0], bounds[3], bounds[5], maxX, minY);
				tessellator.addVertexWithUV(bounds[0], bounds[3], bounds[4], minX, minY);
				tessellator.addVertexWithUV(bounds[0], bounds[2], bounds[4], minX, maxY);
				tessellator.addVertexWithUV(bounds[0], bounds[2], bounds[5], maxX, maxY);
				break;
			case 5:
				tessellator.addVertexWithUV(bounds[1], bounds[3], bounds[4], maxX, minY);
				tessellator.addVertexWithUV(bounds[1], bounds[3], bounds[5], minX, minY);
				tessellator.addVertexWithUV(bounds[1], bounds[2], bounds[5], minX, maxY);
				tessellator.addVertexWithUV(bounds[1], bounds[2], bounds[4], maxX, maxY);
				break;
		}
	}
    
    public static class DisplayInteger
    {
    	public int display;
    	
    	@Override
    	public int hashCode()
    	{
    		int code = 1;
    		code = 31 * code + display;
    		return code;
    	}
    	
    	@Override
    	public boolean equals(Object obj)
    	{
    		return obj instanceof DisplayInteger && ((DisplayInteger)obj).display == display;
    	}
    }
}
