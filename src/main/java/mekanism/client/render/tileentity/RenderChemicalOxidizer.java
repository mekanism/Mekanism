package mekanism.client.render.tileentity;

import java.util.HashMap;
import java.util.Map;

import mekanism.api.gas.Gas;
import mekanism.client.model.ModelChemicalOxidizer;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.common.tile.TileEntityChemicalOxidizer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;

import org.lwjgl.opengl.GL11;

public class RenderChemicalOxidizer extends TileEntitySpecialRenderer<TileEntityChemicalOxidizer>
{
	private ModelChemicalOxidizer model = new ModelChemicalOxidizer();

	private static final double offset = 0.001;

	private Map<EnumFacing, HashMap<Gas, DisplayInteger>> cachedGasses = new HashMap<EnumFacing, HashMap<Gas, DisplayInteger>>();

	@Override
	public void renderTileEntityAt(TileEntityChemicalOxidizer tileEntity, double x, double y, double z, float partialTick, int destroyStage)
	{
		GlStateManager.pushMatrix();
		GlStateManager.translate((float)x + 0.5F, (float)y + 1.5F, (float)z + 1.5F);

		bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ChemicalOxidizer.png"));

		switch(tileEntity.facing.ordinal()) /*TODO: switch the enum*/
		{
			case 2: GlStateManager.rotate(0, 0.0F, 1.0F, 0.0F); break;
			case 3: GlStateManager.rotate(180, 0.0F, 1.0F, 0.0F); break;
			case 4: GlStateManager.rotate(90, 0.0F, 1.0F, 0.0F); break;
			case 5: GlStateManager.rotate(270, 0.0F, 1.0F, 0.0F); break;
		}

		GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);

		model.render(0.0625F);

		GlStateManager.popMatrix();
	}
}
