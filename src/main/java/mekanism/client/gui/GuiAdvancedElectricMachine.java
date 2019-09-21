package mekanism.client.gui;

import java.util.Arrays;
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
import mekanism.common.inventory.container.tile.advanced.AdvancedElectricMachineContainer;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class GuiAdvancedElectricMachine<TILE extends TileEntityAdvancedElectricMachine, CONTAINER extends AdvancedElectricMachineContainer<TILE>> extends
      GuiMekanismTile<TILE, CONTAINER> {

    protected GuiAdvancedElectricMachine(CONTAINER container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        ResourceLocation resource = getGuiLocation();
        addButton(new GuiRedstoneControl(this, tileEntity, resource));
        addButton(new GuiUpgradeTab(this, tileEntity, resource));
        addButton(new GuiSecurityTab<>(this, tileEntity, resource));
        addButton(new GuiSideConfigurationTab(this, tileEntity, resource));
        addButton(new GuiTransporterConfigTab(this, tileEntity, resource));
        addButton(new GuiVerticalPowerBar(this, tileEntity, resource, 164, 15));
        addButton(new GuiEnergyInfo(() -> Arrays.asList(
              TextComponentUtil.build(Translation.of("gui.mekanism.using"), ": ", EnergyDisplay.of(tileEntity.getEnergyPerTick()), "/t"),
              TextComponentUtil.build(Translation.of("gui.mekanism.needed"), ": ", EnergyDisplay.of(tileEntity.getNeededEnergy()))
        ), this, resource));
        addButton(new GuiSlot(SlotType.INPUT, this, resource, 55, 16));
        addButton(new GuiSlot(SlotType.POWER, this, resource, 30, 34).with(SlotOverlay.POWER));
        addButton(new GuiSlot(SlotType.EXTRA, this, resource, 55, 52));
        addButton(new GuiSlot(SlotType.OUTPUT_LARGE, this, resource, 111, 30));
        addButton(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return tileEntity.getScaledProgress();
            }
        }, getProgressType(), this, resource, 77, 37));
    }

    public ProgressBar getProgressType() {
        return ProgressBar.BLUE;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tileEntity.getName(), (xSize / 2) - (getStringWidth(tileEntity.getName()) / 2), 6, 0x404040);
        drawString(TextComponentUtil.translate("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
        //TODO: 1.14 Convert to GuiElement
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        if (xAxis >= 61 && xAxis <= 67 && yAxis >= 37 && yAxis <= 49) {
            GasStack gasStack = tileEntity.gasTank.getGas();
            if (gasStack != null) {
                displayTooltip(TextComponentUtil.build(gasStack, ": " + tileEntity.gasTank.getStored()), xAxis, yAxis);
            } else {
                displayTooltip(TextComponentUtil.translate("gui.mekanism.none"), xAxis, yAxis);
            }
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        if (tileEntity.getScaledGasLevel(12) > 0) {
            int displayInt = tileEntity.getScaledGasLevel(12);
            displayGauge(61, 37 + 12 - displayInt, 6, displayInt, tileEntity.gasTank.getGas());
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return tileEntity.guiLocation;
    }

    public void displayGauge(int xPos, int yPos, int sizeX, int sizeY, GasStack gas) {
        if (gas != null) {
            minecraft.textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
            MekanismRenderer.color(gas);
            drawTexturedRectFromIcon(guiLeft + xPos, guiTop + yPos, gas.getGas().getSprite(), sizeX, sizeY);
            MekanismRenderer.resetColor();
        }
    }
}