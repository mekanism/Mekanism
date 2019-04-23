package mekanism.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.client.gui.element.GuiScrollList;
import mekanism.client.gui.element.GuiSideConfigurationTab;
import mekanism.client.gui.element.GuiTransporterConfigTab;
import mekanism.client.gui.element.GuiUpgradeTab;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.api.TileNetworkList;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.inventory.container.ContainerQuantumEntangloporter;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
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
public class GuiQuantumEntangloporter extends GuiMekanismTile<TileEntityQuantumEntangloporter> {

    private GuiButton publicButton;
    private GuiButton privateButton;
    private GuiButton setButton;
    private GuiButton deleteButton;
    private GuiScrollList scrollList;
    private GuiTextField frequencyField;
    private boolean privateMode;

    public GuiQuantumEntangloporter(InventoryPlayer inventory, TileEntityQuantumEntangloporter tile) {
        super(tile, new ContainerQuantumEntangloporter(inventory, tile));
        ResourceLocation resource = getGuiLocation();
        addGuiElement(scrollList = new GuiScrollList(this, resource, 28, 37, 120, 4));
        addGuiElement(new GuiSideConfigurationTab(this, tileEntity, resource));
        addGuiElement(new GuiTransporterConfigTab(this, 34, tileEntity, resource));
        addGuiElement(new GuiUpgradeTab(this, tileEntity, resource));
        if (tileEntity.frequency != null) {
            privateMode = !tileEntity.frequency.publicFreq;
        }
        ySize += 64;
    }

    @Override
    public void initGui() {
        super.initGui();
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        buttonList.clear();
        publicButton = new GuiButton(0, guiWidth + 27, guiHeight + 14, 60, 20, LangUtils.localize("gui.public"));
        privateButton = new GuiButton(1, guiWidth + 89, guiHeight + 14, 60, 20, LangUtils.localize("gui.private"));
        setButton = new GuiButton(2, guiWidth + 27, guiHeight + 116, 60, 20, LangUtils.localize("gui.set"));
        deleteButton = new GuiButton(3, guiWidth + 89, guiHeight + 116, 60, 20, LangUtils.localize("gui.delete"));
        frequencyField = new GuiTextField(4, fontRenderer, guiWidth + 50, guiHeight + 104, 86, 11);
        frequencyField.setMaxStringLength(FrequencyManager.MAX_FREQ_LENGTH);
        frequencyField.setEnableBackgroundDrawing(false);
        updateButtons();
        buttonList.add(publicButton);
        buttonList.add(privateButton);
        buttonList.add(setButton);
        buttonList.add(deleteButton);
    }

    public void setFrequency(String freq) {
        if (freq.isEmpty()) {
            return;
        }
        TileNetworkList data = TileNetworkList.withContents(0, freq, !privateMode);
        Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
    }

    public String getSecurity(Frequency freq) {
        return !freq.publicFreq ? EnumColor.DARK_RED + LangUtils.localize("gui.private")
              : LangUtils.localize("gui.public");
    }

    public void updateButtons() {
        if (tileEntity.getSecurity().getClientOwner() == null) {
            return;
        }
        List<String> text = new ArrayList<>();
        if (privateMode) {
            for (Frequency freq : tileEntity.privateCache) {
                text.add(freq.name);
            }
        } else {
            for (Frequency freq : tileEntity.publicCache) {
                text.add(freq.name + " (" + freq.clientOwner + ")");
            }
        }
        scrollList.setText(text);
        if (privateMode) {
            publicButton.enabled = true;
            privateButton.enabled = false;
        } else {
            publicButton.enabled = false;
            privateButton.enabled = true;
        }
        if (scrollList.hasSelection()) {
            Frequency freq = privateMode ? tileEntity.privateCache.get(scrollList.getSelection())
                  : tileEntity.publicCache.get(scrollList.getSelection());
            setButton.enabled = tileEntity.getFrequency(null) == null || !tileEntity.getFrequency(null).equals(freq);
            deleteButton.enabled = tileEntity.getSecurity().getOwnerUUID().equals(freq.ownerUUID);
        } else {
            setButton.enabled = false;
            deleteButton.enabled = false;
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        updateButtons();
        frequencyField.updateCursorCounter();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        updateButtons();
        frequencyField.mouseClicked(mouseX, mouseY, button);
        if (button == 0) {
            int xAxis = (mouseX - (width - xSize) / 2);
            int yAxis = (mouseY - (height - ySize) / 2);
            if (xAxis >= 137 && xAxis <= 148 && yAxis >= 103 && yAxis <= 114) {
                setFrequency(frequencyField.getText());
                frequencyField.setText("");
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
            }
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiTeleporter.png");
    }

    @Override
    public void keyTyped(char c, int i) throws IOException {
        if (!frequencyField.isFocused() || i == Keyboard.KEY_ESCAPE) {
            super.keyTyped(c, i);
        }
        if (i == Keyboard.KEY_RETURN) {
            if (frequencyField.isFocused()) {
                setFrequency(frequencyField.getText());
                frequencyField.setText("");
            }
        }
        if (Character.isDigit(c) || Character.isLetter(c) || isTextboxKey(c, i) ||
              FrequencyManager.SPECIAL_CHARS.contains(c)) {
            frequencyField.textboxKeyTyped(c, i);
        }
        updateButtons();
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) throws IOException {
        super.actionPerformed(guibutton);
        if (guibutton.id == 0) {
            privateMode = false;
        } else if (guibutton.id == 1) {
            privateMode = true;
        } else if (guibutton.id == 2) {
            int selection = scrollList.getSelection();
            if (selection != -1) {
                Frequency freq =
                      privateMode ? tileEntity.privateCache.get(selection) : tileEntity.publicCache.get(selection);
                setFrequency(freq.name);
            }
        } else if (guibutton.id == 3) {
            int selection = scrollList.getSelection();
            if (selection != -1) {
                Frequency freq =
                      privateMode ? tileEntity.privateCache.get(selection) : tileEntity.publicCache.get(selection);
                TileNetworkList data = TileNetworkList.withContents(1, freq.name, freq.publicFreq);
                Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
                scrollList.clearSelection();
            }
        }
        updateButtons();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer
              .drawString(tileEntity.getName(), (xSize / 2) - (fontRenderer.getStringWidth(tileEntity.getName()) / 2),
                    4, 0x404040);
        fontRenderer.drawString(
              LangUtils.localize("gui.owner") + ": " + (tileEntity.getSecurity().getClientOwner() != null ? tileEntity
                    .getSecurity().getClientOwner() : LangUtils.localize("gui.none")), 8, (ySize - 96) + 4, 0x404040);
        fontRenderer.drawString(LangUtils.localize("gui.freq") + ":", 32, 81, 0x404040);
        fontRenderer.drawString(LangUtils.localize("gui.security") + ":", 32, 91, 0x404040);
        fontRenderer.drawString(" " + (tileEntity.getFrequency(null) != null ? tileEntity.getFrequency(null).name
                    : EnumColor.DARK_RED + LangUtils.localize("gui.none")),
              32 + fontRenderer.getStringWidth(LangUtils.localize("gui.freq") + ":"), 81, 0x797979);
        fontRenderer.drawString(
              " " + (tileEntity.getFrequency(null) != null ? getSecurity(tileEntity.getFrequency(null))
                    : EnumColor.DARK_RED + LangUtils.localize("gui.none")),
              32 + fontRenderer.getStringWidth(LangUtils.localize("gui.security") + ":"), 91, 0x797979);
        String str = LangUtils.localize("gui.set") + ":";
        renderScaledText(str, 27, 104, 0x404040, 20);
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
        if (xAxis >= 137 && xAxis <= 148 && yAxis >= 103 && yAxis <= 114) {
            drawTexturedModalRect(guiWidth + 137, guiHeight + 103, xSize, 0, 11, 11);
        } else {
            drawTexturedModalRect(guiWidth + 137, guiHeight + 103, xSize, 11, 11, 11);
        }
        super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
        frequencyField.drawTextBox();
    }
}