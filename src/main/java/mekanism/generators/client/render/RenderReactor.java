package mekanism.generators.client.render;

import mekanism.api.EnumColor;
import mekanism.client.MekanismClient;
import mekanism.client.model.ModelEnergyCube.ModelEnergyCore;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderReactor extends TileEntitySpecialRenderer
{
	private ModelEnergyCore core = new ModelEnergyCore();

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTick)
	{
		renderAModelAt((TileEntityReactorController)tileEntity, x, y, z, partialTick);
	}

	private void renderAModelAt(TileEntityReactorController tileEntity, double x, double y, double z, float partialTick)
	{
		if(tileEntity.isBurning())
		{
			GL11.glPushMatrix();
			GL11.glTranslatef((float)x + 0.5f, (float)y - 1.5f, (float)z + 0.5f);
			bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "EnergyCore.png"));

			MekanismRenderer.blendOn();
			MekanismRenderer.glowOn();

			EnumColor c;
			float scale;
			long scaledTemp = Math.round(tileEntity.getPlasmaTemp() / 1E8);

			c = EnumColor.AQUA;

			GL11.glPushMatrix();
			scale = 1f + 0.7f * (float)Math.sin(Math.toRadians((MekanismClient.ticksPassed + partialTick) * 3.14 * scaledTemp + 135F));
			GL11.glScalef(scale, scale, scale);
			GL11.glColor4f(c.getColor(0), c.getColor(1), c.getColor(2), 1);
			GL11.glRotatef((MekanismClient.ticksPassed + partialTick) * -6f * scaledTemp, 0, 1, 0);
			GL11.glRotatef(36F + (MekanismClient.ticksPassed + partialTick) * -7f * scaledTemp, 0, 1, 1);
			core.render(0.0625F);
			GL11.glPopMatrix();

			c = EnumColor.RED;

			GL11.glPushMatrix();
			scale = 1f + 0.8f * (float)Math.sin(Math.toRadians((MekanismClient.ticksPassed + partialTick) * 3f * scaledTemp));
			GL11.glScalef(scale, scale, scale);
			GL11.glColor4f(c.getColor(0), c.getColor(1), c.getColor(2), 1);
			GL11.glRotatef((MekanismClient.ticksPassed + partialTick) * 4f * scaledTemp, 0, 1, 0);
			GL11.glRotatef(36F + (MekanismClient.ticksPassed + partialTick) * 4 * scaledTemp, 0, 1, 1);
			core.render(0.0625F);
			GL11.glPopMatrix();

			c = EnumColor.ORANGE;

			GL11.glPushMatrix();
			scale = 1f - 0.9f * (float)Math.sin(Math.toRadians((MekanismClient.ticksPassed + partialTick) * 4 * scaledTemp + 90F));
			GL11.glScalef(scale, scale, scale);
			GL11.glColor4f(c.getColor(0), c.getColor(1), c.getColor(2), 1);
			GL11.glRotatef((MekanismClient.ticksPassed + partialTick) * 5f * scaledTemp - 35F, 0, 1, 0);
			GL11.glRotatef(36F + (MekanismClient.ticksPassed + partialTick) * -3f * scaledTemp + 70F, 0, 1, 1);
			core.render(0.0625F);
			GL11.glPopMatrix();

			MekanismRenderer.glowOff();
			MekanismRenderer.blendOff();

			GL11.glPopMatrix();
		}
	}
}
