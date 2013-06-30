package mekanism.client;

import java.util.HashMap;

import mekanism.client.MekanismRenderer.Model3D;
import mekanism.common.PipeUtils;
import mekanism.common.TileEntityMechanicalPipe;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.LiquidStack;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderMechanicalPipe extends TileEntitySpecialRenderer
{
	private ModelTransmitter model = new ModelTransmitter();
	
	private HashMap<ForgeDirection, HashMap<LiquidStack, int[]>> cachedLiquids = new HashMap<ForgeDirection, HashMap<LiquidStack, int[]>>();
	
	private static final int stages = 40;
	
	private static final double offset = 0.015;
	
	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTick)
	{
		renderAModelAt((TileEntityMechanicalPipe)tileEntity, x, y, z, partialTick);
	}

	public void renderAModelAt(TileEntityMechanicalPipe tileEntity, double x, double y, double z, float partialTick)
	{
		bindTextureByName("/mods/mekanism/render/MechanicalPipe" + (tileEntity.isActive ? "Active" : "") + ".png");
		GL11.glPushMatrix();
		GL11.glTranslatef((float)x + 0.5F, (float)y + 1.5F, (float)z + 0.5F);
		GL11.glScalef(1.0F, -1F, -1F);
		
		boolean[] connectable = PipeUtils.getConnections(tileEntity);
		
		for(int i = 0; i < 6; i++)
		{
			if(connectable[i])
			{
				model.renderSide(ForgeDirection.getOrientation(i));
			}
		}
		
		model.Center.render(0.0625F);
		GL11.glPopMatrix();
		
		if(tileEntity.liquidScale > 0 && tileEntity.refLiquid != null)
		{
			push();
			
			if(tileEntity.refLiquid.itemID == Block.lavaStill.blockID)
			{
				MekanismRenderer.glowOn();
			}
			
			bindTextureByName(tileEntity.refLiquid.getTextureSheet());
			GL11.glTranslatef((float)x, (float)y, (float)z);
			
			for(int i = 0; i < 6; i++)
			{
				if(connectable[i])
				{
					int[] displayList = getListAndRender(ForgeDirection.getOrientation(i), tileEntity.refLiquid);
					
					if(displayList != null)
					{
						GL11.glCallList(displayList[Math.max(3, (int)((float)tileEntity.liquidScale*(stages-1)))]);
					}
				}
			}
			
			int[] displayList = getListAndRender(ForgeDirection.UNKNOWN, tileEntity.refLiquid);
			
			if(displayList != null)
			{
				GL11.glCallList(displayList[Math.max(3, (int)((float)tileEntity.liquidScale*(stages-1)))]);
			}
			
			if(tileEntity.refLiquid.itemID == Block.lavaStill.blockID)
			{
				MekanismRenderer.glowOff();
			}
			
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
	
	private int[] getListAndRender(ForgeDirection side, LiquidStack stack)
	{
		if(side == null || stack == null || stack.getRenderingIcon() == null)
		{
			return null;
		}
		
		if(cachedLiquids.containsKey(side) && cachedLiquids.get(side).containsKey(stack))
		{
			return cachedLiquids.get(side).get(stack);
		}
		
		Model3D toReturn = new Model3D();
		toReturn.baseBlock = Block.waterStill;
		toReturn.setTexture(stack.getRenderingIcon());
		
		if(stack.itemID < Block.blocksList.length && Block.blocksList[stack.itemID] != null) 
		{
			toReturn.baseBlock = Block.blocksList[stack.itemID];
		}
		
		int[] displays = new int[stages];
		
		if(cachedLiquids.containsKey(side))
		{
			cachedLiquids.get(side).put(stack, displays);
		}
		else {
			HashMap<LiquidStack, int[]> map = new HashMap<LiquidStack, int[]>();
			map.put(stack, displays);
			cachedLiquids.put(side, map);
		}
		
		MekanismRenderer.colorLiquid(stack);
		
		for(int i = 0; i < stages; i++)
		{
			displays[i] = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(displays[i], 4864);
			
			switch(side)
			{
				case UNKNOWN:
				{
					toReturn.minX = 0.3 + offset;
					toReturn.minY = 0.3 + offset;
					toReturn.minZ = 0.3 + offset;
					
					toReturn.maxX = 0.7 - offset;
					toReturn.maxY = 0.3 - offset + ((float)i / (float)100);
					toReturn.maxZ = 0.7 - offset;
					break;
				}
				case DOWN:
				{
					toReturn.minX = 0.5 + offset - ((float)i / (float)100)/2;
					toReturn.minY = 0.0;
					toReturn.minZ = 0.5 + offset - ((float)i / (float)100)/2;
					
					toReturn.maxX = 0.5 - offset + ((float)i / (float)100)/2;
					toReturn.maxY = 0.3 + offset;
					toReturn.maxZ = 0.5 - offset + ((float)i / (float)100)/2;
					break;
				}
				case UP:
				{
					toReturn.minX = 0.5 + offset - ((float)i / (float)100)/2;
					toReturn.minY = 0.3 - offset + ((float)i / (float)100);
					toReturn.minZ = 0.5 + offset - ((float)i / (float)100)/2;
					
					toReturn.maxX = 0.5 - offset + ((float)i / (float)100)/2;
					toReturn.maxY = 1.0;
					toReturn.maxZ = 0.5 - offset + ((float)i / (float)100)/2;
					break;
				}
				case NORTH:
				{
					toReturn.minX = 0.3 + offset;
					toReturn.minY = 0.3 + offset;
					toReturn.minZ = 0.0;
					
					toReturn.maxX = 0.7 - offset;
					toReturn.maxY = 0.3 - offset + ((float)i / (float)100);
					toReturn.maxZ = 0.3 + offset;
					break;
				}
				case SOUTH:
				{
					toReturn.minX = 0.3 + offset;
					toReturn.minY = 0.3 + offset;
					toReturn.minZ = 0.7 - offset;
					
					toReturn.maxX = 0.7 - offset;
					toReturn.maxY = 0.3 - offset + ((float)i / (float)100);
					toReturn.maxZ = 1.0;
					break;
				}
				case WEST:
				{
					toReturn.minX = 0.0;
					toReturn.minY = 0.3 + offset;
					toReturn.minZ = 0.3 + offset;
					
					toReturn.maxX = 0.3 + offset;
					toReturn.maxY = 0.3 - offset + ((float)i / (float)100);
					toReturn.maxZ = 0.7 - offset;
					break;
				}
				case EAST:
				{
					toReturn.minX = 0.7 - offset;
					toReturn.minY = 0.3 + offset;
					toReturn.minZ = 0.3 + offset;
					
					toReturn.maxX = 1.0;
					toReturn.maxY = 0.3 - offset + ((float)i / (float)100);
					toReturn.maxZ = 0.7 - offset;
					break;
				}
			}
			
			MekanismRenderer.renderObject(toReturn);
			GL11.glEndList();
		}
		
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		
		return displays;
	}
}
