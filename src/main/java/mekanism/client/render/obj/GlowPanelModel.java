package mekanism.client.render.obj;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Matrix4f;

import mekanism.api.EnumColor;
import mekanism.client.render.ctm.CTMModelFactory;
import mekanism.common.multipart.ColorProperty;
import mekanism.common.multipart.TileEntityGlowPanel;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.obj.OBJModel.Face;
import net.minecraftforge.client.model.obj.OBJModel.OBJBakedModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.property.IExtendedBlockState;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

public class GlowPanelModel extends OBJBakedModelBase
{
	private static Map<Integer, List<BakedQuad>> glowPanelCache = new HashMap<Integer, List<BakedQuad>>();
	private static Map<Integer, GlowPanelModel> glowPanelItemCache = new HashMap<Integer, GlowPanelModel>();
	
	private IBlockState tempState;
	private ItemStack tempStack;
	
	private GlowPanelOverride override = new GlowPanelOverride();
	
	public GlowPanelModel(IBakedModel base, OBJModel model, IModelState state, VertexFormat format, ImmutableMap<String, TextureAtlasSprite> textures, HashMap<TransformType, Matrix4f> transform)
	{
		super(base, model, state, format, textures, transform);
	}

	public static void forceRebake()
	{
		glowPanelCache.clear();
		glowPanelItemCache.clear();
	}
	
	public EnumColor getColor()
	{
		if(tempStack != null)
		{
			return EnumColor.DYES[tempStack.getItemDamage()];
		}
		
		if(tempState != null)
		{
			return ((IExtendedBlockState)tempState).getValue(ColorProperty.INSTANCE).color;
		}
		
		return EnumColor.WHITE;
	}
	
    private class GlowPanelOverride extends ItemOverrideList 
    {
		public GlowPanelOverride() 
		{
			super(Lists.newArrayList());
		}

	    @Override
	    public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) 
	    {
			if(glowPanelItemCache.containsKey(stack.getItemDamage()))
			{
				return glowPanelItemCache.get(stack.getItemDamage());
			}

			ImmutableMap.Builder<String, TextureAtlasSprite> builder = ImmutableMap.builder();
			builder.put(ModelLoader.White.LOCATION.toString(), ModelLoader.White.INSTANCE);
			TextureAtlasSprite missing = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(new ResourceLocation("missingno").toString());

			for(String s : getModel().getMatLib().getMaterialNames())
			{
				TextureAtlasSprite sprite = null;
				
				if(sprite == null)
				{
					sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(getModel().getMatLib().getMaterial(s).getTexture().getTextureLocation().toString());
				}
				
				if(sprite == null)
				{
					sprite = missing;
				}
				
				builder.put(s, sprite);
			}
			
			builder.put("missingno", missing);
			GlowPanelModel bakedModel = new GlowPanelModel(baseModel, getModel(), getState(), vertexFormat, builder.build(), transformationMap);
			bakedModel.tempStack = stack;
			glowPanelItemCache.put(stack.getItemDamage(), bakedModel);
			
			return bakedModel;
	    }
	}
	
	@Override
	public ItemOverrideList getOverrides()
	{
		return override;
	}
	
	@Override
	public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand)
	{
    	if(side != null) 
    	{
    		return ImmutableList.of();
    	}
    	
    	if(state != null && tempState == null)
    	{
	    	int hash = TileEntityGlowPanel.hash((IExtendedBlockState)state);
			EnumColor color = ((IExtendedBlockState)state).getValue(ColorProperty.INSTANCE).color;
			
			if(!glowPanelCache.containsKey(hash))
			{
				GlowPanelModel model = new GlowPanelModel(baseModel, getModel(), getState(), vertexFormat, textureMap, transformationMap);
				model.tempState = state;
				glowPanelCache.put(hash, model.getQuads(state, side, rand));
			}
			
			return glowPanelCache.get(hash);
    	}
    	
    	return super.getQuads(state, side, rand);
	}
	
	@Override
	public float[] getOverrideColor(Face f, String groupName)
	{
		if(groupName.equals("light"))
		{
			EnumColor c = getColor();
			return new float[] {c.getColor(0), c.getColor(1), c.getColor(2), 1};
		}
		
		return null;
	}

	private Pair<IPerspectiveAwareModel, Matrix4f> thirdPersonTransform;
    
    @Override
    public Pair<? extends IPerspectiveAwareModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType transformType) 
    {
    	if(transformType == TransformType.GUI)
    	{
    		GlStateManager.rotate(180, 1, 0, 0);
    		ForgeHooksClient.multiplyCurrentGlMatrix(CTMModelFactory.transforms.get(transformType).getMatrix());
    		GlStateManager.translate(0.65F, 0.45F, 0.0F);
    		GlStateManager.rotate(90, 1, 0, 0);
    		GlStateManager.scale(1.6F, 1.6F, 1.6F);
    		
    		return Pair.of(this, null);
    	}
    	else if(transformType == TransformType.FIRST_PERSON_RIGHT_HAND || transformType == TransformType.FIRST_PERSON_LEFT_HAND)
    	{
    		GlStateManager.translate(0.0F, 0.2F, 0.0F);
    	}
    	else if(transformType == TransformType.THIRD_PERSON_RIGHT_HAND || transformType == TransformType.THIRD_PERSON_LEFT_HAND) 
        {
    		ForgeHooksClient.multiplyCurrentGlMatrix(CTMModelFactory.transforms.get(transformType).getMatrix());
        	GlStateManager.translate(0.0F, 0.3F, 0.2F);
        	
        	return Pair.of(this, null);
        }
        
        return Pair.of(this, CTMModelFactory.transforms.get(transformType).getMatrix());
    }
}
