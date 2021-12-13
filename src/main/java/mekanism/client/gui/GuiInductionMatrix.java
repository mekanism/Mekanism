package mekanism.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Arrays;
import javax.annotation.Nonnull;
import mekanism.api.math.FloatingLong;
import mekanism.client.SpecialColors;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.GuiSideHolder;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.gauge.GuiEnergyGauge.IEnergyInfoHandler;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.tab.GuiMatrixTab;
import mekanism.client.gui.element.tab.GuiMatrixTab.MatrixTab;
import mekanism.common.MekanismLang;
import mekanism.common.content.matrix.MatrixMultiblockData;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.multiblock.TileEntityInductionCasing;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiInductionMatrix extends GuiMekanismTile<TileEntityInductionCasing, MekanismTileContainer<TileEntityInductionCasing>> {

    public GuiInductionMatrix(MekanismTileContainer<TileEntityInductionCasing> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        inventoryLabelY += 2;
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        //Add the side holder before the slots, as it holds a couple of the slots
        addButton(GuiSideHolder.create(this, -26, 36, 98, true, true, SpecialColors.TAB_ARMOR_SLOTS));
        addButton(new GuiElementHolder(this, 141, 16, 26, 56));
        super.addGuiElements();
        addButton(new GuiSlot(SlotType.INNER_HOLDER_SLOT, this, 145, 20));
        addButton(new GuiSlot(SlotType.INNER_HOLDER_SLOT, this, 145, 50));
        addButton(new GuiInnerScreen(this, 49, 21, 84, 46, () -> {
            MatrixMultiblockData multiblock = tile.getMultiblock();
            return Arrays.asList(MekanismLang.ENERGY.translate(EnergyDisplay.of(multiblock.getEnergy())),
                  MekanismLang.CAPACITY.translate(EnergyDisplay.of(multiblock.getStorageCap())),
                  MekanismLang.MATRIX_INPUT_AMOUNT.translate(MekanismLang.GENERIC_PER_TICK.translate(EnergyDisplay.of(multiblock.getLastInput()))),
                  MekanismLang.MATRIX_OUTPUT_AMOUNT.translate(MekanismLang.GENERIC_PER_TICK.translate(EnergyDisplay.of(multiblock.getLastOutput()))));
        }).spacing(2));
        addButton(new GuiMatrixTab(this, tile, MatrixTab.STAT));
        addButton(new GuiEnergyGauge(new IEnergyInfoHandler() {
            @Override
            public FloatingLong getEnergy() {
                return tile.getMultiblock().getEnergy();
            }

            @Override
            public FloatingLong getMaxEnergy() {
                return tile.getMultiblock().getStorageCap();
            }
        }, GaugeType.MEDIUM, this, 7, 16, 34, 56));
        addButton(new GuiEnergyTab(this, () -> {
            MatrixMultiblockData multiblock = tile.getMultiblock();
            return Arrays.asList(MekanismLang.STORING.translate(EnergyDisplay.of(multiblock.getEnergy(), multiblock.getStorageCap())),
                  MekanismLang.MATRIX_INPUT_RATE.translate(EnergyDisplay.of(multiblock.getLastInput())),
                  MekanismLang.MATRIX_OUTPUT_RATE.translate(EnergyDisplay.of(multiblock.getLastOutput())));
        }));
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        drawTitleText(matrix, MekanismLang.MATRIX.translate(), titleLabelY);
        drawString(matrix, inventory.getDisplayName(), inventoryLabelX, inventoryLabelY, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}