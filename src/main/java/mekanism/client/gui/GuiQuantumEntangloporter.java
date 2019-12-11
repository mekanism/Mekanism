package mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.TileNetworkList;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.button.MekanismButton;
import mekanism.client.gui.button.MekanismImageButton;
import mekanism.client.gui.button.TranslationButton;
import mekanism.client.gui.element.GuiScrollList;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiSideConfigurationTab;
import mekanism.client.gui.element.tab.GuiTransporterConfigTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.common.Mekanism;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.inventory.container.tile.QuantumEntangloporterContainer;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.OwnerDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;

public class GuiQuantumEntangloporter extends GuiMekanismTile<TileEntityQuantumEntangloporter, QuantumEntangloporterContainer> {

    private MekanismButton publicButton;
    private MekanismButton privateButton;
    private MekanismButton setButton;
    private MekanismButton deleteButton;
    private GuiScrollList scrollList;
    private TextFieldWidget frequencyField;
    private boolean privateMode;

    public GuiQuantumEntangloporter(QuantumEntangloporterContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        if (tile.frequency != null) {
            privateMode = !tile.frequency.publicFreq;
        }
        ySize += 64;
    }

    @Override
    public void init() {
        super.init();
        ResourceLocation resource = getGuiLocation();
        addButton(scrollList = new GuiScrollList(this, resource, 28, 37, 120, 40));
        addButton(new GuiSideConfigurationTab(this, tile, resource));
        addButton(new GuiTransporterConfigTab(this, tile, resource));
        addButton(new GuiUpgradeTab(this, tile, resource));
        addButton(new GuiSecurityTab<>(this, tile, resource));

        addButton(publicButton = new TranslationButton(this, guiLeft + 27, guiTop + 14, 60, 20, "gui.mekanism.public", () -> {
            privateMode = false;
            updateButtons();
        }));
        addButton(privateButton = new TranslationButton(this, guiLeft + 89, guiTop + 14, 60, 20, "gui.mekanism.private", () -> {
            privateMode = true;
            updateButtons();
        }));
        addButton(setButton = new TranslationButton(this, guiLeft + 27, guiTop + 116, 60, 20, "gui.mekanism.set", () -> {
            int selection = scrollList.getSelection();
            if (selection != -1) {
                Frequency freq = privateMode ? tile.privateCache.get(selection) : tile.publicCache.get(selection);
                setFrequency(freq.name);
            }
            updateButtons();
        }));
        addButton(deleteButton = new TranslationButton(this, guiLeft + 89, guiTop + 116, 60, 20, "gui.mekanism.delete", () -> {
            int selection = scrollList.getSelection();
            if (selection != -1) {
                Frequency freq = privateMode ? tile.privateCache.get(selection) : tile.publicCache.get(selection);
                TileNetworkList data = TileNetworkList.withContents(1, freq.name, freq.publicFreq);
                Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, data));
                scrollList.clearSelection();
            }
            updateButtons();
        }));
        addButton(frequencyField = new TextFieldWidget(font, guiLeft + 50, guiTop + 104, 86, 11, ""));
        frequencyField.setMaxStringLength(FrequencyManager.MAX_FREQ_LENGTH);
        frequencyField.setEnableBackgroundDrawing(false);
        addButton(new MekanismImageButton(this, guiLeft + 137, guiTop + 103, 11, 12, getButtonLocation("checkmark"), () -> {
            setFrequency(frequencyField.getText());
            frequencyField.setText("");
            updateButtons();
        }));
        updateButtons();
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
            return TextComponentUtil.build(EnumColor.DARK_RED, Translation.of("gui.mekanism.private"));
        }
        return TextComponentUtil.translate("gui.mekanism.public");
    }

    public void updateButtons() {
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
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "teleporter.png");
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (frequencyField.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                //Manually handle hitting escape making the field lose focus
                frequencyField.setFocused2(false);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER) {
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
        if (frequencyField.isFocused()) {
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
        drawString(tile.getName(), (xSize / 2) - (getStringWidth(tile.getName()) / 2), 4, 0x404040);
        drawString(OwnerDisplay.of(tile.getSecurity().getOwnerUUID(), tile.getSecurity().getClientOwner()).getTextComponent(), 8, (ySize - 96) + 4, 0x404040);
        ITextComponent frequencyComponent = TextComponentUtil.build(Translation.of("gui.mekanism.freq"), ": ");
        drawString(frequencyComponent, 32, 81, 0x404040);
        ITextComponent securityComponent = TextComponentUtil.build(Translation.of("gui.mekanism.security"), ": ");
        drawString(securityComponent, 32, 91, 0x404040);
        //TODO: 1.14 Convert to GuiElement
        Frequency frequency = tile.getFrequency(null);
        if (frequency != null) {
            drawString(frequency.name, 32 + getStringWidth(frequencyComponent), 81, 0x797979);
            drawString(getSecurity(frequency), 32 + getStringWidth(securityComponent), 91, 0x797979);
        } else {
            drawString(TextComponentUtil.build(EnumColor.DARK_RED, Translation.of("gui.mekanism.none")), 32 + getStringWidth(frequencyComponent), 81, 0x797979);
            drawString(TextComponentUtil.build(EnumColor.DARK_RED, Translation.of("gui.mekanism.none")), 32 + getStringWidth(securityComponent), 91, 0x797979);
        }
        renderScaledText(TextComponentUtil.build(Translation.of("gui.mekanism.set"), ":"), 27, 104, 0x404040, 20);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}