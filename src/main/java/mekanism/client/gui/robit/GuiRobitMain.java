package mekanism.client.gui.robit;

import javax.annotation.Nonnull;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiRobitScreen;
import mekanism.client.gui.element.GuiSideHolder;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiHorizontalPowerBar;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.entity.robit.MainRobitContainer;
import mekanism.common.network.PacketGuiButtonPress;
import mekanism.common.network.PacketGuiButtonPress.ClickedEntityButton;
import mekanism.common.network.PacketRobit;
import mekanism.common.network.PacketRobit.RobitPacketType;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;

public class GuiRobitMain extends GuiMekanism<MainRobitContainer> {

    private final EntityRobit robit;

    private TextFieldWidget nameChangeField;
    private MekanismButton confirmName;

    public GuiRobitMain(MainRobitContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        robit = container.getEntity();
        dynamicSlots = true;
    }

    private void toggleNameChange() {
        nameChangeField.visible = !nameChangeField.visible;
        confirmName.visible = nameChangeField.visible;
        nameChangeField.setFocused2(nameChangeField.visible);
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
        addButton(new GuiSideHolder(this, 176, 6, 106));
        addButton(new GuiRobitScreen(this, 27, 16, 122, 56, () -> nameChangeField.visible));
        addButton(new GuiHorizontalPowerBar(this, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return EnergyDisplay.of(robit.getEnergy(), robit.getMaxEnergy()).getTextComponent();
            }

            @Override
            public double getLevel() {
                return robit.getEnergy() / robit.getMaxEnergy();
            }
        }, 27, 74, 120));
        addButton(confirmName = new TranslationButton(this, getGuiLeft() + 58, getGuiTop() + 47, 60, 20, MekanismLang.BUTTON_CONFIRM, this::changeName));
        confirmName.visible = false;

        addButton(nameChangeField = new TextFieldWidget(font, getGuiLeft() + 48, getGuiTop() + 21, 80, 12, ""));
        nameChangeField.setMaxStringLength(12);
        nameChangeField.setFocused2(true);
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
    public void resize(@Nonnull Minecraft minecraft, int scaledWidth, int scaledHeight) {
        String s = nameChangeField.getText();
        super.resize(minecraft, scaledWidth, scaledHeight);
        nameChangeField.setText(s);
    }

    @Override
    public boolean charTyped(char c, int keyCode) {
        if (nameChangeField.canWrite()) {
            return nameChangeField.charTyped(c, keyCode);
        }
        return super.charTyped(c, keyCode);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (nameChangeField.canWrite()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                //Manually handle hitting escape making the field lose focus
                nameChangeField.setFocused2(false);
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ENTER) {
                changeName();
                return true;
            }
            return nameChangeField.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(MekanismLang.ROBIT.translate(), 76, 6, 0x404040);
        if (!nameChangeField.visible) {
            CharSequence owner = robit.getOwnerName().length() > 14 ? robit.getOwnerName().subSequence(0, 14) : robit.getOwnerName();
            renderScaledText(MekanismLang.ROBIT_GREETING.translate(robit.getName()), 29, 18, 0x00CD00, 119);
            renderScaledText(MekanismLang.ENERGY.translate(EnergyDisplay.of(robit.getEnergy(), robit.getMaxEnergy())), 29, 36 - 4, 0x00CD00, 119);
            renderScaledText(MekanismLang.ROBIT_FOLLOWING.translate(robit.getFollowing()), 29, 45 - 4, 0x00CD00, 119);
            renderScaledText(MekanismLang.ROBIT_DROP_PICKUP.translate(robit.getDropPickup()), 29, 54 - 4, 0x00CD00, 119);
            renderScaledText(MekanismLang.ROBIT_OWNER.translate(owner), 29, 63 - 4, 0x00CD00, 119);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    public void tick() {
        super.tick();
        if (nameChangeField.visible) {
            nameChangeField.tick();
        }
    }
}