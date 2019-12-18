package mekanism.client.gui;

import java.util.Arrays;
import javax.annotation.Nonnull;
import mekanism.api.gas.GasStack;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiSideConfigurationTab;
import mekanism.client.gui.element.tab.GuiTransporterConfigTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiAdvancedElectricMachine<TILE extends TileEntityAdvancedElectricMachine, CONTAINER extends MekanismTileContainer<TILE>> extends
      GuiMekanismTile<TILE, CONTAINER> {

    protected GuiAdvancedElectricMachine(CONTAINER container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        ResourceLocation resource = getGuiLocation();
        addButton(new GuiRedstoneControl(this, tile, resource));
        addButton(new GuiUpgradeTab(this, tile, resource));
        addButton(new GuiSecurityTab<>(this, tile, resource));
        addButton(new GuiSideConfigurationTab(this, tile, resource));
        addButton(new GuiTransporterConfigTab(this, tile, resource));
        addButton(new GuiVerticalPowerBar(this, tile, resource, 164, 15));
        addButton(new GuiEnergyInfo(() -> Arrays.asList(
              TextComponentUtil.build(Translation.of("gui.mekanism.using"), ": ", EnergyDisplay.of(tile.getEnergyPerTick()), "/t"),
              TextComponentUtil.build(Translation.of("gui.mekanism.needed"), ": ", EnergyDisplay.of(tile.getNeededEnergy()))
        ), this, resource));
        addButton(new GuiSlot(SlotType.INPUT, this, resource, 55, 16));
        addButton(new GuiSlot(SlotType.POWER, this, resource, 30, 34).with(SlotOverlay.POWER));
        addButton(new GuiSlot(SlotType.EXTRA, this, resource, 55, 52));
        addButton(new GuiSlot(SlotType.OUTPUT_LARGE, this, resource, 111, 30));
        addButton(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return tile.getScaledProgress();
            }
        }, getProgressType(), this, resource, 77, 37));
    }

    public ProgressBar getProgressType() {
        return ProgressBar.BLUE;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), (xSize / 2) - (getStringWidth(tile.getName()) / 2), 6, 0x404040);
        drawString(TextComponentUtil.translate("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
        //TODO: 1.14 Convert to GuiElement
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        if (xAxis >= 61 && xAxis <= 67 && yAxis >= 37 && yAxis <= 49) {
            GasStack gasStack = tile.gasTank.getStack();
            if (gasStack.isEmpty()) {
                displayTooltip(TextComponentUtil.translate("gui.mekanism.none"), xAxis, yAxis);
            } else {
                displayTooltip(TextComponentUtil.build(gasStack, ": " + tile.gasTank.getStored()), xAxis, yAxis);
            }
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        if (tile.getScaledGasLevel(12) > 0) {
            int displayInt = tile.getScaledGasLevel(12);
            displayGauge(61, 37 + 12 - displayInt, 6, displayInt, tile.gasTank.getStack());
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return tile.guiLocation;
    }

    public void displayGauge(int xPos, int yPos, int sizeX, int sizeY, @Nonnull GasStack gas) {
        if (!gas.isEmpty()) {
            minecraft.textureManager.bindTexture(PlayerContainer.field_226615_c_);
            MekanismRenderer.color(gas);
            drawTexturedRectFromIcon(guiLeft + xPos, guiTop + yPos, MekanismRenderer.getChemicalTexture(gas.getType()), sizeX, sizeY);
            MekanismRenderer.resetColor();
        }
    }
}