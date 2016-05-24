package mekanism.client.render.obj;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IModelState;
import net.minecraftforge.client.model.obj.OBJModel;

import com.google.common.base.Function;

public class MekanismOBJModel extends OBJModel
{
	public OBJModelType modelType;
	
	public MekanismOBJModel(OBJModelType type, MaterialLibrary matLib, ResourceLocation modelLocation)
	{
		super(matLib, modelLocation);
		
		modelType = type;
	}
	
	@Override
	public IFlexibleBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
	{
		IFlexibleBakedModel preBaked = super.bake(state, format, bakedTextureGetter);
		
		if(modelType == OBJModelType.GLOW_PANEL)
		{
			return new GlowPanelModel(preBaked, this, state, format, GlowPanelModel.getTexturesForOBJModel(preBaked), null);
		}
		else if(modelType == OBJModelType.TRANSMITTER)
		{
			return new TransmitterModel(preBaked, this, state, format, TransmitterModel.getTexturesForOBJModel(preBaked), null);
		}
		
		return null;
	}
	
	public static enum OBJModelType
	{
		GLOW_PANEL,
		TRANSMITTER
	}
}
