package mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToLongFunction;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.client.gui.element.GuiDownArrow;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.custom.GuiContainerEditMode;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiMergedTankGauge;
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
            switch (tile.getMultiblock().mergedTank.getCurrentType()) {
                case EMPTY:
                    ret.add(MekanismLang.EMPTY.translate());
                    break;
                case FLUID:
                    addStored(ret, tile.getMultiblock().getFluidTank().getFluid(), FluidStack::getAmount);
                    break;
                case GAS:
                    addStored(ret, tile.getMultiblock().getGasTank());
                    break;
                case INFUSION:
                    addStored(ret, tile.getMultiblock().getInfusionTank());
                    break;
                case PIGMENT:
                    addStored(ret, tile.getMultiblock().getPigmentTank());
                    break;
                case SLURRY:
                    addStored(ret, tile.getMultiblock().getSlurryTank());
                    break;
            }
            ret.add(MekanismLang.CAPACITY.translate(""));
            // capacity is the same for the tank no matter what type it is currently stored
            ret.add(MekanismLang.GENERIC_MB.translate(formatInt(tile.getMultiblock().getTankCapacity())));
            return ret;
        }).defaultFormat().spacing(2));
        addButton(new GuiDownArrow(this, 150, 39));
        addButton(new GuiContainerEditMode<>(this, tile));
        addButton(new GuiMergedTankGauge<>(() -> tile.getMultiblock().mergedTank, tile::getMultiblock, GaugeType.MEDIUM, this, 7, 16, 34, 56));
    }

    private void addStored(List<ITextComponent> ret, IChemicalTank<?, ?> tank) {
        addStored(ret, tank.getStack(), ChemicalStack::getAmount);
    }

    private <STACK> void addStored(List<ITextComponent> ret, STACK stack, ToLongFunction<STACK> amountGetter) {
        ret.add(MekanismLang.GENERIC_PRE_COLON.translate(stack));
        ret.add(MekanismLang.GENERIC_MB.translate(formatInt(amountGetter.applyAsLong(stack))));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        renderTitleText();
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 94) + 2, titleTextColor());
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}