package mekanism.client.render.obj;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.vecmath.Matrix4f;

import mcmultipart.client.multipart.ISmartMultipartModel;
import mekanism.common.multipart.ColorProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.IModelState;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.obj.OBJModel.Face;
import net.minecraftforge.client.model.obj.OBJModel.Group;
import net.minecraftforge.client.model.obj.OBJModel.OBJProperty;
import net.minecraftforge.client.model.obj.OBJModel.OBJState;
import net.minecraftforge.common.property.IExtendedBlockState;

import com.google.common.collect.ImmutableMap;

public class TransmitterModel extends OBJBakedModelBase implements ISmartMultipartModel
{
	private Map<Integer, TransmitterModel> modelCache = new HashMap<Integer, TransmitterModel>();
	
	private Set<BakedQuad> bakedQuads;
	private IBlockState tempState;
	private ItemStack tempStack;
	
	public TransmitterModel(IBakedModel base, OBJModel model, IModelState state, VertexFormat format, ImmutableMap<String, TextureAtlasSprite> textures, HashMap<TransformType, Matrix4f> transform)
	{
		super(base, model, state, format, textures, transform);
	}

	@Override
	public IBakedModel handlePartState(IBlockState state)
	{
		IExtendedBlockState extended = (IExtendedBlockState)state;
		EnumWorldBlockLayer layer = MinecraftForgeClient.getRenderLayer();
		int color = extended.getValue(ColorProperty.INSTANCE).color.ordinal();
		OBJState obj = extended.getValue(OBJProperty.instance);
		
		if(layer != EnumWorldBlockLayer.TRANSLUCENT)
		{
			color = -1;
		}

		int hash = Objects.hash(layer.ordinal(), 5, color, obj.hashCode());
		
		if(obj.getVisibilityMap().containsKey(Group.ALL) || obj.getVisibilityMap().containsKey(Group.ALL_EXCEPT))
        {
            updateStateVisibilityMap(obj);
        }
		
		if(!modelCache.containsKey(hash))
		{
			TransmitterModel model = new TransmitterModel(baseModel, getModel(), getState(), getFormat(), textureMap, transformationMap);
			model.tempState = state;
			modelCache.put(hash, model);
		}
		
		return modelCache.get(hash);
	}
	
	@Override
	public TextureAtlasSprite getOverrideTexture(Face f, String groupName)
	{
		System.out.println(groupName);
		return null;
	}
}
