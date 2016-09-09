package mekanism.generators.client.render.item;

import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Matrix4f;

import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.ctm.CTMModelFactory;
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
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IPerspectiveAwareModel;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.ImmutableList;

public class BakedGeneratorItemModel implements IBakedModel, IPerspectiveAwareModel
{
	private IBakedModel baseModel;
	private ItemStack stack;
	
	private TransformType prevTransform;
	
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
	public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand)
	{
		if(side != null) 
    	{
    		return ImmutableList.of();
    	}
		
		Tessellator tessellator = Tessellator.getInstance();
		VertexFormat prevFormat = null;
		
		MekanismRenderer.pauseRenderer(tessellator);
		
		List<BakedQuad> generalQuads = new LinkedList<BakedQuad>();
		
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.5F, 0.5F, 0.5F);
        GlStateManager.rotate(180, 0.0F, 1.0F, 0.0F);
    	doRender(prevTransform);
        GlStateManager.enableLighting();
        GlStateManager.enableLight(0);
        GlStateManager.enableLight(1);
        GlStateManager.enableColorMaterial();
        GlStateManager.colorMaterial(1032, 5634);
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
    	GlStateManager.popMatrix();
    	
    	MekanismRenderer.resumeRenderer(tessellator);
		
		if(Block.getBlockFromItem(stack.getItem()) != null)
		{
			generalQuads.addAll(baseModel.getQuads(state, side, rand));
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
    public Pair<? extends IPerspectiveAwareModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) 
    {
    	prevTransform = cameraTransformType;
    	
        return Pair.of(this, CTMModelFactory.transforms.get(cameraTransformType).getMatrix());
    }

	@Override
	public ItemOverrideList getOverrides() 
	{
		return ItemOverrideList.NONE;
	}
}
