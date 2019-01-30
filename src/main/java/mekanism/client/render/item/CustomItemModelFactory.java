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

public class CustomItemModelFactory implements IBakedModel
{
	private IBakedModel baseModel;
	
	private MachineOverride override = new MachineOverride();
	
	public CustomItemModelFactory(IBakedModel base)
	{
		baseModel = base;
	}
	
	@Override
	public ItemOverrideList getOverrides()
	{
		return override;
	}
	
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
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleTexture() 
	{
		throw new UnsupportedOperationException();
	}

	
    private class MachineOverride extends ItemOverrideList 
    {
		public MachineOverride() 
		{
			super(Lists.newArrayList());
		}

	    @Override
	    public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) 
	    {
	    	return new BakedCustomItemModel(baseModel, stack);
	    }
	}
}
