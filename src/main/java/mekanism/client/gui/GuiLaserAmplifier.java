package mekanism.client.gui;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.TileNetworkList;
import mekanism.api.math.FloatingLong;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.tab.GuiAmplifierTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.laser.TileEntityLaserAmplifier;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;

public class GuiLaserAmplifier extends GuiMekanismTile<TileEntityLaserAmplifier, MekanismTileContainer<TileEntityLaserAmplifier>> {

    private TextFieldWidget minField;
    private TextFieldWidget maxField;
    private TextFieldWidget timerField;

    public GuiLaserAmplifier(MekanismTileContainer<TileEntityLaserAmplifier> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiEnergyGauge(tile.getEnergyContainer(), GaugeType.STANDARD, this, 6, 10));
        addButton(new GuiSecurityTab<>(this, tile));
        addButton(new GuiRedstoneControl(this, tile));
        addButton(new GuiAmplifierTab(this, tile));
        addButton(timerField = new TextFieldWidget(font, getGuiLeft() + 96, getGuiTop() + 28, 36, 11, ""));
        timerField.setMaxStringLength(4);
        addButton(minField = new TextFieldWidget(font, getGuiLeft() + 96, getGuiTop() + 43, 72, 11, ""));
        minField.setMaxStringLength(10);
        addButton(maxField = new TextFieldWidget(font, getGuiLeft() + 96, getGuiTop() + 58, 72, 11, ""));
        maxField.setMaxStringLength(10);
    }

    @Override
    public void resize(@Nonnull Minecraft minecraft, int scaledWidth, int scaledHeight) {
        String prevTime = timerField.getText();
        String prevMin = minField.getText();
        String prevMax = maxField.getText();
        super.resize(minecraft, scaledWidth, scaledHeight);
        timerField.setText(prevTime);
        minField.setText(prevMin);
        maxField.setText(prevMax);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), 55, 6, 0x404040);
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 2, 0x404040);
        if (tile.time > 0) {
            drawString(MekanismLang.DELAY.translate(tile.time), 26, 30, 0x404040);
        } else {
            drawString(MekanismLang.NO_DELAY.translate(), 26, 30, 0x404040);
        }
        drawString(MekanismLang.MIN.translate(EnergyDisplay.of(tile.minThreshold)), 26, 45, 0x404040);
        drawString(MekanismLang.MAX.translate(EnergyDisplay.of(tile.maxThreshold)), 26, 60, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    public void tick() {
        super.tick();
        minField.tick();
        maxField.tick();
        timerField.tick();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        TextFieldWidget focusedField = getFocusedField();
        if (focusedField != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                //Manually handle hitting escape making the field lose focus
                focusedField.setFocused2(false);
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ENTER) {
                if (minField.canWrite()) {
                    setMinThreshold();
                } else if (maxField.canWrite()) {
                    setMaxThreshold();
                } else if (timerField.canWrite()) {
                    setTime();
                }
                return true;
            }
            return focusedField.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int keyCode) {
        TextFieldWidget focusedField = getFocusedField();
        if (focusedField != null) {
            if (Character.isDigit(c) || ((c == '.' || c == 'E') && focusedField != timerField)) {
                //Only allow a subset of characters to be entered
                return focusedField.charTyped(c, keyCode);
            }
            return false;
        }
        return super.charTyped(c, keyCode);
    }

    @Nullable
    private TextFieldWidget getFocusedField() {
        if (minField.canWrite()) {
            return minField;
        } else if (maxField.canWrite()) {
            return maxField;
        } else if (timerField.canWrite()) {
            return timerField;
        }
        return null;
    }

    private void setMinThreshold() {
        if (!minField.getText().isEmpty()) {
            FloatingLong toUse;
            try {
                toUse = FloatingLong.ZERO.max(FloatingLong.parseFloatingLong(minField.getText()));
            } catch (Exception e) {
                minField.setText("");
                return;
            }
            Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(0, toUse)));
            minField.setText("");
        }
    }

    private void setMaxThreshold() {
        if (!maxField.getText().isEmpty()) {
            FloatingLong toUse;
            try {
                toUse = FloatingLong.ZERO.max(FloatingLong.parseFloatingLong(maxField.getText()));
            } catch (Exception e) {
                maxField.setText("");
                return;
            }
            Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(1, toUse)));
            maxField.setText("");
        }
    }

    private void setTime() {
        if (!timerField.getText().isEmpty()) {
            int toUse = Math.max(0, Integer.parseInt(timerField.getText()));
            TileNetworkList data = TileNetworkList.withContents(2, toUse);
            Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, data));
            timerField.setText("");
        }
    }
}