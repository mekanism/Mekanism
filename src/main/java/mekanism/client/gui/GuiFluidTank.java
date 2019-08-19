package mekanism.client.gui;

import mekanism.client.gui.element.GuiContainerEditMode;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.inventory.container.tile.fluid.FluidTankContainer;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiFluidTank extends GuiMekanismTile<TileEntityFluidTank, FluidTankContainer> {

    public GuiFluidTank(FluidTankContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        ResourceLocation resource = getGuiLocation();
        addGuiElement(new GuiContainerEditMode(this, tileEntity, resource));
        addGuiElement(new GuiSecurityTab<>(this, tileEntity, resource));
        addGuiElement(new GuiFluidGauge(() -> tileEntity.fluidTank, GuiFluidGauge.Type.WIDE, this, resource, 48, 18));
        addGuiElement(new GuiSlot(SlotType.NORMAL, this, resource, 145, 18).with(SlotOverlay.INPUT));
        addGuiElement(new GuiSlot(SlotType.NORMAL, this, resource, 145, 50).with(SlotOverlay.OUTPUT));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tileEntity.getName(), (xSize / 2) - (getStringWidth(tileEntity.getName()) / 2), 6, 0x404040);
        drawString(TextComponentUtil.translate("container.inventory"), 8, ySize - 96 + 2, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "blank.png");
    }
}