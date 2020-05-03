package mekanism.client.gui;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.lwjgl.glfw.GLFW;
import mekanism.api.math.FloatingLong;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.tab.GuiAmplifierTab;
import mekanism.client.gui.element.tab.GuiRedstoneControlTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.PacketGuiInteract;
import mekanism.common.network.PacketGuiInteract.GuiInteraction;
import mekanism.common.network.PacketGuiSetEnergy;
import mekanism.common.network.PacketGuiSetEnergy.GuiEnergyValue;
import mekanism.common.tile.laser.TileEntityLaserAmplifier;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

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
        addButton(new GuiRedstoneControlTab(this, tile));
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
        renderTitleText();
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 2, titleTextColor());
        if (tile.time > 0) {
            drawString(MekanismLang.DELAY.translate(tile.time), 26, 30, titleTextColor());
        } else {
            drawString(MekanismLang.NO_DELAY.translate(), 26, 30, titleTextColor());
        }
        drawString(MekanismLang.MIN.translate(EnergyDisplay.of(tile.minThreshold)), 26, 45, titleTextColor());
        drawString(MekanismLang.MAX.translate(EnergyDisplay.of(tile.maxThreshold)), 26, 60, titleTextColor());
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
            try {
                Mekanism.packetHandler.sendToServer(new PacketGuiSetEnergy(GuiEnergyValue.MIN_THRESHOLD, tile.getPos(),
                      MekanismUtils.convertToJoules(FloatingLong.parseFloatingLong(minField.getText()))));
            } catch (Exception ignored) {
            }
            minField.setText("");
        }
    }

    private void setMaxThreshold() {
        if (!maxField.getText().isEmpty()) {
            try {
                Mekanism.packetHandler.sendToServer(new PacketGuiSetEnergy(GuiEnergyValue.MAX_THRESHOLD, tile.getPos(),
                      MekanismUtils.convertToJoules(FloatingLong.parseFloatingLong(maxField.getText()))));
            } catch (NumberFormatException ignored) {
            }
            maxField.setText("");
        }
    }

    private void setTime() {
        if (!timerField.getText().isEmpty()) {
            Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.SET_TIME, tile, Integer.parseInt(timerField.getText())));
            timerField.setText("");
        }
    }
}