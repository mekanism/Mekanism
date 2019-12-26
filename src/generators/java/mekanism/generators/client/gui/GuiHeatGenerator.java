package mekanism.generators.client.gui;

import java.util.Arrays;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiHeatInfo;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGauge.Type;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.inventory.container.HeatGeneratorContainer;
import mekanism.generators.common.tile.TileEntityHeatGenerator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiHeatGenerator extends GuiMekanismTile<TileEntityHeatGenerator, HeatGeneratorContainer> {

    public GuiHeatGenerator(HeatGeneratorContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        ResourceLocation resource = getGuiLocation();
        addButton(new GuiRedstoneControl(this, tile, resource));
        addButton(new GuiSecurityTab<>(this, tile, resource));
        addButton(new GuiEnergyInfo(() -> Arrays.asList(
              GeneratorsLang.PRODUCING.translate(EnergyDisplay.of(tile.producingEnergy)),
              MekanismLang.MAX_OUTPUT.translate(EnergyDisplay.of(tile.getMaxOutput()))), this, resource));
        addButton(new GuiFluidGauge(() -> tile.lavaTank, Type.WIDE, this, resource, 55, 18));
        addButton(new GuiVerticalPowerBar(this, tile, resource, 164, 15));
        addButton(new GuiSlot(SlotType.NORMAL, this, resource, 16, 34));
        addButton(new GuiSlot(SlotType.NORMAL, this, resource, 142, 34).with(SlotOverlay.POWER));
        addButton(new GuiHeatInfo(() -> {
            TemperatureUnit unit = EnumUtils.TEMPERATURE_UNITS[MekanismConfig.general.tempUnit.get().ordinal()];
            ITextComponent transfer = UnitDisplayUtils.getDisplayShort(tile.lastTransferLoss, unit, false);
            ITextComponent environment = UnitDisplayUtils.getDisplayShort(tile.lastEnvironmentLoss, unit, false);
            return Arrays.asList(GeneratorsLang.TRANSFERRED_RATE.translate(transfer), MekanismLang.DISSIPATED_RATE.translate(environment));
        }, this, resource));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), 45, 6, 0x404040);
        drawString(MekanismLang.INVENTORY.translate(), 8, (ySize - 96) + 2, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "blank.png");
    }
}