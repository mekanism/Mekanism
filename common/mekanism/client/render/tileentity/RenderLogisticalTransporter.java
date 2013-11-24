package mekanism.client.render.tileentity;

import mekanism.api.Object3D;
import mekanism.client.model.ModelTransmitter;
import mekanism.client.model.ModelTransmitter.Size;
import mekanism.client.model.ModelTransporterBox;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.tileentity.TileEntityLogisticalTransporter;
import mekanism.common.transporter.TransporterStack;
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
	private ModelTransmitter model = new ModelTransmitter(Size.LARGE);
	private ModelTransporterBox modelBox = new ModelTransporterBox();
	
	private EntityItem entityItem = new EntityItem(null);
	private RenderItem renderer = (RenderItem)RenderManager.instance.getEntityClassRenderObject(EntityItem.class);
	
	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTick)
	{
		renderAModelAt((TileEntityLogisticalTransporter)tileEntity, x, y, z, partialTick);
	}

	public void renderAModelAt(TileEntityLogisticalTransporter tileEntity, double x, double y, double z, float partialTick)
	{
		int meta = Object3D.get(tileEntity).getMetadata(tileEntity.worldObj);
		
		if(meta == 3)
		{
			bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "LogisticalTransporter.png"));
		}
		else if(meta == 4)
		{
			bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "RestrictiveTransporter.png"));
		}
		else if(meta == 5)
		{
			bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "DiversionTransporter.png"));
		}
		
		GL11.glPushMatrix();
		GL11.glTranslatef((float)x + 0.5F, (float)y + 1.5F, (float)z + 0.5F);
		GL11.glScalef(1.0F, -1F, -1F);
		GL11.glDisable(GL11.GL_CULL_FACE);
		
		if(tileEntity.color != null)
		{
			GL11.glColor4f(tileEntity.color.getColor(0), tileEntity.color.getColor(1), tileEntity.color.getColor(2), 1.0F);
		}
		
		boolean[] connectable = TransporterUtils.getConnections(tileEntity);
		
		model.renderCenter(connectable);
		
		for(int i = 0; i < 6; i++)
		{
			model.renderSide(ForgeDirection.getOrientation(i), connectable[i]);
		}
		
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glPopMatrix();
		
		entityItem.age = 0;
		entityItem.hoverStart = 0;
		
		entityItem.setPosition(tileEntity.xCoord + 0.5, tileEntity.yCoord + 0.5, tileEntity.zCoord + 0.5);
		entityItem.worldObj = tileEntity.worldObj;
		
		for(TransporterStack stack : tileEntity.transit)
		{
			if(stack != null)
			{
				GL11.glPushMatrix();
				entityItem.setEntityItemStack(stack.itemStack);
				
				float[] pos = TransporterUtils.getStackPosition(tileEntity, stack, partialTick*TileEntityLogisticalTransporter.SPEED);
				
				GL11.glTranslated(x + pos[0], y + pos[1] - entityItem.yOffset, z + pos[2]);
				GL11.glScalef(0.75F, 0.75F, 0.75F);
				
				renderer.doRenderItem(entityItem, 0, 0, 0, 0, 0);
				GL11.glPopMatrix();
				
				if(stack.color != null)
				{
					bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "TransporterBox.png"));
					GL11.glPushMatrix();
					MekanismRenderer.glowOn();
					GL11.glDisable(GL11.GL_CULL_FACE);
					GL11.glColor4f(stack.color.getColor(0), stack.color.getColor(1), stack.color.getColor(2), 1.0F);
					GL11.glTranslatef((float)(x + pos[0]), (float)(y + pos[1] - entityItem.yOffset - (stack.itemStack.itemID < 256 ? 0.1 : 0)), (float)(z + pos[2]));
					modelBox.render(0.0625F);
					MekanismRenderer.glowOff();
					GL11.glPopMatrix();
				}
			}
		}
	}
}
