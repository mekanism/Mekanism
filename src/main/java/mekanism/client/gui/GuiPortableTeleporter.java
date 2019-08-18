package mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import mekanism.api.text.EnumColor;
import mekanism.client.ClientTickHandler;
import mekanism.client.MekanismClient;
import mekanism.client.gui.button.GuiButtonDisableableImage;
import mekanism.client.gui.button.GuiButtonTranslation;
import mekanism.client.gui.element.GuiPowerBar;
import mekanism.client.gui.element.GuiPowerBar.IPowerInfoHandler;
import mekanism.client.gui.element.GuiScrollList;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.inventory.container.item.PortableTeleporterContainer;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.network.PacketPortableTeleporter;
import mekanism.common.network.PacketPortableTeleporter.PortableTeleporterPacketType;
import mekanism.common.security.IOwnerItem;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.OwnerDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class GuiPortableTeleporter extends GuiMekanism<PortableTeleporterContainer> {

    private Hand currentHand;
    private ItemStack itemStack;
    private PlayerEntity player;
    private Button publicButton;
    private Button privateButton;
    private Button setButton;
    private Button deleteButton;
    private Button teleportButton;
    private Button checkboxButton;
    private GuiScrollList scrollList;
    private TextFieldWidget frequencyField;
    private boolean privateMode;
    private Frequency clientFreq;
    private byte clientStatus;
    private List<Frequency> clientPublicCache = new ArrayList<>();
    private List<Frequency> clientPrivateCache = new ArrayList<>();
    private boolean isInit = true;

    public GuiPortableTeleporter(PortableTeleporterContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        //TODO: Can the hand/stack stuff be partially removed?? There is not much reason to contain the hand
        currentHand = container.getHand();
        itemStack = container.getStack();
        player = inv.player;
        ResourceLocation resource = getGuiLocation();
        addGuiElement(new GuiPowerBar(this, new IPowerInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return EnergyDisplay.of(getEnergy(), getMaxEnergy()).getTextComponent();
            }

            @Override
            public double getLevel() {
                return getEnergy() / getMaxEnergy();
            }
        }, resource, 158, 26));
        addGuiElement(scrollList = new GuiScrollList(this, resource, 28, 37, 120, 4));
        ItemPortableTeleporter item = (ItemPortableTeleporter) itemStack.getItem();
        if (item.getFrequency(itemStack) != null) {
            privateMode = !item.getFrequency(itemStack).publicFreq;
            setFrequency(item.getFrequency(itemStack).name);
        } else {
            Mekanism.packetHandler.sendToServer(new PacketPortableTeleporter(PortableTeleporterPacketType.DATA_REQUEST, currentHand, clientFreq));
        }
        ySize = 175;
    }

    @Override
    public void init() {
        super.init();
        buttons.clear();
        buttons.add(publicButton = new GuiButtonTranslation(guiLeft + 27, guiTop + 14, 60, 20, "gui.public", onPress -> {
            privateMode = false;
            updateButtons();
        }));
        buttons.add(privateButton = new GuiButtonTranslation(guiLeft + 89, guiTop + 14, 60, 20, "gui.private", onPress -> {
            privateMode = true;
            updateButtons();
        }));
        buttons.add(setButton = new GuiButtonTranslation(guiLeft + 27, guiTop + 116, 60, 20, "gui.set", onPress -> {
            int selection = scrollList.getSelection();
            if (selection != -1) {
                Frequency freq = privateMode ? clientPrivateCache.get(selection) : clientPublicCache.get(selection);
                setFrequency(freq.name);
            }
            updateButtons();
        }));
        buttons.add(deleteButton = new GuiButtonTranslation(guiLeft + 89, guiTop + 116, 60, 20, "gui.delete", onPress -> {
            int selection = scrollList.getSelection();
            if (selection != -1) {
                Frequency freq = privateMode ? clientPrivateCache.get(selection) : clientPublicCache.get(selection);
                Mekanism.packetHandler.sendToServer(new PacketPortableTeleporter(PortableTeleporterPacketType.DEL_FREQ, currentHand, freq));
                Mekanism.packetHandler.sendToServer(new PacketPortableTeleporter(PortableTeleporterPacketType.DATA_REQUEST, currentHand, null));
                scrollList.clearSelection();
            }
            updateButtons();
        }));
        buttons.add(teleportButton = new GuiButtonTranslation(guiLeft + 42, guiTop + 140, 92, 20, "gui.teleport", onPress -> {
            if (clientFreq != null && clientStatus == 1) {
                //TODO: Set focus
                //minecraft.mainWindow.setIngameFocus();
                ClientTickHandler.portableTeleport(player, currentHand, clientFreq);
            }
            updateButtons();
        }));
        frequencyField = new TextFieldWidget(font, guiLeft + 50, guiTop + 104, 86, 11, "");
        frequencyField.setMaxStringLength(FrequencyManager.MAX_FREQ_LENGTH);
        frequencyField.setEnableBackgroundDrawing(false);
        buttons.add(checkboxButton = new GuiButtonDisableableImage(guiLeft + 137, guiTop + 103, 11, 11, xSize, 11, -11, getGuiLocation(),
              onPress -> {
                  setFrequency(frequencyField.getText());
                  frequencyField.setText("");
                  updateButtons();
              }));
        updateButtons();
        if (!isInit) {
            Mekanism.packetHandler.sendToServer(new PacketPortableTeleporter(PortableTeleporterPacketType.DATA_REQUEST, currentHand, clientFreq));
        } else {
            isInit = false;
        }
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
            return TextComponentUtil.build(EnumColor.DARK_RED, Translation.of("gui.private"));
        }
        return TextComponentUtil.translate("gui.public");
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
        super.mouseClicked(mouseX, mouseY, button);
        updateButtons();
        frequencyField.mouseClicked(mouseX, mouseY, button);
        return true;
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiPortableTeleporter.png");
    }

    @Override
    public boolean charTyped(char c, int i) {
        boolean returnValue = false;
        if (!frequencyField.isFocused() || i == GLFW.GLFW_KEY_ESCAPE) {
            returnValue = super.charTyped(c, i);
        } else if (i == GLFW.GLFW_KEY_ENTER && frequencyField.isFocused()) {
            setFrequency(frequencyField.getText());
            frequencyField.setText("");
            returnValue = true;
        } else if (Character.isDigit(c) || Character.isLetter(c) || isTextboxKey(c, i) || FrequencyManager.SPECIAL_CHARS.contains(c)) {
            returnValue = frequencyField.charTyped(c, i);
        }
        updateButtons();
        return returnValue;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(getName(), (xSize / 2) - (getStringWidth(getName()) / 2), 4, 0x404040);
        drawString(OwnerDisplay.of(getOwner(), getOwnerUsername()).getTextComponent(), 8, !itemStack.isEmpty() ? ySize - 12 : (ySize - 96) + 4, 0x404040);
        ITextComponent frequencyComponent = TextComponentUtil.build(Translation.of("gui.freq"), ": ");
        drawString(frequencyComponent, 32, 81, 0x404040);
        ITextComponent securityComponent = TextComponentUtil.build(Translation.of("gui.security"), ": ");
        drawString(securityComponent, 32, 91, 0x404040);
        if (clientFreq != null) {
            drawString(clientFreq.name, 32 + getStringWidth(frequencyComponent), 81, 0x797979);
            drawString(getSecurity(clientFreq), 32 + getStringWidth(securityComponent), 91, 0x797979);
        } else {
            drawString(TextComponentUtil.build(EnumColor.DARK_RED, Translation.of("gui.none")), 32 + getStringWidth(frequencyComponent), 81, 0x797979);
            drawString(TextComponentUtil.build(EnumColor.DARK_RED, Translation.of("gui.none")), 32 + getStringWidth(securityComponent), 91, 0x797979);
        }
        renderScaledText(TextComponentUtil.build(Translation.of("gui.set"), ":"), 27, 104, 0x404040, 20);
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        if (xAxis >= 6 && xAxis <= 24 && yAxis >= 6 && yAxis <= 24) {
            if (clientFreq == null) {
                displayTooltip(TextComponentUtil.build(EnumColor.DARK_RED, Translation.of("mekanism.gui.teleporter.noFreq")), xAxis, yAxis);
            } else {
                displayTooltip(getStatusDisplay(), xAxis, yAxis);
            }
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        int y = clientFreq == null ? 94 : clientStatus == 2 ? 22 : clientStatus == 3 ? 40 : clientStatus == 4 ? 58 : 76;
        drawTexturedRect(guiLeft + 6, guiTop + 6, 176, y, 18, 18);
        //TODO: Draw Text box
        //frequencyField.drawTextBox();
        MekanismRenderer.resetColor();
    }

    public ITextComponent getStatusDisplay() {
        switch (clientStatus) {
            case 1:
                return TextComponentUtil.build(EnumColor.DARK_GREEN, Translation.of("mekanism.gui.teleporter.ready"));
            case 2:
                return TextComponentUtil.build(EnumColor.DARK_RED, Translation.of("mekanism.gui.teleporter.noFrame"));
            case 3:
                return TextComponentUtil.build(EnumColor.DARK_RED, Translation.of("mekanism.gui.teleporter.noLink"));
            case 4:
                return TextComponentUtil.build(EnumColor.DARK_RED, Translation.of("mekanism.gui.teleporter.needsEnergy"));
        }
        return TextComponentUtil.build(EnumColor.DARK_RED, Translation.of("mekanism.gui.teleporter.noLink"));
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
        Mekanism.packetHandler.sendToServer(new PacketPortableTeleporter(PortableTeleporterPacketType.SET_FREQ, currentHand, newFreq));
    }

    private ITextComponent getName() {
        return itemStack.getDisplayName();
    }

    private double getEnergy() {
        return ((ItemPortableTeleporter) itemStack.getItem()).getEnergy(itemStack);
    }

    private double getMaxEnergy() {
        return ((ItemPortableTeleporter) itemStack.getItem()).getMaxEnergy(itemStack);
    }

    public boolean isStackEmpty() {
        return itemStack.isEmpty();
    }
}