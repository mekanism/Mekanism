package mekanism.generators.client.render;

import mekanism.common.Mekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.model.ModelWindGenerator;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

public class RenderWindGenerator extends TileEntitySpecialRenderer<TileEntityWindGenerator>
{
	private ModelWindGenerator model = new ModelWindGenerator();

	@Override
	public void renderTileEntityAt(TileEntityWindGenerator tileEntity, double x, double y, double z, float partialTick, int destroyStage)
	{
		GlStateManager.pushMatrix();
		GlStateManager.translate((float)x + 0.5F, (float)y + 1.5F, (float)z + 0.5F);
		bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "WindGenerator.png"));

		switch(tileEntity.facing)
		{
			case 2: GlStateManager.rotate(0, 0.0F, 1.0F, 0.0F); break;
			case 3: GlStateManager.rotate(180, 0.0F, 1.0F, 0.0F); break;
			case 4: GlStateManager.rotate(90, 0.0F, 1.0F, 0.0F); break;
			case 5: GlStateManager.rotate(270, 0.0F, 1.0F, 0.0F); break;
		}

		GlStateManager.rotate(180, 0F, 0F, 1F);

		if(!Mekanism.proxy.isPaused() &&
				tileEntity.getWorld().canBlockSeeTheSky(tileEntity.getPos().getX(), tileEntity.getPos().getY()+4, tileEntity.getPos().getZ()))
		{
			tileEntity.angle = (tileEntity.angle+((tileEntity.getPos().getY()+4F)/256F)*8) % 360;
		}

		model.render(0.0625F, tileEntity.angle);
		GlStateManager.popMatrix();
	}
}
