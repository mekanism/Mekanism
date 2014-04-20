package mekanism.client.render;

import java.util.Arrays;
import java.util.List;

import mekanism.api.EnumColor;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.OreGas;
import mekanism.client.render.tileentity.RenderConfigurableMachine;
import mekanism.common.ISpecialBounds;
import mekanism.common.ObfuscatedNames;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Timer;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MekanismRenderer 
{
	private static RenderBlocks renderBlocks = new RenderBlocks();
	
	public static Icon[] colors = new Icon[256];
	
	public static Icon energyIcon;
	
	private static float lightmapLastX;
    private static float lightmapLastY;
	private static boolean optifineBreak = false;
	
	public static RenderConfigurableMachine machineRenderer = new RenderConfigurableMachine();
	
	public static void init()
	{
		MinecraftForge.EVENT_BUS.register(new MekanismRenderer());
	}
	
	@ForgeSubscribe
	public void onStitch(TextureStitchEvent.Pre event)
	{
		if(event.map.textureType == 0)
		{
			for(EnumColor color : EnumColor.values())
			{
				colors[color.ordinal()] = event.map.registerIcon("mekanism:overlay/Overlay" + color.getLocalizedName().replace(" ", ""));
			}
			
			energyIcon = event.map.registerIcon("mekanism:LiquidEnergy");
			
			GasRegistry.getGas("hydrogen").setIcon(event.map.registerIcon("mekanism:LiquidHydrogen"));
			GasRegistry.getGas("oxygen").setIcon(event.map.registerIcon("mekanism:LiquidOxygen"));
			GasRegistry.getGas("water").setIcon(event.map.registerIcon("mekanism:WaterVapor"));
			GasRegistry.getGas("chlorine").setIcon(event.map.registerIcon("mekanism:Chlorine"));
			GasRegistry.getGas("sulfurDioxideGas").setIcon(event.map.registerIcon("mekanism:LiquidSulfurDioxide"));
			GasRegistry.getGas("sulfurTrioxideGas").setIcon(event.map.registerIcon("mekanism:LiquidSulfurTrioxide"));
			GasRegistry.getGas("sulfuricAcid").setIcon(event.map.registerIcon("mekanism:LiquidSulfuricAcid"));
			GasRegistry.getGas("hydrogenChloride").setIcon(event.map.registerIcon("mekanism:LiquidHydrogenChloride"));
			GasRegistry.getGas("liquidOsmium").setIcon(event.map.registerIcon("mekanism:LiquidOsmium"));
			GasRegistry.getGas("liquidStone").setIcon(event.map.registerIcon("mekanism:LiquidStone"));
			GasRegistry.getGas("ethene").setIcon(event.map.registerIcon("mekanism:Ethene"));
			
			for(Gas gas : GasRegistry.getRegisteredGasses())
			{
				if(gas instanceof OreGas)
				{
					if(gas.getUnlocalizedName().contains("clean"))
					{
						gas.setIcon(event.map.registerIcon("mekanism:LiquidCleanOre"));
					}
					else {
						gas.setIcon(event.map.registerIcon("mekanism:LiquidOre"));
					}
				}
			}

			FluidRegistry.getFluid("brine").setIcons(event.map.registerIcon("mekanism:LiquidBrine"));
		}
	}
    
	public static class Model3D
	{
		public double minX;
		public double minY;
		public double minZ;
		public double maxX;
		public double maxY;
		public double maxZ;
		
		public Icon[] textures = new Icon[6];
		
		public boolean[] renderSides = new boolean[] {true, true, true, true, true, true, false};

		public Block baseBlock = Block.sand;
		
	    public final void setBlockBounds(float xNeg, float yNeg, float zNeg, float xPos, float yPos, float zPos)
	    {
	    	minX = xNeg;
	    	minY = yNeg;
	    	minZ = zNeg;
	    	maxX = xPos;
	    	maxY = yPos;
	    	maxZ = zPos;
	    }
		
		public void setSideRender(ForgeDirection side, boolean value)
		{
			renderSides[side.ordinal()] = value;
		}
		
		public boolean shouldSideRender(ForgeDirection side)
		{
			return renderSides[side.ordinal()];
		}

		public Icon getBlockTextureFromSide(int i) 
		{
			return textures[i];
		}
		
		public void setTexture(Icon tex)
		{
			Arrays.fill(textures, tex);
		}
		
		public void setTextures(Icon down, Icon up, Icon north, Icon south, Icon west, Icon east)
		{
			textures[0] = down;
			textures[1] = up;
			textures[2] = north;
			textures[3] = south;
			textures[4] = west;
			textures[5] = east;
		}
	}
	
	public static void renderObject(Model3D object)
	{
		if(object == null)
		{
			return;
		}
		
        renderBlocks.renderMaxX = object.maxX;
        renderBlocks.renderMinX = object.minX;
        renderBlocks.renderMaxY = object.maxY;
        renderBlocks.renderMinY = object.minY;
        renderBlocks.renderMaxZ = object.maxZ;
        renderBlocks.renderMinZ = object.minZ;
        
        renderBlocks.enableAO = false;

		Tessellator.instance.startDrawingQuads();

		if(object.shouldSideRender(ForgeDirection.DOWN))
		{
			renderBlocks.renderFaceYNeg(null, 0, 0, 0, object.getBlockTextureFromSide(0));
		}

		if(object.shouldSideRender(ForgeDirection.UP))
		{
			renderBlocks.renderFaceYPos(null, 0, 0, 0, object.getBlockTextureFromSide(1));
		}

		if(object.shouldSideRender(ForgeDirection.NORTH))
		{
			renderBlocks.renderFaceZNeg(null, 0, 0, 0, object.getBlockTextureFromSide(2));
		}

		if(object.shouldSideRender(ForgeDirection.SOUTH))
		{
			renderBlocks.renderFaceZPos(null, 0, 0, 0, object.getBlockTextureFromSide(3));
		}

		if(object.shouldSideRender(ForgeDirection.WEST))
		{
			renderBlocks.renderFaceXNeg(null, 0, 0, 0, object.getBlockTextureFromSide(4));
		}

		if(object.shouldSideRender(ForgeDirection.EAST))
		{
			renderBlocks.renderFaceXPos(null, 0, 0, 0, object.getBlockTextureFromSide(5));
		}
		
		if(Tessellator.instance.isDrawing)
		{
			Tessellator.instance.draw();
		}
	}
	
	public static Icon getColorIcon(EnumColor color)
	{
		return colors[color.ordinal()];
	}
	
    public static void glowOn() 
    {
    	glowOn(15);
    }
    
    public static void glowOn(int glow)
    {
        GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
        
        try {
        	lightmapLastX = OpenGlHelper.lastBrightnessX;
        	lightmapLastY = OpenGlHelper.lastBrightnessY;
        } catch(NoSuchFieldError e) {
        	optifineBreak = true;
        }
        
        RenderHelper.disableStandardItemLighting();
        
        float glowRatioX = Math.min((glow/15F)*240F + lightmapLastX, 240);
        float glowRatioY = Math.min((glow/15F)*240F + lightmapLastY, 240);
        
        if(!optifineBreak)
        {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, glowRatioX, glowRatioY);        	
        }
    }

    public static void glowOff() 
    {
    	if(!optifineBreak)
    	{
    		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightmapLastX, lightmapLastY);
    	}
    	
        GL11.glPopAttrib();
    }
    
    public static void blendOn()
    {
		GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_LIGHTING_BIT);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }
    
    public static void blendOff()
    {
    	GL11.glPopAttrib();
    }

	/**
	 * Blender .objs have a different handedness of coordinate system to MC, so faces are wound backwards.
	 */
	public static void cullFrontFace()
	{
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_FRONT);
	}

	public static void disableCullFace()
	{
		GL11.glCullFace(GL11.GL_BACK);
		GL11.glDisable(GL11.GL_CULL_FACE);
	}
    
    /**
     * Cleaned-up snip of ItemRenderer.renderItem() -- meant to render 2D items as equipped.
     * @param item - ItemStack to render
     */
    public static void renderItem(ItemStack item)
    {
		Icon icon = item.getItem().getIconIndex(item);
		TextureManager texturemanager = Minecraft.getMinecraft().getTextureManager();

        if(icon == null)
        {
            GL11.glPopMatrix();
            return;
        }

        texturemanager.bindTexture(texturemanager.getResourceLocation(item.getItemSpriteNumber()));
        Tessellator tessellator = Tessellator.instance;
        
        float minU = icon.getMinU();
        float maxU = icon.getMaxU();
        float minV = icon.getMinV();
        float maxV = icon.getMaxV();
        
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glTranslatef(0.0F, -0.3F, 0.0F);
        
        GL11.glScalef(1.5F, 1.5F, 1.5F);
        GL11.glRotatef(50.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(335.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef(-0.9375F, -0.0625F, 0.0F);
        
        RenderManager.instance.itemRenderer.renderItemIn2D(tessellator, maxU, minV, minU, maxV, icon.getIconWidth(), icon.getIconHeight(), 0.0625F);

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
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
            float red = (renderColor >> 16 & 255) / 255.0F;
            float green = (renderColor >> 8 & 255) / 255.0F;
            float blue = (renderColor & 255) / 255.0F;
            GL11.glColor4f(red, green, blue, 1.0F);
        }

        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        
        try {
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
        } catch(Exception e) {}
        
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
	}
	
	public static void colorFluid(Fluid fluid)
	{
	    int color = fluid.getColor();
	    
	    float cR = (color >> 16 & 0xFF) / 255.0F;
	    float cG = (color >> 8 & 0xFF) / 255.0F;
	    float cB = (color & 0xFF) / 255.0F;
	    
	    GL11.glColor3f(cR, cG, cB);
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
    	
    	public static DisplayInteger createAndStart()
    	{
    		DisplayInteger newInteger = new DisplayInteger();
    		newInteger.display =  GLAllocation.generateDisplayLists(1);
    		GL11.glNewList(newInteger.display, GL11.GL_COMPILE);
    		return newInteger;
    	}
    	
    	public static void endList()
    	{
    		GL11.glEndList();
    	}
    	
    	public void render()
    	{
    		GL11.glCallList(display);
    	}
    }
    
    public static TextureMap getTextureMap(int type)
    {
    	try {
    		List l = (List)MekanismUtils.getPrivateValue(Minecraft.getMinecraft().renderEngine, TextureManager.class, ObfuscatedNames.TextureManager_listTickables);
    		
    		for(Object obj : l)
    		{
    			if(obj instanceof TextureMap)
    			{
    				if(((TextureMap)obj).textureType == type)
    				{
    					return (TextureMap)obj;
    				}
    			}
    		}
    	} catch(Exception e) {}
    	
    	return null;
    }
    
    public static class BooleanArray
    {
    	private final boolean[] boolArray;
    	
    	public BooleanArray(boolean[] array)
		{
			boolArray = array.clone();
		}
    	
    	@Override
    	public boolean equals(Object o)
    	{
    		if(o instanceof BooleanArray)
    		{
    			return Arrays.equals(boolArray, ((BooleanArray)o).boolArray);
    		}
    		else if(o instanceof boolean[]) 
    		{
    			return Arrays.equals(boolArray, (boolean[])o);
    		}
    		else {
    			return false;
    		}
    	}
    	
    	@Override
    	public int hashCode()
    	{
    		return Arrays.hashCode(boolArray);
    	}
    }
    
    public static float getPartialTick()
    {
    	try {
    		Timer t = (Timer)MekanismUtils.getPrivateValue(Minecraft.getMinecraft(), Minecraft.class, ObfuscatedNames.Minecraft_timer);
    		return t.renderPartialTicks;
    	} catch(Exception e) {}
    	
    	return 0;
    }
    
    public void drawTexturedModalRect(int par1, int par2, int par3, int par4, int par5, int par6)
    {
    	int zLevel = 0;
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV((par1 + 0), (par2 + par6), zLevel, ((par3 + 0) * f), ((par4 + par6) * f1));
        tessellator.addVertexWithUV((par1 + par5), (par2 + par6), zLevel, ((par3 + par5) * f), ((par4 + par6) * f1));
        tessellator.addVertexWithUV((par1 + par5), (par2 + 0), zLevel, ((par3 + par5) * f), ((par4 + 0) * f1));
        tessellator.addVertexWithUV((par1 + 0), (par2 + 0), zLevel, ((par3 + 0) * f), ((par4 + 0) * f1));
        tessellator.draw();
    }
    
    public static ResourceLocation getBlocksTexture()
    {
    	return TextureMap.locationBlocksTexture;
    }
    
    public static ResourceLocation getItemsTexture()
    {
    	return TextureMap.locationItemsTexture;
    }
}
