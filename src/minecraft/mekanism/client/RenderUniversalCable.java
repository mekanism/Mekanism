package mekanism.client;

import java.util.Arrays;

import mekanism.common.CableUtils;
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
	}
}
