package mekanism.client.gui.item;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.text.EnumColor;
import mekanism.client.ClientTickHandler;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.button.ColorButton;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.custom.GuiTeleporterStatus;
import mekanism.client.gui.element.scroll.GuiTextScrollList;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.text.BackgroundType;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.gui.element.text.InputValidator;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.content.teleporter.TeleporterFrequency;
import mekanism.common.inventory.container.item.PortableTeleporterContainer;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyManager;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.network.PacketGuiSetFrequency;
import mekanism.common.network.PacketGuiSetFrequency.FrequencyUpdate;
import mekanism.common.network.PacketTeleporterSetColor;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.OwnerDisplay;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;

public class GuiPortableTeleporter extends GuiMekanism<PortableTeleporterContainer> {

    private final Hand currentHand;
    private final ItemStack itemStack;
    private MekanismButton publicButton;
    private MekanismButton privateButton;
    private MekanismButton setButton;
    private MekanismButton deleteButton;
    private MekanismButton teleportButton;
    private GuiTextScrollList scrollList;
    private GuiTextField frequencyField;
    private boolean privateMode;
    private byte clientStatus;
    private boolean init = false;

    public GuiPortableTeleporter(PortableTeleporterContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        currentHand = container.getHand();
        itemStack = container.getStack();
        ySize = 175;
        titleY = 4;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiTeleporterStatus(this, () -> getFrequency() != null, () -> clientStatus));
        addButton(new GuiVerticalPowerBar(this, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                IEnergyContainer container = StorageUtils.getEnergyContainer(itemStack, 0);
                return container == null ? EnergyDisplay.ZERO.getTextComponent() : EnergyDisplay.of(container.getEnergy(), container.getMaxEnergy()).getTextComponent();
            }

            @Override
            public double getLevel() {
                IEnergyContainer container = StorageUtils.getEnergyContainer(itemStack, 0);
                if (container == null) {
                    return 0;
                }
                return container.getEnergy().divideToLevel(container.getMaxEnergy());
            }
        }, 158, 26));
        addButton(scrollList = new GuiTextScrollList(this, 27, 36, 122, 42));

        addButton(publicButton = new TranslationButton(this, guiLeft + 27, guiTop + 14, 60, 20, MekanismLang.PUBLIC, () -> {
            privateMode = false;
            updateButtons();
        }));
        addButton(privateButton = new TranslationButton(this, guiLeft + 89, guiTop + 14, 60, 20, MekanismLang.PRIVATE, () -> {
            privateMode = true;
            updateButtons();
        }));
        addButton(setButton = new TranslationButton(this, guiLeft + 27, guiTop + 120, 50, 18, MekanismLang.BUTTON_SET, () -> {
            int selection = scrollList.getSelection();
            if (selection != -1) {
                TeleporterFrequency freq = privateMode ? getPrivateFrequencies().get(selection) : getPublicFrequencies().get(selection);
                setFrequencyFromName(freq.getName());
            }
            updateButtons();
        }));
        addButton(deleteButton = new TranslationButton(this, guiLeft + 79, guiTop + 120, 50, 18, MekanismLang.BUTTON_DELETE, () -> {
            int selection = scrollList.getSelection();
            if (selection != -1) {
                TeleporterFrequency freq = privateMode ? getPrivateFrequencies().get(selection) : getPublicFrequencies().get(selection);
                Mekanism.packetHandler.sendToServer(PacketGuiSetFrequency.create(FrequencyUpdate.REMOVE_ITEM, FrequencyType.TELEPORTER, freq.getIdentity(), container.getHand()));
                scrollList.clearSelection();
            }
            updateButtons();
        }));
        addButton(new GuiSlot(SlotType.NORMAL, this, 131, 120).setRenderAboveSlots());
        addButton(new ColorButton(this, guiLeft + 132, guiTop + 121, 16, 16,
              () -> getFrequency() == null ? null : getFrequency().getColor(),
              () -> sendColorUpdate(0),
              () -> sendColorUpdate(1)));
        addButton(teleportButton = new TranslationButton(this, guiLeft + 42, guiTop + 140, 92, 20, MekanismLang.BUTTON_TELEPORT, () -> {
            if (getFrequency() != null && clientStatus == 1) {
                ClientTickHandler.portableTeleport(getMinecraft().player, currentHand, getFrequency());
                getMinecraft().player.closeScreen();
            }
            updateButtons();
        }));
        addButton(frequencyField = new GuiTextField(this, 50, 103, 98, 11));
        frequencyField.setMaxStringLength(FrequencyManager.MAX_FREQ_LENGTH);
        frequencyField.setBackground(BackgroundType.INNER_SCREEN);
        frequencyField.setEnterHandler(this::setFrequency);
        frequencyField.setInputValidator(InputValidator.or(InputValidator.DIGIT, InputValidator.LETTER, InputValidator.FREQUENCY_CHARS));
        frequencyField.addCheckmarkButton(this::setFrequency);
        addButton(new MekanismImageButton(this, guiLeft + 137, guiTop + 103, 11, 12, getButtonLocation("checkmark"), this::setFrequency));
        updateButtons();
    }

    public void setStatus(byte status) {
        clientStatus = status;
    }

    public ITextComponent getSecurity(Frequency freq) {
        if (freq.isPrivate()) {
            return MekanismLang.PRIVATE.translateColored(EnumColor.DARK_RED);
        }
        return MekanismLang.PUBLIC.translate();
    }

    private void setFrequency() {
        setFrequencyFromName(frequencyField.getText());
        frequencyField.setText("");
        updateButtons();
    }

    public void updateButtons() {
        if (getOwnerUsername() == null) {
            return;
        }
        List<String> text = new ArrayList<>();
        if (privateMode) {
            for (TeleporterFrequency freq : getPrivateFrequencies()) {
                text.add(freq.getName());
            }
        } else {
            for (TeleporterFrequency freq : getPublicFrequencies()) {
                text.add(MekanismLang.GENERIC_WITH_PARENTHESIS.translate(freq.getName(), freq.getClientOwner()).getString());
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
            setButton.active = getFrequency() == null || !getFrequency().areIdentitiesEqual(freq);
            UUID ownerUUID = getOwnerUUID();
            deleteButton.active = ownerUUID != null && freq.ownerMatches(ownerUUID);
        } else {
            setButton.active = false;
            deleteButton.active = false;
        }
        if (!itemStack.isEmpty()) {
            teleportButton.active = getFrequency() != null && clientStatus == 1;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!init && getFrequency() != null) {
            init = true;
            privateMode = getFrequency().isPrivate();
        }
        updateButtons();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        updateButtons();
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        drawTitleText(matrix, getName(), titleY);
        drawString(matrix, OwnerDisplay.of(getOwnerUUID(), getOwnerUsername()).getTextComponent(), 8, !itemStack.isEmpty() ? ySize - 12 : ySize - 96 + 4, titleTextColor());
        ITextComponent frequencyComponent = MekanismLang.FREQUENCY.translate();
        drawString(matrix, frequencyComponent, 32, 81, titleTextColor());
        ITextComponent securityComponent = MekanismLang.SECURITY.translate("");
        drawString(matrix, securityComponent, 32, 91, titleTextColor());
        Frequency frequency = getFrequency();
        int frequencyOffset = getStringWidth(frequencyComponent) + 1;
        if (frequency == null) {
            drawString(matrix, MekanismLang.NONE.translateColored(EnumColor.DARK_RED), 32 + frequencyOffset, 81, subheadingTextColor());
            drawString(matrix, MekanismLang.NONE.translateColored(EnumColor.DARK_RED), 32 + getStringWidth(securityComponent), 91, subheadingTextColor());
        } else {
            drawTextScaledBound(matrix, frequency.getName(), 32 + frequencyOffset, 81, subheadingTextColor(), xSize - 32 - frequencyOffset - 4);
            drawString(matrix, getSecurity(frequency), 32 + getStringWidth(securityComponent), 91, subheadingTextColor());
        }
        drawTextScaledBound(matrix, MekanismLang.SET.translate(), 27, 104, titleTextColor(), 20);
        super.drawForegroundText(matrix, mouseX, mouseY);
    }

    private UUID getOwnerUUID() {
        return container.getOwnerUUID();
    }

    private String getOwnerUsername() {
        return container.getOwnerUsername();
    }

    private void setFrequencyFromName(String name) {
        if (!name.isEmpty()) {
            Mekanism.packetHandler.sendToServer(PacketGuiSetFrequency.create(FrequencyUpdate.SET_ITEM, FrequencyType.TELEPORTER,
                  new FrequencyIdentity(name, !privateMode), container.getHand()));
        }
    }

    private ITextComponent getName() {
        return itemStack.getDisplayName();
    }

    public TeleporterFrequency getFrequency() {
        return container.getFrequency();
    }

    private List<TeleporterFrequency> getPublicFrequencies() {
        return container.getPublicCache();
    }

    private List<TeleporterFrequency> getPrivateFrequencies() {
        return container.getPrivateCache();
    }

    private void sendColorUpdate(int extra) {
        TeleporterFrequency freq = getFrequency();
        if (freq != null) {
            Mekanism.packetHandler.sendToServer(PacketTeleporterSetColor.create(container.getHand(), freq, extra));
        }
    }
}