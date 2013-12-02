package mekanism.induction.client.render;

import mekanism.common.Mekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.induction.client.model.ModelEMContractor;
import mekanism.induction.common.tileentity.TileEntityEMContractor;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

public class RenderEMContractor extends TileEntitySpecialRenderer
{
	public static final ModelEMContractor model = new ModelEMContractor(false);
	public static final ModelEMContractor MODEL_SPIN = new ModelEMContractor(true);

	@Override
	@SuppressWarnings("incomplete-switch")
	public void renderTileEntityAt(TileEntity t, double x, double y, double z, float f)
	{
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);
		GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
		GL11.glRotatef(90F, 0.0F, 1.0F, 0.0F);

		switch (((TileEntityEMContractor) t).getFacing())
		{
			case DOWN:
				GL11.glRotatef(180, 0, 0, 1);
				GL11.glTranslatef(0, -2, 0);
				break;
			case UP:
				break;
			case NORTH:
				GL11.glTranslatef(1, 1, 0);
				GL11.glRotatef(90, 0, 0, 1);
				break;
			case SOUTH:
				GL11.glTranslatef(-1, 1, 0);
				GL11.glRotatef(-90, 0, 0, 1);
				break;
			case WEST:
				GL11.glTranslatef(0, 1, 1);
				GL11.glRotatef(-90, 1, 0, 0);
				break;
			case EAST:
				GL11.glTranslatef(0, 1, -1);
				GL11.glRotatef(90, 1, 0, 0);
				break;
		}

		if (((TileEntityEMContractor) t).suck)
		{
			bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ElectromagneticContractor.png"));
		}
		else
		{
			bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ElectromagneticContractorOn.png"));
		}

		if (((TileEntityEMContractor) t).canFunction() && !Mekanism.proxy.isPaused())
		{
			MODEL_SPIN.render(0.0625f);
		}
		else
		{
			model.render(0.0625f);
		}

		GL11.glPopMatrix();
	}
}
