package mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToLongFunction;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.client.gui.element.GuiDownArrow;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.GuiSideHolder;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiMergedTankGauge;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.tab.GuiContainerEditModeTab;
import mekanism.common.MekanismLang;
import mekanism.common.content.tank.TankMultiblockData;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.multiblock.TileEntityDynamicTank;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class GuiDynamicTank extends GuiMekanismTile<TileEntityDynamicTank, MekanismTileContainer<TileEntityDynamicTank>> {

    public GuiDynamicTank(MekanismTileContainer<TileEntityDynamicTank> container, Inventory inv, Component title) {
        super(container, inv, title);
        inventoryLabelY += 2;
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        //Add the side holder before the slots, as it holds a couple of the slots
        addRenderableWidget(GuiSideHolder.armorHolder(this));
        addRenderableWidget(new GuiElementHolder(this, 141, 16, 26, 56));
        super.addGuiElements();
        addRenderableWidget(new GuiSlot(SlotType.INNER_HOLDER_SLOT, this, 145, 20));
        addRenderableWidget(new GuiSlot(SlotType.INNER_HOLDER_SLOT, this, 145, 50));
        addRenderableWidget(new GuiInnerScreen(this, 49, 21, 84, 46, () -> {
            List<Component> ret = new ArrayList<>();
            TankMultiblockData multiblock = tile.getMultiblock();
            long capacity = multiblock.getChemicalTankCapacity();
            switch (multiblock.mergedTank.getCurrentType()) {
                case EMPTY -> ret.add(MekanismLang.EMPTY.translate());
                case FLUID -> {
                    addStored(ret, multiblock.getFluidTank().getFluid(), FluidStack::getAmount);
                    capacity = multiblock.getTankCapacity();
                }
                case CHEMICAL -> addStored(ret, multiblock.getChemicalTank());
            }
            ret.add(MekanismLang.CAPACITY.translate(""));
            ret.add(MekanismLang.GENERIC_MB.translate(TextUtils.format(capacity)));
            return ret;
        }).spacing(1));
        addRenderableWidget(new GuiDownArrow(this, 150, 39));
        addRenderableWidget(new GuiContainerEditModeTab<>(this, tile));
        addRenderableWidget(new GuiMergedTankGauge<>(() -> tile.getMultiblock().mergedTank, tile::getMultiblock, GaugeType.MEDIUM, this, 7, 16, 34, 56));
    }

    private void addStored(List<Component> ret, IChemicalTank tank) {
        addStored(ret, tank.getStack(), ChemicalStack::getAmount);
    }

    private <STACK> void addStored(List<Component> ret, STACK stack, ToLongFunction<STACK> amountGetter) {
        ret.add(MekanismLang.GENERIC_PRE_COLON.translate(stack));
        ret.add(MekanismLang.GENERIC_MB.translate(TextUtils.format(amountGetter.applyAsLong(stack))));
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        renderInventoryText(guiGraphics);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }
}