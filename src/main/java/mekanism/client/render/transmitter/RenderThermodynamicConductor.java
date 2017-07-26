package mekanism.client.render.transmitter;

import mekanism.client.render.ColourTemperature;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.config.MekanismConfig.client;
import mekanism.common.tile.transmitter.TileEntityThermodynamicConductor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.EnumFacing;

import org.lwjgl.opengl.GL11;

public class RenderThermodynamicConductor extends RenderTransmitterBase<TileEntityThermodynamicConductor>
{
	public RenderThermodynamicConductor()
	{
		super();
	}
	
	@Override
	public void render(TileEntityThermodynamicConductor transmitter, double x, double y, double z, float partialTick, int destroyStage, float alpha)
	{
		if(client.opaqueTransmitters)
		{
			return;
		}
		
		push();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldRenderer = tessellator.getBuffer();
		GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);

		for(EnumFacing side : EnumFacing.VALUES)
		{
			renderHeatSide(worldRenderer, side, transmitter);
		}

		MekanismRenderer.glowOn();
		
		tessellator.draw();

		MekanismRenderer.glowOff();
		pop();
	}
	
	public void renderHeatSide(BufferBuilder renderer, EnumFacing side, TileEntityThermodynamicConductor cable)
	{
		bindTexture(MekanismRenderer.getBlocksTexture());
		renderTransparency(renderer, MekanismRenderer.heatIcon, getModelForSide(cable, side), ColourTemperature.fromTemperature(cable.temperature, cable.getBaseColour()));
	}
}
