package mekanism.induction.client.render;

import mekanism.induction.client.model.ModelInsulation;
import mekanism.induction.client.model.ModelWire;
import mekanism.induction.common.MekanismInduction;
import mekanism.induction.common.tileentity.TileEntityWire;
import mekanism.induction.common.wire.EnumWireMaterial;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import universalelectricity.core.vector.Vector3;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * TODO: Use ISBRH.
 * 
 * @author Calclavia
 * 
 */
@SideOnly(Side.CLIENT)
public class RenderWire extends TileEntitySpecialRenderer
{
	private static final ResourceLocation WIRE_TEXTURE = new ResourceLocation(MekanismInduction.DOMAIN, MekanismInduction.MODEL_TEXTURE_DIRECTORY + "wire.png");
	private static final ResourceLocation INSULATION_TEXTURE = new ResourceLocation(MekanismInduction.DOMAIN, MekanismInduction.MODEL_TEXTURE_DIRECTORY + "insulation.png");
	public static final ModelWire WIRE_MODEL = new ModelWire();
	public static final ModelInsulation INSULATION_MODEL = new ModelInsulation();

	public void renderModelAt(TileEntityWire tileEntity, double x, double y, double z, float f)
	{
		if (tileEntity != null)
		{
			GL11.glPushMatrix();
			GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
			GL11.glScalef(1, -1, -1);

			EnumWireMaterial material = tileEntity.getMaterial();
			// Texture file
			FMLClientHandler.instance().getClient().renderEngine.bindTexture(WIRE_TEXTURE);
			GL11.glColor4d(material.color.x, material.color.y, material.color.z, 1);

			tileEntity.adjacentConnections = null;
			TileEntity[] adjacentConnections = tileEntity.getAdjacentConnections();

			if (adjacentConnections != null)
			{
				if (adjacentConnections[0] != null)
				{
					WIRE_MODEL.renderBottom();
				}

				if (adjacentConnections[1] != null)
				{
					WIRE_MODEL.renderTop();
				}

				if (adjacentConnections[2] != null)
				{
					WIRE_MODEL.renderBack();
				}

				if (adjacentConnections[3] != null)
				{
					WIRE_MODEL.renderFront();
				}

				if (adjacentConnections[4] != null)
				{
					WIRE_MODEL.renderLeft();
				}

				if (adjacentConnections[5] != null)
				{
					WIRE_MODEL.renderRight();
				}
			}

			WIRE_MODEL.renderMiddle();

			if (tileEntity.isInsulated)
			{
				// Texture file
				FMLClientHandler.instance().getClient().renderEngine.bindTexture(INSULATION_TEXTURE);
				Vector3 insulationColor = MekanismInduction.DYE_COLORS[tileEntity.dyeID];
				GL11.glColor4d(insulationColor.x, insulationColor.y, insulationColor.z, 1);

				if (adjacentConnections != null)
				{
					if (adjacentConnections[0] != null)
					{
						INSULATION_MODEL.renderBottom(0.0625f);
					}

					if (adjacentConnections[1] != null)
					{
						INSULATION_MODEL.renderTop(0.0625f);
					}

					if (adjacentConnections[2] != null)
					{
						INSULATION_MODEL.renderBack(0.0625f);
					}

					if (adjacentConnections[3] != null)
					{
						INSULATION_MODEL.renderFront(0.0625f);
					}

					if (adjacentConnections[4] != null)
					{
						INSULATION_MODEL.renderLeft(0.0625f);
					}

					if (adjacentConnections[5] != null)
					{
						INSULATION_MODEL.renderRight(0.0625f);
					}
				}

				INSULATION_MODEL.renderMiddle(0.0625f);
			}

			GL11.glPopMatrix();
		}
	}

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double var2, double var4, double var6, float var8)
	{
		this.renderModelAt((TileEntityWire) tileEntity, var2, var4, var6, var8);
	}
}