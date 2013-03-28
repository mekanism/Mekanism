package mekanism.client;

import java.util.Arrays;

import mekanism.common.MekanismUtils;
import mekanism.common.TileEntityUniversalCable;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

public class RenderUniversalCable extends TileEntitySpecialRenderer
{
	private ModelTransmitter model;

	public RenderUniversalCable()
	{
		model = new ModelTransmitter();
	}

	public void renderAModelAt(TileEntityUniversalCable tileEntity, double d, double d1, double d2, float f)
	{
		bindTextureByName("/mods/mekanism/render/UniversalCable.png");
		GL11.glPushMatrix();
		GL11.glTranslatef((float) d + 0.5F, (float) d1 + 1.5F, (float) d2 + 0.5F);
		GL11.glScalef(1.0F, -1F, -1F);
		
		boolean[] connectable = new boolean[] {false, false, false, false, false, false};

		TileEntity[] connectedAcceptors = MekanismUtils.getConnectedEnergyAcceptors(tileEntity);
		TileEntity[] connectedCables = MekanismUtils.getConnectedCables(tileEntity);
		TileEntity[] connectedOutputters = MekanismUtils.getConnectedOutputters(tileEntity);
		
		for(TileEntity tile : connectedAcceptors)
		{
			int side = Arrays.asList(connectedAcceptors).indexOf(tile);
			
			if(MekanismUtils.canCableConnect(ForgeDirection.getOrientation(side), tileEntity))
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
	}

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double var2, double var4, double var6, float var8)
	{
		renderAModelAt((TileEntityUniversalCable)tileEntity, var2, var4, var6, var8);
	}
}
