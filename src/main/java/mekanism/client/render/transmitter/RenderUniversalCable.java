package mekanism.client.render.transmitter;

import mcmultipart.multipart.IMultipart;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.ColourRGBA;
import mekanism.common.multipart.PartUniversalCable;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.EnumFacing;

import org.lwjgl.opengl.GL11;

public class RenderUniversalCable extends RenderTransmitterBase
{
	public RenderUniversalCable()
	{
		super();
	}
	
	@Override
	public void renderMultipartAt(IMultipart multipart, double x, double y, double z, float partialTick, int destroyStage) 
	{
		PartUniversalCable cable = (PartUniversalCable)multipart;
		
		if(cable.currentPower == 0)
		{
			return;
		}

		push();
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldRenderer = tessellator.getWorldRenderer();
		GL11.glTranslated(x + 0.5, y+0.5, z + 0.5);

		for(EnumFacing side : EnumFacing.VALUES)
		{
			renderEnergySide(worldRenderer, side, cable);
		}

		MekanismRenderer.glowOn();

		tessellator.draw();
		isDrawing = false;

		MekanismRenderer.glowOff();
		pop();
	}
	
	public void renderEnergySide(WorldRenderer renderer, EnumFacing side, PartUniversalCable cable)
	{
		bindTexture(MekanismRenderer.getBlocksTexture());
		renderTransparency(renderer, MekanismRenderer.energyIcon, getModelForSide(cable, side), new ColourRGBA(1.0, 1.0, 1.0, cable.currentPower));
	}
}
