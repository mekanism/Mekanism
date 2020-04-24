package mekanism.client.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
        addButton(new GuiElementHolder(this, 141, 16, 26, 56));
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiSlot(SlotType.INNER_HOLDER_SLOT, this, 145, 20));
        addButton(new GuiSlot(SlotType.INNER_HOLDER_SLOT, this, 145, 50));
        addButton(new GuiInnerScreen(this, 51, 23, 80, 42, () -> {
            List<ITextComponent> ret = new ArrayList<>();
            ret.add(MekanismLang.VOLUME.translate(tile.structure == null ? 0 : tile.structure.getVolume()));
            FluidStack fluidStored = tile.structure == null ? FluidStack.EMPTY : tile.structure.fluidTank.getFluid();
            if (fluidStored.isEmpty()) {
                ret.add(MekanismLang.NO_FLUID.translate());
            } else {
                ret.add(MekanismLang.GENERIC_PRE_COLON.translate(fluidStored));
                ret.add(MekanismLang.GENERIC_MB.translate(fluidStored.getAmount()));
            }
            return ret;
        }).defaultFormat());
        addButton(new GuiDownArrow(this, 150, 39));
        addButton(new GuiContainerEditMode<>(this, tile));
        addButton(new GuiFluidGauge(() -> tile.structure == null ? null : tile.structure.fluidTank,
              () -> tile.structure == null ? Collections.emptyList() : tile.structure.getFluidTanks(null), GaugeType.MEDIUM, this, 7, 16, 34, 56));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        renderTitleText();
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 94) + 2, titleTextColor());
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}