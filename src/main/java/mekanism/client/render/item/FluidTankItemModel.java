package mekanism.client.render.item;

import java.util.HashMap;
import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.ISmartItemModel;

public class FluidTankItemModel implements ISmartItemModel
{
	public static final int STAGES = 16;
	
	public static final HashMap<String, IBakedModel>[] MODELS = new HashMap[STAGES];
	
	static
	{
		for(int x = 0; x < STAGES; x++)
		{
			MODELS[x] = new HashMap<String, IBakedModel>();
		}
	}
	
	private IBakedModel baseModel;
	
	public FluidTankItemModel(IBakedModel base)
	{
		baseModel = base;
	}
	
	@Override
	public IBakedModel handleItemState(ItemStack stack) 
	{
		return null;
	}
	
	@Override
	public List<BakedQuad> getFaceQuads(EnumFacing p_177551_1_) 
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public List<BakedQuad> getGeneralQuads() 
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isAmbientOcclusion() 
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isGui3d() 
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isBuiltInRenderer() 
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public TextureAtlasSprite getParticleTexture() 
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms() 
	{
		throw new UnsupportedOperationException();
	}
}
