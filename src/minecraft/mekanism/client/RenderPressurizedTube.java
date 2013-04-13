package mekanism.client;

import java.util.Arrays;

import org.lwjgl.opengl.GL11;

import net.minecraftforge.common.ForgeDirection;

import mekanism.api.GasTransmission;
import mekanism.api.ITubeConnection;
import mekanism.common.TileEntityPressurizedTube;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

public class RenderPressurizedTube extends TileEntitySpecialRenderer
{
	private ModelTransmitter model;

	public RenderPressurizedTube()
	{
		model = new ModelTransmitter();
	}
	
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
		
		ITubeConnection[] connections = GasTransmission.getConnections(tileEntity);
		
		for(ITubeConnection connection : connections)
		{
			if(connection != null)
			{
				int side = Arrays.asList(connections).indexOf(connection);
				
				if(connection.canTubeConnect(ForgeDirection.getOrientation(side).getOpposite()))
				{
					model.renderSide(ForgeDirection.getOrientation(side));
				}
			}
		}
		
		model.Center.render(0.0625F);
		GL11.glPopMatrix();
	}
}
