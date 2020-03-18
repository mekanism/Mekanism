package mekanism.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.TileNetworkList;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiHeatInfo;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.scroll.GuiTextScrollList;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiSideConfigurationTab;
import mekanism.client.gui.element.tab.GuiTransporterConfigTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.OwnerDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;

public class GuiQuantumEntangloporter extends GuiMekanismTile<TileEntityQuantumEntangloporter, MekanismTileContainer<TileEntityQuantumEntangloporter>> {

    private MekanismButton publicButton;
    private MekanismButton privateButton;
    private MekanismButton setButton;
    private MekanismButton deleteButton;
    private GuiTextScrollList scrollList;
    private TextFieldWidget frequencyField;
    private boolean privateMode;

    public GuiQuantumEntangloporter(MekanismTileContainer<TileEntityQuantumEntangloporter> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        if (tile.frequency != null) {
            privateMode = !tile.frequency.publicFreq;
        }
        ySize += 64;
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiInnerScreen(this, 48, 102, 89, 13));
        addButton(new GuiInnerScreen(this, 136, 102, 13, 13));
        addButton(scrollList = new GuiTextScrollList(this, 27, 36, 122, 42));
        addButton(new GuiSideConfigurationTab(this, tile));
        addButton(new GuiTransporterConfigTab(this, tile));
        addButton(new GuiUpgradeTab(this, tile));
        addButton(new GuiSecurityTab<>(this, tile));

        addButton(publicButton = new TranslationButton(this, getGuiLeft() + 27, getGuiTop() + 14, 60, 20, MekanismLang.PUBLIC, () -> {
            privateMode = false;
            updateButtons();
        }));
        addButton(privateButton = new TranslationButton(this, getGuiLeft() + 89, getGuiTop() + 14, 60, 20, MekanismLang.PRIVATE, () -> {
            privateMode = true;
            updateButtons();
        }));
        addButton(setButton = new TranslationButton(this, getGuiLeft() + 27, getGuiTop() + 116, 60, 20, MekanismLang.BUTTON_SET, () -> {
            int selection = scrollList.getSelection();
            if (selection != -1) {
                Frequency freq = privateMode ? tile.privateCache.get(selection) : tile.publicCache.get(selection);
                setFrequency(freq.name);
            }
            updateButtons();
        }));
        addButton(deleteButton = new TranslationButton(this, getGuiLeft() + 89, getGuiTop() + 116, 60, 20, MekanismLang.BUTTON_DELETE, () -> {
            int selection = scrollList.getSelection();
            if (selection != -1) {
                Frequency freq = privateMode ? tile.privateCache.get(selection) : tile.publicCache.get(selection);
                TileNetworkList data = TileNetworkList.withContents(1, freq.name, freq.publicFreq);
                Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, data));
                scrollList.clearSelection();
            }
            updateButtons();
        }));
        addButton(frequencyField = new TextFieldWidget(font, getGuiLeft() + 50, getGuiTop() + 104, 86, 11, ""));
        frequencyField.setMaxStringLength(FrequencyManager.MAX_FREQ_LENGTH);
        frequencyField.setEnableBackgroundDrawing(false);
        addButton(new MekanismImageButton(this, getGuiLeft() + 137, getGuiTop() + 103, 11, 12, getButtonLocation("checkmark"), () -> {
            setFrequency(frequencyField.getText());
            frequencyField.setText("");
            updateButtons();
        }));
        addButton(new GuiEnergyInfo(() -> Arrays.asList(MekanismLang.STORING.translate(EnergyDisplay.of(tile.getEnergy(), tile.getMaxEnergy())),
              MekanismLang.MATRIX_INPUT_RATE.translate(EnergyDisplay.of(tile.getInputRate()))), this));
        addButton(new GuiHeatInfo(() -> {
            TemperatureUnit unit = EnumUtils.TEMPERATURE_UNITS[MekanismConfig.general.tempUnit.get().ordinal()];
            ITextComponent transfer = UnitDisplayUtils.getDisplayShort(tile.getLastTransferLoss(), unit, false);
            ITextComponent environment = UnitDisplayUtils.getDisplayShort(tile.getLastEnvironmentLoss(), unit, false);
            return Arrays.asList(MekanismLang.TRANSFERRED_RATE.translate(transfer), MekanismLang.DISSIPATED_RATE.translate(environment));
        }, this));
        updateButtons();
    }

    @Override
    public void resize(@Nonnull Minecraft minecraft, int scaledWidth, int scaledHeight) {
        String s = frequencyField.getText();
        super.resize(minecraft, scaledWidth, scaledHeight);
        frequencyField.setText(s);
    }

    public void setFrequency(String freq) {
        if (freq.isEmpty()) {
            return;
        }
        TileNetworkList data = TileNetworkList.withContents(0, freq, !privateMode);
        Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, data));
    }

    public ITextComponent getSecurity(Frequency freq) {
        if (!freq.publicFreq) {
            return MekanismLang.PRIVATE.translateColored(EnumColor.DARK_RED);
        }
        return MekanismLang.PUBLIC.translate();
    }

    private void updateButtons() {
        if (tile.getSecurity().getClientOwner() == null) {
            return;
        }
        List<String> text = new ArrayList<>();
        if (privateMode) {
            for (Frequency freq : tile.privateCache) {
                text.add(freq.name);
            }
        } else {
            for (Frequency freq : tile.publicCache) {
                text.add(freq.name + " (" + freq.clientOwner + ")");
            }
        }
        scrollList.setText(text);
        if (privateMode) {
            publicButton.active = true;
            privateButton.active = false;
        } else {
            publicButton.active = false;
            privateButton.active = true;
        }
        if (scrollList.hasSelection()) {
            Frequency freq = privateMode ? tile.privateCache.get(scrollList.getSelection()) : tile.publicCache.get(scrollList.getSelection());
            setButton.active = tile.getFrequency(null) == null || !tile.getFrequency(null).equals(freq);
            deleteButton.active = tile.getSecurity().getOwnerUUID().equals(freq.ownerUUID);
        } else {
            setButton.active = false;
            deleteButton.active = false;
        }
    }

    @Override
    public void tick() {
        super.tick();
        updateButtons();
        frequencyField.tick();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        updateButtons();
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (frequencyField.canWrite()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                //Manually handle hitting escape making the field lose focus
                frequencyField.setFocused2(false);
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ENTER) {
                setFrequency(frequencyField.getText());
                frequencyField.setText("");
                return true;
            }
            return frequencyField.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int keyCode) {
        if (frequencyField.canWrite()) {
            if (Character.isDigit(c) || Character.isLetter(c) || FrequencyManager.SPECIAL_CHARS.contains(c)) {
                //Only allow a subset of characters to be entered into the frequency text box
                return frequencyField.charTyped(c, keyCode);
            }
            return false;
        }
        return super.charTyped(c, keyCode);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), (getXSize() / 2) - (getStringWidth(tile.getName()) / 2), 4, 0x404040);
        drawString(OwnerDisplay.of(tile.getSecurity().getOwnerUUID(), tile.getSecurity().getClientOwner()).getTextComponent(), 8, (getYSize() - 96) + 4, 0x404040);
        ITextComponent frequencyComponent = MekanismLang.FREQUENCY.translate();
        drawString(frequencyComponent, 32, 81, 0x404040);
        ITextComponent securityComponent = MekanismLang.SECURITY.translate("");
        drawString(securityComponent, 32, 91, 0x404040);
        Frequency frequency = tile.getFrequency(null);
        int frequencyOffset = getStringWidth(frequencyComponent) + 1;
        if (frequency != null) {
            renderScaledText(frequency.name, 32 + frequencyOffset, 81, 0x797979, xSize - 32 - frequencyOffset - 4);
            drawString(getSecurity(frequency), 32 + getStringWidth(securityComponent), 91, 0x797979);
        } else {
            drawString(MekanismLang.NONE.translateColored(EnumColor.DARK_RED), 32 + frequencyOffset, 81, 0x797979);
            drawString(MekanismLang.NONE.translateColored(EnumColor.DARK_RED), 32 + getStringWidth(securityComponent), 91, 0x797979);
        }
        renderScaledText(MekanismLang.SET.translate(), 27, 104, 0x404040, 20);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}