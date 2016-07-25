package mekanism.client.render.ctm;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import mekanism.common.block.states.BlockStateBasic;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * Model for all chisel blocks
 */
@SuppressWarnings("deprecation")
public class CTMModelFactory implements IPerspectiveAwareModel 
{
    private List<BakedQuad> face;
    private List<BakedQuad> general;

    private ModelCTM model;
    
    private CTMOverride override = new CTMOverride();

    public CTMModelFactory(List<BakedQuad> f, List<BakedQuad> g, ModelCTM m)
    {
        face = f;
        general = g;
        model = m;
    }

    public CTMModelFactory(ModelCTM model)
    {
        this(Collections.emptyList(), Collections.emptyList(), model);
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState stateIn, EnumFacing side, long rand) 
    {
    	CTMModelFactory baked;
    	
    	if(stateIn != null && stateIn.getBlock() instanceof ICTMBlock && stateIn instanceof IExtendedBlockState) 
        {
            IExtendedBlockState state = (IExtendedBlockState)stateIn;
            CTMBlockRenderContext ctxList = state.getValue(BlockStateBasic.ctmProperty);
            baked = createModel(stateIn, model, ctxList);
        } 
        else {
            baked = this;
        }
    	
        return side == null ? baked.general : FluentIterable.from(baked.face).filter(quad -> quad.getFace() == side).toList();
    }

    @Override
    public boolean isAmbientOcclusion()
    {
        return true;
    }

    @Override
    public boolean isGui3d()
    {
        return true;
    }

    @Override
    public boolean isBuiltInRenderer()
    {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() 
    {
        return model.getDefaultFace().getParticle();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms()
    {
        return ItemCameraTransforms.DEFAULT;
    }
    
    private CTMModelFactory createModel(IBlockState state, ModelCTM model, CTMBlockRenderContext ctx) 
    {
        List<BakedQuad> faceQuads = Lists.newArrayList();
        List<BakedQuad> generalQuads = Lists.newArrayList();
        
        for(EnumFacing facing : EnumFacing.VALUES)
        {
            TextureCTM face = model.getFace(facing);
            ICTMBlock block = (ICTMBlock)state.getBlock();
            CTMData data = block.getCTMData(state);
            
            if(block.getOverrideTexture(state, facing) != null)
            {
            	face = CTMRegistry.textureCache.get(block.getOverrideTexture(state, facing));
            }

            int quadGoal = ctx == null ? 1 : CTM.QUADS_PER_SIDE;
            IBakedModel baked = model.getModel(state);
            List<BakedQuad> origFaceQuads = baked.getQuads(state, facing, 0);
            List<BakedQuad> origGeneralQuads = FluentIterable.from(baked.getQuads(state, null, 0)).filter(q -> q.getFace() == facing).toList();
            addAllQuads(origFaceQuads, face, ctx, state, quadGoal, faceQuads);
            addAllQuads(origGeneralQuads, face, ctx, state, quadGoal, generalQuads);
        }
        
        return new CTMModelFactory(faceQuads, generalQuads, model);
    }
    
    private void addAllQuads(List<BakedQuad> from, TextureCTM tex, @Nullable CTMBlockRenderContext ctx, IBlockState state, int quadGoal, List<BakedQuad> to)
    {
        for(BakedQuad q : from)
        {
            to.addAll(tex.transformQuad(q, ctx == null ? null : ctx, quadGoal));
        }
    }
    
    private static TRSRTransformation get(float tx, float ty, float tz, float ax, float ay, float az, float s) 
    {
        return new TRSRTransformation(
            new Vector3f(tx / 16, ty / 16, tz / 16),
            TRSRTransformation.quatFromXYZDegrees(new Vector3f(ax, ay, az)),
            new Vector3f(s, s, s),
            null);
    }

    public static Map<TransformType, TRSRTransformation> transforms = ImmutableMap.<TransformType, TRSRTransformation>builder()
            .put(TransformType.GUI,                         get(0, 0, 0, 30, 225, 0, 0.625f))
            .put(TransformType.THIRD_PERSON_RIGHT_HAND,     get(0, 2.5f, 0, 75, 45, 0, 0.375f))
            .put(TransformType.THIRD_PERSON_LEFT_HAND,      get(0, 2.5f, 0, 75, 45, 0, 0.375f))
            .put(TransformType.FIRST_PERSON_RIGHT_HAND,     get(0, 0, 0, 0, 45, 0, 0.4f))
            .put(TransformType.FIRST_PERSON_LEFT_HAND,      get(0, 0, 0, 0, 225, 0, 0.4f))
            .put(TransformType.GROUND,                      get(0, 2, 0, 0, 0, 0, 0.25f))
            .put(TransformType.HEAD,                        get(0, 0, 0, 0, 0, 0, 1))
            .put(TransformType.FIXED,                       get(0, 0, 0, 0, 0, 0, 1))
            .put(TransformType.NONE,                        get(0, 0, 0, 0, 0, 0, 0))
            .build();
    
    @Override
    public Pair<? extends IPerspectiveAwareModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) 
    {
    	return Pair.of(this, transforms.get(cameraTransformType).getMatrix());
    }
    
    @Override
    public ItemOverrideList getOverrides() 
    {
    	return override;
    }
    
    private class CTMOverride extends ItemOverrideList 
    {
		public CTMOverride() 
		{
			super(Lists.newArrayList());
		}

	    @Override
	    public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) 
	    {
	        Block block = ((ItemBlock)stack.getItem()).getBlock();
	        return createModel(block.getDefaultState(), model, null);
	    }
	}
}
