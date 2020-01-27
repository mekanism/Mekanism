package mekanism.client.render.tileentity;

import java.util.HashMap;
import java.util.Map;

import mekanism.api.transmitters.TransmissionType;
import mekanism.client.MekanismClient;
import mekanism.client.model.ModelEnergyCube;
import mekanism.client.model.ModelEnergyCube.ModelEnergyCore;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Tier.EnergyCubeTier;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderEnergyCube extends TileEntitySpecialRenderer
{
	public static int[][] COLORS = new int[][] {new int[] {100, 210, 125}, new int[] {215, 85, 70}, new int[] {80, 125, 230},
												new int[] {154, 120, 200}, new int[] {0, 0, 0}};
	
	private ModelEnergyCube model = new ModelEnergyCube();
	private ModelEnergyCore core = new ModelEnergyCore();
	
	public static Map<EnergyCubeTier, ResourceLocation> resources = new HashMap<EnergyCubeTier, ResourceLocation>();
	public static ResourceLocation baseTexture = MekanismUtils.getResource(ResourceType.RENDER, "EnergyCube.png");
	public static ResourceLocation coreTexture = MekanismUtils.getResource(ResourceType.RENDER, "EnergyCore.png");

	static {
		if(resources.isEmpty())
		{
			for(EnergyCubeTier tier : EnergyCubeTier.values())
			{
				resources.put(tier, MekanismUtils.getResource(ResourceType.RENDER, "EnergyCube" + tier.getBaseTier().getName() + ".png"));
			}
		}
	}
	
	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTick)
	{
		renderAModelAt((TileEntityEnergyCube)tileEntity, x, y, z, partialTick);
	}

	private void renderAModelAt(TileEntityEnergyCube tileEntity, double x, double y, double z, float partialTick)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef((float)x + 0.5F, (float)y + 1.5F, (float)z + 0.5F);

		bindTexture(baseTexture);

		switch(tileEntity.facing)
		{
			case 0:
			{
				GL11.glRotatef(90F, -1.0F, 0.0F, 0.0F);
				GL11.glTranslatef(0.0F, 1.0F, -1.0F);
				break;
			}
			case 1:
			{
				GL11.glRotatef(90F, 1.0F, 0.0F, 0.0F);
				GL11.glTranslatef(0.0F, 1.0F, 1.0F);
				break;
			}
			case 2: GL11.glRotatef(0, 0.0F, 1.0F, 0.0F); break;
			case 3: GL11.glRotatef(180, 0.0F, 1.0F, 0.0F); break;
			case 4: GL11.glRotatef(90, 0.0F, 1.0F, 0.0F); break;
			case 5: GL11.glRotatef(270, 0.0F, 1.0F, 0.0F); break;
		}

		GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
		model.render(0.0625F, tileEntity.tier, field_147501_a.field_147553_e);
		
		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			bindTexture(baseTexture);
			model.renderSide(0.0625F, side, tileEntity.configComponent.getOutput(TransmissionType.ENERGY, side.ordinal()).ioState, tileEntity.tier, field_147501_a.field_147553_e);
		}
		
		GL11.glPopMatrix();

		if(tileEntity.getEnergy()/tileEntity.getMaxEnergy() > 0.1)
		{
			GL11.glPushMatrix();
			GL11.glTranslatef((float)x + 0.5f, (float)y + 0.5f, (float)z + 0.5f);
			bindTexture(coreTexture);

			MekanismRenderer.blendOn();
			MekanismRenderer.glowOn();

			int[] c = COLORS[tileEntity.tier.getBaseTier().ordinal()];

			GL11.glPushMatrix();
			GL11.glScalef(0.4F, 0.4F, 0.4F);
			GL11.glColor4f((float)c[0]/255F, (float)c[1]/255F, (float)c[2]/255F, (float)(tileEntity.getEnergy() / tileEntity.getMaxEnergy()));
			GL11.glTranslatef(0, (float)Math.sin(Math.toRadians((MekanismClient.ticksPassed + partialTick) * 3)) / 7, 0);
			GL11.glRotatef((MekanismClient.ticksPassed + partialTick) * 4, 0, 1, 0);
			GL11.glRotatef(36F + (MekanismClient.ticksPassed + partialTick) * 4, 0, 1, 1);
			core.render(0.0625F);
			GL11.glPopMatrix();

			MekanismRenderer.glowOff();
			MekanismRenderer.blendOff();

			GL11.glPopMatrix();
		}
		
		MekanismRenderer.machineRenderer.renderAModelAt(tileEntity, x, y, z, partialTick);
	}
}
