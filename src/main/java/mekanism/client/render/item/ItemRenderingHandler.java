package mekanism.client.render.item;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ItemRenderingHandler// implements IItemRenderer
{
	/*
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
			GlStateManager.translate(0.5F, 0.5F, 0.5F);
		}

		if(item.getItem() instanceof IEnergyCube)
		{
			EnergyCubeTier tier = ((IEnergyCube)item.getItem()).getEnergyCubeTier(item);
			IEnergizedItem energized = (IEnergizedItem)item.getItem();
			mc.renderEngine.bindTexture(RenderEnergyCube.baseTexture);

			GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
			GlStateManager.rotate(270F, 0.0F, -1.0F, 0.0F);
			GlStateManager.translate(0.0F, -1.0F, 0.0F);

			MekanismRenderer.blendOn();
			
			energyCube.render(0.0625F, tier, mc.renderEngine);
			
			for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				mc.renderEngine.bindTexture(RenderEnergyCube.baseTexture);
				energyCube.renderSide(0.0625F, side, side == ForgeDirection.NORTH ? IOState.OUTPUT : IOState.INPUT, tier, mc.renderEngine);
			}
			
			MekanismRenderer.blendOff();

			GlStateManager.pushMatrix();
			GL11.glTranslated(0.0, 1.0, 0.0);
			mc.renderEngine.bindTexture(RenderEnergyCube.coreTexture);

			GL11.glShadeModel(GL11.GL_SMOOTH);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			MekanismRenderer.glowOn();

			EnumColor c = tier.getBaseTier().getColor();

			GlStateManager.pushMatrix();
			GlStateManager.scale(0.4F, 0.4F, 0.4F);
			GL11.glColor4f(c.getColor(0), c.getColor(1), c.getColor(2), (float)(energized.getEnergy(item)/energized.getMaxEnergy(item)));
			GlStateManager.translate(0, (float)Math.sin(Math.toRadians((MekanismClient.ticksPassed + MekanismRenderer.getPartialTick()) * 3)) / 7, 0);
			GlStateManager.rotate((MekanismClient.ticksPassed + MekanismRenderer.getPartialTick()) * 4, 0, 1, 0);
			GlStateManager.rotate(36F + (MekanismClient.ticksPassed + MekanismRenderer.getPartialTick()) * 4, 0, 1, 1);
			energyCore.render(0.0625F);
			GlStateManager.popMatrix();

			MekanismRenderer.glowOff();

			GL11.glShadeModel(GL11.GL_FLAT);
			GL11.glDisable(GL11.GL_LINE_SMOOTH);
			GL11.glDisable(GL11.GL_POLYGON_SMOOTH);
			GL11.glDisable(GL11.GL_BLEND);

			GlStateManager.popMatrix();
		}
		else if(BasicType.get(item) == BasicType.INDUCTION_CELL || BasicType.get(item) == BasicType.INDUCTION_PROVIDER)
		{
			MekanismRenderer.renderCustomItem((RenderBlocks)data[0], item);
		}
		else if(BasicType.get(item) == BasicType.BIN)
		{
			GlStateManager.rotate(270, 0.0F, 1.0F, 0.0F);
			MekanismRenderer.renderCustomItem((RenderBlocks)data[0], item);
			GlStateManager.rotate(-270, 0.0F, 1.0F, 0.0F);
			
			if(binRenderer == null || binRenderer.func_147498_b()/*getFontRenderer()* / == null)
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
				GlStateManager.pushMatrix();

				if(!(itemStack.getItem() instanceof ItemBlock) || Block.getBlockFromItem(itemStack.getItem()).getRenderType() != 0)
				{
					GlStateManager.rotate(180, 0, 0, 1);
					GlStateManager.translate(-1.02F, -0.2F, 0);

					if(type == ItemRenderType.INVENTORY)
					{
						GlStateManager.translate(-0.45F, -0.4F, 0.0F);
					}
				}

				if(type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON || type == ItemRenderType.ENTITY)
				{
					GlStateManager.translate(-0.22F, -0.2F, -0.22F);
				}

				GL11.glTranslated(0.73, 0.08, 0.44);
				GlStateManager.rotate(90, 0, 1, 0);

				float scale = 0.03125F;
				float scaler = 0.9F;

				GlStateManager.scale(scale*scaler, scale*scaler, 0);

				TextureManager renderEngine = mc.renderEngine;

				GL11.glDisable(GL11.GL_LIGHTING);

				renderItem.renderItemAndEffectIntoGUI(binRenderer.func_147498_b()/*getFontRenderer()* /, renderEngine, itemStack, 0, 0);

				GL11.glEnable(GL11.GL_LIGHTING);
				GlStateManager.popMatrix();
			}
			
			MekanismRenderer.glowOff();

			if(amount != "")
			{
				float maxScale = 0.02F;

				GlStateManager.pushMatrix();

				GL11.glPolygonOffset(-10, -10);
				GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);

				float displayWidth = 1 - (2 / 16);
				float displayHeight = 1 - (2 / 16);
				GlStateManager.translate(0, -0.31F, 0);

				if(type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON || type == ItemRenderType.ENTITY)
				{
					GL11.glTranslated(-0.5, -0.4, -0.5);
				}

				GlStateManager.translate(0, 0.9F, 1);
				GlStateManager.rotate(90, 0, 1, 0);
				GlStateManager.rotate(90, 1, 0, 0);

				GlStateManager.translate(displayWidth / 2, 1F, displayHeight / 2);
				GlStateManager.rotate(-90, 1, 0, 0);

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

				GlStateManager.scale(scale, -scale, scale);
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

				GlStateManager.popMatrix();
			}
		}
		else if(Block.getBlockFromItem(item.getItem()) == MekanismBlocks.GasTank)
		{
			GlStateManager.pushMatrix();
			
			BaseTier tier = ((ItemBlockGasTank)item.getItem()).getBaseTier(item);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "GasTank" + tier.getName() + ".png"));
			GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
			GlStateManager.rotate(90F, 0.0F, 1.0F, 0.0F);
			GlStateManager.translate(0.0F, -1.0F, 0.0F);
			gasTank.render(0.0625F);
			
			GlStateManager.popMatrix();
		}
		else if(Block.getBlockFromItem(item.getItem()) == MekanismBlocks.ObsidianTNT)
		{
			GlStateManager.pushMatrix();
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ObsidianTNT.png"));
			GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
			GlStateManager.rotate(180F, 0.0F, -1.0F, 0.0F);
			GlStateManager.translate(0.0F, -1.0F, 0.0F);
			obsidianTNT.render(0.0625F);
			GlStateManager.popMatrix();
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
			GlStateManager.pushMatrix();
			ItemBlockMachine chest = (ItemBlockMachine)item.getItem();

			GlStateManager.rotate(90F, 0.0F, 1.0F, 0.0F);
			GlStateManager.translate(-0.5F, -0.5F, -0.5F);
			GlStateManager.translate(0, 1.0F, 1.0F);
			GlStateManager.scale(1.0F, -1F, -1F);

			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "PersonalChest.png"));

			personalChest.renderAll();
			GlStateManager.popMatrix();
		}
		else if(item.getItem() instanceof ItemRobit)
		{
			GlStateManager.pushMatrix();
			GlStateManager.rotate(180, 0.0F, 0.0F, 1.0F);
			GlStateManager.rotate(90, 0.0F, -1.0F, 0.0F);
			GlStateManager.translate(0.0F, -1.5F, 0.0F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "Robit.png"));
			robit.render(0.08F);
			GlStateManager.popMatrix();
		}
		else if(item.getItem() == MekanismItems.Jetpack)
		{
			GlStateManager.pushMatrix();
			GlStateManager.rotate(180, 0.0F, 0.0F, 1.0F);
			GlStateManager.rotate(90, 0.0F, -1.0F, 0.0F);
			GlStateManager.translate(0.2F, -0.35F, 0.0F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "Jetpack.png"));
			jetpack.render(0.0625F);
			GlStateManager.popMatrix();
		}
		else if(item.getItem() == MekanismItems.ArmoredJetpack)
		{
			GlStateManager.pushMatrix();
			GlStateManager.rotate(180, 0.0F, 0.0F, 1.0F);
			GlStateManager.rotate(90, 0.0F, -1.0F, 0.0F);
			GlStateManager.translate(0.2F, -0.35F, 0.0F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "Jetpack.png"));
			armoredJetpack.render(0.0625F);
			GlStateManager.popMatrix();
		}
		else if(item.getItem() instanceof ItemGasMask)
		{
			GlStateManager.pushMatrix();
			GlStateManager.rotate(180, 0.0F, 0.0F, 1.0F);
			GlStateManager.rotate(90, 0.0F, -1.0F, 0.0F);
			GlStateManager.translate(0.1F, 0.2F, 0.0F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ScubaSet.png"));
			gasMask.render(0.0625F);
			GlStateManager.popMatrix();
		}
		else if(item.getItem() instanceof ItemScubaTank)
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
		else if(item.getItem() instanceof ItemFreeRunners)
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
		else if(item.getItem() instanceof ItemBalloon)
		{
			GlStateManager.pushMatrix();
			
			if(type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON)
			{
				GlStateManager.scale(2.5F, 2.5F, 2.5F);
				GlStateManager.translate(0.2F, 0, 0.1F);
				GlStateManager.rotate(15, -1, 0, 1);
				balloonRenderer.render(((ItemBalloon)item.getItem()).getColor(item), 0, 1.9F, 0);
			}
			else {
				balloonRenderer.render(((ItemBalloon)item.getItem()).getColor(item), 0, 1, 0);
			}
			
			GlStateManager.popMatrix();
		}
		else if(item.getItem() instanceof ItemAtomicDisassembler)
		{
			GlStateManager.pushMatrix();
			GlStateManager.scale(1.4F, 1.4F, 1.4F);
			GlStateManager.rotate(180, 0.0F, 0.0F, 1.0F);

			if(type == ItemRenderType.EQUIPPED)
			{
				GlStateManager.rotate(-45, 0.0F, 1.0F, 0.0F);
				GlStateManager.rotate(50, 1.0F, 0.0F, 0.0F);
				GlStateManager.scale(2.0F, 2.0F, 2.0F);
				GlStateManager.translate(0.0F, -0.4F, 0.4F);
			}
			else if(type == ItemRenderType.INVENTORY)
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
		else if(item.getItem() instanceof ItemPartTransmitter)
		{
			GlStateManager.pushMatrix();
			GL11.glTranslated(-0.5, -0.5, -0.5);
			MekanismRenderer.blendOn();
			GL11.glDisable(GL11.GL_CULL_FACE);
			RenderPartTransmitter.getInstance().renderItem(TransmitterType.values()[item.getItemDamage()]);
			GL11.glEnable(GL11.GL_CULL_FACE);
			MekanismRenderer.blendOff();
			GlStateManager.popMatrix();
		}
		else if(item.getItem() instanceof ItemGlowPanel)
		{
			GlStateManager.pushMatrix();
			GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
			GL11.glTranslated(-0.5, -0.5, -0.5);
			double d = 0.15;
			GL11.glTranslated(d, d, d);
			GL11.glScaled(2, 2, 2);
			GL11.glTranslated(0.4-2*d, -2*d, -2*d);
			GL11.glDisable(GL11.GL_CULL_FACE);
			RenderHelper.disableStandardItemLighting();
			RenderGlowPanel.getInstance().renderItem(item.getItemDamage());
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glPopAttrib();
			GlStateManager.popMatrix();
		}
		else if(item.getItem() instanceof ItemFlamethrower)
		{
			GlStateManager.pushMatrix();
			GlStateManager.rotate(160, 0.0F, 0.0F, 1.0F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "Flamethrower.png"));
			
			GlStateManager.translate(0.0F, -1.0F, 0.0F);
			GlStateManager.rotate(135, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(-20, 0.0F, 0.0F, 1.0F);
			
			if(type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON)
			{
				if(type == ItemRenderType.EQUIPPED_FIRST_PERSON)
				{
					GlStateManager.rotate(55, 0.0F, 1.0F, 0.0F);
				}
				else {
					GlStateManager.translate(0.0F, 0.5F, 0.0F);
				}
				
				GlStateManager.scale(2.5F, 2.5F, 2.5F);
				GlStateManager.translate(0.0F, -1.0F, -0.5F);
			}
			else if(type == ItemRenderType.INVENTORY)
			{
				GlStateManager.translate(-0.6F, 0.0F, 0.0F);
				GlStateManager.rotate(45, 0.0F, 1.0F, 0.0F);
			}
			
			flamethrower.render(0.0625F);
			GlStateManager.popMatrix();
		}
		else if(MachineType.get(item) == MachineType.FLUID_TANK)
		{
			GlStateManager.pushMatrix();
			GlStateManager.rotate(270F, 0.0F, -1.0F, 0.0F);
			mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "FluidTank.png"));
			ItemBlockMachine itemMachine = (ItemBlockMachine)item.getItem();
			float targetScale = (float)(itemMachine.getFluidStack(item) != null ? itemMachine.getFluidStack(item).amount : 0)/itemMachine.getCapacity(item);
			FluidTankTier tier = FluidTankTier.values()[itemMachine.getBaseTier(item).ordinal()];
			Fluid fluid = itemMachine.getFluidStack(item) != null ? itemMachine.getFluidStack(item).getFluid() : null;
			portableTankRenderer.render(tier, fluid, targetScale, false, null, -0.5, -0.5, -0.5);
			GlStateManager.popMatrix();
		}
		else {
			if(item.getItem() instanceof ItemBlockMachine)
			{
				MachineType machine = MachineType.get(item);
				
				if(machine == MachineType.BASIC_FACTORY || machine == MachineType.ADVANCED_FACTORY || machine == MachineType.ELITE_FACTORY)
				{
					GlStateManager.rotate(-90F, 0.0F, 1.0F, 0.0F);
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
				*/
}
