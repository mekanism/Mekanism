package mekanism.generators.client.render.item;

import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Matrix4f;

import mekanism.client.render.ctm.ModelChiselBlock;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.model.ModelAdvancedSolarGenerator;
import mekanism.generators.client.model.ModelBioGenerator;
import mekanism.generators.client.model.ModelGasGenerator;
import mekanism.generators.client.model.ModelHeatGenerator;
import mekanism.generators.client.model.ModelSolarGenerator;
import mekanism.generators.client.model.ModelWindGenerator;
import mekanism.generators.common.block.states.BlockStateGenerator.GeneratorType;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.Attributes;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IPerspectiveAwareModel;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

public class BakedGeneratorItemModel implements IBakedModel, IPerspectiveAwareModel
{
	private IBakedModel baseModel;
	private ItemStack stack;
	
	private Minecraft mc = Minecraft.getMinecraft();
	
	public static ModelAdvancedSolarGenerator advancedSolarGenerator = new ModelAdvancedSolarGenerator();
	public static ModelSolarGenerator solarGenerator = new ModelSolarGenerator();
	public static ModelBioGenerator bioGenerator = new ModelBioGenerator();
	public static ModelHeatGenerator heatGenerator = new ModelHeatGenerator();
	public static ModelGasGenerator gasGenerator = new ModelGasGenerator();
	public static ModelWindGenerator windGenerator = new ModelWindGenerator();
	
	public BakedGeneratorItemModel(IBakedModel model, ItemStack s)
	{
		baseModel = model;
		stack = s;
	}
	
	private void doRender(TransformType type)
	{
		GeneratorType generatorType = GeneratorType.get(stack);
		
		if(generatorType != null)
		{
			GlStateManager.rotate(180F, 0.0F, 1.0F, 0.0F);
			
			if(generatorType == GeneratorType.BIO_GENERATOR)
			{
				GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
				GL11.glTranslated(0.0F, -1.0F, 0.0F);
				mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "BioGenerator.png"));
				bioGenerator.render(0.0625F);
			}
			else if(generatorType == GeneratorType.ADVANCED_SOLAR_GENERATOR)
			{
				GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
				GlStateManager.rotate(90F, 0.0F, 1.0F, 0.0F);
				GlStateManager.translate(0.0F, 0.2F, 0.0F);
				mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "AdvancedSolarGenerator.png"));
				advancedSolarGenerator.render(0.022F);
			}
			else if(generatorType == GeneratorType.SOLAR_GENERATOR)
			{
				GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
				GlStateManager.rotate(90F, 0.0F, -1.0F, 0.0F);
				GL11.glTranslated(0.0F, -1.0F, 0.0F);
				mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "SolarGenerator.png"));
				solarGenerator.render(0.0625F);
			}
			else if(generatorType == GeneratorType.HEAT_GENERATOR)
			{
				GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
				GL11.glTranslated(0.0F, -1.0F, 0.0F);
				mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "HeatGenerator.png"));
				heatGenerator.render(0.0625F, false, mc.renderEngine);
			}
			else if(generatorType == GeneratorType.GAS_GENERATOR)
			{
				GlStateManager.rotate(180F, 0.0F, 1.0F, 1.0F);
				GlStateManager.rotate(90F, -1.0F, 0.0F, 0.0F);
				GL11.glTranslated(0.0F, -1.0F, 0.0F);
				GlStateManager.rotate(180F, 0.0F, 1.0F, 0.0F);
				mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "GasGenerator.png"));
				gasGenerator.render(0.0625F);
			}
			else if(generatorType == GeneratorType.WIND_GENERATOR)
			{
				GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
				GlStateManager.rotate(180F, 0.0F, 1.0F, 0.0F);
				GlStateManager.translate(0.0F, 0.4F, 0.0F);
				mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "WindGenerator.png"));
				windGenerator.render(0.016F, 0);
			}
			
			return;
		}
	}

	@Override
	public List<BakedQuad> getFaceQuads(EnumFacing facing)
	{
		return new LinkedList<BakedQuad>();
	}

	@Override
	public List<BakedQuad> getGeneralQuads()
	{
		List<BakedQuad> generalQuads = new LinkedList<BakedQuad>();
		
		if(Block.getBlockFromItem(stack.getItem()) != null)
		{
			generalQuads.addAll(baseModel.getGeneralQuads());
		}
		
		return generalQuads;
	}

	@Override
	public boolean isAmbientOcclusion()
	{
		return baseModel.isAmbientOcclusion();
	}

	@Override
	public boolean isGui3d()
	{
		return baseModel.isGui3d();
	}

	@Override
	public boolean isBuiltInRenderer()
	{
		return baseModel.isBuiltInRenderer();
	}

	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return baseModel.getParticleTexture();
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms()
	{
		return baseModel.getItemCameraTransforms();
	}
	
    @Override
    public VertexFormat getFormat()
    {
        return Attributes.DEFAULT_BAKED_FORMAT;
    }
	
    @Override
    public Pair<? extends IFlexibleBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) 
    {    	
        if(cameraTransformType == TransformType.THIRD_PERSON) 
        {
            ForgeHooksClient.multiplyCurrentGlMatrix(ModelChiselBlock.DEFAULT_BLOCK_THIRD_PERSON_MATRIX);
        }
        
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
    	doRender(cameraTransformType);
    	GlStateManager.scale(2.0F, 2.0F, 2.0F);
        GlStateManager.enableLighting();
        GlStateManager.enableLight(0);
        GlStateManager.enableLight(1);
        GlStateManager.enableColorMaterial();
        GlStateManager.colorMaterial(1032, 5634);
    	GlStateManager.popMatrix();
    	
        return Pair.of(this, null);
    }
}
