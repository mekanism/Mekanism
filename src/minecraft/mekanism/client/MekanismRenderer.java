package mekanism.client;

import mekanism.common.ISpecialBounds;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/*
 * Credit to BuildCraft
 */
@SideOnly(Side.CLIENT)
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
    
	/**
	 * Cleaned-up snip of RenderBlocks.renderBlockAsItem() -- used for rendering an item as an entity,
	 * in a player's inventory, and in a player's hand.
	 * @param renderer - RenderBlocks renderer to render the item with
	 * @param metadata - block/item metadata
	 * @param block - block to render
	 */
	public static void renderItem(RenderBlocks renderer, int metadata, Block block)
	{
		if(!(block instanceof ISpecialBounds) || ((ISpecialBounds)block).doDefaultBoundSetting(metadata))
		{
			block.setBlockBoundsForItemRender();
		}
		
		if(block instanceof ISpecialBounds)
		{
			((ISpecialBounds)block).setRenderBounds(block, metadata);
		}
		
		if(!(block instanceof ISpecialBounds) || ((ISpecialBounds)block).doDefaultBoundSetting(metadata))
		{
			renderer.setRenderBoundsFromBlock(block);
		}
		else {
			renderer.setRenderBounds(0, 0, 0, 1, 1, 1);
		}

        if(renderer.useInventoryTint)
        {
            int renderColor = block.getRenderColor(metadata);
            float red = (float)(renderColor >> 16 & 255) / 255.0F;
            float green = (float)(renderColor >> 8 & 255) / 255.0F;
            float blue = (float)(renderColor & 255) / 255.0F;
            GL11.glColor4f(red, green, blue, 1.0F);
        }

        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, -1.0F, 0.0F);
        renderer.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(0, metadata));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 1.0F, 0.0F);
        renderer.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(1, metadata));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 0.0F, -1.0F);
        renderer.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(2, metadata));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 0.0F, 1.0F);
        renderer.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(3, metadata));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(-1.0F, 0.0F, 0.0F);
        renderer.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(4, metadata));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(1.0F, 0.0F, 0.0F);
        renderer.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(5, metadata));
        tessellator.draw();
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
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
