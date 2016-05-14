package mekanism.generators.client;

import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.model.ModelAdvancedSolarGenerator;
import mekanism.generators.client.model.ModelBioGenerator;
import mekanism.generators.client.model.ModelGasGenerator;
import mekanism.generators.client.model.ModelHeatGenerator;
import mekanism.generators.client.model.ModelSolarGenerator;
import mekanism.generators.client.model.ModelWindGenerator;
import mekanism.generators.common.GeneratorsBlocks;
import mekanism.generators.common.block.states.BlockStateGenerator;
import mekanism.generators.common.block.states.BlockStateGenerator.GeneratorType;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class BlockRenderingHandler implements ISimpleBlockRenderingHandler
{
	private Minecraft mc = Minecraft.getMinecraft();
	
	public ModelAdvancedSolarGenerator advancedSolarGenerator = new ModelAdvancedSolarGenerator();
	public ModelSolarGenerator solarGenerator = new ModelSolarGenerator();
	public ModelBioGenerator bioGenerator = new ModelBioGenerator();
	public ModelHeatGenerator heatGenerator = new ModelHeatGenerator();
	public ModelGasGenerator gasGenerator = new ModelGasGenerator();
	public ModelWindGenerator windGenerator = new ModelWindGenerator();

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer)
	{
		GlStateManager.pushMatrix();
		GlStateManager.rotate(90F, 0.0F, 1.0F, 0.0F);

		if(block == GeneratorsBlocks.Generator)
		{
			if(metadata == BlockStateGenerator.GeneratorType.BIO_GENERATOR.meta)
			{
				GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
				GL11.glTranslated(0.0F, -1.0F, 0.0F);
				mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "BioGenerator.png"));
				bioGenerator.render(0.0625F);
			}
			else if(metadata == BlockStateGenerator.GeneratorType.ADVANCED_SOLAR_GENERATOR.meta)
			{
				GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
				GlStateManager.rotate(90F, 0.0F, 1.0F, 0.0F);
				GlStateManager.translate(0.0F, 0.2F, 0.0F);
				mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "AdvancedSolarGenerator.png"));
				advancedSolarGenerator.render(0.022F);
			}
			else if(metadata == BlockStateGenerator.GeneratorType.SOLAR_GENERATOR.meta)
			{
				GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
				GlStateManager.rotate(90F, 0.0F, -1.0F, 0.0F);
				GL11.glTranslated(0.0F, -1.0F, 0.0F);
				mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "SolarGenerator.png"));
				solarGenerator.render(0.0625F);
			}
			else if(metadata == BlockStateGenerator.GeneratorType.HEAT_GENERATOR.meta)
			{
				GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
				GL11.glTranslated(0.0F, -1.0F, 0.0F);
				mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "HeatGenerator.png"));
				heatGenerator.render(0.0625F, false, mc.renderEngine);
			}
			else if(metadata == BlockStateGenerator.GeneratorType.GAS_GENERATOR.meta)
			{
				GlStateManager.rotate(180F, 0.0F, 1.0F, 1.0F);
				GlStateManager.rotate(90F, -1.0F, 0.0F, 0.0F);
				GL11.glTranslated(0.0F, -1.0F, 0.0F);
				GlStateManager.rotate(180F, 0.0F, 1.0F, 0.0F);
				mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "GasGenerator.png"));
				gasGenerator.render(0.0625F);
			}
			else if(metadata == BlockStateGenerator.GeneratorType.WIND_GENERATOR.meta)
			{
				GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
				GlStateManager.rotate(180F, 0.0F, 1.0F, 0.0F);
				GlStateManager.translate(0.0F, 0.4F, 0.0F);
				mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "WindGenerator.png"));
				windGenerator.render(0.016F, 0);
			}
			else if(metadata != 2) 
			{
				MekanismRenderer.renderItem(renderer, metadata, block);
			}
		}

		GlStateManager.popMatrix();
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
	{
		//Handled by CTMRenderingHandler
		return false;
	}

	@Override
	public boolean shouldRender3DInInventory(int meta)
	{
		return true;
	}

	@Override
	public int getRenderId()
	{
		return GeneratorsClientProxy.GENERATOR_RENDER_ID;
	}
}
