package mekanism.client.gui.machine;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiDownArrow;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.machine.TileEntityFluidicPlenisher;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextUtils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;

public class GuiFluidicPlenisher extends GuiMekanismTile<TileEntityFluidicPlenisher, MekanismTileContainer<TileEntityFluidicPlenisher>> {

    public GuiFluidicPlenisher(MekanismTileContainer<TileEntityFluidicPlenisher> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        inventoryLabelY += 2;
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addButton(new GuiInnerScreen(this, 54, 23, 80, 41, () -> {
            List<ITextComponent> list = new ArrayList<>();
            list.add(EnergyDisplay.of(tile.getEnergyContainer()).getTextComponent());
            list.add(MekanismLang.FINISHED.translate(YesNo.of(tile.finishedCalc)));
            FluidStack fluid = tile.fluidTank.getFluid();
            if (fluid.isEmpty()) {
                list.add(MekanismLang.NO_FLUID.translate());
            } else {
                list.add(MekanismLang.GENERIC_STORED_MB.translate(fluid, TextUtils.format(fluid.getAmount())));
            }
            return list;
        }));
        addButton(new GuiDownArrow(this, 32, 39));
        addButton(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 164, 15));
        addButton(new GuiFluidGauge(() -> tile.fluidTank, () -> tile.getFluidTanks(null), GaugeType.STANDARD, this, 6, 13));
        addButton(new GuiEnergyTab(this, tile.getEnergyContainer()));
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawString(matrix, inventory.getDisplayName(), inventoryLabelX, inventoryLabelY, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}