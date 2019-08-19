package mekanism.client.gui;

import java.util.Collections;
import mekanism.client.gui.element.GuiHeatInfo;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.tile.FuelwoodHeaterContainer;
import mekanism.common.tile.TileEntityFuelwoodHeater;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiFuelwoodHeater extends GuiMekanismTile<TileEntityFuelwoodHeater, FuelwoodHeaterContainer> {

    public GuiFuelwoodHeater(FuelwoodHeaterContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        ResourceLocation resource = getGuiLocation();
        addGuiElement(new GuiSlot(SlotType.NORMAL, this, resource, 14, 28));
        addGuiElement(new GuiSecurityTab<>(this, tileEntity, resource));
        addGuiElement(new GuiHeatInfo(() -> {
            TemperatureUnit unit = TemperatureUnit.values()[MekanismConfig.general.tempUnit.get().ordinal()];
            String environment = UnitDisplayUtils.getDisplayShort(tileEntity.lastEnvironmentLoss * unit.intervalSize, false, unit);
            return Collections.singletonList(TextComponentUtil.build(Translation.of("mekanism.gui.dissipated"), ": " + environment + "/t"));
        }, this, resource));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tileEntity.getName(), (xSize / 2) - (getStringWidth(tileEntity.getName()) / 2), 6, 0x404040);
        drawString(TextComponentUtil.translate("container.inventory"), 8, (ySize - 94) + 2, 0x404040);
        renderScaledText(TextComponentUtil.build(Translation.of("mekanism.gui.temp"), ": ",
              MekanismUtils.getTemperatureDisplay(tileEntity.temperature, TemperatureUnit.AMBIENT)), 50, 25, 0x00CD00, 76);
        renderScaledText(TextComponentUtil.build(Translation.of("mekanism.gui.fuel"), ": " + tileEntity.burnTime), 50, 41, 0x00CD00, 76);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        if (tileEntity.burnTime > 0) {
            int displayInt = tileEntity.burnTime * 13 / tileEntity.maxBurnTime;
            drawTexturedRect(guiLeft + 143, guiTop + 30 + 12 - displayInt, 176, 12 - displayInt, 14, displayInt + 1);
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "fuelwood_heater.png");
    }
}