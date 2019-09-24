package mekanism.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mekanism.api.TileNetworkList;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.button.MekanismButton;
import mekanism.client.gui.button.MekanismImageButton;
import mekanism.client.gui.button.TranslationButton;
import mekanism.client.gui.element.GuiScrollList;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.tile.SecurityDeskContainer;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.security.ISecurityTile.SecurityMode;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import mekanism.common.util.text.OwnerDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class GuiSecurityDesk extends GuiMekanismTile<TileEntitySecurityDesk, SecurityDeskContainer> {

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

    public GuiSecurityDesk(SecurityDeskContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        ySize += 64;
    }

    @Override
    public void init() {
        super.init();
        addButton(scrollList = new GuiScrollList(this, getGuiLocation(), 14, 14, 120, 40));

        addButton(removeButton = new TranslationButton(this, guiLeft + 13, guiTop + 81, 122, 20, "gui.mekanism.remove", () -> {
            int selection = scrollList.getSelection();
            if (tileEntity.frequency != null && selection != -1) {
                TileNetworkList data = TileNetworkList.withContents(1, tileEntity.frequency.trusted.get(selection));
                Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, data));
                scrollList.clearSelection();
            }
            updateButtons();
        }));
        addButton(trustedField = new TextFieldWidget(font, guiLeft + 35, guiTop + 69, 86, 11, ""));
        trustedField.setMaxStringLength(MAX_LENGTH);
        trustedField.setEnableBackgroundDrawing(false);
        addButton(publicButton = new MekanismImageButton(this, guiLeft + 13, guiTop + 113, 40, 16, 40, 16, getButtonLocation("public"),
              () -> {
                  Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, TileNetworkList.withContents(3, SecurityMode.PUBLIC)));
                  updateButtons();
              },
              getOnHover("gui.mekanism.publicMode")));
        addButton(privateButton = new MekanismImageButton(this, guiLeft + 54, guiTop + 113, 40, 16, 40, 16, getButtonLocation("private"),
              () -> {
                  Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, TileNetworkList.withContents(3, SecurityMode.PRIVATE)));
                  updateButtons();
              },
              getOnHover("gui.mekanism.privateMode")));
        addButton(trustedButton = new MekanismImageButton(this, guiLeft + 95, guiTop + 113, 40, 16, 40, 16, getButtonLocation("trusted"),
              () -> {
                  Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, TileNetworkList.withContents(3, SecurityMode.TRUSTED)));
                  updateButtons();
              },
              getOnHover("gui.mekanism.trustedMode")));
        addButton(checkboxButton = new MekanismImageButton(this, guiLeft + 123, guiTop + 68, 11, 12, getButtonLocation("checkmark"),
              () -> {
                  addTrusted(trustedField.getText());
                  trustedField.setText("");
                  updateButtons();
              }));
        addButton(overrideButton = new MekanismImageButton(this, guiLeft + 146, guiTop + 59, 16, 16, getButtonLocation("exclamation"),
              () -> {
                  Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, TileNetworkList.withContents(2)));
                  updateButtons();
              },
              (onHover, xAxis, yAxis) -> {
                  if (tileEntity.frequency != null) {
                      displayTooltip(TextComponentUtil.build(Translation.of("gui.mekanism.securityOverride"), ": ", OnOff.of(tileEntity.frequency.override)), xAxis, yAxis);
                  }
              }));
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
            removeButton.active = scrollList.hasSelection();
        }

        if (tileEntity.frequency != null && tileEntity.ownerUUID != null && tileEntity.ownerUUID.equals(minecraft.player.getUniqueID())) {
            publicButton.active = tileEntity.frequency.securityMode != SecurityMode.PUBLIC;
            privateButton.active = tileEntity.frequency.securityMode != SecurityMode.PRIVATE;
            trustedButton.active = tileEntity.frequency.securityMode != SecurityMode.TRUSTED;
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
    public boolean charTyped(char c, int i) {
        boolean returnValue = false;
        if (!trustedField.isFocused() || i == GLFW.GLFW_KEY_ESCAPE) {
            returnValue = super.charTyped(c, i);
        } else if (i == GLFW.GLFW_KEY_ENTER && trustedField.isFocused()) {
            addTrusted(trustedField.getText());
            trustedField.setText("");
            returnValue = true;
        } else if (SPECIAL_CHARS.contains(c) || Character.isDigit(c) || Character.isLetter(c) || isTextboxKey(c, i)) {
            returnValue = trustedField.charTyped(c, i);
        }
        updateButtons();
        return returnValue;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tileEntity.getName(), (xSize / 2) - (getStringWidth(tileEntity.getName()) / 2), 4, 0x404040);
        ITextComponent ownerComponent = OwnerDisplay.of(tileEntity.ownerUUID, tileEntity.clientOwner).getTextComponent();
        drawString(ownerComponent, xSize - 7 - getStringWidth(ownerComponent), (ySize - 96) + 2, 0x404040);
        drawString(TextComponentUtil.translate("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
        drawCenteredText(TextComponentUtil.translate("gui.mekanism.trustedPlayers"), 74, 57, 0x787878);
        //TODO: 1.14 Convert to GuiElement
        if (tileEntity.frequency != null) {
            drawString(TextComponentUtil.build(Translation.of("gui.mekanism.security"), ": ", tileEntity.frequency.securityMode), 13, 103, 0x404040);
        } else {
            drawString(TextComponentUtil.build(EnumColor.RED, Translation.of("gui.mekanism.securityOffline")), 13, 103, 0x404040);
        }
        renderScaledText(TextComponentUtil.build(Translation.of("gui.mekanism.add"), ":"), 13, 70, 0x404040, 20);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        if (tileEntity.frequency != null && tileEntity.ownerUUID != null && tileEntity.ownerUUID.equals(minecraft.player.getUniqueID())) {
            drawTexturedRect(guiLeft + 145, guiTop + 78, xSize + (tileEntity.frequency.override ? 0 : 6), 22, 6, 6);
        } else {
            drawTexturedRect(guiLeft + 145, guiTop + 78, xSize, 28, 6, 6);
        }
        MekanismRenderer.resetColor();
    }
}