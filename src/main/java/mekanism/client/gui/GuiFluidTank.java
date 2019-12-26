package mekanism.client.gui;

import mekanism.client.gui.element.GuiContainerEditMode;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.FluidTankContainer;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiFluidTank extends GuiMekanismTile<TileEntityFluidTank, FluidTankContainer> {

    public GuiFluidTank(FluidTankContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        ResourceLocation resource = getGuiLocation();
        addButton(new GuiContainerEditMode(this, tile, resource));
        addButton(new GuiSecurityTab<>(this, tile, resource));
        addButton(new GuiFluidGauge(() -> tile.fluidTank, GuiFluidGauge.Type.WIDE, this, resource, 48, 18));
        addButton(new GuiSlot(SlotType.NORMAL, this, resource, 145, 18).with(SlotOverlay.INPUT));
        addButton(new GuiSlot(SlotType.NORMAL, this, resource, 145, 50).with(SlotOverlay.OUTPUT));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), (xSize / 2) - (getStringWidth(tile.getName()) / 2), 6, 0x404040);
        drawString(MekanismLang.INVENTORY.translate(), 8, ySize - 96 + 2, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "blank.png");
    }
}