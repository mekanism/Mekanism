package mekanism.client.render.tileentity;

import mekanism.client.model.ModelQuantumEntangloporter;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderQuantumEntangloporter extends TileEntitySpecialRenderer<TileEntityQuantumEntangloporter>
{
	private ModelQuantumEntangloporter model = new ModelQuantumEntangloporter();

	@Override
	public void renderTileEntityAt(TileEntityQuantumEntangloporter tileEntity, double x, double y, double z, float partialTick, int destroyStage)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef((float)x + 0.5F, (float)y + 1.5F, (float)z + 0.5F);

		bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "QuantumEntangloporter.png"));

		switch(tileEntity.facing.ordinal())
		{
			case 2: GL11.glRotatef(0, 0.0F, 1.0F, 0.0F); break;
			case 3: GL11.glRotatef(180, 0.0F, 1.0F, 0.0F); break;
			case 4: GL11.glRotatef(90, 0.0F, 1.0F, 0.0F); break;
			case 5: GL11.glRotatef(270, 0.0F, 1.0F, 0.0F); break;
		}

		GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
		model.render(0.0625F, rendererDispatcher.renderEngine);
		GL11.glPopMatrix();

		MekanismRenderer.machineRenderer.renderTileEntityAt(tileEntity, x, y, z, partialTick, destroyStage);
	}
}
