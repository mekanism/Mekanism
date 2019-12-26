package mekanism.client.gui;

import java.util.Arrays;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiSideConfigurationTab;
import mekanism.client.gui.element.tab.GuiTransporterConfigTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.PressurizedReactionChamberContainer;
import mekanism.common.tile.TileEntityPressurizedReactionChamber;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiPRC extends GuiMekanismTile<TileEntityPressurizedReactionChamber, PressurizedReactionChamberContainer> {

    public GuiPRC(PressurizedReactionChamberContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        ResourceLocation resource = getGuiLocation();
        addButton(new GuiRedstoneControl(this, tile, resource));
        addButton(new GuiSecurityTab<>(this, tile, resource));
        addButton(new GuiSideConfigurationTab(this, tile, resource));
        addButton(new GuiTransporterConfigTab(this, tile, resource));
        addButton(new GuiUpgradeTab(this, tile, resource));
        addButton(new GuiEnergyInfo(() -> {
            //TODO: Use a getter for the cached recipe
            CachedRecipe<PressurizedReactionRecipe> recipe = tile.getUpdatedCache(0);
            double extra = recipe == null ? 0 : recipe.getRecipe().getEnergyRequired();
            double energyPerTick = MekanismUtils.getEnergyPerTick(tile, tile.getBaseStorage() + extra);
            return Arrays.asList(MekanismLang.USING.translate(EnergyDisplay.of(energyPerTick)),
                  MekanismLang.NEEDED.translate(EnergyDisplay.of(tile.getNeededEnergy())));
        }, this, resource));
        addButton(new GuiFluidGauge(() -> tile.inputFluidTank, GuiGauge.Type.STANDARD_YELLOW, this, resource, 5, 10));
        addButton(new GuiGasGauge(() -> tile.inputGasTank, GuiGauge.Type.STANDARD_RED, this, resource, 28, 10));
        addButton(new GuiGasGauge(() -> tile.outputGasTank, GuiGauge.Type.SMALL_BLUE, this, resource, 140, 40));
        addButton(new GuiVerticalPowerBar(this, tile, resource, 164, 15));
        addButton(new GuiSlot(SlotType.INPUT, this, resource, 53, 34));
        addButton(new GuiSlot(SlotType.POWER, this, resource, 140, 18).with(SlotOverlay.POWER));
        addButton(new GuiSlot(SlotType.OUTPUT, this, resource, 115, 34));
        addButton(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return tile.getScaledProgress();
            }
        }, getProgressType(), this, resource, 75, 37));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), (xSize / 2) - (getStringWidth(tile.getName()) / 2), 6, 0x404040);
        drawString(MekanismLang.INVENTORY.translate(), 8, (ySize - 96) + 2, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "blank.png");
    }

    public ProgressBar getProgressType() {
        return ProgressBar.MEDIUM;
    }
}