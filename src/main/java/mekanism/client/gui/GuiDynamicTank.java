package mekanism.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ToLongFunction;
import javax.annotation.Nonnull;
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
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;

public class GuiDynamicTank extends GuiMekanismTile<TileEntityDynamicTank, MekanismTileContainer<TileEntityDynamicTank>> {

    public GuiDynamicTank(MekanismTileContainer<TileEntityDynamicTank> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        inventoryLabelY += 2;
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        //Add the side holder before the slots, as it holds a couple of the slots
        addButton(GuiSideHolder.armorHolder(this));
        addButton(new GuiElementHolder(this, 141, 16, 26, 56));
        super.addGuiElements();
        addButton(new GuiSlot(SlotType.INNER_HOLDER_SLOT, this, 145, 20));
        addButton(new GuiSlot(SlotType.INNER_HOLDER_SLOT, this, 145, 50));
        addButton(new GuiInnerScreen(this, 49, 21, 84, 46, () -> {
            List<ITextComponent> ret = new ArrayList<>();
            TankMultiblockData multiblock = tile.getMultiblock();
            long capacity = multiblock.getChemicalTankCapacity();
            switch (multiblock.mergedTank.getCurrentType()) {
                case EMPTY:
                    ret.add(MekanismLang.EMPTY.translate());
                    break;
                case FLUID:
                    addStored(ret, multiblock.getFluidTank().getFluid(), FluidStack::getAmount);
                    capacity = multiblock.getTankCapacity();
                    break;
                case GAS:
                    addStored(ret, multiblock.getGasTank());
                    break;
                case INFUSION:
                    addStored(ret, multiblock.getInfusionTank());
                    break;
                case PIGMENT:
                    addStored(ret, multiblock.getPigmentTank());
                    break;
                case SLURRY:
                    addStored(ret, multiblock.getSlurryTank());
                    break;
            }
            ret.add(MekanismLang.CAPACITY.translate(""));
            ret.add(MekanismLang.GENERIC_MB.translate(TextUtils.format(capacity)));
            return ret;
        }).spacing(2));
        addButton(new GuiDownArrow(this, 150, 39));
        addButton(new GuiContainerEditModeTab<>(this, tile));
        addButton(new GuiMergedTankGauge<>(() -> tile.getMultiblock().mergedTank, tile::getMultiblock, GaugeType.MEDIUM, this, 7, 16, 34, 56));
    }

    private void addStored(List<ITextComponent> ret, IChemicalTank<?, ?> tank) {
        addStored(ret, tank.getStack(), ChemicalStack::getAmount);
    }

    private <STACK> void addStored(List<ITextComponent> ret, STACK stack, ToLongFunction<STACK> amountGetter) {
        ret.add(MekanismLang.GENERIC_PRE_COLON.translate(stack));
        ret.add(MekanismLang.GENERIC_MB.translate(TextUtils.format(amountGetter.applyAsLong(stack))));
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawString(matrix, inventory.getDisplayName(), inventoryLabelX, inventoryLabelY, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}