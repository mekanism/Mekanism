package mekanism.client.render.item;

import mekanism.api.EnumColor;
import mekanism.api.energy.IEnergizedItem;
import mekanism.client.ClientProxy;
import mekanism.client.MekanismClient;
import mekanism.client.model.ModelArmoredJetpack;
import mekanism.client.model.ModelAtomicDisassembler;
import mekanism.client.model.ModelEnergyCube;
import mekanism.client.model.ModelEnergyCube.ModelEnergyCore;
import mekanism.client.model.ModelFlamethrower;
import mekanism.client.model.ModelFreeRunners;
import mekanism.client.model.ModelGasMask;
import mekanism.client.model.ModelGasTank;
import mekanism.client.model.ModelJetpack;
import mekanism.client.model.ModelObsidianTNT;
import mekanism.client.model.ModelRobit;
import mekanism.client.model.ModelScubaTank;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.RenderGlowPanel;
import mekanism.client.render.RenderPartTransmitter;
import mekanism.client.render.entity.RenderBalloon;
import mekanism.client.render.tileentity.RenderBin;
import mekanism.client.render.tileentity.RenderEnergyCube;
import mekanism.client.render.tileentity.RenderFluidTank;
import mekanism.common.MekanismBlocks;
import mekanism.common.MekanismItems;
import mekanism.common.SideData.IOState;
import mekanism.common.Tier.BaseTier;
import mekanism.common.Tier.EnergyCubeTier;
import mekanism.common.Tier.FluidTankTier;
import mekanism.common.base.IEnergyCube;
import mekanism.common.block.BlockBasic.BasicType;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.inventory.InventoryBin;
import mekanism.common.item.ItemAtomicDisassembler;
import mekanism.common.item.ItemBalloon;
import mekanism.common.item.ItemBlockBasic;
import mekanism.common.item.ItemBlockGasTank;
import mekanism.common.item.ItemBlockMachine;
import mekanism.common.item.ItemFlamethrower;
import mekanism.common.item.ItemFreeRunners;
import mekanism.common.item.ItemGasMask;
import mekanism.common.item.ItemRobit;
import mekanism.common.item.ItemScubaTank;
import mekanism.common.item.ItemWalkieTalkie;
import mekanism.common.multipart.ItemGlowPanel;
import mekanism.common.multipart.ItemPartTransmitter;
import mekanism.common.multipart.TransmitterType;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelChest;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ItemRenderingHandler implements IItemRenderer
{
	private Minecraft mc = Minecraft.getMinecraft();
	
	public ModelRobit robit = new ModelRobit();
	public ModelChest personalChest = new ModelChest();
	public ModelEnergyCube energyCube = new ModelEnergyCube();
	public ModelEnergyCore energyCore = new ModelEnergyCore();
	public ModelGasTank gasTank = new ModelGasTank();
	public ModelObsidianTNT obsidianTNT = new ModelObsidianTNT();
	public ModelJetpack jetpack = new ModelJetpack();
	public ModelArmoredJetpack armoredJetpack = new ModelArmoredJetpack();
	public ModelGasMask gasMask = new ModelGasMask();
	public ModelScubaTank scubaTank = new ModelScubaTank();
	public ModelFreeRunners freeRunners = new ModelFreeRunners();
	public ModelAtomicDisassembler atomicDisassembler = new ModelAtomicDisassembler();
	public ModelFlamethrower flamethrower = new ModelFlamethrower();

	private final RenderBalloon balloonRenderer = new RenderBalloon();
	private final RenderBin binRenderer = (RenderBin)TileEntityRendererDispatcher.instance.mapSpecialRenderers.get(TileEntityBin.class);
	private final RenderFluidTank portableTankRenderer = (RenderFluidTank)TileEntityRendererDispatcher.instance.mapSpecialRenderers.get(TileEntityFluidTank.class);
	private final RenderItem renderItem = (RenderItem)RenderManager.instance.getEntityClassRenderObject(EntityItem.class);

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type)
	{
		if(item.getItem() == MekanismItems.WalkieTalkie)
		{
			return type != ItemRenderType.INVENTORY;
		}

		return true;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
	{
		return true;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data)
	{
		RenderBlocks renderBlocks = (RenderBlocks)data[0];

		if(type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON)
		{
			GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		}

		if(item.getItem() instanceof IEnergyCube)
		{
			EnergyCubeTier tier = ((IEnergyCube)item.getItem()).getEnergyCubeTier(item);
			IEnergizedItem energized = (IEnergizedItem)item.getItem();
			mc.renderEngine.bindTexture(RenderEnergyCube.baseTexture);

			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(270F, 0.0F, -1.0F, 0.0F);
			GL11.glTranslatef(0.0F, -1.0F, 0.0F);

			MekanismRenderer.blendOn();
			
			energyCube.render(0.0625F, tier, mc.renderEngine, true);
			
			for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				mc.renderEngine.bindTexture(RenderEnergyCube.baseTexture);
				energyCube.renderSide(0.0625F, side, side == ForgeDirection.NORTH ? IOState.OUTPUT : IOState.INPUT, tier, mc.renderEngine);
			}
			
			MekanismRenderer.blendOff();

			GL11.glPushMatrix();
			GL11.glTranslatef(0.0f, 1.0f, 0.0f);
			mc.renderEngine.bindTexture(RenderEnergyCube.coreTexture);

			GL11.glShadeModel(GL11.GL_SMOOTH);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			MekanismRenderer.glowOn();

			EnumColor c = tier.getBaseTier().getColor();

			GL11.glPushMatrix();
			GL11.glScalef(0.4F, 0.4F, 0.4F);
			GL11.glColor4f(c.getColor(0), c.getColor(1), c.getColor(2), (float)(energized.getEnergy(item)/energized.getMaxEnergy(item)));
			GL11.glTranslatef(0, (float)Math.sin(Math.toRadians((MekanismClient.ticksPassed + MekanismRenderer.getPartialTick()) * 3)) / 7, 0);
			GL11.glRotatef((MekanismClient.ticksPassed + MekanismRenderer.getPartialTick()) * 4, 0, 1, 0);
			GL11.glRotatef(36F + (MekanismClient.ticksPassed + MekanismRenderer.getPartialTick()) * 4, 0, 1, 1);
			energyCore.render(0.0625F, true);
			GL11.glPopMatrix();

			MekanismRenderer.glowOff();

			GL11.glShadeModel(GL11.GL_FLAT);
			GL11.glDisable(GL11.GL_LINE_SMOOTH);
			GL11.glDisable(GL11.GL_POLYGON_SMOOTH);
			GL11.glDisable(GL11.GL_BLEND);

			GL11.glPopMatrix();
		}
		else if(BasicType.get(item) == BasicType.INDUCTION_CELL || BasicType.get(item) == BasicType.INDUCTION_PROVIDER)
		{
			MekanismRenderer.renderCustomItem((RenderBlocks)data[0], item);
		}
		else if(BasicType.get(item) == BasicType.BIN)
		{
			GL11.glRotatef(270, 0.0F, 1.0F, 0.0F);
			MekanismRenderer.renderCustomItem((RenderBlocks)data[0], item);
			GL11.glRotatef(-270, 0.0F, 1.0F, 0.0F);
			
			if(binRenderer == null || binRenderer.func_147498_b()/*getFontRenderer()*/ == null)
			{
				return;
			}

			InventoryBin inv = new InventoryBin(item);
			ForgeDirection side = ForgeDirection.getOrientation(2);

			String amount = "";
			ItemStack itemStack = inv.getStack();

			if(itemStack != null)
			{
				amount = Integer.toString(inv.getItemCount());
			}

			MekanismRenderer.glowOn();

			if(itemStack != null)
			{
				GL11.glPushMatrix();

				if(!(itemStack.getItem() instanceof ItemBlock) || Block.getBlockFromItem(itemStack.getItem()).getRenderType() != 0)
				{
					GL11.glRotatef(180, 0, 0, 1);
					GL11.glTranslatef(-1.02F, -0.2F, 0);

					if(type == ItemRenderType.INVENTORY)
					{
						GL11.glTranslatef(-0.45F, -0.4F, 0.0F);
					}
				}

				if(type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON || type == ItemRenderType.ENTITY)
				{
					GL11.glTranslatef(-0.22F, -0.2F, -0.22F);
				}

				GL11.glTranslatef(0.73f, 0.08f, 0.44f);
				GL11.glRotatef(90, 0, 1, 0);

				float scale = 0.03125F;
				float scaler = 0.9F;

				GL11.glScalef(scale*scaler, scale*scaler, 0);

				TextureManager renderEngine = mc.renderEngine;

				GL11.glDisable(GL11.GL_LIGHTING);

				renderItem.renderItemAndEffectIntoGUI(binRenderer.func_147498_b()/*getFontRenderer()*/, renderEngine, itemStack, 0, 0);

				GL11.glEnable(GL11.GL_LIGHTING);
				GL11.glPopMatrix();
			}
			
			MekanismRenderer.glowOff();

			if(amount != "")
			{
				float maxScale = 0.02F;

				GL11.glPushMatrix();

				GL11.glPolygonOffset(-10, -10);
				GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);

				float displayWidth = 1 - (2 / 16);
				float displayHeight = 1 - (2 / 16);
				GL11.glTranslatef(0, -0.31F, 0);

				if(type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON || type == ItemRenderType.ENTITY)
				{
					GL11.glTranslatef(-0.5f, -0.4f, -0.5f);
				}

				GL11.glTranslatef(0, 0.9F, 1);
				GL11.glRotatef(90, 0, 1, 0);
				GL11.glRotatef(90, 1, 0, 0);

				GL11.glTranslatef(displayWidth / 2, 1F, displayHeight / 2);
				GL11.glRotatef(-90, 1, 0, 0);

				FontRenderer fontRenderer = binRenderer.func_147498_b();//getFontRenderer();

				int requiredWidth = Math.max(fontRenderer.getStringWidth(amount), 1);
				int lineHeight = fontRenderer.FONT_HEIGHT + 2;
				int requiredHeight = lineHeight * 1;
				float scaler = 0.4F;
				float scaleX = (displayWidth / requiredWidth);
				float scale = scaleX * scaler;

				if(maxScale > 0)
				{
					scale = Math.min(scale, maxScale);
				}

				GL11.glScalef(scale, -scale, scale);
				GL11.glDepthMask(false);

				int offsetX;
				int offsetY;
				int realHeight = (int)Math.floor(displayHeight / scale);
				int realWidth = (int)Math.floor(displayWidth / scale);

				offsetX = (realWidth - requiredWidth) / 2;
				offsetY = (realHeight - requiredHeight) / 2;

				GL11.glDisable(GL11.GL_LIGHTING);
				fontRenderer.drawString("\u00a7f" + amount, offsetX - (realWidth / 2), 1 + offsetY - (realHeight / 2), 1);
				GL11.glEnable(GL11.GL_LIGHTING);
				GL11.glDepthMask(true);
				GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);

				GL11.glPopMatrix();
			}
		}
		else if(Block.getBlockFromItem(item.getItem()) == MekanismBlocks.GasTank)
		{
			GL11.glPushMatrix();
			
			BaseTier tier = ((ItemBlockGasTank)item.getItem()).getBaseTier(item);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "GasTank" + tier.getName() + ".png"));
			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(90F, 0.0F, 1.0F, 0.0F);
			GL11.glTranslatef(0.0F, -1.0F, 0.0F);
			gasTank.render(0.0625F);
			
			GL11.glPopMatrix();
		}
		else if(Block.getBlockFromItem(item.getItem()) == MekanismBlocks.ObsidianTNT)
		{
			GL11.glPushMatrix();
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ObsidianTNT.png"));
			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(180F, 0.0F, -1.0F, 0.0F);
			GL11.glTranslatef(0.0F, -1.0F, 0.0F);
			obsidianTNT.render(0.0625F);
			GL11.glPopMatrix();
		}
		else if(item.getItem() instanceof ItemWalkieTalkie)
		{
			if(((ItemWalkieTalkie)item.getItem()).getOn(item))
			{
				MekanismRenderer.glowOn();
			}

			MekanismRenderer.renderItem(item);

			if(((ItemWalkieTalkie)item.getItem()).getOn(item))
			{
				MekanismRenderer.glowOff();
			}
		}
		else if(MachineType.get(item) == MachineType.PERSONAL_CHEST)
		{
			GL11.glPushMatrix();
			ItemBlockMachine chest = (ItemBlockMachine)item.getItem();

			GL11.glRotatef(90F, 0.0F, 1.0F, 0.0F);
			GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
			GL11.glTranslatef(0, 1.0F, 1.0F);
			GL11.glScalef(1.0F, -1F, -1F);

			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "PersonalChest.png"));

			personalChest.renderAll();
			GL11.glPopMatrix();
		}
		else if(item.getItem() instanceof ItemRobit)
		{
			GL11.glPushMatrix();
			GL11.glRotatef(180, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(90, 0.0F, -1.0F, 0.0F);
			GL11.glTranslatef(0.0F, -1.5F, 0.0F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "Robit.png"));
			robit.render(0.08F);
			GL11.glPopMatrix();
		}
		else if(item.getItem() == MekanismItems.Jetpack)
		{
			GL11.glPushMatrix();
			GL11.glRotatef(180, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(90, 0.0F, -1.0F, 0.0F);
			GL11.glTranslatef(0.2F, -0.35F, 0.0F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "Jetpack.png"));
			jetpack.render(0.0625F);
			GL11.glPopMatrix();
		}
		else if(item.getItem() == MekanismItems.ArmoredJetpack)
		{
			GL11.glPushMatrix();
			GL11.glRotatef(180, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(90, 0.0F, -1.0F, 0.0F);
			GL11.glTranslatef(0.2F, -0.35F, 0.0F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "Jetpack.png"));
			armoredJetpack.render(0.0625F);
			GL11.glPopMatrix();
		}
		else if(item.getItem() instanceof ItemGasMask)
		{
			GL11.glPushMatrix();
			GL11.glRotatef(180, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(90, 0.0F, -1.0F, 0.0F);
			GL11.glTranslatef(0.1F, 0.2F, 0.0F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ScubaSet.png"));
			gasMask.render(0.0625F);
			GL11.glPopMatrix();
		}
		else if(item.getItem() instanceof ItemScubaTank)
		{
			GL11.glPushMatrix();
			GL11.glRotatef(180, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(90, 0.0F, -1.0F, 0.0F);
			GL11.glScalef(1.6F, 1.6F, 1.6F);
			GL11.glTranslatef(0.2F, -0.5F, 0.0F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ScubaSet.png"));
			scubaTank.render(0.0625F);
			GL11.glPopMatrix();
		}
		else if(item.getItem() instanceof ItemFreeRunners)
		{
			GL11.glPushMatrix();
			GL11.glRotatef(180, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(90, 0.0F, -1.0F, 0.0F);
			GL11.glScalef(2.0F, 2.0F, 2.0F);
			GL11.glTranslatef(0.2F, -1.43F, 0.12F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "FreeRunners.png"));
			freeRunners.render(0.0625F);
			GL11.glPopMatrix();
		}
		else if(item.getItem() instanceof ItemBalloon)
		{
			GL11.glPushMatrix();
			
			if(type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON)
			{
				GL11.glScalef(2.5F, 2.5F, 2.5F);
				GL11.glTranslatef(0.2F, 0, 0.1F);
				GL11.glRotatef(15, -1, 0, 1);
				balloonRenderer.render(((ItemBalloon)item.getItem()).getColor(item), 0, 1.9F, 0);
			}
			else {
				balloonRenderer.render(((ItemBalloon)item.getItem()).getColor(item), 0, 1, 0);
			}
			
			GL11.glPopMatrix();
		}
		else if(item.getItem() instanceof ItemAtomicDisassembler)
		{
			GL11.glPushMatrix();
			GL11.glScalef(1.4F, 1.4F, 1.4F);
			GL11.glRotatef(180, 0.0F, 0.0F, 1.0F);

			if(type == ItemRenderType.EQUIPPED)
			{
				GL11.glRotatef(-45, 0.0F, 1.0F, 0.0F);
				GL11.glRotatef(50, 1.0F, 0.0F, 0.0F);
				GL11.glScalef(2.0F, 2.0F, 2.0F);
				GL11.glTranslatef(0.0F, -0.4F, 0.4F);
			}
			else if(type == ItemRenderType.INVENTORY)
			{
				GL11.glRotatef(225, 0.0F, 1.0F, 0.0F);
				GL11.glRotatef(45, -1.0F, 0.0F, -1.0F);
				GL11.glScalef(0.6F, 0.6F, 0.6F);
				GL11.glTranslatef(0.0F, -0.2F, 0.0F);
			}
			else {
				GL11.glRotatef(45, 0.0F, 1.0F, 0.0F);
				GL11.glTranslatef(0.0F, -0.7F, 0.0F);
			}

			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "AtomicDisassembler.png"));
			atomicDisassembler.render(0.0625F);
			GL11.glPopMatrix();
		}
		else if(item.getItem() instanceof ItemPartTransmitter)
		{
			GL11.glPushMatrix();
			GL11.glTranslatef(-0.5f, -0.5f, -0.5f);
			MekanismRenderer.blendOn();
			GL11.glDisable(GL11.GL_CULL_FACE);
			RenderPartTransmitter.getInstance().renderItem(TransmitterType.values()[item.getItemDamage()]);
			GL11.glEnable(GL11.GL_CULL_FACE);
			MekanismRenderer.blendOff();
			GL11.glPopMatrix();
		}
		else if(item.getItem() instanceof ItemGlowPanel)
		{
			GL11.glPushMatrix();
			GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
			GL11.glTranslatef(-0.5f, -0.5f, -0.5f);
			float d = 0.15f;
			GL11.glTranslatef(d, d, d);
			GL11.glScalef(2, 2, 2);
			GL11.glTranslatef(0.4f-2f*d, -2f*d, -2f*d);
			GL11.glDisable(GL11.GL_CULL_FACE);
			RenderHelper.disableStandardItemLighting();
			RenderGlowPanel.getInstance().renderItem(item.getItemDamage());
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glPopAttrib();
			GL11.glPopMatrix();
		}
		else if(item.getItem() instanceof ItemFlamethrower)
		{
			GL11.glPushMatrix();
			GL11.glRotatef(160, 0.0F, 0.0F, 1.0F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "Flamethrower.png"));
			
			GL11.glTranslatef(0.0F, -1.0F, 0.0F);
			GL11.glRotatef(135, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(-20, 0.0F, 0.0F, 1.0F);
			
			if(type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON)
			{
				if(type == ItemRenderType.EQUIPPED_FIRST_PERSON)
				{
					GL11.glRotatef(55, 0.0F, 1.0F, 0.0F);
				}
				else {
					GL11.glTranslatef(0.0F, 0.5F, 0.0F);
				}
				
				GL11.glScalef(2.5F, 2.5F, 2.5F);
				GL11.glTranslatef(0.0F, -1.0F, -0.5F);
			}
			else if(type == ItemRenderType.INVENTORY)
			{
				GL11.glTranslatef(-0.6F, 0.0F, 0.0F);
				GL11.glRotatef(45, 0.0F, 1.0F, 0.0F);
			}
			
			flamethrower.render(0.0625F);
			GL11.glPopMatrix();
		}
		else if(MachineType.get(item) == MachineType.FLUID_TANK)
		{
			GL11.glPushMatrix();
			GL11.glRotatef(270F, 0.0F, -1.0F, 0.0F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "FluidTank.png"));
			ItemBlockMachine itemMachine = (ItemBlockMachine)item.getItem();
			float targetScale = (float)(itemMachine.getFluidStack(item) != null ? itemMachine.getFluidStack(item).amount : 0)/itemMachine.getCapacity(item);
			FluidTankTier tier = FluidTankTier.values()[itemMachine.getBaseTier(item).ordinal()];
			Fluid fluid = itemMachine.getFluidStack(item) != null ? itemMachine.getFluidStack(item).getFluid() : null;
			portableTankRenderer.render(tier, fluid, targetScale, false, null, -0.5, -0.5, -0.5);
			GL11.glPopMatrix();
		}
		else {
			if(item.getItem() instanceof ItemBlockMachine)
			{
				MachineType machine = MachineType.get(item);
				
				if(machine == MachineType.BASIC_FACTORY || machine == MachineType.ADVANCED_FACTORY || machine == MachineType.ELITE_FACTORY)
				{
					GL11.glRotatef(-90F, 0.0F, 1.0F, 0.0F);
					MekanismRenderer.renderCustomItem(((RenderBlocks)data[0]), item);
				}
				else {
					RenderingRegistry.instance().renderInventoryBlock((RenderBlocks)data[0], Block.getBlockFromItem(item.getItem()), item.getItemDamage(), ClientProxy.MACHINE_RENDER_ID);
				}
			}
			else if(item.getItem() instanceof ItemBlockBasic)
			{
				RenderingRegistry.instance().renderInventoryBlock((RenderBlocks)data[0], Block.getBlockFromItem(item.getItem()), item.getItemDamage(), ClientProxy.BASIC_RENDER_ID);
			}
		}
	}
}
