package mekanism.client.gui;

import mekanism.client.gui.element.GuiContainerEditMode;
import mekanism.client.gui.element.GuiDownArrow;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.TileEntityDynamicTank;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;

public class GuiDynamicTank extends GuiMekanismTile<TileEntityDynamicTank, MekanismTileContainer<TileEntityDynamicTank>> {

    public GuiDynamicTank(MekanismTileContainer<TileEntityDynamicTank> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiInnerScreen(this, 50, 23, 80, 42));
        addButton(new GuiDownArrow(this, 150, 38));
        addButton(new GuiContainerEditMode(this, tile));
        addButton(new GuiFluidGauge(() -> tile.structure == null ? null : tile.structure.fluidTank, GaugeType.MEDIUM, this, 6, 13));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), (getXSize() / 2) - (getStringWidth(tile.getName()) / 2), 6, 0x404040);
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 94) + 2, 0x404040);
        drawString(MekanismLang.VOLUME.translate(tile.structure == null ? 0 : tile.structure.volume), 53, 26, 0x00CD00);
        FluidStack fluidStored = tile.structure == null ? FluidStack.EMPTY : tile.structure.fluidTank.getFluid();
        if (fluidStored.isEmpty()) {
            renderScaledText(MekanismLang.NO_FLUID.translate(), 53, 44, 0x00CD00, 74);
        } else {
            renderScaledText(MekanismLang.GENERIC_PRE_COLON.translate(fluidStored), 53, 44, 0x00CD00, 74);
            drawString(MekanismLang.GENERIC_MB.translate(fluidStored.getAmount()), 53, 53, 0x00CD00);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "dynamic_tank.png");
    }
}