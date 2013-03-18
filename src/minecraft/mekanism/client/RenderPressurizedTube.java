package mekanism.client;

import java.util.Arrays;

import org.lwjgl.opengl.GL11;

import net.minecraftforge.common.ForgeDirection;

import mekanism.api.GasTransmission;
import mekanism.api.IGasAcceptor;
import mekanism.api.ITubeConnection;
import mekanism.common.MekanismUtils;
import mekanism.common.TileEntityPressurizedTube;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

public class RenderPressurizedTube extends TileEntitySpecialRenderer
{
	private ModelPressurizedTube model;

	public RenderPressurizedTube()
	{
		model = new ModelPressurizedTube();
	}

	public void renderAModelAt(TileEntityPressurizedTube tileEntity, double d, double d1, double d2, float f)
	{
		bindTextureByName("/mods/mekanism/render/PressurizedTube.png");
		GL11.glPushMatrix();
		GL11.glTranslatef((float) d + 0.5F, (float) d1 + 1.5F, (float) d2 + 0.5F);
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

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double var2, double var4, double var6, float var8)
	{
		renderAModelAt((TileEntityPressurizedTube)tileEntity, var2, var4, var6, var8);
	}
}
