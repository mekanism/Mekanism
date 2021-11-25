package mekanism.client.gui.machine;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.GuiDumpButton;
import mekanism.client.gui.element.bar.GuiChemicalBar;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.machine.TileEntityMetallurgicInfuser;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiMetallurgicInfuser extends GuiConfigurableTile<TileEntityMetallurgicInfuser, MekanismTileContainer<TileEntityMetallurgicInfuser>> {

    public GuiMetallurgicInfuser(MekanismTileContainer<TileEntityMetallurgicInfuser> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addButton(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 164, 15));
        addButton(new GuiEnergyTab(this, tile.getEnergyContainer(), tile::getActive));
        addButton(new GuiProgress(tile::getScaledProgress, ProgressType.RIGHT, this, 72, 47).jeiCategory(tile));
        addButton(new GuiChemicalBar<>(this, GuiChemicalBar.getProvider(tile.infusionTank, tile.getInfusionTanks(null)), 7, 15, 4, 52, false));
        addButton(new GuiDumpButton<>(this, tile, 16, 59));
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawString(matrix, inventory.getDisplayName(), inventoryLabelX, inventoryLabelY, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}