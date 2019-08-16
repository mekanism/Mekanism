package mekanism.client.gui;

import java.util.Arrays;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiPowerBar;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiSideConfigurationTab;
import mekanism.client.gui.element.tab.GuiTransporterConfigTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.common.inventory.container.tile.PressurizedReactionChamberContainer;
import mekanism.common.tile.TileEntityPRC;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiPRC extends GuiMekanismTile<TileEntityPRC, PressurizedReactionChamberContainer> {

    public GuiPRC(PressurizedReactionChamberContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        ResourceLocation resource = getGuiLocation();
        addGuiElement(new GuiRedstoneControl(this, tileEntity, resource));
        addGuiElement(new GuiSecurityTab<>(this, tileEntity, resource));
        addGuiElement(new GuiSideConfigurationTab(this, tileEntity, resource));
        addGuiElement(new GuiTransporterConfigTab(this, 34, tileEntity, resource));
        addGuiElement(new GuiUpgradeTab(this, tileEntity, resource));
        addGuiElement(new GuiEnergyInfo(() -> {
            double extra = tileEntity.getRecipe() != null ? tileEntity.getRecipe().extraEnergy : 0;
            double energyPerTick = MekanismUtils.getEnergyPerTick(tileEntity, tileEntity.getBaseStorage() + extra);
            return Arrays.asList(TextComponentUtil.build(Translation.of("mekanism.gui.using"), ": ", EnergyDisplay.of(energyPerTick), "/t"),
                  TextComponentUtil.build(Translation.of("mekanism.gui.needed"), ": ", EnergyDisplay.of(tileEntity.getNeededEnergy())));
        }, this, resource));
        addGuiElement(new GuiFluidGauge(() -> tileEntity.inputFluidTank, GuiGauge.Type.STANDARD_YELLOW, this, resource, 5, 10));
        addGuiElement(new GuiGasGauge(() -> tileEntity.inputGasTank, GuiGauge.Type.STANDARD_RED, this, resource, 28, 10));
        addGuiElement(new GuiGasGauge(() -> tileEntity.outputGasTank, GuiGauge.Type.SMALL_BLUE, this, resource, 140, 40));
        addGuiElement(new GuiPowerBar(this, tileEntity, resource, 164, 15));
        addGuiElement(new GuiSlot(SlotType.INPUT, this, resource, 53, 34));
        addGuiElement(new GuiSlot(SlotType.POWER, this, resource, 140, 18).with(SlotOverlay.POWER));
        addGuiElement(new GuiSlot(SlotType.OUTPUT, this, resource, 115, 34));
        addGuiElement(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return tileEntity.getScaledProgress();
            }
        }, getProgressType(), this, resource, 75, 37));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tileEntity.getName(), (xSize / 2) - (getStringWidth(tileEntity.getName()) / 2), 6, 0x404040);
        drawString(TextComponentUtil.translate("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png");
    }

    public ProgressBar getProgressType() {
        return ProgressBar.MEDIUM;
    }
}