/**
 * 
 */
package mekanism.induction.client.render;

import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.induction.client.InductionClientProxy;
import mekanism.induction.common.block.BlockBattery;
import mekanism.induction.common.block.BlockEMContractor;
import mekanism.induction.common.block.BlockMultimeter;
import mekanism.induction.common.block.BlockTesla;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
@SideOnly(Side.CLIENT)
public class BlockRenderingHandler implements ISimpleBlockRenderingHandler
{
	public static final BlockRenderingHandler INSTANCE = new BlockRenderingHandler();

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer)
	{
		if(block instanceof BlockTesla)
		{
			GL11.glPushMatrix();
			GL11.glTranslated(0.5, 1.5, 0.5);
			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
			Minecraft.getMinecraft().renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "TeslaBottom.png"));
			RenderTesla.bottom.render(0.0625f);
			GL11.glPopMatrix();
		}
		else if(block instanceof BlockEMContractor)
		{
			GL11.glPushMatrix();
			GL11.glTranslated(0.5, 1.5, 0.5);
			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
			Minecraft.getMinecraft().renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ElectromagneticContractor.png"));
			RenderEMContractor.model.render(0.0625f);
			GL11.glPopMatrix();
		}
		else if(block instanceof BlockBattery)
		{
			GL11.glPushMatrix();
			GL11.glTranslated(0.5, 1.42, 0.5);
			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
			Minecraft.getMinecraft().renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "Battery.png"));
			RenderBattery.model.render(0.0625f);
			GL11.glPopMatrix();
		}
		else if(block instanceof BlockMultimeter)
		{
			GL11.glPushMatrix();
			GL11.glRotatef(180, 0, 1, 0);
			GL11.glTranslated(0, -1, -0.7);
			Minecraft.getMinecraft().renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "Multimeter.png"));
			RenderMultimeter.model.render(0.0625f);
			GL11.glPopMatrix();
		}
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
	{
		if(block instanceof BlockBattery)
		{
			return true;
		}

		return false;
	}

	@Override
	public boolean shouldRender3DInInventory()
	{
		return true;
	}

	@Override
	public int getRenderId()
	{
		return InductionClientProxy.INDUCTION_RENDER_ID;
	}
}
