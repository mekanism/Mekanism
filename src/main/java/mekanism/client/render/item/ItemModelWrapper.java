package mekanism.client.render.item;

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

public class ItemModelWrapper implements IBakedModel
{
	private IBakedModel baseModel;
	private OverrideList override = new OverrideList();
	
	public ItemModelWrapper(IBakedModel base)
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
		return baseModel.getQuads(state, facing, rand);
	}

	@Override
	public boolean isAmbientOcclusion() 
	{
		return baseModel.isAmbientOcclusion();
	}

	@Override
	public boolean isGui3d() 
	{
		return baseModel.isGui3d();
	}

	@Override
	public boolean isBuiltInRenderer() 
	{
		return baseModel.isBuiltInRenderer();
	}

	@Nonnull
	@Override
	public TextureAtlasSprite getParticleTexture() 
	{
		return baseModel.getParticleTexture();
	}

	@Nonnull
	@Override
	public ItemCameraTransforms getItemCameraTransforms() 
	{
		return baseModel.getItemCameraTransforms();
	}
	
    private class OverrideList extends ItemOverrideList
    {
		public OverrideList()
		{
			super(Lists.newArrayList());
		}

		@Nonnull
	    @Override
	    public IBakedModel handleItemState(@Nonnull IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity)
	    {
	    	return new BakedCustomItemModel(baseModel, stack);
	    }
	}
}
