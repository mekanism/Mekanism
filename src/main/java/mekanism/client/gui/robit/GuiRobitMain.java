package mekanism.client.gui.robit;

import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiHorizontalPowerBar;
import mekanism.client.gui.element.tab.GuiRobitTab;
import mekanism.client.render.MekanismRenderer;
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
        addButton(new GuiRobitTab(this));
        addButton(new GuiInnerScreen(this, 27, 16, 122, 56));
        addButton(new GuiHorizontalPowerBar(this, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return EnergyDisplay.of(robit.getEnergy(), robit.MAX_ELECTRICITY).getTextComponent();
            }

            @Override
            public double getLevel() {
                return robit.getEnergy() / robit.MAX_ELECTRICITY;
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
        }));
        addButton(new MekanismImageButton(this, getGuiLeft() + 179, getGuiTop() + 30, 18, getButtonLocation("crafting"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedEntityButton.ROBIT_CRAFTING, robit.getEntityId()))));
        addButton(new MekanismImageButton(this, getGuiLeft() + 179, getGuiTop() + 50, 18, getButtonLocation("inventory"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedEntityButton.ROBIT_INVENTORY, robit.getEntityId()))));
        addButton(new MekanismImageButton(this, getGuiLeft() + 179, getGuiTop() + 70, 18, getButtonLocation("smelting"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedEntityButton.ROBIT_SMELTING, robit.getEntityId()))));
        addButton(new MekanismImageButton(this, getGuiLeft() + 179, getGuiTop() + 90, 18, getButtonLocation("repair"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedEntityButton.ROBIT_REPAIR, robit.getEntityId()))));
    }

    @Override
    public boolean charTyped(char c, int i) {
        //TODO: FIXME
        if (!nameChangeField.visible) {
            return super.charTyped(c, i);
        }
        if (i == GLFW.GLFW_KEY_ENTER) {
            changeName();
            return true;
        } else if (i == GLFW.GLFW_KEY_ESCAPE) {
            minecraft.player.closeScreen();
            return true;
        }
        return nameChangeField.charTyped(c, i);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(MekanismLang.ROBIT.translate(), 76, 6, 0x404040);
        if (!nameChangeField.visible) {
            CharSequence owner = robit.getOwnerName().length() > 14 ? robit.getOwnerName().subSequence(0, 14) : robit.getOwnerName();
            drawString(MekanismLang.ROBIT_GREETING.translate(robit.getName()), 29, 18, 0x00CD00);
            drawString(MekanismLang.ENERGY.translate(EnergyDisplay.of(robit.getEnergy(), robit.MAX_ELECTRICITY)), 29, 36 - 4, 0x00CD00);
            drawString(MekanismLang.ROBIT_FOLLOWING.translate(robit.getFollowing()), 29, 45 - 4, 0x00CD00);
            drawString(MekanismLang.ROBIT_DROP_PICKUP.translate(robit.getDropPickup()), 29, 54 - 4, 0x00CD00);
            drawString(MekanismLang.ROBIT_OWNER.translate(owner), 29, 63 - 4, 0x00CD00);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        if (nameChangeField.visible) {
            drawTexturedRect(getGuiLeft() + 28, getGuiTop() + 17, 0, 166 + 4, 120, 54);
            MekanismRenderer.resetColor();
        }
    }

    @Override
    public void tick() {
        super.tick();
        nameChangeField.tick();
    }

    /*@Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "robit_main.png");
    }*/
}