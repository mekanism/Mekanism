package mekanism.client.render.tileentity;

import java.util.HashMap;
import java.util.Map;

import mekanism.client.model.ModelTransmitter;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.BooleanArray;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.Mekanism;
import mekanism.common.tileentity.TileEntityUniversalCable;
import mekanism.common.util.CableUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderUniversalCable extends TileEntitySpecialRenderer
{
	private static ModelTransmitter model = new ModelTransmitter();
	
	private static Icon renderIcon = MekanismRenderer.getTextureMap(1).registerIcon("mekanism:LiquidEnergy");
	
	private static Model3D[] energy = null;
	
	private static Map<ForgeDirection, DisplayInteger> sideDisplayLists = new HashMap<ForgeDirection, DisplayInteger>();
	private static Map<BooleanArray, DisplayInteger> centerDisplayLists = new HashMap<BooleanArray, DisplayInteger>();
	
	private static final double offset = 0.015;
	
	private boolean[] connectable;
	
	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTick)
	{
		renderAModelAt((TileEntityUniversalCable)tileEntity, x, y, z, partialTick);
	}

	public void renderAModelAt(TileEntityUniversalCable tileEntity, double x, double y, double z, float partialTick)
	{
		func_110628_a(MekanismUtils.getResource(ResourceType.RENDER, "UniversalCable.png"));
		GL11.glPushMatrix();
		GL11.glTranslatef((float)x + 0.5F, (float)y + 1.5F, (float)z + 0.5F);
		GL11.glScalef(1.0F, -1F, -1F);
		GL11.glDisable(GL11.GL_CULL_FACE);
		
		connectable = CableUtils.getConnections(tileEntity);
		
		model.renderCenter(connectable);
		for(int i = 0; i < 6; i++)
		{
			model.renderSide(ForgeDirection.getOrientation(i), connectable[i]);
		}
		
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glPopMatrix();

		if(tileEntity.getEnergyScale() <= 0 || !Mekanism.fancyUniversalCableRender)
		{
			return;
		}
		
		push();
		
		MekanismRenderer.glowOn();
		GL11.glColor4f(1.F, 1.F, 1.F, tileEntity.getEnergyScale());
		func_110628_a(MekanismUtils.getResource(ResourceType.TEXTURE_ITEMS, "LiquidEnergy.png"));
		GL11.glTranslatef((float)x, (float)y, (float)z);
		
		if(energy == null)
		{
			energy = assignEnergy();
		}
		
		for(int i = 0; i < 6; i++)
		{
			if(connectable[i])
			{
				renderEnergy(ForgeDirection.getOrientation(i));
			}
		}
		
		renderEnergy(ForgeDirection.UNKNOWN);
		
		MekanismRenderer.glowOff();
		pop();
	}
	
	private void renderEnergy(ForgeDirection side)
	{
		DisplayInteger list = getDisplayList(side);
		list.render();
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
	
	private static Model3D[] assignEnergy()
	{
		Model3D[] energyArray = new Model3D[7];
		Model3D centerModel = new Model3D();
		centerModel.baseBlock = Block.waterStill;
		centerModel.setTexture(renderIcon);
		
		centerModel.minX = 0.3 + offset;
		centerModel.minY = 0.3 + offset;
		centerModel.minZ = 0.3 + offset;
		
		centerModel.maxX = 0.7 - offset;
		centerModel.maxY = 0.7 - offset;
		centerModel.maxZ = 0.7 - offset;
		
		energyArray[ForgeDirection.UNKNOWN.ordinal()] = centerModel;
		
		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			Model3D toReturn = new Model3D();
			toReturn.baseBlock = Block.waterStill;
			toReturn.setTexture(renderIcon);
			
			toReturn.setSideRender(side, false);
			toReturn.setSideRender(side.getOpposite(), false);
			
			switch(side)
			{
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
					toReturn.minY = 0.7 - offset;
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
			
			energyArray[side.ordinal()] = toReturn;
		}
		return energyArray;
	}
	
	private DisplayInteger getDisplayList(ForgeDirection side)
	{
		
		DisplayInteger newDisplayList;
		
		Model3D toRender = energy[side.ordinal()];
		if(side == ForgeDirection.UNKNOWN)
		{
			newDisplayList = centerDisplayLists.get(new BooleanArray(connectable));
			if(newDisplayList != null)
			{
				return newDisplayList;
			}
			
			for(ForgeDirection face : ForgeDirection.VALID_DIRECTIONS)
			{
				toRender.setSideRender(face, !connectable[face.ordinal()]);
			}
			
			newDisplayList = DisplayInteger.createAndStart();

			MekanismRenderer.renderObject(toRender);
			
			newDisplayList.endList();
			centerDisplayLists.put(new BooleanArray(connectable), newDisplayList);
		}
		else {
			newDisplayList = sideDisplayLists.get(side);
			if(newDisplayList != null)
			{
				return newDisplayList;
			}
			
			newDisplayList = DisplayInteger.createAndStart();
			
			MekanismRenderer.renderObject(toRender);
			
			newDisplayList.endList();
			sideDisplayLists.put(side, newDisplayList);
		}
		return newDisplayList;
	}
}
