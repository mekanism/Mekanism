package mekanism.client.render.item;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.EnumFacing;

public class BakedFluidModel implements IBakedModel
{
	public static final int STAGES = 16;
	
	public static final HashMap<String, IBakedModel>[] MODELS = new HashMap[STAGES];
	
	static
	{
		for(int x = 0; x < STAGES; x++)
		{
			MODELS[x] = new HashMap<String, IBakedModel>();
		}
	}
	
	private IBakedModel baseModel;
	private String fluidName;
	private int fluidLevel;
	private boolean cullFluidTop;
	
	public BakedFluidModel(IBakedModel model, String id, int level, boolean cullTop)
	{
		baseModel = model;
		fluidName = id;
		fluidLevel = level;
		cullFluidTop = cullTop;
	}

	@Override
	public List<BakedQuad> getFaceQuads(EnumFacing facing)
	{
		List<BakedQuad> faceQuads = new LinkedList<BakedQuad>();
		
		faceQuads.addAll(baseModel.getFaceQuads(facing));
		
		if(fluidLevel > 0 && fluidLevel <= STAGES)
		{
			HashMap<String, IBakedModel> fluidModels = MODELS[fluidLevel - 1];
			
			if(fluidModels.containsKey(fluidName))
			{
				faceQuads.addAll(fluidModels.get(fluidName).getFaceQuads(facing));
			}
		}
		
		return faceQuads;
	}

	@Override
	public List<BakedQuad> getGeneralQuads()
	{
		List<BakedQuad> generalQuads = new LinkedList<BakedQuad>();
		
		generalQuads.addAll(baseModel.getGeneralQuads());
		
		if(fluidLevel > 0 && fluidLevel <= STAGES)
		{
			HashMap<String, IBakedModel> fluidModels = MODELS[fluidLevel - 1];
			
			if(fluidModels.containsKey(fluidName))
			{
				// The fluid model needs a separate culling logic from the rest of the tank, 
				// because the top of the fluid is supposed to be visible if the tank block 
				// above is empty. (getGeneralQuads() handles quads that don't have a cullface
				// annotation in the .json)
				
				if(cullFluidTop)
				{
					for(BakedQuad quad : fluidModels.get(fluidName).getGeneralQuads())
					{
						if(quad.getFace() != EnumFacing.UP) 
						{
							generalQuads.add(quad);
						}
					}
				}
				else {
					generalQuads.addAll(fluidModels.get(fluidName).getGeneralQuads());
				}
			}
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
}