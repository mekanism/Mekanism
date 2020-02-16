package mekanism.client.gui;

import mekanism.client.gui.element.GuiContainerEditMode;
import mekanism.common.MekanismLang;
import mekanism.common.content.tank.TankUpdateProtocol;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.TileEntityDynamicTank;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;

public class GuiDynamicTank extends GuiEmbeddedGaugeTile<TileEntityDynamicTank, MekanismTileContainer<TileEntityDynamicTank>> {

    public GuiDynamicTank(MekanismTileContainer<TileEntityDynamicTank> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiContainerEditMode(this, tile));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), (getXSize() / 2) - (getStringWidth(tile.getName()) / 2), 6, 0x404040);
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 94) + 2, 0x404040);
        drawString(MekanismLang.VOLUME.translate(tile.clientCapacity / TankUpdateProtocol.FLUID_PER_TANK), 53, 26, 0x00CD00);
        //TODO: 1.14 Convert to GuiElement
        FluidStack fluidStored = tile.structure != null ? tile.structure.fluidStored : FluidStack.EMPTY;
        if (fluidStored.isEmpty()) {
            renderScaledText(MekanismLang.NO_FLUID.translate(), 53, 44, 0x00CD00, 74);
        } else {
            renderScaledText(MekanismLang.GENERIC_PRE_COLON.translate(fluidStored), 53, 44, 0x00CD00, 74);
            drawString(MekanismLang.GENERIC_MB.translate(fluidStored.getAmount()), 53, 53, 0x00CD00);
        }
        int xAxis = mouseX - getGuiLeft();
        int yAxis = mouseY - getGuiTop();
        if (xAxis >= 7 && xAxis <= 39 && yAxis >= 14 && yAxis <= 72) {
            if (fluidStored.isEmpty()) {
                displayTooltip(MekanismLang.EMPTY.translate(), xAxis, yAxis);
            } else {
                displayTooltip(MekanismLang.GENERIC_STORED_MB.translate(fluidStored, fluidStored.getAmount()), xAxis, yAxis);
            }
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        int scaledFluidLevel = tile.getScaledFluidLevel(58);
        if (scaledFluidLevel > 0) {
            displayGauge(7, 14, scaledFluidLevel, tile.structure.fluidStored, 0);
            displayGauge(23, 14, scaledFluidLevel, tile.structure.fluidStored, 1);
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "dynamic_tank.png");
    }

    @Override
    protected ResourceLocation getGaugeResource() {
        return getGuiLocation();
    }
}