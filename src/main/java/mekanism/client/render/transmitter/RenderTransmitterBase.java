package mekanism.client.render.transmitter;

import java.util.HashMap;
import java.util.Map;

import mekanism.client.render.MekanismRenderer;
import mekanism.common.ColourRGBA;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.Attributes;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.pipeline.LightUtil;

import org.lwjgl.opengl.GL11;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

public abstract class RenderTransmitterBase<T extends TileEntityTransmitter> extends TileEntitySpecialRenderer<T>
{
	private static OBJModel contentsModel;
	private static Map<String, IBakedModel> contentsMap = new HashMap<String, IBakedModel>();
	
	protected Minecraft mc = Minecraft.getMinecraft();
	
	public RenderTransmitterBase()
	{
		if(contentsModel == null)
		{
			try {
				contentsModel = (OBJModel)OBJLoader.INSTANCE.loadModel(MekanismUtils.getResource(ResourceType.MODEL, "transmitter_contents.obj"));
				contentsMap = buildModelMap(contentsModel);
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
	
	public void renderTransparency(VertexBuffer renderer, TextureAtlasSprite icon, IBakedModel cc, ColourRGBA color)
	{
		if(!renderer.isDrawing)
		{
			renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
		}
		
		for(EnumFacing side : EnumFacing.values())
		{
			for(BakedQuad quad : cc.getQuads(null, side, 0))
			{
				quad = MekanismRenderer.iconTransform(quad, icon);
				LightUtil.renderQuadColor(renderer, quad, color.argb());
			}
		}
		
		for(BakedQuad quad : cc.getQuads(null, null, 0))
		{
			quad = MekanismRenderer.iconTransform(quad, icon);
			LightUtil.renderQuadColor(renderer, quad, color.argb());
		}
	}
	
	public static HashMap<String, IBakedModel> buildModelMap(OBJModel objModel) 
	{
		HashMap<String, IBakedModel> modelParts = new HashMap<String, IBakedModel>();

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
	
	public IBakedModel getModelForSide(TileEntityTransmitter part, EnumFacing side)
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
