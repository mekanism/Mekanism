package mekanism.client;

import java.util.Arrays;
import java.util.HashMap;

import mekanism.client.ObjectRenderer.Model3D;
import mekanism.common.CableUtils;
import mekanism.common.Mekanism;
import mekanism.common.TileEntityUniversalCable;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidStack;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderUniversalCable extends TileEntitySpecialRenderer
{
	private ModelTransmitter model = new ModelTransmitter();
	
	private HashMap<ForgeDirection, int[]> cachedLiquids = new HashMap<ForgeDirection, int[]>();
	
	private static final int stages = 40;
	
	private static final double offset = 0.015;
	
	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTick)
	{
		renderAModelAt((TileEntityUniversalCable)tileEntity, x, y, z, partialTick);
	}

	public void renderAModelAt(TileEntityUniversalCable tileEntity, double x, double y, double z, float partialTick)
	{
		bindTextureByName("/mods/mekanism/render/UniversalCable.png");
		GL11.glPushMatrix();
		GL11.glTranslatef((float)x + 0.5F, (float)y + 1.5F, (float)z + 0.5F);
		GL11.glScalef(1.0F, -1F, -1F);
		
		boolean[] connectable = new boolean[] {false, false, false, false, false, false};

		TileEntity[] connectedAcceptors = CableUtils.getConnectedEnergyAcceptors(tileEntity);
		TileEntity[] connectedCables = CableUtils.getConnectedCables(tileEntity);
		TileEntity[] connectedOutputters = CableUtils.getConnectedOutputters(tileEntity);
		
		for(TileEntity tile : connectedAcceptors)
		{
			int side = Arrays.asList(connectedAcceptors).indexOf(tile);
			
			if(CableUtils.canConnectToAcceptor(ForgeDirection.getOrientation(side), tileEntity))
			{
				connectable[side] = true;
			}
		}
		
		for(TileEntity tile : connectedOutputters)
		{
			if(tile != null)
			{
				int side = Arrays.asList(connectedOutputters).indexOf(tile);
				
				connectable[side] = true;
			}
		}
		
		for(TileEntity tile : connectedCables)
		{
			if(tile != null)
			{
				int side = Arrays.asList(connectedCables).indexOf(tile);
				
				connectable[side] = true;
			}
		}
		
		for(int i = 0; i < 6; i++)
		{
			if(connectable[i])
			{
				model.renderSide(ForgeDirection.getOrientation(i));
			}
		}
		
		model.Center.render(0.0625F);
		GL11.glPopMatrix();
		
		if(tileEntity.energyScale > 0)
		{
			GL11.glPushMatrix();
			GL11.glDisable(2896);
			bindTextureByName("/mods/mekanism/textures/items/LiquidEnergy.png");
			GL11.glTranslatef((float)x, (float)y, (float)z);
			
			for(int i = 0; i < 6; i++)
			{
				if(connectable[i])
				{
					int[] displayList = getListAndRender(ForgeDirection.getOrientation(i), tileEntity.worldObj);
					GL11.glCallList(displayList[Math.max(3, (int)((float)tileEntity.energyScale*(stages-1)))]);
				}
			}
			
			int[] displayList = getListAndRender(ForgeDirection.UNKNOWN, tileEntity.worldObj);
			GL11.glCallList(displayList[Math.max(3, (int)((float)tileEntity.energyScale*(stages-1)))]);
			
			GL11.glEnable(2896);
			GL11.glPopMatrix();
		}
	}
	
	private int[] getListAndRender(ForgeDirection side, World world)
	{
		if(cachedLiquids.containsKey(side))
		{
			return cachedLiquids.get(side);
		}
		
		Model3D toReturn = new Model3D();
		toReturn.baseBlock = Block.waterStill;
		toReturn.texture = Mekanism.LiquidEnergy.getIconFromDamage(0);
		
		int[] displays = new int[stages];
		
		cachedLiquids.put(side, displays);
		
		switch(side)
		{
			case UNKNOWN:
			{
				for(int i = 0; i < stages; i++)
				{
					displays[i] = GLAllocation.generateDisplayLists(1);
					GL11.glNewList(displays[i], 4864);
					
					toReturn.minX = 0.3 + offset;
					toReturn.minY = 0.3 + offset;
					toReturn.minZ = 0.3 + offset;
					
					toReturn.maxX = 0.7 - offset;
					toReturn.maxY = 0.3 - offset + ((float)i / (float)100);
					toReturn.maxZ = 0.7 - offset;
					
					ObjectRenderer.renderObject(toReturn, world, 0, 0, 0, false, true);
					GL11.glEndList();
				}
				
				return displays;
			}
			case DOWN:
			{
				for(int i = 0; i < stages; i++)
				{
					displays[i] = GLAllocation.generateDisplayLists(1);
					GL11.glNewList(displays[i], 4864);
					
					toReturn.minX = 0.5 + offset - ((float)i / (float)100)/2;
					toReturn.minY = 0.0;
					toReturn.minZ = 0.5 + offset - ((float)i / (float)100)/2;
					
					toReturn.maxX = 0.5 - offset + ((float)i / (float)100)/2;
					toReturn.maxY = 0.3 + offset;
					toReturn.maxZ = 0.5 - offset + ((float)i / (float)100)/2;
					
					ObjectRenderer.renderObject(toReturn, world, 0, 0, 0, false, true);
					GL11.glEndList();
				}
				
				return displays;
			}
			case UP:
			{
				for(int i = 0; i < stages; i++)
				{
					displays[i] = GLAllocation.generateDisplayLists(1);
					GL11.glNewList(displays[i], 4864);
					
					toReturn.minX = 0.5 + offset - ((float)i / (float)100)/2;
					toReturn.minY = 0.3 - offset + ((float)i / (float)100);
					toReturn.minZ = 0.5 + offset - ((float)i / (float)100)/2;
					
					toReturn.maxX = 0.5 - offset + ((float)i / (float)100)/2;
					toReturn.maxY = 1.0;
					toReturn.maxZ = 0.5 - offset + ((float)i / (float)100)/2;
					
					ObjectRenderer.renderObject(toReturn, world, 0, 0, 0, false, true);
					GL11.glEndList();
				}
				
				return displays;
			}
			case NORTH:
			{
				for(int i = 0; i < stages; i++)
				{
					displays[i] = GLAllocation.generateDisplayLists(1);
					GL11.glNewList(displays[i], 4864);
					
					toReturn.minX = 0.3 + offset;
					toReturn.minY = 0.3 + offset;
					toReturn.minZ = 0.0;
					
					toReturn.maxX = 0.7 - offset;
					toReturn.maxY = 0.3 - offset + ((float)i / (float)100);
					toReturn.maxZ = 0.3 + offset;
					
					ObjectRenderer.renderObject(toReturn, world, 0, 0, 0, false, true);
					GL11.glEndList();
				}
				
				return displays;
			}
			case SOUTH:
			{
				for(int i = 0; i < stages; i++)
				{
					displays[i] = GLAllocation.generateDisplayLists(1);
					GL11.glNewList(displays[i], 4864);
					
					toReturn.minX = 0.3 + offset;
					toReturn.minY = 0.3 + offset;
					toReturn.minZ = 0.7 - offset;
					
					toReturn.maxX = 0.7 - offset;
					toReturn.maxY = 0.3 - offset + ((float)i / (float)100);
					toReturn.maxZ = 1.0;
					
					ObjectRenderer.renderObject(toReturn, world, 0, 0, 0, false, true);
					GL11.glEndList();
				}
				
				return displays;
			}
			case WEST:
			{
				for(int i = 0; i < stages; i++)
				{
					displays[i] = GLAllocation.generateDisplayLists(1);
					GL11.glNewList(displays[i], 4864);
					
					toReturn.minX = 0.0;
					toReturn.minY = 0.3 + offset;
					toReturn.minZ = 0.3 + offset;
					
					toReturn.maxX = 0.3 + offset;
					toReturn.maxY = 0.3 - offset + ((float)i / (float)100);
					toReturn.maxZ = 0.7 - offset;
					
					ObjectRenderer.renderObject(toReturn, world, 0, 0, 0, false, true);
					GL11.glEndList();
				}
				
				return displays;
			}
			case EAST:
			{
				for(int i = 0; i < stages; i++)
				{
					displays[i] = GLAllocation.generateDisplayLists(1);
					GL11.glNewList(displays[i], 4864);
					
					toReturn.minX = 0.7 - offset;
					toReturn.minY = 0.3 + offset;
					toReturn.minZ = 0.3 + offset;
					
					toReturn.maxX = 1.0;
					toReturn.maxY = 0.3 - offset + ((float)i / (float)100);
					toReturn.maxZ = 0.7 - offset;
					
					ObjectRenderer.renderObject(toReturn, world, 0, 0, 0, false, true);
					GL11.glEndList();
				}
				
				return displays;
			}
		}
		
		return null;
	}
}
