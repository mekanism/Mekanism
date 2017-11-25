package mekanism.client.render;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mekanism.api.EnumColor;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.api.infuse.InfuseType;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.render.obj.TransmitterModel;
import mekanism.client.render.tileentity.RenderConfigurableMachine;
import mekanism.client.render.tileentity.RenderFluidTank;
import mekanism.client.render.tileentity.RenderThermalEvaporationController;
import mekanism.client.render.transmitter.RenderLogisticalTransporter;
import mekanism.client.render.transmitter.RenderMechanicalPipe;
import mekanism.common.Mekanism;
import mekanism.common.base.IMetaItem;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class MekanismRenderer 
{
	public enum FluidType
	{
		STILL,
		FLOWING
	}
	
	public static TextureAtlasSprite[] colors = new TextureAtlasSprite[256];
	
	public static TextureAtlasSprite energyIcon;
	public static TextureAtlasSprite heatIcon;
	public static TextureAtlasSprite laserIcon;
	
	public static float GAS_RENDER_BASE = 0.2F;
	
	public static Map<TransmissionType, TextureAtlasSprite> overlays = new HashMap<>();
	
	private static float lightmapLastX;
    private static float lightmapLastY;
	private static boolean optifineBreak = false;
	
	public static int[] directionMap = new int[] {3, 0, 1, 2};
	
	public static RenderConfigurableMachine machineRenderer = new RenderConfigurableMachine();
	
	private static String[] simpleSides = new String[] {"Bottom", "Top", "Front", "Back", "Left", "Right"};
	
	public static void init()
	{
		MinecraftForge.EVENT_BUS.register(new MekanismRenderer());
	}

	public static TextureAtlasSprite missingIcon;

	private static Map<FluidType, Map<Fluid, TextureAtlasSprite>> textureMap = new HashMap<>();

	@SubscribeEvent
	public void onStitch(TextureStitchEvent.Pre event)
	{
		for(EnumColor color : EnumColor.values())
		{
			colors[color.ordinal()] = event.getMap().registerSprite(new ResourceLocation("mekanism:blocks/overlay/overlay_" + color.unlocalizedName));
		}

		for(TransmissionType type : TransmissionType.values())
		{
			overlays.put(type, event.getMap().registerSprite(new ResourceLocation("mekanism:blocks/overlay/" + type.getTransmission() + "Overlay")));
		}

		energyIcon = event.getMap().registerSprite(new ResourceLocation("mekanism:blocks/liquid/LiquidEnergy"));
		heatIcon = event.getMap().registerSprite(new ResourceLocation("mekanism:blocks/liquid/LiquidHeat"));
		laserIcon = event.getMap().registerSprite(new ResourceLocation("mekanism:blocks/Laser"));
		
		event.getMap().registerSprite(new ResourceLocation("mekanism:blocks/liquid/LiquidHeavyWater"));
		
		TransmitterModel.registerIcons(event.getMap());

		for(Gas gas : GasRegistry.getRegisteredGasses())
		{
			gas.registerIcon(event.getMap());
		}

		for(InfuseType type : InfuseRegistry.getInfuseMap().values())
		{
			type.setIcon(event.getMap().registerSprite(type.iconResource));
		}

		FluidRenderer.resetDisplayInts();
		RenderThermalEvaporationController.resetDisplayInts();
		RenderFluidTank.resetDisplayInts();
	}
	
	@SubscribeEvent
	public void onStitch(TextureStitchEvent.Post event)
	{
		initFluidTextures(event.getMap());
		
		RenderLogisticalTransporter.onStitch(event.getMap());
		RenderMechanicalPipe.onStitch();
		
		for(Gas gas : GasRegistry.getRegisteredGasses())
		{
			gas.updateIcon(event.getMap());
		}
	}
	
	public static void registerItemRender(String domain, Item item)
	{
		if(item instanceof IMetaItem)
		{
			IMetaItem metaItem = (IMetaItem)item;
			List<ModelResourceLocation> variants = new ArrayList<>();
			
			for(int i = 0; i < metaItem.getVariants(); i++)
			{
				if(metaItem.getTexture(i) == null)
				{
					continue;
				}
				
				ModelResourceLocation loc = new ModelResourceLocation(domain + ":" + metaItem.getTexture(i), "inventory");
				ModelLoader.setCustomModelResourceLocation(item, i, loc);
				variants.add(loc);
				ModelBakery.registerItemVariants(item, new ResourceLocation(domain + ":" + metaItem.getTexture(i)));
			}
			
			return;
		}
		
		ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
	}


	public static void initFluidTextures(TextureMap map) 
	{
		missingIcon = map.getMissingSprite();

		textureMap.clear();

		for(FluidType type : FluidType.values()) 
		{
			textureMap.put(type, new HashMap<>());
		}

		for(Fluid fluid : FluidRegistry.getRegisteredFluids().values()) 
		{
			if(fluid.getFlowing() != null) 
			{
				String flow = fluid.getFlowing().toString();
				TextureAtlasSprite sprite;
				
				if(map.getTextureExtry(flow) != null) 
				{
					sprite = map.getTextureExtry(flow);
				} 
				else {
					sprite = map.registerSprite(fluid.getStill());
				}
				
				textureMap.get(FluidType.FLOWING).put(fluid, sprite);
			}

			if(fluid.getStill() != null) 
			{
				String still = fluid.getStill().toString();
				TextureAtlasSprite sprite;
				
				if(map.getTextureExtry(still) != null) 
				{
					sprite = map.getTextureExtry(still);
				} 
				else {
					sprite = map.registerSprite(fluid.getStill());
				}
				
				textureMap.get(FluidType.STILL).put(fluid, sprite);
			}
		}
	}

	public static TextureAtlasSprite getFluidTexture(Fluid fluid, FluidType type) 
	{
		if(fluid == null || type == null)
		{
			return missingIcon;
		}
		
		Map<Fluid, TextureAtlasSprite> map = textureMap.get(type);

		if (map == null){
			String errorType;
			if (textureMap.containsKey(type)){
				errorType = "a null got into";
			} else {
				errorType = "key "+type.name()+" is missing from";
			}
			Mekanism.logger.fatal("MekanismRenderer: Somehow "+errorType+" the texture map cache. This is not normal! Please reload your resources.");
			return missingIcon;
		}
		
		return map.getOrDefault(fluid, missingIcon);
	}
	
	private static VertexFormat prevFormat = null;
	private static int prevMode = -1;
	
	public static void pauseRenderer(Tessellator tess)
	{
		if(MekanismRenderer.isDrawing(tess))
		{
			prevFormat = tess.getBuffer().getVertexFormat();
			prevMode = tess.getBuffer().getDrawMode();
			tess.draw();
		}
	}
	
	public static void saveRenderer(Tessellator tess)
	{
		if(MekanismRenderer.isDrawing(tess))
		{
			prevFormat = tess.getBuffer().getVertexFormat();
			prevMode = tess.getBuffer().getDrawMode();
		}
	}
	
	public static void resumeRenderer(Tessellator tess)
	{
    	if(prevFormat != null)
    	{
	    	tess.getBuffer().begin(prevMode, prevFormat);
    	}
    	
    	prevFormat = null;
    	prevMode = -1;
	}
	
	public static boolean isDrawing(Tessellator tess)
	{
		return tess.getBuffer().isDrawing;
	}
	
	public static boolean isDrawing(BufferBuilder buffer)
	{
		return buffer.isDrawing;
	}

	public static class Model3D
	{
		public double posX, posY, posZ;
		
		public double minX, minY, minZ;
		public double maxX, maxY, maxZ;
		
	    public double textureStartX = 0, textureStartY = 0, textureStartZ = 0;
	    public double textureSizeX = 16, textureSizeY = 16, textureSizeZ = 16;
	    public double textureOffsetX = 0, textureOffsetY = 0, textureOffsetZ = 0;
	    
	    public int[] textureFlips = new int[] {2, 2, 2, 2, 2, 2};
		
		public TextureAtlasSprite[] textures = new TextureAtlasSprite[6];
		
		public boolean[] renderSides = new boolean[] {true, true, true, true, true, true, false};

		public Block baseBlock = Blocks.SAND;
		
	    public final void setBlockBounds(double xNeg, double yNeg, double zNeg, double xPos, double yPos, double zPos)
	    {
	    	minX = xNeg;
	    	minY = yNeg;
	    	minZ = zNeg;
	    	maxX = xPos;
	    	maxY = yPos;
	    	maxZ = zPos;
	    }
	    
	    public double sizeX()
	    {
	    	return maxX-minX;
	    }
	    
	    public double sizeY()
	    {
	    	return maxY-minY;
	    }
	    
	    public double sizeZ()
	    {
	    	return maxZ-minZ;
	    }
		
		public void setSideRender(EnumFacing side, boolean value)
		{
			renderSides[side.ordinal()] = value;
		}
		
		public boolean shouldSideRender(EnumFacing side)
		{
			return renderSides[side.ordinal()];
		}

		public TextureAtlasSprite getBlockTextureFromSide(int i)
		{
			return textures[i];
		}
		
		public void setTexture(TextureAtlasSprite tex)
		{
			Arrays.fill(textures, tex);
		}
		
		public void setTextures(TextureAtlasSprite down, TextureAtlasSprite up, TextureAtlasSprite north, TextureAtlasSprite south, TextureAtlasSprite west, TextureAtlasSprite east)
		{
			textures[0] = down;
			textures[1] = up;
			textures[2] = north;
			textures[3] = south;
			textures[4] = west;
			textures[5] = east;
		}
	}
	
	public static BakedQuad iconTransform(BakedQuad quad, TextureAtlasSprite sprite)
	{
		int[] vertices = new int[quad.getVertexData().length];
		System.arraycopy(quad.getVertexData(), 0, vertices, 0, vertices.length);
		
	    for(int i = 0; i < 4; ++i)
        {
            int j = quad.getFormat().getIntegerSize() * i;
            int uvIndex = quad.getFormat().getUvOffsetById(0) / 4;
            vertices[j + uvIndex] = Float.floatToRawIntBits(sprite.getInterpolatedU(quad.getSprite().getUnInterpolatedU(Float.intBitsToFloat(vertices[j + uvIndex]))));
            vertices[j + uvIndex + 1] = Float.floatToRawIntBits(sprite.getInterpolatedV(quad.getSprite().getUnInterpolatedV(Float.intBitsToFloat(vertices[j + uvIndex + 1]))));
        }
	    
		return new BakedQuad(vertices, quad.getTintIndex(), quad.getFace(), sprite, quad.shouldApplyDiffuseLighting(), quad.getFormat());
	}
	
    public static BakedQuad rotate(BakedQuad quad, int amount)
    {
		int[] vertices = new int[quad.getVertexData().length];
		System.arraycopy(quad.getVertexData(), 0, vertices, 0, vertices.length);
		
		for(int i = 0; i < 4; i++)
		{
			int nextIndex = (i+amount)%4;
			int quadSize = quad.getFormat().getIntegerSize();
            int uvIndex = quad.getFormat().getUvOffsetById(0) / 4;
            vertices[quadSize*i + uvIndex] = quad.getVertexData()[quadSize*nextIndex + uvIndex];
            vertices[quadSize*i + uvIndex + 1] = quad.getVertexData()[quadSize*nextIndex + uvIndex + 1];
		}
		
		return new BakedQuad(vertices, quad.getTintIndex(), quad.getFace(), quad.getSprite(), quad.shouldApplyDiffuseLighting(), quad.getFormat());
    }
	
	public static void prepFlowing(Model3D model, Fluid fluid)
	{
		TextureAtlasSprite still = getFluidTexture(fluid, FluidType.STILL);
		TextureAtlasSprite flowing = getFluidTexture(fluid, FluidType.FLOWING);
		
		model.setTextures(still, still, flowing, flowing, flowing, flowing);
	}
	
	public static void renderObject(Model3D object)
	{
		if(object == null)
		{
			return;
		}
		
		GlStateManager.pushMatrix();
		GL11.glTranslated(object.minX, object.minY, object.minZ);
		RenderResizableCuboid.INSTANCE.renderCube(object);
		GlStateManager.popMatrix();
	}
	
	public static void color(EnumColor color)
	{
		color(color, 1.0F);
	}
	
	public static void color(EnumColor color, float alpha)
	{
		color(color, alpha, 1.0F);
	}
	
	public static void color(EnumColor color, float alpha, float multiplier)
	{
		GL11.glColor4f(color.getColor(0)*multiplier, color.getColor(1)*multiplier, color.getColor(2)*multiplier, alpha);
	}
	
	public static void resetColor()
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	}
	
	public static TextureAtlasSprite getColorIcon(EnumColor color)
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

	public static void colorFluid(Fluid fluid)
	{
		color(fluid.getColor());
	}
	
	public static void color(int color)
	{
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
    
    public static float getPartialTick()
    {
    	return Minecraft.getMinecraft().getRenderPartialTicks();
    }
    
    public static ResourceLocation getBlocksTexture()
    {
    	return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }
    
    public interface ICustomBlockIcon
    {
    	ResourceLocation getIcon(ItemStack stack, int side);
    }
}
