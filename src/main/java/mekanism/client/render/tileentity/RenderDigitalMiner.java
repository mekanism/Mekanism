package mekanism.client.render.tileentity;

import mekanism.client.model.ModelDigitalMiner;
import mekanism.client.render.MinerVisualRenderer;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderDigitalMiner extends TileEntitySpecialRenderer<TileEntityDigitalMiner>
{
	private ModelDigitalMiner model = new ModelDigitalMiner();
	
	@Override
	public void render(TileEntityDigitalMiner tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha)
	{
		GlStateManager.pushMatrix();
		GlStateManager.translate((float)x + 0.5F, (float)y + 1.5F, (float)z + 0.5F);

		bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "DigitalMiner.png"));

		switch(tileEntity.facing.ordinal())
		{
			case 2: 
				GL11.glRotatef(0, 0.0F, 1.0F, 0.0F);
				GL11.glTranslatef(0.0F, 0.0F, -1.0F);
				break;
			case 3: 
				GL11.glRotatef(180, 0.0F, 1.0F, 0.0F);
				GL11.glTranslatef(0.0F, 0.0F, -1.0F);
				break;
			case 4: 
				GL11.glRotatef(90, 0.0F, 1.0F, 0.0F);
				GL11.glTranslatef(0.0F, 0.0F, -1.0F);
				break;
			case 5: 
				GL11.glRotatef(270, 0.0F, 1.0F, 0.0F);
				GL11.glTranslatef(0.0F, 0.0F, -1.0F);
				break;
		}

		GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
		model.render(0.0625F, tileEntity.isActive, rendererDispatcher.renderEngine, true);
		GlStateManager.popMatrix();
		
		if(tileEntity.clientRendering)
		{
			MinerVisualRenderer.render(tileEntity);
		}
	}
}
