package mekanism.client.gui;

import java.util.Collections;
import mekanism.client.gui.element.GuiContainerEditMode;
import mekanism.client.gui.element.GuiDownArrow;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.TileEntityDynamicTank;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;

public class GuiDynamicTank extends GuiMekanismTile<TileEntityDynamicTank, MekanismTileContainer<TileEntityDynamicTank>> {

    public GuiDynamicTank(MekanismTileContainer<TileEntityDynamicTank> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    protected void initPreSlots() {
        addButton(new GuiElementHolder(this, 141, 15, 26, 57));
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiSlot(SlotType.INNER_HOLDER_SLOT, this, 145, 19));
        addButton(new GuiSlot(SlotType.INNER_HOLDER_SLOT, this, 145, 50));
        addButton(new GuiInnerScreen(this, 50, 23, 80, 42));
        addButton(new GuiDownArrow(this, 150, 39));
        addButton(new GuiContainerEditMode<>(this, tile));
        addButton(new GuiFluidGauge(() -> tile.structure == null ? null : tile.structure.fluidTank,
              () -> tile.structure == null ? Collections.emptyList() : tile.structure.getFluidTanks(null), GaugeType.MEDIUM, this, 6, 13));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        renderTitleText();
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 94) + 2, titleTextColor());
        drawString(MekanismLang.VOLUME.translate(tile.structure == null ? 0 : tile.structure.getVolume()), 53, 26, screenTextColor());
        FluidStack fluidStored = tile.structure == null ? FluidStack.EMPTY : tile.structure.fluidTank.getFluid();
        if (fluidStored.isEmpty()) {
            renderScaledText(MekanismLang.NO_FLUID.translate(), 53, 44, screenTextColor(), 74);
        } else {
            renderScaledText(MekanismLang.GENERIC_PRE_COLON.translate(fluidStored), 53, 44, screenTextColor(), 74);
            drawString(MekanismLang.GENERIC_MB.translate(fluidStored.getAmount()), 53, 53, screenTextColor());
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}