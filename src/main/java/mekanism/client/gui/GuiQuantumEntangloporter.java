package mekanism.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.scroll.GuiTextScrollList;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.tab.GuiHeatTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.client.gui.element.text.BackgroundType;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.gui.element.text.InputValidator;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyManager;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.network.PacketGuiSetFrequency;
import mekanism.common.network.PacketGuiSetFrequency.FrequencyUpdate;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.OwnerDisplay;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiQuantumEntangloporter extends GuiConfigurableTile<TileEntityQuantumEntangloporter, MekanismTileContainer<TileEntityQuantumEntangloporter>> {

    private MekanismButton publicButton;
    private MekanismButton privateButton;
    private MekanismButton setButton;
    private MekanismButton deleteButton;
    private GuiTextScrollList scrollList;
    private GuiTextField frequencyField;
    private boolean privateMode;

    public GuiQuantumEntangloporter(MekanismTileContainer<TileEntityQuantumEntangloporter> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        if (tile.getFreq() != null) {
            privateMode = tile.getFreq().isPrivate();
        }
        ySize += 64;
        dynamicSlots = true;
    }

    @Override
    public void func_231160_c_() {
        super.func_231160_c_();
        func_230480_a_(scrollList = new GuiTextScrollList(this, 27, 36, 122, 42));
        func_230480_a_(new GuiUpgradeTab(this, tile));
        func_230480_a_(new GuiSecurityTab<>(this, tile));

        func_230480_a_(publicButton = new TranslationButton(this, getGuiLeft() + 27, getGuiTop() + 14, 60, 20, MekanismLang.PUBLIC, () -> {
            privateMode = false;
            updateButtons();
        }));
        func_230480_a_(privateButton = new TranslationButton(this, getGuiLeft() + 89, getGuiTop() + 14, 60, 20, MekanismLang.PRIVATE, () -> {
            privateMode = true;
            updateButtons();
        }));
        func_230480_a_(setButton = new TranslationButton(this, getGuiLeft() + 27, getGuiTop() + 116, 60, 20, MekanismLang.BUTTON_SET, () -> {
            int selection = scrollList.getSelection();
            if (selection != -1) {
                Frequency freq = privateMode ? tile.getPrivateCache(FrequencyType.INVENTORY).get(selection) : tile.getPublicCache(FrequencyType.INVENTORY).get(selection);
                setFrequency(freq.getName());
            }
            updateButtons();
        }));
        func_230480_a_(deleteButton = new TranslationButton(this, getGuiLeft() + 89, getGuiTop() + 116, 60, 20, MekanismLang.BUTTON_DELETE, () -> {
            int selection = scrollList.getSelection();
            if (selection != -1) {
                Frequency freq = privateMode ? tile.getPrivateCache(FrequencyType.INVENTORY).get(selection) : tile.getPublicCache(FrequencyType.INVENTORY).get(selection);
                Mekanism.packetHandler.sendToServer(PacketGuiSetFrequency.create(FrequencyUpdate.REMOVE_TILE, FrequencyType.INVENTORY, freq.getIdentity(), tile.getPos()));
                scrollList.clearSelection();
            }
            updateButtons();
        }));
        func_230480_a_(frequencyField = new GuiTextField(this, 50, 103, 98, 11));
        frequencyField.setMaxStringLength(FrequencyManager.MAX_FREQ_LENGTH);
        frequencyField.setBackground(BackgroundType.INNER_SCREEN);
        frequencyField.setEnterHandler(this::setFrequency);
        frequencyField.setInputValidator(InputValidator.or(InputValidator.DIGIT, InputValidator.LETTER, InputValidator.FREQUENCY_CHARS));
        frequencyField.addCheckmarkButton(this::setFrequency);
        func_230480_a_(new GuiEnergyTab(() -> {
            EnergyDisplay storing = tile.getFreq() == null ? EnergyDisplay.ZERO : EnergyDisplay.of(tile.getFreq().storedEnergy.getEnergy(), tile.getFreq().storedEnergy.getMaxEnergy());
            EnergyDisplay rate = EnergyDisplay.of(tile.getInputRate());
            return Arrays.asList(MekanismLang.STORING.translate(storing), MekanismLang.MATRIX_INPUT_RATE.translate(rate));
        }, this));
        func_230480_a_(new GuiHeatTab(() -> {
            ITextComponent transfer = MekanismUtils.getTemperatureDisplay(tile.getLastTransferLoss(), TemperatureUnit.KELVIN, false);
            ITextComponent environment = MekanismUtils.getTemperatureDisplay(tile.getLastEnvironmentLoss(), TemperatureUnit.KELVIN, false);
            return Arrays.asList(MekanismLang.TRANSFERRED_RATE.translate(transfer), MekanismLang.DISSIPATED_RATE.translate(environment));
        }, this));
        updateButtons();
    }

    public void setFrequency(String freq) {
        if (!freq.isEmpty()) {
            Mekanism.packetHandler.sendToServer(PacketGuiSetFrequency.create(FrequencyUpdate.SET_TILE, FrequencyType.INVENTORY, new FrequencyIdentity(freq, !privateMode), tile.getPos()));
        }
    }

    public ITextComponent getSecurity(Frequency freq) {
        if (freq.isPublic()) {
            return MekanismLang.PUBLIC.translate();
        }
        return MekanismLang.PRIVATE.translateColored(EnumColor.DARK_RED);
    }

    private void updateButtons() {
        if (tile.getSecurity().getClientOwner() == null) {
            return;
        }
        List<String> text = new ArrayList<>();
        if (privateMode) {
            for (Frequency freq : tile.getPrivateCache(FrequencyType.INVENTORY)) {
                text.add(freq.getName());
            }
        } else {
            for (Frequency freq : tile.getPublicCache(FrequencyType.INVENTORY)) {
                text.add(freq.getName() + " (" + freq.getClientOwner() + ")");
            }
        }
        scrollList.setText(text);
        if (privateMode) {
            publicButton.field_230693_o_ = true;
            privateButton.field_230693_o_ = false;
        } else {
            publicButton.field_230693_o_ = false;
            privateButton.field_230693_o_ = true;
        }
        if (scrollList.hasSelection()) {
            Frequency freq = privateMode ? tile.getPrivateCache(FrequencyType.INVENTORY).get(scrollList.getSelection()) :
                             tile.getPublicCache(FrequencyType.INVENTORY).get(scrollList.getSelection());
            setButton.field_230693_o_ = tile.getFrequency(null) == null || !tile.getFrequency(null).equals(freq);
            deleteButton.field_230693_o_ = tile.getSecurity().getOwnerUUID().equals(freq.getOwner());
        } else {
            setButton.field_230693_o_ = false;
            deleteButton.field_230693_o_ = false;
        }
    }

    @Override
    public void func_231023_e_() {
        super.func_231023_e_();
        updateButtons();
    }

    @Override
    public boolean func_231044_a_(double mouseX, double mouseY, int button) {
        updateButtons();
        return super.func_231044_a_(mouseX, mouseY, button);
    }

    private void setFrequency() {
        setFrequency(frequencyField.getText());
        frequencyField.setText("");
        updateButtons();
    }

    @Override
    protected void func_230451_b_(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix, 4);
        drawString(matrix, OwnerDisplay.of(tile.getSecurity().getOwnerUUID(), tile.getSecurity().getClientOwner()).getTextComponent(), 8, (getYSize() - 96) + 4, titleTextColor());
        ITextComponent frequencyComponent = MekanismLang.FREQUENCY.translate();
        drawString(matrix, frequencyComponent, 32, 81, titleTextColor());
        ITextComponent securityComponent = MekanismLang.SECURITY.translate("");
        drawString(matrix, securityComponent, 32, 91, titleTextColor());
        Frequency frequency = tile.getFreq();
        int frequencyOffset = getStringWidth(frequencyComponent) + 1;
        if (frequency == null) {
            drawString(matrix, MekanismLang.NONE.translateColored(EnumColor.DARK_RED), 32 + frequencyOffset, 81, 0x797979);
            drawString(matrix, MekanismLang.NONE.translateColored(EnumColor.DARK_RED), 32 + getStringWidth(securityComponent), 91, 0x797979);
        } else {
            drawTextScaledBound(matrix, frequency.getName(), 32 + frequencyOffset, 81, 0x797979, xSize - 32 - frequencyOffset - 4);
            drawString(matrix, getSecurity(frequency), 32 + getStringWidth(securityComponent), 91, 0x797979);
        }
        drawTextScaledBound(matrix, MekanismLang.SET.translate(), 27, 104, titleTextColor(), 20);
        super.func_230451_b_(matrix, mouseX, mouseY);
    }
}