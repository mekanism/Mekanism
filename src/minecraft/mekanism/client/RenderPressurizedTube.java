package mekanism.client;

import java.util.Arrays;
import java.util.HashMap;

import mekanism.api.EnumGas;
import mekanism.api.GasTransmission;
import mekanism.api.ITubeConnection;
import mekanism.client.MekanismRenderer.DisplayInteger;
import mekanism.client.MekanismRenderer.Model3D;
import mekanism.common.TileEntityPressurizedTube;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderPressurizedTube extends TileEntitySpecialRenderer
{
	private ModelTransmitter model = new ModelTransmitter();
	
	private HashMap<ForgeDirection, HashMap<EnumGas, DisplayInteger>> cachedGasses = new HashMap<ForgeDirection, HashMap<EnumGas, DisplayInteger>>();
	
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
		GL11.glDisable(GL11.GL_CULL_FACE);
		
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
			push();
			
			GL11.glColor4f(1.0F, 1.0F, 1.0F, tileEntity.gasScale);
			bindTextureByName(tileEntity.refGas.texturePath);
			GL11.glTranslatef((float)x, (float)y, (float)z);
			
			for(int i = 0; i < 6; i++)
			{
				if(connectable[i])
				{
					int displayList = getListAndRender(ForgeDirection.getOrientation(i), tileEntity.refGas).display;
					GL11.glCallList(displayList);
				}
			}
			
			int displayList = getListAndRender(ForgeDirection.UNKNOWN, tileEntity.refGas).display;
			GL11.glCallList(displayList);
			
			pop();
		}
	}
	
	private void pop()
	{
		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}
	
	private void push()
	{
		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}
	
	private DisplayInteger getListAndRender(ForgeDirection side, EnumGas type)
	{
		if(cachedGasses.containsKey(side) && cachedGasses.get(side).containsKey(type))
		{
			return cachedGasses.get(side).get(type);
		}
		
		Model3D toReturn = new Model3D();
		toReturn.baseBlock = Block.waterStill;
		toReturn.texture = type.gasIcon;
		
		DisplayInteger display = new DisplayInteger();
		
		if(cachedGasses.containsKey(side))
		{
			cachedGasses.get(side).put(type, display);
		}
		else {
			HashMap<EnumGas, DisplayInteger> map = new HashMap<EnumGas, DisplayInteger>();
			map.put(type, display);
			cachedGasses.put(side, map);
		}
		
		display.display = GLAllocation.generateDisplayLists(1);
		GL11.glNewList(display.display, 4864);
		
		switch(side)
		{
			case UNKNOWN:
			{
				toReturn.minX = 0.3 + offset;
				toReturn.minY = 0.3 + offset;
				toReturn.minZ = 0.3 + offset;
				
				toReturn.maxX = 0.7 - offset;
				toReturn.maxY = 0.7 - offset;
				toReturn.maxZ = 0.7 - offset;
				break;
			}
			case DOWN:
			{
				toReturn.minX = 0.3 + offset;
				toReturn.minY = 0.0;
				toReturn.minZ = 0.3 + offset;
				
				toReturn.maxX = 0.7 - offset;
				toReturn.maxY = 0.3 + offset;
				toReturn.maxZ = 0.7 - offset;
				break;
			}
			case UP:
			{
				toReturn.minX = 0.3 + offset;
				toReturn.minY = 0.3 - offset;
				toReturn.minZ = 0.3 + offset;
				
				toReturn.maxX = 0.7 - offset;
				toReturn.maxY = 1.0;
				toReturn.maxZ = 0.7 - offset;
				break;
			}
			case NORTH:
			{
				toReturn.minX = 0.3 + offset;
				toReturn.minY = 0.3 + offset;
				toReturn.minZ = 0.0;
				
				toReturn.maxX = 0.7 - offset;
				toReturn.maxY = 0.7 - offset;
				toReturn.maxZ = 0.3 + offset;
				break;
			}
			case SOUTH:
			{
				toReturn.minX = 0.3 + offset;
				toReturn.minY = 0.3 + offset;
				toReturn.minZ = 0.7 - offset;
				
				toReturn.maxX = 0.7 - offset;
				toReturn.maxY = 0.7 - offset;
				toReturn.maxZ = 1.0;
				break;
			}
			case WEST:
			{
				toReturn.minX = 0.0;
				toReturn.minY = 0.3 + offset;
				toReturn.minZ = 0.3 + offset;
				
				toReturn.maxX = 0.3 + offset;
				toReturn.maxY = 0.7 - offset;
				toReturn.maxZ = 0.7 - offset;
				break;
			}
			case EAST:
			{
				toReturn.minX = 0.7 - offset;
				toReturn.minY = 0.3 + offset;
				toReturn.minZ = 0.3 + offset;
				
				toReturn.maxX = 1.0;
				toReturn.maxY = 0.7 - offset;
				toReturn.maxZ = 0.7 - offset;
				break;
			}
		}
		
		MekanismRenderer.renderObject(toReturn);
		GL11.glEndList();
		
		return display;
	}
}
