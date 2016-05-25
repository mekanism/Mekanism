package mekanism.generators.client.render.item;

import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public class GeneratorItemModelFactory implements ISmartItemModel
{
	private IBakedModel baseModel;
	
	public GeneratorItemModelFactory(IBakedModel base)
	{
		baseModel = base;
	}
	
	@Override
	public IBakedModel handleItemState(ItemStack stack) 
	{
		return new BakedGeneratorItemModel(baseModel, stack);
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
