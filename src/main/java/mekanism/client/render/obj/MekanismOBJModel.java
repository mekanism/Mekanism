package mekanism.client.render.obj;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.common.model.IModelState;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;

public class MekanismOBJModel extends OBJModel
{
	public OBJModelType modelType;
	public ResourceLocation location;
	
	public MekanismOBJModel(OBJModelType type, MaterialLibrary matLib, ResourceLocation modelLocation)
	{
		super(matLib, modelLocation);
		
		modelType = type;
		location = modelLocation;
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
	
	@Override
    public IModel process(ImmutableMap<String, String> customData)
    {
    	MekanismOBJModel ret = new MekanismOBJModel(modelType, getMatLib(), location);
        return ret;
    }

    @Override
    public IModel retexture(ImmutableMap<String, String> textures)
    {
    	MekanismOBJModel ret = new MekanismOBJModel(modelType, getMatLib().makeLibWithReplacements(textures), location);
        return ret;
    }
	
	public static enum OBJModelType
	{
		GLOW_PANEL,
		TRANSMITTER
	}
}
