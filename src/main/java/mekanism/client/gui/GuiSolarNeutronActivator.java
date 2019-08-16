package mekanism.client.gui;

import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.common.inventory.container.tile.SolarNeutronActivatorContainer;
import mekanism.common.tile.TileEntitySolarNeutronActivator;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiSolarNeutronActivator extends GuiMekanismTile<TileEntitySolarNeutronActivator, SolarNeutronActivatorContainer> {

    public GuiSolarNeutronActivator(SolarNeutronActivatorContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        ResourceLocation resource = getGuiLocation();
        addGuiElement(new GuiSecurityTab<>(this, tileEntity, resource));
        addGuiElement(new GuiRedstoneControl(this, tileEntity, resource));
        addGuiElement(new GuiUpgradeTab(this, tileEntity, resource));
        addGuiElement(new GuiSlot(SlotType.NORMAL, this, resource, 4, 55).with(SlotOverlay.MINUS));
        addGuiElement(new GuiSlot(SlotType.NORMAL, this, resource, 154, 55).with(SlotOverlay.PLUS));
        addGuiElement(new GuiGasGauge(() -> tileEntity.inputTank, GuiGauge.Type.STANDARD, this, resource, 25, 13));
        addGuiElement(new GuiGasGauge(() -> tileEntity.outputTank, GuiGauge.Type.STANDARD, this, resource, 133, 13));
        addGuiElement(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return tileEntity.getProgress();
            }
        }, ProgressBar.LARGE_RIGHT, this, resource, 62, 38));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tileEntity.getName(), 26, 4, 0x404040);
        drawString(TextComponentUtil.translate("container.inventory"), 8, (ySize - 96) + 4, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png");
    }
}