package mekanism.client.render.tileentity;

import mekanism.client.render.MinerVisualRenderer;
import mekanism.common.tile.TileEntityDigitalMiner;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderDigitalMiner extends TileEntitySpecialRenderer<TileEntityDigitalMiner>
{
	@Override
	public void renderTileEntityAt(TileEntityDigitalMiner tileEntity, double x, double y, double z, float partialTick, int destroyStage)
	{
		if(tileEntity.clientRendering)
		{
			MinerVisualRenderer.render(tileEntity);
		}
	}
}
