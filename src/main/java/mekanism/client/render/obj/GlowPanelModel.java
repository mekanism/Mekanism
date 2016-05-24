package mekanism.client.render.obj;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.vecmath.Matrix4f;

import mcmultipart.client.multipart.ISmartMultipartModel;
import mekanism.api.EnumColor;
import mekanism.client.render.ctm.CTMModelFactory;
import mekanism.common.multipart.ColorProperty;
import mekanism.common.multipart.PartGlowPanel;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IModelState;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.TRSRTransformation;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.obj.OBJModel.Face;
import net.minecraftforge.client.model.obj.OBJModel.Group;
import net.minecraftforge.client.model.obj.OBJModel.Normal;
import net.minecraftforge.client.model.obj.OBJModel.OBJBakedModel;
import net.minecraftforge.client.model.obj.OBJModel.OBJState;
import net.minecraftforge.client.model.obj.OBJModel.TextureCoordinate;
import net.minecraftforge.client.model.obj.OBJModel.Vertex;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.property.IExtendedBlockState;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

public class GlowPanelModel extends OBJBakedModel implements ISmartMultipartModel
{
	private IBakedModel baseModel;
	
	private HashMap<TransformType, Matrix4f> transformationMap = new HashMap<TransformType, Matrix4f>();
	
	private static Map<Integer, GlowPanelModel> glowPanelCache = new HashMap<Integer, GlowPanelModel>();
	private static Map<Integer, GlowPanelModel> glowPanelItemCache = new HashMap<Integer, GlowPanelModel>();
	
	private Set<BakedQuad> bakedQuads;
	private IBlockState tempState;
	private TextureAtlasSprite tempSprite;
	private ItemStack tempStack;
	private ImmutableMap<String, TextureAtlasSprite> textureMap;
	
	public GlowPanelModel(IBakedModel base, OBJModel model, IModelState state, VertexFormat format, ImmutableMap<String, TextureAtlasSprite> textures, HashMap<TransformType, Matrix4f> transform)
	{
		model.super(model, state, format, textures);
		baseModel = base;
		transformationMap = transform;
		textureMap = textures;
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
	
	@Override
	public OBJBakedModel handlePartState(IBlockState state)
	{
		int hash = PartGlowPanel.hash((IExtendedBlockState)state);
		EnumColor color = ((IExtendedBlockState)state).getValue(ColorProperty.INSTANCE).color;
		
		if(!glowPanelCache.containsKey(hash))
		{
			GlowPanelModel model = new GlowPanelModel(baseModel, getModel(), getState(), getFormat(), getTextures(), transformationMap);
			model.tempState = state;
			glowPanelCache.put(hash, model);
		}
		
		return glowPanelCache.get(hash);
	}
	
	@Override
	public IBakedModel handleItemState(ItemStack stack)
	{
		if(glowPanelItemCache.containsKey(stack.getItemDamage()))
		{
			return glowPanelItemCache.get(stack.getItemDamage());
		}

		ImmutableMap.Builder<String, TextureAtlasSprite> builder = ImmutableMap.builder();
		builder.put(ModelLoader.White.loc.toString(), ModelLoader.White.instance);
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
		GlowPanelModel bakedModel = new GlowPanelModel(baseModel, getModel(), getState(), getFormat(), builder.build(), transformationMap);
		bakedModel.tempStack = stack;
		glowPanelItemCache.put(stack.getItemDamage(), bakedModel);
		
		return bakedModel;
	}

	@Override
	public List<BakedQuad> getGeneralQuads()
	{
		if(bakedQuads == null)
		{
			bakedQuads = Collections.synchronizedSet(new LinkedHashSet<BakedQuad>());
			Set<Face> faces = Collections.synchronizedSet(new LinkedHashSet<Face>());
			Optional<TRSRTransformation> transform = Optional.absent();
			Set<Face> coloredFaces = new HashSet<Face>();

			for(Group g : getModel().getMatLib().getGroups().values())
			{
				if(getState() instanceof OBJState)
				{
					OBJState state = (OBJState)getState();

					if(state.parent != null)
					{
						transform = state.parent.apply(Optional.absent());
					}

					if(state.getGroupsWithVisibility(true).contains(g.getName()))
					{
						LinkedHashSet<Face> tempFaces = g.applyTransform(transform);
						faces.addAll(tempFaces);

						if(g.getName().equals("light"))
						{
							coloredFaces.addAll(tempFaces);
						}
					}
				}
				else {
					transform = getState().apply(Optional.absent());
					LinkedHashSet<Face> tempFaces = g.applyTransform(transform);
					faces.addAll(tempFaces);

					if(g.getName().equals("light"))
					{
						coloredFaces.addAll(tempFaces);
					}
				}
			}

			for(Face f : faces)
			{
				if(getModel().getMatLib().getMaterial(f.getMaterialName()).isWhite())
				{
					for(Vertex v : f.getVertices())
					{
						if(!v.getMaterial().equals(getModel().getMatLib().getMaterial(v.getMaterial().getName())))
						{
							v.setMaterial(getModel().getMatLib().getMaterial(v.getMaterial().getName()));
						}
					}

					tempSprite = ModelLoader.White.instance;
				}
				else {
					tempSprite = textureMap.get(f.getMaterialName());
				}

				float[] color = new float[] { 1, 1, 1, 1 };

				if(coloredFaces.contains(f))
				{
					EnumColor c = getColor();
					color = new float[] {c.getColor(0), c.getColor(1), c.getColor(2), 1};
				}

				UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(getFormat());
				builder.setQuadOrientation(EnumFacing.getFacingFromVector(f.getNormal().x, f.getNormal().y, f.getNormal().z));
				builder.setQuadColored();
				builder.setQuadTint(0);
				
				Normal faceNormal = f.getNormal();
				putVertexData(builder, f.getVertices()[0], faceNormal, TextureCoordinate.getDefaultUVs()[0], tempSprite, getFormat(), color);
				putVertexData(builder, f.getVertices()[1], faceNormal, TextureCoordinate.getDefaultUVs()[1], tempSprite, getFormat(), color);
				putVertexData(builder, f.getVertices()[2], faceNormal, TextureCoordinate.getDefaultUVs()[2], tempSprite, getFormat(), color);
				putVertexData(builder, f.getVertices()[3], faceNormal, TextureCoordinate.getDefaultUVs()[3], tempSprite, getFormat(), color);
				
				bakedQuads.add(builder.build());
			}
		}

		List<BakedQuad> quadList = Collections.synchronizedList(Lists.newArrayList(bakedQuads));
		
		return quadList;
	}

	private static final void putVertexData(UnpackedBakedQuad.Builder builder, Vertex v, Normal faceNormal, TextureCoordinate defUV, TextureAtlasSprite sprite, VertexFormat format, float[] color)
	{
		for(int e = 0; e < format.getElementCount(); e++)
		{
			switch(format.getElement(e).getUsage())
			{
				case POSITION:
					builder.put(e, v.getPos().x, v.getPos().y, v.getPos().z, v.getPos().w);
					break;
				case COLOR:
					float d;
					
					if(v.hasNormal())
					{
						d = LightUtil.diffuseLight(v.getNormal().x, v.getNormal().y, v.getNormal().z);
					}
					else {
						d = LightUtil.diffuseLight(faceNormal.x, faceNormal.y, faceNormal.z);
					}

					if(v.getMaterial() != null)
					{
						builder.put(e, d * v.getMaterial().getColor().x * color[0], d * v.getMaterial().getColor().y * color[1], d * v.getMaterial().getColor().z * color[2], v.getMaterial().getColor().w * color[3]);
					}
					else {
						builder.put(e, d, d, d, 1);
					}
					
					break;
				case UV:
					if(!v.hasTextureCoordinate())
					{
						builder.put(e, sprite.getInterpolatedU(defUV.u * 16), sprite.getInterpolatedV((1 - defUV.v) * 16), 0, 1);
					}
					else {
						builder.put(e, sprite.getInterpolatedU(v.getTextureCoordinate().u * 16), sprite.getInterpolatedV((1 - v.getTextureCoordinate().v) * 16), 0, 1);
					}
					
					break;
				case NORMAL:
					if(!v.hasNormal())
					{
						builder.put(e, faceNormal.x, faceNormal.y, faceNormal.z, 0);
					}
					else {
						builder.put(e, v.getNormal().x, v.getNormal().y, v.getNormal().z, 0);
					}

					break;
				default:
					builder.put(e);
			}
		}
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
	
	private Pair<IPerspectiveAwareModel, Matrix4f> thirdPersonTransform;
    
    @Override
    public Pair<? extends IFlexibleBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) 
    {
    	if(cameraTransformType == TransformType.GUI)
    	{
    		GlStateManager.translate(0.3F, 0.25F, 0.0F);
    		GlStateManager.rotate(90, 1, 0, 0);
    		GlStateManager.scale(1.6F, 1.6F, 1.6F);
    	}
    	else if(cameraTransformType == TransformType.FIRST_PERSON)
    	{
    		GlStateManager.translate(0.0F, 0.3F, 0.0F);
    	}
    	else if(cameraTransformType == TransformType.THIRD_PERSON) 
        {
        	GlStateManager.translate(0.0F, -0.1F, 0.0F);
        	
            if(thirdPersonTransform == null) 
            {
                thirdPersonTransform = ImmutablePair.of(this, CTMModelFactory.DEFAULT_BLOCK_THIRD_PERSON_MATRIX);
            }
            
            return thirdPersonTransform;
        }
        
        return Pair.of(this, null);
    }
}
