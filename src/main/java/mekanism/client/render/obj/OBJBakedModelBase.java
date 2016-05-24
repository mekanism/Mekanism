package mekanism.client.render.obj;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.vecmath.Matrix4f;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.EnumFacing;
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

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

public abstract class OBJBakedModelBase extends OBJBakedModel
{
	protected IBakedModel baseModel;
	
	protected TextureAtlasSprite tempSprite = ModelLoader.White.instance;
	
	protected ImmutableMap<String, TextureAtlasSprite> textureMap;
	
	protected HashMap<TransformType, Matrix4f> transformationMap = new HashMap<TransformType, Matrix4f>();
	
	private Set<BakedQuad> bakedQuads;
	
	public OBJBakedModelBase(IBakedModel base, OBJModel model, IModelState state, VertexFormat format, ImmutableMap<String, TextureAtlasSprite> textures, HashMap<TransformType, Matrix4f> transform) 
	{
		model.super(model, state, format, textures);
		baseModel = base;
		transformationMap = transform;
		textureMap = textures;
		
        if(state instanceof OBJState)
        {
        	updateStateVisibilityMap((OBJState)state);
        }
	}
	
    @Override
    public List<BakedQuad> getGeneralQuads()
    {
        if(bakedQuads == null)
        {
        	bakedQuads = Collections.synchronizedSet(new LinkedHashSet<BakedQuad>());
            Set<Face> faces = Collections.synchronizedSet(new LinkedHashSet<Face>());
            Optional<TRSRTransformation> transform = Optional.absent();
            Map<Face, String> groupNameMap = new HashMap<Face, String>();
            
            for(Group g : getModel().getMatLib().getGroups().values())
            {
                if(getState() instanceof OBJState)
                {
                    OBJState state = (OBJState)getState();
                    
                    if(state.parent != null)
                    {
                        transform = state.parent.apply(Optional.absent());
                    }
                    
                    updateStateVisibilityMap(state);
                    
                    if(state.getGroupsWithVisibility(true).contains(g.getName()))
                    {
                    	Set<Face> groupFaces = g.applyTransform(transform);
                    	
                    	for(Face f : groupFaces)
                    	{
                    		groupNameMap.put(f, g.getName());
                    		faces.add(f);
                    	}
                    }
                }
                else {
                    transform = getState().apply(Optional.absent());
                    Set<Face> groupFaces = g.applyTransform(transform);
                    
                    for(Face f : groupFaces)
                	{
                		groupNameMap.put(f, g.getName());
                		faces.add(f);
                	}
                }
            }
            
            for(Face f : faces)
            {
            	String groupName = groupNameMap.get(f);
            	
            	if(getOverrideTexture(f, groupName) != null)
            	{
            		tempSprite = getOverrideTexture(f, groupName);
            	}
            	else if(getModel().getMatLib().getMaterial(f.getMaterialName()).isWhite())
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
                
                float[] color = new float[] {1, 1, 1, 1};
                
                if(getOverrideColor(f, groupName) != null)
                {
                	color = getOverrideColor(f, groupName);
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
	
	public static final void putVertexData(UnpackedBakedQuad.Builder builder, Vertex v, Normal faceNormal, TextureCoordinate defUV, TextureAtlasSprite sprite, VertexFormat format, float[] color)
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

	protected void updateStateVisibilityMap(OBJState state)
    {
        if(state.getVisibilityMap().containsKey(Group.ALL))
        {
            boolean operation = state.getVisibilityMap().get(Group.ALL);
            state.getVisibilityMap().clear();
            
            for(String s : getModel().getMatLib().getGroups().keySet())
            {
                state.getVisibilityMap().put(s,  OBJState.Operation.SET_TRUE.performOperation(operation));
            }
        }
        else if(state.getVisibilityMap().containsKey(Group.ALL_EXCEPT))
        {
            List<String> exceptList = state.getGroupNamesFromMap().subList(1, state.getGroupNamesFromMap().size());
            state.getVisibilityMap().remove(Group.ALL_EXCEPT);
          
            for(String s : this.getModel().getMatLib().getGroups().keySet())
            {
                if(!exceptList.contains(s))
                {
                    state.getVisibilityMap().put(s, OBJState.Operation.SET_TRUE.performOperation(state.getVisibilityMap().get(s)));
                }
            }
        }
        else {
            for(String s : state.getVisibilityMap().keySet())
            {
                state.getVisibilityMap().put(s, OBJState.Operation.SET_TRUE.performOperation(state.getVisibilityMap().get(s)));
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
	
    @Override
    public TextureAtlasSprite getParticleTexture()
    {
        return tempSprite;
    }
    
    protected float[] getOverrideColor(Face f, String groupName)
    {
    	return null;
    }
    
    protected TextureAtlasSprite getOverrideTexture(Face f, String groupName)
    {
    	return null;
    }
}
