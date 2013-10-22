package mekanism.client.render.tileentity;

import mekanism.api.Object3D;
import mekanism.client.model.ModelTransmitter;
import mekanism.common.TransporterStack;
import mekanism.common.tileentity.TileEntityLogisticalTransporter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.TransporterUtils;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderLogisticalTransporter extends TileEntitySpecialRenderer
{
	private ModelTransmitter model = new ModelTransmitter();
	
	private EntityItem entityItem = new EntityItem(null);
	private RenderItem renderer = (RenderItem)RenderManager.instance.getEntityClassRenderObject(EntityItem.class);
	
	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTick)
	{
		renderAModelAt((TileEntityLogisticalTransporter)tileEntity, x, y, z, partialTick);
	}

	public void renderAModelAt(TileEntityLogisticalTransporter tileEntity, double x, double y, double z, float partialTick)
	{
		bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "LogisticalTransporter" + (tileEntity.isActive ? "Active" : "") + ".png"));
		GL11.glPushMatrix();
		GL11.glTranslatef((float)x + 0.5F, (float)y + 1.5F, (float)z + 0.5F);
		GL11.glScalef(1.0F, -1F, -1F);
		GL11.glDisable(GL11.GL_CULL_FACE);
		
		boolean[] connectable = TransporterUtils.getConnections(tileEntity);
		
		model.renderCenter(connectable);
		
		for(int i = 0; i < 6; i++)
		{
			model.renderSide(ForgeDirection.getOrientation(i), connectable[i]);
		}
		
		GL11.glEnable(GL11.GL_CULL_FACE);
		
		entityItem.age = 0;
		entityItem.hoverStart = 0;
		
		for(TransporterStack stack : tileEntity.transit)
		{
			entityItem.setEntityItemStack(stack.itemStack);
			Object3D offset = new Object3D(0, 0, 0).step(ForgeDirection.getOrientation(stack.getSide(tileEntity)));
			
			double progress = ((double)stack.progress / 100D) * 0.5D;
			
			renderer.doRenderItem(entityItem, x + 0.5 + offset.xCoord*progress, y + 1.5 + offset.yCoord*progress, z + 0.5 + offset.zCoord*progress, 0, 0);
		}
		
		GL11.glPopMatrix();
	}
}
