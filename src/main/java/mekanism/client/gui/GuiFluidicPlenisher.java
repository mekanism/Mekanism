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
import mekanism.common.inventory.container.ContainerFluidicPlenisher;
import mekanism.common.tile.TileEntityFluidicPlenisher;
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
import net.minecraftforge.fluids.FluidStack;

@OnlyIn(Dist.CLIENT)
public class GuiFluidicPlenisher extends GuiMekanismTile<TileEntityFluidicPlenisher> {

    public GuiFluidicPlenisher(PlayerInventory inventory, TileEntityFluidicPlenisher tile) {
        super(tile, new ContainerFluidicPlenisher(inventory, tile));
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
        drawString(tileEntity.getName(), (xSize / 2) - (font.getStringWidth(tileEntity.getName()) / 2), 6, 0x404040);
        drawString(LangUtils.localize("container.inventory"), 8, (ySize - 94) + 2, 0x404040);
        drawString(MekanismUtils.getEnergyDisplay(tileEntity.getEnergy(), tileEntity.getMaxEnergy()), 51, 26, 0x00CD00);
        drawString(LangUtils.localize("gui.finished") + ": " + LangUtils.transYesNo(tileEntity.finishedCalc), 51, 35, 0x00CD00);
        FluidStack fluid = tileEntity.fluidTank.getFluid();
        drawString(fluid != null ? LangUtils.localizeFluidStack(fluid) + ": " + fluid.amount : LangUtils.localize("gui.noFluid"), 51, 44, 0x00CD00);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiElectricPump.png");
    }
}