package mekanism.client.gui.item;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.text.EnumColor;
import mekanism.client.ClientTickHandler;
import mekanism.client.MekanismClient;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.custom.GuiTeleporterStatus;
import mekanism.client.gui.element.scroll.GuiTextScrollList;
import mekanism.client.gui.element.text.BackgroundType;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.gui.element.text.InputValidator;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.content.teleporter.TeleporterFrequency;
import mekanism.common.inventory.container.item.PortableTeleporterContainer;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.lib.frequency.FrequencyManager;
import mekanism.common.lib.security.IOwnerItem;
import mekanism.common.network.PacketPortableTeleporterGui;
import mekanism.common.network.PacketPortableTeleporterGui.PortableTeleporterPacketType;
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
    private TeleporterFrequency clientFreq;
    private byte clientStatus;
    private List<TeleporterFrequency> clientPublicCache = new ArrayList<>();
    private List<TeleporterFrequency> clientPrivateCache = new ArrayList<>();
    private boolean isInit = true;

    public GuiPortableTeleporter(PortableTeleporterContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        currentHand = container.getHand();
        itemStack = container.getStack();
        ySize = 175;
        ItemPortableTeleporter item = (ItemPortableTeleporter) itemStack.getItem();
        if (item.getFrequency(itemStack) != null) {
            privateMode = !item.getFrequency(itemStack).isPublic();
            setFrequencyFromName((String) item.getFrequency(itemStack).getKey());
        } else {
            Mekanism.packetHandler.sendToServer(new PacketPortableTeleporterGui(PortableTeleporterPacketType.DATA_REQUEST, currentHand, clientFreq));
        }
    }

    @Override
    public void func_231160_c_() {
        super.func_231160_c_();
        //func_230480_a_(new GuiInnerScreen(this, 48, 102, 89, 13));
        //func_230480_a_(new GuiInnerScreen(this, 136, 102, 13, 13));
        func_230480_a_(new GuiTeleporterStatus(this, () -> clientFreq != null, () -> clientStatus));
        func_230480_a_(new GuiVerticalPowerBar(this, new IBarInfoHandler() {
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
        func_230480_a_(scrollList = new GuiTextScrollList(this, 27, 36, 122, 42));

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
                TeleporterFrequency freq = privateMode ? clientPrivateCache.get(selection) : clientPublicCache.get(selection);
                setFrequencyFromName(freq.getName());
            }
            updateButtons();
        }));
        func_230480_a_(deleteButton = new TranslationButton(this, getGuiLeft() + 89, getGuiTop() + 116, 60, 20, MekanismLang.BUTTON_DELETE, () -> {
            int selection = scrollList.getSelection();
            if (selection != -1) {
                TeleporterFrequency freq = privateMode ? clientPrivateCache.get(selection) : clientPublicCache.get(selection);
                Mekanism.packetHandler.sendToServer(new PacketPortableTeleporterGui(PortableTeleporterPacketType.DEL_FREQ, currentHand, freq));
                Mekanism.packetHandler.sendToServer(new PacketPortableTeleporterGui(PortableTeleporterPacketType.DATA_REQUEST, currentHand, null));
                scrollList.clearSelection();
            }
            updateButtons();
        }));
        func_230480_a_(teleportButton = new TranslationButton(this, getGuiLeft() + 42, getGuiTop() + 140, 92, 20, MekanismLang.BUTTON_TELEPORT, () -> {
            if (clientFreq != null && clientStatus == 1) {
                ClientTickHandler.portableTeleport(getMinecraft().player, currentHand, clientFreq);
                getMinecraft().player.closeScreen();
            }
            updateButtons();
        }));
        func_230480_a_(frequencyField = new GuiTextField(this, 50, 104, 86, 11));
        frequencyField.setMaxStringLength(FrequencyManager.MAX_FREQ_LENGTH);
        frequencyField.setBackground(BackgroundType.INNER_SCREEN);
        frequencyField.setEnterHandler(this::setFrequency);
        frequencyField.setInputValidator(InputValidator.or(InputValidator.DIGIT, InputValidator.LETTER, InputValidator.FREQUENCY_CHARS));
        func_230480_a_(new MekanismImageButton(this, getGuiLeft() + 137, getGuiTop() + 103, 11, 12, getButtonLocation("checkmark"), this::setFrequency));
        updateButtons();
        if (isInit) {
            isInit = false;
        } else {
            Mekanism.packetHandler.sendToServer(new PacketPortableTeleporterGui(PortableTeleporterPacketType.DATA_REQUEST, currentHand, clientFreq));
        }
    }

    public void setFrequency(TeleporterFrequency newFrequency) {
        clientFreq = newFrequency;
    }

    public void setPublicCache(List<TeleporterFrequency> cache) {
        clientPublicCache = cache;
    }

    public void setPrivateCache(List<TeleporterFrequency> cache) {
        clientPrivateCache = cache;
    }

    public void setStatus(byte status) {
        clientStatus = status;
    }

    public ITextComponent getSecurity(TeleporterFrequency freq) {
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
        if (getOwner() == null) {
            return;
        }
        List<String> text = new ArrayList<>();
        if (privateMode) {
            for (TeleporterFrequency freq : clientPrivateCache) {
                text.add(freq.getName());
            }
        } else {
            for (TeleporterFrequency freq : clientPublicCache) {
                text.add(MekanismLang.GENERIC_WITH_PARENTHESIS.translate(freq.getName(), freq.getClientOwner()).getString());
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
            TeleporterFrequency freq = privateMode ? clientPrivateCache.get(scrollList.getSelection()) : clientPublicCache.get(scrollList.getSelection());
            setButton.field_230693_o_ = clientFreq == null || !clientFreq.equals(freq);
            deleteButton.field_230693_o_ = getOwner().equals(freq.getOwner());
        } else {
            setButton.field_230693_o_ = false;
            deleteButton.field_230693_o_ = false;
        }
        if (!itemStack.isEmpty()) {
            teleportButton.field_230693_o_ = clientFreq != null && clientStatus == 1;
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

    @Override
    protected void func_230451_b_(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        drawTitleText(matrix, getName(), 4);
        drawString(matrix, OwnerDisplay.of(getOwner(), getOwnerUsername()).getTextComponent(), 8, !itemStack.isEmpty() ? getYSize() - 12 : (getYSize() - 96) + 4, titleTextColor());
        ITextComponent frequencyComponent = MekanismLang.FREQUENCY.translate();
        drawString(matrix, frequencyComponent, 32, 81, titleTextColor());
        ITextComponent securityComponent = MekanismLang.SECURITY.translate("");
        drawString(matrix, securityComponent, 32, 91, titleTextColor());
        int frequencyOffset = getStringWidth(frequencyComponent) + 1;
        if (clientFreq != null) {
            drawTextScaledBound(matrix, clientFreq.getName(), 32 + frequencyOffset, 81, 0x797979, xSize - 32 - frequencyOffset - 4);
            drawString(matrix, getSecurity(clientFreq), 32 + getStringWidth(securityComponent), 91, 0x797979);
        } else {
            drawString(matrix, MekanismLang.NONE.translateColored(EnumColor.DARK_RED), 32 + frequencyOffset, 81, 0x797979);
            drawString(matrix, MekanismLang.NONE.translateColored(EnumColor.DARK_RED), 32 + getStringWidth(securityComponent), 91, 0x797979);
        }
        drawTextScaledBound(matrix, MekanismLang.SET.translate(), 27, 104, titleTextColor(), 20);
        super.func_230451_b_(matrix, mouseX, mouseY);
    }

    private UUID getOwner() {
        return ((IOwnerItem) itemStack.getItem()).getOwnerUUID(itemStack);
    }

    private String getOwnerUsername() {
        return MekanismClient.clientUUIDMap.get(((IOwnerItem) itemStack.getItem()).getOwnerUUID(itemStack));
    }

    public void setFrequencyFromName(String name) {
        if (name.isEmpty()) {
            return;
        }
        TeleporterFrequency newFreq = new TeleporterFrequency(name, null);
        newFreq.setPublic(!privateMode);
        Mekanism.packetHandler.sendToServer(new PacketPortableTeleporterGui(PortableTeleporterPacketType.SET_FREQ, currentHand, newFreq));
    }

    private ITextComponent getName() {
        return itemStack.getDisplayName();
    }
}