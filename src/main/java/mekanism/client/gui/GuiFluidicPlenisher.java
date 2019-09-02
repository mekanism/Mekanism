package mekanism.client.gui;

import java.util.Arrays;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.bar.GuiPowerBar;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.common.inventory.container.tile.FluidicPlenisherContainer;
import mekanism.common.tile.TileEntityFluidicPlenisher;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

@OnlyIn(Dist.CLIENT)
public class GuiFluidicPlenisher extends GuiMekanismTile<TileEntityFluidicPlenisher, FluidicPlenisherContainer> {

    public GuiFluidicPlenisher(FluidicPlenisherContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        ResourceLocation resource = getGuiLocation();
        addButton(new GuiSlot(SlotType.NORMAL, this, resource, 27, 19));
        addButton(new GuiSlot(SlotType.NORMAL, this, resource, 27, 50));
        addButton(new GuiSlot(SlotType.POWER, this, resource, 142, 34).with(SlotOverlay.POWER));
        addButton(new GuiPowerBar(this, tileEntity, resource, 164, 15));
        addButton(new GuiFluidGauge(() -> tileEntity.fluidTank, GuiGauge.Type.STANDARD, this, resource, 6, 13));
        addButton(new GuiEnergyInfo(() -> Arrays.asList(
              TextComponentUtil.build(Translation.of("gui.mekanism.using"), ": ", EnergyDisplay.of(tileEntity.getEnergyPerTick()), "/t"),
              TextComponentUtil.build(Translation.of("gui.mekanism.needed"), ": ", EnergyDisplay.of(tileEntity.getNeededEnergy()))
        ), this, resource));
        addButton(new GuiSecurityTab<>(this, tileEntity, resource));
        addButton(new GuiRedstoneControl(this, tileEntity, resource));
        addButton(new GuiUpgradeTab(this, tileEntity, resource));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tileEntity.getName(), (xSize / 2) - (getStringWidth(tileEntity.getName()) / 2), 6, 0x404040);
        drawString(TextComponentUtil.translate("container.inventory"), 8, (ySize - 94) + 2, 0x404040);
        drawString(EnergyDisplay.of(tileEntity.getEnergy(), tileEntity.getMaxEnergy()).getTextComponent(), 51, 26, 0x00CD00);
        drawString(TextComponentUtil.build(Translation.of("gui.mekanism.finished"), ": ", YesNo.of(tileEntity.finishedCalc)), 51, 35, 0x00CD00);
        //TODO: 1.14 Convert to GuiElement
        FluidStack fluid = tileEntity.fluidTank.getFluid();
        if (fluid.isEmpty()) {
            drawString(TextComponentUtil.translate("gui.mekanism.noFluid"), 51, 44, 0x00CD00);
        } else {
            drawString(TextComponentUtil.build(fluid, ": " + fluid.getAmount()), 51, 44, 0x00CD00);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "energy_cube.png");
    }
}