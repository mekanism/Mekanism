package mekanism.client.gui.machine;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.bar.GuiChemicalBar;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.tab.GuiRedstoneControlTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiAdvancedElectricMachine<TILE extends TileEntityAdvancedElectricMachine, CONTAINER extends MekanismTileContainer<TILE>> extends
      GuiConfigurableTile<TILE, CONTAINER> {

    public GuiAdvancedElectricMachine(CONTAINER container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void func_231160_c_() {
        super.func_231160_c_();
        func_230480_a_(new GuiRedstoneControlTab(this, tile));
        func_230480_a_(new GuiUpgradeTab(this, tile));
        func_230480_a_(new GuiSecurityTab<>(this, tile));
        func_230480_a_(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 164, 15));
        func_230480_a_(new GuiEnergyTab(tile.getEnergyContainer(), this));
        func_230480_a_(new GuiProgress(tile::getScaledProgress, ProgressType.BAR, this, 86, 38).jeiCategory(tile));
        func_230480_a_(new GuiChemicalBar<>(this, GuiChemicalBar.getProvider(tile.gasTank, tile.getGasTanks(null)), 68, 36, 6, 12, false));
    }

    @Override
    protected void func_230451_b_(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawString(matrix, MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 2, titleTextColor());
        super.func_230451_b_(matrix, mouseX, mouseY);
    }
}