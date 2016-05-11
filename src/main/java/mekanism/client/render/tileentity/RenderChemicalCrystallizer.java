package mekanism.client.render.tileentity;

import mekanism.client.model.ModelChemicalCrystallizer;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.tile.TileEntityChemicalCrystallizer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderChemicalCrystallizer extends TileEntitySpecialRenderer<TileEntityChemicalCrystallizer>
{
	private ModelChemicalCrystallizer model = new ModelChemicalCrystallizer();

	@Override
	public void renderTileEntityAt(TileEntityChemicalCrystallizer tileEntity, double x, double y, double z, float partialTick, int destroyStage)
	{
		GlStateManager.pushMatrix();
		GlStateManager.translate((float)x + 0.5F, (float)y + 1.5F, (float)z + 1.5F);
		bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ChemicalCrystallizer.png"));

		switch(tileEntity.facing.ordinal()) /*TODO: switch the enum*/
		{
			case 2: GlStateManager.rotate(0, 0.0F, 1.0F, 0.0F); break;
			case 3: GlStateManager.rotate(180, 0.0F, 1.0F, 0.0F); break;
			case 4: GlStateManager.rotate(90, 0.0F, 1.0F, 0.0F); break;
			case 5: GlStateManager.rotate(270, 0.0F, 1.0F, 0.0F); break;
		}

		if(tileEntity.isActive)
		{
			tileEntity.spinSpeed = Math.min(1, tileEntity.spinSpeed+0.01F);
		}
		else {
			tileEntity.spinSpeed = Math.max(0, tileEntity.spinSpeed-0.02F);
		}

		tileEntity.spin = (tileEntity.spin + (tileEntity.spinSpeed*0.1F)) % 1;

		GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
		model.render(0.0625F);
		GlStateManager.popMatrix();

		MekanismRenderer.machineRenderer.renderTileEntityAt(tileEntity, x, y, z, partialTick, destroyStage);
	}
}
