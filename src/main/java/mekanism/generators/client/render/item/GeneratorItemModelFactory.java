package mekanism.generators.client.render.item;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import com.google.common.collect.Lists;

import javax.annotation.Nonnull;

public class GeneratorItemModelFactory implements IBakedModel
{
	private IBakedModel baseModel;
	
	private GeneratorOverride override = new GeneratorOverride();
	
	public GeneratorItemModelFactory(IBakedModel base)
	{
		baseModel = base;
	}

	@Nonnull
	@Override
	public ItemOverrideList getOverrides()
	{
		return override;
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(IBlockState state, EnumFacing facing, long rand) 
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
		return baseModel.isGui3d();
	}

	@Override
	public boolean isBuiltInRenderer() 
	{
		throw new UnsupportedOperationException();
	}

	@Nonnull
	@Override
	public TextureAtlasSprite getParticleTexture() 
	{
		throw new UnsupportedOperationException();
	}

	@Nonnull
	@Override
	public ItemCameraTransforms getItemCameraTransforms() 
	{
		throw new UnsupportedOperationException();
	}
	
	private class GeneratorOverride extends ItemOverrideList 
    {
		public GeneratorOverride() 
		{
			super(Lists.newArrayList());
		}

		@Nonnull
	    @Override
	    public IBakedModel handleItemState(@Nonnull IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity)
	    {
	    	return new BakedGeneratorItemModel(baseModel, stack);
	    }
	}
}
