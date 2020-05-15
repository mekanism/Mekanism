package mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.chemical.gas.GasStack;
import mekanism.client.gui.element.GuiDownArrow;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.custom.GuiContainerEditMode;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiHybridGauge;
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
        addButton(new GuiInnerScreen(this, 49, 21, 84, 46, () -> {
            List<ITextComponent> ret = new ArrayList<>();
            FluidStack fluidStored = tile.getMultiblock().fluidTank.getFluid();
            GasStack gasStored = tile.getMultiblock().gasTank.getStack();
            if (fluidStored.isEmpty() && gasStored.isEmpty()) {
                ret.add(MekanismLang.EMPTY.translate());
            } else {
                ret.add(MekanismLang.GENERIC_PRE_COLON.translate(!fluidStored.isEmpty() ? fluidStored : gasStored));
                ret.add(MekanismLang.GENERIC_MB.translate(formatInt(!fluidStored.isEmpty() ? fluidStored.getAmount() : gasStored.getAmount())));
            }
            ret.add(MekanismLang.CAPACITY.translate(""));
            // capacity is the same for both fluid and gas tank
            ret.add(MekanismLang.GENERIC_MB.translate(formatInt(tile.getMultiblock().fluidTank.getCapacity())));
            return ret;
        }).defaultFormat().spacing(2));
        addButton(new GuiDownArrow(this, 150, 39));
        addButton(new GuiContainerEditMode<>(this, tile));
        addButton(new GuiHybridGauge(() -> tile.getMultiblock().gasTank, () -> tile.getMultiblock().getGasTanks(null),
                                     () -> tile.getMultiblock().fluidTank, () -> tile.getMultiblock().getFluidTanks(null),
                                     GaugeType.MEDIUM, this, 7, 16, 34, 56));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        renderTitleText();
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 94) + 2, titleTextColor());
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}