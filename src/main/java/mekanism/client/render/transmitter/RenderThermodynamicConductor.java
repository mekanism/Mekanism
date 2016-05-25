package mekanism.client.render.transmitter;

import mekanism.client.render.ColourTemperature;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.multipart.PartThermodynamicConductor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.EnumFacing;

import org.lwjgl.opengl.GL11;

public class RenderThermodynamicConductor extends RenderTransmitterBase<PartThermodynamicConductor>
{
	public RenderThermodynamicConductor()
	{
		super();
	}
	
	@Override
	public void renderMultipartAt(PartThermodynamicConductor transmitter, double x, double y, double z, float partialTick, int destroyStage)
	{
		push();
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldRenderer = tessellator.getWorldRenderer();
		GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);

		for(EnumFacing side : EnumFacing.VALUES)
		{
			renderHeatSide(worldRenderer, side, transmitter);
		}

		MekanismRenderer.glowOn();
		
		tessellator.draw();
		isDrawing = false;

		MekanismRenderer.glowOff();
		pop();
	}
	
	public void renderHeatSide(WorldRenderer renderer, EnumFacing side, PartThermodynamicConductor cable)
	{
		bindTexture(MekanismRenderer.getBlocksTexture());
		renderTransparency(renderer, MekanismRenderer.heatIcon, getModelForSide(cable, side), ColourTemperature.fromTemperature(cable.temperature, cable.getBaseColour()));
	}
}
