package mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.text.EnumColor;
import mekanism.client.ClientTickHandler;
import mekanism.client.MekanismClient;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.GuiTeleporterStatus;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.scroll.GuiTextScrollList;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.inventory.container.item.PortableTeleporterContainer;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.network.PacketPortableTeleporterGui;
import mekanism.common.network.PacketPortableTeleporterGui.PortableTeleporterPacketType;
import mekanism.common.security.IOwnerItem;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.OwnerDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;

public class GuiPortableTeleporter extends GuiMekanism<PortableTeleporterContainer> {

    private final Hand currentHand;
    private final ItemStack itemStack;
    private MekanismButton publicButton;
    private MekanismButton privateButton;
    private MekanismButton setButton;
    private MekanismButton deleteButton;
    private MekanismButton teleportButton;
    private GuiTextScrollList scrollList;
    private TextFieldWidget frequencyField;
    private boolean privateMode;
    private Frequency clientFreq;
    private byte clientStatus;
    private List<Frequency> clientPublicCache = new ArrayList<>();
    private List<Frequency> clientPrivateCache = new ArrayList<>();
    private boolean isInit = true;

    public GuiPortableTeleporter(PortableTeleporterContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        currentHand = container.getHand();
        itemStack = container.getStack();
        ySize = 175;
        ItemPortableTeleporter item = (ItemPortableTeleporter) itemStack.getItem();
        if (item.getFrequency(itemStack) != null) {
            privateMode = !item.getFrequency(itemStack).publicFreq;
            setFrequency(item.getFrequency(itemStack).name);
        } else {
            Mekanism.packetHandler.sendToServer(new PacketPortableTeleporterGui(PortableTeleporterPacketType.DATA_REQUEST, currentHand, clientFreq));
        }
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiInnerScreen(this, 48, 102, 89, 13));
        addButton(new GuiInnerScreen(this, 136, 102, 13, 13));
        addButton(new GuiTeleporterStatus(this, () -> clientFreq != null, () -> clientStatus));
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
                Frequency freq = privateMode ? clientPrivateCache.get(selection) : clientPublicCache.get(selection);
                setFrequency(freq.name);
            }
            updateButtons();
        }));
        addButton(deleteButton = new TranslationButton(this, getGuiLeft() + 89, getGuiTop() + 116, 60, 20, MekanismLang.BUTTON_DELETE, () -> {
            int selection = scrollList.getSelection();
            if (selection != -1) {
                Frequency freq = privateMode ? clientPrivateCache.get(selection) : clientPublicCache.get(selection);
                Mekanism.packetHandler.sendToServer(new PacketPortableTeleporterGui(PortableTeleporterPacketType.DEL_FREQ, currentHand, freq));
                Mekanism.packetHandler.sendToServer(new PacketPortableTeleporterGui(PortableTeleporterPacketType.DATA_REQUEST, currentHand, null));
                scrollList.clearSelection();
            }
            updateButtons();
        }));
        addButton(teleportButton = new TranslationButton(this, getGuiLeft() + 42, getGuiTop() + 140, 92, 20, MekanismLang.BUTTON_TELEPORT, () -> {
            if (clientFreq != null && clientStatus == 1) {
                ClientTickHandler.portableTeleport(minecraft.player, currentHand, clientFreq);
                minecraft.player.closeScreen();
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
        updateButtons();
        if (isInit) {
            isInit = false;
        } else {
            Mekanism.packetHandler.sendToServer(new PacketPortableTeleporterGui(PortableTeleporterPacketType.DATA_REQUEST, currentHand, clientFreq));
        }
    }

    @Override
    public void resize(@Nonnull Minecraft minecraft, int scaledWidth, int scaledHeight) {
        String s = frequencyField.getText();
        super.resize(minecraft, scaledWidth, scaledHeight);
        frequencyField.setText(s);
    }

    public void setFrequency(Frequency newFrequency) {
        clientFreq = newFrequency;
    }

    public void setPublicCache(List<Frequency> cache) {
        clientPublicCache = cache;
    }

    public void setPrivateCache(List<Frequency> cache) {
        clientPrivateCache = cache;
    }

    public void setStatus(byte status) {
        clientStatus = status;
    }

    public ITextComponent getSecurity(Frequency freq) {
        if (!freq.publicFreq) {
            return MekanismLang.PRIVATE.translateColored(EnumColor.DARK_RED);
        }
        return MekanismLang.PUBLIC.translate();
    }

    public void updateButtons() {
        if (getOwner() == null) {
            return;
        }
        List<String> text = new ArrayList<>();
        if (privateMode) {
            for (Frequency freq : clientPrivateCache) {
                text.add(freq.name);
            }
        } else {
            for (Frequency freq : clientPublicCache) {
                text.add(MekanismLang.GENERIC_WITH_PARENTHESIS.translate(freq.name, freq.clientOwner).getFormattedText());
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
            Frequency freq = privateMode ? clientPrivateCache.get(scrollList.getSelection()) : clientPublicCache.get(scrollList.getSelection());
            setButton.active = clientFreq == null || !clientFreq.equals(freq);
            deleteButton.active = getOwner().equals(freq.ownerUUID);
        } else {
            setButton.active = false;
            deleteButton.active = false;
        }
        if (!itemStack.isEmpty()) {
            teleportButton.active = clientFreq != null && clientStatus == 1;
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
        drawString(getName(), (getXSize() / 2) - (getStringWidth(getName()) / 2), 4, 0x404040);
        drawString(OwnerDisplay.of(getOwner(), getOwnerUsername()).getTextComponent(), 8, !itemStack.isEmpty() ? getYSize() - 12 : (getYSize() - 96) + 4, 0x404040);
        ITextComponent frequencyComponent = MekanismLang.FREQUENCY.translate();
        drawString(frequencyComponent, 32, 81, 0x404040);
        ITextComponent securityComponent = MekanismLang.SECURITY.translate("");
        drawString(securityComponent, 32, 91, 0x404040);
        int frequencyOffset = getStringWidth(frequencyComponent) + 1;
        if (clientFreq != null) {
            renderScaledText(clientFreq.name, 32 + frequencyOffset, 81, 0x797979, xSize - 32 - frequencyOffset - 4);
            drawString(getSecurity(clientFreq), 32 + getStringWidth(securityComponent), 91, 0x797979);
        } else {
            drawString(MekanismLang.NONE.translateColored(EnumColor.DARK_RED), 32 + frequencyOffset, 81, 0x797979);
            drawString(MekanismLang.NONE.translateColored(EnumColor.DARK_RED), 32 + getStringWidth(securityComponent), 91, 0x797979);
        }
        renderScaledText(MekanismLang.SET.translate(), 27, 104, 0x404040, 20);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    private UUID getOwner() {
        return ((IOwnerItem) itemStack.getItem()).getOwnerUUID(itemStack);
    }

    private String getOwnerUsername() {
        return MekanismClient.clientUUIDMap.get(((IOwnerItem) itemStack.getItem()).getOwnerUUID(itemStack));
    }

    public void setFrequency(String freq) {
        if (freq.isEmpty()) {
            return;
        }
        Frequency newFreq = new Frequency(freq, null).setPublic(!privateMode);
        Mekanism.packetHandler.sendToServer(new PacketPortableTeleporterGui(PortableTeleporterPacketType.SET_FREQ, currentHand, newFreq));
    }

    private ITextComponent getName() {
        return itemStack.getDisplayName();
    }
}