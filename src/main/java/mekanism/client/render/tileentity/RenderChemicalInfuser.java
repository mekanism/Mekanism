package mekanism.client.render.tileentity;

import mekanism.client.model.ModelChemicalInfuser;
import mekanism.common.tile.TileEntityChemicalInfuser;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;

import org.lwjgl.opengl.GL11;

public class RenderChemicalInfuser extends TileEntitySpecialRenderer<TileEntityChemicalInfuser>
{
	private ModelChemicalInfuser model = new ModelChemicalInfuser();

	@Override
	public void renderTileEntityAt(TileEntityChemicalInfuser tileEntity, double x, double y, double z, float partialTick, int destroyStage)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef((float)x + 0.5F, (float)y + 1.5F, (float)z + 1.5F);

		bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ChemicalInfuser.png"));

		switch(tileEntity.facing.ordinal()) /*TODO: switch the enum*/
		{
			case 2: GL11.glRotatef(0, 0.0F, 1.0F, 0.0F); break;
			case 3: GL11.glRotatef(180, 0.0F, 1.0F, 0.0F); break;
			case 4: GL11.glRotatef(90, 0.0F, 1.0F, 0.0F); break;
			case 5: GL11.glRotatef(270, 0.0F, 1.0F, 0.0F); break;
		}

		GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
		model.render(0.0625F);

		GL11.glPopMatrix();
	}
}
