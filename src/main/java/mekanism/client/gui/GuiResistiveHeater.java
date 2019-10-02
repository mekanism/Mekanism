package mekanism.client.gui;

import java.util.Arrays;
import java.util.Collections;
import mekanism.api.TileNetworkList;
import mekanism.client.gui.button.MekanismImageButton;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiHeatInfo;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.tile.ResistiveHeaterContainer;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.TileEntityResistiveHeater;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class GuiResistiveHeater extends GuiMekanismTile<TileEntityResistiveHeater, ResistiveHeaterContainer> {

    private TextFieldWidget energyUsageField;

    public GuiResistiveHeater(ResistiveHeaterContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        ResourceLocation resource = getGuiLocation();
        addButton(new GuiVerticalPowerBar(this, tileEntity, resource, 164, 15));
        addButton(new GuiSlot(SlotType.POWER, this, resource, 14, 34).with(SlotOverlay.POWER));
        addButton(new GuiSecurityTab<>(this, tileEntity, resource));
        addButton(new GuiRedstoneControl(this, tileEntity, resource));
        addButton(new GuiEnergyInfo(() -> Arrays.asList(
              TextComponentUtil.build(Translation.of("gui.mekanism.using"), ": ", EnergyDisplay.of(tileEntity.energyUsage), "/t"),
              TextComponentUtil.build(Translation.of("gui.mekanism.needed"), ": ", EnergyDisplay.of(tileEntity.getNeededEnergy()))
        ), this, resource));
        addButton(new GuiHeatInfo(() -> {
            TemperatureUnit unit = EnumUtils.TEMPERATURE_UNITS[MekanismConfig.general.tempUnit.get().ordinal()];
            String environment = UnitDisplayUtils.getDisplayShort(tileEntity.lastEnvironmentLoss * unit.intervalSize, false, unit);
            return Collections.singletonList(TextComponentUtil.build(Translation.of("gui.mekanism.dissipated"), ": " + environment + "/t"));
        }, this, resource));

        String prevEnergyUsage = energyUsageField != null ? energyUsageField.getText() : "";
        addButton(energyUsageField = new TextFieldWidget(font, guiLeft + 49, guiTop + 52, 66, 11, prevEnergyUsage));
        energyUsageField.setMaxStringLength(7);
        energyUsageField.setEnableBackgroundDrawing(false);
        addButton(new MekanismImageButton(this, guiLeft + 116, guiTop + 51, 11, 12, getButtonLocation("checkmark"), this::setEnergyUsage));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tileEntity.getName(), (xSize / 2) - (getStringWidth(tileEntity.getName()) / 2), 6, 0x404040);
        drawString(TextComponentUtil.translate("container.inventory"), 8, (ySize - 94) + 2, 0x404040);
        renderScaledText(TextComponentUtil.build(Translation.of("gui.mekanism.temp"), ": ",
              MekanismUtils.getTemperatureDisplay(tileEntity.temperature, TemperatureUnit.AMBIENT)), 50, 25, 0x00CD00, 76);
        renderScaledText(TextComponentUtil.build(Translation.of("gui.mekanism.usage"), ": ", EnergyDisplay.of(tileEntity.energyUsage), "/t"), 50, 41, 0x00CD00, 76);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    private void setEnergyUsage() {
        if (!energyUsageField.getText().isEmpty()) {
            int toUse = Integer.parseInt(energyUsageField.getText());
            TileNetworkList data = TileNetworkList.withContents(toUse);
            Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, data));
            energyUsageField.setText("");
        }
    }

    @Override
    public void tick() {
        super.tick();
        energyUsageField.tick();
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "resistive_heater.png");
    }

    @Override
    public boolean charTyped(char c, int i) {
        if (!energyUsageField.isFocused() || i == GLFW.GLFW_KEY_ESCAPE) {
            return super.charTyped(c, i);
        }
        if (energyUsageField.isFocused() && i == GLFW.GLFW_KEY_ENTER) {
            setEnergyUsage();
            return true;
        }
        if (Character.isDigit(c) || isTextboxKey(c, i)) {
            return energyUsageField.charTyped(c, i);
        }
        return false;
    }
}