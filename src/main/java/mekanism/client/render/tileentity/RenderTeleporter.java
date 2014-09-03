package mekanism.client.render.tileentity;

import java.util.HashMap;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.gas.GasRegistry;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.MekanismBlocks;
import mekanism.common.tile.TileEntityTeleporter;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

public class RenderTeleporter extends TileEntitySpecialRenderer
{
	private HashMap<Integer, DisplayInteger> cachedOverlays = new HashMap<Integer, DisplayInteger>();

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTick)
	{
		renderAModelAt((TileEntityTeleporter)tileEntity, x, y, z, partialTick);
	}

	public void renderAModelAt(TileEntityTeleporter tileEntity, double x, double y, double z, float partialTick)
	{
		if(tileEntity.shouldRender)
		{
			push();

			GL11.glColor4f(EnumColor.PURPLE.getColor(0), EnumColor.PURPLE.getColor(1), EnumColor.PURPLE.getColor(2), 0.75F);

			bindTexture(MekanismRenderer.getBlocksTexture());
			GL11.glTranslatef((float)x, (float)y, (float)z);

			Coord4D obj = Coord4D.get(tileEntity).getFromSide(ForgeDirection.WEST);
			int type = 0;

			if(obj.getBlock(tileEntity.getWorldObj()) == MekanismBlocks.BasicBlock && obj.getMetadata(tileEntity.getWorldObj()) == 7)
			{
				type = 1;
			}

			int display = getOverlayDisplay(type).display;
			GL11.glCallList(display);

			pop();
		}
	}

	private void pop()
	{
		GL11.glPopAttrib();
		MekanismRenderer.glowOff();
		MekanismRenderer.blendOff();
		GL11.glPopMatrix();
	}

	private void push()
	{
		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_LIGHTING);
		MekanismRenderer.glowOn();
		MekanismRenderer.blendOn();
	}

	private DisplayInteger getOverlayDisplay(Integer type)
	{
		if(cachedOverlays.containsKey(type))
		{
			return cachedOverlays.get(type);
		}

		Model3D toReturn = new Model3D();
		toReturn.baseBlock = Blocks.stone;
		toReturn.setTexture(GasRegistry.getGas("oxygen").getIcon());

		DisplayInteger display = DisplayInteger.createAndStart();

		if(cachedOverlays.containsKey(type))
		{
			cachedOverlays.get(type);
		}
		else {
			cachedOverlays.put(type, display);
		}

		switch(type)
		{
			case 0:
			{
				toReturn.minY = 1;
				toReturn.maxY = 3;

				toReturn.minX = 0.46;
				toReturn.minZ = 0;
				toReturn.maxX = 0.54;
				toReturn.maxZ = 1;
				break;
			}
			case 1:
			{
				toReturn.minY = 1;
				toReturn.maxY = 3;

				toReturn.minX = 0;
				toReturn.minZ = 0.46;
				toReturn.maxX = 1;
				toReturn.maxZ = 0.54;
				break;
			}
		}

		MekanismRenderer.renderObject(toReturn);
		display.endList();

		return display;
	}
}
