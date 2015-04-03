package mekanism.client.render;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mekanism.api.EnumColor;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.OreGas;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.render.tileentity.RenderConfigurableMachine;
import mekanism.client.render.tileentity.RenderDynamicTank;
import mekanism.client.render.tileentity.RenderPortableTank;
import mekanism.client.render.tileentity.RenderSalinationController;
import mekanism.common.ObfuscatedNames;
import mekanism.common.base.ISpecialBounds;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Timer;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MekanismRenderer 
{
	private static RenderBlocks renderBlocks = new RenderBlocks();
	
	public static IIcon[] colors = new IIcon[256];
	
	public static IIcon energyIcon;
	public static IIcon heatIcon;
	
	public static Map<TransmissionType, IIcon> overlays = new HashMap<TransmissionType, IIcon>();
	
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
	
	@SubscribeEvent
	public void onStitch(TextureStitchEvent.Pre event)
	{
		if(event.map.getTextureType() == 0)
		{
			for(EnumColor color : EnumColor.values())
			{
				colors[color.ordinal()] = event.map.registerIcon("mekanism:overlay/overlay_" + color.unlocalizedName);
			}
			
			for(TransmissionType type : TransmissionType.values())
			{
				overlays.put(type, event.map.registerIcon("mekanism:overlay/" + type.getTransmission() + "Overlay"));
			}
			
			energyIcon = event.map.registerIcon("mekanism:LiquidEnergy");
			heatIcon = event.map.registerIcon("mekanism:LiquidHeat");
			
			GasRegistry.getGas("hydrogen").setIcon(event.map.registerIcon("mekanism:LiquidHydrogen"));
			GasRegistry.getGas("oxygen").setIcon(event.map.registerIcon("mekanism:LiquidOxygen"));
			GasRegistry.getGas("water").setIcon(event.map.registerIcon("mekanism:LiquidSteam"));
			GasRegistry.getGas("chlorine").setIcon(event.map.registerIcon("mekanism:LiquidChlorine"));
			GasRegistry.getGas("sulfurDioxideGas").setIcon(event.map.registerIcon("mekanism:LiquidSulfurDioxide"));
			GasRegistry.getGas("sulfurTrioxideGas").setIcon(event.map.registerIcon("mekanism:LiquidSulfurTrioxide"));
			GasRegistry.getGas("sulfuricAcid").setIcon(event.map.registerIcon("mekanism:LiquidSulfuricAcid"));
			GasRegistry.getGas("hydrogenChloride").setIcon(event.map.registerIcon("mekanism:LiquidHydrogenChloride"));
			GasRegistry.getGas("liquidOsmium").setIcon(event.map.registerIcon("mekanism:LiquidOsmium"));
			GasRegistry.getGas("liquidStone").setIcon(event.map.registerIcon("mekanism:LiquidStone"));
			GasRegistry.getGas("ethene").setIcon(event.map.registerIcon("mekanism:LiquidEthene"));
			GasRegistry.getGas("brine").setIcon(event.map.registerIcon("mekanism:LiquidBrine"));
			GasRegistry.getGas("sodium").setIcon(event.map.registerIcon("mekanism:LiquidSodium"));
			GasRegistry.getGas("deuterium").setIcon(event.map.registerIcon("mekanism:LiquidDeuterium"));
			GasRegistry.getGas("tritium").setIcon(event.map.registerIcon("mekanism:LiquidTritium"));
			GasRegistry.getGas("fusionFuelDT").setIcon(event.map.registerIcon("mekanism:LiquidDT"));
			GasRegistry.getGas("steam").setIcon(event.map.registerIcon("mekanism:LiquidSteam"));
			GasRegistry.getGas("lithium").setIcon(event.map.registerIcon("mekanism:LiquidLithium"));

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
			FluidRegistry.getFluid("heavywater").setIcons(event.map.registerIcon("mekanism:LiquidHeavyWater"));

			if(RenderPartTransmitter.getInstance() != null)
			{
				RenderPartTransmitter.getInstance().resetDisplayInts();
			}
			
			RenderDynamicTank.resetDisplayInts();
			RenderSalinationController.resetDisplayInts();
			RenderPortableTank.resetDisplayInts();
		}
	}
	
	public static boolean blockIconExists(String texture) //Credit to CoFHCore
	{
		String[] split = texture.split(":");
		texture = split[0] + ":textures/blocks/" + split[1] + ".png";
		
		try {
			Minecraft.getMinecraft().getResourceManager().getAllResources(new ResourceLocation(texture));
			return true;
		} catch(Throwable t) {
			return false;
		}
	}
	
	public static void loadDynamicTextures(IIconRegister register, String name, IIcon[] textures, DefIcon... defaults)
	{
		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			String tex = "mekanism:" + name + simpleSides[side.ordinal()];
			String texOn = tex + "On";
			
			if(blockIconExists(tex))
			{
				textures[side.ordinal()] = register.registerIcon(tex);
				
				if(blockIconExists(texOn))
				{
					textures[side.ordinal()+6] = register.registerIcon(texOn);
				}
				else {
					boolean found = false;
					
					for(DefIcon def : defaults)
					{
						if(def.icons.contains(side.ordinal()+6))
						{
							textures[side.ordinal()+6] = def.defIcon;
							found = true;
						}
					}
					
					if(!found)
					{
						textures[side.ordinal()+6] = register.registerIcon(tex);
					}
				}
			}
			else {
				for(DefIcon def : defaults)
				{
					if(def.icons.contains(side.ordinal()))
					{
						textures[side.ordinal()] = def.defIcon;
					}
					
					if(def.icons.contains(side.ordinal()+6))
					{
						textures[side.ordinal()+6] = def.defIcon;
					}
				}
			}
		}
	}
	
	public static class DefIcon
	{
		public IIcon defIcon;
		
		public List<Integer> icons = new ArrayList<Integer>();
		
		public DefIcon(IIcon icon, int... is)
		{
			defIcon = icon;
			
			for(int i : is)
			{
				icons.add(i);
			}
		}
		
		public static DefIcon getAll(IIcon icon)
		{
			return new DefIcon(icon, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);
		}
		
		public static DefIcon getActivePair(IIcon icon, int... is)
		{
			DefIcon ret = new DefIcon(icon, is);
			
			for(int i : is)
			{
				ret.icons.add(i+6);
			}
			
			return ret;
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
		
		public IIcon[] textures = new IIcon[6];
		
		public boolean[] renderSides = new boolean[] {true, true, true, true, true, true, false};

		public Block baseBlock = Blocks.sand;
		
	    public final void setBlockBounds(double xNeg, double yNeg, double zNeg, double xPos, double yPos, double zPos)
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

		public IIcon getBlockTextureFromSide(int i)
		{
			return textures[i];
		}
		
		public void setTexture(IIcon tex)
		{
			Arrays.fill(textures, tex);
		}
		
		public void setTextures(IIcon down, IIcon up, IIcon north, IIcon south, IIcon west, IIcon east)
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
		
		Tessellator.instance.draw();
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
	
	public static IIcon getColorIcon(EnumColor color)
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
		IIcon icon = item.getItem().getIconIndex(item);
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
    
    public static void prepareItemRender(RenderBlocks renderer, int metadata, Block block)
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
    }
    
    public static void renderCustomItem(RenderBlocks renderer, ItemStack stack)
    {
    	Block block = Block.getBlockFromItem(stack.getItem());
    	
    	if(block instanceof ICustomBlockIcon)
    	{
    		ICustomBlockIcon custom = (ICustomBlockIcon)block;
    		prepareItemRender(renderer, stack.getItemDamage(), Block.getBlockFromItem(stack.getItem()));
    		
            try {
    	        Tessellator tessellator = Tessellator.instance;
    	        tessellator.startDrawingQuads();
    	        tessellator.setNormal(0.0F, -1.0F, 0.0F);
    	        renderer.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, custom.getIcon(stack, 0));
    	        tessellator.draw();
    	        tessellator.startDrawingQuads();
    	        tessellator.setNormal(0.0F, 1.0F, 0.0F);
    	        renderer.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, custom.getIcon(stack, 1));
    	        tessellator.draw();
    	        tessellator.startDrawingQuads();
    	        tessellator.setNormal(0.0F, 0.0F, -1.0F);
    	        renderer.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, custom.getIcon(stack, 2));
    	        tessellator.draw();
    	        tessellator.startDrawingQuads();
    	        tessellator.setNormal(0.0F, 0.0F, 1.0F);
    	        renderer.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, custom.getIcon(stack, 3));
    	        tessellator.draw();
    	        tessellator.startDrawingQuads();
    	        tessellator.setNormal(-1.0F, 0.0F, 0.0F);
    	        renderer.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, custom.getIcon(stack, 4));
    	        tessellator.draw();
    	        tessellator.startDrawingQuads();
    	        tessellator.setNormal(1.0F, 0.0F, 0.0F);
    	        renderer.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, custom.getIcon(stack, 5));
    	        tessellator.draw();
            } catch(Exception e) {}
            
            GL11.glTranslatef(0.5F, 0.5F, 0.5F);
    	}
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
		prepareItemRender(renderer, metadata, block);
		
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
    				if(((TextureMap)obj).getTextureType() == type)
    				{
    					return (TextureMap)obj;
    				}
    			}
    		}
    	} catch(Exception e) {}
    	
    	return null;
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
    
    public static interface ICustomBlockIcon
    {
    	public IIcon getIcon(ItemStack stack, int side);
    }
}
