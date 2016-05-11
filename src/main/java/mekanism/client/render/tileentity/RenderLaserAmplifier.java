package mekanism.client.render.tileentity;

import mekanism.client.model.ModelLaserAmplifier;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.tile.TileEntityLaserAmplifier;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderLaserAmplifier extends TileEntitySpecialRenderer<TileEntityLaserAmplifier>
{
	private ModelLaserAmplifier model = new ModelLaserAmplifier();

	@Override
	public void renderTileEntityAt(TileEntityLaserAmplifier tileEntity, double x, double y, double z, float partialTick, int destroyStage)
	{
		GlStateManager.pushMatrix();
		GlStateManager.translate((float)x + 0.5F, (float)y + 1.5F, (float)z + 1.5F);

		bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "LaserAmplifier.png"));

		switch(tileEntity.facing.ordinal()) /*TODO: switch the enum*/
		{
			case 0:
				GlStateManager.translate(0.0F, -2.0F, 0.0F);
				GlStateManager.rotate(180F, 1.0F, 0.0F, 0.0F);
				break;
			case 5:
				GlStateManager.translate(0.0F, -1.0F, 0.0F);
				GlStateManager.translate(1.0F, 0.0F, 0.0F);
				GlStateManager.rotate(90, 0.0F, 0.0F, -1.0F);
				break;
			case 4:
				GlStateManager.translate(0.0F, -1.0F, 0.0F);
				GlStateManager.translate(-1.0F, 0.0F, 0.0F);
				GlStateManager.rotate(90, 0.0F, 0.0F, 1.0F);
				break;
			case 2:
				GlStateManager.translate(0.0F, -1.0F, 0.0F);
				GlStateManager.translate(0.0F, 0.0F, -1.0F);
				GlStateManager.rotate(90, -1.0F, 0.0F, 0.0F);
				break;
			case 3:
				GlStateManager.translate(0.0F, -1.0F, 0.0F);
				GlStateManager.translate(0.0F, 0.0F, 1.0F);
				GlStateManager.rotate(90, 1.0F, 0.0F, 0.0F);
				break;
		}

		GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
		MekanismRenderer.blendOn();
		model.render(0.0625F);
		MekanismRenderer.blendOff();
		GlStateManager.popMatrix();
	}
}
