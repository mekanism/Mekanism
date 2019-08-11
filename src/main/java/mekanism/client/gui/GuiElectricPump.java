package mekanism.client.gui;

import java.util.Arrays;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiPowerBar;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.common.inventory.container.ContainerElectricPump;
import mekanism.common.tile.TileEntityElectricPump;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.Translation;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiElectricPump extends GuiMekanismTile<TileEntityElectricPump> {


    public GuiElectricPump(PlayerInventory inventory, TileEntityElectricPump tile) {
        super(tile, new ContainerElectricPump(inventory, tile));
        ResourceLocation resource = getGuiLocation();
        addGuiElement(new GuiSlot(SlotType.NORMAL, this, resource, 27, 19));
        addGuiElement(new GuiSlot(SlotType.NORMAL, this, resource, 27, 50));
        addGuiElement(new GuiSlot(SlotType.POWER, this, resource, 142, 34).with(SlotOverlay.POWER));
        addGuiElement(new GuiPowerBar(this, tileEntity, resource, 164, 15));
        addGuiElement(new GuiFluidGauge(() -> tileEntity.fluidTank, GuiGauge.Type.STANDARD, this, resource, 6, 13));
        addGuiElement(new GuiEnergyInfo(() -> Arrays.asList(
              TextComponentUtil.build(Translation.of("mekanism.gui.using"), ": ", EnergyDisplay.of(tileEntity.getEnergyPerTick()), "/t"),
              TextComponentUtil.build(Translation.of("mekanism.gui.needed"), ": ", EnergyDisplay.of(tileEntity.getNeededEnergy()))
        ), this, resource));
        addGuiElement(new GuiSecurityTab<>(this, tileEntity, resource));
        addGuiElement(new GuiRedstoneControl(this, tileEntity, resource));
        addGuiElement(new GuiUpgradeTab(this, tileEntity, resource));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        font.drawString(tileEntity.getName(), (xSize / 2) - (font.getStringWidth(tileEntity.getName()) / 2), 6, 0x404040);
        font.drawString(LangUtils.localize("container.inventory"), 8, (ySize - 94) + 2, 0x404040);
        font.drawString(MekanismUtils.getEnergyDisplay(tileEntity.getEnergy(), tileEntity.getMaxEnergy()), 51, 26, 0x00CD00);
        String text = tileEntity.fluidTank.getFluid() != null ? LangUtils.localizeFluidStack(tileEntity.fluidTank.getFluid()) + ": " + tileEntity.fluidTank.getFluid().amount
                                                              : LangUtils.localize("gui.noFluid");
        renderScaledText(text, 51, 35, 0x00CD00, 74);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiElectricPump.png");
    }
}