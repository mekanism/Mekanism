package mekanism.client.gui.robit;

import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiSideHolder;
import mekanism.client.gui.element.bar.GuiHorizontalPowerBar;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.custom.GuiRobitScreen;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.entity.robit.MainRobitContainer;
import mekanism.common.network.PacketGuiButtonPress;
import mekanism.common.network.PacketGuiButtonPress.ClickedEntityButton;
import mekanism.common.network.PacketRobit;
import mekanism.common.network.PacketRobit.RobitPacketType;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiRobitMain extends GuiMekanism<MainRobitContainer> {

    private final EntityRobit robit;

    private GuiTextField nameChangeField;
    private MekanismButton confirmName;

    public GuiRobitMain(MainRobitContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        robit = container.getEntity();
        dynamicSlots = true;
    }

    private void toggleNameChange() {
        nameChangeField.visible = !nameChangeField.visible;
        confirmName.visible = nameChangeField.visible;
        nameChangeField.setFocused(nameChangeField.visible);
    }

    private void changeName() {
        if (!nameChangeField.getText().isEmpty()) {
            Mekanism.packetHandler.sendToServer(new PacketRobit(robit.getEntityId(), TextComponentUtil.getString(nameChangeField.getText())));
            toggleNameChange();
            nameChangeField.setText("");
        }
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiSideHolder(this, 176, 6, 106, false));
        addButton(new GuiRobitScreen(this, 27, 16, 122, 56, () -> nameChangeField.visible));
        addButton(new GuiHorizontalPowerBar(this, robit.getEnergyContainer(), 27, 74, 120));
        addButton(confirmName = new TranslationButton(this, getGuiLeft() + 58, getGuiTop() + 47, 60, 20, MekanismLang.BUTTON_CONFIRM, this::changeName));
        confirmName.visible = false;

        addButton(nameChangeField = new GuiTextField(this, 48, 21, 80, 12));
        nameChangeField.setMaxStringLength(12);
        nameChangeField.setFocused(true);
        nameChangeField.setEnterHandler(this::changeName);
        nameChangeField.visible = false;

        addButton(new MekanismImageButton(this, getGuiLeft() + 6, getGuiTop() + 16, 18, getButtonLocation("home"), () -> {
            Mekanism.packetHandler.sendToServer(new PacketRobit(RobitPacketType.GO_HOME, robit.getEntityId()));
            minecraft.displayGuiScreen(null);
        }, getOnHover(MekanismLang.ROBIT_TELEPORT)));
        addButton(new MekanismImageButton(this, getGuiLeft() + 6, getGuiTop() + 35, 18, getButtonLocation("drop"),
              () -> Mekanism.packetHandler.sendToServer(new PacketRobit(RobitPacketType.DROP_PICKUP, robit.getEntityId())),
              getOnHover(MekanismLang.ROBIT_TOGGLE_PICKUP)));
        addButton(new MekanismImageButton(this, getGuiLeft() + 6, getGuiTop() + 54, 18, getButtonLocation("rename"),
              this::toggleNameChange, getOnHover(MekanismLang.ROBIT_RENAME)));
        addButton(new MekanismImageButton(this, getGuiLeft() + 152, getGuiTop() + 54, 18, getButtonLocation("follow"),
              () -> Mekanism.packetHandler.sendToServer(new PacketRobit(RobitPacketType.FOLLOW, robit.getEntityId())),
              getOnHover(MekanismLang.ROBIT_TOGGLE_FOLLOW)));
        addButton(new MekanismImageButton(this, getGuiLeft() + 179, getGuiTop() + 10, 18, getButtonLocation("main"), () -> {
            //Clicking main button doesn't do anything while already on the main GUI
        }, getOnHover(MekanismLang.ROBIT)));
        addButton(new MekanismImageButton(this, getGuiLeft() + 179, getGuiTop() + 30, 18, getButtonLocation("crafting"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedEntityButton.ROBIT_CRAFTING, robit.getEntityId())),
              getOnHover(MekanismLang.ROBIT_CRAFTING)));
        addButton(new MekanismImageButton(this, getGuiLeft() + 179, getGuiTop() + 50, 18, getButtonLocation("inventory"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedEntityButton.ROBIT_INVENTORY, robit.getEntityId())),
              getOnHover(MekanismLang.ROBIT_INVENTORY)));
        addButton(new MekanismImageButton(this, getGuiLeft() + 179, getGuiTop() + 70, 18, getButtonLocation("smelting"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedEntityButton.ROBIT_SMELTING, robit.getEntityId())),
              getOnHover(MekanismLang.ROBIT_SMELTING)));
        addButton(new MekanismImageButton(this, getGuiLeft() + 179, getGuiTop() + 90, 18, getButtonLocation("repair"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedEntityButton.ROBIT_REPAIR, robit.getEntityId())),
              getOnHover(MekanismLang.ROBIT_REPAIR)));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(MekanismLang.ROBIT.translate(), 76, 6, titleTextColor());
        if (!nameChangeField.visible) {
            CharSequence owner = robit.getOwnerName().length() > 14 ? robit.getOwnerName().subSequence(0, 14) : robit.getOwnerName();
            drawTextScaledBound(MekanismLang.ROBIT_GREETING.translate(robit.getName()), 29, 18, screenTextColor(), 119);
            drawTextScaledBound(MekanismLang.ENERGY.translate(EnergyDisplay.of(robit.getEnergyContainer().getEnergy(), robit.getEnergyContainer().getMaxEnergy())), 29, 36 - 4, screenTextColor(), 119);
            drawTextScaledBound(MekanismLang.ROBIT_FOLLOWING.translate(robit.getFollowing()), 29, 45 - 4, screenTextColor(), 119);
            drawTextScaledBound(MekanismLang.ROBIT_DROP_PICKUP.translate(robit.getDropPickup()), 29, 54 - 4, screenTextColor(), 119);
            drawTextScaledBound(MekanismLang.ROBIT_OWNER.translate(owner), 29, 63 - 4, screenTextColor(), 119);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}