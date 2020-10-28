package mekanism.client.gui.machine;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.GuiUpArrow;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.tab.GuiRedstoneControlTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.prefab.TileEntityElectricMachine;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiElectricMachine<TILE extends TileEntityElectricMachine, CONTAINER extends MekanismTileContainer<TILE>> extends GuiConfigurableTile<TILE, CONTAINER> {

    public GuiElectricMachine(CONTAINER container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiUpArrow(this, 68, 38));
        addButton(new GuiRedstoneControlTab(this, tile));
        addButton(new GuiUpgradeTab(this, tile));
        addButton(new GuiSecurityTab(this, tile));
        addButton(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 164, 15));
        addButton(new GuiEnergyTab(tile.getEnergyContainer(), tile::getActive, this));
        addButton(new GuiProgress(tile::getScaledProgress, ProgressType.BAR, this, 86, 38).jeiCategory(tile));
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawString(matrix, playerInventory.getDisplayName(), playerInventoryTitleX, playerInventoryTitleY, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}