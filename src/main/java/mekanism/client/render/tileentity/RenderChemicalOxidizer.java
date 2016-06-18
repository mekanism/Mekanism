package mekanism.client.render.tileentity;

import java.util.HashMap;
import java.util.Map;

import mekanism.api.gas.Gas;
import mekanism.client.model.ModelChemicalOxidizer;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.common.tile.TileEntityChemicalOxidizer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

public class RenderChemicalOxidizer extends TileEntitySpecialRenderer
{
	private ModelChemicalOxidizer model = new ModelChemicalOxidizer();

	private static final double offset = 0.001;

	private Map<ForgeDirection, HashMap<Gas, DisplayInteger>> cachedGasses = new HashMap<ForgeDirection, HashMap<Gas, DisplayInteger>>();

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTick)
	{
		renderAModelAt((TileEntityChemicalOxidizer)tileEntity, x, y, z, partialTick);
	}

	private void renderAModelAt(TileEntityChemicalOxidizer tileEntity, double x, double y, double z, float partialTick)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef((float)x + 0.5F, (float)y + 1.5F, (float)z + 0.5F);

		bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ChemicalOxidizer.png"));

		switch(tileEntity.facing)
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
