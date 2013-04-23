package mekanism.client;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;

public class ObjectRenderer 
{
	private static RenderBlocks renderBlocks = new RenderBlocks();
	
	public static class Model3D
	{
		public double minX;
		public double minY;
		public double minZ;
		public double maxX;
		public double maxY;
		public double maxZ;
		
		public int lightValue;

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

		renderBlocks.renderBottomFace(null, 0, 0, 0, object.getBlockTextureFromSide(0));

		if(doLight) 
		{
			f5 = object.getBlockBrightness(blockAccess, i, j, k);
			
			if(f5 < f4)
			{
				f5 = f4;
			}
			
			tessellator.setColorOpaque_F(f1 * f5, f1 * f5, f1 * f5);
		}

		renderBlocks.renderTopFace(null, 0, 0, 0, object.getBlockTextureFromSide(1));

		if(doLight) 
		{
			f5 = object.getBlockBrightness(blockAccess, i, j, k);
			
			if(f5 < f4) 
			{
				f5 = f4;
			}
			
			tessellator.setColorOpaque_F(f2 * f5, f2 * f5, f2 * f5);
		}

		renderBlocks.renderEastFace(null, 0, 0, 0, object.getBlockTextureFromSide(2));

		if(doLight)
		{
			f5 = object.getBlockBrightness(blockAccess, i, j, k);
			
			if(f5 < f4) 
			{
				f5 = f4;
			}
			
			tessellator.setColorOpaque_F(f2 * f5, f2 * f5, f2 * f5);
		}

		renderBlocks.renderWestFace(null, 0, 0, 0, object.getBlockTextureFromSide(3));

		if(doLight)
		{
			f5 = object.getBlockBrightness(blockAccess, i, j, k);
			
			if(f5 < f4)
			{
				f5 = f4;
			}
			
			tessellator.setColorOpaque_F(f3 * f5, f3 * f5, f3 * f5);
		}

		renderBlocks.renderNorthFace(null, 0, 0, 0, object.getBlockTextureFromSide(4));

		if(doLight) 
		{
			f5 = object.getBlockBrightness(blockAccess, i, j, k);
			
			if(f5 < f4)
			{
				f5 = f4;
			}
			
			tessellator.setColorOpaque_F(f3 * f5, f3 * f5, f3 * f5);
		}

		renderBlocks.renderSouthFace(null, 0, 0, 0, object.getBlockTextureFromSide(5));

		if(doTessellating) 
		{
			tessellator.draw();
		}
	}
}
