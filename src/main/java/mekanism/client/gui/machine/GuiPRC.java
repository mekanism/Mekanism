package mekanism.client.gui.machine;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.machine.TileEntityPressurizedReactionChamber;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiPRC extends GuiConfigurableTile<TileEntityPressurizedReactionChamber, MekanismTileContainer<TileEntityPressurizedReactionChamber>> {

    public GuiPRC(MekanismTileContainer<TileEntityPressurizedReactionChamber> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addButton(new GuiEnergyTab(this, tile.getEnergyContainer(), tile::getActive));
        addButton(new GuiFluidGauge(() -> tile.inputFluidTank, () -> tile.getFluidTanks(null), GaugeType.STANDARD, this, 5, 10));
        addButton(new GuiGasGauge(() -> tile.inputGasTank, () -> tile.getGasTanks(null), GaugeType.STANDARD, this, 28, 10));
        addButton(new GuiGasGauge(() -> tile.outputGasTank, () -> tile.getGasTanks(null), GaugeType.SMALL, this, 140, 40));
        addButton(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 163, 16));
        addButton(new GuiProgress(tile::getScaledProgress, ProgressType.RIGHT, this, 77, 38).jeiCategory(tile));
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        float widthThird = imageWidth / 3F;
        drawTextScaledBound(matrix, tile.getName(), widthThird - 7, titleLabelY, titleTextColor(), 2 * widthThird);
        drawString(matrix, inventory.getDisplayName(), inventoryLabelX, inventoryLabelY, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}