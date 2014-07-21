package mekanism.client.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mekanism.api.EnumColor;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.tile.TileEntityDigitalMiner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

public final class MinerVisualRenderer 
{
	private static Minecraft mc = Minecraft.getMinecraft();
	
	private static Map<MinerRenderData, DisplayInteger> cachedVisuals = new HashMap<MinerRenderData, DisplayInteger>();
	
	private static final double offset = 0.01;
	
	public static void render(TileEntityDigitalMiner miner)
	{
		GL11.glPushMatrix();
		GL11.glTranslated(getX(miner.xCoord), getY(miner.yCoord), getZ(miner.zCoord));
		MekanismRenderer.blendOn();
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.4F);
		mc.getTextureManager().bindTexture(MekanismRenderer.getBlocksTexture());
		getList(new MinerRenderData(miner)).render();
		MekanismRenderer.blendOff();
		GL11.glPopMatrix();
	}
	
	private static DisplayInteger getList(MinerRenderData data)
	{
		if(cachedVisuals.containsKey(data))
		{
			return cachedVisuals.get(data);
		}
		
		DisplayInteger display = DisplayInteger.createAndStart();
		cachedVisuals.put(data, display);
		
		List<Model3D> models = new ArrayList<Model3D>();
		
		if(data.radius == 0)
		{
			//Single vertical render
			models.add(createModel(-offset, (data.minY-data.yCoord)-offset, -offset, 1+offset, (data.maxY-data.yCoord)+1+offset, 1+offset));
		}
		else if(data.maxY-data.minY == 0)
		{
			//Flat panel render
			models.add(createModel(-data.radius-offset, (data.minY-data.yCoord)-offset, -data.radius-offset, data.radius+1+offset, (data.maxY-data.yCoord)+1+offset, data.radius+1+offset));
		}
		else {
			//Complete render: Top face
			models.add(createModel(-data.radius-offset+1, (data.maxY-data.yCoord)-offset, -data.radius-offset+1, data.radius+offset, (data.maxY-data.yCoord)+1+offset, data.radius+offset, ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.EAST, ForgeDirection.WEST));
			//Bottom face
			models.add(createModel(-data.radius-offset+1, (data.minY-data.yCoord)-offset, -data.radius-offset+1, data.radius+offset, (data.minY-data.yCoord)+1+offset, data.radius+offset, ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.EAST, ForgeDirection.WEST));
			
			//Northwest strip
			models.add(createModel(-data.radius-offset, (data.minY-data.yCoord)-offset, -data.radius-offset, -data.radius+1+offset, (data.maxY-data.yCoord)+1+offset, -data.radius+1+offset, ForgeDirection.SOUTH, ForgeDirection.EAST));
			//Northeast strip
			models.add(createModel(data.radius-offset, (data.minY-data.yCoord)-offset, -data.radius-offset, data.radius+1+offset, (data.maxY-data.yCoord)+1+offset, -data.radius+1+offset, ForgeDirection.SOUTH, ForgeDirection.WEST));
			//Southwest strip
			models.add(createModel(-data.radius-offset, (data.minY-data.yCoord)-offset, data.radius-offset, -data.radius+1+offset, (data.maxY-data.yCoord)+1+offset, data.radius+1+offset, ForgeDirection.NORTH, ForgeDirection.EAST));
			//Southeast strip
			models.add(createModel(data.radius-offset, (data.minY-data.yCoord)-offset, data.radius-offset, data.radius+1+offset, (data.maxY-data.yCoord)+1+offset, data.radius+1+offset, ForgeDirection.NORTH, ForgeDirection.WEST));
			
			//North face
			models.add(createModel(-data.radius-offset+1, (data.minY-data.yCoord)-offset, -data.radius-offset, data.radius+offset, (data.maxY-data.yCoord)+1+offset, -data.radius+1+offset, ForgeDirection.EAST, ForgeDirection.WEST));
			//South face
			models.add(createModel(-data.radius-offset+1, (data.minY-data.yCoord)-offset, data.radius-offset, data.radius+offset, (data.maxY-data.yCoord)+1+offset, data.radius+1+offset, ForgeDirection.EAST, ForgeDirection.WEST));
			//West face
			models.add(createModel(-data.radius-offset, (data.minY-data.yCoord)-offset, -data.radius-offset+1, -data.radius+1+offset, (data.maxY-data.yCoord)+1+offset, data.radius+offset, ForgeDirection.NORTH, ForgeDirection.SOUTH));
			//East face
			models.add(createModel(data.radius-offset, (data.minY-data.yCoord)-offset, -data.radius-offset+1, data.radius+1+offset, (data.maxY-data.yCoord)+1+offset, data.radius+offset, ForgeDirection.NORTH, ForgeDirection.SOUTH));
		}
		
		for(Model3D model : models)
		{
			MekanismRenderer.renderObject(model);
		}
		
		display.endList();
		
		return display;
	}
	
	private static Model3D createModel(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, ForgeDirection... ignoreDirs)
	{
		Model3D toReturn = new Model3D();
		
		for(ForgeDirection dir : ignoreDirs)
		{
			toReturn.setSideRender(dir, false);
		}
		
		toReturn.setBlockBounds(minX, minY, minZ, maxX, maxY, maxZ);
		toReturn.baseBlock = Blocks.water;
		toReturn.setTexture(MekanismRenderer.getColorIcon(EnumColor.WHITE));
		
		return toReturn;
	}
	
	private static double getX(int x)
	{
		return x - TileEntityRendererDispatcher.staticPlayerX;
	}

	private static double getY(int y)
	{
		return y - TileEntityRendererDispatcher.staticPlayerY;
	}

	private static double getZ(int z)
	{
		return z - TileEntityRendererDispatcher.staticPlayerZ;
	}
	
	public static class MinerRenderData
	{
		public int minY;
		public int maxY;
		public int radius;
		public int yCoord;
		
		public MinerRenderData(int min, int max, int rad, int y)
		{
			minY = min;
			maxY = max;
			radius = rad;
			yCoord = y;
		}
		
		public MinerRenderData(TileEntityDigitalMiner miner)
		{
			this(miner.minY, miner.maxY, miner.radius, miner.yCoord);
		}
		
		@Override
		public boolean equals(Object data)
		{
			return data instanceof MinerRenderData && super.equals(data) && ((MinerRenderData)data).minY == minY && 
					((MinerRenderData)data).maxY == maxY && ((MinerRenderData)data).radius == radius && 
					((MinerRenderData)data).yCoord == yCoord;
		}

		@Override
		public int hashCode()
		{
			int code = 1;
			code = 31 * code + super.hashCode();
			code = 31 * code + minY;
			code = 31 * code + maxY;
			code = 31 * code + radius;
			code = 31 * code + yCoord;
			return code;
		}
	}
}
