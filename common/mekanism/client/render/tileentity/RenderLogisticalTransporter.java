package mekanism.client.render.tileentity;

import mekanism.client.model.ModelTransmitter;
import mekanism.common.tileentity.TileEntityLogisticalTransporter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

public class RenderLogisticalTransporter extends TileEntitySpecialRenderer
{
	private ModelTransmitter model = new ModelTransmitter();
	
	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTick)
	{
		renderAModelAt((TileEntityLogisticalTransporter)tileEntity, x, y, z, partialTick);
	}

	@SuppressWarnings("incomplete-switch")
	public void renderAModelAt(TileEntityLogisticalTransporter tileEntity, double x, double y, double z, float partialTick)
	{
		func_110628_a(MekanismUtils.getResource(ResourceType.RENDER, "LogisticalTransporter" + (tileEntity.isActive ? "Active" : "") + ".png"));
		GL11.glPushMatrix();
		GL11.glTranslatef((float)x + 0.5F, (float)y + 1.5F, (float)z + 0.5F);
		GL11.glScalef(1.0F, -1F, -1F);
		GL11.glDisable(GL11.GL_CULL_FACE);
		
		boolean[] connectable = TransporterUtils.getConnections(tileEntity);
		
		model.renderCenter(connectable);
		
		for(int i = 0; i < 6; i++)
		{
			model.renderSide(ForgeDirection.getOrientation(i), connectable[i]);
		}
		
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glPopMatrix();
	}
}
