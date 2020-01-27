package mekanism.client.render.block;

import mekanism.client.ClientProxy;
import mekanism.client.model.ModelSecurityDesk;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismBlocks;
import mekanism.common.block.BlockBasic.BasicType;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BasicRenderingHandler implements ISimpleBlockRenderingHandler
{
	private Minecraft mc = Minecraft.getMinecraft();
	
	public ModelSecurityDesk securityDesk = new ModelSecurityDesk();
	
	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer)
	{
		GL11.glPushMatrix();
		GL11.glRotatef(90F, 0.0F, 1.0F, 0.0F);

		BasicType type = BasicType.get(block, metadata);
		
		if(type != null)
		{
			if(type == BasicType.STRUCTURAL_GLASS)
			{
				MekanismRenderer.blendOn();
			}
			
			if(type != BasicType.SECURITY_DESK)
			{
				GL11.glRotatef(180, 0.0F, 1.0F, 0.0F);
				MekanismRenderer.renderItem(renderer, metadata, block);
			}
			else {
				GL11.glRotatef(180, 1.0F, 0.0F, 0.0F);
				GL11.glScalef(0.8F, 0.8F, 0.8F);
				GL11.glTranslatef(0.0F, -0.8F, 0.0F);
				mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "SecurityDesk.png"));
				securityDesk.render(0.0625F, mc.renderEngine);
			}
			
			if(type == BasicType.STRUCTURAL_GLASS)
			{
				MekanismRenderer.blendOff();
			}
		}

		GL11.glPopMatrix();
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
	{
		if(block == MekanismBlocks.BasicBlock || block == MekanismBlocks.BasicBlock2)
		{
			int metadata = world.getBlockMetadata(x, y, z);

			renderer.renderStandardBlock(block, x, y, z);
			renderer.setRenderBoundsFromBlock(block);

			return true;
		}

		return false;
	}

	@Override
	public int getRenderId()
	{
		return ClientProxy.BASIC_RENDER_ID;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId)
	{
		if (mekanism.api.MekanismConfig.client.reducerendermachines) return false;
		return true;
	}
}
