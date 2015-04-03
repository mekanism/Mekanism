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
import mekanism.client.model.ModelPortableTank;
import mekanism.client.model.ModelRobit;
import mekanism.client.model.ModelScubaTank;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.RenderGlowPanel;
import mekanism.client.render.RenderPartTransmitter;
import mekanism.client.render.entity.RenderBalloon;
import mekanism.client.render.tileentity.RenderBin;
import mekanism.client.render.tileentity.RenderPortableTank;
import mekanism.common.MekanismBlocks;
import mekanism.common.MekanismItems;
import mekanism.common.Tier.EnergyCubeTier;
import mekanism.common.base.IEnergyCube;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.inventory.InventoryBin;
import mekanism.common.item.ItemAtomicDisassembler;
import mekanism.common.item.ItemBalloon;
import mekanism.common.item.ItemBlockBasic;
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
import mekanism.common.tile.TileEntityPortableTank;
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
	public ModelRobit robit = new ModelRobit();
	public ModelChest electricChest = new ModelChest();
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
	public ModelPortableTank portableTank = new ModelPortableTank();
	public ModelFlamethrower flamethrower = new ModelFlamethrower();

	private final RenderBalloon balloonRenderer = new RenderBalloon();
	private final RenderBin binRenderer = (RenderBin)TileEntityRendererDispatcher.instance.mapSpecialRenderers.get(TileEntityBin.class);
	private final RenderPortableTank portableTankRenderer = (RenderPortableTank)TileEntityRendererDispatcher.instance.mapSpecialRenderers.get(TileEntityPortableTank.class);
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
			Minecraft.getMinecraft().renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "EnergyCube" + tier.getBaseTier().getName() + ".png"));

			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(270F, 0.0F, -1.0F, 0.0F);
			GL11.glTranslatef(0.0F, -1.0F, 0.0F);

			energyCube.render(0.0625F);

			GL11.glPushMatrix();
			GL11.glTranslated(0.0, 1.0, 0.0);
			Minecraft.getMinecraft().renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "EnergyCore.png"));

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
			energyCore.render(0.0625F);
			GL11.glPopMatrix();

			MekanismRenderer.glowOff();

			GL11.glShadeModel(GL11.GL_FLAT);
			GL11.glDisable(GL11.GL_LINE_SMOOTH);
			GL11.glDisable(GL11.GL_POLYGON_SMOOTH);
			GL11.glDisable(GL11.GL_BLEND);

			GL11.glPopMatrix();
		}
		else if(Block.getBlockFromItem(item.getItem()) == MekanismBlocks.BasicBlock2 && item.getItemDamage() == 3)
		{
			MekanismRenderer.renderCustomItem((RenderBlocks)data[0], item);
		}
		else if(Block.getBlockFromItem(item.getItem()) == MekanismBlocks.BasicBlock2 && item.getItemDamage() == 4)
		{
			MekanismRenderer.renderCustomItem((RenderBlocks)data[0], item);
		}
		else if(Block.getBlockFromItem(item.getItem()) == MekanismBlocks.BasicBlock && item.getItemDamage() == 6)
		{
			RenderingRegistry.instance().renderInventoryBlock((RenderBlocks)data[0], MekanismBlocks.BasicBlock, item.getItemDamage(), ClientProxy.BASIC_RENDER_ID);

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

				GL11.glTranslated(0.73, 0.08, 0.44);
				GL11.glRotatef(90, 0, 1, 0);

				float scale = 0.03125F;
				float scaler = 0.9F;

				GL11.glScalef(scale*scaler, scale*scaler, 0);

				TextureManager renderEngine = Minecraft.getMinecraft().renderEngine;

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
					GL11.glTranslated(-0.5, -0.4, -0.5);
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
			Minecraft.getMinecraft().renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "GasTank.png"));
			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(270F, 0.0F, -1.0F, 0.0F);
			GL11.glTranslatef(0.0F, -1.0F, 0.0F);
			gasTank.render(0.0625F);
			GL11.glPopMatrix();
		}
		else if(Block.getBlockFromItem(item.getItem()) == MekanismBlocks.ObsidianTNT)
		{
			GL11.glPushMatrix();
			Minecraft.getMinecraft().renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ObsidianTNT.png"));
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
		else if(MachineType.get(item) == MachineType.ELECTRIC_CHEST)
		{
			GL11.glPushMatrix();
			ItemBlockMachine chest = (ItemBlockMachine)item.getItem();

			GL11.glRotatef(90F, 0.0F, 1.0F, 0.0F);
			GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
			GL11.glTranslatef(0, 1.0F, 1.0F);
			GL11.glScalef(1.0F, -1F, -1F);

			Minecraft.getMinecraft().renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ElectricChest.png"));

			electricChest.renderAll();
			GL11.glPopMatrix();
		}
		else if(item.getItem() instanceof ItemRobit)
		{
			GL11.glPushMatrix();
			GL11.glRotatef(180, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(90, 0.0F, -1.0F, 0.0F);
			GL11.glTranslatef(0.0F, -1.5F, 0.0F);
			Minecraft.getMinecraft().renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "Robit.png"));
			robit.render(0.08F);
			GL11.glPopMatrix();
		}
		else if(item.getItem() == MekanismItems.Jetpack)
		{
			GL11.glPushMatrix();
			GL11.glRotatef(180, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(90, 0.0F, -1.0F, 0.0F);
			GL11.glTranslatef(0.2F, -0.35F, 0.0F);
			Minecraft.getMinecraft().renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "Jetpack.png"));
			jetpack.render(0.0625F);
			GL11.glPopMatrix();
		}
		else if(item.getItem() == MekanismItems.ArmoredJetpack)
		{
			GL11.glPushMatrix();
			GL11.glRotatef(180, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(90, 0.0F, -1.0F, 0.0F);
			GL11.glTranslatef(0.2F, -0.35F, 0.0F);
			Minecraft.getMinecraft().renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "Jetpack.png"));
			armoredJetpack.render(0.0625F);
			GL11.glPopMatrix();
		}
		else if(item.getItem() instanceof ItemGasMask)
		{
			GL11.glPushMatrix();
			GL11.glRotatef(180, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(90, 0.0F, -1.0F, 0.0F);
			GL11.glTranslatef(0.1F, 0.2F, 0.0F);
			Minecraft.getMinecraft().renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ScubaSet.png"));
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
			Minecraft.getMinecraft().renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ScubaSet.png"));
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
			Minecraft.getMinecraft().renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "FreeRunners.png"));
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

			Minecraft.getMinecraft().renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "AtomicDisassembler.png"));
			atomicDisassembler.render(0.0625F);
			GL11.glPopMatrix();
		}
		else if(item.getItem() instanceof ItemPartTransmitter)
		{
			GL11.glPushMatrix();
			GL11.glTranslated(-0.5, -0.5, -0.5);
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
			GL11.glPopMatrix();
		}
		else if(item.getItem() instanceof ItemFlamethrower)
		{
			GL11.glPushMatrix();
			GL11.glRotatef(160, 0.0F, 0.0F, 1.0F);
			Minecraft.getMinecraft().renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "Flamethrower.png"));
			
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
		else if(MachineType.get(item) == MachineType.PORTABLE_TANK)
		{
			GL11.glPushMatrix();
			GL11.glRotatef(270F, 0.0F, -1.0F, 0.0F);
			Minecraft.getMinecraft().renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "PortableTank.png"));
			
			ItemBlockMachine itemMachine = (ItemBlockMachine)item.getItem();
			Fluid fluid = itemMachine.getFluidStack(item) != null ? itemMachine.getFluidStack(item).getFluid() : null;
			portableTankRenderer.render(fluid, itemMachine.getPrevScale(item), false, null, -0.5, -0.5, -0.5);
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
