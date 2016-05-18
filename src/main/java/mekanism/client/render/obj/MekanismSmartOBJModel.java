package mekanism.client.render.obj;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mekanism.api.EnumColor;
import mekanism.common.multipart.GlowPanelBlockState;
import mekanism.common.multipart.PartGlowPanel;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraftforge.client.model.IModelState;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.obj.OBJModel.Group;
import net.minecraftforge.client.model.obj.OBJModel.OBJBakedModel;
import codechicken.lib.vec.Matrix4;

import com.google.common.collect.ImmutableMap;

public class MekanismSmartOBJModel extends OBJBakedModel
{
	private IBakedModel baseModel;
	
	private HashMap<TransformType, Matrix4> transformationMap = new HashMap<TransformType, Matrix4>();
	
	private static Map<EnumColor, MekanismSmartOBJModel> glowPanelCache = new HashMap<EnumColor, MekanismSmartOBJModel>();
	
	private IBlockState tempState;
	
	public MekanismSmartOBJModel(IBakedModel base, OBJModel model, IModelState state, VertexFormat format, ImmutableMap<String, TextureAtlasSprite> textures, HashMap<TransformType, Matrix4> transform)
	{
		model.super(model, state, format, textures);
		baseModel = base;
		transformationMap = transform;
	}
	
	@Override
	public OBJBakedModel handleBlockState(IBlockState state)
	{
		if(state instanceof GlowPanelBlockState)
		{
			EnumColor color = state.getValue(PartGlowPanel.colorProperty);
			
			if(!glowPanelCache.containsKey(color))
			{
				MekanismSmartOBJModel model = new MekanismSmartOBJModel(baseModel, getModel(), getState(), getFormat(), getTextures(), transformationMap);
				model.tempState = state;
				glowPanelCache.put(color, model);
			}
			
			return glowPanelCache.get(color);
		}
		
		return this;
	}
	
	@Override
	public List<BakedQuad> getGeneralQuads()
	{
		for(Map.Entry<String, Group> g : getModel().getMatLib().getGroups().entrySet())
		{
			System.out.println("BAKED " + g.getKey() + " " + g.getValue().getName() + " " + g.getValue().getFaces());
		}
		
		return super.getGeneralQuads();
	}
	
	private static Field f_textures;
	
	public static ImmutableMap<String, TextureAtlasSprite> getTexturesForOBJModel(IBakedModel model)
	{
		try {
			if(f_textures == null)
			{
				f_textures = OBJBakedModel.class.getDeclaredField("textures");
				f_textures.setAccessible(true);
			}
			
			return (ImmutableMap<String, TextureAtlasSprite>)f_textures.get(model);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public ImmutableMap<String, TextureAtlasSprite> getTextures()
	{
		return getTexturesForOBJModel(this);
	}
}
