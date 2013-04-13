package mekanism.generators.client;

import mekanism.generators.common.TileEntityHydrogenGenerator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

public class RenderHydrogenGenerator extends TileEntitySpecialRenderer
{
	private ModelHydrogenGenerator model = new ModelHydrogenGenerator();
	
	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTick)
	{
		renderAModelAt((TileEntityHydrogenGenerator)tileEntity, x, y, z, partialTick);
	}
	
	private void renderAModelAt(TileEntityHydrogenGenerator tileEntity, double x, double y, double z, float partialTick)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef((float)x + 0.5F, (float)y + 1.5F, (float)z + 0.5F);
		bindTextureByName("/mods/mekanism/render/HydrogenGenerator.png");
		
	    switch(tileEntity.facing)
	    {
		    case 2: GL11.glRotatef(270, 0.0F, 1.0F, 0.0F); break;
			case 3: GL11.glRotatef(90, 0.0F, 1.0F, 0.0F); break;
			case 4: GL11.glRotatef(0, 0.0F, 1.0F, 0.0F); break;
			case 5: GL11.glRotatef(180, 0.0F, 1.0F, 0.0F); break;
	    }
		
		GL11.glRotatef(180F, 0.0F, 1.0F, 1.0F);
		GL11.glRotatef(90F, -1.0F, 0.0F, 0.0F);
		GL11.glRotatef(90F, 0.0F, 1.0F, 0.0F);
		model.render(0.0625F);
		GL11.glPopMatrix();
	}
}
