package mekanism.client.render.item;

import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Matrix4f;

import mekanism.client.render.ctm.ModelChiselBlock;
import mekanism.client.render.tileentity.RenderFluidTank;
import mekanism.common.Tier.FluidTankTier;
import mekanism.common.block.states.BlockStateBasic.BasicBlockType;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.item.ItemBlockMachine;
import mekanism.common.tile.TileEntityFluidTank;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.Attributes;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.fluids.Fluid;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

public class BakedCustomItemModel implements IBakedModel, IPerspectiveAwareModel
{
	private IBakedModel baseModel;
	private ItemStack stack;
	
	private static final RenderFluidTank fluidTankRenderer = (RenderFluidTank)TileEntityRendererDispatcher.instance.mapSpecialRenderers.get(TileEntityFluidTank.class);
	
	public BakedCustomItemModel(IBakedModel model, ItemStack s)
	{
		baseModel = model;
		stack = s;
	}
	
	private void doRender(TransformType type)
	{
		BasicBlockType basicType = BasicBlockType.get(stack);
		
		if(basicType != null)
		{
			return;
		}
		
		MachineType machineType = MachineType.get(stack);
		
		if(machineType != null)
		{
			if(machineType == MachineType.FLUID_TANK)
			{
				GL11.glPushMatrix();
				ItemBlockMachine itemMachine = (ItemBlockMachine)stack.getItem();
				float targetScale = (float)(itemMachine.getFluidStack(stack) != null ? itemMachine.getFluidStack(stack).amount : 0)/itemMachine.getCapacity(stack);
				FluidTankTier tier = FluidTankTier.values()[itemMachine.getBaseTier(stack).ordinal()];
				Fluid fluid = itemMachine.getFluidStack(stack) != null ? itemMachine.getFluidStack(stack).getFluid() : null;
				fluidTankRenderer.render(tier, fluid, targetScale, false, null, -0.5, -0.5, -0.5);
				GL11.glPopMatrix();
			}
			
			return;
		}
	}

	@Override
	public List<BakedQuad> getFaceQuads(EnumFacing facing)
	{
		List<BakedQuad> faceQuads = new LinkedList<BakedQuad>();
		
		faceQuads.addAll(baseModel.getFaceQuads(facing));
		
		return faceQuads;
	}

	@Override
	public List<BakedQuad> getGeneralQuads()
	{
		List<BakedQuad> generalQuads = new LinkedList<BakedQuad>();
		
		generalQuads.addAll(baseModel.getGeneralQuads());
		
		return generalQuads;
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

	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return baseModel.getParticleTexture();
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms()
	{
		return baseModel.getItemCameraTransforms();
	}
	
    @Override
    public VertexFormat getFormat()
    {
        return Attributes.DEFAULT_BAKED_FORMAT;
    }
	
    @Override
    public Pair<? extends IFlexibleBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) 
    {    	
        if(cameraTransformType == TransformType.THIRD_PERSON) 
        {
            ForgeHooksClient.multiplyCurrentGlMatrix(ModelChiselBlock.DEFAULT_BLOCK_THIRD_PERSON_MATRIX);
        }
        
        GL11.glScalef(0.5F, 0.5F, 0.5F);
    	doRender(cameraTransformType);
    	GL11.glScalef(2.0F, 2.0F, 2.0F);
    	RenderHelper.enableStandardItemLighting();
    	
        return Pair.of(this, null);
    }
}
