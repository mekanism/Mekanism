package mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
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
import mekanism.common.tile.multiblock.TileEntityDynamicTank;
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
            if (tile.getMultiblock().isEmpty()) {
                ret.add(MekanismLang.EMPTY.translate());
            } else {
                FluidStack fluidStored = tile.getMultiblock().getFluidTank().getFluid();
                if (!fluidStored.isEmpty()) {
                    ret.add(MekanismLang.GENERIC_PRE_COLON.translate(fluidStored));
                    ret.add(MekanismLang.GENERIC_MB.translate(formatInt(fluidStored.getAmount())));
                } else {
                    ChemicalStack<?> stored;
                    GasStack gasStored = tile.getMultiblock().getGasTank().getStack();
                    InfusionStack infusionStored = tile.getMultiblock().getInfusionTank().getStack();
                    PigmentStack pigmentStored = tile.getMultiblock().getPigmentTank().getStack();
                    if (!gasStored.isEmpty()) {
                        stored = gasStored;
                    } else if (!infusionStored.isEmpty()) {
                        stored = infusionStored;
                    } else {// if (!pigmentStored.isEmpty())
                        stored = pigmentStored;
                    }
                    ret.add(MekanismLang.GENERIC_PRE_COLON.translate(stored));
                    ret.add(MekanismLang.GENERIC_MB.translate(formatInt(stored.getAmount())));
                }
            }
            ret.add(MekanismLang.CAPACITY.translate(""));
            // capacity is the same for the tank no matter what type it is currently "attuned" to
            ret.add(MekanismLang.GENERIC_MB.translate(formatInt(tile.getMultiblock().getTankCapacity())));
            return ret;
        }).defaultFormat().spacing(2));
        addButton(new GuiDownArrow(this, 150, 39));
        addButton(new GuiContainerEditMode<>(this, tile));
        //TODO: Merged Tank
        addButton(new GuiHybridGauge(() -> tile.getMultiblock().getGasTank(), () -> tile.getMultiblock().getGasTanks(null),
              () -> tile.getMultiblock().getFluidTank(), () -> tile.getMultiblock().getFluidTanks(null),
              GaugeType.MEDIUM, this, 7, 16, 34, 56));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        renderTitleText();
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 94) + 2, titleTextColor());
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}