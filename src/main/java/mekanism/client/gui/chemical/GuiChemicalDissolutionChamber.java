package mekanism.client.gui.chemical;

import java.util.Arrays;
import mekanism.client.gui.element.GuiEnergyInfo;
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
import mekanism.common.inventory.container.tile.ChemicalDissolutionChamberContainer;
import mekanism.common.tile.TileEntityChemicalDissolutionChamber;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiChemicalDissolutionChamber extends GuiChemical<TileEntityChemicalDissolutionChamber, ChemicalDissolutionChamberContainer> {

    public GuiChemicalDissolutionChamber(ChemicalDissolutionChamberContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        ResourceLocation resource = getGuiLocation();
        addButton(new GuiSecurityTab<>(this, tileEntity, resource));
        addButton(new GuiRedstoneControl(this, tileEntity, resource));
        addButton(new GuiUpgradeTab(this, tileEntity, resource));
        addButton(new GuiEnergyInfo(() -> Arrays.asList(
              TextComponentUtil.build(Translation.of("gui.mekanism.using"), ": ", EnergyDisplay.of(tileEntity.getEnergyPerTick()), "/t"),
              TextComponentUtil.build(Translation.of("gui.mekanism.needed"), ": ", EnergyDisplay.of(tileEntity.getNeededEnergy()))
        ), this, resource));
        addButton(new GuiGasGauge(() -> tileEntity.injectTank, GuiGauge.Type.STANDARD, this, resource, 5, 4));
        addButton(new GuiGasGauge(() -> tileEntity.outputTank, GuiGauge.Type.STANDARD, this, resource, 133, 13));
        addButton(new GuiSlot(SlotType.NORMAL, this, resource, 154, 4).with(SlotOverlay.POWER));
        addButton(new GuiSlot(SlotType.NORMAL, this, resource, 25, 35));
        addButton(new GuiSlot(SlotType.NORMAL, this, resource, 154, 24).with(SlotOverlay.PLUS));
        addButton(new GuiSlot(SlotType.NORMAL, this, resource, 5, 64).with(SlotOverlay.MINUS));
        addButton(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return tileEntity.getScaledProgress();
            }
        }, ProgressBar.LARGE_RIGHT, this, resource, 62, 39));
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "chemical_dissolution_chamber.png");
    }

    @Override
    protected void drawForegroundText() {
        drawString(TextComponentUtil.translate("gui.mekanism.chemicalDissolutionChamber.short"), 35, 4, 0x404040);
    }
}