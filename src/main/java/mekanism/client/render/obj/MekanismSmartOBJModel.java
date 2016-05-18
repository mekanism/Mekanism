package mekanism.client.render.obj;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mcmultipart.client.multipart.ISmartMultipartModel;
import mekanism.api.EnumColor;
import mekanism.common.multipart.GlowPanelBlockState;
import mekanism.common.multipart.PartGlowPanel;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IModelPart;
import net.minecraftforge.client.model.IModelState;
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
import codechicken.lib.vec.Matrix4;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

public class MekanismSmartOBJModel extends OBJBakedModel implements ISmartMultipartModel
{
	private IBakedModel baseModel;
	
	private HashMap<TransformType, Matrix4> transformationMap = new HashMap<TransformType, Matrix4>();
	
	private static Map<Integer, MekanismSmartOBJModel> glowPanelCache = new HashMap<Integer, MekanismSmartOBJModel>();
	
	private Set<BakedQuad> bakedQuads;
	private IBlockState tempState;
	private TextureAtlasSprite tempSprite;
	
	public MekanismSmartOBJModel(IBakedModel base, OBJModel model, IModelState state, VertexFormat format, ImmutableMap<String, TextureAtlasSprite> textures, HashMap<TransformType, Matrix4> transform)
	{
		model.super(model, state, format, textures);
		baseModel = base;
		transformationMap = transform;
	}
	
	@Override
	public OBJBakedModel handlePartState(IBlockState state)
	{
		if(isGlowPanel(state))
		{
			int hash = PartGlowPanel.hash((IExtendedBlockState)state);
			EnumColor color = ((IExtendedBlockState)state).getValue(GlowPanelBlockState.colorState).color;
			
			if(!glowPanelCache.containsKey(hash))
			{
				MekanismSmartOBJModel model = new MekanismSmartOBJModel(baseModel, getModel(), getState(), getFormat(), getTextures(), transformationMap);
				model.tempState = state;
				glowPanelCache.put(hash, model);
			}
			
			return glowPanelCache.get(hash);
		}
		
		return this;
	}
	
	public boolean isGlowPanel(IBlockState state)
	{
		return state instanceof IExtendedBlockState && ((IExtendedBlockState)state).getUnlistedProperties().containsKey(GlowPanelBlockState.colorState);
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

			for(Group g : this.getModel().getMatLib().getGroups().values())
			{
				if(getState() instanceof OBJState)
				{
					OBJState state = (OBJState) this.getState();

					if(state.parent != null)
					{
						transform = state.parent.apply(Optional.<IModelPart> absent());
					}

					if(state.getGroupsWithVisibility(true).contains(g.getName()))
					{
						faces.addAll(g.applyTransform(transform));
					}
				}
				else {
					transform = getState().apply(Optional.<IModelPart> absent());
					LinkedHashSet<Face> tempFaces = g.applyTransform(transform);
					faces.addAll(tempFaces);

					if(isGlowPanel(tempState) && g.getName().equals("light"))
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
						if(!v.getMaterial().equals(this.getModel().getMatLib().getMaterial(v.getMaterial().getName())))
						{
							v.setMaterial(this.getModel().getMatLib().getMaterial(v.getMaterial().getName()));
						}
					}

					tempSprite = ModelLoader.White.instance;
				}
				else {
					tempSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(this.getModel().getMatLib().getMaterial(f.getMaterialName()).getTexture().getTextureLocation().toString());
				}

				float[] color = new float[] { 1, 1, 1, 1 };

				if(coloredFaces.contains(f))
				{
					EnumColor c = ((IExtendedBlockState) tempState).getValue(GlowPanelBlockState.colorState).color;
					color = new float[] {c.getColor(0), c.getColor(1), c.getColor(2), 1};
				}

				UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(getFormat());
				builder.setQuadOrientation(EnumFacing.getFacingFromVector(f.getNormal().x, f.getNormal().y, f.getNormal().z));
				builder.setQuadColored();
				builder.setQuadTint(0);
				
				Normal faceNormal = f.getNormal();
				putVertexData(builder, f.getVertices()[0], faceNormal, TextureCoordinate.getDefaultUVs()[0], tempSprite, color);
				putVertexData(builder, f.getVertices()[1], faceNormal, TextureCoordinate.getDefaultUVs()[1], tempSprite, color);
				putVertexData(builder, f.getVertices()[2], faceNormal, TextureCoordinate.getDefaultUVs()[2], tempSprite, color);
				putVertexData(builder, f.getVertices()[3], faceNormal, TextureCoordinate.getDefaultUVs()[3], tempSprite, color);
				
				bakedQuads.add(builder.build());
			}
		}

		List<BakedQuad> quadList = Collections.synchronizedList(Lists.newArrayList(bakedQuads));
		
		return quadList;
	}

	private final void putVertexData(UnpackedBakedQuad.Builder builder, Vertex v, Normal faceNormal, TextureCoordinate defUV, TextureAtlasSprite sprite, float[] color)
	{
		for(int e = 0; e < getFormat().getElementCount(); e++)
		{
			switch(getFormat().getElement(e).getUsage())
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
}
