package mekanism.client.render.transmitter;

import java.util.HashMap;
import java.util.Map;

import mcmultipart.client.multipart.MultipartSpecialRenderer;
import mekanism.common.ColourRGBA;
import mekanism.common.multipart.PartTransmitter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.Attributes;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.pipeline.LightUtil;

import org.lwjgl.opengl.GL11;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

public abstract class RenderTransmitterBase extends MultipartSpecialRenderer
{
	private static OBJModel contentsModel;
	private static Map<String, IFlexibleBakedModel> contentsMap = new HashMap<String, IFlexibleBakedModel>();
	
	public RenderTransmitterBase()
	{
		if(contentsModel == null)
		{
			try {
				contentsModel = (OBJModel)OBJLoader.instance.loadModel(MekanismUtils.getResource(ResourceType.MODEL, "transmitter_contents.obj"));
				contentsMap = getModelsForGroups(contentsModel);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	protected void push()
	{
		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}

	protected void pop()
	{
		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}
	
	public static boolean isDrawing = false;
	
	public void renderTransparency(WorldRenderer renderer, TextureAtlasSprite icon, IFlexibleBakedModel cc, ColourRGBA color)
	{
		if(!isDrawing)
		{
			renderer.begin(GL11.GL_QUADS, cc.getFormat());
			isDrawing = true;
		}
		
		for(BakedQuad quad : cc.getGeneralQuads())
		{
			quad = iconTransform(quad, icon);
			LightUtil.renderQuadColor(renderer, quad, color.argb());
		}
	}
	
	public static HashMap<String, IFlexibleBakedModel> getModelsForGroups(OBJModel objModel) 
	{
		HashMap<String, IFlexibleBakedModel> modelParts = new HashMap<String, IFlexibleBakedModel>();

		if(!objModel.getMatLib().getGroups().keySet().isEmpty())
		{
			for(String key : objModel.getMatLib().getGroups().keySet()) 
			{
				String k = key;
				
				if(!modelParts.containsKey(key)) 
				{
					modelParts.put(k, objModel.bake(new OBJModel.OBJState(ImmutableList.of(k), false), Attributes.DEFAULT_BAKED_FORMAT, textureGetterFlipV));
				}
			}
		}

		return modelParts;
	}
	
	public IFlexibleBakedModel getModelForSide(PartTransmitter part, EnumFacing side)
	{
		String sideName = side.name().toLowerCase();
		String typeName = part.getConnectionType(side).name().toUpperCase();
		String name = sideName + typeName;

		return contentsMap.get(name);
	}
	
	/* Credit to Eternal Energy */
	public static Function<ResourceLocation, TextureAtlasSprite> textureGetterFlipV = new Function<ResourceLocation, TextureAtlasSprite>() 
	{
		@Override
		public TextureAtlasSprite apply(ResourceLocation location) 
		{
			return DummyAtlasTextureFlipV.instance;
		}
	};
	
	public static BakedQuad iconTransform(BakedQuad quad, TextureAtlasSprite sprite)
	{
		int[] vertices = new int[28];
		System.arraycopy(quad.getVertexData(), 0, vertices, 0, vertices.length);
		
		vertices[4] = Float.floatToRawIntBits(sprite.getInterpolatedU(16));
		vertices[5] = Float.floatToRawIntBits(sprite.getInterpolatedV(16));
		
		vertices[7+4] = Float.floatToRawIntBits(sprite.getInterpolatedU(16));
		vertices[7+5] = Float.floatToRawIntBits(sprite.getInterpolatedV(0));
		
		vertices[14+4] = Float.floatToRawIntBits(sprite.getInterpolatedU(0));
		vertices[14+5] = Float.floatToRawIntBits(sprite.getInterpolatedV(0));
		
		vertices[21+4] = Float.floatToRawIntBits(sprite.getInterpolatedU(0));
		vertices[21+5] = Float.floatToRawIntBits(sprite.getInterpolatedV(16));
		
		return new BakedQuad(vertices, quad.getTintIndex(), quad.getFace());
	}
    
    private static class DummyAtlasTextureFlipV extends TextureAtlasSprite 
    {
		public static DummyAtlasTextureFlipV instance = new DummyAtlasTextureFlipV();

		protected DummyAtlasTextureFlipV()
		{
			super("dummyFlipV");
		}

		@Override
		public float getInterpolatedU(double u)
		{
			return (float)u / 16;
		}

		@Override
		public float getInterpolatedV(double v) 
		{
			return (float)v / -16;
		}
	}
}
