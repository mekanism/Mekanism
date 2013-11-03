package mekanism.client.render.tileentity;

import mekanism.api.EnumColor;
import mekanism.client.MekanismClient;
import mekanism.client.model.ModelEnergyCube;
import mekanism.client.model.ModelEnergyCube.ModelEnergyCore;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.tileentity.TileEntityEnergyCube;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderEnergyCube extends TileEntitySpecialRenderer
{
	private ModelEnergyCube model = new ModelEnergyCube();
	private ModelEnergyCore core = new ModelEnergyCore();
	
	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTick)
	{
		renderAModelAt((TileEntityEnergyCube)tileEntity, x, y, z, 1F);
	}
	
	private void renderAModelAt(TileEntityEnergyCube tileEntity, double x, double y, double z, float partialTick)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef((float)x + 0.5F, (float)y + 1.5F, (float)z + 0.5F);
		
		bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "EnergyCube" + tileEntity.tier.name + ".png"));
		
		GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
		model.render(0.0625F);
		GL11.glPopMatrix();
		
		//Energy Core Render
        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);
        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "EnergyCore.png"));

        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        MekanismRenderer.glowOn();
        
        EnumColor c = tileEntity.tier.color;

        GL11.glPushMatrix();
        GL11.glScalef(0.4F, 0.4F, 0.4F);
        GL11.glColor4f(c.getColor(0), c.getColor(1), c.getColor(2), tileEntity.getEnergyStored()/tileEntity.getMaxEnergyStored());
        GL11.glTranslatef(0, (float)Math.sin(Math.toRadians((MekanismClient.ticksPassed + partialTick) * 3)) / 7, 0);
        GL11.glRotatef((MekanismClient.ticksPassed + partialTick) * 4, 0, 1, 0);
        GL11.glRotatef(36F + (MekanismClient.ticksPassed + partialTick) * 4, 0, 1, 1);
        core.render(0.0625F);
        GL11.glPopMatrix();

        MekanismRenderer.glowOff();

        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_POLYGON_SMOOTH);
        GL11.glDisable(GL11.GL_BLEND);

        GL11.glPopMatrix();
	}
}
