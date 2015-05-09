package mekanism.client.gui;

import java.util.List;

import mekanism.api.gas.GasStack;
import mekanism.api.util.ListUtils;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiEnergyInfo.IInfoHandler;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.GuiPowerBar;
import mekanism.client.gui.element.GuiRecipeType;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSideConfigurationTab;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSortingTab;
import mekanism.client.gui.element.GuiTransporterConfigTab;
import mekanism.client.gui.element.GuiUpgradeTab;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Tier.FactoryTier;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.inventory.container.ContainerFactory;
import mekanism.common.tile.TileEntityFactory;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiFactory extends GuiMekanism
{
	public TileEntityFactory tileEntity;

	public GuiFactory(InventoryPlayer inventory, TileEntityFactory tentity)
	{
		super(tentity, new ContainerFactory(inventory, tentity));
		tileEntity = tentity;

		ySize = 214;

		guiElements.add(new GuiRedstoneControl(this, tileEntity, tileEntity.tier.guiLocation, 176, 92));
		guiElements.add(new GuiUpgradeTab(this, tileEntity, tileEntity.tier.guiLocation, 176, 5));
		guiElements.add(new GuiRecipeType(this, tileEntity, tileEntity.tier.guiLocation, 176, 34));
		guiElements.add(new GuiSideConfigurationTab(this, tileEntity, tileEntity.tier.guiLocation, -26, 5));
		guiElements.add(new GuiTransporterConfigTab(this, tileEntity, tileEntity.tier.guiLocation, -26, 34));
		guiElements.add(new GuiSortingTab(this, tileEntity, tileEntity.tier.guiLocation, -26, 63));
		guiElements.add(new GuiPowerBar(this, tileEntity, tileEntity.tier.guiLocation, 7, 64));
		guiElements.add(new GuiEnergyInfo(new IInfoHandler() {
			@Override
			public List<String> getInfo()
			{
				String multiplier = MekanismUtils.getEnergyDisplay(tileEntity.energyPerTick);
				return ListUtils.asList(MekanismUtils.localize("gui.using") + ": " + multiplier + "/t", MekanismUtils.localize("gui.needed") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getMaxEnergy()-tileEntity.getEnergy()));
			}
		}, this, tileEntity.tier.guiLocation, -26, 92));
		guiElements.add(new GuiSlot(SlotType.POWER, this, tileEntity.tier.guiLocation, 13, 100).with(SlotOverlay.POWER));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = mouseX - guiLeft;
		int yAxis = mouseY - guiTop;

		fontRendererObj.drawString(tileEntity.getInventoryName(), (xSize/2)-(fontRendererObj.getStringWidth(tileEntity.getInventoryName())/2), 4, 0x404040);
		fontRendererObj.drawString(MekanismUtils.localize("container.inventory"), 8, (ySize - 93) + 2, 0x404040);

		if(xAxis >= 25 && xAxis <= 168 && yAxis >= 17 && yAxis <= 25)
		{
			if( tileEntity.recipeType == RecipeType.COMBINING || tileEntity.recipeType == RecipeType.COMPRESSING || tileEntity.recipeType == RecipeType.INJECTING || tileEntity.recipeType == RecipeType.PURIFYING )
				drawCreativeTabHoveringText(tileEntity.gasTank.getGas() != null ? tileEntity.gasTank.getGas().getGas().getLocalizedName() + ": " + tileEntity.gasTank.getStored() : MekanismUtils.localize("gui.none"), xAxis, yAxis);
		}

		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		mc.renderEngine.bindTexture(tileEntity.tier.guiLocation);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);

		int xAxis = mouseX - guiLeft;
		int yAxis = mouseY - guiTop;

		int displayInt;
		int progress = 0;
		
		switch( tileEntity.recipeType )
		{
			case COMBINING:
				progress = 110;
				break;
			case COMPRESSING:
				progress = 88;
				break;
			case CRUSHING:
				progress = 66;
				break;
			case ENRICHING:
				progress = 44;
				break;
			case INJECTING:
				progress = 154;
				break;
			case PURIFYING:
				progress = 132;
				break;
			case SMELTING:
				progress = 22;
				break;
		}

		if(tileEntity.tier == FactoryTier.BASIC)
		{
			for(int i = 0; i < tileEntity.tier.processes; i++)
			{
				int xPos = 58 + (i*38);

				displayInt = tileEntity.getScaledProgress(22, i);
				drawTexturedModalRect(guiLeft + xPos, guiTop + 55, 176, progress, 10, 22);
				drawTexturedModalRect(guiLeft + xPos, guiTop + 55, 186, progress, 10, displayInt);
			}
		}
		else if(tileEntity.tier == FactoryTier.ADVANCED)
		{
			for(int i = 0; i < tileEntity.tier.processes; i++)
			{
				int xPos = 38 + (i*26);

				displayInt = tileEntity.getScaledProgress(22, i);
				drawTexturedModalRect(guiLeft + xPos, guiTop + 55, 176, progress, 10, 22);
				drawTexturedModalRect(guiLeft + xPos, guiTop + 55, 186, progress, 10, displayInt);
			}
		}
		else if(tileEntity.tier == FactoryTier.ELITE)
		{
			for(int i = 0; i < tileEntity.tier.processes; i++)
			{
				int xPos = 32 + (i*19);

				displayInt = tileEntity.getScaledProgress(22, i);
				drawTexturedModalRect(guiLeft + xPos, guiTop + 55, 176, progress, 10, 22);
				drawTexturedModalRect(guiLeft + xPos, guiTop + 55, 186, progress, 10, displayInt);
			}
		}

		if( tileEntity.recipeType == RecipeType.COMBINING || tileEntity.recipeType == RecipeType.COMPRESSING || tileEntity.recipeType == RecipeType.INJECTING || tileEntity.recipeType == RecipeType.PURIFYING )
		{
			drawTexturedModalRect(guiLeft + 7, guiTop + 17, 7, 215, 162, 18);
			if(tileEntity.getScaledGasLevel(160) > 0)
			{
				displayGauge(27, 19, tileEntity.getScaledGasLevel(140), 5, tileEntity.gasTank.getGas());
			}
		}
	}

	public void displayGauge(int xPos, int yPos, int sizeX, int sizeY, GasStack gas)
	{
		if(gas == null)
		{
			return;
		}

		mc.renderEngine.bindTexture(MekanismRenderer.getBlocksTexture());
		drawTexturedModelRectFromIcon(guiLeft + xPos, guiTop + yPos, gas.getGas().getIcon(), sizeX, sizeY);
	}
}
