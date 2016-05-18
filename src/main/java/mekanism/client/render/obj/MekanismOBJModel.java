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
	public MekanismOBJModel(MaterialLibrary matLib, ResourceLocation modelLocation)
	{
		super(matLib, modelLocation);
	}
	
	@Override
	public IFlexibleBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
	{
		IFlexibleBakedModel preBaked = super.bake(state, format, bakedTextureGetter);
		return new MekanismSmartOBJModel(preBaked, this, state, format, MekanismSmartOBJModel.getTexturesForOBJModel(preBaked), null);
	}
}
