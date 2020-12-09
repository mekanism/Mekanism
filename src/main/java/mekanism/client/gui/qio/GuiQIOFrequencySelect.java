package mekanism.client.gui.qio;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.button.ColorButton;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.scroll.GuiTextScrollList;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.text.BackgroundType;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.gui.element.text.InputValidator;
import mekanism.client.gui.element.window.GuiConfirmationDialog;
import mekanism.client.gui.element.window.GuiConfirmationDialog.DialogType;
import mekanism.common.MekanismLang;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyManager;
import mekanism.common.util.text.OwnerDisplay;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiQIOFrequencySelect<CONTAINER extends Container> extends GuiMekanism<CONTAINER> {

    private MekanismButton publicButton;
    private MekanismButton privateButton;
    private MekanismButton setButton;
    private MekanismButton deleteButton;
    private GuiTextScrollList scrollList;
    private GuiTextField frequencyField;
    private boolean privateMode;

    private boolean init = false;

    public GuiQIOFrequencySelect(CONTAINER container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        ySize -= 6;
        titleY = 5;
    }

    @Override
    public void init() {
        super.init();
        addButton(scrollList = new GuiTextScrollList(this, 27, 39, 122, 42));

        addButton(publicButton = new TranslationButton(this, guiLeft + 27, guiTop + 17, 60, 20, MekanismLang.PUBLIC, () -> {
            privateMode = false;
            updateButtons();
        }));
        addButton(privateButton = new TranslationButton(this, guiLeft + 89, guiTop + 17, 60, 20, MekanismLang.PRIVATE, () -> {
            privateMode = true;
            updateButtons();
        }));
        addButton(setButton = new TranslationButton(this, guiLeft + 27, guiTop + 120, 50, 18, MekanismLang.BUTTON_SET, () -> {
            int selection = scrollList.getSelection();
            if (selection != -1) {
                Frequency freq = privateMode ? getPrivateFrequencies().get(selection) : getPublicFrequencies().get(selection);
                setFrequency(freq.getName());
            }
            updateButtons();
        }));
        addButton(deleteButton = new TranslationButton(this, guiLeft + 79, guiTop + 120, 50, 18, MekanismLang.BUTTON_DELETE,
              () -> GuiConfirmationDialog.show(this, MekanismLang.FREQUENCY_DELETE_CONFIRM.translate(), () -> {
                  int selection = scrollList.getSelection();
                  if (selection != -1) {
                      Frequency freq = privateMode ? getPrivateFrequencies().get(selection) : getPublicFrequencies().get(selection);
                      sendRemoveFrequency(freq.getIdentity());
                      scrollList.clearSelection();
                  }
                  updateButtons();
              }, DialogType.DANGER)));
        addButton(new GuiSlot(SlotType.NORMAL, this, 131, 120).setRenderAboveSlots());
        addButton(new ColorButton(this, guiLeft + 132, guiTop + 121, 16, 16, () -> {
            QIOFrequency frequency = getFrequency();
            return frequency == null ? null : frequency.getColor();
        }, () -> sendColorUpdate(0), () -> sendColorUpdate(1)));
        addButton(frequencyField = new GuiTextField(this, 50, 106, 98, 11));
        frequencyField.setMaxStringLength(FrequencyManager.MAX_FREQ_LENGTH);
        frequencyField.setBackground(BackgroundType.INNER_SCREEN);
        frequencyField.setEnterHandler(this::setFrequency);
        frequencyField.setInputValidator(InputValidator.or(InputValidator.DIGIT, InputValidator.LETTER, InputValidator.FREQUENCY_CHARS));
        frequencyField.addCheckmarkButton(this::setFrequency);
        updateButtons();
    }

    public void setFrequency(String freq) {
        if (!freq.isEmpty()) {
            sendSetFrequency(new FrequencyIdentity(freq, !privateMode));
        }
    }

    public ITextComponent getSecurity(Frequency freq) {
        if (freq.isPublic()) {
            return MekanismLang.PUBLIC.translate();
        }
        return MekanismLang.PRIVATE.translateColored(EnumColor.DARK_RED);
    }

    private void updateButtons() {
        if (getOwnerUsername() == null) {
            return;
        }
        List<String> text = new ArrayList<>();
        if (privateMode) {
            for (Frequency freq : getPrivateFrequencies()) {
                text.add(freq.getName());
            }
        } else {
            for (Frequency freq : getPublicFrequencies()) {
                text.add(freq.getName() + " (" + freq.getClientOwner() + ")");
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
            Frequency freq = privateMode ? getPrivateFrequencies().get(scrollList.getSelection()) :
                             getPublicFrequencies().get(scrollList.getSelection());
            QIOFrequency frequency = getFrequency();
            setButton.active = frequency == null || !frequency.equals(freq);
            UUID ownerUUID = getOwnerUUID();
            deleteButton.active = ownerUUID != null && ownerUUID.equals(freq.getOwner());
        } else {
            setButton.active = false;
            deleteButton.active = false;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!init) {
            QIOFrequency frequency = getFrequency();
            if (frequency != null) {
                init = true;
                privateMode = frequency.isPrivate();
            }
        }
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
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        drawTitleText(matrix, MekanismLang.QIO_FREQUENCY_SELECT.translate(), titleY);
        drawString(matrix, OwnerDisplay.of(getOwnerUUID(), getOwnerUsername()).getTextComponent(), 8, 143, titleTextColor());
        ITextComponent frequencyComponent = MekanismLang.FREQUENCY.translate();
        drawString(matrix, frequencyComponent, 32, 84, titleTextColor());
        ITextComponent securityComponent = MekanismLang.SECURITY.translate("");
        drawString(matrix, securityComponent, 32, 94, titleTextColor());
        Frequency frequency = getFrequency();
        int frequencyOffset = getStringWidth(frequencyComponent) + 1;
        if (frequency == null) {
            drawString(matrix, MekanismLang.NONE.translateColored(EnumColor.DARK_RED), 32 + frequencyOffset, 84, subheadingTextColor());
            drawString(matrix, MekanismLang.NONE.translateColored(EnumColor.DARK_RED), 32 + getStringWidth(securityComponent), 94, subheadingTextColor());
        } else {
            drawTextScaledBound(matrix, frequency.getName(), 32 + frequencyOffset, 84, subheadingTextColor(), xSize - 32 - frequencyOffset - 4);
            drawString(matrix, getSecurity(frequency), 32 + getStringWidth(securityComponent), 94, subheadingTextColor());
        }
        drawTextScaledBound(matrix, MekanismLang.SET.translate(), 27, 107, titleTextColor(), 20);
        super.drawForegroundText(matrix, mouseX, mouseY);
    }

    public abstract void sendSetFrequency(FrequencyIdentity identity);

    public abstract void sendRemoveFrequency(FrequencyIdentity identity);

    public abstract void sendColorUpdate(int extra);

    public abstract QIOFrequency getFrequency();

    public abstract String getOwnerUsername();

    public abstract UUID getOwnerUUID();

    public abstract List<QIOFrequency> getPublicFrequencies();

    public abstract List<QIOFrequency> getPrivateFrequencies();
}
