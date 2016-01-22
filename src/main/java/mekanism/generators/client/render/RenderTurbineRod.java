package mekanism.generators.client.render;

import mekanism.common.Mekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.model.ModelTurbine;
import mekanism.generators.common.tile.turbine.TileEntityRotationalComplex;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRod;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderTurbineRod extends TileEntitySpecialRenderer
{
	private ModelTurbine model = new ModelTurbine();
	
	private static final float BASE_SPEED = 512F;

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTick)
	{
		renderAModelAt((TileEntityTurbineRod)tileEntity, x, y, z, partialTick);
	}

	private void renderAModelAt(TileEntityTurbineRod tileEntity, double x, double y, double z, float partialTick)
	{
		GL11.glPushMatrix();
		
		bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "Turbine.png"));
		
		int baseIndex = tileEntity.clientIndex*2;
		float rotateSpeed = 0.0F;
		
		if(tileEntity.multiblockUUID != null && TileEntityRotationalComplex.clientRotationMap.containsKey(tileEntity.multiblockUUID))
		{
			rotateSpeed = TileEntityRotationalComplex.clientRotationMap.get(tileEntity.multiblockUUID);
		}
		
		if(!Mekanism.proxy.isPaused())
		{
			tileEntity.rotationLower = (tileEntity.rotationLower + rotateSpeed*BASE_SPEED*(1F/(float)(baseIndex+1))) % 360;
			tileEntity.rotationUpper = (tileEntity.rotationUpper + rotateSpeed*BASE_SPEED*(1F/(float)(baseIndex+2))) % 360;
		}
		
		if(tileEntity.getHousedBlades() > 0)
		{
			GL11.glPushMatrix();
			GL11.glTranslated(x + 0.5, y - 1, z + 0.5);
			GL11.glRotatef(tileEntity.rotationLower, 0.0F, 1.0F, 0.0F);
			model.render(0.0625F, baseIndex);
			GL11.glPopMatrix();
		}
		
		if(tileEntity.getHousedBlades() == 2)
		{
			GL11.glPushMatrix();
			GL11.glTranslated(x + 0.5, y - 0.5, z + 0.5);
			GL11.glRotatef(tileEntity.rotationUpper, 0.0F, 1.0F, 0.0F);
			model.render(0.0625F, baseIndex+1);
			GL11.glPopMatrix();
		}
		
		GL11.glPopMatrix();
	}
}
