package mekanism.client.render.obj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.vecmath.Matrix4f;

import mekanism.api.MekanismConfig.client;
import mekanism.client.render.ctm.CTMModelFactory;
import mekanism.common.multipart.ColorProperty;
import mekanism.common.multipart.ConnectionProperty;
import mekanism.common.multipart.PartSidedPipe;
import mekanism.common.multipart.PartSidedPipe.ConnectionType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.obj.OBJModel.Face;
import net.minecraftforge.client.model.obj.OBJModel.Group;
import net.minecraftforge.client.model.obj.OBJModel.OBJProperty;
import net.minecraftforge.client.model.obj.OBJModel.OBJState;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.property.IExtendedBlockState;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

public class TransmitterModel extends OBJBakedModelBase
{
	private static Set<TransmitterModel> modelInstances = new HashSet<TransmitterModel>();
	
	private Map<Integer, List<BakedQuad>> modelCache = new HashMap<Integer, List<BakedQuad>>();
	private TransmitterModel itemCache;
	
	private IBlockState tempState;
	private ItemStack tempStack;
	
	private static TextureAtlasSprite[] transporter_center = new TextureAtlasSprite[2];
	private static TextureAtlasSprite[] transporter_center_color = new TextureAtlasSprite[2];
	private static TextureAtlasSprite[] transporter_side = new TextureAtlasSprite[2];
	private static TextureAtlasSprite[] transporter_side_color = new TextureAtlasSprite[2];
	
	private TransmitterOverride override = new TransmitterOverride();
	
	public TransmitterModel(IBakedModel base, OBJModel model, IModelState state, VertexFormat format, ImmutableMap<String, TextureAtlasSprite> textures, HashMap<TransformType, Matrix4f> transform)
	{
		super(base, model, state, format, textures, transform);
		
		modelInstances.add(this);
	}
	
	private class TransmitterOverride extends ItemOverrideList 
    {
		public TransmitterOverride() 
		{
			super(Lists.newArrayList());
		}

	    @Override
	    public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) 
	    {
			if(itemCache == null)
			{
				List<String> visible = new ArrayList<String>();
				
				for(EnumFacing side : EnumFacing.values())
				{
					visible.add(side.getName() + (side.getAxis() == Axis.Y ? "NORMAL" : "NONE"));
				}
				
				itemCache = new TransmitterModel(baseModel, getModel(), new OBJState(visible, true), vertexFormat, textureMap, transformationMap);
				itemCache.tempStack = stack;
			}
			
			return itemCache;
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
			IExtendedBlockState extended = (IExtendedBlockState)state;
			BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
			ColorProperty colorProp = extended.getValue(ColorProperty.INSTANCE);
			int color = -1;
			
			if(colorProp != null && colorProp.color != null)
			{
				color = extended.getValue(ColorProperty.INSTANCE).color.ordinal();
			}
			
			OBJState obj = extended.getValue(OBJProperty.INSTANCE);
			
			if(layer != BlockRenderLayer.TRANSLUCENT)
			{
				color = -1;
			}
			
			int hash = Objects.hash(layer.ordinal(), color, ConnectionProperty.INSTANCE.valueToString(extended.getValue(ConnectionProperty.INSTANCE)));
			
			if(obj.getVisibilityMap().containsKey(Group.ALL) || obj.getVisibilityMap().containsKey(Group.ALL_EXCEPT))
	        {
	            updateStateVisibilityMap(obj);
	        }
			
			if(!modelCache.containsKey(hash))
			{
				TransmitterModel model = new TransmitterModel(baseModel, getModel(), obj, vertexFormat, textureMap, transformationMap);
				model.tempState = state;
				modelCache.put(hash, model.getQuads(state, side, rand));
			}
			
			return modelCache.get(hash);
    	}
    	
    	return super.getQuads(state, side, rand);
	}
	
	@Override
	public float[] getOverrideColor(Face f, String groupName)
	{
		if(tempState != null)
		{
			ColorProperty prop = ((IExtendedBlockState)tempState).getValue(ColorProperty.INSTANCE);
			
			if(MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.TRANSLUCENT && prop != null && prop.color != null)
			{
				return new float[] {prop.color.getColor(0), prop.color.getColor(1), prop.color.getColor(2), 1};
			}
		}
		
		return null;
	}
	
	@Override
	public TextureAtlasSprite getOverrideTexture(Face f, String groupName)
	{
		if(tempState != null)
		{
			EnumFacing side = EnumFacing.getFacingFromVector(f.getNormal().x, f.getNormal().y, f.getNormal().z);
			ColorProperty prop = ((IExtendedBlockState)tempState).getValue(ColorProperty.INSTANCE);
			ConnectionProperty connection = ((IExtendedBlockState)tempState).getValue(ConnectionProperty.INSTANCE);
			boolean sideIconOverride = getIconStatus(side, connection) > 0;
			
			if(MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.TRANSLUCENT)
			{
				int opaqueVal = client.opaqueTransmitters ? 1 : 0;
				
				if(prop != null && prop.color != null)
				{
					return (!sideIconOverride && f.getMaterialName().contains("Center")) ? transporter_center_color[opaqueVal] : transporter_side_color[opaqueVal];
				}
				else {
					return (!sideIconOverride && f.getMaterialName().contains("Center")) ? transporter_center[opaqueVal] : transporter_side[opaqueVal];
				}
			}
			else {
				if(groupName.endsWith("NONE") && sideIconOverride)
				{
					for(String s : getModel().getMatLib().getMaterialNames())
					{
						if(s.contains("Texture.Name"))
						{
							continue;
						}
						
						if(!s.contains("Center") && !s.contains("Centre") && (client.opaqueTransmitters ? s.contains("Opaque") : !s.contains("Opaque")))
						{
							return textureMap.get(s);
						}
					}
				}
				else {
					if(client.opaqueTransmitters)
					{
						return textureMap.get(f.getMaterialName() + "_Opaque");
					}
				}
			}
		}
		
		return null;
	}
	
	@Override
	public boolean shouldRotate(Face f, String groupName)
	{
		if(tempState != null)
		{
			EnumFacing side = EnumFacing.getFacingFromVector(f.getNormal().x, f.getNormal().y, f.getNormal().z);
			ConnectionProperty connection = ((IExtendedBlockState)tempState).getValue(ConnectionProperty.INSTANCE);
			
			if(groupName.endsWith("NONE") && getIconStatus(side, connection) == 2)
			{
				return true;
			}
		}
		
		return false;
	}
	
	public byte getIconStatus(EnumFacing side, ConnectionProperty connection)
	{
		ConnectionType type = PartSidedPipe.getConnectionType(side, connection.connectionByte, connection.transmitterConnections, connection.connectionTypes);

		if(type == ConnectionType.NONE)
		{
			if(client.oldTransmitterRender || connection.renderCenter)
			{
				return (byte)0;
			}
			else if(connection.connectionByte == 3 && side != EnumFacing.DOWN && side != EnumFacing.UP)
			{
				return (byte)1;
			}
			else if(connection.connectionByte == 12 && (side == EnumFacing.DOWN || side == EnumFacing.UP))
			{
				return (byte)1;
			}
			else if(connection.connectionByte == 12 && (side == EnumFacing.EAST || side == EnumFacing.WEST))
			{
				return (byte)2;
			}
			else if(connection.connectionByte == 48 && side != EnumFacing.EAST && side != EnumFacing.WEST)
			{
				return (byte)2;
			}
		}
		
		return (byte)0;
	}
	
    @Override
    public Pair<? extends IPerspectiveAwareModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) 
    {
        return Pair.of(this, CTMModelFactory.transforms.get(cameraTransformType).getMatrix());
    }
	
	public static void registerIcons(TextureMap map)
	{
		transporter_center[0] = map.registerSprite(new ResourceLocation("mekanism:blocks/models/multipart/LogisticalTransporterGlass"));
		transporter_center_color[0] = map.registerSprite(new ResourceLocation("mekanism:blocks/models/multipart/LogisticalTransporterGlassColored"));
		transporter_side[0] = map.registerSprite(new ResourceLocation("mekanism:blocks/models/multipart/LogisticalTransporterVerticalGlass"));
		transporter_side_color[0] = map.registerSprite(new ResourceLocation("mekanism:blocks/models/multipart/LogisticalTransporterVerticalGlassColored"));
		
		transporter_center[1] = map.registerSprite(new ResourceLocation("mekanism:blocks/models/multipart/opaque/LogisticalTransporterGlass"));
		transporter_center_color[1] = map.registerSprite(new ResourceLocation("mekanism:blocks/models/multipart/opaque/LogisticalTransporterGlassColored"));
		transporter_side[1] = map.registerSprite(new ResourceLocation("mekanism:blocks/models/multipart/opaque/LogisticalTransporterVerticalGlass"));
		transporter_side_color[1] = map.registerSprite(new ResourceLocation("mekanism:blocks/models/multipart/opaque/LogisticalTransporterVerticalGlassColored"));
	}
	
	public static void clearCache()
	{
		for(TransmitterModel model : modelInstances)
		{
			model.modelCache.clear();
			model.itemCache = null;
		}
	}
}
