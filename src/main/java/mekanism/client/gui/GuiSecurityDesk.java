package mekanism.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mekanism.api.EnumColor;
import mekanism.api.TileNetworkList;
import mekanism.client.gui.button.GuiButtonDisableableImage;
import mekanism.client.gui.element.GuiScrollList;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.ContainerSecurityDesk;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.security.ISecurityTile.SecurityMode;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class GuiSecurityDesk extends GuiMekanismTile<TileEntitySecurityDesk> {

    private static final List<Character> SPECIAL_CHARS = Arrays.asList('-', '|', '_');
    private static final int MAX_LENGTH = 24;
    private Button removeButton;
    private Button publicButton;
    private Button privateButton;
    private Button trustedButton;
    private Button checkboxButton;
    private Button overrideButton;
    private GuiScrollList scrollList;
    private TextFieldWidget trustedField;

    public GuiSecurityDesk(PlayerInventory inventory, TileEntitySecurityDesk tile) {
        super(tile, new ContainerSecurityDesk(inventory, tile));
        addGuiElement(scrollList = new GuiScrollList(this, getGuiLocation(), 14, 14, 120, 4));
        ySize += 64;
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.clear();
        buttonList.add(removeButton = new Button(0, guiLeft + 13, guiTop + 81, 122, 20, LangUtils.localize("gui.remove")));
        trustedField = new TextFieldWidget(1, fontRenderer, guiLeft + 35, guiTop + 69, 86, 11);
        trustedField.setMaxStringLength(MAX_LENGTH);
        trustedField.setEnableBackgroundDrawing(false);
        buttonList.add(publicButton = new GuiButtonDisableableImage(2, guiLeft + 13, guiTop + 113, 40, 16, xSize, 64, -16, 16, getGuiLocation()));
        buttonList.add(privateButton = new GuiButtonDisableableImage(3, guiLeft + 54, guiTop + 113, 40, 16, xSize + 40, 64, -16, 16, getGuiLocation()));
        buttonList.add(trustedButton = new GuiButtonDisableableImage(4, guiLeft + 95, guiTop + 113, 40, 16, xSize, 112, -16, 16, getGuiLocation()));
        buttonList.add(checkboxButton = new GuiButtonDisableableImage(5, guiLeft + 123, guiTop + 68, 11, 11, xSize, 11, -11, getGuiLocation()));
        buttonList.add(overrideButton = new GuiButtonDisableableImage(6, guiLeft + 146, guiTop + 59, 16, 16, xSize + 12, 16, -16, 16, getGuiLocation()));
        updateButtons();
    }

    public void addTrusted(String trusted) {
        if (trusted.isEmpty()) {
            return;
        }
        TileNetworkList data = TileNetworkList.withContents(0, trusted);
        Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, data));
    }

    public void updateButtons() {
        if (tileEntity.ownerUUID != null) {
            List<String> text = new ArrayList<>();
            if (tileEntity.frequency != null) {
                for (String s : tileEntity.frequency.trusted) {
                    text.add(s);
                }
            }
            scrollList.setText(text);
            removeButton.enabled = scrollList.hasSelection();
        }

        if (tileEntity.frequency != null && tileEntity.ownerUUID != null && tileEntity.ownerUUID.equals(minecraft.player.getUniqueID())) {
            publicButton.enabled = tileEntity.frequency.securityMode != SecurityMode.PUBLIC;
            privateButton.enabled = tileEntity.frequency.securityMode != SecurityMode.PRIVATE;
            trustedButton.enabled = tileEntity.frequency.securityMode != SecurityMode.TRUSTED;
            checkboxButton.enabled = true;
            overrideButton.enabled = true;
        } else {
            publicButton.enabled = false;
            privateButton.enabled = false;
            trustedButton.enabled = false;
            checkboxButton.enabled = false;
            overrideButton.enabled = false;
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        updateButtons();
        trustedField.updateCursorCounter();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        updateButtons();
        trustedField.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiSecurityDesk.png");
    }

    @Override
    public void keyTyped(char c, int i) throws IOException {
        if (!trustedField.isFocused() || i == GLFW.GLFW_KEY_ESCAPE) {
            super.keyTyped(c, i);
        }
        if (i == GLFW.GLFW_KEY_ENTER) {
            if (trustedField.isFocused()) {
                addTrusted(trustedField.getText());
                trustedField.setText("");
            }
        }
        if (SPECIAL_CHARS.contains(c) || Character.isDigit(c) || Character.isLetter(c) || isTextboxKey(c, i)) {
            trustedField.textboxKeyTyped(c, i);
        }
        updateButtons();
    }

    @Override
    protected void actionPerformed(Button guibutton) throws IOException {
        super.actionPerformed(guibutton);
        if (guibutton.id == removeButton.id) {
            int selection = scrollList.getSelection();
            if (tileEntity.frequency != null && selection != -1) {
                TileNetworkList data = TileNetworkList.withContents(1, tileEntity.frequency.trusted.get(selection));
                Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, data));
                scrollList.clearSelection();
            }
        } else if (guibutton.id == publicButton.id) {
            Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, TileNetworkList.withContents(3, 0)));
        } else if (guibutton.id == privateButton.id) {
            Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, TileNetworkList.withContents(3, 1)));
        } else if (guibutton.id == trustedButton.id) {
            Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, TileNetworkList.withContents(3, 2)));
        } else if (guibutton.id == checkboxButton.id) {
            addTrusted(trustedField.getText());
            trustedField.setText("");
        } else if (guibutton.id == overrideButton.id) {
            Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, TileNetworkList.withContents(2)));
        }
        updateButtons();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String ownerText = tileEntity.clientOwner != null ? (LangUtils.localize("gui.owner") + ": " + tileEntity.clientOwner) : EnumColor.RED + LangUtils.localize("gui.noOwner");
        font.drawString(tileEntity.getName(), (xSize / 2) - (font.getStringWidth(tileEntity.getName()) / 2), 4, 0x404040);
        font.drawString(ownerText, xSize - 7 - font.getStringWidth(ownerText), (ySize - 96) + 2, 0x404040);
        font.drawString(LangUtils.localize("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
        String trusted = LangUtils.localize("gui.trustedPlayers");
        font.drawString(trusted, 74 - (font.getStringWidth(trusted) / 2), 57, 0x787878);
        String security = EnumColor.RED + LangUtils.localize("gui.securityOffline");
        if (tileEntity.frequency != null) {
            security = LangUtils.localize("gui.security") + ": " + tileEntity.frequency.securityMode.getDisplay();
        }
        font.drawString(security, 13, 103, 0x404040);
        renderScaledText(LangUtils.localize("gui.add") + ":", 13, 70, 0x404040, 20);
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        if (tileEntity.frequency != null && overrideButton.isMouseOver()) {
            displayTooltip(LangUtils.localize("gui.securityOverride") + ": " + LangUtils.transOnOff(tileEntity.frequency.override), xAxis, yAxis);
        } else if (publicButton.isMouseOver()) {
            displayTooltip(LangUtils.localize("gui.publicMode"), xAxis, yAxis);
        } else if (privateButton.isMouseOver()) {
            displayTooltip(LangUtils.localize("gui.privateMode"), xAxis, yAxis);
        } else if (trustedButton.isMouseOver()) {
            displayTooltip(LangUtils.localize("gui.trustedMode"), xAxis, yAxis);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        if (tileEntity.frequency != null && tileEntity.ownerUUID != null && tileEntity.ownerUUID.equals(minecraft.player.getUniqueID())) {
            drawTexturedModalRect(guiLeft + 145, guiTop + 78, xSize + (tileEntity.frequency.override ? 0 : 6), 22, 6, 6);
        } else {
            drawTexturedModalRect(guiLeft + 145, guiTop + 78, xSize, 28, 6, 6);
        }
        trustedField.drawTextBox();
        MekanismRenderer.resetColor();
    }
}