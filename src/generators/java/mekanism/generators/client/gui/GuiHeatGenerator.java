package mekanism.generators.client.gui;

import java.util.Arrays;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiHeatInfo;
import mekanism.client.gui.element.GuiPowerBar;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGauge.Type;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import mekanism.generators.common.inventory.container.fuel.HeatGeneratorContainer;
import mekanism.generators.common.tile.TileEntityHeatGenerator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiHeatGenerator extends GuiMekanismTile<TileEntityHeatGenerator, HeatGeneratorContainer> {

    public GuiHeatGenerator(HeatGeneratorContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        ResourceLocation resource = getGuiLocation();
        addButton(new GuiRedstoneControl(this, tileEntity, resource));
        addButton(new GuiSecurityTab<>(this, tileEntity, resource));
        addButton(new GuiEnergyInfo(() -> Arrays.asList(
              TextComponentUtil.build(Translation.of("gui.mekanism.producing"), ": ", EnergyDisplay.of(tileEntity.producingEnergy), "/t"),
              TextComponentUtil.build(Translation.of("gui.mekanism.maxOutput"), ": ", EnergyDisplay.of(tileEntity.getMaxOutput()), "/t"))
              , this, resource));
        addButton(new GuiFluidGauge(() -> tileEntity.lavaTank, Type.WIDE, this, resource, 55, 18));
        addButton(new GuiPowerBar(this, tileEntity, resource, 164, 15));
        addButton(new GuiSlot(SlotType.NORMAL, this, resource, 16, 34));
        addButton(new GuiSlot(SlotType.NORMAL, this, resource, 142, 34).with(SlotOverlay.POWER));
        addButton(new GuiHeatInfo(() -> {
            TemperatureUnit unit = TemperatureUnit.values()[MekanismConfig.general.tempUnit.get().ordinal()];
            String transfer = UnitDisplayUtils.getDisplayShort(tileEntity.lastTransferLoss, false, unit);
            String environment = UnitDisplayUtils.getDisplayShort(tileEntity.lastEnvironmentLoss, false, unit);
            return Arrays.asList(TextComponentUtil.build(Translation.of("gui.mekanism.transferred"), ": " + transfer + "/t"),
                  TextComponentUtil.build(Translation.of("gui.mekanism.dissipated"), ": " + environment + "/t"));
        }, this, resource));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tileEntity.getName(), 45, 6, 0x404040);
        drawString(TextComponentUtil.translate("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "heat_generator.png");
    }
}