package mekanism.client.render.ctm;

import java.util.Collections;
import java.util.List;

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
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

/**
 * Model for all chisel blocks
 */
@SuppressWarnings("deprecation")
public class CTMModelFactory implements ISmartBlockModel, ISmartItemModel, IPerspectiveAwareModel 
{
	public static final TRSRTransformation DEFAULT_BLOCK_THIRD_PERSON_TRANSOFRM = new TRSRTransformation(new Vector3f(0, 1.5f / 16f, -2.75f / 16f), TRSRTransformation.quatFromYXZDegrees(new Vector3f(10, -45, 170)), new Vector3f(0.375f, 0.375f, 0.375f), null);
    public static final Matrix4f DEFAULT_BLOCK_THIRD_PERSON_MATRIX = DEFAULT_BLOCK_THIRD_PERSON_TRANSOFRM.getMatrix();
    
    private List<BakedQuad> face;
    private List<BakedQuad> general;

    private ModelCTM model;

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
    	
    	if(stateIn.getBlock() instanceof ICTMBlock && stateIn instanceof IExtendedBlockState) 
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

    @Override
    public IBakedModel handleItemState(ItemStack stack) 
    {
        Block block = ((ItemBlock)stack.getItem()).getBlock();
        return createModel(block.getDefaultState(), model, null);
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

    private Pair<IPerspectiveAwareModel, Matrix4f> thirdPersonTransform;
    
    @Override
    public Pair<? extends IPerspectiveAwareModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) 
    {
        if(cameraTransformType == TransformType.THIRD_PERSON_RIGHT_HAND) 
        {
            if(thirdPersonTransform == null) 
            {
                thirdPersonTransform = ImmutablePair.of(this, DEFAULT_BLOCK_THIRD_PERSON_MATRIX);
            }
            
            return thirdPersonTransform;
        }
        
        return Pair.of(this, null);
    }
}
