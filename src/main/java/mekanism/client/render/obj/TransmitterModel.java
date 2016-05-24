package mekanism.client.render.obj;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.vecmath.Matrix4f;

import mcmultipart.client.multipart.ISmartMultipartModel;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.IModelState;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.obj.OBJModel.OBJBakedModel;

import com.google.common.collect.ImmutableMap;

public class TransmitterModel extends OBJBakedModel implements ISmartMultipartModel
{
	private IBakedModel baseModel;
	
	private HashMap<TransformType, Matrix4f> transformationMap = new HashMap<TransformType, Matrix4f>();
	
	private static Map<Integer, TransmitterModel> modelCache = new HashMap<Integer, TransmitterModel>();
	
	private Set<BakedQuad> bakedQuads;
	private IBlockState tempState;
	private TextureAtlasSprite tempSprite;
	private ItemStack tempStack;
	private ImmutableMap<String, TextureAtlasSprite> textureMap;
	
	public TransmitterModel(IBakedModel base, OBJModel model, IModelState state, VertexFormat format, ImmutableMap<String, TextureAtlasSprite> textures, HashMap<TransformType, Matrix4f> transform)
	{
		model.super(model, state, format, textures);
		baseModel = base;
		transformationMap = transform;
		textureMap = textures;
	}
	
	public static void forceRebake()
	{
		modelCache.clear();
	}

	@Override
	public IBakedModel handlePartState(IBlockState arg0)
	{
		return null;
	}
}
