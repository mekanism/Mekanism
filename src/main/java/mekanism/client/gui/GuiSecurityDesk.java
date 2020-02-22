package mekanism.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import mekanism.api.TileNetworkList;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.GuiScrollList;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.security.ISecurityTile.SecurityMode;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import mekanism.common.util.text.OwnerDisplay;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;

public class GuiSecurityDesk extends GuiMekanismTile<TileEntitySecurityDesk, MekanismTileContainer<TileEntitySecurityDesk>> {

    private static final List<Character> SPECIAL_CHARS = Arrays.asList('-', '|', '_');
    private static final int MAX_LENGTH = 24;
    private MekanismButton removeButton;
    private MekanismButton publicButton;
    private MekanismButton privateButton;
    private MekanismButton trustedButton;
    private MekanismButton checkboxButton;
    private MekanismButton overrideButton;
    private GuiScrollList scrollList;
    private TextFieldWidget trustedField;

    public GuiSecurityDesk(MekanismTileContainer<TileEntitySecurityDesk> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        ySize += 64;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiInnerScreen(this, 34, 67, 89, 13));
        addButton(new GuiInnerScreen(this, 122, 67, 13, 13));
        addButton(scrollList = new GuiScrollList(this, 14, 14, 120, 40));

        addButton(removeButton = new TranslationButton(this, getGuiLeft() + 13, getGuiTop() + 81, 122, 20, MekanismLang.BUTTON_REMOVE, () -> {
            int selection = scrollList.getSelection();
            if (tile.frequency != null && selection != -1) {
                TileNetworkList data = TileNetworkList.withContents(1, tile.frequency.trusted.get(selection));
                Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, data));
                scrollList.clearSelection();
            }
            updateButtons();
        }));
        addButton(trustedField = new TextFieldWidget(font, getGuiLeft() + 35, getGuiTop() + 69, 86, 11, ""));
        trustedField.setMaxStringLength(MAX_LENGTH);
        trustedField.setEnableBackgroundDrawing(false);
        addButton(publicButton = new MekanismImageButton(this, getGuiLeft() + 13, getGuiTop() + 113, 40, 16, 40, 16, getButtonLocation("public"),
              () -> {
                  Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(3, SecurityMode.PUBLIC)));
                  updateButtons();
              }, getOnHover(MekanismLang.PUBLIC_MODE)));
        addButton(privateButton = new MekanismImageButton(this, getGuiLeft() + 54, getGuiTop() + 113, 40, 16, 40, 16, getButtonLocation("private"),
              () -> {
                  Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(3, SecurityMode.PRIVATE)));
                  updateButtons();
              }, getOnHover(MekanismLang.PRIVATE_MODE)));
        addButton(trustedButton = new MekanismImageButton(this, getGuiLeft() + 95, getGuiTop() + 113, 40, 16, 40, 16, getButtonLocation("trusted"),
              () -> {
                  Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(3, SecurityMode.TRUSTED)));
                  updateButtons();
              }, getOnHover(MekanismLang.TRUSTED_MODE)));
        addButton(checkboxButton = new MekanismImageButton(this, getGuiLeft() + 123, getGuiTop() + 68, 11, 12, getButtonLocation("checkmark"),
              () -> {
                  addTrusted(trustedField.getText());
                  trustedField.setText("");
                  updateButtons();
              }));
        addButton(overrideButton = new MekanismImageButton(this, getGuiLeft() + 146, getGuiTop() + 59, 16, 16, getButtonLocation("exclamation"),
              () -> {
                  Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(2)));
                  updateButtons();
              }, (onHover, xAxis, yAxis) -> {
            if (tile.frequency != null) {
                displayTooltip(MekanismLang.SECURITY_OVERRIDE.translate(OnOff.of(tile.frequency.override)), xAxis, yAxis);
            }
        }));
        updateButtons();
    }

    public void addTrusted(String trusted) {
        if (trusted.isEmpty()) {
            return;
        }
        TileNetworkList data = TileNetworkList.withContents(0, trusted);
        Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, data));
    }

    public void updateButtons() {
        if (tile.ownerUUID != null) {
            List<String> text = new ArrayList<>();
            if (tile.frequency != null) {
                for (UUID uuid : tile.frequency.trusted) {
                    text.add(MekanismUtils.getLastKnownUsername(uuid));
                }
            }
            scrollList.setText(text);
            removeButton.active = scrollList.hasSelection();
        }

        if (tile.frequency != null && tile.ownerUUID != null && tile.ownerUUID.equals(minecraft.player.getUniqueID())) {
            publicButton.active = tile.frequency.securityMode != SecurityMode.PUBLIC;
            privateButton.active = tile.frequency.securityMode != SecurityMode.PRIVATE;
            trustedButton.active = tile.frequency.securityMode != SecurityMode.TRUSTED;
            checkboxButton.active = true;
            overrideButton.active = true;
        } else {
            publicButton.active = false;
            privateButton.active = false;
            trustedButton.active = false;
            checkboxButton.active = false;
            overrideButton.active = false;
        }
    }

    @Override
    public void tick() {
        super.tick();
        updateButtons();
        trustedField.tick();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        updateButtons();
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "security_desk.png");
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (trustedField.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                //Manually handle hitting escape making the field lose focus
                trustedField.setFocused2(false);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                addTrusted(trustedField.getText());
                trustedField.setText("");
                return true;
            }
            return trustedField.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int keyCode) {
        if (trustedField.isFocused()) {
            if (SPECIAL_CHARS.contains(c) || Character.isDigit(c) || Character.isLetter(c)) {
                //Only allow a subset of characters to be entered into the trustedField text box
                return trustedField.charTyped(c, keyCode);
            }
            return false;
        }
        return super.charTyped(c, keyCode);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), (getXSize() / 2) - (getStringWidth(tile.getName()) / 2), 4, 0x404040);
        ITextComponent ownerComponent = OwnerDisplay.of(tile.ownerUUID, tile.clientOwner).getTextComponent();
        drawString(ownerComponent, getXSize() - 7 - getStringWidth(ownerComponent), (getYSize() - 96) + 2, 0x404040);
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 2, 0x404040);
        drawCenteredText(MekanismLang.TRUSTED_PLAYERS.translate(), 74, 57, 0x787878);
        //TODO: Convert to GuiElement
        if (tile.frequency != null) {
            drawString(MekanismLang.SECURITY.translate(tile.frequency.securityMode), 13, 103, 0x404040);
        } else {
            drawString(MekanismLang.SECURITY_OFFLINE.translateColored(EnumColor.RED), 13, 103, 0x404040);
        }
        renderScaledText(MekanismLang.SECURITY_ADD.translate(), 13, 70, 0x404040, 20);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        if (tile.frequency != null && tile.ownerUUID != null && tile.ownerUUID.equals(minecraft.player.getUniqueID())) {
            drawTexturedRect(getGuiLeft() + 145, getGuiTop() + 78, getXSize() + (tile.frequency.override ? 0 : 6), 22, 6, 6);
        } else {
            drawTexturedRect(getGuiLeft() + 145, getGuiTop() + 78, getXSize(), 28, 6, 6);
        }
        MekanismRenderer.resetColor();
    }
}