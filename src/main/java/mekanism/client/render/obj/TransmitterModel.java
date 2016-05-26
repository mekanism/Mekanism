package mekanism.client.render.obj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.vecmath.Matrix4f;

import mekanism.api.MekanismConfig.client;
import mekanism.common.multipart.ColorProperty;
import mekanism.common.multipart.ConnectionProperty;
import mekanism.common.multipart.PartSidedPipe;
import mekanism.common.multipart.PartSidedPipe.ConnectionType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
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
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.obj.OBJModel.Face;
import net.minecraftforge.client.model.obj.OBJModel.Group;
import net.minecraftforge.client.model.obj.OBJModel.OBJProperty;
import net.minecraftforge.client.model.obj.OBJModel.OBJState;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.property.IExtendedBlockState;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

public class TransmitterModel extends OBJBakedModelBase
{
	private Map<Integer, List<BakedQuad>> modelCache = new HashMap<Integer, List<BakedQuad>>();
	private TransmitterModel itemCache;
	
	private IBlockState tempState;
	private ItemStack tempStack;
	
	private static TextureAtlasSprite transporter_center;
	private static TextureAtlasSprite transporter_center_color;
	private static TextureAtlasSprite transporter_side;
	private static TextureAtlasSprite transporter_side_color;
	
	private TransmitterOverride override = new TransmitterOverride();
	
	public TransmitterModel(IBakedModel base, OBJModel model, IModelState state, VertexFormat format, ImmutableMap<String, TextureAtlasSprite> textures, HashMap<TransformType, Matrix4f> transform)
	{
		super(base, model, state, format, textures, transform);
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

		int hash = Objects.hash(layer.ordinal(), color, obj.hashCode());
		
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
				if(prop != null && prop.color != null)
				{
					return (!sideIconOverride && f.getMaterialName().contains("Center")) ? transporter_center_color : transporter_side_color;
				}
				else {
					return (!sideIconOverride && f.getMaterialName().contains("Center")) ? transporter_center : transporter_side;
				}
			}
			else {
				if(groupName.endsWith("NONE") && sideIconOverride)
				{
					for(Group g : getModel().getMatLib().getGroups().values())
					{
						for(Face testFace : g.getFaces())
						{
							String s = testFace.getMaterialName();
							
							if(!s.contains("Center") && !s.contains("Centre"))
							{
								return textureMap.get(s);
							}
						}
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
	
	public static void registerIcons(TextureMap map)
	{
		transporter_center = map.registerSprite(new ResourceLocation("mekanism:blocks/models/multipart/LogisticalTransporterGlass"));
		transporter_center_color = map.registerSprite(new ResourceLocation("mekanism:blocks/models/multipart/LogisticalTransporterGlassColored"));
		transporter_side = map.registerSprite(new ResourceLocation("mekanism:blocks/models/multipart/LogisticalTransporterVerticalGlass"));
		transporter_side_color = map.registerSprite(new ResourceLocation("mekanism:blocks/models/multipart/LogisticalTransporterVerticalGlassColored"));
	}
}
