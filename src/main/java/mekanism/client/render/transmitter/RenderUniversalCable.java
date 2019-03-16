package mekanism.client.render.transmitter;

import mekanism.client.render.MekanismRenderer;
import mekanism.common.ColourRGBA;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.transmitter.TileEntityUniversalCable;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.EnumFacing;

import org.lwjgl.opengl.GL11;

public class RenderUniversalCable extends RenderTransmitterBase<TileEntityUniversalCable>
{
	public RenderUniversalCable()
	{
		super();
	}
	
	@Override
	public void render(TileEntityUniversalCable cable, double x, double y, double z, float partialTick, int destroyStage, float alpha)
	{
		if(MekanismConfig.current().client.opaqueTransmitters.val() || cable.currentPower == 0)
		{
			return;
		}

		push();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldRenderer = tessellator.getBuffer();
		GL11.glTranslated(x + 0.5, y+0.5, z + 0.5);

		for(EnumFacing side : EnumFacing.VALUES)
		{
			renderEnergySide(worldRenderer, side, cable);
		}

		MekanismRenderer.glowOn();

		tessellator.draw();

		MekanismRenderer.glowOff();
		pop();
	}
	
	public void renderEnergySide(BufferBuilder renderer, EnumFacing side, TileEntityUniversalCable cable)
	{
		bindTexture(MekanismRenderer.getBlocksTexture());
		renderTransparency(renderer, MekanismRenderer.energyIcon, getModelForSide(cable, side), new ColourRGBA(1.0, 1.0, 1.0, cable.currentPower));
	}
}
