package mekanism.client;

import java.util.Arrays;

import mekanism.common.CableUtils;
import mekanism.common.PipeUtils;
import mekanism.common.TileEntityMechanicalPipe;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidStack;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderMechanicalPipe extends TileEntitySpecialRenderer
{
	private ModelTransmitter model = new ModelTransmitter();
	
	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTick)
	{
		renderAModelAt((TileEntityMechanicalPipe)tileEntity, x, y, z, partialTick);
	}

	public void renderAModelAt(TileEntityMechanicalPipe tileEntity, double x, double y, double z, float partialTick)
	{
		bindTextureByName("/mods/mekanism/render/MechanicalPipe" + (tileEntity.isActive ? "Active" : "") + ".png");
		GL11.glPushMatrix();
		GL11.glTranslatef((float)x + 0.5F, (float)y + 1.5F, (float)z + 0.5F);
		GL11.glScalef(1.0F, -1F, -1F);
		
		boolean[] connectable = new boolean[] {false, false, false, false, false, false};
		
		TileEntity[] connectedPipes = PipeUtils.getConnectedPipes(tileEntity);
		ITankContainer[] connectedAcceptors = PipeUtils.getConnectedAcceptors(tileEntity);
		
		for(ITankContainer container : connectedAcceptors)
		{
			if(container != null)
			{
				int side = Arrays.asList(connectedAcceptors).indexOf(container);
				
				if(container.getTanks(ForgeDirection.getOrientation(side).getOpposite()).length != 0)
				{
					connectable[side] = true;
				}
				else if(container.getTank(ForgeDirection.getOrientation(side).getOpposite(), new LiquidStack(-1, 1000)) != null)
				{
					connectable[side] = true;
				}
			}
		}
		
		for(TileEntity tile : connectedPipes)
		{
			if(tile != null)
			{
				int side = Arrays.asList(connectedPipes).indexOf(tile);
				
				connectable[side] = true;
			}
		}
		
		for(int i = 0; i < 6; i++)
		{
			if(connectable[i])
			{
				model.renderSide(ForgeDirection.getOrientation(i));
			}
		}
		
		model.Center.render(0.0625F);
		GL11.glPopMatrix();
	}
}
