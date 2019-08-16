package mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import mekanism.api.TileNetworkList;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.button.GuiButtonDisableableImage;
import mekanism.client.gui.button.GuiButtonTranslation;
import mekanism.client.gui.element.GuiPowerBar;
import mekanism.client.gui.element.GuiPowerBar.IPowerInfoHandler;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiScrollList;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.inventory.container.tile.TeleporterContainer;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.OwnerDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class GuiTeleporter extends GuiMekanismTile<TileEntityTeleporter, TeleporterContainer> {

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

    public GuiTeleporter(TeleporterContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        ResourceLocation resource = getGuiLocation();
        addGuiElement(new GuiRedstoneControl(this, tileEntity, resource));
        addGuiElement(new GuiUpgradeTab(this, tileEntity, resource));
        addGuiElement(new GuiSecurityTab<>(this, tileEntity, resource));
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
        addGuiElement(new GuiSlot(SlotType.NORMAL, this, resource, 152, 6).with(SlotOverlay.POWER));
        addGuiElement(scrollList = new GuiScrollList(this, resource, 28, 37, 120, 4));
        if (tileEntity.frequency != null) {
            privateMode = !tileEntity.frequency.publicFreq;
        }
        ySize += 64;
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
                Frequency freq = privateMode ? getPrivateCache().get(selection) : getPublicCache().get(selection);
                setFrequency(freq.name);
            }
            updateButtons();
        }));
        buttons.add(deleteButton = new GuiButtonTranslation(guiLeft + 89, guiTop + 116, 60, 20, "gui.delete", onPress -> {
            int selection = scrollList.getSelection();
            if (selection != -1) {
                Frequency freq = privateMode ? getPrivateCache().get(selection) : getPublicCache().get(selection);
                TileNetworkList data = TileNetworkList.withContents(1, freq.name, freq.publicFreq);
                Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, data));
                scrollList.clearSelection();
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
            for (Frequency freq : getPrivateCache()) {
                text.add(freq.name);
            }
        } else {
            for (Frequency freq : getPublicCache()) {
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
            Frequency freq = privateMode ? getPrivateCache().get(scrollList.getSelection()) : getPublicCache().get(scrollList.getSelection());
            setButton.active = getFrequency() == null || !getFrequency().equals(freq);
            deleteButton.active = getOwner().equals(freq.ownerUUID);
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
        super.mouseClicked(mouseX, mouseY, button);
        updateButtons();
        frequencyField.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiTeleporter.png");
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
        drawString(OwnerDisplay.of(getOwner(), tileEntity.getSecurity().getClientOwner()).getTextComponent(), 8, ySize - 92, 0x404040);
        ITextComponent frequencyComponent = TextComponentUtil.build(Translation.of("gui.freq"), ": ");
        drawString(frequencyComponent, 32, 81, 0x404040);
        ITextComponent securityComponent = TextComponentUtil.build(Translation.of("gui.security"), ": ");
        drawString(securityComponent, 32, 91, 0x404040);
        Frequency frequency = getFrequency();
        if (frequency != null) {
            drawString(frequency.name, 32 + getStringWidth(frequencyComponent), 81, 0x797979);
            drawString(getSecurity(frequency), 32 + getStringWidth(securityComponent), 91, 0x797979);
        } else {
            drawString(TextComponentUtil.build(EnumColor.DARK_RED, Translation.of("gui.none")), 32 + getStringWidth(frequencyComponent), 81, 0x797979);
            drawString(TextComponentUtil.build(EnumColor.DARK_RED, Translation.of("gui.none")), 32 + getStringWidth(securityComponent), 91, 0x797979);
        }
        renderScaledText(TextComponentUtil.build(Translation.of("gui.set"), ":"), 27, 104, 0x404040, 20);
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        if (xAxis >= 6 && xAxis <= 24 && yAxis >= 6 && yAxis <= 24) {
            if (frequency == null) {
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
        int y = getFrequency() == null ? 94 : getStatus() == 2 ? 22 : getStatus() == 3 ? 40 : getStatus() == 4 ? 58 : 76;
        drawTexturedRect(guiLeft + 6, guiTop + 6, 176, y, 18, 18);
        frequencyField.drawTextBox();
        MekanismRenderer.resetColor();
    }

    public ITextComponent getStatusDisplay() {
        switch (getStatus()) {
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
        return tileEntity.getSecurity().getOwnerUUID();
    }

    private byte getStatus() {
        return tileEntity != null ? tileEntity.status : clientStatus;
    }

    private List<Frequency> getPublicCache() {
        return tileEntity != null ? tileEntity.publicCache : clientPublicCache;
    }

    private List<Frequency> getPrivateCache() {
        return tileEntity != null ? tileEntity.privateCache : clientPrivateCache;
    }

    private Frequency getFrequency() {
        return tileEntity != null ? tileEntity.frequency : clientFreq;
    }

    public void setFrequency(String freq) {
        if (freq.isEmpty()) {
            return;
        }
        TileNetworkList data = TileNetworkList.withContents(0, freq, !privateMode);
        Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, data));
    }

    private ITextComponent getName() {
        return tileEntity.getName();
    }

    private double getEnergy() {
        return tileEntity.getEnergy();
    }

    private double getMaxEnergy() {
        return tileEntity.getMaxEnergy();
    }
}