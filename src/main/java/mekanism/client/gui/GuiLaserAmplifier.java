package mekanism.client.gui;

import javax.annotation.Nullable;
import mekanism.api.TileNetworkList;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.gauge.GuiGauge.Type;
import mekanism.client.gui.element.gauge.GuiNumberGauge;
import mekanism.client.gui.element.gauge.GuiNumberGauge.INumberInfoHandler;
import mekanism.client.gui.element.tab.GuiAmplifierTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.TileEntityLaserAmplifier;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;

public class GuiLaserAmplifier extends GuiMekanismTile<TileEntityLaserAmplifier, MekanismTileContainer<TileEntityLaserAmplifier>> {

    private TextFieldWidget minField;
    private TextFieldWidget maxField;
    private TextFieldWidget timerField;

    public GuiLaserAmplifier(MekanismTileContainer<TileEntityLaserAmplifier> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiNumberGauge(new INumberInfoHandler() {
            @Override
            public TextureAtlasSprite getIcon() {
                return MekanismRenderer.energyIcon;
            }

            @Override
            public double getLevel() {
                return tile.collectedEnergy;
            }

            @Override
            public double getMaxLevel() {
                return TileEntityLaserAmplifier.MAX_ENERGY;
            }

            @Override
            public ITextComponent getText(double level) {
                return MekanismLang.STORING.translate(EnergyDisplay.of(tile.getEnergy(), tile.getMaxEnergy()));
            }
        }, Type.STANDARD, this, 6, 10));
        addButton(new GuiSecurityTab<>(this, tile));
        addButton(new GuiRedstoneControl(this, tile));
        addButton(new GuiAmplifierTab(this, tile));

        String prevTime = timerField != null ? timerField.getText() : "";
        addButton(timerField = new TextFieldWidget(font, getGuiLeft() + 96, getGuiTop() + 28, 36, 11, prevTime));
        timerField.setMaxStringLength(4);

        String prevMin = minField != null ? minField.getText() : "";
        addButton(minField = new TextFieldWidget(font, getGuiLeft() + 96, getGuiTop() + 43, 72, 11, prevMin));
        minField.setMaxStringLength(10);

        String prevMax = maxField != null ? maxField.getText() : "";
        addButton(maxField = new TextFieldWidget(font, getGuiLeft() + 96, getGuiTop() + 58, 72, 11, prevMax));
        maxField.setMaxStringLength(10);
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
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "blank.png");
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        TextFieldWidget focusedField = getFocusedField();
        if (focusedField != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                //Manually handle hitting escape making the field lose focus
                focusedField.setFocused2(false);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                if (minField.isFocused()) {
                    setMinThreshold();
                } else if (maxField.isFocused()) {
                    setMaxThreshold();
                } else if (timerField.isFocused()) {
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
        if (minField.isFocused()) {
            return minField;
        } else if (maxField.isFocused()) {
            return maxField;
        } else if (timerField.isFocused()) {
            return timerField;
        }
        return null;
    }

    private void setMinThreshold() {
        if (!minField.getText().isEmpty()) {
            double toUse;
            try {
                toUse = Math.max(0, Double.parseDouble(minField.getText()));
            } catch (Exception e) {
                minField.setText("");
                return;
            }
            TileNetworkList data = TileNetworkList.withContents(0, toUse);
            Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, data));
            minField.setText("");
        }
    }

    private void setMaxThreshold() {
        if (!maxField.getText().isEmpty()) {
            double toUse;
            try {
                toUse = Math.max(0, Double.parseDouble(maxField.getText()));
            } catch (Exception e) {
                maxField.setText("");
                return;
            }
            TileNetworkList data = TileNetworkList.withContents(1, toUse);
            Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, data));
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