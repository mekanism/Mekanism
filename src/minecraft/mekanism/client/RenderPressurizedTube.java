package mekanism.client;

import java.util.Arrays;
import java.util.HashMap;

import mekanism.api.EnumGas;
import mekanism.api.GasTransmission;
import mekanism.api.ITubeConnection;
import mekanism.client.ObjectRenderer.Model3D;
import mekanism.common.TileEntityPressurizedTube;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderPressurizedTube extends TileEntitySpecialRenderer
{
	private ModelTransmitter model = new ModelTransmitter();
	
	private HashMap<ForgeDirection, HashMap<EnumGas, int[]>> cachedGasses = new HashMap<ForgeDirection, HashMap<EnumGas, int[]>>();
	
	private static final int stages = 40;
	
	private static final double offset = 0.015;
	
	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTick)
	{
		renderAModelAt((TileEntityPressurizedTube)tileEntity, x, y, z, partialTick);
	}

	public void renderAModelAt(TileEntityPressurizedTube tileEntity, double x, double y, double z, float partialTick)
	{
		bindTextureByName("/mods/mekanism/render/PressurizedTube.png");
		GL11.glPushMatrix();
		GL11.glTranslatef((float)x + 0.5F, (float)y + 1.5F, (float)z + 0.5F);
		GL11.glScalef(1.0F, -1F, -1F);
		
		boolean[] connectable = new boolean[] {false, false, false, false, false, false};
		
		ITubeConnection[] connections = GasTransmission.getConnections(tileEntity);
		
		for(ITubeConnection connection : connections)
		{
			if(connection != null)
			{
				int side = Arrays.asList(connections).indexOf(connection);
				
				if(connection.canTubeConnect(ForgeDirection.getOrientation(side).getOpposite()))
				{
					connectable[side] = true;
				}
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
	
		if(tileEntity.gasScale > 0 && tileEntity.refGas != null && tileEntity.refGas.hasTexture())
		{
			GL11.glPushMatrix();
			GL11.glDisable(2896);
			bindTextureByName(tileEntity.refGas.texturePath);
			GL11.glTranslatef((float)x, (float)y, (float)z);
			
			for(int i = 0; i < 6; i++)
			{
				if(connectable[i])
				{
					int[] displayList = getListAndRender(ForgeDirection.getOrientation(i), tileEntity.refGas, tileEntity.worldObj);
					GL11.glCallList(displayList[Math.max(3, (int)((float)tileEntity.gasScale*(stages-1)))]);
				}
			}
			
			int[] displayList = getListAndRender(ForgeDirection.UNKNOWN, tileEntity.refGas, tileEntity.worldObj);
			GL11.glCallList(displayList[Math.max(3, (int)((float)tileEntity.gasScale*(stages-1)))]);
			
			GL11.glEnable(2896);
			GL11.glPopMatrix();
		}
	}
	
	private int[] getListAndRender(ForgeDirection side, EnumGas type, World world)
	{
		if(cachedGasses.containsKey(side) && cachedGasses.get(side).containsKey(type))
		{
			return cachedGasses.get(side).get(type);
		}
		
		Model3D toReturn = new Model3D();
		toReturn.baseBlock = Block.waterStill;
		toReturn.texture = type.gasItem.getIconFromDamage(0);
		
		int[] displays = new int[stages];
		
		if(cachedGasses.containsKey(side))
		{
			cachedGasses.get(side).put(type, displays);
		}
		else {
			HashMap<EnumGas, int[]> map = new HashMap<EnumGas, int[]>();
			map.put(type, displays);
			cachedGasses.put(side, map);
		}
		
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
