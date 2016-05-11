package mekanism.client.render.item;

import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Matrix4f;

import mekanism.client.model.ModelArmoredJetpack;
import mekanism.client.model.ModelAtomicDisassembler;
import mekanism.client.model.ModelFlamethrower;
import mekanism.client.model.ModelFreeRunners;
import mekanism.client.model.ModelGasMask;
import mekanism.client.model.ModelJetpack;
import mekanism.client.model.ModelScubaTank;
import mekanism.client.render.ctm.ModelChiselBlock;
import mekanism.client.render.tileentity.RenderBin;
import mekanism.client.render.tileentity.RenderFluidTank;
import mekanism.common.MekanismItems;
import mekanism.common.Tier.FluidTankTier;
import mekanism.common.block.states.BlockStateBasic.BasicBlockType;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.inventory.InventoryBin;
import mekanism.common.item.ItemAtomicDisassembler;
import mekanism.common.item.ItemBlockBasic;
import mekanism.common.item.ItemBlockMachine;
import mekanism.common.item.ItemFlamethrower;
import mekanism.common.item.ItemFreeRunners;
import mekanism.common.item.ItemGasMask;
import mekanism.common.item.ItemScubaTank;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
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

public class BakedCustomItemModel implements IBakedModel, IPerspectiveAwareModel
{
	private IBakedModel baseModel;
	private ItemStack stack;
	
	private Minecraft mc = Minecraft.getMinecraft();
	
	private static final RenderFluidTank fluidTankRenderer = (RenderFluidTank)TileEntityRendererDispatcher.instance.mapSpecialRenderers.get(TileEntityFluidTank.class);
	private final RenderBin binRenderer = (RenderBin)TileEntityRendererDispatcher.instance.mapSpecialRenderers.get(TileEntityBin.class);
	
	public static ModelJetpack jetpack = new ModelJetpack();
	public static ModelArmoredJetpack armoredJetpack = new ModelArmoredJetpack();
	public static ModelGasMask gasMask = new ModelGasMask();
	public static ModelScubaTank scubaTank = new ModelScubaTank();
	public static ModelFreeRunners freeRunners = new ModelFreeRunners();
	public static ModelAtomicDisassembler atomicDisassembler = new ModelAtomicDisassembler();
	public static ModelFlamethrower flamethrower = new ModelFlamethrower();
	
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
			if(basicType == BasicBlockType.BIN)
			{
				GlStateManager.pushMatrix();
				ItemBlockBasic itemBasic = (ItemBlockBasic)stack.getItem();
				InventoryBin inv = new InventoryBin(stack);
				binRenderer.render(EnumFacing.NORTH, inv.getItemType(), inv.getItemCount(), -0.5, -0.5, -0.5);
				Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
				GlStateManager.enableRescaleNormal();
		        GlStateManager.enableAlpha();
		        GlStateManager.alphaFunc(516, 0.1F);
		        GlStateManager.enableBlend();
		        GlStateManager.blendFunc(770, 771);
		        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				GlStateManager.popMatrix();
			}
			
			return;
		}
		
		MachineType machineType = MachineType.get(stack);
		
		if(machineType != null)
		{
			if(machineType == MachineType.FLUID_TANK)
			{
				GlStateManager.pushMatrix();
				ItemBlockMachine itemMachine = (ItemBlockMachine)stack.getItem();
				float targetScale = (float)(itemMachine.getFluidStack(stack) != null ? itemMachine.getFluidStack(stack).amount : 0)/itemMachine.getCapacity(stack);
				FluidTankTier tier = FluidTankTier.values()[itemMachine.getBaseTier(stack).ordinal()];
				Fluid fluid = itemMachine.getFluidStack(stack) != null ? itemMachine.getFluidStack(stack).getFluid() : null;
				fluidTankRenderer.render(tier, fluid, targetScale, false, null, -0.5, -0.5, -0.5);
				GlStateManager.popMatrix();
			}
			
			return;
		}
		
		GlStateManager.translate(-0.5, -0.5, -0.5);
		
		if(stack.getItem() == MekanismItems.Jetpack)
		{
			GlStateManager.pushMatrix();
			GlStateManager.rotate(180, 0.0F, 0.0F, 1.0F);
			GlStateManager.rotate(90, 0.0F, -1.0F, 0.0F);
			GlStateManager.translate(0.2F, -0.35F, 0.0F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "Jetpack.png"));
			jetpack.render(0.0625F);
			GlStateManager.popMatrix();
		}
		else if(stack.getItem() == MekanismItems.ArmoredJetpack)
		{
			GlStateManager.pushMatrix();
			GlStateManager.rotate(180, 0.0F, 0.0F, 1.0F);
			GlStateManager.rotate(90, 0.0F, -1.0F, 0.0F);
			GlStateManager.translate(0.2F, -0.35F, 0.0F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "Jetpack.png"));
			armoredJetpack.render(0.0625F);
			GlStateManager.popMatrix();
		}
		else if(stack.getItem() instanceof ItemGasMask)
		{
			GlStateManager.pushMatrix();
			GlStateManager.rotate(180, 0.0F, 0.0F, 1.0F);
			GlStateManager.rotate(90, 0.0F, -1.0F, 0.0F);
			GlStateManager.translate(0.1F, 0.2F, 0.0F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ScubaSet.png"));
			gasMask.render(0.0625F);
			GlStateManager.popMatrix();
		}
		else if(stack.getItem() instanceof ItemScubaTank)
		{
			GlStateManager.pushMatrix();
			GlStateManager.rotate(180, 0.0F, 0.0F, 1.0F);
			GlStateManager.rotate(90, 0.0F, -1.0F, 0.0F);
			GlStateManager.scale(1.6F, 1.6F, 1.6F);
			GlStateManager.translate(0.2F, -0.5F, 0.0F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ScubaSet.png"));
			scubaTank.render(0.0625F);
			GlStateManager.popMatrix();
		}
		else if(stack.getItem() instanceof ItemFreeRunners)
		{
			GlStateManager.pushMatrix();
			GlStateManager.rotate(180, 0.0F, 0.0F, 1.0F);
			GlStateManager.rotate(90, 0.0F, -1.0F, 0.0F);
			GlStateManager.scale(2.0F, 2.0F, 2.0F);
			GlStateManager.translate(0.2F, -1.43F, 0.12F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "FreeRunners.png"));
			freeRunners.render(0.0625F);
			GlStateManager.popMatrix();
		}
		else if(stack.getItem() instanceof ItemAtomicDisassembler)
		{
			GlStateManager.pushMatrix();
			GlStateManager.scale(1.4F, 1.4F, 1.4F);
			GlStateManager.rotate(180, 0.0F, 0.0F, 1.0F);

			if(type == TransformType.THIRD_PERSON)
			{
				GlStateManager.rotate(-45, 0.0F, 1.0F, 0.0F);
				GlStateManager.rotate(50, 1.0F, 0.0F, 0.0F);
				GlStateManager.scale(2.0F, 2.0F, 2.0F);
				GlStateManager.translate(0.0F, -0.4F, 0.4F);
			}
			else if(type == TransformType.GUI)
			{
				GlStateManager.rotate(225, 0.0F, 1.0F, 0.0F);
				GlStateManager.rotate(45, -1.0F, 0.0F, -1.0F);
				GlStateManager.scale(0.6F, 0.6F, 0.6F);
				GlStateManager.translate(0.0F, -0.2F, 0.0F);
			}
			else {
				GlStateManager.rotate(45, 0.0F, 1.0F, 0.0F);
				GlStateManager.translate(0.0F, -0.7F, 0.0F);
			}

			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "AtomicDisassembler.png"));
			atomicDisassembler.render(0.0625F);
			GlStateManager.popMatrix();
		}
		else if(stack.getItem() instanceof ItemFlamethrower)
		{
			GlStateManager.pushMatrix();
			GlStateManager.rotate(160, 0.0F, 0.0F, 1.0F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "Flamethrower.png"));
			
			GlStateManager.translate(0.0F, -1.0F, 0.0F);
			GlStateManager.rotate(135, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(-20, 0.0F, 0.0F, 1.0F);
			
			if(type == TransformType.FIRST_PERSON || type == TransformType.THIRD_PERSON)
			{
				if(type == TransformType.FIRST_PERSON)
				{
					GlStateManager.rotate(55, 0.0F, 1.0F, 0.0F);
				}
				else {
					GlStateManager.translate(0.0F, 0.5F, 0.0F);
				}
				
				GlStateManager.scale(2.5F, 2.5F, 2.5F);
				GlStateManager.translate(0.0F, -1.0F, -0.5F);
			}
			else if(type == TransformType.GUI)
			{
				GlStateManager.translate(-0.6F, 0.0F, 0.0F);
				GlStateManager.rotate(45, 0.0F, 1.0F, 0.0F);
			}
			
			flamethrower.render(0.0625F);
			GlStateManager.popMatrix();
		}
	}

	@Override
	public List<BakedQuad> getFaceQuads(EnumFacing facing)
	{
		List<BakedQuad> faceQuads = new LinkedList<BakedQuad>();
		
		if(Block.getBlockFromItem(stack.getItem()) != null)
		{
			faceQuads.addAll(baseModel.getFaceQuads(facing));
		}
		
		return faceQuads;
	}

	@Override
	public List<BakedQuad> getGeneralQuads()
	{
		List<BakedQuad> generalQuads = new LinkedList<BakedQuad>();
		
		if(Block.getBlockFromItem(stack.getItem()) != null)
		{
			generalQuads.addAll(baseModel.getGeneralQuads());
		}
		
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
        
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
    	doRender(cameraTransformType);
    	GlStateManager.scale(2.0F, 2.0F, 2.0F);
    	RenderHelper.enableStandardItemLighting();
    	GlStateManager.popMatrix();
    	
        return Pair.of(this, null);
    }
}
