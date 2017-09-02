package mekanism.generators.client.render.item;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import com.google.common.collect.ImmutableMap;
import mekanism.client.render.MekanismRenderer;
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

import net.minecraftforge.common.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.ImmutableList;

public class BakedGeneratorItemModel implements IBakedModel
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
		
		List<BakedQuad> generalQuads = new LinkedList<>();
		
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
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType)
    {
    	prevTransform = cameraTransformType;
    	
        return Pair.of(this, transforms.get(cameraTransformType).getMatrix());
    }

	// Copy from old CTM
	public static Map<TransformType, TRSRTransformation> transforms = ImmutableMap.<TransformType, TRSRTransformation>builder()
			.put(TransformType.GUI,                         get(0, 0, 0, 30, 225, 0, 0.625f))
			.put(TransformType.THIRD_PERSON_RIGHT_HAND,     get(0, 2.5f, 0, 75, 45, 0, 0.375f))
			.put(TransformType.THIRD_PERSON_LEFT_HAND,      get(0, 2.5f, 0, 75, 45, 0, 0.375f))
			.put(TransformType.FIRST_PERSON_RIGHT_HAND,     get(0, 0, 0, 0, 45, 0, 0.4f))
			.put(TransformType.FIRST_PERSON_LEFT_HAND,      get(0, 0, 0, 0, 225, 0, 0.4f))
			.put(TransformType.GROUND,                      get(0, 2, 0, 0, 0, 0, 0.25f))
			.put(TransformType.HEAD,                        get(0, 0, 0, 0, 0, 0, 1))
			.put(TransformType.FIXED,                       get(0, 0, 0, 0, 0, 0, 1))
			.put(TransformType.NONE,                        get(0, 0, 0, 0, 0, 0, 0))
			.build();
	private static TRSRTransformation get(float tx, float ty, float tz, float ax, float ay, float az, float s)
	{
		return new TRSRTransformation(
				new Vector3f(tx / 16, ty / 16, tz / 16),
				TRSRTransformation.quatFromXYZDegrees(new Vector3f(ax, ay, az)),
				new Vector3f(s, s, s),
				null);
	}

	@Override
	public ItemOverrideList getOverrides() 
	{
		return ItemOverrideList.NONE;
	}
}
