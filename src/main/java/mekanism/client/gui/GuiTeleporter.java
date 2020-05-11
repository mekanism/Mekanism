package mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.GuiTextField;
import mekanism.client.gui.element.GuiTextField.InputValidator;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.custom.GuiTeleporterStatus;
import mekanism.client.gui.element.scroll.GuiTextScrollList;
import mekanism.client.gui.element.tab.GuiRedstoneControlTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.Frequency.FrequencyIdentity;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.frequency.FrequencyType;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.PacketGuiSetFrequency;
import mekanism.common.network.PacketGuiSetFrequency.FrequencyUpdate;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.util.text.OwnerDisplay;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiTeleporter extends GuiMekanismTile<TileEntityTeleporter, MekanismTileContainer<TileEntityTeleporter>> {

    private MekanismButton publicButton;
    private MekanismButton privateButton;
    private MekanismButton setButton;
    private MekanismButton deleteButton;
    private GuiTextScrollList scrollList;
    private GuiTextField frequencyField;
    private boolean privateMode;

    public GuiTeleporter(MekanismTileContainer<TileEntityTeleporter> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        if (tile.getFrequency(FrequencyType.TELEPORTER) != null) {
            privateMode = tile.getFrequency(FrequencyType.TELEPORTER).isPrivate();
        }
        ySize += 64;
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiInnerScreen(this, 48, 102, 89, 13));
        addButton(new GuiInnerScreen(this, 136, 102, 13, 13));
        addButton(new GuiTeleporterStatus(this, () -> tile.getFrequency(FrequencyType.TELEPORTER) != null, () -> tile.status));
        addButton(new GuiRedstoneControlTab(this, tile));
        addButton(new GuiUpgradeTab(this, tile));
        addButton(new GuiSecurityTab<>(this, tile));
        addButton(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 158, 26));
        addButton(scrollList = new GuiTextScrollList(this, 27, 36, 122, 42));

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
                Frequency freq = privateMode ? tile.getPrivateCache(FrequencyType.TELEPORTER).get(selection) : tile.getPublicCache(FrequencyType.TELEPORTER).get(selection);
                setFrequency(freq.getName());
            }
            updateButtons();
        }));
        addButton(deleteButton = new TranslationButton(this, getGuiLeft() + 89, getGuiTop() + 116, 60, 20, MekanismLang.BUTTON_DELETE, () -> {
            int selection = scrollList.getSelection();
            if (selection != -1) {
                Frequency freq = privateMode ? tile.getPrivateCache(FrequencyType.TELEPORTER).get(selection) : tile.getPublicCache(FrequencyType.TELEPORTER).get(selection);
                Mekanism.packetHandler.sendToServer(PacketGuiSetFrequency.create(FrequencyUpdate.REMOVE_TILE, FrequencyType.TELEPORTER, freq.getIdentity(), tile.getPos()));
                scrollList.clearSelection();
            }
            updateButtons();
        }));
        addButton(frequencyField = new GuiTextField(this, 50, 104, 86, 11));
        frequencyField.setMaxStringLength(FrequencyManager.MAX_FREQ_LENGTH);
        frequencyField.setEnableBackgroundDrawing(false);
        frequencyField.setEnterHandler(this::setFrequency);
        frequencyField.setInputValidator(InputValidator.or(InputValidator.DIGIT, InputValidator.LETTER, InputValidator.FREQUENCY_CHARS));
        addButton(new MekanismImageButton(this, getGuiLeft() + 137, getGuiTop() + 103, 11, 12, getButtonLocation("checkmark"), this::setFrequency));
        updateButtons();
    }

    public ITextComponent getSecurity(Frequency freq) {
        if (freq.isPublic()) {
            return MekanismLang.PUBLIC.translate();
        }
        return MekanismLang.PRIVATE.translateColored(EnumColor.DARK_RED);
    }

    private void updateButtons() {
        if (getOwner() == null) {
            return;
        }
        List<String> text = new ArrayList<>();
        if (privateMode) {
            for (Frequency freq : tile.getPrivateCache(FrequencyType.TELEPORTER)) {
                text.add(freq.getName());
            }
        } else {
            for (Frequency freq : tile.getPublicCache(FrequencyType.TELEPORTER)) {
                text.add(MekanismLang.GENERIC_WITH_PARENTHESIS.translate(freq.getName(), freq.getClientOwner()).getFormattedText());
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
            Frequency freq = privateMode ? tile.getPrivateCache(FrequencyType.TELEPORTER).get(scrollList.getSelection()) :
                                           tile.getPublicCache(FrequencyType.TELEPORTER).get(scrollList.getSelection());
            setButton.active = tile.getFrequency(FrequencyType.TELEPORTER) == null || !tile.getFrequency(FrequencyType.TELEPORTER).equals(freq);
            deleteButton.active = getOwner().equals(freq.getOwner());
        } else {
            setButton.active = false;
            deleteButton.active = false;
        }
    }

    @Override
    public void tick() {
        super.tick();
        //TODO: Why do we call updateButtons every tick?
        updateButtons();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        updateButtons();
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void setFrequency() {
        setFrequency(frequencyField.getText());
        frequencyField.setText("");
        updateButtons();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        renderTitleText(4);
        drawString(OwnerDisplay.of(getOwner(), tile.getSecurity().getClientOwner()).getTextComponent(), 8, getYSize() - 92, titleTextColor());
        ITextComponent frequencyComponent = MekanismLang.FREQUENCY.translate();
        drawString(frequencyComponent, 32, 81, titleTextColor());
        ITextComponent securityComponent = MekanismLang.SECURITY.translate("");
        drawString(securityComponent, 32, 91, titleTextColor());
        int frequencyOffset = getStringWidth(frequencyComponent) + 1;
        Frequency freq = tile.getFrequency(FrequencyType.TELEPORTER);
        if (freq != null) {
            drawTextScaledBound(freq.getName(), 32 + frequencyOffset, 81, 0x797979, xSize - 32 - frequencyOffset - 4);
            drawString(getSecurity(freq), 32 + getStringWidth(securityComponent), 91, 0x797979);
        } else {
            drawString(MekanismLang.NONE.translateColored(EnumColor.DARK_RED), 32 + frequencyOffset, 81, 0x797979);
            drawString(MekanismLang.NONE.translateColored(EnumColor.DARK_RED), 32 + getStringWidth(securityComponent), 91, 0x797979);
        }
        drawTextScaledBound(MekanismLang.SET.translate(), 27, 104, titleTextColor(), 20);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    private UUID getOwner() {
        return tile.getSecurity().getOwnerUUID();
    }

    public void setFrequency(String freq) {
        if (!freq.isEmpty()) {
            Mekanism.packetHandler.sendToServer(PacketGuiSetFrequency.create(FrequencyUpdate.SET_TILE, FrequencyType.TELEPORTER, new FrequencyIdentity(freq, !privateMode), tile.getPos()));
        }
    }
}