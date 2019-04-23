package mekanism.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.client.gui.element.GuiScrollList;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.api.TileNetworkList;
import mekanism.common.inventory.container.ContainerSecurityDesk;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.security.ISecurityTile.SecurityMode;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiSecurityDesk extends GuiMekanismTile<TileEntitySecurityDesk> {

    private static final List<Character> SPECIAL_CHARS = Arrays.asList('-', '|', '_');
    private static int MAX_LENGTH = 24;
    private GuiButton removeButton;
    private GuiScrollList scrollList;
    private GuiTextField trustedField;

    public GuiSecurityDesk(InventoryPlayer inventory, TileEntitySecurityDesk tile) {
        super(tile, new ContainerSecurityDesk(inventory, tile));
        addGuiElement(scrollList = new GuiScrollList(this, getGuiLocation(), 14, 14, 120, 4));
        ySize += 64;
    }

    @Override
    public void initGui() {
        super.initGui();
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        buttonList.clear();
        removeButton = new GuiButton(0, guiWidth + 13, guiHeight + 81, 122, 20, LangUtils.localize("gui.remove"));
        trustedField = new GuiTextField(1, fontRenderer, guiWidth + 35, guiHeight + 69, 86, 11);
        trustedField.setMaxStringLength(MAX_LENGTH);
        trustedField.setEnableBackgroundDrawing(false);
        updateButtons();
        buttonList.add(removeButton);
    }

    public void addTrusted(String trusted) {
        if (trusted.isEmpty()) {
            return;
        }
        TileNetworkList data = TileNetworkList.withContents(0, trusted);
        Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
    }

    public void updateButtons() {
        if (tileEntity.clientOwner == null) {
            return;
        }
        List<String> text = new ArrayList<>();
        if (tileEntity.frequency != null) {
            for (String s : tileEntity.frequency.trusted) {
                text.add(s);
            }
        }
        scrollList.setText(text);
        removeButton.enabled = scrollList.hasSelection();
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
        if (button == 0) {
            int xAxis = (mouseX - (width - xSize) / 2);
            int yAxis = (mouseY - (height - ySize) / 2);
            if (tileEntity.frequency != null && tileEntity.ownerUUID != null && tileEntity.clientOwner
                  .equals(mc.player.getName())) {
                if (xAxis >= 123 && xAxis <= 134 && yAxis >= 68 && yAxis <= 79) {
                    addTrusted(trustedField.getText());
                    trustedField.setText("");
                    SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                }
                TileNetworkList data = new TileNetworkList();
                if (xAxis >= 146 && xAxis <= 162 && yAxis >= 59 && yAxis <= 75) {
                    data.add(2);
                }
                if (tileEntity.frequency.securityMode != SecurityMode.PUBLIC) {
                    if (xAxis >= 13 && xAxis <= 53 && yAxis >= 113 && yAxis <= 129) {
                        data.add(3);
                        data.add(0);
                    }
                }
                if (tileEntity.frequency.securityMode != SecurityMode.PRIVATE) {
                    if (xAxis >= 54 && xAxis <= 94 && yAxis >= 113 && yAxis <= 129) {
                        data.add(3);
                        data.add(1);
                    }
                }
                if (tileEntity.frequency.securityMode != SecurityMode.TRUSTED) {
                    if (xAxis >= 95 && xAxis <= 135 && yAxis >= 113 && yAxis <= 129) {
                        data.add(3);
                        data.add(2);
                    }
                }
                if (!data.isEmpty()) {
                    Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
                }
            }
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiSecurityDesk.png");
    }

    @Override
    public void keyTyped(char c, int i) throws IOException {
        if (!trustedField.isFocused() || i == Keyboard.KEY_ESCAPE) {
            super.keyTyped(c, i);
        }
        if (i == Keyboard.KEY_RETURN) {
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
    protected void actionPerformed(GuiButton guibutton) throws IOException {
        super.actionPerformed(guibutton);
        if (guibutton.id == 0) {
            int selection = scrollList.getSelection();
            if (tileEntity.frequency != null && selection != -1) {
                TileNetworkList data = TileNetworkList.withContents(1, tileEntity.frequency.trusted.get(selection));
                Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
                scrollList.clearSelection();
            }
        }
        updateButtons();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String ownerText =
              tileEntity.clientOwner != null ? (LangUtils.localize("gui.owner") + ": " + tileEntity.clientOwner)
                    : EnumColor.RED + LangUtils.localize("gui.noOwner");
        fontRenderer
              .drawString(tileEntity.getName(), (xSize / 2) - (fontRenderer.getStringWidth(tileEntity.getName()) / 2),
                    4, 0x404040);
        fontRenderer
              .drawString(ownerText, (xSize - 7) - fontRenderer.getStringWidth(ownerText), (ySize - 96) + 2, 0x404040);
        fontRenderer.drawString(LangUtils.localize("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
        String trusted = LangUtils.localize("gui.trustedPlayers");
        fontRenderer.drawString(trusted, 74 - (fontRenderer.getStringWidth(trusted) / 2), 57, 0x787878);
        String security = EnumColor.RED + LangUtils.localize("gui.securityOffline");
        if (tileEntity.frequency != null) {
            security = LangUtils.localize("gui.security") + ": " + tileEntity.frequency.securityMode.getDisplay();
        }
        fontRenderer.drawString(security, 13, 103, 0x404040);
        renderScaledText(LangUtils.localize("gui.add") + ":", 13, 70, 0x404040, 20);
        int xAxis = (mouseX - (width - xSize) / 2);
        int yAxis = (mouseY - (height - ySize) / 2);
        if (tileEntity.frequency != null && xAxis >= 146 && xAxis <= 162 && yAxis >= 59 && yAxis <= 75) {
            displayTooltip(LangUtils.localize("gui.securityOverride") + ": " + LangUtils
                  .transOnOff(tileEntity.frequency.override), xAxis, yAxis);
        }
        if (xAxis >= 13 && xAxis <= 53 && yAxis >= 113 && yAxis <= 129) {
            displayTooltip(LangUtils.localize("gui.publicMode"), xAxis, yAxis);
        }
        if (xAxis >= 54 && xAxis <= 94 && yAxis >= 113 && yAxis <= 129) {
            displayTooltip(LangUtils.localize("gui.privateMode"), xAxis, yAxis);
        }
        if (xAxis >= 95 && xAxis <= 135 && yAxis >= 113 && yAxis <= 129) {
            displayTooltip(LangUtils.localize("gui.trustedMode"), xAxis, yAxis);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        mc.renderEngine.bindTexture(getGuiLocation());
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        int xAxis = (mouseX - (width - xSize) / 2);
        int yAxis = (mouseY - (height - ySize) / 2);
        if (tileEntity.frequency != null && tileEntity.clientOwner != null && mc.player.getName()
              .equals(tileEntity.clientOwner)) {
            drawTexturedModalRect(guiWidth + 145, guiHeight + 78, xSize + (tileEntity.frequency.override ? 0 : 6), 22,
                  6, 6);
            if (xAxis >= 146 && xAxis <= 162 && yAxis >= 59 && yAxis <= 75) {
                drawTexturedModalRect(guiWidth + 146, guiHeight + 59, xSize + 12, 0, 16, 16);
            } else {
                drawTexturedModalRect(guiWidth + 146, guiHeight + 59, xSize + 12, 16, 16, 16);
            }
            if (tileEntity.frequency.securityMode != SecurityMode.PUBLIC) {
                if (xAxis >= 13 && xAxis <= 53 && yAxis >= 113 && yAxis <= 129) {
                    drawTexturedModalRect(guiWidth + 13, guiHeight + 113, xSize, 48, 40, 16);
                } else {
                    drawTexturedModalRect(guiWidth + 13, guiHeight + 113, xSize, 64, 40, 16);
                }
            } else {
                drawTexturedModalRect(guiWidth + 13, guiHeight + 113, xSize, 80, 40, 16);
            }
            if (tileEntity.frequency.securityMode != SecurityMode.PRIVATE) {
                if (xAxis >= 54 && xAxis <= 94 && yAxis >= 113 && yAxis <= 129) {
                    drawTexturedModalRect(guiWidth + 54, guiHeight + 113, xSize + 40, 48, 40, 16);
                } else {
                    drawTexturedModalRect(guiWidth + 54, guiHeight + 113, xSize + 40, 64, 40, 16);
                }
            } else {
                drawTexturedModalRect(guiWidth + 54, guiHeight + 113, xSize + 40, 80, 40, 16);
            }
            if (tileEntity.frequency.securityMode != SecurityMode.TRUSTED) {
                if (xAxis >= 95 && xAxis <= 135 && yAxis >= 113 && yAxis <= 129) {
                    drawTexturedModalRect(guiWidth + 95, guiHeight + 113, xSize, 96, 40, 16);
                } else {
                    drawTexturedModalRect(guiWidth + 95, guiHeight + 113, xSize, 112, 40, 16);
                }
            } else {
                drawTexturedModalRect(guiWidth + 95, guiHeight + 113, xSize, 128, 40, 16);
            }
        } else {
            drawTexturedModalRect(guiWidth + 145, guiHeight + 78, xSize, 28, 6, 6);
            drawTexturedModalRect(guiWidth + 146, guiHeight + 59, xSize + 12, 32, 16, 16);
            drawTexturedModalRect(guiWidth + 13, guiHeight + 113, xSize, 80, 40, 16);
            drawTexturedModalRect(guiWidth + 54, guiHeight + 113, xSize + 40, 80, 40, 16);
            drawTexturedModalRect(guiWidth + 95, guiHeight + 113, xSize, 128, 40, 16);
        }
        if (xAxis >= 123 && xAxis <= 134 && yAxis >= 68 && yAxis <= 79) {
            drawTexturedModalRect(guiWidth + 123, guiHeight + 68, xSize, 0, 11, 11);
        } else {
            drawTexturedModalRect(guiWidth + 123, guiHeight + 68, xSize, 11, 11, 11);
        }
        super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
        trustedField.drawTextBox();
    }
}