package mekanism.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import mekanism.api.EnumColor;
import mekanism.api.TileNetworkList;
import mekanism.client.ClientTickHandler;
import mekanism.client.MekanismClient;
import mekanism.client.gui.button.GuiButtonDisableableImage;
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
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.inventory.container.ContainerTeleporter;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.network.PacketPortableTeleporter;
import mekanism.common.network.PacketPortableTeleporter.PortableTeleporterPacketType;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.security.IOwnerItem;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
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
public class GuiTeleporter extends GuiMekanismTile<TileEntityTeleporter> {

    private Hand currentHand;
    private ItemStack itemStack = ItemStack.EMPTY;
    private PlayerEntity entityPlayer;
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
    private final boolean isPortable;

    public GuiTeleporter(PlayerInventory inventory, TileEntityTeleporter tile) {
        super(tile, new ContainerTeleporter(inventory, tile));
        isPortable = false;
        ResourceLocation resource = getGuiLocation();
        addGuiElement(new GuiRedstoneControl(this, tileEntity, resource));
        addGuiElement(new GuiUpgradeTab(this, tileEntity, resource));
        addGuiElement(new GuiSecurityTab<>(this, tileEntity, resource));
        addGuiElement(new GuiPowerBar(this, new IPowerInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return TextComponentUtil.build(EnergyDisplay.of(getEnergy(), getMaxEnergy()));
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

    public GuiTeleporter(PlayerEntity player, Hand hand, ItemStack stack) {
        super(null, new ContainerNull());
        isPortable = true;
        currentHand = hand;
        itemStack = stack;
        entityPlayer = player;
        ResourceLocation resource = getGuiLocation();
        addGuiElement(new GuiPowerBar(this, new IPowerInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return TextComponentUtil.build(EnergyDisplay.of(getEnergy(), getMaxEnergy()));
            }

            @Override
            public double getLevel() {
                return getEnergy() / getMaxEnergy();
            }
        }, resource, 158, 26));
        addGuiElement(scrollList = new GuiScrollList(this, resource, 28, 37, 120, 4));
        ItemPortableTeleporter item = (ItemPortableTeleporter) itemStack.getItem();
        if (item.getFrequency(stack) != null) {
            privateMode = !item.getFrequency(stack).publicFreq;
            setFrequency(item.getFrequency(stack).name);
        } else {
            Mekanism.packetHandler.sendToServer(new PacketPortableTeleporter(PortableTeleporterPacketType.DATA_REQUEST, currentHand, clientFreq));
        }
        ySize = 175;
    }

    @Override
    public void init() {
        super.init();
        buttons.clear();
        buttons.add(publicButton = new Button(guiLeft + 27, guiTop + 14, 60, 20, LangUtils.localize("gui.public"),
              onPress -> {
                  privateMode = false;
                  updateButtons();
              }));
        buttons.add(privateButton = new Button(guiLeft + 89, guiTop + 14, 60, 20, LangUtils.localize("gui.private"),
              onPress -> {
                  privateMode = true;
                  updateButtons();
              }));
        buttons.add(setButton = new Button(guiLeft + 27, guiTop + 116, 60, 20, LangUtils.localize("gui.set"),
              onPress -> {
                  int selection = scrollList.getSelection();
                  if (selection != -1) {
                      Frequency freq = privateMode ? getPrivateCache().get(selection) : getPublicCache().get(selection);
                      setFrequency(freq.name);
                  }
                  updateButtons();
              }));
        buttons.add(deleteButton = new Button(guiLeft + 89, guiTop + 116, 60, 20, LangUtils.localize("gui.delete"),
              onPress -> {
                  int selection = scrollList.getSelection();
                  if (selection != -1) {
                      Frequency freq = privateMode ? getPrivateCache().get(selection) : getPublicCache().get(selection);
                      if (tileEntity != null) {
                          TileNetworkList data = TileNetworkList.withContents(1, freq.name, freq.publicFreq);
                          Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, data));
                      } else {
                          Mekanism.packetHandler.sendToServer(new PacketPortableTeleporter(PortableTeleporterPacketType.DEL_FREQ, currentHand, freq));
                          Mekanism.packetHandler.sendToServer(new PacketPortableTeleporter(PortableTeleporterPacketType.DATA_REQUEST, currentHand, null));
                      }
                      scrollList.clearSelection();
                  }
                  updateButtons();
              }));
        if (!itemStack.isEmpty()) {
            buttons.add(teleportButton = new Button(guiLeft + 42, guiTop + 140, 92, 20, LangUtils.localize("gui.teleport"),
                  onPress -> {
                      if (clientFreq != null && clientStatus == 1) {
                          minecraft.mainWindow.setIngameFocus();
                          ClientTickHandler.portableTeleport(entityPlayer, currentHand, clientFreq);
                      }
                      updateButtons();
                  }));
        }
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
        if (!itemStack.isEmpty()) {
            if (!isInit) {
                Mekanism.packetHandler.sendToServer(new PacketPortableTeleporter(PortableTeleporterPacketType.DATA_REQUEST, currentHand, clientFreq));
            } else {
                isInit = false;
            }
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

    public String getSecurity(Frequency freq) {
        return !freq.publicFreq ? EnumColor.DARK_RED + LangUtils.localize("gui.private") : LangUtils.localize("gui.public");
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
    public void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        updateButtons();
        frequencyField.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, isPortable ? "GuiPortableTeleporter.png" : "GuiTeleporter.png");
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
        drawString(getName(), (xSize / 2) - (font.getStringWidth(getName()) / 2), 4, 0x404040);
        drawString(LangUtils.localize("gui.owner") + ": " + (getOwnerUsername() != null ? getOwnerUsername() : LangUtils.localize("gui.none")),
              8, !itemStack.isEmpty() ? ySize - 12 : (ySize - 96) + 4, 0x404040);
        drawString(LangUtils.localize("gui.freq") + ":", 32, 81, 0x404040);
        drawString(LangUtils.localize("gui.security") + ":", 32, 91, 0x404040);
        drawString(" " + (getFrequency() != null ? getFrequency().name : EnumColor.DARK_RED + LangUtils.localize("gui.none")),
              32 + font.getStringWidth(LangUtils.localize("gui.freq") + ":"), 81, 0x797979);
        drawString(" " + (getFrequency() != null ? getSecurity(getFrequency()) : EnumColor.DARK_RED + LangUtils.localize("gui.none")),
              32 + font.getStringWidth(LangUtils.localize("gui.security") + ":"), 91, 0x797979);
        String str = LangUtils.localize("gui.set") + ":";
        renderScaledText(str, 27, 104, 0x404040, 20);
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        if (xAxis >= 6 && xAxis <= 24 && yAxis >= 6 && yAxis <= 24) {
            if (getFrequency() == null) {
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
        if (tileEntity != null) {
            return tileEntity.getSecurity().getOwnerUUID();
        }
        return ((IOwnerItem) itemStack.getItem()).getOwnerUUID(itemStack);
    }

    private String getOwnerUsername() {
        if (tileEntity != null) {
            return tileEntity.getSecurity().getClientOwner();
        }
        return MekanismClient.clientUUIDMap.get(((IOwnerItem) itemStack.getItem()).getOwnerUUID(itemStack));
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
        if (tileEntity != null) {
            TileNetworkList data = TileNetworkList.withContents(0, freq, !privateMode);
            Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, data));
        } else {
            Frequency newFreq = new Frequency(freq, null).setPublic(!privateMode);
            Mekanism.packetHandler.sendToServer(new PacketPortableTeleporter(PortableTeleporterPacketType.SET_FREQ, currentHand, newFreq));
        }
    }

    private String getName() {
        return tileEntity != null ? tileEntity.getName() : itemStack.getDisplayName();
    }

    private double getEnergy() {
        if (!itemStack.isEmpty()) {
            return ((ItemPortableTeleporter) itemStack.getItem()).getEnergy(itemStack);
        }
        return tileEntity.getEnergy();
    }

    private double getMaxEnergy() {
        if (!itemStack.isEmpty()) {
            return ((ItemPortableTeleporter) itemStack.getItem()).getMaxEnergy(itemStack);
        }
        return tileEntity.getMaxEnergy();
    }

    public boolean isStackEmpty() {
        return itemStack.isEmpty();
    }
}